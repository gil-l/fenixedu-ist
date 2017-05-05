<%--

    Copyright © 2013 Instituto Superior Técnico

    This file is part of FenixEdu IST Teacher Evaluation.

    FenixEdu IST Teacher Evaluation is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu IST Teacher Evaluation is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu IST Teacher Evaluation.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page isELIgnored="true"%>
<%@page contentType="text/html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/struts-example-1.0" prefix="app" %>
<html:xhtml/>

<jsp:include page="../teacherCreditsStyles.jsp"/>

<h3><bean:message key="label.managementFunctionNote" bundle="TEACHER_CREDITS_SHEET_RESOURCES"/></h3>

<logic:present name="personFunctionBean">
	<bean:define id="url" type="java.lang.String">/user/photo/<bean:write name="personFunctionBean" property="teacher.person.username"/></bean:define>
	<table class="headerTable"><tr>
	<td><img src="<%= request.getContextPath() + url %>"/></td>
	<td ><fr:view name="personFunctionBean">
		<fr:schema bundle="TEACHER_CREDITS_SHEET_RESOURCES" type="pt.ist.fenixedu.teacher.evaluation.domain.credits.util.PersonFunctionBean">
			<fr:slot name="teacher.person.presentationName" key="label.name"/>
			<fr:slot name="teacher.department.name.content" key="label.department" layout="null-as-label"/>
			<fr:slot name="executionSemester" key="label.period" layout="format">
				<fr:property name="format" value="${name}  ${executionYear.year}" />
			</fr:slot>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="creditsStyle"/>
		</fr:layout>
	</fr:view></td>
	</tr></table>

<br/>
<bean:define id="executionYearOid" name="personFunctionBean" property="executionSemester.executionYear.externalId"/>
<bean:define id="teacherOid" name="personFunctionBean" property="teacher.externalId"/>

<p><html:link page="<%="/credits.do?method=viewAnnualTeachingCredits&amp;executionYearOid="+executionYearOid+"&teacherOid="+teacherOid%>"><bean:message key="label.return" bundle="APPLICATION_RESOURCES"/></html:link></p>
<html:messages id="message" message="true" bundle="TEACHER_CREDITS_SHEET_RESOURCES">
	<span class="error"><bean:write name="message" filter="false" /></span>
