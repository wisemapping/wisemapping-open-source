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

import java.io.IOException;
import java.util.*;


public class MindmapServiceImpl
        implements MindmapService {

    private MindmapManager mindmapManager;
    private UserService userService;
    private Mailer mailer;

    public boolean isAllowedToColaborate(User user, int mapId, UserRole grantedRole) {
        final MindMap map = mindmapManager.getMindmapById(mapId);
        return isAllowedToColaborate(user, map, grantedRole);
    }

    public boolean isAllowedToView(User user, int mapId, UserRole grantedRole) {
        final MindMap map = mindmapManager.getMindmapById(mapId);
        return isAllowedToView(user, map, grantedRole);
    }

    public boolean isAllowedToView(User user, MindMap map, UserRole grantedRole) {
        boolean isAllowed = false;
        if (map != null) {

            if (map.isPublic()) {
                isAllowed = true;
            } else if (user != null) {
                isAllowed = isAllowedToColaborate(user, map, grantedRole);
            }
        }
        return isAllowed;
    }

    public boolean isAllowedToColaborate(User user, MindMap map, UserRole grantedRole) {
        boolean isAllowed = false;
        if (map != null) {
            if (map.getOwner().getId() == user.getId()) {
                isAllowed = true;
            } else {
                final Set<MindmapUser> users = map.getMindmapUsers();
                UserRole rol = null;
                for (MindmapUser mindmapUser : users) {
                    if (mindmapUser.getColaborator().getId() == user.getId()) {
                        rol = mindmapUser.getRole();
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

    public MindmapUser getMindmapUserBy(int mindmapId, User user) {
        return mindmapManager.getMindmapUserBy(mindmapId, user);
    }

    public MindMap getMindmapByTitle(String title, User user) {
        return mindmapManager.getMindmapByTitle(title, user);
    }

    public MindMap getMindmapById(int mindmapId) {
        return mindmapManager.getMindmapById(mindmapId);
    }

    public List<MindmapUser> getMindmapUserByUser(User user) {
        return mindmapManager.getMindmapUserByColaborator(user.getId());
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

    public void removeColaboratorFromMindmap(MindMap mindmap, long colaboratorId) {
        // remove colaborator association
        Set<MindmapUser> mindmapusers = mindmap.getMindmapUsers();
        MindmapUser mindmapuserToDelete = null;
        for (MindmapUser mindmapuser : mindmapusers) {
            if (mindmapuser.getColaborator().getId() == colaboratorId) {
                mindmapuserToDelete = mindmapuser;
                //@TODO evaluar si el colaborador no tiene mas asociaciones si hay que eliminarlo, por ahora NO
//                final List<MindmapUser> otherAsociations = mindmapManager.getMindmapUserByColaborator(colaboratorId);
//                if (otherAsociations != null)
//                {
//
//                    final User user = userService.getUserBy(colaboratorId);
//                    // Is not a User
//                    if (user == null)
//                    {
//                        final Colaborator col = mindmapManager.getColaboratorBy(colaboratorId);
//                        mindmapManager.removeColaborator(col);
//                    }
//                }
                break;
            }
        }
        if (mindmapuserToDelete != null) {
            // When you delete an object from hibernate you have to delete it from *all* collections it exists in...
            mindmapusers.remove(mindmapuserToDelete);
            mindmapManager.removeMindmapUser(mindmapuserToDelete);
        }
    }

    public void removeMindmap(MindMap mindmap, User user) throws WiseMappingException {
        if (mindmap.getOwner().equals(user)) {

            mindmapManager.removeMindmap(mindmap);
        } else {
            this.removeColaboratorFromMindmap(mindmap, user.getId());
        }
    }

    public void addMindmap(MindMap map, User user) throws WiseMappingException {

        final String title = map.getTitle();

        if (title == null || title.length() == 0) {
            throw new IllegalArgumentException("The tile can not be empty");
        }

        if (user==null) {
            throw new IllegalArgumentException("User can not be null");
        }

        final Calendar creationTime = Calendar.getInstance();
        final String username = user.getUsername();
        map.setCreator(username);
        map.setLastModifierUser(username);
        map.setCreationTime(creationTime);
        map.setLastModificationTime(creationTime);
        map.setOwner(user);

        final MindmapUser mindmapUser = new MindmapUser(UserRole.OWNER.ordinal(), user, map);
        map.getMindmapUsers().add(mindmapUser);

        mindmapManager.addMindmap(user, map);
    }

    public void addColaborators(MindMap mindmap, String[] colaboratorEmails, UserRole role, ColaborationEmail email)
            throws InvalidColaboratorException {
        if (colaboratorEmails != null && colaboratorEmails.length > 0) {
            final Colaborator owner = mindmap.getOwner();
            final Set<MindmapUser> mindmapUsers = mindmap.getMindmapUsers();

            for (String colaboratorEmail : colaboratorEmails) {
                if (owner.getEmail().equals(colaboratorEmail)) {
                    throw new InvalidColaboratorException("The user " + owner.getEmail() + " is the owner");
                }
                MindmapUser mindmapUser = getMindmapUserBy(colaboratorEmail, mindmapUsers);
                if (mindmapUser == null) {
                    addColaborator(colaboratorEmail, role, mindmap, email);
                } else if (mindmapUser.getRole() != role) {
                    // If the relationship already exists and the role changed then only update the role
                    mindmapUser.setRoleId(role.ordinal());
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
        if(savedWelcome!=null)
        {
            final MindMap welcomeMap = new MindMap();
            welcomeMap.setTitle(savedWelcome.getTitle() + " " + user.getFirstname());
            welcomeMap.setDescription(savedWelcome.getDescription());
            welcomeMap.setXml(savedWelcome.getXml());

            final MindMapNative mindmapNative = new MindMapNative();
            mindmapNative.setSvgXml(savedWelcome.getNativeBrowser().getSvgXml());
            mindmapNative.setVmlXml(savedWelcome.getNativeBrowser().getVmlXml());

            welcomeMap.setNativeBrowser(mindmapNative);

            addMindmap(welcomeMap, user);
        }
    }

    public void addView(int mapId) {
        mindmapManager.addView(mapId);
    }

    public List<MindMapHistory> getMindMapHistory(int mindmapId) {
        return mindmapManager.getHistoryFrom(mindmapId);
    }

    public void revertMapToHistory(MindMap map, int historyId)
            throws IOException, WiseMappingException {
        final MindMapHistory history = mindmapManager.getHistory(historyId);
        map.setNativeXml(history.getNativeXml());
        updateMindmap(map, false);
    }

    private MindmapUser getMindmapUserBy(String email, Set<MindmapUser> mindmapUsers) {
        MindmapUser mindmapUser = null;

        for (MindmapUser user : mindmapUsers) {
            if (user.getColaborator().getEmail().equals(email)) {
                mindmapUser = user;
                break;
            }
        }
        return mindmapUser;
    }

    private void addColaborator(String colaboratorEmail, UserRole role, MindMap mindmap, ColaborationEmail email) {

        Colaborator colaborator = mindmapManager.getColaboratorBy(colaboratorEmail);
        if (colaborator == null) {
            colaborator = new Colaborator();
            colaborator.setEmail(colaboratorEmail);
            colaborator.setCreationDate(Calendar.getInstance());
            mindmapManager.addColaborator(colaborator);
        }

        final MindmapUser newMindmapUser = new MindmapUser(role.ordinal(), colaborator, mindmap);
        mindmap.getMindmapUsers().add(newMindmapUser);

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
