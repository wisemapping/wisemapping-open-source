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

public interface MindmapService {

    static final String TAG_SEPARATOR = " ";

    @NotNull
    Mindmap findMindmapById(int id);

    Mindmap getMindmapByTitle(String title, User user);

    List<Collaboration> findCollaborations(@NotNull User user);

    void updateMindmap(Mindmap mindMap, boolean saveHistory) throws WiseMappingException;

    void addMindmap(Mindmap map, User user) throws WiseMappingException;

    void addCollaboration(@NotNull Mindmap mindmap, @NotNull String email, @NotNull CollaborationRole role, @Nullable String message)
            throws CollaborationException;

    void removeCollaboration(@NotNull Mindmap mindmap, @NotNull Collaboration collaboration) throws CollaborationException;

    void addTags(@NotNull Mindmap mindmap, String tags);

    void removeMindmap(@NotNull final Mindmap mindmap, @NotNull final User user) throws WiseMappingException;

    List<Mindmap> search(MindMapCriteria criteria);

    List<MindMapHistory> findMindmapHistory(int mindmapId);

    boolean hasPermissions(@Nullable User user, Mindmap map, CollaborationRole allowedRole);

    boolean hasPermissions(@Nullable User user, int mapId, CollaborationRole allowedRole);

    void revertChange(@NotNull Mindmap map, int historyId) throws WiseMappingException;

    MindMapHistory findMindmapHistory(int id, int hid) throws WiseMappingException;

    void updateCollaboration(@NotNull Collaborator collaborator, @NotNull Collaboration collaboration) throws WiseMappingException;

    LockManager getLockManager();
}
