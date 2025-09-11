package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import com.wisemapping.model.*;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.MindmapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MindmapServiceSpamAccessTest {

    @Mock
    private MindmapManager mindmapManager;

    @Mock
    private UserService userService;

    private MindmapService mindmapService;

    private Account owner;
    private Account nonOwner;
    private Mindmap publicMap;
    private Mindmap spamMap;

    @BeforeEach
    void setUp() {
        mindmapService = new MindmapServiceImpl();
        // Use reflection to inject the mocked dependencies
        try {
            java.lang.reflect.Field mindmapManagerField = MindmapServiceImpl.class.getDeclaredField("mindmapManager");
            mindmapManagerField.setAccessible(true);
            mindmapManagerField.set(mindmapService, mindmapManager);

            java.lang.reflect.Field userServiceField = MindmapServiceImpl.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(mindmapService, userService);

            java.lang.reflect.Field adminUserField = MindmapServiceImpl.class.getDeclaredField("adminUser");
            adminUserField.setAccessible(true);
            adminUserField.set(mindmapService, "admin@wisemapping.org");
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // Create test accounts
        owner = new Account();
        owner.setId(1);
        owner.setEmail("owner@example.com");

        nonOwner = new Account();
        nonOwner.setId(2);
        nonOwner.setEmail("nonowner@example.com");

        // Create a normal public map
        publicMap = new Mindmap();
        publicMap.setId(1);
        publicMap.setPublic(true);
        publicMap.setCreator(owner);
        publicMap.setSpamInfo(null); // Not marked as spam

        // Create a spam-marked public map
        spamMap = new Mindmap();
        spamMap.setId(2);
        spamMap.setPublic(true);
        spamMap.setCreator(owner);
        MindmapSpamInfo spamInfo = new MindmapSpamInfo(spamMap);
        spamInfo.setSpamDetected(true);
        spamInfo.setSpamDescription("Test spam content");
        spamMap.setSpamInfo(spamInfo);
    }

    @Test
    public void testHasPermissions_PublicMap_NonSpam_AllowsAccess() {
        // Test that non-owners can access normal public maps
        boolean result = mindmapService.hasPermissions(nonOwner, publicMap, CollaborationRole.VIEWER);
        assertTrue(result, "Non-owners should be able to access normal public maps");
    }

    @Test
    public void testHasPermissions_PublicMap_Spam_AllowsAuthenticatedUsers() {
        // Test that authenticated non-owners can still access spam-marked public maps
        // Spam detection prevents publication but doesn't block authenticated access to already-public maps
        boolean result = mindmapService.hasPermissions(nonOwner, spamMap, CollaborationRole.VIEWER);
        assertTrue(result, "Authenticated users should be able to access public maps even if marked as spam");
    }

    @Test
    public void testHasPermissions_PublicMap_Spam_ServiceLayerAllowsAccess() {
        // Service layer should allow access to public maps regardless of spam status
        // Spam control is handled at the controller layer, not service layer
        boolean result = mindmapService.hasPermissions(null, spamMap, CollaborationRole.VIEWER);
        assertTrue(result, "Service layer should allow access to public maps - spam control is at controller level");
    }

    @Test
    public void testHasPermissions_PublicMap_NonSpam_AllowsUnauthenticatedAccess() {
        // Test that unauthenticated users can access normal (non-spam) public maps
        boolean result = mindmapService.hasPermissions(null, publicMap, CollaborationRole.VIEWER);
        assertTrue(result, "Unauthenticated users should be able to access normal public maps");
    }

    @Test
    public void testHasPermissions_PublicMap_Spam_AllowsOwner() {
        // Test that owners can still access their own spam-marked public maps
        boolean result = mindmapService.hasPermissions(owner, spamMap, CollaborationRole.VIEWER);
        assertTrue(result, "Owners should be able to access their own spam-marked public maps");
    }

    @Test
    public void testHasPermissions_PublicMap_Spam_AllowsAdmin() {
        // Create admin user
        Account admin = new Account();
        admin.setId(3);
        admin.setEmail("admin@wisemapping.org");

        // Test that admins can access spam-marked public maps
        boolean result = mindmapService.hasPermissions(admin, spamMap, CollaborationRole.VIEWER);
        assertTrue(result, "Admins should be able to access spam-marked public maps");
    }

    @Test
    public void testHasPermissions_PrivateMap_Spam_BlocksNonOwner() {
        // Create a private spam-marked map
        Mindmap privateSpamMap = new Mindmap();
        privateSpamMap.setId(3);
        privateSpamMap.setPublic(false);
        privateSpamMap.setCreator(owner);
        MindmapSpamInfo spamInfo = new MindmapSpamInfo(privateSpamMap);
        spamInfo.setSpamDetected(true);
        privateSpamMap.setSpamInfo(spamInfo);

        // Test that non-owners cannot access private spam-marked maps
        boolean result = mindmapService.hasPermissions(nonOwner, privateSpamMap, CollaborationRole.VIEWER);
        assertFalse(result, "Non-owners should not be able to access private spam-marked maps");
    }

    @Test
    public void testHasPermissions_PrivateMap_Spam_AllowsOwner() {
        // Create a private spam-marked map
        Mindmap privateSpamMap = new Mindmap();
        privateSpamMap.setId(3);
        privateSpamMap.setPublic(false);
        privateSpamMap.setCreator(owner);
        MindmapSpamInfo spamInfo = new MindmapSpamInfo(privateSpamMap);
        spamInfo.setSpamDetected(true);
        privateSpamMap.setSpamInfo(spamInfo);

        // Test that owners can access their own private spam-marked maps
        boolean result = mindmapService.hasPermissions(owner, privateSpamMap, CollaborationRole.VIEWER);
        assertTrue(result, "Owners should be able to access their own private spam-marked maps");
    }

    @Test
    public void testHasPermissions_EditorRole_Spam_AllowsOwner() {
        // Test that owners can still edit their spam-marked maps
        boolean result = mindmapService.hasPermissions(owner, spamMap, CollaborationRole.EDITOR);
        assertTrue(result, "Owners should be able to edit their own spam-marked maps");
    }

    @Test
    public void testHasPermissions_EditorRole_Spam_BlocksNonOwner() {
        // Test that non-owners cannot edit spam-marked maps
        boolean result = mindmapService.hasPermissions(nonOwner, spamMap, CollaborationRole.EDITOR);
        assertFalse(result, "Non-owners should not be able to edit spam-marked maps");
    }

    @Test
    public void testHasPermissions_PublicMap_Spam_AllowsCollaborator() {
        // Create a collaborator user
        Account collaborator = new Account();
        collaborator.setId(3);
        collaborator.setEmail("collaborator@example.com");

        // Add collaboration to the spam-marked map
        Collaboration collaboration = new Collaboration();
        collaboration.setCollaborator(collaborator);
        collaboration.setRole(CollaborationRole.VIEWER);
        spamMap.addCollaboration(collaboration);

        // Test that collaborators can access spam-marked public maps
        boolean result = mindmapService.hasPermissions(collaborator, spamMap, CollaborationRole.VIEWER);
        assertTrue(result, "Collaborators should be able to access spam-marked public maps");
    }

    @Test
    public void testHasPermissions_PublicMap_Spam_AllowsEditorCollaborator() {
        // Create an editor collaborator user
        Account editorCollaborator = new Account();
        editorCollaborator.setId(4);
        editorCollaborator.setEmail("editor@example.com");

        // Add collaboration to the spam-marked map
        Collaboration collaboration = new Collaboration();
        collaboration.setCollaborator(editorCollaborator);
        collaboration.setRole(CollaborationRole.EDITOR);
        spamMap.addCollaboration(collaboration);

        // Test that editor collaborators can access and edit spam-marked public maps
        boolean viewerResult = mindmapService.hasPermissions(editorCollaborator, spamMap, CollaborationRole.VIEWER);
        boolean editorResult = mindmapService.hasPermissions(editorCollaborator, spamMap, CollaborationRole.EDITOR);
        
        assertTrue(viewerResult, "Editor collaborators should be able to view spam-marked public maps");
        assertTrue(editorResult, "Editor collaborators should be able to edit spam-marked public maps");
    }
}