</html:messages>

	<logic:empty name="personFunctionBean" property="unit">
		<fr:edit id="personFunctionBean1" name="personFunctionBean" action="/managePersonFunctionsShared.do?method=prepareToEditPersonFunctionShared">
			<fr:schema bundle="TEACHER_CREDITS_SHEET_RESOURCES" type="pt.ist.fenixedu.teacher.evaluation.domain.credits.util.PersonFunctionBean">
				<fr:slot name="unit" key="label.departmentOrDegreeOrUnit" layout="autoCompleteWithPostBack">
					<fr:property name="size" value="80"/>
					<fr:property name="labelField" value="presentationNameWithParents"/>
					<fr:property name="indicatorShown" value="true"/>
					<fr:property name="provider" value="pt.ist.fenixedu.teacher.evaluation.ui.renderers.providers.SearchInternalUnits"/>	
					<fr:property name="args" value="slot=name"/>		
					<fr:property name="minChars" value="3"/>
					<fr:property name="errorStyleClass" value="error0"/>
					<fr:property name="destination" value="/managePersonFunctionsShared.do?method=prepareToEditPersonFunctionShared"/>
					<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator" />
				</fr:slot>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2 thlight thleft mtop05 mbottom05"/>
				<fr:property name="columnClasses" value=",,tdclear tderror1"/>
			</fr:layout>
			<fr:destination name="cancel" path="<%="/credits.do?method=viewAnnualTeachingCredits&executionYearOid="+executionYearOid+"&teacherOid="+teacherOid %>"/>
		</fr:edit>
	</logic:empty>
	<logic:notEmpty name="personFunctionBean" property="unit">
		<logic:empty name="personFunctionBean" property="function">
			<fr:edit id="personFunctionBean2" name="personFunctionBean" action="/managePersonFunctionsShared.do?method=prepareToEditPersonFunctionShared">
				<fr:schema bundle="TEACHER_CREDITS_SHEET_RESOURCES" type="pt.ist.fenixedu.teacher.evaluation.domain.credits.util.PersonFunctionBean">
					<fr:slot name="unit.presentationNameWithParents" key="label.unit" readOnly="true"/>
					<fr:slot name="function" key="label.function" layout="menu-select-postback" required="true">
						<fr:property name="from" value="availableSharedFunctions"/>
						<fr:property name="destination" value="/managePersonFunctionsShared.do?method=prepareToEditPersonFunctionShared"/>
						<fr:property name="format" value="${typeName}"/>
					</fr:slot>
				</fr:schema>
				<fr:layout name="tabular">
					<fr:property name="classes" value="tstyle2 thlight thleft mtop05 mbottom05"/>
					<fr:property name="columnClasses" value=",,tdclear tderror1"/>
				</fr:layout>
				<fr:destination name="cancel" path="<%="/credits.do?method=viewAnnualTeachingCredits&executionYearOid="+executionYearOid+"&teacherOid="+teacherOid %>"/>
			</fr:edit>
		</logic:empty>
		<logic:notEmpty name="personFunctionBean" property="function">
			<fr:edit id="personFunctionBean3" name="personFunctionBean" action="/managePersonFunctionsShared.do?method=editPersonFunctionShared">
				<fr:schema bundle="TEACHER_CREDITS_SHEET_RESOURCES" type="pt.ist.fenixedu.teacher.evaluation.domain.credits.util.PersonFunctionBean">
					<fr:slot name="unit.presentationNameWithParents" key="label.unit" readOnly="true"/>
					<fr:slot name="function.typeName" key="label.function" readOnly="true"/>
					<fr:slot name="percentage" key="label.teacher-dfp-student.percentage" required="true"/>
				</fr:schema>
				<fr:layout name="tabular">
					<fr:property name="classes" value="tstyle2 thlight thleft mtop05 mbottom05"/>
					<fr:property name="columnClasses" value=",,tdclear tderror1"/>
				</fr:layout>
				<fr:destination name="cancel" path="<%="/credits.do?method=viewAnnualTeachingCredits&executionYearOid="+executionYearOid+"&teacherOid="+teacherOid %>"/>
			</fr:edit>
		</logic:notEmpty>
	</logic:notEmpty>
	<br/>
	<logic:notEmpty name="personFunctionBean" property="function">
		<logic:notEmpty name="personFunctionBean" property="personFunctionsShared">
			<h3><bean:message key="label.percentageDistribuition" bundle="TEACHER_CREDITS_SHEET_RESOURCES"/></h3>
			<fr:view name="personFunctionBean" property="personFunctionsShared">
				<fr:schema bundle="TEACHER_CREDITS_SHEET_RESOURCES" type="pt.ist.fenixedu.teacher.evaluation.domain.credits.util.PersonFunctionBean">
					<fr:slot name="childParty" key="label.empty" layout="view-as-image">
						<fr:property name="classes" value="column3" />
						<fr:property name="useParent" value="true" />
						<fr:property name="moduleRelative" value="false" />
						<fr:property name="contextRelative" value="true" />
						<fr:property name="imageFormat" value="/user/photo/${person.username}" />
					</fr:slot>
					<fr:slot name="childParty.presentationName" key="label.name"/>
					<fr:slot name="percentage" key="label.teacher-dfp-student.percentage"/>
				</fr:schema>
				<fr:layout name="tabular">
					<fr:property name="classes" value="tstyle2 thlight thleft mtop05 mbottom05"/>
					<fr:property name="columnClasses" value="headerTable,,"/>
				</fr:layout>
			</fr:view>
		</logic:notEmpty>
	</logic:notEmpty>
</logic:present>