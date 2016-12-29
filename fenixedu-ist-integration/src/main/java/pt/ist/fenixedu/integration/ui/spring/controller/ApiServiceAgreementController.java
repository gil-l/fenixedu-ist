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
package pt.ist.fenixedu.integration.ui.spring.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.portal.servlet.PortalLayoutInjector;
import org.fenixedu.bennu.spring.I18NBean;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

@SpringApplication(group = "!#developers", path = "api-service-agreement", title = "api.service.agreement.title")
@SpringFunctionality(app = ApiServiceAgreementController.class, title = "api.service.agreement.title")
@Controller
@RequestMapping("/api-service-agreement")
public class ApiServiceAgreementController {

    @Autowired
    I18NBean i18nBean;

    @RequestMapping(method = RequestMethod.GET)
    public String home(Model model) {
        model.addAttribute("serviceAgreement", getServiceAgreementHtml());
        return "fenixedu-ist-integration/public/agreeServiceAgreement";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String agree(Model model, @RequestParam Boolean agreedServiceAgreement) {
        if (agreedServiceAgreement) {
            addDeveloperRole(Authenticate.getUser());
            return "redirect:/personal";
        }
        return "redirect:/api-service-agreement";
    }

    @Atomic
    private void addDeveloperRole(User user) {
        Group.dynamic("developers").mutator().grant(user);
    }

    @RequestMapping(method = RequestMethod.GET, value = "show")
    public String showServiceAgreementHtml(Model model, HttpServletRequest request) {
        String serviceAgreementHtml = getServiceAgreementHtml();
        model.addAttribute("serviceAgreement", serviceAgreementHtml);
        model.addAttribute("serviceAgreementChecksum", Hashing.md5().newHasher().putString(serviceAgreementHtml, Charsets.UTF_8)
                .hash().toString());
        PortalLayoutInjector.skipLayoutOn(request);
        return "fenixedu-ist-integration/public/showServiceAgreement";
    }

    private String getServiceAgreementHtml() {
        final InputStream resourceAsStream = getClass().getResourceAsStream("/api/serviceAgreement.html");
        if (resourceAsStream == null) {
            return i18nBean.message("oauthapps.default.service.agreement");
        }
        try {
            return new String(ByteStreams.toByteArray(resourceAsStream));
        } catch (IOException e) {
            return i18nBean.message("oauthapps.default.service.agreement");
        }
    }

}
