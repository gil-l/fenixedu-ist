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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fenixedu.academic.domain.Department;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.TeacherAuthorization;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.domain.phd.InternalPhdParticipant;
import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.academic.domain.thesis.ThesisEvaluationParticipant;
import org.fenixedu.academic.domain.thesis.ThesisParticipationType;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.LocalDate;

import pt.ist.fenixedu.contracts.domain.accessControl.DepartmentPresidentStrategy;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonProfessionalData;
import pt.ist.fenixedu.teacher.evaluation.domain.TeacherCredits;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.AnnualTeachingCredits;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.OtherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherServiceComment;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherServiceLog;

public class AnnualTeachingCreditsBean implements Serializable {
    private final ExecutionYear executionYear;
    private final Teacher teacher;
    private BigDecimal effectiveTeachingLoad;
    private BigDecimal teachingCredits;
    private BigDecimal masterDegreeThesesCredits;
    private BigDecimal phdDegreeThesesCredits;
    private BigDecimal projectsTutorialsCredits;
    private BigDecimal managementFunctionCredits;
    private BigDecimal othersCredits;
    private BigDecimal creditsReduction;
    private BigDecimal serviceExemptionCredits;
    private BigDecimal annualTeachingLoad;
    private BigDecimal yearCredits;
    private BigDecimal finalCredits;
    private BigDecimal accumulatedCredits;
    private Boolean hasAnyLimitation = false;
    private Boolean areCreditsCalculated = false;
    private boolean canEditTeacherCredits;
    private boolean canEditTeacherCreditsInAnyPeriod = false;
    private boolean canSeeCreditsReduction = false;

    public boolean isCanSeeCreditsReduction() {
        return canSeeCreditsReduction;
    }

    public void setCanSeeCreditsReduction(boolean canSeeCreditsReduction) {
        this.canSeeCreditsReduction = canSeeCreditsReduction;
    }

    private Set<ExecutionYear> correctionInYears = new TreeSet<ExecutionYear>(ExecutionYear.COMPARATOR_BY_YEAR);

    private final List<AnnualTeachingCreditsByPeriodBean> annualTeachingCreditsByPeriodBeans =
            new ArrayList<AnnualTeachingCreditsByPeriodBean>();

    public AnnualTeachingCreditsBean(AnnualTeachingCredits annualTeachingCredits) {
        super();
        this.executionYear = annualTeachingCredits.getAnnualCreditsState().getExecutionYear();
        this.teacher = annualTeachingCredits.getTeacher();
        this.effectiveTeachingLoad = annualTeachingCredits.getEfectiveTeachingLoad();
        this.teachingCredits = annualTeachingCredits.getTeachingCredits();
        this.masterDegreeThesesCredits = annualTeachingCredits.getMasterDegreeThesesCredits();
        this.phdDegreeThesesCredits = annualTeachingCredits.getPhdDegreeThesesCredits();
        this.projectsTutorialsCredits = annualTeachingCredits.getProjectsTutorialsCredits();
        this.managementFunctionCredits = annualTeachingCredits.getManagementFunctionCredits();
        this.othersCredits = annualTeachingCredits.getOthersCredits();
        this.creditsReduction = annualTeachingCredits.getCreditsReduction();
        this.serviceExemptionCredits = annualTeachingCredits.getServiceExemptionCredits();
        this.annualTeachingLoad = annualTeachingCredits.getAnnualTeachingLoad();
        this.yearCredits = annualTeachingCredits.getYearCredits();
        this.finalCredits = annualTeachingCredits.getFinalCredits();
        this.accumulatedCredits = annualTeachingCredits.getAccumulatedCredits();
        this.hasAnyLimitation = annualTeachingCredits.getHasAnyLimitation();
        setAreCreditsCalculated(annualTeachingCredits.getAnnualCreditsState().getIsFinalCreditsCalculated());
        setAnnualTeachingCreditsByPeriod(executionYear, teacher);
        for (ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            if (!annualTeachingCredits.isPastResume()) {
                for (OtherService otherService : executionSemester.getOtherServicesCorrectionsSet()) {
                    if (otherService.getTeacherService().getTeacher().equals(teacher)
                            && !otherService.getCorrectedExecutionSemester().equals(
                                    otherService.getTeacherService().getExecutionPeriod())) {
                        correctionInYears.add(otherService.getTeacherService().getExecutionPeriod().getExecutionYear());
                    }
                }
            }
        }
    }

