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

package com.wisemapping.dao;

import com.wisemapping.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;

public interface MindmapManager {

    Collaborator findCollaborator(@NotNull String email);

    List<Collaboration> findCollaboration(final int collaboratorId);

    @Nullable
    Mindmap getMindmapById(int mindmapId);

    Mindmap getMindmapByTitle(final String name, final Account user);

    void addCollaborator(Collaborator collaborator);

    void addMindmap(Account user, Mindmap mindmap);

    void saveMindmap(Mindmap mindmap);

    void updateMindmap(@NotNull Mindmap mindmap, boolean saveHistory);

    void updateMindmapSpamInfo(@NotNull com.wisemapping.model.MindmapSpamInfo spamInfo);

    void removeCollaborator(@NotNull Collaborator collaborator);

    void removeMindmap(Mindmap mindmap);

    void removeCollaboration(Collaboration collaboration);

    List<MindMapHistory> getHistoryFrom(int mindmapId);

    MindMapHistory getHistory(int historyId);

    void updateCollaboration(@NotNull Collaboration collaboration);

    /**
     * Find an existing collaboration between a specific mindmap and collaborator
     * @param mindmapId the mindmap ID
     * @param collaboratorId the collaborator ID
     * @return the Collaboration entity if found, null otherwise
     */
    @Nullable
    Collaboration findCollaboration(int mindmapId, int collaboratorId);

    /**
     * Find or create a collaboration between a specific mindmap and collaborator
     * This method prevents constraint violations by checking existence before creating
     * @param mindmap the mindmap
     * @param collaborator the collaborator
     * @param role the collaboration role
     * @return the existing or newly created Collaboration entity
     */
    @NotNull
    Collaboration findOrCreateCollaboration(@NotNull Mindmap mindmap, @NotNull Collaborator collaborator, @NotNull CollaborationRole role);

    List<Mindmap> findMindmapByUser(Account user);

    /**
     * Find users who have multiple spam-detected mindmaps
     * @param spamThreshold minimum number of spam mindmaps to consider for suspension
     * @return list of users with spam mindmaps count
     */
    List<SpamUserResult> findUsersWithSpamMindmaps(int spamThreshold);

    /**
     * Find users who have multiple spam-detected mindmaps and were created in the last N months
     * @param spamThreshold minimum number of spam mindmaps to consider for suspension
     * @param monthsBack number of months to look back for account creation
     * @return list of users with spam mindmaps count
     */
    List<SpamUserResult> findUsersWithSpamMindaps(int spamThreshold, int monthsBack);

    /**
     * Find users who have multiple spam-detected mindmaps and were created in the last N months with pagination
     * @param spamThreshold minimum number of spam mindmaps to consider for suspension
     * @param monthsBack number of months to look back for account creation
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of users with spam mindmaps count for the given page
     */
    List<SpamUserResult> findUsersWithSpamMindaps(int spamThreshold, int monthsBack, int offset, int limit);
    
    /**
     * Find users with spam mindmaps using cursor-based pagination (more efficient for large datasets)
     * @param spamThreshold minimum number of spam mindmaps required
     * @param monthsBack only process accounts created in the last N months
     * @param lastUserId ID of the last processed user (null for first page)
     * @param limit maximum number of results to return
     * @return list of users with spam mindmaps count
     */
    List<SpamUserResult> findUsersWithSpamMindapsCursor(int spamThreshold, int monthsBack, Integer lastUserId, int limit);

    /**
     * Count users who have multiple spam-detected mindmaps and were created in the last N months
     * @param spamThreshold minimum number of spam mindmaps to consider for suspension
     * @param monthsBack number of months to look back for account creation
     * @return total count of users with spam mindmaps
     */
    long countUsersWithSpamMindaps(int spamThreshold, int monthsBack);

    /**
     * Find users with high spam ratios (spam maps / total maps) for accounts created in the last N months
     * @param minSpamCount minimum number of spam mindmaps required
     * @param spamRatioThreshold minimum spam ratio (0.0 to 1.0) to consider for suspension
     * @param monthsBack number of months to look back for account creation
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of users with their spam ratios
     */
    List<SpamRatioUserResult> findUsersWithHighSpamRatio(int minSpamCount, double spamRatioThreshold, int monthsBack, int offset, int limit);

    /**
     * Count users with high spam ratios for accounts created in the last N months
     * @param minSpamCount minimum number of spam mindmaps required
     * @param spamRatioThreshold minimum spam ratio (0.0 to 1.0) to consider for suspension
     * @param monthsBack number of months to look back for account creation
     * @return count of users meeting the criteria
     */
    long countUsersWithHighSpamRatio(int minSpamCount, double spamRatioThreshold, int monthsBack);

    /**
     * Find users with minimum total public maps and minimum spam count
     * @param minTotalMaps minimum number of total public maps required (e.g., >6 means at least 7)
     * @param minSpamCount minimum number of spam public maps required
     * @param monthsBack number of months to look back for account creation
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of users with their spam counts
     */
    List<SpamUserResult> findUsersWithMinimumMapsAndSpam(int minTotalMaps, int minSpamCount, int monthsBack, int offset, int limit);

    /**
     * Count users with minimum total public maps and minimum spam count
     * @param minTotalMaps minimum number of total public maps required (e.g., >6 means at least 7)
     * @param minSpamCount minimum number of spam public maps required
     * @param monthsBack number of months to look back for account creation
     * @return count of users meeting the criteria
     */
    long countUsersWithMinimumMapsAndSpam(int minTotalMaps, int minSpamCount, int monthsBack);

