/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Integration.
 *
 * FenixEdu IST Integration is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Integration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Integration.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.integration.ui.struts.action.person;

import java.util.Collections;
import java.util.List;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.academic.util.MultiLanguageString;

import pt.ist.fenixedu.integration.domain.UnitFile;

public class PersonFileSourceBean implements PersonFileSource {

    /**
     * Default serial id.
     */
    private static final long serialVersionUID = 1L;

    private Unit unit;
    private int count;

    public PersonFileSourceBean(Unit unit) {
        this.unit = unit;
        this.count = -1;
    }

    @Override
    public MultiLanguageString getName() {
        return getUnit().getNameI18n();
    }

    public Unit getUnit() {
        return this.unit;
    }

    @Override
    public int getCount() {
        if (this.count < 0) {
            this.count = UnitFile.getAccessibileFiles(getUnit(), AccessControl.getPerson()).size();
        }

        return this.count;
    }

    @Override
    public List<PersonFileSource> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAllowedToUpload(Person person) {
        return getUnit().getAllowedPeopleToUploadFilesSet().contains(person);
    }

}
