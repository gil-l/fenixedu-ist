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
package pt.ist.fenixframework.plugins.remote.domain;

import org.apache.commons.lang.StringUtils;

public class RemoteHost extends RemoteHost_Base {

    public RemoteHost() {
        super();
        setRemoteSystem(RemoteSystem.getInstance());
        setAllowInvocationAccess(Boolean.FALSE);
    }

    public RemoteHost(final String username, final String password, final Boolean allowInvocationAccess) {
        this();
        setUsername(username);
        setPassword(password);
        setAllowInvocationAccess(allowInvocationAccess);
    }

    public void delete() {
        setRemoteSystem(null);
        deleteDomainObject();
    }

    @Override
    public Boolean getAllowInvocationAccess() {
        final Boolean value = super.getAllowInvocationAccess();
        return value == null ? Boolean.FALSE : value;
    }

    public boolean matches(final String username, final String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return false;
        }
        return username.equalsIgnoreCase(getUsername()) && password.equals(getPassword());
    }

    @Deprecated
    public boolean hasName() {
        return getName() != null;
    }

    @Deprecated
    public boolean hasPassword() {
        return getPassword() != null;
    }

    @Deprecated
    public boolean hasUsername() {
        return getUsername() != null;
    }

    @Deprecated
    public boolean hasRemoteSystem() {
        return getRemoteSystem() != null;
    }

    @Deprecated
    public boolean hasAllowInvocationAccess() {
        return getAllowInvocationAccess() != null;
    }

}
