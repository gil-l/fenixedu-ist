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

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.util.MultiLanguageString;

public interface PersonFileSource extends Serializable {

    public static Comparator<PersonFileSource> COMPARATOR = new Comparator<PersonFileSource>() {

        @Override
        public int compare(PersonFileSource o1, PersonFileSource o2) {
            int c = o1.getName().compareTo(o2.getName());
            if (c != 0) {
                return c;
            } else {
                int o1Count = o1.getCount();
                int o2Count = o2.getCount();

                if (o1Count < o2Count) {
                    return -1;
                } else if (o1Count > o2Count) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

    };

    public MultiLanguageString getName();

    public List<PersonFileSource> getChildren();

    public int getCount();

    public boolean isAllowedToUpload(Person person);
}
