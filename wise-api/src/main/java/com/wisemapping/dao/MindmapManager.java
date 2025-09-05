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

    Mindmap getMindmapByTitle(final String name, final Account user);

    void addCollaborator(Collaborator collaborator);

    void addMindmap(Account user, Mindmap mindmap);

    void saveMindmap(Mindmap mindmap);

    void updateMindmap(@NotNull Mindmap mindmap, boolean saveHistory);

    void removeCollaborator(@NotNull Collaborator collaborator);

    void removeMindmap(Mindmap mindmap);

    void removeCollaboration(Collaboration collaboration);

    List<MindMapHistory> getHistoryFrom(int mindmapId);

    MindMapHistory getHistory(int historyId);

    void updateCollaboration(@NotNull Collaboration collaboration);

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
}
