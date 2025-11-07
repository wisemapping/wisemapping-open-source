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
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserManagerImpl.class);
    
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    @Lazy
    private InactiveMindmapMigrationService inactiveMindmapMigrationService;
    @Autowired
    private MetricsService metricsService;

    public UserManagerImpl() {
    }

    public void setEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<Account> getAllUsers() {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        final Root<Account> root = cq.from(Account.class);
        cq.select(root);
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    @Nullable
    public Account getUserBy(@NotNull final String email) {
        // Use Criteria API for type-safe query
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        final Root<Account> root = cq.from(Account.class);
        
        cq.select(root).where(cb.equal(root.get("email"), email));

        final List<Account> users = entityManager.createQuery(cq).getResultList();
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same email!";
            return users.get(0);
        }
        return null;
    }

    @Override
    public Collaborator getCollaboratorBy(final String email) {
        // Use Criteria API for type-safe query that handles inheritance properly
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Collaborator> cq = cb.createQuery(Collaborator.class);
        final Root<Collaborator> root = cq.from(Collaborator.class);
        
        cq.select(root).where(cb.equal(root.get("email"), email));

        final List<Collaborator> cols = entityManager.createQuery(cq).getResultList();
        if (cols != null && !cols.isEmpty()) {
            assert cols.size() == 1 : "More than one colaborator with the same email!";
            return cols.get(0);
        }
        return null;
    }

    @Nullable
    @Override
    @Transactional(readOnly = true)
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

    /**
     * Evicts Account entity from Hibernate second-level cache.
     * This ensures that subsequent loads get fresh data from the database,
     * which is critical for fields like suspension status and activation date.
     */
    private void evictAccountCache(int accountId) {
        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            sessionFactory.getCache().evict(Account.class, accountId);
        } catch (Exception e) {
            // Log but don't fail the operation - cache eviction is best effort
            logger.warn("Failed to evict Account cache for ID {}: {}", accountId, e.getMessage());
        }
    }

    @Transactional
    public void updateUser(@NotNull Account user) {
        assert user != null : "user is null";

        // Store previous state to detect critical changes (before merge modifies the entity)
        int accountId = user.getId();
        boolean previousSuspended = user.isSuspended();
        Calendar previousActivationDate = user.getActivationDate();
        
        // Does the password need to be encrypted ?
        final String password = user.getPassword();
        if (password != null && (!password.startsWith(LegacyPasswordEncoder.ENC_PREFIX) && !password.startsWith("{" + DefaultPasswordEncoderFactories.ENCODING_ID))) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        Account mergedUser = entityManager.merge(user);
        entityManager.flush();
        
        // Evict cache if critical fields changed (suspension or activation status)
        boolean suspendedChanged = previousSuspended != mergedUser.isSuspended();
        boolean activationChanged = (previousActivationDate == null && mergedUser.getActivationDate() != null) ||
                                   (previousActivationDate != null && mergedUser.getActivationDate() == null);
        
        if (suspendedChanged || activationChanged) {
            evictAccountCache(accountId);
        }
    }

    public Account getUserByActivationCode(long code) {
        final Account user;

        final TypedQuery<Account> query = entityManager.createNamedQuery("Account.getUserByActivationCode", Account.class);
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
        final TypedQuery<Account> query = entityManager.createNamedQuery("Account.getAllUsers", Account.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countAllUsers() {
        final TypedQuery<Long> query = entityManager.createNamedQuery("Account.countAllUsers", Long.class);
        return query.getSingleResult();
    }

    @Override
    public List<Account> searchUsers(String search, int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createNamedQuery("Account.searchUsers", Account.class);
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countUsersBySearch(String search) {
        final TypedQuery<Long> query = entityManager.createNamedQuery("Account.countUsersBySearch", Long.class);
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
        final TypedQuery<Account> query = entityManager.createNamedQuery("Account.findSuspendedUsers", Account.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Account> findUsersSuspendedForInactivity(int offset, int limit) {
        final TypedQuery<Account> query = entityManager.createNamedQuery("Account.findUsersSuspendedForInactivity", Account.class);
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
            final TypedQuery<Calendar> query = entityManager.createNamedQuery("AccessAuditory.findLastLoginDate", Calendar.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<InactiveUserResult> findInactiveUsersWithActivity(Calendar cutoffDate, Calendar creationCutoffDate, int offset, int limit) {
        final TypedQuery<InactiveUserResult> query = entityManager.createQuery(
            "SELECT new com.wisemapping.model.InactiveUserResult(" +
            "    a," +
            "    (SELECT MAX(aa.loginDate) FROM com.wisemapping.model.AccessAuditory aa WHERE aa.user.id = a.id)," +
            "    (SELECT MAX(m.lastModificationTime) FROM com.wisemapping.model.Mindmap m WHERE m.creator.id = a.id)" +
            ") " +
            "FROM com.wisemapping.model.Account a " +
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
            InactiveUserResult.class);
        
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("creationCutoffDate", creationCutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void suspendUser(@NotNull Account user, @NotNull SuspensionReason reason) {
        user.setSuspended(true);
        user.setSuspensionReason(reason);
        entityManager.merge(user);
        entityManager.flush();
        
        // Evict from cache to ensure suspension status is immediately reflected
        evictAccountCache(user.getId());
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
        
        // Evict from cache to ensure unsuspension status is immediately reflected
        evictAccountCache(user.getId());

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
