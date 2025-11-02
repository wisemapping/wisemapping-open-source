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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InactiveMindmap> cq = cb.createQuery(InactiveMindmap.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(root).where(cb.equal(root.get("creator").get("id"), creatorId));
        
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<InactiveMindmap> findCreatedBefore(@NotNull Calendar cutoffDate) {
        assert cutoffDate != null : "cutoffDate is null";
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InactiveMindmap> cq = cb.createQuery(InactiveMindmap.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(root)
          .where(cb.lessThanOrEqualTo(root.get("creationTime"), cutoffDate))
          .orderBy(cb.desc(root.get("creationTime")));
        
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countCreatedBefore(@NotNull Calendar cutoffDate) {
        assert cutoffDate != null : "cutoffDate is null";
        // Use Criteria API for type-safe count query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(cb.count(root))
          .where(cb.lessThanOrEqualTo(root.get("creationTime"), cutoffDate));
        
        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countAllInactiveMindmaps() {
        // Use Criteria API for type-safe count query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(cb.count(root));
        
        Long result = entityManager.createQuery(cq).getSingleResult();
        return result != null ? result : 0L;
    }

    @Override
    public InactiveMindmap findByOriginalMindmapId(int originalMindmapId) {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InactiveMindmap> cq = cb.createQuery(InactiveMindmap.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(root).where(cb.equal(root.get("originalMindmapId"), originalMindmapId));
        
        final List<InactiveMindmap> results = entityManager.createQuery(cq).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public int deleteOlderThan(@NotNull Calendar cutoffDate) {
        assert cutoffDate != null : "cutoffDate is null";
        // Use Criteria API for type-safe delete query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaDelete<InactiveMindmap> cd = cb.createCriteriaDelete(InactiveMindmap.class);
        final Root<InactiveMindmap> root = cd.from(InactiveMindmap.class);
        
        cd.where(cb.lessThanOrEqualTo(root.get("creationTime"), cutoffDate));
        
        return entityManager.createQuery(cd).executeUpdate();
    }

    @Override
    public List<InactiveMindmap> findAll() {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InactiveMindmap> cq = cb.createQuery(InactiveMindmap.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(root).orderBy(cb.desc(root.get("migrationDate")));
        
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<InactiveMindmap> findAll(int offset, int limit) {
        // Use Criteria API for type-safe query with pagination
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InactiveMindmap> cq = cb.createQuery(InactiveMindmap.class);
        final Root<InactiveMindmap> root = cq.from(InactiveMindmap.class);
        
        cq.select(root).orderBy(cb.desc(root.get("migrationDate")));
        
        return entityManager.createQuery(cq)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    @Transactional
    public void removeInactiveMindmap(@NotNull InactiveMindmap inactiveMindmap) {
        assert inactiveMindmap != null : "inactiveMindmap is null";
        entityManager.remove(inactiveMindmap);
    }
}
