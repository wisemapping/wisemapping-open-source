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

package com.wisemapping.security;

import com.wisemapping.dao.UserManager;
import com.wisemapping.model.UserLogin;
import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;
import org.acegisecurity.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

public class CustomTokenBasedRememberMeServices extends
        TokenBasedRememberMeServices {
    private UserManager userManager;

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
     if(authentication!=null)
         super.logout(request, response, authentication);
     else
     {
         logger.debug("Session Already Expired. Authentication is null");
         response.addCookie(makeCancelCookie(request));
     }
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        final User user = (User)successfulAuthentication.getPrincipal();

        final UserLogin userLogin = new UserLogin();
        final Calendar now = Calendar.getInstance();
        userLogin.setLoginDate(now);
        userLogin.setEmail(user.getUsername());
        userManager.auditLogin(userLogin);

        super.loginSuccess(request, response, successfulAuthentication);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}