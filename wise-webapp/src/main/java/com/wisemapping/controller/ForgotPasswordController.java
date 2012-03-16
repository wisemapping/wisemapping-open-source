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

import com.wisemapping.exceptions.EmailNotExistsException;
import com.wisemapping.service.InvalidUserEmailException;
import com.wisemapping.service.UserService;
import com.wisemapping.view.ForgotPasswordBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;

public class ForgotPasswordController
    extends BaseSimpleFormController
{

    //~ Instance fields ......................................................................................

    private UserService userService;

    //~ Methods ..............................................................................................

    public ModelAndView onSubmit(Object command)
            throws ServletException, EmailNotExistsException {
        
        final ForgotPasswordBean bean = (ForgotPasswordBean) command;
        try {
            userService.sendEmailPassword(bean.getEmail());
        } catch (InvalidUserEmailException e) {
            throw new EmailNotExistsException(e);
        }
        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    public void setUserService(UserService userService)
    {
        this.userService = userService;
    }
}
