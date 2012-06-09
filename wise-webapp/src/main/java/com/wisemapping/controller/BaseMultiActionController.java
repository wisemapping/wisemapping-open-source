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

package com.wisemapping.controller;

import com.wisemapping.model.MindMap;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseMultiActionController
        extends MultiActionController {

    private MindmapService mindmapService;
    private UserService userService;

    protected MindMap getMindmapFromRequest(HttpServletRequest request) {
        final String mapIdStr = request.getParameter(MAP_ID_PARAMNAME);
        assert mapIdStr != null : "mapId parameter can not be null";
        logger.info("MapIdStr:" + mapIdStr);
        MindMap map = null;
        int mapId;
        try {
            mapId = Integer.parseInt(mapIdStr);
            map = mindmapService.getMindmapById(mapId);
        } catch (Exception e) {
            logger.debug("An error ocurred trying to get mapId " + mapIdStr + "'", e);
        }

        if (map == null) {
            throw new IllegalStateException("Map with id '" + mapIdStr + "' can not be found");
        }
        return map;
    }

    public MindmapService getMindmapService() {
        return mindmapService;
    }

    public void setMindmapService(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public static final String MAP_ID_PARAMNAME = "mapId";
    public static final String MINDMAP_EMAILS_PARAMNAME = "userEmails";
}
