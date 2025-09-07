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
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.List;

@Repository("mindmapManager")
public class MindmapManagerImpl
        implements MindmapManager {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Collaborator findCollaborator(@NotNull final String email) {
        final Collaborator collaborator;
        final TypedQuery<Collaborator> query = entityManager.createQuery("SELECT c FROM com.wisemapping.model.Collaborator c WHERE c.email = :email", Collaborator.class);
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
        entityManager.persist(collaborator);
    }

    @Override
    public void removeCollaboration(Collaboration collaboration) {
        entityManager.remove(collaboration);
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
    }

    @Override
    public void updateMindmap(@NotNull Mindmap mindMap, boolean saveHistory) {
        assert mindMap != null : "Save Mindmap: Mindmap is required!";
        entityManager.merge(mindMap);
        if (saveHistory) {
            saveHistory(mindMap);
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
            "WHERE m.spamDetected = true " +
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
            "WHERE m.spamDetected = true " +
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
            "WHERE m.spamDetected = true " +
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
            "WHERE m.spamDetected = true " +
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
            "       COUNT(CASE WHEN m.spamDetected = true THEN 1 END) as spamCount, " +
            "       COUNT(m.id) as totalCount " +
            "FROM com.wisemapping.model.Mindmap m " +
            "WHERE m.creator.creationDate >= :cutoffDate " +
            "  AND m.isPublic = true " +
            "GROUP BY m.creator " +
            "HAVING COUNT(CASE WHEN m.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "   AND (COUNT(CASE WHEN m.spamDetected = true THEN 1 END) * 1.0 / COUNT(m.id)) >= :spamRatioThreshold " +
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
            "    WHERE m2.creator.creationDate >= :cutoffDate " +
            "      AND m2.isPublic = true " +
            "    GROUP BY m2.creator " +
            "    HAVING COUNT(CASE WHEN m2.spamDetected = true THEN 1 END) >= :minSpamCount " +
            "       AND (COUNT(CASE WHEN m2.spamDetected = true THEN 1 END) * 1.0 / COUNT(m2.id)) >= :spamRatioThreshold" +
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
            "WHERE m.spamDetected = true " +
            "  AND m.isPublic = true " +
            "AND m.creator.creationDate >= :cutoffDate " +
            "AND m.creator IN (" +
            "    SELECT m2.creator " +
            "    FROM com.wisemapping.model.Mindmap m2 " +
            "    WHERE m2.spamDetected = true " +
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

    private void saveHistory(@NotNull final Mindmap mindMap) {
        final MindMapHistory history = new MindMapHistory();

        history.setZippedXml(mindMap.getZippedXml());
        history.setCreationTime(Calendar.getInstance());
        history.setEditor(mindMap.getLastEditor());
        history.setMindmapId(mindMap.getId());
        entityManager.merge(history);
    }
}
