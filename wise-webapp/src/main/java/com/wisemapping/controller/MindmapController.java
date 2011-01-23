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

import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.view.MindMapBean;
import com.wisemapping.filter.UserAgent;
import com.wisemapping.exceptions.WiseMappingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

public class MindmapController extends BaseMultiActionController {
    protected ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return list(httpServletRequest, httpServletResponse);
    }

    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Mindmap Controller: myMindmap action");
        final HttpSession session = request.getSession(false);

        // Try to loaded from the request ...
        UserAgent userAgent = null;
        if (session != null) {
            userAgent = (UserAgent) session.getAttribute(USER_AGENT);
        }

        // I could not loaded. I will create a new one...
        if (userAgent == null) {
            userAgent = UserAgent.create(request);
            if (session != null) {
                session.setAttribute(USER_AGENT, userAgent);
            }
        }

        // It's a supported browser ?.
        final UserAgent.OS os = userAgent.getOs();

        final User user = Utils.getUser(request);
        final ModelAndView view = new ModelAndView("mindmapList", "wisemapsList", getMindMapBeanList(user));
        view.addObject("isMAC", os == UserAgent.OS.MAC);
        view.addObject("user", user);
        return view;
    }

    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Mindmap Controller: EDIT action");
        final MindMap mindmap = getMindmapFromRequest(request);
        return new ModelAndView("mindmapEditor", "wisemapsList", new MindMapBean(mindmap));
    }

    public ModelAndView collaborator(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Mindmap Controller: COLLABORATE action");
        final MindMap mindmap = getMindmapFromRequest(request);
        return new ModelAndView("mindmapCollaborator", "mindmap", new MindMapBean(mindmap));
    }

    public ModelAndView viewer(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Mindmap Controller: VIEWER action");
        final MindMap mindmap = getMindmapFromRequest(request);
        return new ModelAndView("mindmapViewer", "wisemapsList", new MindMapBean(mindmap));
    }

    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws WiseMappingException {
        logger.info("Mindmap Controller: DELETE action");
        final User user = Utils.getUser(request);

        final MindMap mindmap = getMindmapFromRequest(request);
        getMindmapService().removeColaboratorFromMindmap(mindmap, user.getId());

        return list(request, response);
    }

    public ModelAndView deleteAll(HttpServletRequest request, HttpServletResponse response) throws WiseMappingException {
        logger.info("Mindmap Controller: DELETE ALL action");

        final List<MindmapUser> mindmaps = getMindmapUsersFromRequest(request);
        final User user = Utils.getUser(request);
        for (MindmapUser mindmap : mindmaps)
            getMindmapService().removeMindmap(mindmap.getMindMap(), user);
        return list(request, response);
    }

    public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Mindmap Controller: DETAIL action");
        final MindMap mindMap = getMindmapFromRequest(request);
        final ModelAndView view = new ModelAndView("mindmapDetail", "wisemapDetail", new MindMapBean(mindMap));
        view.addObject("user", Utils.getUser());
        return view;
    }

    public ModelAndView changeStatus(HttpServletRequest request, HttpServletResponse response) throws WiseMappingException {
        final MindMap mindmap = getMindmapFromRequest(request);
        boolean isPublic = !mindmap.isPublic();
        mindmap.setPublic(isPublic);
        getMindmapService().updateMindmap(mindmap, false);
        return new ModelAndView("mindmapDetail", "wisemapDetail", new MindMapBean(mindmap));
    }

    public ModelAndView editMindmap(HttpServletRequest request, HttpServletResponse response) throws WiseMappingException {
        final MindMap mindmap = getMindmapFromRequest(request);
        final ModelAndView view = new ModelAndView("editMindmap", "mindmap", new MindMapBean(mindmap));
        view.addObject("user", Utils.getUser());
        return view;
    }

    public ModelAndView updateMindmap(HttpServletRequest request, HttpServletResponse response) throws WiseMappingException {
        final MindMap mindmap = getMindmapFromRequest(request);

        final String title = request.getParameter("title");
        final String description = request.getParameter("description");

        mindmap.setTitle(title);
        mindmap.setDescription(description);

        getMindmapService().updateMindmap(mindmap, false);
        return list(request, response);
    }

    private static final String USER_AGENT = "wisemapping.userAgent";
}
