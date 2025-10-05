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

import com.wisemapping.model.Account;
import com.wisemapping.model.InactiveMindmap;
import jakarta.validation.constraints.NotNull;

import java.util.Calendar;
import java.util.List;

/**
 * Data Access Object for managing inactive mindmaps.
 * These are mindmaps that have been moved from inactive users and are no longer accessible.
 */
public interface InactiveMindmapManager {

    /**
     * Save an inactive mindmap to the database.
     * @param inactiveMindmap the inactive mindmap to save
     */
    void addInactiveMindmap(@NotNull InactiveMindmap inactiveMindmap);

    /**
     * Find inactive mindmaps by creator (original creator before migration).
     * @param creator the creator account
     * @return list of inactive mindmaps created by the user
     */
    List<InactiveMindmap> findByCreator(@NotNull Account creator);

    /**
     * Find inactive mindmaps by creator ID.
     * @param creatorId the creator's ID
     * @return list of inactive mindmaps created by the user
     */
    List<InactiveMindmap> findByCreator(int creatorId);

    /**
     * Find inactive mindmaps created before a specific date.
     * @param cutoffDate the cutoff date
     * @return list of inactive mindmaps created before the cutoff date
     */
    List<InactiveMindmap> findCreatedBefore(@NotNull Calendar cutoffDate);

    /**
     * Count inactive mindmaps created before a specific date.
     * @param cutoffDate the cutoff date
     * @return count of inactive mindmaps created before the cutoff date
     */
    long countCreatedBefore(@NotNull Calendar cutoffDate);

    /**
     * Count all inactive mindmaps.
     * @return total count of inactive mindmaps
     */
    long countAllInactiveMindmaps();

    /**
     * Find inactive mindmaps by original mindmap ID.
     * @param originalMindmapId the original mindmap ID
     * @return the inactive mindmap if found, null otherwise
     */
    InactiveMindmap findByOriginalMindmapId(int originalMindmapId);

    /**
     * Delete inactive mindmaps older than a specific date.
     * @param cutoffDate the cutoff date - mindmaps created before this date will be deleted
     * @return number of mindmaps deleted
     */
    int deleteOlderThan(@NotNull Calendar cutoffDate);

    /**
     * Get all inactive mindmaps (for administrative purposes).
     * @return list of all inactive mindmaps
     */
    List<InactiveMindmap> findAll();

    /**
     * Find inactive mindmaps with pagination.
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of inactive mindmaps
     */
    List<InactiveMindmap> findAll(int offset, int limit);

    /**
     * Remove an inactive mindmap from the inactive table.
     * @param inactiveMindmap the inactive mindmap to remove
     */
    void removeInactiveMindmap(@NotNull InactiveMindmap inactiveMindmap);
}
