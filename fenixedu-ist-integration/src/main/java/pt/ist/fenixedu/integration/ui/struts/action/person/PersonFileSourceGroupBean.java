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

import java.util.ArrayList;
import java.util.List;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.util.MultiLanguageString;

public class PersonFileSourceGroupBean implements PersonFileSource {

    /**
     * Default serial id.
     */
    private static final long serialVersionUID = 1L;

    private MultiLanguageString name;
    private List<PersonFileSource> children;
    private int count;

    public PersonFileSourceGroupBean(MultiLanguageString name) {
        this.name = name;
        this.children = new ArrayList<PersonFileSource>();
        this.count = -1; // not initialized
    }

    public PersonFileSourceGroupBean(Unit unit) {
        this(unit.getNameI18n());
    }

    @Override
    public MultiLanguageString getName() {
        return name;
    }

    @Override
    public List<PersonFileSource> getChildren() {
        return this.children;
    }

    public PersonFileSourceGroupBean add(PersonFileSourceBean child) {
        this.children.add(child);
        return this;
    }

    @Override
    public int getCount() {
        if (this.count < 0) {
            this.count = 0;

            for (PersonFileSource child : getChildren()) {
                this.count += child.getCount();
            }
        }

        return this.count;
    }

    @Override
    public boolean isAllowedToUpload(Person person) {
        for (PersonFileSource child : getChildren()) {
            if (child.isAllowedToUpload(person)) {
                return true;
            }
        }
        return false;
    }
}
