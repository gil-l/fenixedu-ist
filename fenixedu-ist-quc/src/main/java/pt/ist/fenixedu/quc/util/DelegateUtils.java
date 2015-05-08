/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST QUC.
 *
 * FenixEdu IST QUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST QUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST QUC.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.quc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.CurricularYear;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.DegreeModuleScope;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.bennu.core.domain.User;
import org.joda.time.Interval;

import pt.ist.fenixedu.delegates.domain.student.CycleDelegate;
import pt.ist.fenixedu.delegates.domain.student.DegreeDelegate;
import pt.ist.fenixedu.delegates.domain.student.Delegate;
import pt.ist.fenixedu.delegates.domain.student.YearDelegate;
import pt.ist.fenixedu.quc.domain.InquiriesRoot;

public class DelegateUtils {

    public static boolean DelegateIsActiveForFirstExecutionYear(Delegate delegate, ExecutionYear executionYear) {
        Interval interval = new Interval(delegate.getStart(), delegate.getEnd());
        return executionYear.overlapsInterval(interval);
    }

    public static YearDelegate getLastYearDelegateByExecutionYearAndCurricularYear(Degree degree, ExecutionYear executionYear,
            CurricularYear curricularYear) {
        List<YearDelegate> yearDelegates =
                degree.getDelegateSet().stream().filter(d -> d instanceof YearDelegate).map(d -> (YearDelegate) d)
                        .collect(Collectors.toList());

        YearDelegate lastDelegateFunction = null;
        for (YearDelegate yearDelegate : yearDelegates) {
            if (yearDelegate.getCurricularYear().equals(curricularYear)
                    && yearDelegate.getInterval().overlaps(
                            new Interval(executionYear.getBeginDateYearMonthDay().toDateTimeAtMidnight(), executionYear
                                    .getEndDateYearMonthDay().toDateTimeAtMidnight()))) {
                if (lastDelegateFunction == null || lastDelegateFunction.getEnd().isBefore(yearDelegate.getEnd())) {
                    lastDelegateFunction = yearDelegate;
                }
            }
        }
        return lastDelegateFunction;
    }

    private static void addIfNecessaryExecutionCoursesFromOtherYears(YearDelegate yearDelegate,
            final ExecutionSemester executionSemester, ExecutionDegree executionDegree, final Set<ExecutionCourse> result) {
        final Degree degree = yearDelegate.getDegree();
        //final CycleType currentCycleType = getRegistration().getCurrentCycleType(); //TODO to pass EC to degree and master delegates
        final Student student = yearDelegate.getUser().getPerson().getStudent();
        final Delegate degreeDelegateFunction =
                getActiveDelegateByStudent(degree, student, executionSemester.getExecutionYear(), true);

        if (degreeDelegateFunction != null) {
            addExecutionCoursesForOtherYears(yearDelegate, executionSemester, executionDegree, degree, student, result);
        }
    }

    public static void addExecutionCoursesForOtherYears(YearDelegate yearDelegate, ExecutionSemester executionPeriod,
            ExecutionDegree executionDegree, Degree degree, Student student, Set<ExecutionCourse> executionCoursesToInquiries) {
        List<YearDelegate> otherYearDelegates = new ArrayList<YearDelegate>();
        for (User user : getAllActiveDelegatesByType(degree, true, null)) {
            if (user.getPerson().getStudent() != student) {
                YearDelegate otherYearDelegate = null;
                for (Delegate delegate : user.getDelegatesSet()) {
                    if (delegate instanceof YearDelegate) {
                        if (DelegateUtils.DelegateIsActiveForFirstExecutionYear(delegate, executionPeriod.getExecutionYear())) {
                            if (otherYearDelegate == null || delegate.getEnd().isAfter(otherYearDelegate.getEnd())) {
                                otherYearDelegate = (YearDelegate) delegate;
                            }
                        }
                    }
                }
                if (otherYearDelegate != null) {
                    otherYearDelegates.add(otherYearDelegate);
                }
            }
        }

        List<DegreeCurricularPlan> dcps = degree.getDegreeCurricularPlansForYear(executionPeriod.getExecutionYear());

        if (!dcps.isEmpty()) {
            for (int iter = 1; iter <= dcps.get(0).getDurationInYears(); iter++) {
                YearDelegate yearDelegateForYear = getYearDelegate(otherYearDelegates, iter);
                if (yearDelegateForYear == null) {
                    executionCoursesToInquiries.addAll(DelegateUtils.getExecutionCoursesToInquiries(yearDelegate,
                            executionPeriod, executionDegree, iter));
                }
            }
        }
    }

