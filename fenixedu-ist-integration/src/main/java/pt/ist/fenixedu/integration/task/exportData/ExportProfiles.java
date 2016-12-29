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
package pt.ist.fenixedu.integration.task.exportData;

import java.util.Optional;
import pt.ist.fenixedu.contracts.domain.LegacyRoleUtils;
import pt.ist.fenixframework.Atomic.TxMode;

import java.io.FileOutputStream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.joda.time.LocalDate;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import static org.fenixedu.commons.stream.StreamUtils.toJsonArray;

@Task(englishTitle = "Export user profiles to shared json file with other applications.")
public class ExportProfiles extends CustomTask {

    @Override
    public TxMode getTxMode() {
        return TxMode.READ;
    }

    @Override
    public void runTask() throws Exception {
        final String profiles =
                Bennu.getInstance().getUserSet().stream().filter(u -> u.getProfile() != null)
                        .map(u -> toJsonObject(u.getProfile())).collect(toJsonArray()).toString();
        final byte[] bytes = profiles.getBytes();
        output("profiles.json", bytes);
        try (final FileOutputStream fos = new FileOutputStream("/afs/ist.utl.pt/ciist/fenix/fenix015/ist/profiles.json")) {
            fos.write(bytes);
        }
    }

    private JsonObject toJsonObject(final UserProfile up) {
        final JsonObject object = new JsonObject();
        object.addProperty("username", up.getUser().getUsername());
        object.addProperty("givenNames", up.getGivenNames());
        object.addProperty("familyNames", up.getFamilyNames());
        object.addProperty("displayName", up.getDisplayName());
        object.addProperty("email", up.getEmail());
        Optional<LocalDate> expiration = up.getUser().getExpiration();
        object.addProperty("expiration", expiration.map(Object::toString).orElse(null));
        object.add("roles", LegacyRoleUtils.mainRoleKeys(up.getUser()).stream().map(JsonPrimitive::new).collect(toJsonArray()));
        return object;
    }

}
