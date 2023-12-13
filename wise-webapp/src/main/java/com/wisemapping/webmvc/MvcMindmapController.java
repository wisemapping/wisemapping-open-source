/*
 *    Copyright [2022] [wisemapping]
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

package com.wisemapping.webmvc;


import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.MapCouldNotFoundException;
import com.wisemapping.exceptions.MapNotPublicSecurityException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LockManager;
import com.wisemapping.service.MindmapService;
import com.wisemapping.view.MindMapBean;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

@Controller
@Transactional(propagation = Propagation.REQUIRED)
public class MvcMindmapController {

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(value = "maps/{id}/print")
    public String showPrintPage(@PathVariable int id, @NotNull Model model) throws WiseMappingException {

        final MindMapBean mindmap = findMindmapBean(id);
        model.addAttribute("principal", Utils.getUser());
        model.addAttribute("mindmap", mindmap);
        model.addAttribute("creatorFullName", mindmap.getCreator().getFullName());
        final Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("locale", locale.toString().toLowerCase());
        model.addAttribute("properties", mindmap.getProperties());
        return "mindmapViewonly";
    }

    @RequestMapping(value = "maps/")
    public String showListPage(@NotNull Model model) {
        return "reactInclude";
    }

    @RequestMapping(value = "maps/{id}/edit", method = RequestMethod.GET)
    public String showMindmapEditorPage(@PathVariable int id, @NotNull Model model) throws WiseMappingException {
        return showEditorPage(id, model, true);
    }

    private String showEditorPage(int id, @NotNull final Model model, boolean requiresLock) throws WiseMappingException {
        final MindMapBean mindmapBean = findMindmapBean(id);
        final Mindmap mindmap = mindmapBean.getDelegated();
        final User user = Utils.getUser();
        final Locale locale = LocaleContextHolder.getLocale();

        // Is the mindmap locked ?.
        boolean isLocked = false;
        boolean readOnlyMode = !requiresLock || !mindmap.hasPermissions(user, CollaborationRole.EDITOR);
        if (!readOnlyMode) {
            final LockManager lockManager = this.mindmapService.getLockManager();
            if (lockManager.isLocked(mindmap) && !lockManager.isLockedBy(mindmap, user)) {
                isLocked = true;
            }
            model.addAttribute("lockInfo", lockManager.getLockInfo(mindmap));
        }
        // Set render attributes ...
        model.addAttribute("mindmap", mindmapBean);

        // Configure default locale for the editor ...
        model.addAttribute("locale", locale.toString().toLowerCase());
        model.addAttribute("principal", user);
        model.addAttribute("mindmapLocked", isLocked);

        return "mindmapEditor";
    }

    @RequestMapping(value = "maps/{id}/view", method = RequestMethod.GET)
    public String showMindmapViewerPage(@PathVariable int id, @NotNull Model model) throws WiseMappingException {
        final String result = showPrintPage(id, model);
        return result;
    }

    @RequestMapping(value = "maps/{id}/try", method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    public String showMindmapTryPage(@PathVariable int id, @NotNull Model model) throws WiseMappingException {
        return  showEditorPage(id, model, false);
    }

    @RequestMapping(value = "maps/{id}/{hid}/view", method = RequestMethod.GET)
    public String showMindmapViewerRevPage(@PathVariable int id, @PathVariable int hid, @NotNull Model model) throws WiseMappingException {
        final String result = showPrintPage(id, model);
        model.addAttribute("hid", String.valueOf(hid));
        return result;
    }

    @RequestMapping(value = "maps/{id}/embed")
    @PreAuthorize("permitAll()")
    public ModelAndView showEmbeddedPage(@PathVariable int id, @RequestParam(required = false) Float zoom) throws MapCouldNotFoundException, MapNotPublicSecurityException, AccessDeniedSecurityException {
        if (!mindmapService.isMindmapPublic(id)) {
            throw new MapNotPublicSecurityException("Map " + id + " is not public.");
        }

        final MindMapBean mindmap = findMindmapBean(id);
        final ModelAndView view = new ModelAndView("mindmapViewonly", "mindmap", mindmap);
        view.addObject("zoom", zoom == null ? 1 : zoom);
        view.addObject("properties", "{}");
        final Locale locale = LocaleContextHolder.getLocale();
        view.addObject("locale", locale.toString().toLowerCase());
        return view;
    }

    @RequestMapping(value = "maps/{id}/public", method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    public String showPublicViewPage(@PathVariable int id, @NotNull Model model) throws WiseMappingException {
        if (!mindmapService.isMindmapPublic(id)) {
            throw new MapNotPublicSecurityException("Map " + id + " is not public.");
        }
        return this.showPrintPage(id, model);
    }

    @Deprecated
    @RequestMapping(value = "publicView", method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    public String showPublicViewPageLegacy(@RequestParam(required = true) int mapId) {
        return "redirect:maps/" + mapId + "/public";
    }

    @Deprecated
    @RequestMapping(value = "embeddedView", method = RequestMethod.GET)
    @PreAuthorize("permitAll()")
    public String showPublicViewLegacyPage(@RequestParam(required = true) int mapId, @RequestParam(required = false) int zoom) {
        return "redirect:maps/" + mapId + "/embed?zoom=" + zoom;
    }

    @NotNull
    private Mindmap findMindmap(int mapId) throws MapCouldNotFoundException {
        final Mindmap result = mindmapService.findMindmapById(mapId);
        if (result == null) {
            throw new MapCouldNotFoundException("Map could not be found " + mapId);
        }
        return result;

    }

    @NotNull
    private MindMapBean findMindmapBean(int mapId) throws MapCouldNotFoundException, AccessDeniedSecurityException {
        final User user = Utils.getUser();
        if (!mindmapService.hasPermissions(user, mapId, CollaborationRole.VIEWER)) {
            throw new AccessDeniedSecurityException(mapId, user);
        }

        final Mindmap mindmap = findMindmap(mapId);
        return new MindMapBean(mindmap, Utils.getUser());
    }
}
