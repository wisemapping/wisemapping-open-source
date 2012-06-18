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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.io.IOException;

public interface MindmapService {

    public static final String TAG_SEPARATOR = " ";

    public MindMap findMindmapById(int mindmapId);

    public MindMap getMindmapByTitle(String title, User user);

    public List<Collaboration> findCollaborations(@NotNull User user);

    public void updateMindmap(MindMap mindMap, boolean saveHistory) throws WiseMappingException;

    public void addMindmap(MindMap map, User user) throws WiseMappingException;

    public void addCollaboration(@NotNull MindMap mindmap, @NotNull String email, @NotNull CollaborationRole role, @Nullable String message)
            throws CollaborationException;

    public void removeCollaboration(@NotNull MindMap mindmap, @NotNull Collaboration collaboration) throws CollaborationException;

    public void addTags(@NotNull MindMap mindmap, String tags);

    public void removeMindmap(@NotNull final MindMap mindmap, @NotNull final User user) throws WiseMappingException;

    public List<MindMap> search(MindMapCriteria criteria);

    public List<MindMapHistory> findMindmapHistory(int mindmapId);

    public boolean hasPermissions(@Nullable User user, MindMap map, CollaborationRole allowedRole);

    public boolean hasPermissions(@Nullable User user, int mapId, CollaborationRole allowedRole);

    public void addWelcomeMindmap(User user) throws WiseMappingException;

    public void revertChange(@NotNull MindMap map, int historyId) throws WiseMappingException;

    MindMapHistory findMindmapHistory(int id, int hid) throws WiseMappingException;
}
