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

import com.wisemapping.model.*;
import com.wisemapping.security.DefaultPasswordEncoderFactories;
import com.wisemapping.security.LegacyPasswordEncoder;
import com.wisemapping.service.InactiveMindmapMigrationService;
import com.wisemapping.service.MetricsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Repository
public class UserManagerImpl
        implements UserManager {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    @Lazy
    private InactiveMindmapMigrationService inactiveMindmapMigrationService;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private MindmapManager mindmapManager;

    public UserManagerImpl() {
    }

    public void setEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<Account> getAllUsers() {
        return entityManager.createQuery("from com.wisemapping.model.Account user", Account.class).getResultList();
    }

    @Override
    @Nullable
    public Account getUserBy(@NotNull final String email) {
        Account user = null;

        TypedQuery<Account> query = entityManager.createQuery("from com.wisemapping.model.Account colaborator where email=:email", Account.class);
        query.setParameter("email", email);

        final List<Account> users = query.getResultList();
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same email!";
            user = users.get(0);
        }
        return user;

    }

    @Override
    public Collaborator getCollaboratorBy(final String email) {
        final Collaborator result;

        final TypedQuery<Collaborator> query = entityManager.createQuery("from com.wisemapping.model.Collaborator colaborator where " +
                "email=:email", Collaborator.class);
        query.setParameter("email", email);

        final List<Collaborator> cols = query.getResultList();
        if (cols != null && !cols.isEmpty()) {
            assert cols.size() == 1 : "More than one colaborator with the same email!";
            result = cols.get(0);
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    @Override
    public Account getUserBy(int id) {
        return entityManager.find(Account.class, id);
    }

    @Override
    @Transactional
    public void createUser(Account user) {
        assert user != null : "Trying to store a null user";
        if (!AuthenticationType.GOOGLE_OAUTH2.equals(user.getAuthenticationType())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword("");
        }
        entityManager.persist(user);
    }

    @Override
    @Transactional
    public Account createUser(@NotNull Account user, @NotNull Collaborator collaborator) {
        assert user != null : "Trying to store a null user";
        assert collaborator != null : "Trying to store a null collaborator";

        try {
            // Migrate from previous temporal collab to new user ...
            // First, rename the collaborator email to avoid constraint violations
            String originalEmail = collaborator.getEmail();
            collaborator.setEmail(originalEmail + "_toRemove_" + System.currentTimeMillis());
            entityManager.merge(collaborator);
            entityManager.flush();

            // Save the new account...
            this.createUser(user);

            // Update all collaborations to point to the new account
            final Set<Collaboration> collaborations = new CopyOnWriteArraySet<>(collaborator.getCollaborations());
            for (Collaboration collabs : collaborations) {
                collabs.setCollaborator(user);
            }

            // Delete the old collaborator record
            entityManager.remove(collaborator);
            entityManager.flush();
            
            return user;
        } catch (Exception e) {
            // If anything goes wrong, we need to clean up and rethrow
            throw new RuntimeException("Failed to migrate collaborator to account for email: " + 
                collaborator.getEmail() + " -> " + user.getEmail(), e);
        }
    }

    @Override
    @Transactional
    public void removeUser(@NotNull final Account user) {
        entityManager.remove(user);
    }

    @Transactional
    public void auditLogin(@NotNull AccessAuditory accessAuditory) {
        assert accessAuditory != null : "accessAuditory is null";
        entityManager.persist(accessAuditory);
    }

    @Transactional
    public void updateUser(@NotNull Account user) {
        assert user != null : "user is null";

        // Does the password need to be encrypted ?
        final String password = user.getPassword();
        if (password != null && (!password.startsWith(LegacyPasswordEncoder.ENC_PREFIX) && !password.startsWith("{" + DefaultPasswordEncoderFactories.ENCODING_ID))) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        entityManager.merge(user);
        entityManager.flush();
    }

    public Account getUserByActivationCode(long code) {
        final Account user;

        final TypedQuery<Account> query = entityManager.createQuery("from com.wisemapping.model.Account user where " +
                "activationCode=:activationCode", Account.class);
        query.setParameter("activationCode", code);

        final List<Account> users = query.getResultList();
        if (users != null && !users.isEmpty()) {

            assert users.size() == 1 : "More than one user with the same activation code!";
            user = users.get(0);
        } else {
            user = null;
        }
        return user;
    }

    @Override
    public List<Account> getAllUsers(int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createQuery(
            "SELECT u FROM com.wisemapping.model.Account u " +
            "ORDER BY u.id DESC", 
            Account.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllUsers() {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(u) FROM com.wisemapping.model.Account u", 
            Long.class);
        return query.getSingleResult();
    }

    @Override
    public List<Account> searchUsers(String search, int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createQuery(
            "SELECT u FROM com.wisemapping.model.Account u " +
            "WHERE LOWER(u.email) LIKE LOWER(:search) " +
            "   OR LOWER(u.firstname) LIKE LOWER(:search) " +
            "   OR LOWER(u.lastname) LIKE LOWER(:search) " +
            "ORDER BY u.id DESC", 
            Account.class);
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countUsersBySearch(String search) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(u) FROM com.wisemapping.model.Account u " +
            "WHERE LOWER(u.email) LIKE LOWER(:search) " +
            "   OR LOWER(u.firstname) LIKE LOWER(:search) " +
            "   OR LOWER(u.lastname) LIKE LOWER(:search)", 
            Long.class);
        query.setParameter("search", "%" + search + "%");
        return query.getSingleResult();
    }

    @Override
    public List<Account> findUsersInactiveSince(Calendar cutoffDate, Calendar creationCutoffDate, int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createQuery(
            "SELECT DISTINCT a FROM com.wisemapping.model.Account a " +
            "WHERE a.suspended = false " +
            "  AND a.activationDate IS NOT NULL " +
            "  AND a.creationDate <= :creationCutoffDate " +
            "  AND a.id NOT IN (" +
            "      SELECT DISTINCT aa.user.id FROM com.wisemapping.model.AccessAuditory aa " +
            "      WHERE aa.loginDate >= :cutoffDate" +
            "  ) " +
            "  AND a.id NOT IN (" +
            "      SELECT DISTINCT m.creator.id FROM com.wisemapping.model.Mindmap m " +
            "      WHERE m.lastModificationTime >= :cutoffDate" +
            "  ) " +
            "ORDER BY a.id", 
            Account.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("creationCutoffDate", creationCutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Account> findSuspendedUsers(int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createQuery(
            "SELECT a FROM com.wisemapping.model.Account a " +
            "WHERE a.suspended = true " +
            "ORDER BY a.id", 
            Account.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Account> findUsersSuspendedForInactivity(int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createQuery(
            "SELECT a FROM com.wisemapping.model.Account a " +
            "WHERE a.suspended = true " +
            "AND a.suspensionReasonCode = :inactivityCode " +
            "ORDER BY a.id", 
            Account.class);
        query.setParameter("inactivityCode", SuspensionReason.INACTIVITY.getCode().charAt(0));
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Account> getUsersWithFilters(String search, Boolean filterActive, Boolean filterSuspended, 
                                             String filterAuthType, int offset, int limit) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM com.wisemapping.model.Account u WHERE 1=1 ");
        
        // Build WHERE clause dynamically based on provided filters
        if (search != null && !search.trim().isEmpty()) {
            String trimmedSearch = search.trim();
            
            // Optimize: if search looks like an email, only search email field (uses email index)
            if (trimmedSearch.contains("@")) {
                jpql.append("AND LOWER(u.email) LIKE LOWER(:search) ");
            } else {
                // Search across all fields for non-email searches
                jpql.append("AND (LOWER(u.email) LIKE LOWER(:search) ")
                    .append("OR LOWER(u.firstname) LIKE LOWER(:search) ")
                    .append("OR LOWER(u.lastname) LIKE LOWER(:search)) ");
            }
        }
        
        if (filterActive != null) {
            if (filterActive) {
                // Active users have activationDate set
                jpql.append("AND u.activationDate IS NOT NULL ");
            } else {
                // Inactive users don't have activationDate set
                jpql.append("AND u.activationDate IS NULL ");
            }
        }
        
        if (filterSuspended != null) {
            jpql.append("AND u.suspended = :suspended ");
        }
        
        if (filterAuthType != null && !filterAuthType.trim().isEmpty()) {
            jpql.append("AND u.authenticationType = :authType ");
        }
        
        jpql.append("ORDER BY u.id DESC");
        
        TypedQuery<Account> query = entityManager.createQuery(jpql.toString(), Account.class);
        
        // Set parameters only for conditions that were added
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.trim() + "%");
        }
        
        if (filterSuspended != null) {
            query.setParameter("suspended", filterSuspended);
        }
        
        if (filterAuthType != null && !filterAuthType.trim().isEmpty()) {
            try {
                query.setParameter("authType", AuthenticationType.valueOf(filterAuthType));
            } catch (IllegalArgumentException e) {
                // Invalid auth type, return empty result
                return new java.util.ArrayList<>();
            }
        }
        
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countUsersWithFilters(String search, Boolean filterActive, Boolean filterSuspended, String filterAuthType) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM com.wisemapping.model.Account u WHERE 1=1 ");
        
        // Build WHERE clause dynamically - same logic as getUsersWithFilters
        if (search != null && !search.trim().isEmpty()) {
            String trimmedSearch = search.trim();
            
            // Optimize: if search looks like an email, only search email field (uses email index)
            if (trimmedSearch.contains("@")) {
                jpql.append("AND LOWER(u.email) LIKE LOWER(:search) ");
            } else {
                // Search across all fields for non-email searches
                jpql.append("AND (LOWER(u.email) LIKE LOWER(:search) ")
                    .append("OR LOWER(u.firstname) LIKE LOWER(:search) ")
                    .append("OR LOWER(u.lastname) LIKE LOWER(:search)) ");
            }
        }
        
        if (filterActive != null) {
            if (filterActive) {
                jpql.append("AND u.activationDate IS NOT NULL ");
            } else {
                jpql.append("AND u.activationDate IS NULL ");
            }
        }
        
        if (filterSuspended != null) {
            jpql.append("AND u.suspended = :suspended ");
        }
        
        if (filterAuthType != null && !filterAuthType.trim().isEmpty()) {
            jpql.append("AND u.authenticationType = :authType ");
        }
        
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        
        // Set parameters only for conditions that were added
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.trim() + "%");
        }
        
        if (filterSuspended != null) {
            query.setParameter("suspended", filterSuspended);
        }
        
        if (filterAuthType != null && !filterAuthType.trim().isEmpty()) {
            try {
                query.setParameter("authType", AuthenticationType.valueOf(filterAuthType));
            } catch (IllegalArgumentException e) {
                // Invalid auth type, return 0
                return 0L;
            }
        }
        
        return query.getSingleResult();
    }

    @Override
    public long countUsersInactiveSince(Calendar cutoffDate, Calendar creationCutoffDate) {
        final TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT a) FROM com.wisemapping.model.Account a " +
            "WHERE a.suspended = false " +
            "  AND a.activationDate IS NOT NULL " +
            "  AND a.creationDate <= :creationCutoffDate " +
            "  AND a.id NOT IN (" +
            "      SELECT DISTINCT aa.user.id FROM com.wisemapping.model.AccessAuditory aa " +
            "      WHERE aa.loginDate >= :cutoffDate" +
            "  ) " +
            "  AND a.id NOT IN (" +
            "      SELECT DISTINCT m.creator.id FROM com.wisemapping.model.Mindmap m " +
            "      WHERE m.lastModificationTime >= :cutoffDate" +
            "  )",
            Long.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("creationCutoffDate", creationCutoffDate);
        return query.getSingleResult();
    }

    @Override
    @Nullable
    public Calendar findLastLoginDate(int userId) {
        try {
            final TypedQuery<Calendar> query = entityManager.createQuery(
                "SELECT MAX(aa.loginDate) FROM com.wisemapping.model.AccessAuditory aa WHERE aa.user.id = :userId", 
                Calendar.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<InactiveUserResult> findInactiveUsersWithActivity(Calendar cutoffDate, Calendar creationCutoffDate, int offset, int limit) {
        // Use a simpler approach that doesn't require complex GROUP BY
        // First get inactive users, then get their activity in separate queries
        final TypedQuery<Account> userQuery = entityManager.createQuery(
            "SELECT a FROM com.wisemapping.model.Account a " +
            "WHERE a.suspended = false " +
            "  AND a.activationDate IS NOT NULL " +
            "  AND a.creationDate <= :creationCutoffDate " +
            "  AND a.id NOT IN (" +
            "      SELECT DISTINCT aa.user.id FROM com.wisemapping.model.AccessAuditory aa " +
            "      WHERE aa.loginDate >= :cutoffDate" +
            "  ) " +
            "  AND a.id NOT IN (" +
            "      SELECT DISTINCT m.creator.id FROM com.wisemapping.model.Mindmap m " +
            "      WHERE m.lastModificationTime >= :cutoffDate" +
            "  ) " +
            "ORDER BY a.id", 
            Account.class);
        
        userQuery.setParameter("cutoffDate", cutoffDate);
        userQuery.setParameter("creationCutoffDate", creationCutoffDate);
        userQuery.setFirstResult(offset);
        userQuery.setMaxResults(limit);
        
        List<Account> inactiveUsers = userQuery.getResultList();
        
        // Get activity data for each user
        return inactiveUsers.stream()
                .map(user -> {
                    Calendar lastLogin = findLastLoginDate(user.getId());
                    Calendar lastActivity = mindmapManager.findLastModificationTimeByCreator(user.getId());
                    return new InactiveUserResult(user, lastLogin, lastActivity);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void suspendUser(@NotNull Account user, @NotNull SuspensionReason reason) {
        user.setSuspended(true);
        user.setSuspensionReason(reason);
        entityManager.merge(user);
    }

    @Override
    @Transactional
    public int unsuspendUser(@NotNull Account user) {
        // Store the previous suspension reason before unsuspending
        SuspensionReason previousSuspensionReason = user.getSuspensionReason();
        
        // Unsuspend the user
        user.unsuspend();
        entityManager.merge(user);
        entityManager.flush();

        // Restore mindmaps if user was suspended for inactivity
        int restoredCount = 0;
        if (previousSuspensionReason == SuspensionReason.INACTIVITY) {
            try {
                restoredCount = inactiveMindmapMigrationService.restoreUserMindmaps(user);
                if (restoredCount > 0) {
                    // Log mindmap restoration for inactive user reactivation
                    metricsService.trackInactiveMindmapMigration(1, -restoredCount); // Negative count indicates restoration
                }
            } catch (Exception e) {
                // Log error but don't fail the unsuspension - mindmaps can be restored later manually if needed
                // The user unsuspension should succeed even if mindmap restoration fails
            }
        }

        return restoredCount;
    }
}
