/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
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
