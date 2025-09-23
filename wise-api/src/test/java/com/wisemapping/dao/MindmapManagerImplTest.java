package com.wisemapping.dao;

import com.wisemapping.model.Account;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MindmapManagerImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Collaborator> typedQuery;

    private MindmapManagerImpl mindmapManager;

    @BeforeEach
    void setUp() {
        mindmapManager = new MindmapManagerImpl();
        // Use reflection to inject the mocked EntityManager
        try {
            java.lang.reflect.Field field = MindmapManagerImpl.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(mindmapManager, entityManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject EntityManager", e);
        }
    }

    @Test
    void testAddCollaborator_WhenAccountExists_ShouldReuseExistingAccount() {
        // Arrange
        String email = "test@example.com";
        Calendar newCreationDate = Calendar.getInstance();
        
        // Create a mock Account (which extends Collaborator)
        Account existingAccount = new Account();
        existingAccount.setId(123);
        existingAccount.setEmail(email);
        existingAccount.setCreationDate(Calendar.getInstance());
        existingAccount.setFirstname("John");
        existingAccount.setLastname("Doe");
        existingAccount.setAuthenticationType(AuthenticationType.DATABASE);
        
        // Create a new Collaborator to add
        Collaborator newCollaborator = new Collaborator();
        newCollaborator.setEmail(email);
        newCollaborator.setCreationDate(newCreationDate);
        
        // Mock the findCollaborator query to return the existing Account
        when(entityManager.createQuery(anyString(), eq(Collaborator.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(existingAccount));
        
        // Act
        mindmapManager.addCollaborator(newCollaborator);
        
        // Assert
        // Verify that findCollaborator was called
        verify(entityManager).createQuery("SELECT c FROM com.wisemapping.model.Collaborator c WHERE c.email = :email", Collaborator.class);
        verify(typedQuery).setParameter("email", email);
        
        // Verify that merge was called on the existing account (not persist on new collaborator)
        verify(entityManager).merge(existingAccount);
        verify(entityManager, never()).persist(any(Collaborator.class));
        
        // Verify that the new collaborator got the ID from the existing account
        assertEquals(123, newCollaborator.getId());
        
        // Verify that the existing account's creation date was updated
        assertEquals(newCreationDate, existingAccount.getCreationDate());
    }

    @Test
    void testAddCollaborator_WhenNoExistingCollaborator_ShouldPersistNewCollaborator() {
        // Arrange
        String email = "new@example.com";
        Calendar creationDate = Calendar.getInstance();
        
        // Create a new Collaborator to add
        Collaborator newCollaborator = new Collaborator();
        newCollaborator.setEmail(email);
        newCollaborator.setCreationDate(creationDate);
        
        // Mock the findCollaborator query to return empty list (no existing collaborator)
        when(entityManager.createQuery(anyString(), eq(Collaborator.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());
        
        // Act
        mindmapManager.addCollaborator(newCollaborator);
        
        // Assert
        // Verify that findCollaborator was called
        verify(entityManager).createQuery("SELECT c FROM com.wisemapping.model.Collaborator c WHERE c.email = :email", Collaborator.class);
        verify(typedQuery).setParameter("email", email);
        
        // Verify that persist was called on the new collaborator
        verify(entityManager).persist(newCollaborator);
        verify(entityManager, never()).merge(any(Collaborator.class));
    }

    @Test
    void testAddCollaborator_WhenAccountExistsWithNullCreationDate_ShouldNotUpdateCreationDate() {
        // Arrange
        String email = "test@example.com";
        
        // Create a mock Account with existing creation date
        Account existingAccount = new Account();
        existingAccount.setId(123);
        existingAccount.setEmail(email);
        Calendar existingCreationDate = Calendar.getInstance();
        existingAccount.setCreationDate(existingCreationDate);
        existingAccount.setFirstname("John");
        existingAccount.setLastname("Doe");
        existingAccount.setAuthenticationType(AuthenticationType.DATABASE);
        
        // Create a new Collaborator with null creation date
        Collaborator newCollaborator = new Collaborator();
        newCollaborator.setEmail(email);
        newCollaborator.setCreationDate(null); // null creation date
        
        // Mock the findCollaborator query to return the existing Account
        when(entityManager.createQuery(anyString(), eq(Collaborator.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(existingAccount));
        
        // Act
        mindmapManager.addCollaborator(newCollaborator);
        
        // Assert
        // Verify that merge was NOT called since creation date is null
        verify(entityManager, never()).merge(existingAccount);
        
        // Verify that the existing account's creation date was NOT changed
        assertEquals(existingCreationDate, existingAccount.getCreationDate());
        
        // Verify that the new collaborator got the ID from the existing account
        assertEquals(123, newCollaborator.getId());
    }

    @Test
    void testAddCollaborator_ShouldHandleDuplicateEmailConstraintGracefully() {
        // This test verifies that the method handles the scenario where an Account
        // with the same email already exists, preventing the "Duplicate entry for key COLLABORATOR.email" error
        
        // Arrange
        String email = "enzo.verrecchia@a-just.fr"; // The email from the original error
        Calendar creationDate = Calendar.getInstance();
        
        // Create a mock Account (which extends Collaborator) - this represents an existing user
        Account existingAccount = new Account();
        existingAccount.setId(456);
        existingAccount.setEmail(email);
        existingAccount.setCreationDate(Calendar.getInstance());
        existingAccount.setFirstname("Enzo");
        existingAccount.setLastname("Verrecchia");
        existingAccount.setAuthenticationType(AuthenticationType.DATABASE);
        
        // Create a new Collaborator that would cause the duplicate constraint violation
        Collaborator newCollaborator = new Collaborator();
        newCollaborator.setEmail(email);
        newCollaborator.setCreationDate(creationDate);
        
        // Mock the findCollaborator query to return the existing Account
        when(entityManager.createQuery(anyString(), eq(Collaborator.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(existingAccount));
        
        // Act - This should NOT throw a duplicate key constraint exception
        assertDoesNotThrow(() -> {
            mindmapManager.addCollaborator(newCollaborator);
        });
        
        // Assert
        // Verify that no persist was called (which would cause the constraint violation)
        verify(entityManager, never()).persist(any(Collaborator.class));
        
        // Verify that merge was called instead (reusing existing entity)
        verify(entityManager).merge(existingAccount);
        
        // Verify that the new collaborator got the ID from the existing account
        assertEquals(456, newCollaborator.getId());
    }


}
