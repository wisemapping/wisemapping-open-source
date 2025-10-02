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

    /**
     * Get all mindmaps with pagination support (admin only)
     * @param page page number (0-based)
     * @param pageSize number of mindmaps per page
     * @return list of mindmaps for the given page
     */
    List<Mindmap> getAllMindmaps(int page, int pageSize);

    /**
     * Get all mindmaps with pagination and spam filtering support (admin only)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param page page number (0-based)
     * @param pageSize number of mindmaps per page
     * @return list of mindmaps for the given page
     */
    List<Mindmap> getAllMindmaps(Boolean filterSpam, int page, int pageSize);

    /**
     * Get all mindmaps with pagination, spam filtering, and date filtering support (admin only)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param dateFilter filter by creation date ("1" = last 1 month, "3" = last 3 months, "6" = last 6 months, "all" = no filter)
     * @param page page number (0-based)
     * @param pageSize number of mindmaps per page
     * @return list of mindmaps for the given page
     */
    List<Mindmap> getAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter, int page, int pageSize);

    /**
     * Count total number of mindmaps (admin only)
     * @return total count of mindmaps
     */
    long countAllMindmaps();

    /**
     * Count total number of mindmaps with spam filtering (admin only)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @return total count of mindmaps
     */
    long countAllMindmaps(Boolean filterSpam);

    /**
     * Count total number of mindmaps with spam filtering and date filtering (admin only)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param dateFilter filter by creation date ("1" = last 1 month, "3" = last 3 months, "6" = last 6 months, "all" = no filter)
     * @return total count of mindmaps
     */
    long countAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter);

    /**
     * Search mindmaps with filtering and pagination (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @param page page number (0-based)
     * @param pageSize number of mindmaps per page
     * @return list of filtered mindmaps for the given page
     */
    List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, int page, int pageSize);

    /**
     * Search mindmaps with filtering and pagination including spam filter (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param page page number (0-based)
     * @param pageSize number of mindmaps per page
     * @return list of filtered mindmaps for the given page
     */
    List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, int page, int pageSize);

    /**
     * Count mindmaps matching search criteria (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @return total count of matching mindmaps
     */
    long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked);

    /**
     * Count mindmaps matching search criteria including spam filter (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @return total count of matching mindmaps
     */
    long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam);
}
