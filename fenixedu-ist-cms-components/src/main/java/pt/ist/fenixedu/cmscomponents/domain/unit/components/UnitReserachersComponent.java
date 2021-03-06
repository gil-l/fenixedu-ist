/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST CMS Components.
 *
 * FenixEdu IST CMS Components is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST CMS Components is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST CMS Components.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.cmscomponents.domain.unit.components;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;
import org.fenixedu.commons.i18n.I18N;

import pt.ist.fenixedu.cmscomponents.FenixEduIstCmsComponentsConfiguration;

@ComponentType(name = "Unit Researchers", description = "Researchers for a Unit")
public class UnitReserachersComponent extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        globalContext.put("researchers", researchers(unit(page)));
        globalContext.put("researcher", researchers(unit(page)).map(Person::getUsername).collect(Collectors.joining(",")));
        globalContext.put("sotisUrl", FenixEduIstCmsComponentsConfiguration.getConfiguration().sotisURL());
        globalContext.put("language", I18N.getLocale().toLanguageTag());
        globalContext.put("dataKey", "list");
    }

    Stream<Person> researchers(Unit unit) {
        return unit.getAllSubUnits().stream().flatMap(subunit -> (Stream<Person>) subunit.getChildParties(Person.class).stream());
    }
}