    public AnnualTeachingCreditsBean(ExecutionYear executionYear, Teacher teacher) {
        this.executionYear = executionYear;
        this.teacher = teacher;
        this.effectiveTeachingLoad = BigDecimal.ZERO;
        this.teachingCredits = BigDecimal.ZERO;
        this.masterDegreeThesesCredits = BigDecimal.ZERO;
        this.phdDegreeThesesCredits = BigDecimal.ZERO;
        this.projectsTutorialsCredits = BigDecimal.ZERO;
        this.managementFunctionCredits = BigDecimal.ZERO;
        this.othersCredits = BigDecimal.ZERO;
        this.creditsReduction = BigDecimal.ZERO;
        this.serviceExemptionCredits = BigDecimal.ZERO;
        this.annualTeachingLoad = BigDecimal.ZERO;
        this.yearCredits = BigDecimal.ZERO;
        this.finalCredits = BigDecimal.ZERO;
        this.accumulatedCredits = BigDecimal.ZERO;
        setAnnualTeachingCreditsByPeriod(executionYear, teacher);
    }

    protected void setAnnualTeachingCreditsByPeriod(ExecutionYear executionYear, Teacher teacher) {
        User user = Authenticate.getUser();
        if (RoleType.SCIENTIFIC_COUNCIL.isMember(user) || teacher.getPerson().getUser().equals(user)) {
            setCanSeeCreditsReduction(true);
        }
        for (ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            AnnualTeachingCreditsByPeriodBean annualTeachingCreditsByPeriodBean =
                    new AnnualTeachingCreditsByPeriodBean(executionSemester, teacher);
            annualTeachingCreditsByPeriodBeans.add(annualTeachingCreditsByPeriodBean);
            if (annualTeachingCreditsByPeriodBean.getCanEditTeacherCredits()) {
                setCanEditTeacherCreditsInAnyPeriod(true);
            }
            if (executionSemester.isFirstOfYear()) {
                setCanEditTeacherCredits(annualTeachingCreditsByPeriodBean.getCanEditTeacherCredits());
            }
        }
    }

    public List<AnnualTeachingCreditsByPeriodBean> getAnnualTeachingCreditsByPeriodBeans() {
        Collections.sort(annualTeachingCreditsByPeriodBeans,
                Comparator.comparing(AnnualTeachingCreditsByPeriodBean::getExecutionPeriod));
        return annualTeachingCreditsByPeriodBeans;
    }

    public String getProfessionalCategoryName() {
        return teacher.getLastCategory(executionYear.getAcademicInterval()).map(tc -> tc.getProfessionalCategory())
                .map(pc -> pc.getName().getContent()).orElse(null);
    }

    public String getDepartmentName() {
        Department department = teacher.getLastDepartment(executionYear.getAcademicInterval());
        return department == null ? null : department.getName().getContent();
    }

    public List<ThesisEvaluationParticipant> getMasterDegreeThesis() {
        ArrayList<ThesisEvaluationParticipant> participants = new ArrayList<ThesisEvaluationParticipant>();
        if (!executionYear.getYear().equals("2011/2012")) {
            for (ThesisEvaluationParticipant participant : teacher.getPerson().getThesisEvaluationParticipantsSet()) {
                Thesis thesis = participant.getThesis();
                if (thesis.isEvaluated()
                        && thesis.hasFinalEnrolmentEvaluation()
                        && thesis.getEvaluation().getYear() == executionYear.getBeginCivilYear()
                        && (participant.getType() == ThesisParticipationType.ORIENTATOR || participant.getType() == ThesisParticipationType.COORIENTATOR)) {
                    participants.add(participant);
                }
            }
        }
        Collections.sort(participants, ThesisEvaluationParticipant.COMPARATOR_BY_STUDENT_NUMBER);
        return participants;
    }

