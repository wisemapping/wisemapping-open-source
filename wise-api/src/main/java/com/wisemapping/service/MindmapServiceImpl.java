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

package com.wisemapping.service;

import com.newrelic.api.agent.Trace;
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

    @Value("${app.admin.user}")
    private String adminUser;
    final private LockManager lockManager;

    public MindmapServiceImpl() {
        this.lockManager = new LockManagerImpl();
    }

    @Override
    @Trace
    public boolean hasPermissions(@Nullable Account user, int mapId, @NotNull CollaborationRole grantedRole) {
        final Mindmap map = mindmapManager.getMindmapById(mapId);
        return hasPermissions(user, map, grantedRole);
    }

    @Override
    @Trace
    public boolean isMindmapPublic(int mapId) {
        final Mindmap map = mindmapManager.getMindmapById(mapId);
        return map != null && map.isPublic();
    }

    @Override
    @Trace
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

    @Trace
    public boolean isAdmin(@Nullable Account user) {
        return user != null && user.getEmail() != null && user.getEmail().trim().endsWith(adminUser);
    }

    @Override
    @Trace
    public List<Mindmap> getAllMindmaps() {
        return mindmapManager.getAllMindmaps();
    }

    @Override
    @PreAuthorize("hasPermission(#user, 'READ')")
    @Trace
    public Mindmap getMindmapByTitle(String title, Account user) {
        return mindmapManager.getMindmapByTitle(title, user);
    }

    @Override
    @Nullable
    @PreAuthorize("hasPermission(#mapId, 'READ')")
    @Trace
    public Mindmap findMindmapById(int mapId) {
        return mindmapManager.getMindmapById(mapId);
    }

    @NotNull
    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    @Trace
    public List<Mindmap> findMindmapsByUser(@NotNull Account user) {
        return mindmapManager.findMindmapByUser(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    @Trace
    public List<Collaboration> findCollaborations(@NotNull Account user) {
        return mindmapManager.findCollaboration(user.getId());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    @Trace
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
    @Trace
    public void removeCollaboration(@NotNull Mindmap mindmap, @NotNull Collaboration collaboration) throws CollaborationException {
        // remove collaborator association
        final Mindmap mindMap = collaboration.getMindMap();
        final Account creator = mindMap.getCreator();
        if (creator.identityEquality(collaboration.getCollaborator())) {
            throw new CollaborationException("User is the creator and must have ownership permissions.Creator Email:" + mindMap.getCreator().getEmail() + ",Collaborator:" + collaboration.getCollaborator().getEmail());
        }

        // Check if collaboration still exists in the mindmap's collection
        // It might have been already removed by a bulk delete operation
        if (!mindMap.getCollaborations().contains(collaboration)) {
            // Collaboration already removed, nothing to do
            return;
        }

        // When you delete an object from hibernate you have to delete it from *all* collections it exists in...
        mindMap.removedCollaboration(collaboration);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'READ')")
    @Trace
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
    @Trace
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
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    @Trace
    public void addCollaboration(@NotNull Mindmap mindmap, @NotNull String email, @NotNull CollaborationRole role, @Nullable String message)
            throws CollaborationException {

        // Validate input
        validateCollaborationRequest(mindmap, email, role);

        // Get or create collaborator
        final Collaborator collaborator = addCollaborator(email);
        
        // Check if collaboration already exists BEFORE creating it
        final Collaboration existingCollaboration = mindmapManager.findCollaboration(mindmap.getId(), collaborator.getId());
        final boolean isNewCollaboration = (existingCollaboration == null);
        
        // Use DAO's find-or-create pattern to prevent constraint violations
        Collaboration collaboration = mindmapManager.findOrCreateCollaboration(mindmap, collaborator, role);
        
        // Send notification only for new collaborations
        if (isNewCollaboration) {
            final Account user = Utils.getUser();
            notificationService.newCollaboration(collaboration, mindmap, user, message);
        }
    }

    private void validateCollaborationRequest(@NotNull Mindmap mindmap, @NotNull String email, @NotNull CollaborationRole role) 
            throws CollaborationException {
        final Collaborator owner = mindmap.getCreator();
        if (owner.getEmail().equals(email)) {
            throw new CollaborationException("The user " + owner.getEmail() + " is the owner");
        }

        if (role == CollaborationRole.OWNER) {
            throw new CollaborationException("Ownership can not be modified");
        }
    }



    private Collaborator addCollaborator(@NotNull String email) {
        // Find existing collaborator or create new one
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
    @Trace
    public List<MindMapHistory> findMindmapHistory(int mapId) {
        return mindmapManager.getHistoryFrom(mapId);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mindmap, 'WRITE')")
    @Trace
    public void revertChange(@NotNull Mindmap mindmap, int historyId)
            throws WiseMappingException {
        final MindMapHistory history = mindmapManager.getHistory(historyId);
        mindmap.setZippedXml(history.getZippedXml());
        updateMindmap(mindmap, true);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#mapId, 'READ')")
    @Trace
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
    @Trace
    public void updateCollaboration(@NotNull Collaborator collaborator, @NotNull Collaboration collaboration) throws WiseMappingException {
        if (!collaborator.identityEquality(collaboration.getCollaborator())) {
            throw new WiseMappingException("No enough permissions for this operation.");
        }
        mindmapManager.updateCollaboration(collaboration);
    }

    @Override
    @NotNull
    @Trace
    public LockManager getLockManager() {
        return this.lockManager;
    }



    @Trace
    public void setMindmapManager(MindmapManager mindmapManager) {
        this.mindmapManager = mindmapManager;
    }

    @Trace
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Trace
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Trace
    public void setAdminUser(@NotNull String adminUser) {
        this.adminUser = adminUser;
    }

    @NotNull
    @Trace
    public String getAdminUser() {
        return adminUser;
    }

    @Override
    @Trace
    public List<Mindmap> getAllMindmaps(int page, int pageSize) {
        int offset = page * pageSize;
        return mindmapManager.getAllMindmaps(offset, pageSize);
    }

    @Override
    @Trace
    public long countAllMindmaps() {
        return mindmapManager.countAllMindmaps();
    }

    @Override
    @Trace
    public List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, int page, int pageSize) {
        int offset = page * pageSize;
        return mindmapManager.searchMindmaps(search, filterPublic, filterLocked, offset, pageSize);
    }

    @Override
    @Trace
    public long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked) {
        return mindmapManager.countMindmapsBySearch(search, filterPublic, filterLocked);
    }

    @Override
    @Trace
    public List<Mindmap> getAllMindmaps(Boolean filterSpam, int page, int pageSize) {
        int offset = page * pageSize;
        return mindmapManager.getAllMindmaps(filterSpam, offset, pageSize);
    }

    @Override
    @Trace
    public List<Mindmap> getAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter, int page, int pageSize) {
        int offset = page * pageSize;
        return mindmapManager.getAllMindmaps(filterPublic, filterLocked, filterSpam, dateFilter, offset, pageSize);
    }

    @Override
    @Trace
    public long countAllMindmaps(Boolean filterSpam) {
        return mindmapManager.countAllMindmaps(filterSpam);
    }

    @Override
    @Trace
    public long countAllMindmaps(Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, String dateFilter) {
        return mindmapManager.countAllMindmaps(filterPublic, filterLocked, filterSpam, dateFilter);
    }

    @Override
    @Trace
    public List<Mindmap> searchMindmaps(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam, int page, int pageSize) {
        int offset = page * pageSize;
        return mindmapManager.searchMindmaps(search, filterPublic, filterLocked, filterSpam, offset, pageSize);
    }

    @Override
    @Trace
    public long countMindmapsBySearch(String search, Boolean filterPublic, Boolean filterLocked, Boolean filterSpam) {
        return mindmapManager.countMindmapsBySearch(search, filterPublic, filterLocked, filterSpam);
    }
}
