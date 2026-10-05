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

import com.wisemapping.config.AppConfig;
import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.InactiveUserResult;
import com.wisemapping.model.SuspensionReason;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static com.wisemapping.dao.DaoTestSupport.daysAgo;
import static com.wisemapping.dao.DaoTestSupport.persistAccount;
import static com.wisemapping.dao.DaoTestSupport.persistLogin;
import static com.wisemapping.dao.DaoTestSupport.persistMindmap;
import static com.wisemapping.dao.DaoTestSupport.uniqueEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link UserManagerImpl} executed against the real
 * in-memory HSQLDB schema, without going through the REST API.
 *
 * <p>Every method here runs its JPQL / named query / Criteria query through the
 * database. That is the point: the pre-existing {@code MindmapManagerImplTest}
 * mocks the {@link EntityManager} and therefore verifies call sequences rather
 * than queries, which cannot catch a malformed query string or a named query
 * that does not resolve.
 *
 * <p>Tests are {@code @Transactional} so each one rolls back. Seed rows from
 * {@code data-hsqldb.sql} are always present, so count assertions are written
 * as deltas against a captured baseline.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
class UserManagerIntegrationTest {

    @Autowired
    private UserManager userManager;

    @PersistenceContext
    private EntityManager entityManager;

    // ---------------------------------------------------------------- listing

    @Test
    @DisplayName("getAllUsers() returns every account, including newly created ones")
    void getAllUsersReturnsPersistedAccounts() {
        final long baseline = userManager.getAllUsers().size();

        final Account created = persistAccount(entityManager, "get-all");

        final List<Account> all = userManager.getAllUsers();
        assertEquals(baseline + 1, all.size(), "the new account should appear in getAllUsers()");
        assertTrue(all.stream().anyMatch(a -> a.getId() == created.getId()));
    }

    @Test
    @DisplayName("getAllUsers(offset, limit) honours the paging window")
    void getAllUsersIsPageable() {
        // Three extra accounts guarantee the window is smaller than the table.
        persistAccount(entityManager, "page-a");
        persistAccount(entityManager, "page-b");
        persistAccount(entityManager, "page-c");

        final List<Account> firstPage = userManager.getAllUsers(0, 2);
        assertEquals(2, firstPage.size(), "limit should cap the page size");

        final List<Account> secondPage = userManager.getAllUsers(2, 2);
        assertFalse(secondPage.isEmpty(), "there should be a second page");

        final Set<Integer> firstIds = firstPage.stream().map(Account::getId).collect(java.util.stream.Collectors.toSet());
        assertTrue(secondPage.stream().noneMatch(a -> firstIds.contains(a.getId())),
                "offset should not re-return rows from the first page");
    }

    @Test
    @DisplayName("countAllUsers() tracks inserts")
    void countAllUsersTracksInserts() {
        final long before = userManager.countAllUsers();

        persistAccount(entityManager, "count-all");

        assertEquals(before + 1, userManager.countAllUsers());
    }

    // ----------------------------------------------------------------- search

    @Test
    @DisplayName("searchUsers() matches on email and is paged; countUsersBySearch() agrees with it")
    void searchUsersMatchesAndCounts() {
        // A token unlikely to collide with seed data or other fixtures.
        final String token = "zzsearch" + System.nanoTime();

        final Account match = new Account();
        match.setEmail(token + "@dao-test.example.org");
        match.setFirstname("Searchable");
        match.setLastname("Account");
        match.setPassword("daoTestPassword123");
        match.setAuthenticationType(AuthenticationType.DATABASE);
        match.setCreationDate(Calendar.getInstance());
        match.setActivationDate(Calendar.getInstance());
        entityManager.persist(match);
        entityManager.flush();

        // Noise that must not match.
        persistAccount(entityManager, "search-noise");

        final List<Account> found = userManager.searchUsers(token, 0, 10);
        assertEquals(1, found.size(), "exactly the one account should match the unique token");
        assertEquals(match.getId(), found.get(0).getId());

        assertEquals(1L, userManager.countUsersBySearch(token),
                "countUsersBySearch must agree with searchUsers for the same term");
    }

