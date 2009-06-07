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

package com.wisemapping.service;

import com.wisemapping.model.*;
import com.wisemapping.exceptions.WiseMappingException;

import java.util.List;
import java.io.IOException;

public interface MindmapService {

    public static final String TAG_SEPARATOR = " ";

    public MindmapUser getMindmapUserBy(int mindmapId, User user);

    public MindMap getMindmapById(int mindmapId);

    public MindMap getMindmapByTitle(String title, User user);

    public List<MindmapUser> getMindmapUserByUser(User user);

    public void updateMindmap(MindMap mindMap, boolean saveHistory) throws WiseMappingException;

    public void addMindmap(MindMap map, User user) throws WiseMappingException;

    public void addColaborators(MindMap mindmap, String[] colaboratorEmails, UserRole role, ColaborationEmail email)
            throws InvalidColaboratorException;

    public void addTags(MindMap mindmap, String tags);

    public void removeColaboratorFromMindmap(MindMap mindmap, long colaboratorId);

    public void removeMindmap(MindMap mindmap, User user) throws WiseMappingException;

    public List<MindMap> search(MindMapCriteria criteria);

    public List<MindMap> getPublicMaps(int cant);

    public List<MindMapHistory> getMindMapHistory(int mindmapId);

    public boolean isAllowedToView(User user, MindMap map, UserRole allowedRole);

    public boolean isAllowedToView(User user, int mapId, UserRole allowedRole);

    public boolean isAllowedToColaborate(User user, int mapId, UserRole grantedRole);

    public boolean isAllowedToColaborate(User user, MindMap map, UserRole grantedRole);

    public void addWelcomeMindmap(User user) throws WiseMappingException;

    public void addView(int mapId);

    public void revertMapToHistory(MindMap map, int historyId) throws IOException, WiseMappingException;
}
