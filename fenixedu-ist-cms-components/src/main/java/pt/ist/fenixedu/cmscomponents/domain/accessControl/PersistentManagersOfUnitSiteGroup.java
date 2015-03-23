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
package pt.ist.fenixedu.cmscomponents.domain.accessControl;

import java.util.Optional;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.cms.domain.Site;

public class PersistentManagersOfUnitSiteGroup extends PersistentManagersOfUnitSiteGroup_Base {
    protected PersistentManagersOfUnitSiteGroup(Site site) {
        super();
        setUnitSite(site);
    }

    @Override
    public Group toGroup() {
        return ManagersOfUnitSiteGroup.get(getUnitSite());
    }

    @Override
    protected void gc() {
        setUnitSite(null);
        super.gc();
    }

    public static PersistentManagersOfUnitSiteGroup getInstance(final Site site) {
        return singleton(() -> Optional.ofNullable(site.getManagersOfUnitSiteGroup()),
                () -> new PersistentManagersOfUnitSiteGroup(site));
    }

}