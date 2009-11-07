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

import com.wisemapping.model.ColaborationEmail;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.model.UserRole;
import com.wisemapping.security.Utils;
import com.wisemapping.service.InvalidColaboratorException;
import com.wisemapping.view.MindMapBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MindmapSharingController extends BaseMultiActionController {
    private static final String COLABORATOR_ID = "colaboratorId";          

    public ModelAndView addCollaborator(HttpServletRequest request, HttpServletResponse response)
            throws InvalidColaboratorException {
        logger.info("Sharing Controller: add collaborators action");
        addColaborator(request, UserRole.COLLABORATOR);
        return new ModelAndView("closeDialog");
    }

    public ModelAndView addViewer(HttpServletRequest request, HttpServletResponse response)
            throws InvalidColaboratorException {
        logger.info("Sharing Controller: add viewer action");
        addColaborator(request, UserRole.VIEWER);
        return new ModelAndView("closeDialog");
    }

    public ModelAndView removeCollaborator(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Sharing Controller: remove collaborator action");
        final MindMap mindmap = removeColaborator(request);
        return new ModelAndView("mindmapCollaborator", "mindmap", new MindMapBean(mindmap));
    }

    private MindMap removeColaborator(HttpServletRequest request) {
        final MindMap mindmap = getMindmapFromRequest(request);
        final String colaboratorId = request.getParameter(COLABORATOR_ID);
        long colaborator = Long.parseLong(colaboratorId);
        getMindmapService().removeColaboratorFromMindmap(mindmap, colaborator);
        return mindmap;
    }

    private String[] getEmailsToAdd(final HttpServletRequest request) {
        final String[] result;
        String collaboratorEmails = request.getParameter(MINDMAP_EMAILS_PARAMNAME);
        if (collaboratorEmails != null && collaboratorEmails.trim().length() > 0) {
            result = collaboratorEmails.split("\\s*[,|\\;|\\s]\\s*");
        } else {
            result = null;
        }
        return result;
    }

    private MindMapBean addColaborator(HttpServletRequest request, UserRole role) throws InvalidColaboratorException {
        final MindMap mindMap = getMindmapFromRequest(request);
        User user = Utils.getUser();
        if (!mindMap.getOwner().equals(user)) {
            throw new IllegalStateException("No enought right to execute this operation");
        }


        final String[] emails = getEmailsToAdd(request);

        final ColaborationEmail email = new ColaborationEmail();
        email.setSubject(request.getParameter("subject"));
        email.setMessage(request.getParameter("message"));
        getMindmapService().addColaborators(mindMap, emails, role, email);

        return new MindMapBean(mindMap);
    }
}