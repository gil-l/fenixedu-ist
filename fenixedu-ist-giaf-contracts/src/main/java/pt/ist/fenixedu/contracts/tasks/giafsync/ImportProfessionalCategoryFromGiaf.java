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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.fenixedu.academic.util.MultiLanguageString;
import org.slf4j.Logger;

import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.ProfessionalCategory;
import pt.ist.fenixedu.contracts.domain.util.CategoryType;
import pt.ist.fenixedu.contracts.persistenceTierOracle.Oracle.PersistentSuportGiaf;
import pt.ist.fenixedu.contracts.tasks.giafsync.GiafSync.MetadataProcessor;

class ImportProfessionalCategoryFromGiaf implements MetadataProcessor {
    private static final String NDOCENTE = "NDOCENTE";

    private static final String INVEST = "INVEST";

    private static final String DOCENTE = "DOCENTE";

    private static final String BOLSEIRO = "BOLSEIRO";

    private static final String PSERV = "PSERV";

    public ImportProfessionalCategoryFromGiaf() {

    }

    @Override
    public void processChanges(GiafMetadata metadata, PrintWriter log, Logger logger) throws Exception {
        int updatedCategories = 0;
        int newCategories = 0;

        PersistentSuportGiaf oracleConnection = PersistentSuportGiaf.getInstance();
        PreparedStatement preparedStatement = oracleConnection.prepareStatement(getQuery());
        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            String giafId = result.getString("emp_cat_func");
            String descriptionString = result.getString("cat_func_dsc");
            MultiLanguageString description = new MultiLanguageString(MultiLanguageString.pt, descriptionString);
            CategoryType categoryType = null;
            String agrupamento = (result.getString("agrupamento"));
            if (agrupamento != null) {
                if (agrupamento.equals(BOLSEIRO)) {
                    categoryType = CategoryType.GRANT_OWNER;
                } else if (agrupamento.equals(DOCENTE)) {
                    categoryType = CategoryType.TEACHER;
                } else if (agrupamento.equals(INVEST)) {
                    categoryType = CategoryType.RESEARCHER;
                } else if (agrupamento.equals(NDOCENTE)) {
                    categoryType = CategoryType.EMPLOYEE;
                } else if (agrupamento.equals(PSERV)) {
                }
            }
            if (categoryType == null) {
                logger.error("Erro no agrupamento da categoria. Categoria: " + giafId + " - " + descriptionString
                        + ". Agrupamento: " + agrupamento);
            } else {
                ProfessionalCategory professionalCategory = metadata.category(giafId);
                if (professionalCategory != null) {
                    if ((!professionalCategory.getCategoryType().equals(categoryType))
                            || (!professionalCategory.getName().getContent().equalsIgnoreCase(description.getContent()))) {
                        professionalCategory.edit(description, categoryType);
                        updatedCategories++;
                    }
                } else {
                    metadata.registerCategory(giafId, categoryType, description);
                    newCategories++;
                }
            }

        }
        result.close();
        preparedStatement.close();
        oracleConnection.closeConnection();
        log.printf("Categories: %d updated, %d new\n", updatedCategories, newCategories);
    }

    protected String getQuery() {
        return "SELECT a.emp_cat_func, a.cat_func_dsc, a.agrupamento FROM sltcatfunc a where emp_cat_func in (select distinct emp_cat_func from sldemp24 where emp_sit is not null) or emp_cat_func in (select distinct emp_cat_func from sldemp25) or emp_cat_func in (select distinct emp_cat_func from sltprog_vinc) or emp_cat_func in (select distinct emp_cat_func from sldemp01)";
    }
}
