
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

package com.wisemapping.ncontroller;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.filter.UserAgent;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.view.MindMapBean;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MindmapController {

    private String baseUrl;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(value = "maps/{id}/export")
    public ModelAndView showExportPage(@PathVariable int id) throws IOException {
        final MindMapBean modelObject = findMindmapBean(id);
        return new ModelAndView("mindmapExport", "mindmap", modelObject);
    }

    @RequestMapping(value = "maps/import")
    public ModelAndView showImportPAge() throws IOException {
        return new ModelAndView("mindmapImport");
    }

    @RequestMapping(value = "maps/{id}/exportf")
    public ModelAndView showExportPageFull(@PathVariable int id) throws IOException {
        final MindMapBean modelObject = findMindmapBean(id);
        return new ModelAndView("mindmapExportFull", "mindmap", modelObject);
    }

    @RequestMapping(value = "maps/{id}/details")
    public ModelAndView showDetails(@PathVariable int id) {
        final MindMapBean modelObject = findMindmapBean(id);
        final ModelAndView view = new ModelAndView("mindmapDetail", "wisemapDetail", modelObject);
        view.addObject("user", Utils.getUser());
        return view;
    }

    @RequestMapping(value = "maps/{id}/print")
    public ModelAndView showPrintPage(@PathVariable int id) {
        final MindMap mindmap = findMindmap(id);
        return new ModelAndView("mindmapPrint", "mindmap", mindmap);
    }

    @RequestMapping(value = "maps/{id}/share")
    public ModelAndView showSharePage(@PathVariable int id) {
        final MindMap mindmap = findMindmap(id);
        return new ModelAndView("mindmapShare", "mindmap", mindmap);
    }

    @RequestMapping(value = "maps/{id}/sharef")
    public ModelAndView showSharePageFull(@PathVariable int id) {
        final MindMap mindmap = findMindmap(id);
        return new ModelAndView("mindmapShareFull", "mindmap", mindmap);
    }

    @RequestMapping(value = "maps/{id}/publish")
    public ModelAndView showPublishPage(@PathVariable int id) {
        final MindMap mindmap = findMindmap(id);
        return new ModelAndView("mindmapPublish", "mindmap", mindmap);
    }

    @RequestMapping(value = "maps/{id}/publishf")
    public ModelAndView showPublishPageFull(@PathVariable int id) {
        final MindMap mindmap = findMindmap(id);
        return new ModelAndView("mindmapPublishFull", "mindmap", mindmap);
    }

    @RequestMapping(value = "maps/{id}/edit")
    public ModelAndView editMap(@PathVariable int id, @NotNull HttpServletRequest request) {
        ModelAndView view;
        final UserAgent userAgent = UserAgent.create(request);
        if (userAgent.needsGCF()) {
            view = new ModelAndView("gcfPluginNeeded");
//            view.addObject(MINDMAP_ID_PARAMETER, mindmapId);
        } else {

            final MindMap mindmap = mindmapService.getMindmapById(id);
            view = new ModelAndView("mindmapEditor", "mindmap", mindmap);
            view.addObject("editorTryMode", false);
            final boolean showHelp = isWelcomeMap(mindmap);
            view.addObject("showHelp", showHelp);
            view.addObject("user", Utils.getUser());
        }
        return view;
    }

    @RequestMapping(value = "maps/{id}/embed")
    public ModelAndView embeddedView(@PathVariable int id, @RequestParam(required = false) Float zoom, @NotNull HttpServletRequest request) {
        ModelAndView view;
        final UserAgent userAgent = UserAgent.create(request);
        final MindMap mindmap = mindmapService.getMindmapById(id);
        view = new ModelAndView("mindmapEmbedded", "mindmap", mindmap);
        view.addObject("user", Utils.getUser());
        view.addObject("zoom", zoom == null ? 1 : zoom);
        return view;
    }

    @RequestMapping(value = "collaborator")
    public ModelAndView showCollaborator(@RequestParam(required = true) long mapId) {
        final MindMapBean modelObject = findMindmapBean(mapId);
        return new ModelAndView("mindmapCollaborator", "mindmap", modelObject);
    }

    @RequestMapping(value = "viewer")
    public ModelAndView viewer(@RequestParam(required = true) long mapId) {
        final MindMapBean modelObject = findMindmapBean(mapId);
        return new ModelAndView("mindmapViewer", "wisemapsList", modelObject);
    }

    @RequestMapping(value = "changeStatus")
    public ModelAndView changeStatus(@RequestParam(required = true) long mapId) throws WiseMappingException {
        final MindMap mindmap = findMindmap(mapId);
        boolean isPublic = !mindmap.isPublic();
        mindmap.setPublic(isPublic);
        mindmapService.updateMindmap(mindmap, false);
        return new ModelAndView("mindmapDetail", "wisemapDetail", new MindMapBean(mindmap));
    }

    @RequestMapping(value = "maps/")
    public ModelAndView list(@NotNull HttpServletRequest request) {
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

        final User user = Utils.getUser();
        final ModelAndView view = new ModelAndView("mindmapList", "wisemapsList", findMindMapBeanList(user));
        view.addObject("isMAC", os == UserAgent.OS.MAC);
        view.addObject("user", user);
        return view;
    }

    @RequestMapping(value = "updateMindmap")
    public ModelAndView updateMindmap(@RequestParam(required = true) long mapId, @RequestParam(required = true) String title, @RequestParam(required = true) String description, @NotNull HttpServletRequest request) throws WiseMappingException {
        final MindMap mindmap = findMindmap(mapId);
        mindmap.setTitle(title);
        mindmap.setDescription(description);

        mindmapService.updateMindmap(mindmap, false);
        return list(request);
    }

    private MindMap findMindmap(long mapId) {
        final MindMap mindmap = mindmapService.getMindmapById((int) mapId);
        if (mindmap == null) {
            throw new IllegalArgumentException("Mindmap could not be found");
        }
        return mindmap;
    }

    private List<MindMapBean> findMindMapBeanList(@NotNull User user) {
        final List<Collaboration> userMindmaps = mindmapService.getCollaborationsBy(user);

        final List<MindMapBean> mindMapBeans = new ArrayList<MindMapBean>(userMindmaps.size());
        for (Collaboration mindmap : userMindmaps) {
            mindMapBeans.add(new MindMapBean(mindmap.getMindMap()));
        }
        return mindMapBeans;
    }

    private MindMapBean findMindmapBean(long mapId) {
        return new MindMapBean(findMindmap(mapId));
    }

    private boolean isWelcomeMap(MindMap map) {
        return map.getTitle().startsWith("Welcome ");
    }


    private static final String USER_AGENT = "wisemapping.userAgent";
}
