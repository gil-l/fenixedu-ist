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
package pt.ist.fenixedu.teacher.domain.time.calendarStructure;

import java.util.List;

import org.fenixedu.academic.domain.time.calendarStructure.AcademicCalendarEntry;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicCalendarRootEntry;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicInterval;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicSemesterCE;
import org.fenixedu.academic.domain.time.chronologies.AcademicChronology;
import org.fenixedu.academic.domain.time.chronologies.dateTimeFields.AcademicSemesterDateTimeFieldType;
import org.fenixedu.academic.util.MultiLanguageString;
import org.joda.time.DateTime;

public class TeacherCreditsFillingForDepartmentAdmOfficeCE extends TeacherCreditsFillingForDepartmentAdmOfficeCE_Base {

    public TeacherCreditsFillingForDepartmentAdmOfficeCE(AcademicCalendarEntry parentEntry, MultiLanguageString title,
            MultiLanguageString description, DateTime begin, DateTime end, AcademicCalendarRootEntry rootEntry) {

        super();
        super.initEntry(parentEntry, title, description, begin, end, rootEntry);
    }

    public static TeacherCreditsFillingForDepartmentAdmOfficeCE getTeacherCreditsFillingForDepartmentAdmOffice(
            AcademicInterval academicInterval) {
        AcademicCalendarEntry entry = academicInterval.getAcademicCalendarEntry();
        AcademicChronology academicChronology = academicInterval.getAcademicChronology();

        if (entry instanceof AcademicSemesterCE) {
            final AcademicSemesterCE academicSemesterCE = (AcademicSemesterCE) academicChronology.findSameEntry(entry);
            List<AcademicCalendarEntry> childEntries =
                    academicSemesterCE.getChildEntriesWithTemplateEntries(TeacherCreditsFillingForDepartmentAdmOfficeCE.class);
            return (TeacherCreditsFillingForDepartmentAdmOfficeCE) (!childEntries.isEmpty() ? childEntries.iterator().next() : null);
        }
        int index = entry.getBegin().withChronology(academicChronology).get(AcademicSemesterDateTimeFieldType.academicSemester());
        AcademicSemesterCE academicSemester = academicChronology.getAcademicSemesterIn(index);
        if (academicSemester == null) {
            return null;
        }
        final AcademicSemesterCE academicSemesterCE = (AcademicSemesterCE) academicChronology.findSameEntry(academicSemester);
        List<AcademicCalendarEntry> childEntries =
                academicSemesterCE.getChildEntriesWithTemplateEntries(TeacherCreditsFillingForDepartmentAdmOfficeCE.class);
        return (TeacherCreditsFillingForDepartmentAdmOfficeCE) (!childEntries.isEmpty() ? childEntries.iterator().next() : null);
    }
}
