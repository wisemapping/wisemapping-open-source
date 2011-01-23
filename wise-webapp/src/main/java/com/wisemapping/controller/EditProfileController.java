/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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
import com.wisemapping.security.Utils;
import com.wisemapping.service.UserService;
import com.wisemapping.view.UserBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EditProfileController extends BaseSimpleFormController {


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        final User model = Utils.getUser(httpServletRequest);
        return new UserBean(model);
    }

    //~ Methods ..............................................................................................

    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
                                 BindException errors)
            throws ServletException {
        final UserBean bean = (UserBean) command;
        final UserService userService = this.getUserService();

        // Reload user only in case of beeing necessary...
        final User model = Utils.getUser(request);
        final User user = userService.reloadUser(model);

        user.setFirstname(bean.getFirstname());
        user.setLastname(bean.getLastname());
        // trim() the email email in order to remove spaces
        user.setEmail(bean.getEmail().trim());
        user.setAllowSendEmail(bean.isAllowSendEmail());

        // Sync Acegi user and DB user
        model.setAllowSendEmail(bean.isAllowSendEmail());
        userService.updateUser(user);

        return new ModelAndView(getSuccessView());
    }
}
