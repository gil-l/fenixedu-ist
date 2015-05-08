/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST QUC.
 *
 * FenixEdu IST QUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST QUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST QUC.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package pt.ist.fenixedu.quc.ui.struts.action.gep.inquiries;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.EntryPoint;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixedu.quc.domain.InquiryResult;
import pt.ist.fenixedu.quc.dto.ResultsFileBean;

import com.google.common.io.CharStreams;

/**
 * @author - Ricardo Rodrigues (ricardo.rodrigues@ist.utl.pt)
 * 
 */
@StrutsFunctionality(app = GepInquiriesApp.class, path = "upload-results", titleKey = "link.inquiries.uploadResults")
@Mapping(path = "/uploadInquiriesResults", module = "gep")
@Forwards(@Forward(name = "prepareUploadPage", path = "/gep/inquiries/uploadInquiriesResults.jsp"))
public class UploadInquiriesResultsDA extends FenixDispatchAction {

    private static final Logger logger = LoggerFactory.getLogger(UploadInquiriesResultsDA.class);

    @EntryPoint
    public ActionForward prepare(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        request.setAttribute("uploadFileBean", new ResultsFileBean());
        return mapping.findForward("prepareUploadPage");
    }

    public ActionForward submitResultsFile(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        ResultsFileBean resultsBean = getRenderedObject("uploadFileBean");
        RenderUtils.invalidateViewState("uploadFileBean");

        try {
            final String stringResults = readFile(resultsBean);
            if (resultsBean.getNewResults()) {
                InquiryResult.importResults(stringResults, resultsBean.getResultsDate());
            } else {
                InquiryResult.updateRows(stringResults, resultsBean.getResultsDate());
            }
            request.setAttribute("success", "true");
        } catch (IOException e) {
            addErrorMessage(request, e.getMessage(), e.getMessage());
        } catch (DomainException e) {
            logger.error(e.getMessage(), e);
            addErrorMessage(request, e.getKey(), e.getKey(), e.getArgs());
        }
        return prepare(mapping, actionForm, request, response);
    }

    private String readFile(ResultsFileBean resultsBean) throws IOException {
        return CharStreams.toString(new InputStreamReader(resultsBean.getInputStream()));
    }
}
