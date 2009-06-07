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
import com.wisemapping.model.MindMapCriteria;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import com.wisemapping.view.MindMapBean;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMultiActionController
        extends MultiActionController {

    private MindmapService mindmapService;
    private UserService userService;

    protected List<MindmapUser> getMindmapUsersFromRequest(HttpServletRequest request) {
        List<MindmapUser> result = new ArrayList<MindmapUser>();
        final String mindmapIds = request.getParameter("mindmapIds");

        final String ids[] = mindmapIds.split(",");
        for (String id : ids)
            if (mindmapIds.length()!=0){
                result.add(getMindmapUser(Integer.parseInt(id), request));
            }
        return result;
    }

    protected List<MindMap> getMindmapsFromRequest(HttpServletRequest request) {
        List<MindMap> result = new ArrayList<MindMap>();
        final String mindmapIds = request.getParameter("mindmapIds");

        final String ids[] = mindmapIds.split(",");
        for (String id : ids) {
            MindMap map = mindmapService.getMindmapById(Integer.parseInt(id));
            result.add(map);
        }
        return result;
    }

    protected List<MindMapBean> getMindMapBeanList(User user) {
        final List<MindmapUser> userMindmaps = getMindmapService().getMindmapUserByUser(user);

        final List<MindMapBean> mindMapBeans = new ArrayList<MindMapBean>(userMindmaps.size());
        for (MindmapUser mindmap : userMindmaps) {
            mindMapBeans.add(new MindMapBean(mindmap.getMindMap()));
        }
        return mindMapBeans;
    }

    protected MindMapCriteria getMindMapCriteriaFromRequest(HttpServletRequest request) {
        final MindMapCriteria criteria = new MindMapCriteria();

        final String titleOrTags = request.getParameter("titleOrTags");
        if (titleOrTags != null && titleOrTags.length() != 0) {
            criteria.orCriteria();
            criteria.setTitle(titleOrTags);
            final String tag[] = titleOrTags.split(MindmapService.TAG_SEPARATOR);
            // Add new Tags to User
            for (String searchTag : tag) {
                criteria.getTags().add(searchTag);
            }
        }

        final String title = request.getParameter("name");
        if (title != null && title.length() != 0) {
            criteria.setTitle(title);
        }
        final String description = request.getParameter("description");
        if (description != null && description.length() != 0) {
            criteria.setDescription(description);
        }

        final String tags = request.getParameter("tags");
        if (tags != null && tags.length() != 0) {
            final String tag[] = tags.split(MindmapService.TAG_SEPARATOR);
            // Add new Tags to User
            for (String searchTag : tag) {
                criteria.getTags().add(searchTag);
            }
        }

        return criteria;
    }

    protected MindMap getMindmapFromRequest(HttpServletRequest request) {
        final String mapIdStr = request.getParameter(MAP_ID_PARAMNAME);
        assert mapIdStr != null : "mapId parameter can not be null";
        logger.info("MapIdStr:" + mapIdStr);
        MindMap map = null;
        int mapId;
        try
        {
            mapId = Integer.parseInt(mapIdStr);
            map = mindmapService.getMindmapById(mapId);
        }
        catch (Exception e)
        {
            logger.debug("An error ocurred trying to get mapId "+ mapIdStr + "'",e);            
        }

        if (map == null) {
            throw new IllegalStateException("Map with id '" + mapIdStr + "' can not be found");
        }
        return map;
    }

    private MindmapUser getMindmapUser(Integer mapId, HttpServletRequest request) {
        assert mapId != null : "EDIT action: mindmapId is required!";
        assert request != null : "EDIT action: request is required!";
        final User user = Utils.getUser(request);
        return mindmapService.getMindmapUserBy(mapId, user);
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
    public static final String MINDMAP_EMAIL_PARAMNAME = "userEmail";
}
