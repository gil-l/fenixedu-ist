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
package pt.ist.fenixedu.contracts.tasks.giafsync;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.util.MultiLanguageString;
import org.slf4j.Logger;

import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.ProfessionalRegime;
import pt.ist.fenixedu.contracts.domain.util.CategoryType;
import pt.ist.fenixedu.contracts.persistenceTierOracle.Oracle.PersistentSuportGiaf;
import pt.ist.fenixedu.contracts.tasks.giafsync.GiafSync.MetadataProcessor;

class ImportProfessionalRegimesFromGiaf implements MetadataProcessor {
    public ImportProfessionalRegimesFromGiaf() {

    }

    @Override
    public void processChanges(GiafMetadata metadata, PrintWriter log, Logger logger) throws Exception {
        int updatedRegimes = 0;
        int newRegimes = 0;

        PersistentSuportGiaf oracleConnection = PersistentSuportGiaf.getInstance();
        String query = getQuery();
        PreparedStatement preparedStatement = oracleConnection.prepareStatement(query);
        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            String giafId = result.getString("emp_regime");
            String regimeName = result.getString("regime_dsc");
            Integer weighting = result.getInt("regime_pond");
            BigDecimal fullTimeEquivalent = result.getBigDecimal("valor_eti");

            CategoryType categoryType = null;
            if (!StringUtils.isBlank(regimeName)) {
                if (regimeName.contains("Bolseiro")) {
                    categoryType = CategoryType.GRANT_OWNER;
                } else if (regimeName.contains("Investigador")) {
                    categoryType = CategoryType.RESEARCHER;
                } else if (regimeName.contains("Pessoal não Docente") || regimeName.contains("Pess. não Doc.")
                        || regimeName.contains("Pessoal Não Docente")) {
                    categoryType = CategoryType.EMPLOYEE;
                } else if (regimeName.contains("(Docentes)") || regimeName.contains("(Doc)")) {
                    categoryType = CategoryType.TEACHER;
                }
            }

            ProfessionalRegime professionalRegime = metadata.regime(giafId);
            MultiLanguageString name = new MultiLanguageString(MultiLanguageString.pt, regimeName);
            if (professionalRegime != null) {
                if (!isEqual(professionalRegime, name, weighting, fullTimeEquivalent, categoryType)) {
                    professionalRegime.edit(name, weighting, fullTimeEquivalent, categoryType);
                    updatedRegimes++;
                }
            } else {
                metadata.registerRegime(giafId, weighting, fullTimeEquivalent, categoryType, name);
                newRegimes++;
            }
        }
        result.close();
        preparedStatement.close();
        oracleConnection.closeConnection();
        log.printf("Regimes: %d updated, %d new\n", updatedRegimes, newRegimes);
    }

    private boolean isEqual(ProfessionalRegime professionalRegime, MultiLanguageString name, Integer weighting,
            BigDecimal fullTimeEquivalent, CategoryType categoryType) {
        return professionalRegime.getName().getContent().equalsIgnoreCase(name.getContent())
                && Objects.equals(professionalRegime.getFullTimeEquivalent(), fullTimeEquivalent)
                && Objects.equals(professionalRegime.getWeighting(), weighting)
                && Objects.equals(professionalRegime.getCategoryType(), categoryType);
    }

    protected String getQuery() {
        return "SELECT a.emp_regime, a.regime_dsc, a.regime_pond, a.valor_eti FROM sltregimes a";
    }
}
