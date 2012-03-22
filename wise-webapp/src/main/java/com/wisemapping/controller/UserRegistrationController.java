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

import com.wisemapping.model.User;
import com.wisemapping.service.UserService;
import com.wisemapping.view.UserBean;
import com.wisemapping.exceptions.WiseMappingException;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UserRegistrationController
        extends BaseSimpleFormController {

    //~ Instance fields ......................................................................................

    private boolean emailConfirmEnabled;
    private UserService userService;
    private ReCaptcha captchaService;
    private boolean captchaEnabled;

    //~ Methods ..............................................................................................


    public boolean isEmailConfirmEnabled() {
        return emailConfirmEnabled;
    }

    public void setEmailConfirmEnabled(boolean emailConfirmEnabled) {
        this.emailConfirmEnabled = emailConfirmEnabled;
    }

    public ModelAndView onSubmit(@Nullable Object command) throws WiseMappingException {
        final UserBean userBean = ((UserBean) command);

        if (userBean != null) {
            final User user = new User();
            // trim() the email email in order to remove spaces
            user.setEmail(userBean.getEmail().trim());
            user.setUsername(userBean.getUsername());
            user.setFirstname(userBean.getFirstname());
            user.setLastname(userBean.getLastname());
            user.setPassword(userBean.getPassword());
            userService.createUser(user, emailConfirmEnabled);
        }

        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("confirmByEmail", emailConfirmEnabled);
        return new ModelAndView(getSuccessView(), model);
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCaptchaService(@NotNull final ReCaptcha captchaService) {
        this.captchaService = captchaService;
    }

    public ReCaptcha getCaptchaService() {
        return captchaService;
    }

    public boolean isCaptchaEnabled() {
        return captchaEnabled;
    }

    public void setCaptchaEnabled(boolean captchaEnabled) {
        this.captchaEnabled = captchaEnabled;
    }

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {

        super.onBindAndValidate(request, command, errors);
        // If captcha is enabled, generate it ...
        if (isCaptchaEnabled()) {

            final String challenge = request.getParameter("recaptcha_challenge_field");
            final String uresponse = request.getParameter("recaptcha_response_field");

            final String remoteAddr = request.getRemoteAddr();
            final ReCaptchaResponse reCaptchaResponse = captchaService.checkAnswer(remoteAddr, challenge, uresponse);
            if (!reCaptchaResponse.isValid()) {
                errors.rejectValue("captcha", Messages.CAPTCHA_ERROR);
            }
        }
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException bindException) throws Exception {
        final ModelAndView modelAndView = super.showForm(request, response, bindException);

        // If captcha is enabled, generate it ...
        if (isCaptchaEnabled()) {
            final Properties prop = new Properties();
            prop.put("theme", "white");
            final String captchaHtml = captchaService.createRecaptchaHtml(null, prop);
            request.setAttribute("captchaHtml", captchaHtml);
            request.setAttribute("captchaEnabled", true);
        }
        return modelAndView;
    }

}
