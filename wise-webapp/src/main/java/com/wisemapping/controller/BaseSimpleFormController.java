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

import com.wisemapping.model.MindMap;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;


public class BaseSimpleFormController extends SimpleFormController
{

    private MindmapService mindmapService;
    private UserService userService;
    private String errorView;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public MindmapService getMindmapService() {
        return mindmapService;
    }

    public void setMindmapService(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }

    public String getErrorView() {
        return errorView;
    }

    public void setErrorView(String errorView) {
        this.errorView = errorView;
    }

    @Override protected org.springframework.web.servlet.ModelAndView showForm(javax.servlet.http.HttpServletRequest httpServletRequest, javax.servlet.http.HttpServletResponse httpServletResponse, org.springframework.validation.BindException bindException) throws java.lang.Exception
     {
        final ModelAndView view = super.showForm(httpServletRequest, httpServletResponse,bindException);
        final String viewName = getErrorView();
        if(viewName !=null && bindException.getAllErrors().size()>0)
        {
            view.setViewName(viewName);
            view.addObject("errorView",true);
        }
        return view;
    }

    /* TODO codigo repetido en BaseMultiActionController */
     protected MindMap getMindmapFromRequest(HttpServletRequest request) {
        final String mapIdStr = request.getParameter(BaseMultiActionController.MAP_ID_PARAMNAME);
        assert mapIdStr != null : "mapId parameter can not be null";
        logger.info("MapIdStr:" + mapIdStr);
        int mapId = Integer.parseInt(mapIdStr);
        return mindmapService.getMindmapById(mapId);
    }
}


