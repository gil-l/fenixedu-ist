/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Pre Bolonha.
 *
 * FenixEdu IST Pre Bolonha is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Pre Bolonha is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Pre Bolonha.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.service.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.fenixedu.academic.domain.Coordinator;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.service.services.exceptions.NotAuthorizedException;
import org.fenixedu.academic.util.SituationName;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

import pt.ist.fenixframework.FenixFramework;

/**
 * @author Nuno Nunes (nmsn@rnl.ist.utl.pt)
 * @author Joana Mota (jccm@rnl.ist.utl.pt)
 */
public class ReadCandidatesForSelectionAuthorizationFilter extends Filtro {

    public static final ReadCandidatesForSelectionAuthorizationFilter instance =
            new ReadCandidatesForSelectionAuthorizationFilter();

    public ReadCandidatesForSelectionAuthorizationFilter() {

    }

    public void execute(String executionDegreeID, List<SituationName> situationNames) throws NotAuthorizedException {

        User id = Authenticate.getUser();
        if ((id != null && !containsRoleType(id)) || (id != null && !hasPrivilege(id, executionDegreeID)) || (id == null)) {
            throw new NotAuthorizedException();
        }
    }

    /**
     * @return The Needed Roles to Execute The Service
     */
    @Override
    protected Collection<RoleType> getNeededRoleTypes() {
        List<RoleType> roles = new ArrayList<RoleType>();
        roles.add(RoleType.MASTER_DEGREE_ADMINISTRATIVE_OFFICE);
        roles.add(RoleType.COORDINATOR);
        return roles;
    }

    /**
     * @param id
     * @param argumentos
     * @return
     */
    private boolean hasPrivilege(User id, String executionDegreeID) {

        ExecutionDegree executionDegree = null;

        // Read The DegreeCurricularPlan
        try {

            executionDegree = FenixFramework.getDomainObject(executionDegreeID);

        } catch (Exception e) {
            return false;
        }

        if (executionDegree == null) {
            return false;
        }

        if (RoleType.MASTER_DEGREE_ADMINISTRATIVE_OFFICE.isMember(id)) {
            if (executionDegree.getDegreeCurricularPlan().getDegree().getDegreeType().isPreBolonhaMasterDegree()) {

                return true;
            }
            return false;

        }

        if (RoleType.COORDINATOR.isMember(id)) {
            // modified by Tânia Pousão
            Collection<Coordinator> coodinatorsList = executionDegree.getCoordinatorsListSet();
            if (coodinatorsList == null) {
                return false;
            }
            Iterator<Coordinator> listIterator = coodinatorsList.iterator();
            while (listIterator.hasNext()) {
                Coordinator coordinator = listIterator.next();

                if (coordinator.getPerson() == id.getPerson()) {
                    return true;
                }
            }
        }
        return false;
    }

}