    public static Set<ExecutionCourse> getExecutionCoursesToInquiries(YearDelegate yearDelegate,
            ExecutionSemester executionSemester, ExecutionDegree executionDegree) {

        final Set<ExecutionCourse> result = new TreeSet<ExecutionCourse>(ExecutionCourse.EXECUTION_COURSE_NAME_COMPARATOR);
        for (ExecutionCourse executionCourse : getDelegatedExecutionCourses(yearDelegate, executionSemester)) {
            if (InquiriesRoot.getAvailableForInquiries(executionCourse) && !executionCourse.getInquiryResultsSet().isEmpty()
                    && executionCourse.hasAnyEnrolment(executionDegree)) {
                result.add(executionCourse);
            }
        }

        addIfNecessaryExecutionCoursesFromOtherYears(yearDelegate, executionSemester, executionDegree, result);
        return result;
    }

    private static List<ExecutionCourse> getExecutionCoursesToInquiries(YearDelegate yearDelegate,
            final ExecutionSemester executionSemester, final ExecutionDegree executionDegree, Integer curricularYear) {
        final List<ExecutionCourse> result = new ArrayList<ExecutionCourse>();
        for (ExecutionCourse executionCourse : yearDelegate.getDegree().getExecutionCourses(curricularYear, executionSemester)) {
            if (InquiriesRoot.getAvailableForInquiries(executionCourse) && executionCourse.hasAnyEnrolment(executionDegree)
                    && !executionCourse.getInquiryResultsSet().isEmpty()) {
                result.add(executionCourse);
            }
        }
        return result;
    }

    private static YearDelegate getYearDelegate(List<YearDelegate> otherYearDelegates, int year) {
        for (YearDelegate yearDelegate : otherYearDelegates) {
            if (yearDelegate.getCurricularYear().getYear() == year) {
                return yearDelegate;
            }
        }
        return null;
    }

    public static Delegate getActiveDelegateByStudent(Degree degree, Student student, ExecutionYear executionYear,
            Boolean degreeDelegate) {

        Stream<Delegate> stream =
                degree.getDelegateSet().stream().filter(d -> d.isActive() && d.getUser().equals(student.getPerson().getUser()));
        if (degreeDelegate) {
            Stream<Delegate> degreeStream = null;
            if (degree.isSecondCycle()) {
                degreeStream = stream.filter(d -> d instanceof DegreeDelegate);
            } else {
                degreeStream = stream.filter(d -> d instanceof CycleDelegate);
            }
            Optional<Delegate> result = degreeStream.findAny();
            return result.isPresent() ? result.get() : null;
        }
        Optional<Delegate> result = stream.findAny();
        return result.isPresent() ? result.get() : null;
    }

    public static List<User> getAllActiveDelegatesByType(Degree degree, boolean delegateOfYear, ExecutionYear executionYear) {
        if (degree.isEmpty()) {
            return Collections.emptyList();
        }
        Stream<Delegate> stream = degree.getDelegateSet().stream();
        if (delegateOfYear) {
            stream = stream.filter(d -> d instanceof YearDelegate);
        }
        List<User> result = stream.filter(d -> d.isActive()).map(d -> d.getUser()).collect(Collectors.toList());
        return result;
    }

    private static Collection<ExecutionCourse> getDelegatedExecutionCourses(YearDelegate yearDelegate,
            ExecutionSemester executionSemester) {
        final List<ExecutionCourse> result = new ArrayList<ExecutionCourse>();
        for (final DegreeCurricularPlan degreeCurricularPlan : yearDelegate.getDegree().getDegreeCurricularPlansSet()) {
            for (final CurricularCourse course : degreeCurricularPlan.getCurricularCoursesSet()) {
                for (final ExecutionCourse executionCourse : course.getAssociatedExecutionCoursesSet()) {
                    if (executionSemester == executionCourse.getExecutionPeriod()) {
                        for (final DegreeModuleScope scope : course.getDegreeModuleScopes()) {
                            if (scope.isActiveForExecutionPeriod(executionSemester)
                                    && scope.getCurricularYear() == yearDelegate.getCurricularYear().getYear()) {
                                if (scope.getCurricularSemester() == executionSemester.getSemester()) {
                                    result.add(executionCourse);
                                    break;
                                } else
                                //even if it hasn't an active scope in one of the curricular semesters,
                                //it must appear to the delegate since it's an annual course
                                if (course.isAnual(executionSemester.getExecutionYear())) {
                                    result.add(executionCourse);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<Delegate> getAllDelegatesByExecutionYearAndFunctionType(Degree degree, ExecutionYear executionYear,
            boolean delegateOfYear) {
        Interval execInterval =
                new Interval(executionYear.getBeginDateYearMonthDay().toDateTimeAtMidnight(), executionYear
                        .getEndDateYearMonthDay().toDateTimeAtMidnight());
        Stream<Delegate> stream = degree.getDelegateSet().stream();
        if (delegateOfYear) {
            stream = stream.filter(d -> d instanceof YearDelegate);
        }
        return stream.filter(d -> d.getInterval().overlaps(execInterval)).collect(Collectors.toList());
    }
}
