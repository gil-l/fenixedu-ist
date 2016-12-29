/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Teacher Evaluation.
 *
 * FenixEdu IST Teacher Evaluation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Teacher Evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Teacher Evaluation.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.teacher.evaluation.domain.credits.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityTypeEnum;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.Interval;

import pt.ist.fenixedu.contracts.domain.organizationalStructure.PersonFunction;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonContractSituation;
import pt.ist.fenixedu.teacher.evaluation.domain.CreditsManagerGroup;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.InstitutionWorkTime;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.OtherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.ReductionService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherServiceLog;
import pt.ist.fenixedu.teacher.evaluation.domain.time.calendarStructure.TeacherCreditsFillingCE;

public class AnnualTeachingCreditsByPeriodBean implements Serializable {
    private final ExecutionSemester executionPeriod;
    private final Teacher teacher;
    private Boolean showTeacherCreditsLockedMessage = false;
    private Boolean showTeacherCreditsUnlockedMessage = false;
    private Boolean canLockTeacherCredits = false;
    private Boolean canUnlockTeacherCredits = false;
    private Boolean canEditTeacherCredits = false;
    private Boolean canEditTeacherCreditsReductions = false;
    private Boolean canEditTeacherManagementFunctions = false;

    public AnnualTeachingCreditsByPeriodBean(ExecutionSemester executionPeriod, Teacher teacher) {
        super();
        this.executionPeriod = executionPeriod;
        this.teacher = teacher;
        TeacherService teacherService = TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionPeriod);
        User user = Authenticate.getUser();
        boolean inValidCreditsPeriod = TeacherCreditsFillingCE.isInValidCreditsPeriod(executionPeriod, user);
        boolean isLocked = teacherService != null && teacherService.getTeacherServiceLock() != null;
        if (RoleType.DEPARTMENT_MEMBER.isMember(user)) {
            boolean canLockAndEditTeacherCredits = inValidCreditsPeriod && !isLocked;
            setCanLockTeacherCredits(canLockAndEditTeacherCredits);
            setCanEditTeacherCredits(canLockAndEditTeacherCredits);
        } else if (new CreditsManagerGroup().isMember(user) || RoleType.SCIENTIFIC_COUNCIL.isMember(user)) {
            boolean inValidTeacherCreditsPeriod = TeacherCreditsFillingCE.isInValidTeacherCreditsPeriod(executionPeriod);
            setCanUnlockTeacherCredits(inValidCreditsPeriod && inValidTeacherCreditsPeriod && isLocked);
            setCanEditTeacherCredits(RoleType.SCIENTIFIC_COUNCIL.isMember(user)
                    || (inValidCreditsPeriod && (isLocked || !inValidTeacherCreditsPeriod)));
        }
        setShowTeacherCreditsLockedMessage(isLocked);
        setShowTeacherCreditsUnlockedMessage(!isLocked);
        ReductionService creditsReductionService = getCreditsReductionService();
        setCanEditTeacherCreditsReductions(RoleType.DEPARTMENT_MEMBER.isMember(user) && getCanEditTeacherCredits()
                && (creditsReductionService == null || creditsReductionService.getAttributionDate() == null));
        setCanEditTeacherManagementFunctions(RoleType.DEPARTMENT_MEMBER.isMember(user) ? false : getCanEditTeacherCredits());
    }

    public List<Professorship> getProfessorships() {
        List<Professorship> professorships = new ArrayList<Professorship>();
        for (Professorship professorship : getTeacher().getProfessorships()) {
            if (professorship.getExecutionCourse().getExecutionPeriod().equals(executionPeriod)
                    && professorship.getExecutionCourse().hasAnyLesson()) {
                professorships.add(professorship);
            }
        }

        Collections.sort(professorships,
                Comparator.comparing(Professorship::getExecutionCourse, Comparator.comparing(ExecutionCourse::getName)));

        return professorships;
    }

    public List<InstitutionWorkTime> getInstitutionWorkTime() {
        List<InstitutionWorkTime> institutionWorkingTimes = new ArrayList<InstitutionWorkTime>();
        TeacherService teacherService = TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionPeriod);
        if (teacherService != null && !teacherService.getInstitutionWorkTimes().isEmpty()) {
            institutionWorkingTimes.addAll(teacherService.getInstitutionWorkTimes());
        }

        Collections.sort(
                institutionWorkingTimes,
                Comparator
                        .comparing(InstitutionWorkTime::getTeacherService,
                                Comparator.comparing(TeacherService::getExecutionPeriod))
                        .thenComparing(InstitutionWorkTime::getWeekDay).thenComparing(InstitutionWorkTime::getStartTime));
        return institutionWorkingTimes;
    }

    public List<PersonFunctionBean> getPersonFunctions() {
        List<PersonFunctionBean> personFunctionBeans = new ArrayList<PersonFunctionBean>();
        for (PersonFunction personFunction : (Collection<PersonFunction>) teacher.getPerson().getParentAccountabilities(
                AccountabilityTypeEnum.MANAGEMENT_FUNCTION, PersonFunction.class)) {
            if (personFunction.belongsToPeriod(executionPeriod.getBeginDateYearMonthDay(),
                    executionPeriod.getEndDateYearMonthDay())
                    && !personFunction.getFunction().isVirtual()) {
                personFunctionBeans.add(new PersonFunctionBean(personFunction, executionPeriod));
            }
        }
        return personFunctionBeans;
    }

    public List<OtherService> getOtherServices() {
        List<OtherService> otherServices = new ArrayList<OtherService>();
        TeacherService teacherService = TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionPeriod);
        if (teacherService != null && !teacherService.getOtherServices().isEmpty()) {
            otherServices.addAll(teacherService.getOtherServices());
        }
        return otherServices;
    }

    public List<PersonContractSituation> getServiceExemptions() {
        Interval executionYearInterval =
                new Interval(executionPeriod.getBeginDateYearMonthDay().toDateTimeAtMidnight(), executionPeriod
                        .getEndDateYearMonthDay().plusDays(1).toDateTimeAtMidnight());
        return new ArrayList<PersonContractSituation>(PersonContractSituation.getValidTeacherServiceExemptions(teacher,
                executionYearInterval));
    }

    public ReductionService getCreditsReductionService() {
        TeacherService teacherService = TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionPeriod);
        return teacherService != null ? teacherService.getReductionService() : null;
    }

    public BigDecimal getCreditsReduction() {
        ReductionService reductionService = getCreditsReductionService();
        return reductionService != null ? reductionService.getCreditsReduction() : null;
    }

    public Boolean getRequestCreditsReduction() {
        ReductionService reductionService = getCreditsReductionService();
        return reductionService != null && reductionService.getRequestCreditsReduction() != null ? reductionService
                .getRequestCreditsReduction() : false;
    }

    public String getCreditsReductionServiceAttribute() {
        ReductionService reductionService = getCreditsReductionService();
        return reductionService != null ? reductionService.getAttributionDate() != null ? reductionService
                .getCreditsReductionAttributed().toString() : null : "-";
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public ExecutionSemester getExecutionPeriod() {
        return executionPeriod;
    }

    public Boolean getCanEditTeacherCredits() {
        return canEditTeacherCredits;
    }

    public void setCanEditTeacherCredits(Boolean canEditTeacherCredits) {
        this.canEditTeacherCredits = canEditTeacherCredits;
    }

    public Boolean getCanEditTeacherCreditsReductions() {
        return canEditTeacherCreditsReductions;
    }

    public void setCanEditTeacherCreditsReductions(Boolean canEditTeacherCreditsReductions) {
        this.canEditTeacherCreditsReductions = canEditTeacherCreditsReductions;
    }

    public Boolean getCanEditTeacherManagementFunctions() {
        return canEditTeacherManagementFunctions;
    }

    public void setCanEditTeacherManagementFunctions(Boolean canEditTeacherManagementFunctions) {
        this.canEditTeacherManagementFunctions = canEditTeacherManagementFunctions;
    }

    public TeacherService getTeacherService() {
        return TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionPeriod);
    }

    public Boolean getCanLockTeacherCredits() {
        return canLockTeacherCredits;
    }

    public void setCanLockTeacherCredits(Boolean canLockTeacherCredits) {
        this.canLockTeacherCredits = canLockTeacherCredits;
    }

    public Boolean getCanUnlockTeacherCredits() {
        return canUnlockTeacherCredits;
    }

    public void setCanUnlockTeacherCredits(Boolean canUnlockTeacherCredits) {
        this.canUnlockTeacherCredits = canUnlockTeacherCredits;
    }

    public Set<TeacherServiceLog> getLogs() {
        final TeacherService teacherService = getTeacherService();
        return teacherService == null ? Collections.EMPTY_SET : teacherService.getSortedLogs();
    }

    public Boolean getShowTeacherCreditsLockedMessage() {
        return showTeacherCreditsLockedMessage;
    }

    public void setShowTeacherCreditsLockedMessage(Boolean showTeacherCreditsLockedMessage) {
        this.showTeacherCreditsLockedMessage = showTeacherCreditsLockedMessage;
    }

    public Boolean getShowTeacherCreditsUnlockedMessage() {
        return showTeacherCreditsUnlockedMessage;
    }

    public void setShowTeacherCreditsUnlockedMessage(Boolean showTeacherCreditsUnlockedMessage) {
        this.showTeacherCreditsUnlockedMessage = showTeacherCreditsUnlockedMessage;
    }

}