    public List<InternalPhdParticipant> getPhdDegreeTheses() {
        ArrayList<InternalPhdParticipant> participants = new ArrayList<InternalPhdParticipant>();
        if (!executionYear.getYear().equals("2011/2012")) {
            for (InternalPhdParticipant internalPhdParticipant : teacher.getPerson().getInternalParticipantsSet()) {
                LocalDate conclusionDate = internalPhdParticipant.getIndividualProcess().getConclusionDate();
                if (conclusionDate != null
                        && conclusionDate.getYear() == executionYear.getBeginCivilYear()
                        && (internalPhdParticipant.getProcessForGuiding() != null || internalPhdParticipant
                                .getProcessForAssistantGuiding() != null)) {
                    participants.add(internalPhdParticipant);
                }
            }
        }
        return participants;
    }

    public List<Professorship> getProjectAndTutorialProfessorships() {
        List<Professorship> professorships = new ArrayList<Professorship>();
        ExecutionYear previousExecutionYear = executionYear.getPreviousExecutionYear();
        for (Professorship professorship : getTeacher().getPerson().getProfessorshipsSet()) {
            if (professorship.getExecutionCourse().getExecutionPeriod().getExecutionYear().equals(previousExecutionYear)
                    && professorship.getExecutionCourse().getProjectTutorialCourse()
                    && !professorship.getExecutionCourse().isDissertation()) {
                professorships.add(professorship);
            }
        }
        return professorships;
    }

    public List<TeacherServiceComment> getTeacherServiceComments() {
        List<TeacherServiceComment> teacherServiceComments = new ArrayList<TeacherServiceComment>();
        for (ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            TeacherService teacherService = TeacherService.getTeacherServiceByExecutionPeriod(teacher, executionSemester);
            if (teacherService != null) {
                teacherServiceComments.addAll(teacherService.getTeacherServiceComments());
            }
        }
        Collections.sort(teacherServiceComments, Comparator.comparing(TeacherServiceComment::getLastModifiedDate));
        return teacherServiceComments;
    }

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public BigDecimal getEffectiveTeachingLoad() {
        return effectiveTeachingLoad;
    }

    public BigDecimal getTeachingCredits() {
        return teachingCredits;
    }

    public BigDecimal getMasterDegreeThesesCredits() {
        return masterDegreeThesesCredits;
    }

    public BigDecimal getPhdDegreeThesesCredits() {
        return phdDegreeThesesCredits;
    }

    public BigDecimal getProjectsTutorialsCredits() {
        return projectsTutorialsCredits;
    }

    public BigDecimal getOthersCredits() {
        return othersCredits;
    }

    public BigDecimal getManagementFunctionCredits() {
        return managementFunctionCredits;
    }

    public BigDecimal getCreditsReduction() {
        return creditsReduction;
    }

    public BigDecimal getServiceExemptionCredits() {
        return serviceExemptionCredits;
    }

    public BigDecimal getAnnualTeachingLoad() {
        return annualTeachingLoad;
    }

    public BigDecimal getYearCredits() {
        return yearCredits;
    }

    public BigDecimal getFinalCredits() {
        return finalCredits;
    }

    public BigDecimal getAccumulatedCredits() {
        return accumulatedCredits;
    }

    public boolean isCanEditTeacherCredits() {
        return canEditTeacherCredits;
    }

    public void setCanEditTeacherCredits(boolean canEditTeacherCredits) {
        this.canEditTeacherCredits = canEditTeacherCredits;
    }

    public boolean isCanEditTeacherCreditsInAnyPeriod() {
        return canEditTeacherCreditsInAnyPeriod;
    }

    public void setCanEditTeacherCreditsInAnyPeriod(boolean canEditTeacherCreditsInAnyPeriod) {
        this.canEditTeacherCreditsInAnyPeriod = canEditTeacherCreditsInAnyPeriod;
    }

    public Boolean getHasAnyLimitation() {
        return hasAnyLimitation;
    }

    public void setHasAnyLimitation(Boolean hasAnyLimitation) {
        this.hasAnyLimitation = hasAnyLimitation;
    }

    public Boolean getAreCreditsCalculated() {
        return areCreditsCalculated;
    }

