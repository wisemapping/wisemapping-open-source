/*
*    Copyright [2007-2025] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.*;
import org.jetbrains.annotations.Nullable;

import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

public interface MindmapService {

    @Nullable
    Mindmap findMindmapById(int id);

    @NotNull
    List<Mindmap> findMindmapsByUser(@NotNull Account user);

    Mindmap getMindmapByTitle(@NotNull  String title, Account user);

    List<Collaboration> findCollaborations(@NotNull Account user);

    void updateMindmap(Mindmap mindMap, boolean saveHistory) throws WiseMappingException;

    void addMindmap(Mindmap map, Account user) throws WiseMappingException;

    void addCollaboration(@NotNull Mindmap mindmap, @NotNull String email, @NotNull CollaborationRole role, @Nullable String message)
            throws CollaborationException;

    void removeCollaboration(@NotNull Mindmap mindmap, @NotNull Collaboration collaboration) throws CollaborationException;

    void removeMindmap(@NotNull final Mindmap mindmap, @NotNull final Account user) throws WiseMappingException;

    List<MindMapHistory> findMindmapHistory(int mindmapId);

    boolean hasPermissions(@Nullable Account user, Mindmap map, CollaborationRole allowedRole);

    boolean hasPermissions(@Nullable Account user, int mapId, CollaborationRole allowedRole);

    boolean isMindmapPublic(int mapId);

    void revertChange(@NotNull Mindmap map, int historyId) throws WiseMappingException, IOException;

    MindMapHistory findMindmapHistory(int id, int hid) throws WiseMappingException;

    void updateCollaboration(@NotNull Collaborator collaborator, @NotNull Collaboration collaboration) throws WiseMappingException;

    LockManager getLockManager();

    boolean isAdmin(@Nullable Account user);

    List<Mindmap> getAllMindmaps();
}