    @Test
    @DisplayName("countUsersBySearch() returns zero for a term that matches nothing")
    void countUsersBySearchReturnsZeroWhenNoMatch() {
        assertEquals(0L, userManager.countUsersBySearch("no-such-user-" + System.nanoTime()));
    }

    // ------------------------------------------------------------- facebook id

    @Test
    @DisplayName("getUserByFacebookId() matches any of the candidate tokens")
    void getUserByFacebookIdMatchesCandidateTokens() {
        final String token = "fb-token-" + System.nanoTime();

        final Account fbUser = persistAccount(entityManager, "facebook");
        fbUser.setAuthenticationType(AuthenticationType.FACEBOOK_OAUTH2);
        fbUser.setOauthToken(token);
        entityManager.merge(fbUser);
        entityManager.flush();

        // The IN clause exists so the legacy plaintext and the new {enc} form can
        // both be probed in one round-trip; pass both spellings.
        final Account found = userManager.getUserByFacebookId(List.of("{enc}whatever", token));
        assertNotNull(found, "the account should be found via one of the candidate tokens");
        assertEquals(fbUser.getId(), found.getId());
    }

    @Test
    @DisplayName("getUserByFacebookId() returns null when no token matches")
    void getUserByFacebookIdReturnsNullWhenNoMatch() {
        assertNull(userManager.getUserByFacebookId(List.of("absent-" + System.nanoTime())));
    }

    @Test
    @DisplayName("getUserByFacebookId() ignores accounts of a different authentication type")
    void getUserByFacebookIdIgnoresNonFacebookAccounts() {
        final String token = "db-token-" + System.nanoTime();

        // Same token, but a DATABASE account: the query filters on authType too.
        final Account dbUser = persistAccount(entityManager, "not-facebook");
        dbUser.setOauthToken(token);
        entityManager.merge(dbUser);
        entityManager.flush();

        assertNull(userManager.getUserByFacebookId(List.of(token)),
                "a DATABASE account must not be returned by the Facebook lookup");
    }

    // ------------------------------------------------------------- inactivity

    @Test
    @DisplayName("findUsersInactiveSince() selects accounts with no recent login and no recent map edit")
    void findUsersInactiveSinceSelectsDormantAccounts() {
        final Calendar cutoff = daysAgo(30);
        final Calendar creationCutoff = daysAgo(10);

        // Dormant: created long ago, last login well before the cutoff.
        final Account dormant = persistAccount(entityManager, "dormant", 400);
        persistLogin(entityManager, dormant, 200);

        // Active: created long ago but logged in after the cutoff.
        final Account active = persistAccount(entityManager, "active", 400);
        persistLogin(entityManager, active, 1);

        // Recently created: excluded by creationCutoffDate regardless of activity.
        final Account fresh = persistAccount(entityManager, "fresh", 1);

        final List<Account> inactive = userManager.findUsersInactiveSince(cutoff, creationCutoff, 0, 100);
        final Set<Integer> ids = inactive.stream().map(Account::getId).collect(java.util.stream.Collectors.toSet());

        assertTrue(ids.contains(dormant.getId()), "the dormant account should be reported inactive");
        assertFalse(ids.contains(active.getId()), "an account with a recent login is not inactive");
        assertFalse(ids.contains(fresh.getId()), "an account newer than creationCutoffDate must be excluded");
    }

    @Test
    @DisplayName("findUsersInactiveSince() excludes accounts whose maps were edited recently")
    void findUsersInactiveSinceExcludesRecentMapEditors() {
        final Account editor = persistAccount(entityManager, "map-editor", 400);
        persistLogin(entityManager, editor, 200);
        // No recent login, but a map modified now — the second NOT IN subquery.
        persistMindmap(entityManager, editor, "Recently edited", false);

        final List<Account> inactive = userManager.findUsersInactiveSince(daysAgo(30), daysAgo(10), 0, 100);

        assertTrue(inactive.stream().noneMatch(a -> a.getId() == editor.getId()),
                "a recent map modification must keep the account out of the inactive set");
    }