    public void setAreCreditsCalculated(Boolean areCreditsCalculated) {
        this.areCreditsCalculated = areCreditsCalculated;
    }

    public String getCorrections() {
        StringBuilder result = new StringBuilder();
        for (ExecutionYear executionTear : correctionInYears) {
            result.append("(** ").append(executionTear.getName()).append(") ");
        }
        if (hasAnyLimitation) {
            result.append("(*)");
        }
        return result.toString();
    }

    public Set<ExecutionYear> getCorrectionInYears() {
        return correctionInYears;
    }

    public void setCorrectionInYears(Set<ExecutionYear> correctionInYears) {
        this.correctionInYears = correctionInYears;
    }

    public SortedSet<TeacherServiceLog> getLogs() {
        final SortedSet<TeacherServiceLog> logs = new TreeSet<TeacherServiceLog>();
        for (final AnnualTeachingCreditsByPeriodBean bean : annualTeachingCreditsByPeriodBeans) {
            logs.addAll(bean.getLogs());
        }
        return logs;
    }

    public boolean getCanUserSeeTeacherServiceLogs() {
        User userView = Authenticate.getUser();
        Teacher loggedTeacher = userView.getPerson().getTeacher();
        Department department = getTeacher().getDepartment();
        return RoleType.SCIENTIFIC_COUNCIL.isMember(userView) || (loggedTeacher != null && loggedTeacher.equals(getTeacher()))
                || (department != null && DepartmentPresidentStrategy.isCurrentUserCurrentDepartmentPresident(department));
    }

