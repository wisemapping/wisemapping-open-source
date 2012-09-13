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

package com.wisemapping.dao;

import com.wisemapping.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MindmapManager {

    Collaborator findCollaborator(@NotNull String email);

    Collaborator findCollaborator(long id);
    
    List<Collaboration> findCollaboration(final long collaboratorId);

    List<Collaboration> findCollaboration(final CollaborationRole userRole);

    Collaboration findCollaboration(final int mindmapId, final User user);

    List<Mindmap> getAllMindmaps();

    @Nullable
    Mindmap getMindmapById(int mindmapId);

    Mindmap getMindmapByTitle(final String name, final User user);

    void addCollaborator(Collaborator collaborator);

    void addMindmap(User user, Mindmap mindMap);

    void saveMindmap(Mindmap mindMap);

    void updateMindmap(@NotNull Mindmap mindMap, boolean saveHistory);

    void removeCollaborator(@NotNull Collaborator collaborator);

    void removeMindmap(Mindmap mindap);

    void removeCollaboration(Collaboration collaboration);

    public List<Mindmap> search(MindMapCriteria criteria);

    public List<Mindmap> search(MindMapCriteria criteria, int maxResult);

    public List<MindMapHistory> getHistoryFrom(int mindmapId);

    public MindMapHistory getHistory(int historyId);

    void updateCollaboration(@NotNull Collaboration collaboration);
}