    /**
     * Find all public mindmaps (excluding those from disabled accounts)
     * @return list of public mindmaps from active accounts
     */
    List<Mindmap> findPublicMindmaps();

    /**
     * Find all public mindmaps including those from disabled accounts with pagination
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of public mindmaps for the given page
     */
    List<Mindmap> findAllPublicMindmaps(int offset, int limit);

    /**
     * Count total number of public mindmaps
     * @return total count of public mindmaps
     */
    long countAllPublicMindmaps();

    /**
     * Find all public mindmaps created since a specific date with pagination
     * @param cutoffDate only return mindmaps created after this date
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of public mindmaps for the given page
     */
    List<Mindmap> findAllPublicMindmapsSince(java.util.Calendar cutoffDate, int offset, int limit);

    /**
     * Count total number of public mindmaps created since a specific date
     * @param cutoffDate only count mindmaps created after this date
     * @return total count of public mindmaps
     */
    long countAllPublicMindmapsSince(java.util.Calendar cutoffDate);

    /**
     * Find public mindmaps that need spam detection (version < current version)
     * @param cutoffDate only return mindmaps created after this date
     * @param currentVersion only return mindmaps with version less than this
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of public mindmaps needing spam detection
     */
    List<Mindmap> findPublicMindmapsNeedingSpamDetection(java.util.Calendar cutoffDate, int currentVersion, int offset, int limit);

    /**
     * Count public mindmaps that need spam detection (version < current version)
     * @param cutoffDate only count mindmaps created after this date
     * @param currentVersion only count mindmaps with version less than this
     * @return total count of public mindmaps needing spam detection
     */
    long countPublicMindmapsNeedingSpamDetection(java.util.Calendar cutoffDate, int currentVersion);

    /**
     * Find users with public spam mindmaps detected by specific spam type strategies
     * @param spamTypeCodes array of spam type codes to filter by (e.g., "FewNodesWithContent", "UserBehavior")
     * @param monthsBack number of months to look back for account creation
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of users with their spam counts for the specified spam types
     */
    List<SpamUserResult> findUsersWithPublicSpamMapsByType(String[] spamTypeCodes, int monthsBack, int offset, int limit);

    /**
     * Count users with public spam mindmaps detected by specific spam type strategies
     * @param spamTypeCodes array of spam type codes to filter by
     * @param monthsBack number of months to look back for account creation
     * @return count of users meeting the criteria
     */
    long countUsersWithPublicSpamMapsByType(String[] spamTypeCodes, int monthsBack);

    /**
     * Get all mindmap IDs that have history entries, for batch processing
     * 
     * @param offset starting position for pagination
     * @param batchSize maximum number of IDs to return
     * @return list of mindmap IDs that have history
     */
    List<Integer> getMindmapIdsWithHistory(int offset, int batchSize);

    /**
     * Get the last modification time of a mindmap
     * 
     * @param mindmapId the mindmap ID
     * @return the last modification time, or null if not found
     */
    @Nullable
    Calendar getMindmapLastModificationTime(int mindmapId);

    /**
     * Get all mindmaps (admin only)
     * @return list of all mindmaps in the system
     */
    List<Mindmap> getAllMindmaps();

    /**
     * Get all mindmaps with pagination support (admin only)
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of mindmaps for the given page
     */
    List<Mindmap> getAllMindmaps(int offset, int limit);

    /**
     * Count total number of mindmaps (admin only)
     * @return total count of mindmaps
     */
    long countAllMindmaps();

    /**
     * Search mindmaps with filtering and pagination (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of filtered mindmaps for the given page
     */
    List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, int offset, int limit);

    /**
     * Count mindmaps matching search criteria (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @return total count of matching mindmaps
     */
    long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked);

    /**
     * Get all mindmaps with pagination and spam filtering support (admin only)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of mindmaps for the given page
     */
    List<Mindmap> getAllMindmaps(Boolean filterSpam, int offset, int limit);

    /**
     * Get all mindmaps with pagination, spam filtering, and date filtering support (admin only)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param dateFilter filter by creation date ("1" = last 1 month, "3" = last 3 months, "6" = last 6 months, "all" = no filter)
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of mindmaps for the given page
     */
    List<Mindmap> getAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter, int offset, int limit);

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
     * Search mindmaps with filtering and pagination including spam filter (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of filtered mindmaps for the given page
     */
    List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, int offset, int limit);

    /**
     * Count mindmaps matching search criteria including spam filter (admin only)
     * @param search search term for title or description
     * @param filterPublic filter by public status (null for all)
     * @param filterLocked filter by locked status (null for all)
     * @param filterSpam filter by spam status (null for all, true for spam only, false for non-spam only)
     * @return total count of matching mindmaps
     */
    long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam);

    /**
     * Find mindmaps created by a specific user
     * @param userId the user ID
     * @return list of mindmaps created by the user
     */
    List<Mindmap> findByCreator(int userId);

    /**
     * Find the last modification time for mindmaps created by a specific user
     * @param userId the user ID
     * @return the last modification time, or null if no mindmaps found
     */
    @Nullable
    Calendar findLastModificationTimeByCreator(int userId);

    /**
     * Remove history entries for a specific mindmap
     * @param mindmapId the mindmap ID
     * @return number of history entries removed
     */
    int removeHistoryByMindmapId(int mindmapId);

    /**
     * Remove excess history entries for a mindmap, keeping only the most recent ones
     * @param mindmapId the mindmap ID
     * @param maxEntries maximum number of entries to keep
     * @return number of history entries removed
     */
    int removeExcessHistoryByMindmapId(int mindmapId, int maxEntries);
}
