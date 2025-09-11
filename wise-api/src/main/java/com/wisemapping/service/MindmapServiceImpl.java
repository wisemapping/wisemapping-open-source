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

package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.*;
import com.wisemapping.security.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service("mindmapService")
@Transactional(propagation = Propagation.REQUIRED)
public class MindmapServiceImpl

        implements MindmapService {

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MetricsService metricsService;


    @Value("${app.admin.user}")
    private String adminUser;
    final private LockManager lockManager;

    public MindmapServiceImpl() {
        this.lockManager = new LockManagerImpl();
    }

    @Override
    public boolean hasPermissions(@Nullable Account user, int mapId, @NotNull CollaborationRole grantedRole) {
        final Mindmap map = mindmapManager.getMindmapById(mapId);
        return hasPermissions(user, map, grantedRole);
    }

    @Override
    public boolean isMindmapPublic(int mapId) {
        final Mindmap map = mindmapManager.getMindmapById(mapId);
        return map != null && map.isPublic();
    }

    @Override
    public boolean hasPermissions(@Nullable Account user, @Nullable Mindmap map, @NotNull CollaborationRole role) {
        boolean result = false;
        if (map != null) {
            // Admin always has access
            if (isAdmin(user)) {
                result = true;
            } else if (user != null) {
                // Check if user is the creator/owner
                if (map.isCreator(user)) {
                    result = true;
                } else {
                    // Check collaboration permissions first
                    final Optional<Collaboration> collaboration = map.findCollaboration(user);
                    if (collaboration.isPresent()) {
                        result = collaboration.get().hasPermissions(role);
                    }
                }
            }

            // In case, users should have access to public maps.
            if (!result) {
                result = map.isPublic() && role == CollaborationRole.VIEWER;
            }
        }
        return result;
    }

    public boolean isAdmin(@Nullable Account user) {
        return user != null && user.getEmail() != null && user.getEmail().equals(adminUser);
    }

    @Override
    @PreAuthorize("hasPermission(#user, 'READ')")
    public Mindmap getMindmapByTitle(String title, Account user) {
        return mindmapManager.getMindmapByTitle(title, user);
    }

    @Override
    @Nullable
    @PreAuthorize("hasPermission(#mapId, 'READ')")
    public Mindmap findMindmapById(int mapId) {
        return mindmapManager.getMindmapById(mapId);
    }

    @NotNull
    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    public List<Mindmap> findMindmapsByUser(@NotNull Account user) {
        return mindmapManager.findMindmapByUser(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    public List<Collaboration> findCollaborations(@NotNull Account user) {
        return mindmapManager.findCollaboration(user.getId());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    public void updateMindmap(@NotNull Mindmap mindmap, boolean saveHistory) throws WiseMappingException {
        if (mindmap.getTitle() == null || mindmap.getTitle().length() == 0) {
            throw new WiseMappingException("The title can not be empty");
        }

        // Check that what we received a valid mindmap...
        final String xml;
        try {
            xml = mindmap.getXmlStr().trim();
        } catch (UnsupportedEncodingException e) {
            throw new WiseMappingException("Could not be decoded.", e);
        }

        if (!xml.endsWith("</map>")) {
            throw new WiseMappingException("Map seems not to be a valid mindmap: '" + xml + "'");
        }

        mindmapManager.updateMindmap(mindmap, saveHistory);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    public void removeCollaboration(@NotNull Mindmap mindmap, @NotNull Collaboration collaboration) throws CollaborationException {
        // remove collaborator association
        final Mindmap mindMap = collaboration.getMindMap();
        final Account creator = mindMap.getCreator();
        if (creator.identityEquality(collaboration.getCollaborator())) {
            throw new CollaborationException("User is the creator and must have ownership permissions.Creator Email:" + mindMap.getCreator().getEmail() + ",Collaborator:" + collaboration.getCollaborator().getEmail());
        }

        // When you delete an object from hibernate you have to delete it from *all* collections it exists in...
        mindMap.removedCollaboration(collaboration);
        mindmapManager.removeCollaboration(collaboration);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'READ')")
    public void removeMindmap(@NotNull Mindmap mindmap, @NotNull Account user) throws WiseMappingException {
        if (mindmap.getCreator().identityEquality(user)) {
            mindmapManager.removeMindmap(mindmap);
        } else {
            final Optional<Collaboration> collaboration = mindmap.findCollaboration(user);
            if (collaboration.isPresent()) {
                this.removeCollaboration(mindmap, collaboration.get());
            }
        }
    }

    @Override
    @PreAuthorize("hasPermission(#mindmap, 'WRITE')")
    public void addMindmap(@NotNull Mindmap mindmap, @NotNull Account user) {

        final String title = mindmap.getTitle();

        if (title == null || title.length() == 0) {
            throw new IllegalArgumentException("The tile can not be empty");
        }

        //noinspection ConstantConditions
        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }

        final Calendar creationTime = Calendar.getInstance();
        mindmap.setLastEditor(user);
        mindmap.setCreationTime(creationTime);
        mindmap.setLastModificationTime(creationTime);
        mindmap.setCreator(user);

        // Add map creator with owner permissions ...
        final Account dbUser = userService.getUserBy(user.getId());
        final Collaboration collaboration = new Collaboration(CollaborationRole.OWNER, dbUser, mindmap);
        mindmap.getCollaborations().add(collaboration);

        mindmapManager.addMindmap(dbUser, mindmap);

        // Track mindmap creation with enhanced metrics
        metricsService.trackMindmapCreation(mindmap, dbUser, "new");
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    public void addCollaboration(@NotNull Mindmap mindmap, @NotNull String email, @NotNull CollaborationRole role, @Nullable String message)
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
            final Account user = Utils.getUser();
            notificationService.newCollaboration(collaboration, mindmap, user, message);

        } else if (collaboration.getRole() != role) {
            // If the relationship already exists and the role changed then only update the role
            collaboration.setRole(role);
            mindmapManager.updateMindmap(mindmap, false);
        }
    }


    private Collaborator addCollaborator(@NotNull String email) {
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mapId, 'READ')")
    public List<MindMapHistory> findMindmapHistory(int mapId) {
        return mindmapManager.getHistoryFrom(mapId);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    public void revertChange(@NotNull Mindmap mindmap, int historyId)
            throws WiseMappingException {
        final MindMapHistory history = mindmapManager.getHistory(historyId);
        mindmap.setZippedXml(history.getZippedXml());
        updateMindmap(mindmap, true);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mapId, 'READ')")
    public MindMapHistory findMindmapHistory(int mapId, int hid) throws WiseMappingException {
        final List<MindMapHistory> mindmapHistory = this.findMindmapHistory(mapId);
        MindMapHistory result = null;
        for (MindMapHistory history : mindmapHistory) {
            if (history.getId() == hid) {
                result = history;
                break;
            }
        }

        if (result == null) {
            throw new WiseMappingException("History could not be found for mapid=" + mapId + ",hid" + hid);
        }
        return result;
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#collaborator, 'WRITE')")
    public void updateCollaboration(@NotNull Collaborator collaborator, @NotNull Collaboration collaboration) throws WiseMappingException {
        if (!collaborator.identityEquality(collaboration.getCollaborator())) {
            throw new WiseMappingException("No enough permissions for this operation.");
        }
        mindmapManager.updateCollaboration(collaboration);
    }

    @Override
    @NotNull
    public LockManager getLockManager() {
        return this.lockManager;
    }

    private Collaboration getCollaborationBy(@NotNull final String email, @NotNull final Set<Collaboration> collaborations) {
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

    public void setAdminUser(@NotNull String adminUser) {
        this.adminUser = adminUser;
    }

    @NotNull
    public String getAdminUser() {
        return adminUser;
    }
}