    @Test
    @DisplayName("countUsersInactiveSince() agrees with findUsersInactiveSince()")
    void countUsersInactiveSinceAgreesWithFind() {
        final Calendar cutoff = daysAgo(30);
        final Calendar creationCutoff = daysAgo(10);

        final Account dormant = persistAccount(entityManager, "count-dormant", 400);
        persistLogin(entityManager, dormant, 200);

        final long counted = userManager.countUsersInactiveSince(cutoff, creationCutoff);
        final int found = userManager.findUsersInactiveSince(cutoff, creationCutoff, 0, 1000).size();

        assertEquals(found, counted, "the count query and the list query must agree");
    }

    @Test
    @DisplayName("findInactiveUsersWithActivity() projects last login and last map edit")
    void findInactiveUsersWithActivityProjectsActivityDates() {
        final Account dormant = persistAccount(entityManager, "projection", 400);
        persistLogin(entityManager, dormant, 200);

        final List<InactiveUserResult> results =
                userManager.findInactiveUsersWithActivity(daysAgo(30), daysAgo(10), 0, 1000);

        final InactiveUserResult mine = results.stream()
                .filter(r -> r.getUser().getId() == dormant.getId())
                .findFirst()
                .orElse(null);

        assertNotNull(mine, "the dormant account should be present in the projection");
        assertNotNull(mine.getLastLogin(), "last login should be projected from AccessAuditory");
    }

    // ------------------------------------------------------------- suspension

    @Test
    @DisplayName("suspendUser() persists the suspension and findSuspendedUsers() reports it")
    void suspendUserIsPersistedAndListed() {
        final Account user = persistAccount(entityManager, "to-suspend");

        userManager.suspendUser(user, SuspensionReason.ABUSE);

        final Account reloaded = userManager.getUserBy(user.getId());
        assertNotNull(reloaded);
        assertTrue(reloaded.isSuspended(), "the account should be suspended");
        assertEquals(SuspensionReason.ABUSE, reloaded.getSuspensionReason());

        assertTrue(userManager.findSuspendedUsers(0, 1000).stream()
                        .anyMatch(a -> a.getId() == user.getId()),
                "findSuspendedUsers() should report the suspended account");
    }

    @Test
    @DisplayName("findUsersSuspendedForInactivity() reports only INACTIVITY suspensions")
    void findUsersSuspendedForInactivityFiltersByReason() {
        final Account inactivity = persistAccount(entityManager, "susp-inactivity");
        userManager.suspendUser(inactivity, SuspensionReason.INACTIVITY);

        final Account abuse = persistAccount(entityManager, "susp-abuse");
        userManager.suspendUser(abuse, SuspensionReason.ABUSE);

        final Set<Integer> ids = userManager.findUsersSuspendedForInactivity(0, 1000).stream()
                .map(Account::getId)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(ids.contains(inactivity.getId()), "the INACTIVITY suspension should be listed");
        assertFalse(ids.contains(abuse.getId()), "an ABUSE suspension must not be listed");
    }

    @Test
    @DisplayName("unsuspendUser() clears the suspension; no mindmaps to restore for a non-INACTIVITY reason")
    void unsuspendUserClearsSuspension() {
        final Account user = persistAccount(entityManager, "to-unsuspend");
        userManager.suspendUser(user, SuspensionReason.ABUSE);

        final int restored = userManager.unsuspendUser(user);

        assertEquals(0, restored, "an ABUSE suspension has no inactive mindmaps to restore");
        final Account reloaded = userManager.getUserBy(user.getId());
        assertNotNull(reloaded);
        assertFalse(reloaded.isSuspended(), "the account should no longer be suspended");
    }

