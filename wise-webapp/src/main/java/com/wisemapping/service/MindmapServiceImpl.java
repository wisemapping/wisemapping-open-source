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

package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.*;
import com.wisemapping.security.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;


public class MindmapServiceImpl
        implements MindmapService {

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public boolean hasPermissions(@Nullable User user, int mapId, @NotNull CollaborationRole grantedRole) {
        final MindMap map = mindmapManager.getMindmapById(mapId);
        return hasPermissions(user, map, grantedRole);
    }

    @Override
    public boolean hasPermissions(@Nullable User user, @Nullable MindMap map, @NotNull CollaborationRole role) {
        boolean result = false;
        if (map != null) {
            if (map.isPublic() && role == CollaborationRole.VIEWER) {
                result = true;
            } else if (user != null) {
                final Collaboration collaboration = map.findCollaboration(user);
                if (collaboration != null) {
                    result = collaboration.hasPermissions(role);
                }

            }
        }
        return result;
    }

    @Override
    public MindMap getMindmapByTitle(String title, User user) {
        return mindmapManager.getMindmapByTitle(title, user);
    }

    @Override
    public MindMap findMindmapById(int mindmapId) {
        return mindmapManager.getMindmapById(mindmapId);
    }

    @Override
    public List<Collaboration> findCollaborations(@NotNull User user) {
        return mindmapManager.findCollaboration(user.getId());
    }

    @Override
    public void updateMindmap(@NotNull MindMap mindMap, boolean saveHistory) throws WiseMappingException {
        if (mindMap.getTitle() == null || mindMap.getTitle().length() == 0) {
            throw new WiseMappingException("The tile can not be empty");
        }
        mindmapManager.updateMindmap(mindMap, saveHistory);
    }

    @Override
    public List<MindMap> search(MindMapCriteria criteria) {
        return mindmapManager.search(criteria);
    }

    @Override
    public void removeCollaboration(@NotNull MindMap mindmap, @NotNull Collaboration collaboration) throws CollaborationException {
        // remove collaborator association
        final MindMap mindMap = collaboration.getMindMap();
        final Set<Collaboration> collaborations = mindMap.getCollaborations();

        if (mindMap.getCreator().getEmail().equals(collaboration.getCollaborator().getEmail())) {
            throw new CollaborationException("User is the creator and must have ownership permissions");
        }

        // When you delete an object from hibernate you have to delete it from *all* collections it exists in...
        mindmapManager.removeCollaboration(collaboration);
        collaborations.remove(collaboration);
    }

    @Override
    public void removeMindmap(@NotNull MindMap mindmap, @NotNull User user) throws WiseMappingException {
        if (mindmap.getCreator().equals(user)) {
            mindmapManager.removeMindmap(mindmap);
        } else {
            final Collaboration collaboration = mindmap.findCollaboration(user);
            if (collaboration != null) {
                this.removeCollaboration(mindmap, collaboration);
            }
        }
    }

    @Override
    public void addMindmap(@NotNull MindMap map, @NotNull User user) throws WiseMappingException {

        final String title = map.getTitle();

        if (title == null || title.length() == 0) {
            throw new IllegalArgumentException("The tile can not be empty");
        }

        //noinspection ConstantConditions
        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }

        final Calendar creationTime = Calendar.getInstance();
        final String username = user.getUsername();
        map.setLastModifierUser(username);
        map.setCreationTime(creationTime);
        map.setLastModificationTime(creationTime);
        map.setCreator(user);

        // Add map creator with owner permissions ...
        final User dbUser = userService.getUserBy(user.getId());
        final Collaboration collaboration = new Collaboration(CollaborationRole.OWNER, dbUser, map);
        map.getCollaborations().add(collaboration);

        mindmapManager.addMindmap(dbUser, map);
    }

    @Override
    public void addCollaboration(@NotNull MindMap mindmap, @NotNull String email, @NotNull CollaborationRole role, @Nullable String message)
            throws CollaborationException {

        // Validate
        final Collaborator owner = mindmap.getCreator();
        if (owner.getEmail().equals(email)) {
            throw new CollaborationException("The user " + owner.getEmail() + " is the owner");
        }

        if (role == CollaborationRole.OWNER) {
            throw new CollaborationException("Ownership can not be modified");

        }

        final Set<Collaboration> collaborations = mindmap.getCollaborations();
        Collaboration collaboration = getCollaborationBy(email, collaborations);
        if (collaboration == null) {
            final Collaborator collaborator = addCollaborator(email);
            collaboration = new Collaboration(role, collaborator, mindmap);
            mindmap.getCollaborations().add(collaboration);
            mindmapManager.saveMindmap(mindmap);

            // Notify by email ...
            final User user = Utils.getUser();
            notificationService.newCollaboration(collaboration, mindmap, user, message);

        } else if (collaboration.getRole() != role) {
            // If the relationship already exists and the role changed then only update the role
            collaboration.setRole(role);
            mindmapManager.updateMindmap(mindmap, false);
        }
    }

    private Collaborator addCollaborator(String email) {
        // Add a new collaborator ...
        Collaborator collaborator = mindmapManager.findCollaborator(email);
        if (collaborator == null) {
            collaborator = new Collaborator();
            collaborator.setEmail(email);
            collaborator.setCreationDate(Calendar.getInstance());
            mindmapManager.addCollaborator(collaborator);
        }
        return collaborator;
    }

    @Override
    public void addTags(@NotNull MindMap mindmap, String tags) {
        mindmap.setTags(tags);
        mindmapManager.updateMindmap(mindmap, false);
        if (tags != null && tags.length() > 0) {
            final String tag[] = tags.split(TAG_SEPARATOR);
            final User user = mindmap.getCreator();
            // Add new Tags to User
            boolean updateUser = false;
            for (String userTag : tag) {
                if (!user.getTags().contains(userTag)) {
                    user.getTags().add(userTag);
                    updateUser = true;
                }
            }
            if (updateUser) {
                //update user
                userService.updateUser(user);
            }
        }
    }

    @Override
    public void addWelcomeMindmap(User user) throws WiseMappingException {
        final MindMap savedWelcome = findMindmapById(Constants.WELCOME_MAP_ID);

        // Is there a welcomed map configured ?        
        if (savedWelcome != null) {
            final MindMap welcomeMap = new MindMap();
            welcomeMap.setTitle(savedWelcome.getTitle() + " " + user.getFirstname());
            welcomeMap.setDescription(savedWelcome.getDescription());
            welcomeMap.setXml(savedWelcome.getXml());

            addMindmap(welcomeMap, user);
        }
    }

    @Override
    public List<MindMapHistory> findMindmapHistory(int mindmapId) {
        return mindmapManager.getHistoryFrom(mindmapId);
    }

    @Override
    public void revertChange(@NotNull MindMap mindmap, int historyId)
            throws WiseMappingException {
        final MindMapHistory history = mindmapManager.getHistory(historyId);
        mindmap.setXml(history.getXml());
        updateMindmap(mindmap, true);
    }

    @Override
    public MindMapHistory findMindmapHistory(int id, int hid) throws WiseMappingException {
        final List<MindMapHistory> mindmapHistory = this.findMindmapHistory(id);
        MindMapHistory result = null;
        for (MindMapHistory history : mindmapHistory) {
            if (history.getId() == hid) {
                result = history;
                break;
            }
        }

        if (result == null) {
            throw new WiseMappingException("History could not be found for mapid=" + id + ",hid" + hid);
        }
        return result;
    }

    @Override
    public void updateCollaboration(@NotNull Collaborator collaborator, @NotNull Collaboration collaboration) throws WiseMappingException {
        if (collaborator.equals(collaboration.getCollaborator())) {
            throw new WiseMappingException("No enough permissions for this operation.");
        }
        mindmapManager.updateCollaboration(collaboration);
    }

    private Collaboration getCollaborationBy(String email, Set<Collaboration> collaborations) {
        Collaboration collaboration = null;

        for (Collaboration user : collaborations) {
            if (user.getCollaborator().getEmail().equals(email)) {
                collaboration = user;
                break;
            }
        }
        return collaboration;
    }


    public void setMindmapManager(MindmapManager mindmapManager) {
        this.mindmapManager = mindmapManager;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
