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

package com.wisemapping.controller;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import com.wisemapping.view.ImportMapBean;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImportController
        extends BaseSimpleFormController {

    //~ Methods ..............................................................................................
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return new ImportMapBean();
    }

    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws ServletException, WiseMappingException, ImporterException {
        final ImportMapBean bean = (ImportMapBean) command;
 
        User user = Utils.getUser();
        final UserService userService = this.getUserService();

        user = userService.getUserBy(user.getId());
        final MindMap mindMap = bean.getImportedMap();
        mindMap.setOwner(user);

        final MindmapService mindmapService = this.getMindmapService();
        mindmapService.addMindmap(mindMap,user);

        final StringBuilder redirectionTo = new StringBuilder("redirect:editor.htm?mapId=");
        redirectionTo.append(mindMap.getId());
        redirectionTo.append("&action=open");
        return new ModelAndView(redirectionTo.toString());
    }

     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
        throws ServletException {
        // to actually be able to convert Multipart instance to a String
        // we have to register a custom editor
        binder.registerCustomEditor(String.class, new StringMultipartFileEditor());
        // now Spring knows how to handle multipart object and convert them
    }
}
