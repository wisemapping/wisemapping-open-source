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

import java.util.List;

public interface MindmapManager {

    Collaborator getCollaboratorBy(String email);

    Collaborator getCollaboratorBy(long id);
    
    List<Collaboration> getMindmapUserByCollaborator(final long collaboratorId);

    List<Collaboration> getMindmapUserByRole(final CollaborationRole userRole);

    Collaboration getMindmapUserBy(final int mindmapId, final User user);

    List<MindMap> getAllMindmaps();

    MindMap getMindmapById(int mindmapId);

    MindMap getMindmapByTitle(final String name, final User user);

    void addCollaborator(Collaborator collaborator);

    void addMindmap(User user, MindMap mindMap);

    void saveMindmap(MindMap mindMap);

    void updateMindmap(MindMap mindMap, boolean saveHistory);

    void removeCollaborator(Collaborator collaborator);

    void removeMindmap(MindMap mindap);

    void removeMindmapUser(Collaboration collaboration);

    public List<MindMap> search(MindMapCriteria criteria);

    public List<MindMap> search(MindMapCriteria criteria, int maxResult);

    public List<MindMapHistory> getHistoryFrom(int mindmapId);

    public MindMapHistory getHistory(int historyId);
}
