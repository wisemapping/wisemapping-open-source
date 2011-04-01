/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package com.wisemapping.controller;

import com.wisemapping.model.UserLogin;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

public class LoginController
        extends BaseMultiActionController {
    private String driver;

    protected ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        // Reload user only in case of beeing necessary...
        final User user = Utils.getUser(request);

        ModelAndView result;
        if (user != null) {
            result = new ModelAndView("forward:/c/mymaps.htm");

        } else {
            result = new ModelAndView("login");
            result.addObject("isHsql", driver.indexOf("hsql")!=-1);
        }

        return result;
    }

    public ModelAndView auditLogin(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Login Controller: auditLogin");

        final String username = request.getUserPrincipal().getName();

        final UserLogin userLogin = new UserLogin();
        final Calendar now = Calendar.getInstance();
        userLogin.setLoginDate(now);
        userLogin.setEmail(username);
        //userManager.auditLogin(userLogin);

        return new ModelAndView("forward:/c/mymaps.htm");
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
}
