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
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MindmapManagerImpl.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @Override
    public Collaborator findCollaborator(@NotNull final String email) {
        // Use Criteria API for type-safe query that handles inheritance properly
        // This ensures we find both Collaborator and Account entities
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Collaborator> cq = cb.createQuery(Collaborator.class);
        final Root<Collaborator> root = cq.from(Collaborator.class);
        
        cq.select(root).where(cb.equal(root.get("email"), email));
        
        final TypedQuery<Collaborator> query = entityManager.createQuery(cq);
        final List<Collaborator> collaborators = query.getResultList();
        
        if (collaborators != null && !collaborators.isEmpty()) {
            assert collaborators.size() == 1 : "More than one user with the same email!";
            return collaborators.get(0);
        }
        return null;
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
        // Use Criteria API with JOIN FETCH to explicitly load Account creator (not Collaborator proxy)
        // This avoids proxy narrowing warnings for JOINED inheritance
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Mindmap> cq = cb.createQuery(Mindmap.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        
        // JOIN FETCH creator to load Account directly, avoiding proxy narrowing
        root.fetch("creator", JoinType.LEFT);
        
        // Subquery for collaborations - using Subquery API
        final jakarta.persistence.criteria.Subquery<Integer> subquery = cq.subquery(Integer.class);
        final Root<Collaboration> collaborationRoot = subquery.from(Collaboration.class);
        subquery.select(collaborationRoot.get("mindMap").get("id"))
                .where(cb.equal(collaborationRoot.get("collaborator").get("id"), user.getId()));
        
        // Main query with IN subquery
        cq.select(root).where(root.get("id").in(subquery));
        
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Collaboration> findCollaboration(final int collaboratorId) {
        // Use named query for better performance and maintainability
        final TypedQuery<Collaboration> query = entityManager.createNamedQuery("Collaboration.findByCollaboratorId", Collaboration.class);
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
            try {
                entityManager.remove(managedCollaboration);
                entityManager.flush(); // Force immediate deletion to catch concurrent modification
            } catch (org.hibernate.StaleObjectStateException | jakarta.persistence.OptimisticLockException e) {
                // The collaboration was already deleted by another transaction
                // This is acceptable - the desired state (collaboration removed) is achieved
                logger.warn("Collaboration {} was already deleted by another transaction", collaboration.getId());
            }
        }
    }

    @Override
    public void removeCollaborator(@NotNull Collaborator collaborator) {
        entityManager.remove(collaborator);
    }

    @Override
    @Nullable
    public Mindmap getMindmapById(int id) {
        // Use Criteria API with JOIN FETCH to explicitly load Account (not Collaborator proxy)
        // This avoids proxy narrowing warnings for JOINED inheritance
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Mindmap> cq = cb.createQuery(Mindmap.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        
        // JOIN FETCH creator to load Account directly, avoiding proxy narrowing
        root.fetch("creator", JoinType.LEFT);
        
        cq.select(root).where(cb.equal(root.get("id"), id));
        
        final TypedQuery<Mindmap> query = entityManager.createQuery(cq);
        final List<Mindmap> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Mindmap getMindmapByTitle(final String title, final Account user) {
        // Use Criteria API with JOIN FETCH to explicitly load Account creator (not Collaborator proxy)
        // This avoids proxy narrowing warnings for JOINED inheritance
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Mindmap> cq = cb.createQuery(Mindmap.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        
        // JOIN FETCH creator to load Account directly, avoiding proxy narrowing
        root.fetch("creator", JoinType.LEFT);
        
        cq.select(root)
          .where(cb.and(
              cb.equal(root.get("title"), title),
              cb.equal(root.get("creator"), user)
          ));

        final TypedQuery<Mindmap> query = entityManager.createQuery(cq);
        final List<Mindmap> mindMaps = query.getResultList();

        return mindMaps != null && !mindMaps.isEmpty() ? mindMaps.get(0) : null;
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

    /**
     * Evicts Collaborator entities from Hibernate second-level cache.
     * This ensures that cached Collaborator entities with stale collaboration collections
     * are refreshed after bulk collaboration deletions.
     */
    private void evictCollaboratorCache(java.util.Set<Integer> collaboratorIds) {
        if (collaboratorIds.isEmpty()) {
            return;
        }
        try {
            org.hibernate.SessionFactory sessionFactory = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class);
            for (Integer collaboratorId : collaboratorIds) {
                sessionFactory.getCache().evict(Collaborator.class, collaboratorId);
            }
        } catch (Exception e) {
            // Log but don't fail the operation - cache eviction is best effort
            logger.warn("Failed to evict Collaborator cache for IDs {}: {}", collaboratorIds, e.getMessage());
        }
    }

    @Override
    public void removeMindmap(@NotNull final Mindmap mindmap) {
        final int mindmapId = mindmap.getId();
        final Mindmap managedMindmap = entityManager.find(Mindmap.class, mindmapId);
        if (managedMindmap == null) {
            logger.warn("Mindmap with ID {} was not found during deletion", mindmapId);
            return;
        }

        final java.util.Set<Integer> collaboratorIdsToEvict = managedMindmap.getCollaborations().stream()
            .map(Collaboration::getCollaborator)
            .filter(java.util.Objects::nonNull)
            .map(Collaborator::getId)
            .collect(java.util.stream.Collectors.toSet());

        // Break the many-to-many to prevent cascade re-persist attempts
        if (!managedMindmap.getLabels().isEmpty()) {
            managedMindmap.getLabels().clear();
        }

        // Explicitly remove the spam info to avoid @MapsId re-persist side effects
        final MindmapSpamInfo spamInfo = managedMindmap.getSpamInfo();
        if (spamInfo != null) {
            managedMindmap.setSpamInfo(null);
            entityManager.remove(spamInfo);
        }

        // Remove collaborations to keep both sides of the relationship consistent
        if (!managedMindmap.getCollaborations().isEmpty()) {
            final java.util.Set<Collaboration> collaborations = new java.util.HashSet<>(managedMindmap.getCollaborations());
            for (Collaboration collaboration : collaborations) {
                final Collaborator collaborator = collaboration.getCollaborator();
                if (collaborator != null) {
                    collaborator.getCollaborations().remove(collaboration);
                }
                managedMindmap.removedCollaboration(collaboration);
                entityManager.remove(collaboration);
            }
        }

        entityManager.remove(managedMindmap);
        entityManager.flush();

        // Evict caches after the delete is safely flushed
        evictCollaboratorCache(collaboratorIdsToEvict);
        entityManager.getEntityManagerFactory().getCache().evict(Mindmap.class, mindmapId);
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
            "JOIN m.creator c " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND c.creationDate >= :cutoffDate " +
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
            "JOIN m.creator c " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND c.creationDate >= :cutoffDate " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) >= :spamThreshold " +
            "ORDER BY c.id", 
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
            "JOIN m.creator c " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND c.creationDate >= :cutoffDate " +
            "AND (:lastUserId IS NULL OR c.id > :lastUserId) " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) >= :spamThreshold " +
            "ORDER BY c.id", 
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
            "JOIN m.creator c " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE c.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "GROUP BY m.creator " +
            "HAVING COUNT(CASE WHEN s.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "   AND (COUNT(CASE WHEN s.spamDetected = true THEN 1 END) * 1.0 / COUNT(m.id)) >= :spamRatioThreshold " +
            "ORDER BY c.id", 
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
            "JOIN m.creator c " +
            "WHERE c.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    JOIN m2.creator c2 " +
            "    LEFT JOIN m2.spamInfo s2 " +
            "    WHERE c2.creationDate >= :cutoffDate " +
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
            "JOIN m.creator c " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND c.creationDate >= :cutoffDate " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    JOIN m2.creator c2 " +
            "    JOIN m2.spamInfo s2 " +
            "    WHERE s2.spamDetected = true " +
            "      AND m2.isPublic = true " +
            "    AND c2.creationDate >= :cutoffDate " +
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
    public List<SpamUserResult> findUsersWithMinimumMapsAndSpam(int minTotalMaps, int minSpamCount, int monthsBack, int offset, int limit) {
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT m.creator, " +
            "       COUNT(CASE WHEN s.spamDetected = true THEN 1 END) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE c.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "GROUP BY m.creator " +
            "HAVING COUNT(m.id) > :minTotalMaps " +
            "   AND COUNT(CASE WHEN s.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "ORDER BY c.id", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("minTotalMaps", minTotalMaps);
        query.setParameter("minSpamCount", minSpamCount);
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
    public long countUsersWithMinimumMapsAndSpam(int minTotalMaps, int minSpamCount, int monthsBack) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT m.creator) " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "WHERE c.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    JOIN m2.creator c2 " +
            "    LEFT JOIN m2.spamInfo s2 " +
            "    WHERE c2.creationDate >= :cutoffDate " +
            "      AND m2.isPublic = true " +
            "    GROUP BY m2.creator " +
            "    HAVING COUNT(m2.id) > :minTotalMaps " +
            "       AND COUNT(CASE WHEN s2.spamDetected = true THEN 1 END) >= :minSpamCount" +
            ")",
            Long.class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("minTotalMaps", minTotalMaps);
        query.setParameter("minSpamCount", minSpamCount);
        query.setParameter("cutoffDate", cutoffDate);
        
        return query.getSingleResult();
    }

    @Override
    public List<SpamRatioUserResult> findUsersWithHighPublicSpamRatio(double spamRatioThreshold, int monthsBack, int offset, int limit) {
        // Optimized: Select Account ID instead of full Account entity to avoid eager loading of large fields
        // This prevents loading oauthToken (TEXT), password, and other unnecessary data
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT c.id, " +
            "       COUNT(CASE WHEN s.spamDetected = true THEN 1 END) as spamCount, " +
            "       COUNT(m.id) as totalCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE c.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "GROUP BY c.id " +
            "HAVING COUNT(m.id) > 0 " +
            "   AND (COUNT(CASE WHEN s.spamDetected = true THEN 1 END) * 1.0 / COUNT(m.id)) >= :spamRatioThreshold " +
            "ORDER BY c.id", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("spamRatioThreshold", spamRatioThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        // Get query results first
        List<Object[]> results = query.getResultList();
        
        // Batch load Account entities to avoid N+1 queries
        List<Integer> accountIds = results.stream()
                .map(result -> (Integer) result[0])
                .collect(java.util.stream.Collectors.toList());
        
        // Load all Account entities in a single query
        List<Account> accounts = accountIds.isEmpty() ? new ArrayList<>() :
            entityManager.createQuery(
                "SELECT a FROM com.wisemapping.model.Account a WHERE a.id IN :ids",
                Account.class)
                .setParameter("ids", accountIds)
                .getResultList();
        
        // Create a map for quick lookup
        java.util.Map<Integer, Account> accountMap = accounts.stream()
                .collect(java.util.stream.Collectors.toMap(Account::getId, account -> account));
        
        // Convert Object[] results to SpamRatioUserResult objects
        // Load Account entities separately to avoid eager loading of unnecessary relationships
        return results.stream()
                .map(result -> {
                    Integer accountId = (Integer) result[0];
                    Long spamCount = (Long) result[1];
                    Long totalCount = (Long) result[2];
                    Account user = accountMap.get(accountId);
                    if (user == null) {
                        logger.warn("Account with ID {} not found in batch load", accountId);
                        return null;
                    }
                    return new SpamRatioUserResult(user, spamCount, totalCount);
                })
                .filter(result -> result != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long countUsersWithHighPublicSpamRatio(double spamRatioThreshold, int monthsBack) {
        // Count distinct users with high public spam ratio.
        // Uses outer COUNT over subquery to ensure we always get a result (0 if no matches),
        // avoiding EmptyResultDataAccessException when the database is empty.
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT m.creator) " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "WHERE c.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "  AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    JOIN m2.creator c2 " +
            "    LEFT JOIN m2.spamInfo s2 " +
            "    WHERE c2.creationDate >= :cutoffDate " +
            "      AND m2.isPublic = true " +
            "    GROUP BY m2.creator " +
            "    HAVING COUNT(m2.id) > 0 " +
            "       AND (COUNT(CASE WHEN s2.spamDetected = true THEN 1 END) * 1.0 / COUNT(m2.id)) >= :spamRatioThreshold" +
            "  )",
            Long.class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("spamRatioThreshold", spamRatioThreshold);
        query.setParameter("cutoffDate", cutoffDate);
        
        return query.getSingleResult();
    }

    @Override
    public List<SpamUserResult> findUsersWithAnySpamMaps(int minSpamCount, int monthsBack, int offset, int limit) {
        // Optimized: Select Account ID instead of full Account entity to avoid eager loading of large fields
        // This prevents loading oauthToken (TEXT), password, and other unnecessary data
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT c.id, " +
            "       COUNT(CASE WHEN s.spamDetected = true THEN 1 END) as totalSpamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "LEFT JOIN m.spamInfo s " +
            "WHERE c.creationDate >= :cutoffDate " +
            "GROUP BY c.id " +
            "HAVING COUNT(CASE WHEN s.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "ORDER BY c.id", 
            Object[].class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("minSpamCount", (long) minSpamCount);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        // Get query results first
        List<Object[]> results = query.getResultList();
        
        // Batch load Account entities to avoid N+1 queries
        List<Integer> accountIds = results.stream()
                .map(result -> (Integer) result[0])
                .collect(java.util.stream.Collectors.toList());
        
        // Load all Account entities in a single query
        List<Account> accounts = accountIds.isEmpty() ? new ArrayList<>() :
            entityManager.createQuery(
                "SELECT a FROM com.wisemapping.model.Account a WHERE a.id IN :ids",
                Account.class)
                .setParameter("ids", accountIds)
                .getResultList();
        
        // Create a map for quick lookup
        java.util.Map<Integer, Account> accountMap = accounts.stream()
                .collect(java.util.stream.Collectors.toMap(Account::getId, account -> account));
        
        // Convert Object[] results to SpamUserResult objects
        // Load Account entities separately to avoid eager loading of unnecessary relationships
        return results.stream()
                .map(result -> {
                    Integer accountId = (Integer) result[0];
                    Long spamCount = (Long) result[1];
                    Account user = accountMap.get(accountId);
                    if (user == null) {
                        logger.warn("Account with ID {} not found in batch load", accountId);
                        return null;
                    }
                    return new SpamUserResult(user, spamCount);
                })
                .filter(result -> result != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public long countUsersWithAnySpamMaps(int minSpamCount, int monthsBack) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT m.creator) " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "WHERE c.creationDate >= :cutoffDate " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    JOIN m2.creator c2 " +
            "    LEFT JOIN m2.spamInfo s2 " +
            "    WHERE c2.creationDate >= :cutoffDate " +
            "    GROUP BY m2.creator " +
            "    HAVING COUNT(CASE WHEN s2.spamDetected = true THEN 1 END) >= :minSpamCount" +
            ")",
            Long.class);
        
        // Calculate cutoff date (monthsBack months ago)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -monthsBack);
        
        query.setParameter("minSpamCount", (long) minSpamCount);
        query.setParameter("cutoffDate", cutoffDate);
        
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> findPublicMindmaps() {
        final TypedQuery<Mindmap> query = entityManager.createNamedQuery("Mindmap.findPublicMindmaps", Mindmap.class);
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
        final TypedQuery<Long> query = entityManager.createNamedQuery("Mindmap.countAllPublicMindmaps", Long.class);
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

        // Optimized: Select Account ID instead of full Account entity to avoid eager loading of large fields
        final TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT c.id, COUNT(m.id) as spamCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "JOIN m.creator c " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "  AND c.creationDate >= :cutoffDate " +
            "  AND s.spamTypeCode IN (" + inClause.toString() + ") " +
            "GROUP BY c.id " +
            "ORDER BY c.id", 
            Object[].class);
        
        query.setParameter("cutoffDate", cutoffDate);
        for (int i = 0; i < spamTypeCodes.length; i++) {
            query.setParameter("spamType" + i, spamTypeCodes[i]);
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        
        // Get query results first
        List<Object[]> results = query.getResultList();
        
        // Batch load Account entities to avoid N+1 queries
        List<Integer> accountIds = results.stream()
                .map(result -> (Integer) result[0])
                .collect(java.util.stream.Collectors.toList());
        
        // Load all Account entities in a single query
        List<Account> accounts = accountIds.isEmpty() ? new ArrayList<>() :
            entityManager.createQuery(
                "SELECT a FROM com.wisemapping.model.Account a WHERE a.id IN :ids",
                Account.class)
                .setParameter("ids", accountIds)
                .getResultList();
        
        // Create a map for quick lookup
        java.util.Map<Integer, Account> accountMap = accounts.stream()
                .collect(java.util.stream.Collectors.toMap(Account::getId, account -> account));
        
        // Convert Object[] results to SpamUserResult objects
        // Load Account entities separately to avoid eager loading of unnecessary relationships
        return results.stream()
                .map(result -> {
                    Integer accountId = (Integer) result[0];
                    Long spamCount = (Long) result[1];
                    Account user = accountMap.get(accountId);
                    if (user == null) {
                        logger.warn("Account with ID {} not found in batch load", accountId);
                        return null;
                    }
                    return new SpamUserResult(user, spamCount);
                })
                .filter(result -> result != null)
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
            "JOIN m.creator c " +
            "JOIN m.spamInfo s " +
            "WHERE s.spamDetected = true " +
            "  AND m.isPublic = true " +
            "  AND c.creationDate >= :cutoffDate " +
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

    /**
     * Clean up old mindmap history entries.
     * Iterates through mindmaps in batches and removes history based on criteria.
     * - Cleans ALL history for mindmaps not modified in over 2 years
     * - Only cleans up history for mindmaps that haven't been modified in the last 1 week
     * - Cleans history with excess entries or old entries for mindmaps between 1 week and 2 years old
     *
     * @param cutoffDate entries older than this date will be deleted
     * @param maxEntriesPerMap maximum number of entries to keep per mindmap
     * @param batchSize number of mindmaps to process in each batch
     * @return total number of history entries deleted
     */
    @Override
    public List<Integer> getMindmapIdsWithHistory(int offset, int batchSize) {
        final TypedQuery<Integer> mindmapIdsQuery = entityManager.createQuery(
            "SELECT DISTINCT h.mindmapId FROM com.wisemapping.model.MindMapHistory h", Integer.class);
        mindmapIdsQuery.setFirstResult(offset);
        mindmapIdsQuery.setMaxResults(batchSize);
        return mindmapIdsQuery.getResultList();
    }

    @Override
    public Calendar getMindmapLastModificationTime(int mindmapId) {
        final TypedQuery<Calendar> lastModQuery = entityManager.createQuery(
            "SELECT m.lastModificationTime FROM com.wisemapping.model.Mindmap m " +
            "WHERE m.id = :mindmapId", Calendar.class);
        lastModQuery.setParameter("mindmapId", mindmapId);
        List<Calendar> results = lastModQuery.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }


    /**
     * Remove excess history entries for a mindmap, keeping only the most recent ones
     * @param mindmapId the mindmap ID
     * @param maxEntries maximum number of entries to keep
     * @return number of entries deleted
     */
    @Override
    @Transactional
    public int removeExcessHistoryByMindmapId(int mindmapId, int maxEntries) {
        // First, count total entries
        final TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(h) FROM com.wisemapping.model.MindMapHistory h " +
            "WHERE h.mindmapId = :mindmapId", Long.class);
        countQuery.setParameter("mindmapId", mindmapId);
        long totalCount = countQuery.getSingleResult();
        
        // If we don't have excess entries, nothing to do
        if (totalCount <= maxEntries) {
            return 0;
        }
        
        // Get the IDs of the most recent entries we want to keep
        final TypedQuery<Integer> keepQuery = entityManager.createQuery(
            "SELECT h.id FROM com.wisemapping.model.MindMapHistory h " +
            "WHERE h.mindmapId = :mindmapId " +
            "ORDER BY h.creationTime DESC", Integer.class);
        keepQuery.setParameter("mindmapId", mindmapId);
        keepQuery.setMaxResults(maxEntries);
        List<Integer> keepIds = keepQuery.getResultList();
        
        // Delete all entries except the ones we want to keep
        final Query deleteQuery = entityManager.createQuery(
            "DELETE FROM com.wisemapping.model.MindMapHistory h " +
            "WHERE h.mindmapId = :mindmapId AND h.id NOT IN :keepIds");
        deleteQuery.setParameter("mindmapId", mindmapId);
        deleteQuery.setParameter("keepIds", keepIds);
        
        return deleteQuery.executeUpdate();
    }


    @Override
    public List<Mindmap> getAllMindmaps() {
        final TypedQuery<Mindmap> query = entityManager.createNamedQuery("Mindmap.getAllMindmaps", Mindmap.class);
        return query.getResultList();
    }

    @Override
    public List<Mindmap> getAllMindmaps(int offset, int limit) {
        final TypedQuery<Mindmap> query = entityManager.createNamedQuery("Mindmap.getAllMindmaps", Mindmap.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllMindmaps() {
        final TypedQuery<Long> query = entityManager.createNamedQuery("Mindmap.countAllMindmaps", Long.class);
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
        // Use Criteria API with JOIN FETCH to explicitly load Account creator (not Collaborator proxy)
        // This avoids proxy narrowing warnings for JOINED inheritance
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Mindmap> cq = cb.createQuery(Mindmap.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        
        // JOIN FETCH all related entities to avoid lazy loading issues
        root.fetch("creator", JoinType.LEFT);
        root.fetch("spamInfo", JoinType.LEFT);
        root.fetch("lastEditor", JoinType.LEFT);
        root.fetch("collaborations", JoinType.LEFT);
        
        // Apply spam filter if provided - need to join again for WHERE clause
        if (filterSpam != null) {
            final jakarta.persistence.criteria.Join<Mindmap, ?> spamJoin = root.join("spamInfo", JoinType.LEFT);
            if (filterSpam) {
                cq.where(cb.isTrue(spamJoin.get("spamDetected")));
            } else {
                cq.where(cb.or(
                    cb.isNull(spamJoin),
                    cb.isFalse(spamJoin.get("spamDetected"))
                ));
            }
        }
        
        cq.select(root).distinct(true).orderBy(cb.desc(root.get("creationTime")));
        
        return entityManager.createQuery(cq)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public long countAllMindmaps(Boolean filterSpam) {
        // Use Criteria API for type-safe count query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        
        // Apply spam filter if provided
        if (filterSpam != null) {
            if (filterSpam) {
                final jakarta.persistence.criteria.Join<Mindmap, ?> spamJoin = root.join("spamInfo", JoinType.INNER);
                cq.where(cb.isTrue(spamJoin.get("spamDetected")));
            } else {
                final jakarta.persistence.criteria.Join<Mindmap, ?> spamJoin = root.join("spamInfo", JoinType.LEFT);
                cq.where(cb.or(
                    cb.isNull(spamJoin),
                    cb.isFalse(spamJoin.get("spamDetected"))
                ));
            }
        }
        
        cq.select(cb.countDistinct(root.get("id")));
        
        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public List<Mindmap> getAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter, int offset, int limit) {
        StringBuilder queryString = new StringBuilder(
            "SELECT DISTINCT m FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN FETCH m.spamInfo s " +
            "LEFT JOIN FETCH m.lastEditor le " +
            "LEFT JOIN FETCH m.collaborations col WHERE 1=1");
        
        if (filterPublic != null) {
            if (filterPublic) {
                queryString.append(" AND m.isPublic = true");
            } else {
                queryString.append(" AND m.isPublic = false");
            }
        }
        
        // Note: isLocked field doesn't exist in Mindmap entity, so this filter is ignored
        // Keeping the parameter for API compatibility but not applying it to the query
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND s.spamDetected = true");
            } else {
                queryString.append(" AND (s IS NULL OR s.spamDetected = false)");
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
            "SELECT COUNT(DISTINCT m.id) FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN m.spamInfo s WHERE 1=1");
        
        if (filterPublic != null) {
            if (filterPublic) {
                queryString.append(" AND m.isPublic = true");
            } else {
                queryString.append(" AND m.isPublic = false");
            }
        }
        
        // Note: isLocked field doesn't exist in Mindmap entity, so this filter is ignored
        // Keeping the parameter for API compatibility but not applying it to the query
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND s.spamDetected = true");
            } else {
                queryString.append(" AND (s IS NULL OR s.spamDetected = false)");
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
            "SELECT DISTINCT m FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN FETCH m.spamInfo s " +
            "LEFT JOIN FETCH m.lastEditor le " +
            "LEFT JOIN FETCH m.collaborations col ");
        
        // Handle special search patterns
        boolean isIdSearch = false;
        boolean isEmailSearch = false;
        String processedSearch = search;
        
        if (search != null && !search.trim().isEmpty()) {
            String trimmedSearch = search.trim();
            
            // Check if searching by ID (starts with #)
            if (trimmedSearch.startsWith("#")) {
                String idStr = trimmedSearch.substring(1);
                try {
                    Integer.parseInt(idStr);
                    isIdSearch = true;
                    processedSearch = idStr;
                } catch (NumberFormatException e) {
                    // Not a valid ID, treat as normal search
                }
            }
            // Check if searching by email (contains @ and .)
            else if (trimmedSearch.contains("@") && trimmedSearch.contains(".")) {
                isEmailSearch = true;
                processedSearch = trimmedSearch;
            }
        }
        
        // Build WHERE clause
        queryString.append("WHERE 1=1");
        
        if (search != null && !search.trim().isEmpty()) {
            if (isIdSearch) {
                queryString.append(" AND m.id = :searchId");
            } else if (isEmailSearch) {
                queryString.append(" AND LOWER(m.creator.email) = LOWER(:searchEmail)");
            } else {
                queryString.append(" AND (LOWER(m.title) LIKE LOWER(:search) OR LOWER(m.description) LIKE LOWER(:search))");
            }
        }
        
        if (filterPublic != null) {
            queryString.append(" AND m.isPublic = :filterPublic");
        }
        
        // Note: isLocked field doesn't exist in Mindmap entity, so this filter is ignored
        // Keeping the parameter for API compatibility but not applying it to the query
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND s.spamDetected = true");
            } else {
                queryString.append(" AND (s IS NULL OR s.spamDetected = false)");
            }
        }
        
        queryString.append(" ORDER BY m.creationTime DESC");
        
        final TypedQuery<Mindmap> query = entityManager.createQuery(queryString.toString(), Mindmap.class);
        
        // Set parameters based on search type
        if (search != null && !search.trim().isEmpty()) {
            if (isIdSearch) {
                query.setParameter("searchId", Integer.parseInt(processedSearch));
            } else if (isEmailSearch) {
                query.setParameter("searchEmail", processedSearch);
            } else {
                query.setParameter("search", "%" + processedSearch + "%");
            }
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
            "SELECT COUNT(DISTINCT m.id) FROM com.wisemapping.model.Mindmap m " +
            "LEFT JOIN m.spamInfo s WHERE 1=1");
        
        // Handle special search patterns
        boolean isIdSearch = false;
        boolean isEmailSearch = false;
        String processedSearch = search;
        
        if (search != null && !search.trim().isEmpty()) {
            String trimmedSearch = search.trim();
            
            // Check if searching by ID (starts with #)
            if (trimmedSearch.startsWith("#")) {
                String idStr = trimmedSearch.substring(1);
                try {
                    Integer.parseInt(idStr);
                    isIdSearch = true;
                    processedSearch = idStr;
                } catch (NumberFormatException e) {
                    // Not a valid ID, treat as normal search
                }
            }
            // Check if searching by email (contains @ and .)
            else if (trimmedSearch.contains("@") && trimmedSearch.contains(".")) {
                isEmailSearch = true;
                processedSearch = trimmedSearch;
            }
        }
        
        if (search != null && !search.trim().isEmpty()) {
            if (isIdSearch) {
                queryString.append(" AND m.id = :searchId");
            } else if (isEmailSearch) {
                queryString.append(" AND LOWER(m.creator.email) = LOWER(:searchEmail)");
            } else {
                queryString.append(" AND (LOWER(m.title) LIKE LOWER(:search) OR LOWER(m.description) LIKE LOWER(:search))");
            }
        }
        
        if (filterPublic != null) {
            queryString.append(" AND m.isPublic = :filterPublic");
        }
        
        // Note: isLocked field doesn't exist in Mindmap entity, so this filter is ignored
        // Keeping the parameter for API compatibility but not applying it to the query
        
        if (filterSpam != null) {
            if (filterSpam) {
                queryString.append(" AND s.spamDetected = true");
            } else {
                queryString.append(" AND (s IS NULL OR s.spamDetected = false)");
            }
        }
        
        final TypedQuery<Long> query = entityManager.createQuery(queryString.toString(), Long.class);
        
        // Set parameters based on search type
        if (search != null && !search.trim().isEmpty()) {
            if (isIdSearch) {
                query.setParameter("searchId", Integer.parseInt(processedSearch));
            } else if (isEmailSearch) {
                query.setParameter("searchEmail", processedSearch);
            } else {
                query.setParameter("search", "%" + processedSearch + "%");
            }
        }
        
        if (filterPublic != null) {
            query.setParameter("filterPublic", filterPublic);
        }
        
        return query.getSingleResult();
    }

    @Override
    public List<Mindmap> findByCreator(int userId) {
        // Use Criteria API with JOIN FETCH to explicitly load Account (not Collaborator proxy)
        // This avoids proxy narrowing warnings for JOINED inheritance
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Mindmap> cq = cb.createQuery(Mindmap.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        
        // JOIN FETCH creator to load Account directly, avoiding proxy narrowing
        root.fetch("creator", JoinType.LEFT);
        // Also create a join for the WHERE clause
        final jakarta.persistence.criteria.Join<Mindmap, Account> creatorJoin = root.join("creator", JoinType.LEFT);
        
        cq.select(root).where(cb.equal(creatorJoin.get("id"), userId));
        
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Integer> findMindmapIdsByCreator(int userId, int offset, int limit) {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        final Root<Mindmap> root = cq.from(Mindmap.class);
        final jakarta.persistence.criteria.Join<Mindmap, Account> creatorJoin = root.join("creator", JoinType.INNER);
        
        cq.select(root.get("id"))
          .where(cb.equal(creatorJoin.get("id"), userId))
          .orderBy(cb.asc(root.get("id")));
        
        return entityManager.createQuery(cq)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    @Nullable
    public Calendar findLastModificationTimeByCreator(int userId) {
        try {
            // Use Criteria API for type-safe query with aggregation
            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<Calendar> cq = cb.createQuery(Calendar.class);
            final Root<Mindmap> root = cq.from(Mindmap.class);
            final jakarta.persistence.criteria.Join<Mindmap, Account> creatorJoin = root.join("creator", JoinType.INNER);
            
            cq.select(cb.greatest(root.get("lastModificationTime").as(Calendar.class)))
              .where(cb.equal(creatorJoin.get("id"), userId));
            
            return entityManager.createQuery(cq).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public int removeHistoryByMindmapId(int mindmapId) {
        final Query query = entityManager.createQuery(
            "DELETE FROM com.wisemapping.model.MindMapHistory mh WHERE mh.mindmapId = :mindmapId");
        query.setParameter("mindmapId", mindmapId);
        return query.executeUpdate();
    }
}
