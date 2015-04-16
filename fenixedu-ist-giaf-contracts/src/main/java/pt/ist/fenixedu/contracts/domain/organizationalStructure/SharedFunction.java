/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST GIAF Contracts.
 *
 * FenixEdu IST GIAF Contracts is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST GIAF Contracts is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST GIAF Contracts.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.contracts.domain.organizationalStructure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.fenixedu.academic.domain.organizationalStructure.Accountability;
import org.fenixedu.academic.domain.organizationalStructure.AccountabilityTypeEnum;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.util.MultiLanguageString;
import org.joda.time.YearMonthDay;

public class SharedFunction extends SharedFunction_Base {

    public SharedFunction(MultiLanguageString functionName, YearMonthDay beginDate, YearMonthDay endDate, FunctionType type,
            Unit unit, BigDecimal credits) {
        setValues(functionName, beginDate, endDate, type, unit, AccountabilityTypeEnum.MANAGEMENT_FUNCTION);
        setCredits(credits);
    }

    @Override
    public boolean isSharedFunction() {
        return true;
    }

    @Override
    public void setCredits(BigDecimal credits) {
        super.setCredits(credits);
        for (PersonFunctionShared personFunctionShared : getPersonFunctionsShared()) {
            personFunctionShared.recalculateCredits();
        }
    }

    public List<PersonFunctionShared> getPersonFunctionsShared() {
        List<PersonFunctionShared> personFunctions = new ArrayList<PersonFunctionShared>();
        for (Accountability accountability : getAccountabilitiesSet()) {
            if (accountability instanceof PersonFunctionShared) {
                personFunctions.add((PersonFunctionShared) accountability);
            }
        }
        return personFunctions;
    }

}
