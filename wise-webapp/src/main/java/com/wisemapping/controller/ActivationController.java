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

import com.wisemapping.service.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ActivationController
    implements Controller, InitializingBean
{

    //~ Instance fields ......................................................................................
    private UserService userService;

    //~ Methods ..............................................................................................

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        ModelAndView modelAndView;
        try
        {
            final String code = request.getParameter("code");
            userService.activateAcount(Long.parseLong(code));
            modelAndView = new ModelAndView("activationAccountConfirmation");
        }
        catch (Throwable exc)
        {
            // TODO Fix it !
            exc.printStackTrace();
            // Any type of error
            modelAndView = new ModelAndView("activationAccountConfirmationFail");
        }
        return modelAndView;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
            throws Exception {
        if (userService == null) {
            throw new RuntimeException("userService was not set!");
        }
    }
}
