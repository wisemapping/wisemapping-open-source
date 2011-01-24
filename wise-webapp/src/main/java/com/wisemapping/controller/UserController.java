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

// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2007-04-03 09:29:20 (-0300), by: nachomanz. $Revision$
// ...........................................................................................................

package com.wisemapping.controller;

import com.wisemapping.model.User;
import com.wisemapping.service.UserService;
import com.wisemapping.view.UserBean;
import com.wisemapping.exceptions.WiseMappingException;
import org.springframework.web.servlet.ModelAndView;

public class UserController
        extends CaptchaFormController {

    //~ Instance fields ......................................................................................

    private UserService userService;

    //~ Methods ..............................................................................................

    public ModelAndView onSubmit(Object command) throws WiseMappingException {
        final UserBean userBean = ((UserBean) command);

        if (userBean != null) {
            final User user = new User();
            // trim() the email email in order to remove spaces
            user.setEmail(userBean.getEmail().trim());
            user.setUsername(userBean.getUsername());
            user.setFirstname(userBean.getFirstname());
            user.setLastname(userBean.getLastname());
            user.setPassword(userBean.getPassword());
            userService.createUser(user);
        }

        return new ModelAndView(getSuccessView());
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