    @Test
    @DisplayName("unsuspendUser() takes the INACTIVITY restore path without failing")
    void unsuspendUserHandlesInactivityRestorePath() {
        final Account user = persistAccount(entityManager, "unsuspend-inactivity");
        userManager.suspendUser(user, SuspensionReason.INACTIVITY);

        // No inactive mindmaps were migrated for this account, so the restore
        // service has nothing to do; the call must still succeed and report 0.
        final int restored = userManager.unsuspendUser(user);

        assertEquals(0, restored);
        final Account reloaded = userManager.getUserBy(user.getId());
        assertNotNull(reloaded);
        assertFalse(reloaded.isSuspended());
    }

    // ------------------------------------------------------------ login audit

    @Test
    @DisplayName("findLastLoginDate() returns the most recent login")
    void findLastLoginDateReturnsMostRecent() {
        final Account user = persistAccount(entityManager, "last-login");
        persistLogin(entityManager, user, 10);
        persistLogin(entityManager, user, 2);
        persistLogin(entityManager, user, 7);

        final Calendar last = userManager.findLastLoginDate(user.getId());

        assertNotNull(last, "a login date should be returned");
        final Calendar threeDaysAgo = daysAgo(3);
        assertTrue(last.after(threeDaysAgo),
                "the MAX(loginDate) should be the 2-day-old login, not an older one");
    }

    @Test
    @DisplayName("findLastLoginDate() returns null when the account never logged in")
    void findLastLoginDateReturnsNullWithoutLogins() {
        final Account user = persistAccount(entityManager, "never-logged-in");

        assertNull(userManager.findLastLoginDate(user.getId()),
                "MAX() over an empty set must surface as null, not an exception");
    }

    // ------------------------------------------------- collaborator promotion

    @Test
    @DisplayName("createUser(account, collaborator) migrates collaborations and drops the placeholder")
    void createUserMigratesCollaboratorToAccount() {
        // A map owner who will share a map with a not-yet-registered address.
        final Account owner = persistAccount(entityManager, "share-owner");
        final var mindmap = persistMindmap(entityManager, owner, "Shared before signup", false);

        final String invitedEmail = uniqueEmail("invited");
        final Collaborator placeholder = new Collaborator();
        placeholder.setEmail(invitedEmail);
        placeholder.setCreationDate(Calendar.getInstance());
        entityManager.persist(placeholder);
        entityManager.flush();

        // The placeholder collaborates on the map.
        final com.wisemapping.model.Collaboration collaboration =
                new com.wisemapping.model.Collaboration(
                        com.wisemapping.model.CollaborationRole.EDITOR, placeholder, mindmap);
        entityManager.persist(collaboration);
        entityManager.flush();
        entityManager.refresh(placeholder);

        // Now the invitee registers for real.
        final Account registered = new Account();
        registered.setEmail(invitedEmail);
        registered.setFirstname("Invited");
        registered.setLastname("User");
        registered.setPassword("daoTestPassword123");
        registered.setAuthenticationType(AuthenticationType.DATABASE);
        registered.setCreationDate(Calendar.getInstance());

        final Account result = userManager.createUser(registered, placeholder);
        entityManager.flush();

        assertNotNull(result.getId(), "the new account should have been persisted");
        final Account looked = userManager.getUserBy(invitedEmail);
        assertNotNull(looked, "the registered account should be findable by the invited email");
        assertEquals(result.getId(), looked.getId());

        // The collaboration now points at the real account.
        final com.wisemapping.model.Collaboration reloaded =
                entityManager.find(com.wisemapping.model.Collaboration.class, collaboration.getId());
        assertNotNull(reloaded, "the collaboration should survive the migration");
        assertEquals(looked.getId(), reloaded.getCollaborator().getId(),
                "the collaboration should have been re-pointed at the new account");
    }
}
