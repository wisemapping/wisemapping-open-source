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
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Repository("mindmapManager")
public class MindmapManagerImpl
        implements MindmapManager {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Collaborator findCollaborator(@NotNull final String email) {
        final Collaborator collaborator;
        // Use a more explicit query that handles inheritance properly
        // This ensures we find both Collaborator and Account entities
        final TypedQuery<Collaborator> query = entityManager.createQuery(
            "SELECT c FROM com.wisemapping.model.Collaborator c WHERE c.email = :email", 
            Collaborator.class);
        query.setParameter("email", email);

        final List<Collaborator> collaborators = query.getResultList();
        if (collaborators != null && !collaborators.isEmpty()) {
            assert collaborators.size() == 1 : "More than one user with the same email!";
            collaborator = collaborators.get(0);
        } else {
            collaborator = null;
        }
        return collaborator;
    }

    @Override
    public List<MindMapHistory> getHistoryFrom(int mindmapId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        final CriteriaQuery<MindMapHistory> cr = cb.createQuery(MindMapHistory.class);
        final Root<MindMapHistory> root = cr.from(MindMapHistory.class);

        final CriteriaQuery<MindMapHistory> select = cr.select(root)
                .where(cb.equal(root.get("mindmapId"), mindmapId))
                .orderBy(cb.desc(root.get("creationTime")));

        return entityManager.
                createQuery(select)
                .setMaxResults(30)
                .getResultList();
    }

    @Override
    public MindMapHistory getHistory(int historyId) {
        return entityManager.find(MindMapHistory.class, historyId);
    }

    @Override
    public void updateCollaboration(@NotNull Collaboration collaboration) {
        entityManager.persist(collaboration);
    }

    @Override
    @Nullable
    public Collaboration findCollaboration(int mindmapId, int collaboratorId) {
        // Use JPA Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Collaboration> cq = cb.createQuery(Collaboration.class);
        final Root<Collaboration> root = cq.from(Collaboration.class);
        
        // Build the query: WHERE mindMap.id = ? AND collaborator.id = ?
        cq.select(root)
          .where(cb.and(
              cb.equal(root.get("mindMap").get("id"), mindmapId),
              cb.equal(root.get("collaborator").get("id"), collaboratorId)
          ));
        
        // Execute query and get result
        final TypedQuery<Collaboration> query = entityManager.createQuery(cq);
        return query.getResultStream()
                   .findFirst()
                   .orElse(null);
    }

    @Override
    @NotNull
    public Collaboration findOrCreateCollaboration(@NotNull Mindmap mindmap, @NotNull Collaborator collaborator, @NotNull CollaborationRole role) {
        // Use JPA Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Collaboration> cq = cb.createQuery(Collaboration.class);
        final Root<Collaboration> root = cq.from(Collaboration.class);
        
        // Build the query: WHERE mindMap.id = ? AND collaborator.id = ?
        cq.select(root)
          .where(cb.and(
              cb.equal(root.get("mindMap").get("id"), mindmap.getId()),
              cb.equal(root.get("collaborator").get("id"), collaborator.getId())
          ));
        
        // Execute query and get result
        final TypedQuery<Collaboration> query = entityManager.createQuery(cq);
        final Optional<Collaboration> existing = query.getResultStream().findFirst();
        
        if (existing.isPresent()) {
            // Update role if different
            Collaboration collaboration = existing.get();
            if (collaboration.getRole() != role) {
                collaboration.setRole(role);
                entityManager.merge(collaboration);
            }
            return collaboration;
        }
        
        // Create new collaboration - this is safe because we verified it doesn't exist
        Collaboration newCollaboration = new Collaboration(role, collaborator, mindmap);
        entityManager.persist(newCollaboration);
        
        return newCollaboration;
    }

    @Override
    public List<Mindmap> findMindmapByUser(@NotNull Account user) {

        final TypedQuery<Mindmap> query = entityManager
                .createQuery("from com.wisemapping.model.Mindmap m where m.id in (select c.mindMap.id from com.wisemapping.model.Collaboration as c where c.collaborator.id=:collabId )", Mindmap.class);
        query.setParameter("collabId", user.getId());

        return query.getResultList();
    }

    @Override
    public List<Collaboration> findCollaboration(final int collaboratorId) {
        final TypedQuery<Collaboration> query = entityManager.createQuery("SELECT c FROM com.wisemapping.model.Collaboration c WHERE c.collaborator.id = :collaboratorId", Collaboration.class);
        query.setParameter("collaboratorId", collaboratorId);
        return query.getResultList();
    }

    @Override
    public void addCollaborator(@NotNull Collaborator collaborator) {
        assert collaborator != null : "ADD MINDMAP COLLABORATOR: Collaborator is required!";
        
        // Use DAO pattern: check if collaborator exists first, then persist or merge
        Collaborator existingCollaborator = findCollaborator(collaborator.getEmail());
        
        if (existingCollaborator != null) {
            // Update existing collaborator with new creation date if provided
            if (collaborator.getCreationDate() != null) {
                existingCollaborator.setCreationDate(collaborator.getCreationDate());
                entityManager.merge(existingCollaborator);
            }
            // Copy the ID to the collaborator object so caller can use it
            collaborator.setId(existingCollaborator.getId());
        } else {
            // No existing collaborator found, persist the new one
            entityManager.persist(collaborator);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeCollaboration(Collaboration collaboration) {
        // Use a fresh entity manager to avoid optimistic locking issues
        // The collaboration entity may have been modified in the current transaction
        Collaboration managedCollaboration = entityManager.find(Collaboration.class, collaboration.getId());
        if (managedCollaboration != null) {
            entityManager.remove(managedCollaboration);
        }
    }

    @Override
    public void removeCollaborator(@NotNull Collaborator collaborator) {
        entityManager.remove(collaborator);
    }

    @Override
    @Nullable
    public Mindmap getMindmapById(int id) {
        return entityManager.find(Mindmap.class, id);
    }

    @Override
    public Mindmap getMindmapByTitle(final String title, final Account user) {

        final TypedQuery<Mindmap> query = entityManager.createQuery("SELECT m FROM com.wisemapping.model.Mindmap m WHERE m.title = :title AND m.creator = :creator", Mindmap.class);
        query.setParameter("title", title);
        query.setParameter("creator", user);

        List<Mindmap> mindMaps = query.getResultList();

        Mindmap result = null;
        if (mindMaps != null && !mindMaps.isEmpty()) {
            result = mindMaps.get(0);
        }
        return result;
    }

    @Override
    public void addMindmap(Account user, Mindmap mindMap) {
        saveMindmap(mindMap);
    }

    @Override
    public void saveMindmap(Mindmap mindMap) {
        assert mindMap != null : "Save Mindmap: Mindmap is required!";
        entityManager.persist(mindMap);
        
        // Flush to ensure the mindmap is persisted and has an ID
        entityManager.flush();
        
        // Handle spam info after the mindmap has been persisted and has an ID
        MindmapSpamInfo spamInfo = mindMap.getSpamInfo();
        if (spamInfo != null) {
            // Ensure the spam info has the correct mindmap ID
            spamInfo.setMindmapId(mindMap.getId());
            updateMindmapSpamInfo(spamInfo);
        }
    }

    @Override
    public void updateMindmap(@NotNull Mindmap mindMap, boolean saveHistory) {
        assert mindMap != null : "Save Mindmap: Mindmap is required!";
        
        // Handle spam info separately using native SQL to prevent duplicate key violations
        MindmapSpamInfo spamInfo = mindMap.getSpamInfo();
        if (spamInfo != null) {
            updateMindmapSpamInfo(spamInfo);
        }
        
        entityManager.merge(mindMap);
        if (saveHistory) {
            saveHistory(mindMap);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMindmapSpamInfo(@NotNull com.wisemapping.model.MindmapSpamInfo spamInfo) {
        assert spamInfo != null : "Update MindmapSpamInfo: SpamInfo is required!";
        
        // Validate that we have a valid mindmap ID
        if (spamInfo.getMindmapId() == null || spamInfo.getMindmapId() <= 0) {
            throw new IllegalArgumentException("Invalid mindmap ID for spam info: " + spamInfo.getMindmapId());
        }
        
        // "Last Win" strategy: Use native SQL to force update regardless of conflicts
        // This ensures the latest data always wins, even in high concurrency scenarios
        try {
            // Use Java Calendar for consistent timestamp handling across all databases
            Calendar now = Calendar.getInstance();
            
            // Use native SQL for guaranteed "last win" behavior
            String sql = """
                INSERT INTO MINDMAP_SPAM_INFO (mindmap_id, spam_detected, spam_detection_version, spam_type_code, spam_description, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    spam_detected = VALUES(spam_detected),
                    spam_detection_version = VALUES(spam_detection_version),
                    spam_type_code = VALUES(spam_type_code),
                    spam_description = VALUES(spam_description),
                    updated_at = ?
                """;
            
            // Convert SpamStrategyType enum to Character for native SQL
            Character spamTypeCode = spamInfo.getSpamTypeCode() != null ? 
                spamInfo.getSpamTypeCode().getCode() : null;
            
            entityManager.createNativeQuery(sql)
                .setParameter(1, spamInfo.getMindmapId())
                .setParameter(2, spamInfo.isSpamDetected())
                .setParameter(3, spamInfo.getSpamDetectionVersion())
                .setParameter(4, spamTypeCode) // Convert enum to char
                .setParameter(5, spamInfo.getSpamDescription())
                .setParameter(6, now) // created_at
                .setParameter(7, now) // updated_at
                .setParameter(8, now) // updated_at for UPDATE clause
                .executeUpdate();
                
        } catch (Exception e) {
            // If native SQL fails, log the error and throw a runtime exception
            // This ensures we don't fall back to JPA merge which causes optimistic locking issues
            throw new RuntimeException("Failed to update MindmapSpamInfo for mindmap ID: " + 
                spamInfo.getMindmapId() + " (native SQL failed)", e);
        }
    }

    @Override
    public void removeMindmap(@NotNull final Mindmap mindmap) {
        // Delete history first ...
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        final CriteriaDelete<MindMapHistory> cr = cb.createCriteriaDelete(MindMapHistory.class);
        final Root<MindMapHistory> root = cr.from(MindMapHistory.class);

        final CriteriaDelete<MindMapHistory> deleteStatement = cr.where(cb.equal(root.get("mindmapId"), mindmap.getId()));
        entityManager.createQuery(deleteStatement).executeUpdate();

        // Remove collaborations ...
        mindmap.removedCollaboration(mindmap.getCollaborations());

        // Delete mindmap ....
        entityManager.remove(mindmap);
    }

    @Override
    public List<SpamUserResult> findUsersWithSpamMindmaps(int spamThreshold) {
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, COUNT(m.id) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) >= :spamThreshold", 
            Object[].class);
        query.setParameter("spamThreshold", spamThreshold);
        
        // Convert Object[] results to SpamUserResult objects
        return query.getResultList().stream()
                .map(result -> {
                    Account user = (Account) result[0];
                    Long spamCount = (Long) result[1];
                    return new SpamUserResult(user, spamCount);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SpamUserResult> findUsersWithSpamMindaps(int spamThreshold, int monthsBack) {
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, COUNT(m.id) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND m.creator.creationDate >= :cutoffDate " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) >= :spamThreshold", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("spamThreshold", spamThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        
        // Convert Object[] results to SpamUserResult objects
        return query.getResultList().stream()
                .map(result -> {
                    Account user = (Account) result[0];
                    Long spamCount = (Long) result[1];
                    return new SpamUserResult(user, spamCount);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SpamUserResult> findUsersWithSpamMindaps(int spamThreshold, int monthsBack, int offset, int limit) {
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, COUNT(m.id) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "AND m.creator.creationDate >= :cutoffDate " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) >= :spamThreshold " +
            "ORDER BY m.creator.id", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("spamThreshold", spamThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        // Convert Object[] results to SpamUserResult objects
        return query.getResultList().stream()
                .map(result -> {
                    Account user = (Account) result[0];
                    Long spamCount = (Long) result[1];
                    return new SpamUserResult(user, spamCount);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SpamUserResult> findUsersWithSpamMindapsCursor(int spamThreshold, int monthsBack, Integer lastUserId, int limit) {
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, COUNT(m.id) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND m.creator.creationDate >= :cutoffDate " +
            "AND (:lastUserId IS NULL OR m.creator.id > :lastUserId) " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) >= :spamThreshold " +
            "ORDER BY m.creator.id", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("spamThreshold", spamThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("lastUserId", lastUserId);
        query.setMaxResults(limit);
        
        // Convert Object[] results to SpamUserResult objects
        return query.getResultList().stream()
                .map(result -> {
                    Account user = (Account) result[0];
                    Long spamCount = (Long) result[1];
                    return new SpamUserResult(user, spamCount);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SpamRatioUserResult> findUsersWithHighSpamRatio(int minSpamCount, double spamRatioThreshold, int monthsBack, int offset, int limit) {
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, " +
            "       COUNT(CASE WHEN s.spamDetected = true THEN 1 END) as spamCount, " +
            "       COUNT(m.id) as totalCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE m.creator.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "GROUP BY m.creator " +
            "HAVING COUNT(CASE WHEN s.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "   AND (COUNT(CASE WHEN s.spamDetected = true THEN 1 END) * 1.0 / COUNT(m.id)) >= :spamRatioThreshold " +
            "ORDER BY m.creator.id", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("minSpamCount", minSpamCount);
        query.setParameter("spamRatioThreshold", spamRatioThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        // Convert Object[] results to SpamRatioUserResult objects
        return query.getResultList().stream()
                .map(result -> {
                    Account user = (Account) result[0];
                    Long spamCount = (Long) result[1];
                    Long totalCount = (Long) result[2];
                    return new SpamRatioUserResult(user, spamCount, totalCount);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long countUsersWithHighSpamRatio(int minSpamCount, double spamRatioThreshold, int monthsBack) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT m.creator) " +
            "FROM com.wisemapping.model.Mindmap m " +
            "WHERE m.creator.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    LEFT JOIN m2.spamInfo s2 " +
            "    WHERE m2.creator.creationDate >= :cutoffDate " +
            "      AND m2.isPublic = true " +
            "    GROUP BY m2.creator " +
            "    HAVING COUNT(CASE WHEN s2.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "       AND (COUNT(CASE WHEN s2.spamDetected = true THEN 1 END) * 1.0 / COUNT(m2.id)) >= :spamRatioThreshold" +
            ")",
            Long.class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("minSpamCount", minSpamCount);
        query.setParameter("spamRatioThreshold", spamRatioThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        
        return query.getSingleResult();
    }

    @Override
    public long countUsersWithSpamMindaps(int spamThreshold, int monthsBack) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT m.creator) " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND m.creator.creationDate >= :cutoffDate " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    JOIN m2.spamInfo s2 " +
            "    WHERE s2.spamDetected = true " +
            "      AND m2.isPublic = true " +
            "    AND m2.creator.creationDate >= :cutoffDate " +
            "    GROUP BY m2.creator " +
            "    HAVING COUNT(m2.id) >= :spamThreshold" +
            ")",
            Long.class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("spamThreshold", spamThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> findPublicMindmaps() {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE m.isPublic = true AND m.creator.suspended = false", 
            Mindmap.class);
        return query.getResultList();
    }

    @Override
    public List<Mindmap> findAllPublicMindmaps(int offset, int limit) {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE m.isPublic = true ORDER BY m.id", 
            Mindmap.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllPublicMindmaps() {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m WHERE m.isPublic = true", 
            Long.class);
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> findAllPublicMindmapsSince(Calendar cutoffDate, int offset, int limit) {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE m.isPublic = true AND m.creationTime >= :cutoffDate ORDER BY m.id", 
            Mindmap.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllPublicMindmapsSince(Calendar cutoffDate) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m WHERE m.isPublic = true AND m.creationTime >= :cutoffDate", 
            Long.class);
        query.setParameter("cutoffDate", cutoffDate);
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> findPublicMindmapsNeedingSpamDetection(Calendar cutoffDate, int currentVersion, int offset, int limit) {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE m.isPublic = true " +
            "  AND m.creationTime >= :cutoffDate " +
            "  AND (s.spamDetectionVersion < :currentVersion OR s.spamDetectionVersion IS NULL) " +
            "ORDER BY m.creationTime ASC", 
            Mindmap.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("currentVersion", currentVersion);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countPublicMindmapsNeedingSpamDetection(Calendar cutoffDate, int currentVersion) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE m.isPublic = true " +
            "  AND m.creationTime >= :cutoffDate " +
            "  AND (s.spamDetectionVersion < :currentVersion OR s.spamDetectionVersion IS NULL)", 
            Long.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("currentVersion", currentVersion);
        return query.getSingleResult();
    }

    @Override
    public List<SpamUserResult> findUsersWithPublicSpamMapsByType(String[] spamTypeCodes, int monthsBack, int offset, int limit) {
        if (spamTypeCodes == null || spamTypeCodes.length == 0) {
            return new ArrayList<>();
        }

        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);

        // Build the IN clause for spam type codes
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < spamTypeCodes.length; i++) {
            if (i > 0) inClause.append(", ");
            inClause.append(":spamType").append(i);
        }

        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, COUNT(m.id) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "  AND m.creator.creationDate >= :cutoffDate " +
            "  AND s.spamTypeCode IN (" + inClause.toString() + ") " +
            "GROUP BY m.creator " +
            "ORDER BY m.creator.id", 
            Object[].class);
        
        query.setParameter("cutoffDate", cutoffDate);
        for (int i = 0; i < spamTypeCodes.length; i++) {
            query.setParameter("spamType" + i, spamTypeCodes[i]);
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        // Convert Object[] results to SpamUserResult objects
        return query.getResultList().stream()
                .map(result -> {
                    Account user = (Account) result[0];
                    Long spamCount = (Long) result[1];
                    return new SpamUserResult(user, spamCount);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long countUsersWithPublicSpamMapsByType(String[] spamTypeCodes, int monthsBack) {
        if (spamTypeCodes == null || spamTypeCodes.length == 0) {
            return 0;
        }

        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);

        // Build the IN clause for spam type codes
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < spamTypeCodes.length; i++) {
            if (i > 0) inClause.append(", ");
            inClause.append(":spamType").append(i);
        }

        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT m.creator) " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "  AND m.creator.creationDate >= :cutoffDate " +
            "  AND s.spamTypeCode IN (" + inClause.toString() + ")", 
            Long.class);
        
        query.setParameter("cutoffDate", cutoffDate);
        for (int i = 0; i < spamTypeCodes.length; i++) {
            query.setParameter("spamType" + i, spamTypeCodes[i]);
        }
        
        return query.getSingleResult();
    }

    private void saveHistory(@NotNull final Mindmap mindMap) {
        final MindMapHistory history = new MindMapHistory();

        history.setZippedXml(mindMap.getZippedXml());
        history.setCreationTime(Calendar.getInstance());
        history.setEditor(mindMap.getLastEditor());
        history.setMindmapId(mindMap.getId());
        entityManager.merge(history);
    }

    @Override
    @Transactional
    public int cleanupOldMindmapHistory(Calendar cutoffDate, int maxEntriesPerMap, int batchSize) {
        int totalDeleted = 0;
        
        // First, delete entries older than cutoff date
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaDelete<MindMapHistory> deleteOldEntries = cb.createCriteriaDelete(MindMapHistory.class);
        final Root<MindMapHistory> root = deleteOldEntries.from(MindMapHistory.class);
        
        deleteOldEntries.where(cb.lessThan(root.get("creationTime"), cutoffDate));
        int deletedByDate = entityManager.createQuery(deleteOldEntries).executeUpdate();
        totalDeleted += deletedByDate;
        
        // Second, for each mindmap, keep only the most recent maxEntriesPerMap entries
        // Process mindmap IDs in batches to avoid loading all into memory
        int offset = 0;
        List<Integer> mindmapIds;
        
        do {
            // Get a batch of unique mindmap IDs that have history
            final TypedQuery<Integer> mindmapIdsQuery = entityManager.createQuery(
                "SELECT DISTINCT h.mindmapId FROM com.wisemapping.model.MindMapHistory h", Integer.class);
            mindmapIdsQuery.setFirstResult(offset);
            mindmapIdsQuery.setMaxResults(batchSize);
            mindmapIds = mindmapIdsQuery.getResultList();
            
            for (Integer mindmapId : mindmapIds) {
                // Count total history entries for this mindmap
                final TypedQuery<Long> countQuery = entityManager.createQuery(
                    "SELECT COUNT(h) FROM com.wisemapping.model.MindMapHistory h " +
                    "WHERE h.mindmapId = :mindmapId", Long.class);
                countQuery.setParameter("mindmapId", mindmapId);
                long totalCount = countQuery.getSingleResult();
                
                // If we have more entries than allowed, delete the excess
                if (totalCount > maxEntriesPerMap) {
                    // Get the IDs of entries to delete (oldest ones beyond the limit)
                    final TypedQuery<Integer> toDeleteQuery = entityManager.createQuery(
                        "SELECT h.id FROM com.wisemapping.model.MindMapHistory h " +
                        "WHERE h.mindmapId = :mindmapId " +
                        "ORDER BY h.creationTime ASC", Integer.class);
                    toDeleteQuery.setParameter("mindmapId", mindmapId);
                    toDeleteQuery.setFirstResult(maxEntriesPerMap); // Skip the newest entries
                    toDeleteQuery.setMaxResults((int) (totalCount - maxEntriesPerMap)); // Get the rest to delete
                    
                    final List<Integer> idsToDelete = toDeleteQuery.getResultList();
                    
                    // Delete the excess entries by ID
                    for (Integer historyId : idsToDelete) {
                        final MindMapHistory history = entityManager.find(MindMapHistory.class, historyId);
                        if (history != null) {
                            entityManager.remove(history);
                            totalDeleted++;
                        }
                    }
                }
            }
            
            offset += batchSize;
        } while (mindmapIds.size() == batchSize); // Continue while we get a full batch
        
        return totalDeleted;
    }

    @Override
    public List<Mindmap> getAllMindmaps() {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m ORDER BY m.creationTime DESC", Mindmap.class);
        return query.getResultList();
    }

    @Override
    public List<Mindmap> getAllMindmaps(int offset, int limit) {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m " +
            "ORDER BY m.creationTime DESC", Mindmap.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllMindmaps() {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m", Long.class);
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, int offset, int limit) {
        StringBuilder queryString = new StringBuilder(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (search != null && !search.trim().isEmpty()) {
            queryString.append(" AND (LOWER(m.title) LIKE LOWER(:search) OR LOWER(m.description) LIKE LOWER(:search))");
        }
        
        if (filterPublic != null) {
            queryString.append(" AND m.isPublic = :filterPublic");
        }
        
        // Note: Locked status would need to be determined by checking the lock manager
        // For now, we'll implement a basic version that doesn't filter by locked status
        // This could be enhanced later by joining with a locks table or checking lock status
        
        queryString.append(" ORDER BY m.creationTime DESC");
        
        final TypedQuery<Mindmap> query = entityManager.createQuery(queryString.toString(), Mindmap.class);
        
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        if (filterPublic != null) {
            query.setParameter("filterPublic", filterPublic);
        }
        
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked) {
        StringBuilder queryString = new StringBuilder(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (search != null && !search.trim().isEmpty()) {
            queryString.append(" AND (LOWER(m.title) LIKE LOWER(:search) OR LOWER(m.description) LIKE LOWER(:search))");
        }
        
        if (filterPublic != null) {
            queryString.append(" AND m.isPublic = :filterPublic");
        }
        
        final TypedQuery<Long> query = entityManager.createQuery(queryString.toString(), Long.class);
        
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        if (filterPublic != null) {
            query.setParameter("filterPublic", filterPublic);
        }
        
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> getAllMindmaps(Boolean filterSpam, int offset, int limit) {
        StringBuilder queryString = new StringBuilder(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND m.spamInfo.spamDetected = true");
            } else {
                queryString.append(" AND (m.spamInfo IS NULL OR m.spamInfo.spamDetected = false)");
            }
        }
        
        queryString.append(" ORDER BY m.creationTime DESC");
        
        final TypedQuery<Mindmap> query = entityManager.createQuery(queryString.toString(), Mindmap.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllMindmaps(Boolean filterSpam) {
        StringBuilder queryString = new StringBuilder(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND m.spamInfo.spamDetected = true");
            } else {
                queryString.append(" AND (m.spamInfo IS NULL OR m.spamInfo.spamDetected = false)");
            }
        }
        
        final TypedQuery<Long> query = entityManager.createQuery(queryString.toString(), Long.class);
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> getAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter, int offset, int limit) {
        StringBuilder queryString = new StringBuilder(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (filterPublic != null) {
            if (filterPublic) {
                queryString.append(" AND m.isPublic = true");
            } else {
                queryString.append(" AND m.isPublic = false");
            }
        }
        
        if (filterLocked != null) {
            if (filterLocked) {
                queryString.append(" AND m.isLocked = true");
            } else {
                queryString.append(" AND m.isLocked = false");
            }
        }
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND m.spamInfo.spamDetected = true");
            } else {
                queryString.append(" AND (m.spamInfo IS NULL OR m.spamInfo.spamDetected = false)");
            }
        }
        
        // Add date filter
        if (dateFilter != null && !dateFilter.equals("all")) {
            try {
                Integer.parseInt(dateFilter); // Validate it's a number
                queryString.append(" AND m.creationTime >= :dateThreshold");
            } catch (NumberFormatException e) {
                // Invalid date filter, ignore it
            }
        }
        
        queryString.append(" ORDER BY m.creationTime DESC");
        
        final TypedQuery<Mindmap> query = entityManager.createQuery(queryString.toString(), Mindmap.class);
        
        // Set date parameter if needed
        if (dateFilter != null && !dateFilter.equals("all")) {
            try {
                int months = Integer.parseInt(dateFilter);
                java.util.Calendar threshold = java.util.Calendar.getInstance();
                threshold.add(java.util.Calendar.MONTH, -months);
                query.setParameter("dateThreshold", threshold);
            } catch (NumberFormatException e) {
                // Invalid date filter, ignore it
            }
        }
        
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter) {
        StringBuilder queryString = new StringBuilder(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (filterPublic != null) {
            if (filterPublic) {
                queryString.append(" AND m.isPublic = true");
            } else {
                queryString.append(" AND m.isPublic = false");
            }
        }
        
        if (filterLocked != null) {
            if (filterLocked) {
                queryString.append(" AND m.isLocked = true");
            } else {
                queryString.append(" AND m.isLocked = false");
            }
        }
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND m.spamInfo.spamDetected = true");
            } else {
                queryString.append(" AND (m.spamInfo IS NULL OR m.spamInfo.spamDetected = false)");
            }
        }
        
        // Add date filter
        if (dateFilter != null && !dateFilter.equals("all")) {
            try {
                Integer.parseInt(dateFilter); // Validate it's a number
                queryString.append(" AND m.creationTime >= :dateThreshold");
            } catch (NumberFormatException e) {
                // Invalid date filter, ignore it
            }
        }
        
        final TypedQuery<Long> query = entityManager.createQuery(queryString.toString(), Long.class);
        
        // Set date parameter if needed
        if (dateFilter != null && !dateFilter.equals("all")) {
            try {
                int months = Integer.parseInt(dateFilter);
                java.util.Calendar threshold = java.util.Calendar.getInstance();
                threshold.add(java.util.Calendar.MONTH, -months);
                query.setParameter("dateThreshold", threshold);
            } catch (NumberFormatException e) {
                // Invalid date filter, ignore it
            }
        }
        
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, int offset, int limit) {
        StringBuilder queryString = new StringBuilder(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (search != null && !search.trim().isEmpty()) {
            queryString.append(" AND (LOWER(m.title) LIKE LOWER(:search) OR LOWER(m.description) LIKE LOWER(:search))");
        }
        
        if (filterPublic != null) {
            queryString.append(" AND m.isPublic = :filterPublic");
        }
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND m.spamInfo.spamDetected = true");
            } else {
                queryString.append(" AND (m.spamInfo IS NULL OR m.spamInfo.spamDetected = false)");
            }
        }
        
        queryString.append(" ORDER BY m.creationTime DESC");
        
        final TypedQuery<Mindmap> query = entityManager.createQuery(queryString.toString(), Mindmap.class);
        
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        if (filterPublic != null) {
            query.setParameter("filterPublic", filterPublic);
        }
        
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam) {
        StringBuilder queryString = new StringBuilder(
            "SELECT COUNT(m) FROM com.wisemapping.model.Mindmap m WHERE 1=1");
        
        if (search != null && !search.trim().isEmpty()) {
            queryString.append(" AND (LOWER(m.title) LIKE LOWER(:search) OR LOWER(m.description) LIKE LOWER(:search))");
        }
        
        if (filterPublic != null) {
            queryString.append(" AND m.isPublic = :filterPublic");
        }
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND m.spamInfo.spamDetected = true");
            } else {
                queryString.append(" AND (m.spamInfo IS NULL OR m.spamInfo.spamDetected = false)");
            }
        }
        
        final TypedQuery<Long> query = entityManager.createQuery(queryString.toString(), Long.class);
        
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        if (filterPublic != null) {
            query.setParameter("filterPublic", filterPublic);
        }
        
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> findByCreator(int userId) {
        final TypedQuery<Mindmap> query = entityManager.createQuery(
            "SELECT m FROM com.wisemapping.model.Mindmap m WHERE m.creator.id = :userId", 
            Mindmap.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    @Nullable
    public Calendar findLastModificationTimeByCreator(int userId) {
        try {
            final TypedQuery<Calendar> query = entityManager.createQuery(
                "SELECT MAX(m.lastModificationTime) FROM com.wisemapping.model.Mindmap m WHERE m.creator.id = :userId", 
                Calendar.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int deleteHistoryByMindmapId(int mindmapId) {
        final Query query = entityManager.createQuery(
            "DELETE FROM com.wisemapping.model.MindMapHistory mh WHERE mh.mindmapId = :mindmapId");
        query.setParameter("mindmapId", mindmapId);
        return query.executeUpdate();
    }
}
