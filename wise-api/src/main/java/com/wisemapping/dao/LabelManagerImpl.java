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

import com.wisemapping.model.MindmapLabel;
import com.wisemapping.model.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("labelManager")
public class LabelManagerImpl
        implements LabelManager {
    @Autowired
    private EntityManager entityManager;

    @Override
    public void addLabel(@NotNull final MindmapLabel label) {
        saveLabel(label);
    }

    @Override
    public void saveLabel(@NotNull final MindmapLabel label) {
        entityManager.persist(label);
    }

    @NotNull
    @Override
    public List<MindmapLabel> getAllLabels(@NotNull final Account user) {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<MindmapLabel> cq = cb.createQuery(MindmapLabel.class);
        final Root<MindmapLabel> root = cq.from(MindmapLabel.class);
        
        cq.select(root).where(cb.equal(root.get("creator"), user));
        
        return entityManager.createQuery(cq).getResultList();
    }

    @Nullable
    @Override
    public MindmapLabel getLabelById(int id, @NotNull final Account user) {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<MindmapLabel> cq = cb.createQuery(MindmapLabel.class);
        final Root<MindmapLabel> root = cq.from(MindmapLabel.class);
        
        final Predicate idPredicate = cb.equal(root.get("id"), id);
        final Predicate creatorPredicate = cb.equal(root.get("creator"), user);
        cq.select(root).where(cb.and(idPredicate, creatorPredicate));

        final List<MindmapLabel> resultList = entityManager.createQuery(cq).getResultList();
        return getFirst(resultList);
    }

    @Nullable
    @Override
    public MindmapLabel getLabelByTitle(@NotNull String title, @NotNull final Account user) {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<MindmapLabel> cq = cb.createQuery(MindmapLabel.class);
        final Root<MindmapLabel> root = cq.from(MindmapLabel.class);
        
        final Predicate titlePredicate = cb.equal(root.get("title"), title);
        final Predicate creatorPredicate = cb.equal(root.get("creator"), user);
        cq.select(root).where(cb.and(titlePredicate, creatorPredicate));
        
        return entityManager.createQuery(cq).getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public void removeLabel(@NotNull MindmapLabel label) {
        entityManager.remove(label);
    }

    @Nullable
    private MindmapLabel getFirst(final List<MindmapLabel> labels) {
        MindmapLabel result = null;
        if (labels != null && !labels.isEmpty()) {
            result = labels.get(0);
        }
        return result;
    }

}