    public void calculateCredits() {
        masterDegreeThesesCredits = AnnualTeachingCredits.calculateMasterDegreeThesesCredits(teacher, executionYear);
        phdDegreeThesesCredits = AnnualTeachingCredits.calculatePhdDegreeThesesCredits(teacher, executionYear);
        projectsTutorialsCredits = AnnualTeachingCredits.calculateProjectsTutorialsCredits(teacher, executionYear);

        BigDecimal yearCreditsForFinalCredits = BigDecimal.ZERO;
        BigDecimal annualTeachingLoadFinalCredits = BigDecimal.ZERO;

        boolean hasOrientantionCredits = false;
        boolean hasFinalAndAccumulatedCredits = false;

        for (ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            TeacherAuthorization teacherAuthorization =
                    getTeacher().getTeacherAuthorization(executionSemester.getAcademicInterval()).orElse(null);
            boolean activeContractedTeacherForSemester =
                    teacherAuthorization != null && teacherAuthorization.isContracted()
                            && PersonProfessionalData.isTeacherActiveForSemester(getTeacher(), executionSemester);
            boolean activeExternalTeacher = teacherAuthorization != null && !teacherAuthorization.isContracted();
            if (activeContractedTeacherForSemester || activeExternalTeacher) {
                BigDecimal thisSemesterManagementFunctionCredits =
                        new BigDecimal(TeacherCredits.calculateManagementFunctionsCredits(getTeacher(), executionSemester));
                managementFunctionCredits = managementFunctionCredits.add(thisSemesterManagementFunctionCredits);
                serviceExemptionCredits =
                        serviceExemptionCredits.add(new BigDecimal(TeacherCredits.calculateServiceExemptionCredits(getTeacher(),
                                executionSemester)));
                BigDecimal thisSemesterTeachingLoad =
                        new BigDecimal(TeacherCredits.calculateMandatoryLessonHours(getTeacher(), executionSemester));
                annualTeachingLoad = annualTeachingLoad.add(thisSemesterTeachingLoad).setScale(2, BigDecimal.ROUND_HALF_UP);
                TeacherService teacherService =
                        TeacherService.getTeacherServiceByExecutionPeriod(getTeacher(), executionSemester);
                BigDecimal thisSemesterCreditsReduction = BigDecimal.ZERO;
                if (teacherService != null) {
                    effectiveTeachingLoad =
                            effectiveTeachingLoad.add(new BigDecimal(teacherService.getDegreeTeachingServices().stream()
                                    .filter(d -> !d.getProfessorship().getExecutionCourse().getProjectTutorialCourse())
                                    .mapToDouble(d -> d.getEfectiveLoad()).sum()));
                    teachingCredits = teachingCredits.add(new BigDecimal(teacherService.getTeachingDegreeCredits()));
                    thisSemesterCreditsReduction = teacherService.getReductionServiceCredits();
                    othersCredits = othersCredits.add(new BigDecimal(teacherService.getOtherServiceCredits()));
                }
                creditsReduction = creditsReduction.add(thisSemesterCreditsReduction);
                BigDecimal reductionAndManagement = thisSemesterManagementFunctionCredits.add(thisSemesterCreditsReduction);
                BigDecimal thisSemesterYearCredits = thisSemesterTeachingLoad;
                if (thisSemesterTeachingLoad.compareTo(reductionAndManagement) > 0) {
                    thisSemesterYearCredits = reductionAndManagement;
                } else {
                    setHasAnyLimitation(true);
                }
                yearCredits = yearCredits.add(thisSemesterYearCredits);
                boolean isTeacherMonitorCategory =
                        teacher.getCategory(executionSemester.getAcademicInterval()).map(tc -> tc.getProfessionalCategory())
                                .map(pc -> pc.isTeacherMonitorCategory()).orElse(false);
                if (activeContractedTeacherForSemester && !isTeacherMonitorCategory) {
                    yearCreditsForFinalCredits = yearCreditsForFinalCredits.add(thisSemesterYearCredits);
                    annualTeachingLoadFinalCredits = annualTeachingLoadFinalCredits.add(thisSemesterTeachingLoad);
                    if (executionSemester.getSemester() == 2) {
                        hasFinalAndAccumulatedCredits = true;
                    } else {
                        hasOrientantionCredits = true;
                    }
                }
            }
        }
        yearCredits = yearCredits.add(teachingCredits).add(serviceExemptionCredits).add(othersCredits);
        yearCreditsForFinalCredits =
                yearCreditsForFinalCredits.add(teachingCredits).add(serviceExemptionCredits).add(othersCredits);
        if (hasOrientantionCredits) {
            yearCredits =
                    yearCredits.add(getMasterDegreeThesesCredits()).add(getPhdDegreeThesesCredits())
                            .add(getProjectsTutorialsCredits()).setScale(2, BigDecimal.ROUND_HALF_UP);
            yearCreditsForFinalCredits =
                    yearCreditsForFinalCredits.add(getMasterDegreeThesesCredits()).add(getPhdDegreeThesesCredits())
                            .add(getProjectsTutorialsCredits());
        }
        if (hasFinalAndAccumulatedCredits) {
            finalCredits = yearCreditsForFinalCredits.subtract(annualTeachingLoadFinalCredits);
            BigDecimal lastYearAccumulated = getPreviousAccumulatedCredits();
            accumulatedCredits = (finalCredits.add(lastYearAccumulated)).setScale(2, BigDecimal.ROUND_HALF_UP);
            finalCredits = finalCredits.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    private BigDecimal getPreviousAccumulatedCredits() {
        AnnualTeachingCredits previousAnnualTeachingCredits =
                AnnualTeachingCredits.readByYearAndTeacher(getExecutionYear().getPreviousExecutionYear(), getTeacher());
        return previousAnnualTeachingCredits != null ? previousAnnualTeachingCredits.getAccumulatedCredits() : BigDecimal.ZERO;
    }

    public Boolean getAreCreditsOpen() {
        return !getAreCreditsCalculated();
    }

    public byte[] getAnnualTeacherCreditsDocument(boolean withConfidentionalInformation) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("annualTeachingCreditsBean", this);
        parameters.put("organization", org.fenixedu.academic.domain.organizationalStructure.Unit.getInstitutionAcronym());
        parameters.put("withConfidentionalInformation", withConfidentionalInformation);

        final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources/TeacherCreditsSheetResources");
        parameters.put("REPORT_RESOURCE_BUNDLE", resourceBundle);
        return org.fenixedu.academic.util.report.ReportsUtils.generateReport(
                "pt.ist.fenixedu.teacher.evaluation.domain.credits.AnnualTeachingCredits.report", parameters, null).getData();
    }
}
