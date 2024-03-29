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
        final TypedQuery<Collaborator> query = entityManager.createQuery("from com.wisemapping.model.Collaborator collaborator where email=:email", Collaborator.class);
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
        final TypedQuery<Collaboration> query = entityManager.createQuery("from com.wisemapping.model.Collaboration c where c.collaborator.id=:collaboratorId", Collaboration.class);
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

        final TypedQuery<Mindmap> query = entityManager.createQuery("from com.wisemapping.model.Mindmap wisemapping where title=:title and creator=:creator", Mindmap.class);
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

    private void saveHistory(@NotNull final Mindmap mindMap) {
        final MindMapHistory history = new MindMapHistory();

        history.setZippedXml(mindMap.getZippedXml());
        history.setCreationTime(Calendar.getInstance());
        history.setEditor(mindMap.getLastEditor());
        history.setMindmapId(mindMap.getId());
        entityManager.merge(history);
    }
}
