/*
*    Copyright [2011] [wisemapping]
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
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.Criteria;

import java.util.List;
import java.util.Calendar;

public class MindmapManagerImpl
        extends HibernateDaoSupport
        implements MindmapManager {

    @Override
    public Collaborator getCollaboratorBy(final String email) {
        final Collaborator collaborator;
        final List<Collaborator> collaborators = getHibernateTemplate().find("from com.wisemapping.model.Collaborator collaborator where email=?", email);
        if (collaborators != null && !collaborators.isEmpty()) {
            assert collaborators.size() == 1 : "More than one user with the same username!";
            collaborator = collaborators.get(0);
        } else {
            collaborator = null;
        }
        return collaborator;
    }

    @Override
    public List<MindMap> search(MindMapCriteria criteria) {
        return search(criteria, -1);
    }

    @Override
    public List<MindMapHistory> getHistoryFrom(int mindmapId) {
        final Criteria hibernateCriteria = getSession().createCriteria(MindMapHistory.class);
        hibernateCriteria.add(Restrictions.eq("mindmapId", mindmapId));
        hibernateCriteria.addOrder(Order.desc("creationTime"));
        // Mientras no haya paginacion solo los 10 primeros
        // This line throws errors in some environments, so getting all history and taking firsts 10 records
        // hibernateCriteria.setMaxResults(10);
        List list = hibernateCriteria.list();
        return list.subList(0, (10 < list.size() ? 10 : list.size()));
    }

    @Override
    public MindMapHistory getHistory(int historyId) {
        return getHibernateTemplate().get(MindMapHistory.class, historyId);
    }

    @Override
    public List<MindMap> search(MindMapCriteria criteria, int maxResult) {
        final Criteria hibernateCriteria = getSession().createCriteria(MindMap.class);
        //always search public maps
        hibernateCriteria.add(Restrictions.like("public", Boolean.TRUE));

        if (criteria != null) {
            final Junction junction;
            if (criteria.isOrCriteria()) {
                junction = Restrictions.disjunction();
            } else {
                junction = Restrictions.conjunction();
            }

            if (criteria.getTitle() != null && criteria.getTitle().length() > 0) {
                final SimpleExpression titleRestriction = Restrictions.like("title", "%" + criteria.getTitle() + "%");
                junction.add(titleRestriction);
            }

            if (criteria.getDescription() != null && criteria.getDescription().length() > 0) {
                final SimpleExpression descriptionRestriction = Restrictions.like("description", "%" + criteria.getDescription() + "%");
                junction.add(descriptionRestriction);
            }
            if (criteria.getTags().size() > 0) {
                for (String tag : criteria.getTags()) {
                    final SimpleExpression tagRestriction = Restrictions.like("tags", "%" + tag + "%");
                    junction.add(tagRestriction);
                }
            }

            hibernateCriteria.add(junction);
        }
//        if (maxResult>0)
//        {
//            hibernateCriteria.setMaxResults(maxResult);
//        }
        return hibernateCriteria.list();
    }

    @Override
    public Collaborator getCollaboratorBy(long id) {
        return getHibernateTemplate().get(Collaborator.class, id);
    }

    @Override
    public List<Collaboration> getMindmapUserByCollaborator(final long colaboratorId) {
        return getHibernateTemplate().find("from com.wisemapping.model.Collaboration mindmapUser where colaborator_id=?", colaboratorId);
    }

    @Override
    public List<Collaboration> getMindmapUserByRole(final CollaborationRole collaborationRole) {
        return getHibernateTemplate().find("from com.wisemapping.model.Collaboration mindmapUser where roleId=?", collaborationRole.ordinal());
    }

    @Override
    public Collaboration getMindmapUserBy(final int mindmapId, final User user) {
        final Collaboration result;

        final List<Collaboration> mindMaps = getHibernateTemplate().find("from com.wisemapping.model.Collaboration mindmapUser where mindMap.id=? and colaborator_id=?", new Object[]{mindmapId, user.getId()});
        if (mindMaps != null && !mindMaps.isEmpty()) {
            result = mindMaps.get(0);
        } else {
            result = null;
        }

        return result;
    }

    @Override
    public void addCollaborator(Collaborator collaborator) {
        assert collaborator != null : "ADD MINDMAP COLLABORATOR: Collaborator is required!";
        getHibernateTemplate().save(collaborator);
    }

    @Override
    public void removeCollaboration(Collaboration collaboration) {
        getHibernateTemplate().delete(collaboration);
    }

    @Override
    public void removeCollaborator(@NotNull Collaborator collaborator) {
        getHibernateTemplate().delete(collaborator);
    }

    @Override
    public List<MindMap> getAllMindmaps() {
        return getHibernateTemplate().find("from com.wisemapping.model.MindMap wisemapping");
    }

    @Override
    public MindMap getMindmapById(int mindmapId) {
        return getHibernateTemplate().get(MindMap.class, mindmapId);
    }

    @Override
    public MindMap getMindmapByTitle(final String title, final User user) {
        final MindMap result;
        List<MindMap> mindMaps = getHibernateTemplate().find("from com.wisemapping.model.MindMap wisemapping where title=? and creator=?", new Object[]{title, user});
        if (mindMaps != null && !mindMaps.isEmpty()) {
            result = mindMaps.get(0);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void addMindmap(User user, MindMap mindMap) {
        saveMindmap(mindMap);
    }

    @Override
    public void saveMindmap(MindMap mindMap) {
        assert mindMap != null : "Save Mindmap: Mindmap is required!";
        getSession().save(mindMap);
    }

    @Override
    public void updateMindmap(@NotNull MindMap mindMap, boolean saveHistory) {
        assert mindMap != null : "Save Mindmap: Mindmap is required!";
        getHibernateTemplate().saveOrUpdate(mindMap);
        if (saveHistory) {
            saveHistory(mindMap);
        }
    }

    @Override
    public void removeMindmap(MindMap mindMap) {
        getHibernateTemplate().delete(mindMap);
    }

    private void saveHistory(MindMap mindMap) {
        final MindMapHistory history = new MindMapHistory();

        history.setXml(mindMap.getXml());
        history.setCreationTime(Calendar.getInstance());
        history.setCreator(mindMap.getLastModifierUser());
        history.setMindmapId(mindMap.getId());
        getHibernateTemplate().saveOrUpdate(history);
    }
}

