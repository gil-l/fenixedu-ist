/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Teacher Credits.
 *
 * FenixEdu IST Teacher Credits is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Teacher Credits is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Teacher Credits.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.teacher.domain.teacher;

import java.util.Date;

import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.academic.util.HourMinuteSecond;
import org.fenixedu.academic.util.WeekDay;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;

import pt.ist.fenixedu.teacher.domain.time.calendarStructure.TeacherCreditsFillingCE;
import pt.ist.fenixedu.teacher.dto.teacher.workTime.InstitutionWorkTimeDTO;
import pt.ist.fenixedu.teacher.util.date.TimePeriod;
import pt.ist.fenixframework.Atomic;

public class InstitutionWorkTime extends InstitutionWorkTime_Base {

    public InstitutionWorkTime(TeacherService teacherService, Date startTime, Date endTime, WeekDay weekDay) {
        super();
        if (teacherService == null || startTime == null || endTime == null || weekDay == null) {
            throw new DomainException("arguments can't be null");
        }
        setTeacherService(teacherService);
        setStartTime(startTime);
        setEndTime(endTime);
        setWeekDay(weekDay);
        verifyOverlappings();
        log("label.teacher.schedule.institutionWorkTime.create");
    }

    private void log(final String key) {
        final StringBuilder log = new StringBuilder();
        log.append(BundleUtil.getString(Bundle.TEACHER_CREDITS, key));
        log.append(getWeekDay().getLabel());
        log.append(" ");
        log.append(getStartTime().getHours());
        log.append(":");
        log.append(getStartTime().getMinutes());
        log.append(" - ");
        log.append(getEndTime().getHours());
        log.append(":");
        log.append(getEndTime().getMinutes());
        new TeacherServiceLog(getTeacherService(), log.toString());
    }

    @Override
    @Atomic
    public void delete() {
        TeacherCreditsFillingCE.checkValidCreditsPeriod(getTeacherService().getExecutionPeriod(), Authenticate.getUser());
        log("label.teacher.schedule.institutionWorkTime.delete");
        setTeacherService(null);
        super.delete();
    }

    public double getHours() {
        TimePeriod timePeriod = new TimePeriod(this.getStartTime(), this.getEndTime());
        return timePeriod.hours().doubleValue();
    }

    public void update(InstitutionWorkTimeDTO institutionWorkTimeDTO, RoleType roleType) {
        TeacherCreditsFillingCE.checkValidCreditsPeriod(getTeacherService().getExecutionPeriod(), Authenticate.getUser());
        setWeekDay(institutionWorkTimeDTO.getWeekDay());
        setStartTime(institutionWorkTimeDTO.getStartTime());
        setEndTime(institutionWorkTimeDTO.getEndTime());
        verifyOverlappings();
        log("label.teacher.schedule.institutionWorkTime.edit");
    }

    public void update(WeekDay weekDay, Date startTime, Date endTime) {
        setWeekDay(weekDay);
        setStartTime(startTime);
        setEndTime(endTime);
        verifyOverlappings();
        log("label.teacher.schedule.institutionWorkTime.edit");
    }

    private void verifyOverlappings() {
        verifyOverlappingWithOtherInstitutionWorkingTimes();
    }

    private void verifyOverlappingWithOtherInstitutionWorkingTimes() {
        for (InstitutionWorkTime teacherInstitutionWorkTime : getTeacherService().getInstitutionWorkTimes()) {
            if (this != teacherInstitutionWorkTime) {
                if (teacherInstitutionWorkTime.getWeekDay().equals(getWeekDay())) {
                    HourMinuteSecond supportLessonStart = teacherInstitutionWorkTime.getStartTimeHourMinuteSecond();
                    HourMinuteSecond supportLessonEnd = teacherInstitutionWorkTime.getStartTimeHourMinuteSecond();
                    if (supportLessonEnd.compareTo(getStartTimeHourMinuteSecond()) < 0
                            || supportLessonStart.compareTo(getEndTimeHourMinuteSecond()) > 0) {
                        throw new DomainException("message.overlapping.institution.working.period");
                    }
                }
            }
        }
    }

    @Deprecated
    public java.util.Date getEndTime() {
        org.fenixedu.academic.util.HourMinuteSecond hms = getEndTimeHourMinuteSecond();
        return (hms == null) ? null : new java.util.Date(0, 0, 1, hms.getHour(), hms.getMinuteOfHour(), hms.getSecondOfMinute());
    }

    @Deprecated
    public void setEndTime(java.util.Date date) {
        if (date == null) {
            setEndTimeHourMinuteSecond(null);
        } else {
            setEndTimeHourMinuteSecond(org.fenixedu.academic.util.HourMinuteSecond.fromDateFields(date));
        }
    }

    @Deprecated
    public java.util.Date getStartTime() {
        org.fenixedu.academic.util.HourMinuteSecond hms = getStartTimeHourMinuteSecond();
        return (hms == null) ? null : new java.util.Date(0, 0, 1, hms.getHour(), hms.getMinuteOfHour(), hms.getSecondOfMinute());
    }

    @Deprecated
    public void setStartTime(java.util.Date date) {
        if (date == null) {
            setStartTimeHourMinuteSecond(null);
        } else {
            setStartTimeHourMinuteSecond(org.fenixedu.academic.util.HourMinuteSecond.fromDateFields(date));
        }
    }

}
