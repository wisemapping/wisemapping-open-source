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

package com.wisemapping.dao;

import com.wisemapping.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MindmapManager {

    Collaborator findCollaborator(@NotNull String email);

    List<Collaboration> findCollaboration(final int collaboratorId);

    @Nullable
    Mindmap getMindmapById(int mindmapId);

    Mindmap getMindmapByTitle(final String name, final User user);

    void addCollaborator(Collaborator collaborator);

    void addMindmap(User user, Mindmap mindmap);

    void saveMindmap(Mindmap mindmap);

    void updateMindmap(@NotNull Mindmap mindmap, boolean saveHistory);

    void removeCollaborator(@NotNull Collaborator collaborator);

    void removeMindmap(Mindmap mindmap);

    void removeCollaboration(Collaboration collaboration);

    List<MindMapHistory> getHistoryFrom(int mindmapId);

    MindMapHistory getHistory(int historyId);

    void updateCollaboration(@NotNull Collaboration collaboration);

    List<Mindmap> findMindmapByUser(User user);
}
