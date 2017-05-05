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
package pt.ist.fenixedu.teacher.evaluation.domain.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Country;
import org.fenixedu.academic.domain.Department;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.TeacherAuthorization;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.spreadsheet.Spreadsheet;
import org.fenixedu.commons.spreadsheet.Spreadsheet.Row;
import org.joda.time.Interval;

import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.GiafProfessionalData;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonContractSituation;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonProfessionalData;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.ProfessionalCategory;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.ProfessionalRegime;
import pt.ist.fenixedu.contracts.domain.util.CategoryType;
import pt.ist.fenixedu.teacher.evaluation.domain.TeacherCredits;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.util.AnnualTeachingCreditsBean;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.OtherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherService;

public class TeacherCreditsReportFile extends TeacherCreditsReportFile_Base {

    private static final String EMPTY_CELL = "-";

    public TeacherCreditsReportFile() {
        super();
    }

    @Override
    public String getJobName() {
        return "Listagem de serviço de docência do " + Unit.getInstitutionAcronym();
    }

    @Override
    protected String getPrefix() {
        return "Listagem de serviço de docência do " + Unit.getInstitutionAcronym();
    }

    @Override
    public void renderReport(Spreadsheet spreadsheet) throws Exception {
        ExecutionYear executionYear = getExecutionYear();
        spreadsheet.setName("Docentes do " + Unit.getInstitutionAcronym() + " "
                + executionYear.getQualifiedName().replace("/", ""));
        spreadsheet.setHeader("IstId");
        spreadsheet.setHeader("Nº Mec");
        spreadsheet.setHeader("Nome");
        spreadsheet.setHeader("Semestre");
        spreadsheet.setHeader("Categoria");
        spreadsheet.setHeader("Situação");
        spreadsheet.setHeader("Regime");
        spreadsheet.setHeader("Docente de carreira");
        spreadsheet.setHeader("Departamento - último");
        spreadsheet.setHeader("Departamento - dominante");
        spreadsheet.setHeader("CLE");
        spreadsheet.setHeader("CLE - correcções");
        spreadsheet.setHeader("CL");
        spreadsheet.setHeader("CG");
        spreadsheet.setHeader("O");
        spreadsheet.setHeader("AD65 requerido");
        spreadsheet.setHeader("AD65 atribuído");
        spreadsheet.setHeader("SNE");
        spreadsheet.setHeader("CLN");
        //1º sem
        spreadsheet.setHeader("COT");
        spreadsheet.setHeader("COD");
        spreadsheet.setHeader("COM");
        //2º sem
        spreadsheet.setHeader("CO");
        spreadsheet.setHeader("CF");
        spreadsheet.setHeader("CLA");
        spreadsheet.setHeader("SNE - Descrição");
        spreadsheet.setHeader("O - Descrição");
        spreadsheet.setHeader("Nacionalidade");
        spreadsheet.setHeader("Género");

        Collection<Teacher> teachers = Bennu.getInstance().getTeachersSet();
        for (ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            Interval semesterInterval =
                    new Interval(executionSemester.getBeginDateYearMonthDay().toLocalDate().toDateTimeAtStartOfDay(),
                            executionSemester.getEndDateYearMonthDay().toLocalDate().toDateTimeAtStartOfDay());
            for (Teacher teacher : teachers) {
                TeacherAuthorization teacherAuthorization =
                        teacher.getTeacherAuthorization(executionSemester.getAcademicInterval()).orElse(null);
                if (teacherAuthorization != null) {
                    final Row row = spreadsheet.addRow();
                    row.setCell(teacher.getPerson().getUsername());
                    row.setCell(teacher.getPerson().getEmployee() != null ? teacher.getPerson().getEmployee().getEmployeeNumber() : null);
                    row.setCell(teacher.getPerson().getName());
                    row.setCell(executionSemester.getName());
                    ProfessionalCategory category =
                            teacher.getCategory(executionSemester.getAcademicInterval()).map(tc -> tc.getProfessionalCategory())
                                    .orElse(null);
                    PersonContractSituation situation = null;
                    ProfessionalRegime regime = null;
                    if (teacherAuthorization.isContracted()
                            && PersonProfessionalData.isTeacherActiveForSemester(teacher, executionSemester)) {
                        situation =
                                PersonContractSituation.getCurrentOrLastTeacherContractSituation(teacher, executionSemester
                                        .getBeginDateYearMonthDay().toLocalDate(), executionSemester.getEndDateYearMonthDay()
                                        .toLocalDate());
                        regime = getProfessionalRegime(situation, semesterInterval);
                    }
                    row.setCell(category == null ? null : category.getName().getContent());
                    row.setCell(situation == null ? null : situation.getContractSituation().getName().getContent());

                    row.setCell(regime == null ? null : regime.getName().getContent());
                    row.setCell(category == null ? null : category.isTeacherProfessorCategory() ? "S" : "N");
                    Department lastDepartment = teacher.getLastDepartment(executionSemester.getAcademicInterval());
                    row.setCell(lastDepartment == null ? null : lastDepartment.getName().getContent());
                    Department creditsDepartment = teacher.getDepartment(executionSemester.getAcademicInterval()).orElse(null);
                    row.setCell(creditsDepartment == null ? null : creditsDepartment.getName().getContent());

                    TeacherService teacherService = TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionSemester);
                    row.setCell(teacherService == null ? 0 : teacherService.getTeachingDegreeHours());// CLE

                    row.setCell(teacherService == null ? 0 : teacherService.getTeachingDegreeCorrections());// CLE corrections

                    row.setCell(teacherService == null ? 0 : teacherService.getTeachingDegreeCredits());// CL

                    row.setCell(TeacherCredits.calculateManagementFunctionsCredits(teacher, executionSemester)); // CG
                    //CG (desc)
                    row.setCell(teacherService == null ? 0 : teacherService.getOtherServiceCredits());// O
                    Double creditsReductionRequired =
                            teacherService == null ? null : teacherService.getReductionService() == null ? null : teacherService
                                    .getReductionService().getCreditsReduction() == null ? null : teacherService
                                    .getReductionService().getCreditsReduction().doubleValue();

                    Double creditsReductionAttributed =
                            teacherService == null ? null : teacherService.getReductionService() == null ? null : teacherService
                                    .getReductionService().getCreditsReductionAttributed() == null ? null : teacherService
                                    .getReductionService().getCreditsReductionAttributed().doubleValue();
                    row.setCell(creditsReductionRequired);// AD65 requerido
                    row.setCell(creditsReductionAttributed);// AD65 atribuído

                    row.setCell(TeacherCredits.calculateServiceExemptionCredits(teacher, executionSemester)); //SNE

                    row.setCell(TeacherCredits.calculateMandatoryLessonHours(teacher, executionSemester)); //CLN

                    AnnualTeachingCreditsBean annualTeachingCreditsBean = new AnnualTeachingCreditsBean(executionYear, teacher);
                    annualTeachingCreditsBean.calculateCredits();
                    if (executionSemester.getSemester() == 1) {
                        row.setCell(annualTeachingCreditsBean.getProjectsTutorialsCredits());//COT
                        row.setCell(annualTeachingCreditsBean.getPhdDegreeThesesCredits());//COD
                        row.setCell(annualTeachingCreditsBean.getMasterDegreeThesesCredits());//COM
                        row.setCell(EMPTY_CELL);//CO
                        row.setCell(EMPTY_CELL);//CF
                        row.setCell(EMPTY_CELL);//CLA
                    } else {
                        row.setCell(EMPTY_CELL);//COT
                        row.setCell(EMPTY_CELL);//COD
                        row.setCell(EMPTY_CELL);//COM
                        row.setCell(annualTeachingCreditsBean.getYearCredits());//CO
                        row.setCell(annualTeachingCreditsBean.getFinalCredits());//CF
                        row.setCell(annualTeachingCreditsBean.getAccumulatedCredits());//CLA			
                    }
                    row.setCell(getServiceExemptionDescription(executionSemester, teacher)); //SNE Desc
                    row.setCell(teacherService == null ? EMPTY_CELL : getOthersDesciption(teacherService));//O (desc)
                    row.setCell(getNationality(teacher)); //Nacionalidade
                    row.setCell(
                            teacher.getPerson().getGender() != null ? teacher.getPerson().getGender().getLocalizedName() : null);//Genero
                }
            }
        }
    }

    private String getNationality(Teacher teacher) {
        Country country = teacher.getPerson().getCountry();
        if (country == null || country.getCountryNationality() == null) {
            return null;
        }
        return country.getCountryNationality().toString();
    }

    private ProfessionalRegime getProfessionalRegime(PersonContractSituation teacherContractSituation, Interval interval) {
        GiafProfessionalData giafProfessionalData =
                teacherContractSituation != null ? teacherContractSituation.getGiafProfessionalData() : null;
        PersonProfessionalData personProfessionalData =
                giafProfessionalData != null ? giafProfessionalData.getPersonProfessionalData() : null;
        return personProfessionalData != null ? personProfessionalData.getDominantProfessionalRegime(giafProfessionalData,
                interval, CategoryType.TEACHER) : null;
    }

    private String getOthersDesciption(TeacherService teacherService) {
        List<String> others = new ArrayList<String>();
        for (OtherService otherService : teacherService.getOtherServices()) {
            others.add(otherService.getReason().replace("\r", "").replace("\n", "") + " (" + otherService.getCredits()
                    + " créditos)");
        }
        return others.stream().collect(Collectors.joining(", "));
    }

    public String getServiceExemptionDescription(ExecutionSemester executionSemester, Teacher teacher) {
        Set<PersonContractSituation> personProfessionalExemptions =
                PersonContractSituation.getValidTeacherServiceExemptions(teacher, executionSemester);
        List<String> serviceExemption = new ArrayList<String>();

        for (PersonContractSituation personContractSituation : personProfessionalExemptions) {
            serviceExemption.add(personContractSituation.getContractSituation().getName().getContent());
        }

        return serviceExemption.stream().collect(Collectors.joining(", "));
    }
}
