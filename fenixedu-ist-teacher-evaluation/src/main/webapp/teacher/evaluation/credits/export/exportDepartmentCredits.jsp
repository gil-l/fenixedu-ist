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
<%@ taglib uri="http://jakarta.apache.org/taglibs/struts-example-1.0" prefix="app" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>
<html:xhtml/>

<html:messages id="message" message="true">
	<span class="error">
		<bean:write name="message" filter="false"/>
	</span>
</html:messages>


<logic:present name="departmentCreditsBean">
	<fr:edit id="departmentCreditsBean" name="departmentCreditsBean" action="/departmentCredits.do?method=exportDepartmentCredits">
		<fr:schema bundle="TEACHER_CREDITS_SHEET_RESOURCES" type="pt.ist.fenixedu.teacher.evaluation.domain.credits.util.DepartmentCreditsBean">
			<fr:slot name="department" key="label.department" layout="menu-select">
				<fr:property name="from" value="availableDepartments"/>
				<fr:property name="format" value="${name.content}"/>
			</fr:slot>
			<fr:slot name="executionYear" key="label.executionYear" layout="menu-select" required="true">
				<fr:property name="providerClass" value="org.fenixedu.academic.ui.renderers.providers.ExecutionYearsProvider" />
				<fr:property name="format" value="${qualifiedName}" />
				<fr:property name="nullOptionHidden" value="true" />
			</fr:slot>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle5 thlight mtop15" />
			<fr:property name="columnClasses" value=",,tdclear tderror1" />
		</fr:layout>
	</fr:edit>
</logic:present>