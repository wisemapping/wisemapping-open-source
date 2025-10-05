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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

/**
 * Implementation of InactiveMindmapManager for managing inactive mindmaps.
 */
@Repository
public class InactiveMindmapManagerImpl implements InactiveMindmapManager {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void addInactiveMindmap(@NotNull InactiveMindmap inactiveMindmap) {
        assert inactiveMindmap != null : "inactiveMindmap is null";
        entityManager.persist(inactiveMindmap);
    }

    @Override
    public List<InactiveMindmap> findByCreator(@NotNull Account creator) {
        assert creator != null : "creator is null";
        return findByCreator(creator.getId());
    }

    @Override
    public List<InactiveMindmap> findByCreator(int creatorId) {
        final TypedQuery<InactiveMindmap> query = entityManager.createQuery(
                "SELECT im FROM com.wisemapping.model.InactiveMindmap im WHERE im.creator.id = :creatorId",
                InactiveMindmap.class);
        query.setParameter("creatorId", creatorId);
        return query.getResultList();
    }

    @Override
    public List<InactiveMindmap> findCreatedBefore(@NotNull Calendar cutoffDate) {
        assert cutoffDate != null : "cutoffDate is null";
        final TypedQuery<InactiveMindmap> query = entityManager.createQuery(
                "SELECT im FROM com.wisemapping.model.InactiveMindmap im WHERE im.creationTime <= :cutoffDate ORDER BY im.creationTime DESC",
                InactiveMindmap.class);
        query.setParameter("cutoffDate", cutoffDate);
        return query.getResultList();
    }

    @Override
    public long countCreatedBefore(@NotNull Calendar cutoffDate) {
        assert cutoffDate != null : "cutoffDate is null";
        final TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(im) FROM com.wisemapping.model.InactiveMindmap im WHERE im.creationTime <= :cutoffDate",
                Long.class);
        query.setParameter("cutoffDate", cutoffDate);
        return query.getSingleResult();
    }

    @Override
    public long countAllInactiveMindmaps() {
        final TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(im) FROM com.wisemapping.model.InactiveMindmap im",
                Long.class);
        Long result = query.getSingleResult();
        return result != null ? result : 0L;
    }

    @Override
    public InactiveMindmap findByOriginalMindmapId(int originalMindmapId) {
        final TypedQuery<InactiveMindmap> query = entityManager.createQuery(
                "SELECT im FROM com.wisemapping.model.InactiveMindmap im WHERE im.originalMindmapId = :originalMindmapId",
                InactiveMindmap.class);
        query.setParameter("originalMindmapId", originalMindmapId);
        final List<InactiveMindmap> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public int deleteOlderThan(@NotNull Calendar cutoffDate) {
        assert cutoffDate != null : "cutoffDate is null";
        final Query query = entityManager.createQuery(
                "DELETE FROM com.wisemapping.model.InactiveMindmap im WHERE im.creationTime <= :cutoffDate");
        query.setParameter("cutoffDate", cutoffDate);
        return query.executeUpdate();
    }

    @Override
    public List<InactiveMindmap> findAll() {
        final TypedQuery<InactiveMindmap> query = entityManager.createQuery(
                "SELECT im FROM com.wisemapping.model.InactiveMindmap im ORDER BY im.migrationDate DESC",
                InactiveMindmap.class);
        return query.getResultList();
    }

    @Override
    public List<InactiveMindmap> findAll(int offset, int limit) {
        final TypedQuery<InactiveMindmap> query = entityManager.createQuery(
                "SELECT im FROM com.wisemapping.model.InactiveMindmap im ORDER BY im.migrationDate DESC",
                InactiveMindmap.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void removeInactiveMindmap(@NotNull InactiveMindmap inactiveMindmap) {
        assert inactiveMindmap != null : "inactiveMindmap is null";
        entityManager.remove(inactiveMindmap);
    }
}
