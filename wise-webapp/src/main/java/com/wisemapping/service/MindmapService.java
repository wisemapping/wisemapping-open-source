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

package com.wisemapping.service;

import com.wisemapping.model.*;
import com.wisemapping.exceptions.WiseMappingException;
import org.jetbrains.annotations.NotNull;

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

    public void addCollaborators(MindMap mindmap, String[] colaboratorEmails, UserRole role, ColaborationEmail email)
            throws InvalidColaboratorException;

    public void addTags(MindMap mindmap, String tags);

    public void removeCollaboratorFromMindmap(@NotNull final MindMap mindmap, long colaboratorId);

    public void removeMindmap(@NotNull final MindMap mindmap, @NotNull final User user) throws WiseMappingException;

    public List<MindMap> search(MindMapCriteria criteria);

    public List<MindMap> getPublicMaps(int cant);

    public List<MindMapHistory> getMindMapHistory(int mindmapId);

    public boolean isAllowedToView(User user, MindMap map, UserRole allowedRole);

    public boolean isAllowedToView(User user, int mapId, UserRole allowedRole);

    public boolean isAllowedToColaborate(User user, int mapId, UserRole grantedRole);

    public boolean isAllowedToCollaborate(User user, MindMap map, UserRole grantedRole);

    public void addWelcomeMindmap(User user) throws WiseMappingException;

    public void addView(int mapId);

    public void revertMapToHistory(MindMap map, int historyId) throws IOException, WiseMappingException;
}
