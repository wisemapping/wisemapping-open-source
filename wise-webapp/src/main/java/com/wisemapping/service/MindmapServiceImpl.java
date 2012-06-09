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
import com.wisemapping.mail.Mailer;
import com.wisemapping.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;


public class MindmapServiceImpl
        implements MindmapService {

    private MindmapManager mindmapManager;
    private UserService userService;
    private Mailer mailer;

    public boolean isAllowedToCollaborate(@NotNull User user, int mapId, @NotNull CollaborationRole grantedRole) {
        final MindMap map = mindmapManager.getMindmapById(mapId);
        return isAllowedToCollaborate(user, map, grantedRole);
    }

    public boolean isAllowedToView(User user, int mapId, CollaborationRole grantedRole) {
        final MindMap map = mindmapManager.getMindmapById(mapId);
        return isAllowedToView(user, map, grantedRole);
    }

    public boolean isAllowedToView(@NotNull User user, @NotNull MindMap map, @NotNull CollaborationRole grantedRole) {
        boolean result = false;
        if (map != null) {
            if (map.isPublic()) {
                result = true;
            } else if (user != null) {
                result = isAllowedToCollaborate(user, map, grantedRole);
            }
        }
        return result;
    }

    public boolean isAllowedToCollaborate(@NotNull User user, @Nullable MindMap map, CollaborationRole grantedRole) {
        boolean isAllowed = false;
        if (map != null) {
            if (map.getOwner().getId() == user.getId()) {
                isAllowed = true;
            } else {
                final Set<Collaboration> users = map.getCollaborations();
                CollaborationRole rol = null;
                for (Collaboration collaboration : users) {
                    if (collaboration.getCollaborator().getId() == user.getId()) {
                        rol = collaboration.getRole();
                        break;
                    }
                }
                // only if the user has a role for the current map
                isAllowed = rol != null &&
                        (grantedRole.equals(rol) || rol.ordinal() < grantedRole.ordinal());
            }
        }
        return isAllowed;
    }

    public Collaboration getMindmapUserBy(int mindmapId, User user) {
        return mindmapManager.getMindmapUserBy(mindmapId, user);
    }

    public MindMap getMindmapByTitle(String title, User user) {
        return mindmapManager.getMindmapByTitle(title, user);
    }

    public MindMap getMindmapById(int mindmapId) {
        return mindmapManager.getMindmapById(mindmapId);
    }

    public List<Collaboration> getMindmapUserByUser(@NotNull User user) {
        return mindmapManager.getMindmapUserByCollaborator(user.getId());
    }

    public void updateMindmap(MindMap mindMap, boolean saveHistory) throws WiseMappingException {
        if (mindMap.getTitle() == null || mindMap.getTitle().length() == 0) {
            throw new WiseMappingException("The tile can not be empty");
        }

        mindmapManager.updateMindmap(mindMap, saveHistory);
    }

    public List<MindMap> getPublicMaps(int cant) {
        return mindmapManager.search(null, cant);
    }

    public List<MindMap> search(MindMapCriteria criteria) {
        return mindmapManager.search(criteria);
    }

    public void removeCollaboratorFromMindmap(@NotNull MindMap mindmap, long userId) {
        // remove collaborator association
        Set<Collaboration> mindmapusers = mindmap.getCollaborations();
        Collaboration mindmapuserToDelete = null;
        for (Collaboration mindmapuser : mindmapusers) {
            if (mindmapuser.getCollaborator().getId() == userId) {
                mindmapuserToDelete = mindmapuser;
                break;
            }
        }
        if (mindmapuserToDelete != null) {
            // When you delete an object from hibernate you have to delete it from *all* collections it exists in...
            mindmapusers.remove(mindmapuserToDelete);
            mindmapManager.removeMindmapUser(mindmapuserToDelete);
        }
    }

    public void removeMindmap(@NotNull MindMap mindmap, @NotNull User user) throws WiseMappingException {
        if (mindmap.getOwner().equals(user)) {
            mindmapManager.removeMindmap(mindmap);
        } else {
            this.removeCollaboratorFromMindmap(mindmap, user.getId());
        }
    }

    public void addMindmap(@NotNull MindMap map, @NotNull User user) throws WiseMappingException {

        final String title = map.getTitle();

        if (title == null || title.length() == 0) {
            throw new IllegalArgumentException("The tile can not be empty");
        }

        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }

        final Calendar creationTime = Calendar.getInstance();
        final String username = user.getUsername();
        map.setCreator(username);
        map.setLastModifierUser(username);
        map.setCreationTime(creationTime);
        map.setLastModificationTime(creationTime);
        map.setOwner(user);

        // Hack to reload dbuser ...
        final User dbUser = userService.getUserBy(user.getId());
        final Collaboration collaboration = new Collaboration(CollaborationRole.OWNER, dbUser, map);
        map.getCollaborations().add(collaboration);

        mindmapManager.addMindmap(dbUser, map);
    }

    public void addCollaborators(MindMap mindmap, String[] collaboratorEmails, CollaborationRole role, ColaborationEmail email)
            throws InvalidColaboratorException {
        if (collaboratorEmails != null && collaboratorEmails.length > 0) {
            final Collaborator owner = mindmap.getOwner();
            final Set<Collaboration> collaborations = mindmap.getCollaborations();

            for (String colaboratorEmail : collaboratorEmails) {
                if (owner.getEmail().equals(colaboratorEmail)) {
                    throw new InvalidColaboratorException("The user " + owner.getEmail() + " is the owner");
                }
                Collaboration collaboration = getMindmapUserBy(colaboratorEmail, collaborations);
                if (collaboration == null) {
                    addCollaborator(colaboratorEmail, role, mindmap, email);
                } else if (collaboration.getRole() != role) {
                    // If the relationship already exists and the role changed then only update the role
                    collaboration.setRoleId(role.ordinal());
                    mindmapManager.updateMindmap(mindmap, false);
                }
            }
        }
    }

    public void addTags(MindMap mindmap, String tags) {
        mindmap.setTags(tags);
        mindmapManager.updateMindmap(mindmap, false);
        if (tags != null && tags.length() > 0) {
            final String tag[] = tags.split(TAG_SEPARATOR);
            final User user = mindmap.getOwner();
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

    public void addWelcomeMindmap(User user) throws WiseMappingException {
        final MindMap savedWelcome = getMindmapById(Constants.WELCOME_MAP_ID);

        // Is there a welcomed map configured ?        
        if (savedWelcome != null) {
            final MindMap welcomeMap = new MindMap();
            welcomeMap.setTitle(savedWelcome.getTitle() + " " + user.getFirstname());
            welcomeMap.setDescription(savedWelcome.getDescription());
            welcomeMap.setXml(savedWelcome.getXml());

            addMindmap(welcomeMap, user);
        }
    }

    public List<MindMapHistory> getMindMapHistory(int mindmapId) {
        return mindmapManager.getHistoryFrom(mindmapId);
    }

    public void revertMapToHistory(MindMap map, int historyId)
            throws IOException, WiseMappingException {
        final MindMapHistory history = mindmapManager.getHistory(historyId);
        map.setXml(history.getXml());
        updateMindmap(map, false);
    }

    private Collaboration getMindmapUserBy(String email, Set<Collaboration> collaborations) {
        Collaboration collaboration = null;

        for (Collaboration user : collaborations) {
            if (user.getCollaborator().getEmail().equals(email)) {
                collaboration = user;
                break;
            }
        }
        return collaboration;
    }

    private void addCollaborator(String colaboratorEmail, CollaborationRole role, MindMap mindmap, ColaborationEmail email) {

        Collaborator collaborator = mindmapManager.getCollaboratorBy(colaboratorEmail);
        if (collaborator == null) {
            collaborator = new Collaborator();
            collaborator.setEmail(colaboratorEmail);
            collaborator.setCreationDate(Calendar.getInstance());
            mindmapManager.addCollaborator(collaborator);
        }

        final Collaboration newCollaboration = new Collaboration(role, collaborator, mindmap);
        mindmap.getCollaborations().add(newCollaboration);

        mindmapManager.saveMindmap(mindmap);

        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("role", role);
        model.put("map", mindmap);
        model.put("message", email.getMessage());
        mailer.sendEmail(mailer.getSiteEmail(), colaboratorEmail, email.getSubject(), model, "newColaborator.vm");
    }

    public void setMindmapManager(MindmapManager mindmapManager) {
        this.mindmapManager = mindmapManager;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }
}
