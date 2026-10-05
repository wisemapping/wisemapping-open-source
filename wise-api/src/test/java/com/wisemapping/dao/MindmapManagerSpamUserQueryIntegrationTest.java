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
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.model.SpamUserResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wisemapping.dao.DaoTestSupport.markAsScannedClean;
import static com.wisemapping.dao.DaoTestSupport.markAsSpam;
import static com.wisemapping.dao.DaoTestSupport.persistAccount;
import static com.wisemapping.dao.DaoTestSupport.persistMindmap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the spam-user-threshold query family on
 * {@link MindmapManager} / {@link MindmapManagerImpl}, executed against the
 * real in-memory HSQLDB schema.
 *
 * <p>Every method under test is hand-written JPQL with {@code GROUP BY} /
 * {@code HAVING} aggregation, so a wrong join, a mistyped alias or a broken
 * aggregate only surfaces when the database actually runs the statement. These
 * tests therefore go through the DAO interface rather than mocking the
 * {@link EntityManager}.
 *
 * <p>Tests are {@code @Transactional} so each one rolls back. Seed rows from
 * {@code data-hsqldb.sql} are always present and other seed maps may be
 * public, so assertions are scoped to the account ids created by the fixture
 * instead of absolute result-set sizes.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
class MindmapManagerSpamUserQueryIntegrationTest {

    /** Threshold used by most tests; fixtures are built around this boundary. */
    private static final int THRESHOLD = 3;

    /** Wide enough that every freshly created fixture account is inside it. */
    private static final int MONTHS_BACK = 6;

    @Autowired
    private MindmapManager mindmapManager;

    @PersistenceContext
    private EntityManager entityManager;

    // --------------------------------------------------------------- helpers

    /**
     * Creates {@code count} public, spam-flagged mindmaps for {@code creator}.
     */
    private void persistPublicSpamMaps(@NotNull Account creator, @NotNull String titlePrefix, int count) {
        for (int i = 0; i < count; i++) {
            final Mindmap map = persistMindmap(entityManager, creator, titlePrefix + "-" + i, true);
            markAsSpam(entityManager, map, SpamStrategyType.KEYWORD_PATTERN, 1);
        }
    }

    /**
     * Creates {@code count} private, spam-flagged mindmaps for {@code creator}.
     * These must stay invisible to every query in this family.
     */
    private void persistPrivateSpamMaps(@NotNull Account creator, @NotNull String titlePrefix, int count) {
        for (int i = 0; i < count; i++) {
            final Mindmap map = persistMindmap(entityManager, creator, titlePrefix + "-" + i, false);
            markAsSpam(entityManager, map, SpamStrategyType.KEYWORD_PATTERN, 1);
        }
    }

    @NotNull
    private static Set<Integer> userIds(@NotNull List<SpamUserResult> results) {
        return results.stream().map(r -> r.getUser().getId()).collect(Collectors.toSet());
    }

    @NotNull
    private static List<Integer> orderedUserIds(@NotNull List<SpamUserResult> results) {
        return results.stream().map(r -> r.getUser().getId()).collect(Collectors.toList());
    }

    @NotNull
    private static Optional<SpamUserResult> resultFor(@NotNull List<SpamUserResult> results,
                                                      @NotNull Account account) {
        return results.stream().filter(r -> r.getUser().getId() == account.getId()).findFirst();
    }

    // ---------------------------------------------- findUsersWithSpamMindmaps

    @Test
    @DisplayName("findUsersWithSpamMindmaps(): the HAVING boundary is >=, so exactly-threshold users are returned")
    void findUsersWithSpamMindmapsIncludesExactlyThresholdUsers() {
        final Account atThreshold = persistAccount(entityManager, "spam-at-threshold");
        persistPublicSpamMaps(atThreshold, "At threshold", THRESHOLD);

        final Account belowThreshold = persistAccount(entityManager, "spam-below-threshold");
        persistPublicSpamMaps(belowThreshold, "Below threshold", THRESHOLD - 1);

        final Account aboveThreshold = persistAccount(entityManager, "spam-above-threshold");
        persistPublicSpamMaps(aboveThreshold, "Above threshold", THRESHOLD + 2);

        final List<SpamUserResult> results = mindmapManager.findUsersWithSpamMindmaps(THRESHOLD);
        final Set<Integer> ids = userIds(results);

        assertTrue(ids.contains(atThreshold.getId()),
                "a user with exactly the threshold count must be returned (HAVING COUNT >= :spamThreshold)");
        assertTrue(ids.contains(aboveThreshold.getId()), "a user above the threshold must be returned");
        assertFalse(ids.contains(belowThreshold.getId()), "a user below the threshold must not be returned");

        assertEquals(THRESHOLD, resultFor(results, atThreshold).orElseThrow().getSpamCount(),
                "the projected spam count must be the number of public spam maps");
        assertEquals(THRESHOLD + 2L, resultFor(results, aboveThreshold).orElseThrow().getSpamCount());
    }

    @Test
    @DisplayName("findUsersWithSpamMindmaps(): only public AND spam-detected maps count towards the threshold")
    void findUsersWithSpamMindmapsCountsOnlyPublicSpamMaps() {
        // Flagged as spam, but private: excluded by "m.isPublic = true".
        final Account privateSpammer = persistAccount(entityManager, "spam-private");
        persistPrivateSpamMaps(privateSpammer, "Private spam", THRESHOLD + 2);

        // Public, but scanned clean: excluded by "s.spamDetected = true".
        final Account publicCleanUser = persistAccount(entityManager, "spam-public-clean");
        for (int i = 0; i < THRESHOLD + 2; i++) {
            final Mindmap map = persistMindmap(entityManager, publicCleanUser, "Public clean " + i, true);
            markAsScannedClean(entityManager, map, 1);
        }

        // Public with no spam-info row at all: dropped by the inner JOIN.
        final Account neverScannedUser = persistAccount(entityManager, "spam-never-scanned");
        for (int i = 0; i < THRESHOLD + 2; i++) {
            persistMindmap(entityManager, neverScannedUser, "Public unscanned " + i, true);
        }

        // A mixed user: enough public spam maps to qualify, plus noise that must
        // not inflate the projected count.
        final Account mixed = persistAccount(entityManager, "spam-mixed");
        persistPublicSpamMaps(mixed, "Mixed public spam", THRESHOLD);
        persistPrivateSpamMaps(mixed, "Mixed private spam", 1);
        final Mindmap mixedPublicClean = persistMindmap(entityManager, mixed, "Mixed public clean", true);
        markAsScannedClean(entityManager, mixedPublicClean, 1);

        final List<SpamUserResult> results = mindmapManager.findUsersWithSpamMindmaps(THRESHOLD);
        final Set<Integer> ids = userIds(results);

        assertFalse(ids.contains(privateSpammer.getId()), "private spam maps must not count (m.isPublic = true)");
        assertFalse(ids.contains(publicCleanUser.getId()),
                "public maps scanned clean must not count (s.spamDetected = true)");
        assertFalse(ids.contains(neverScannedUser.getId()),
                "public maps without a spam-info row must not count (JOIN m.spamInfo is an inner join)");

        assertTrue(ids.contains(mixed.getId()), "the mixed user reaches the threshold on public spam maps alone");
        assertEquals(THRESHOLD, resultFor(results, mixed).orElseThrow().getSpamCount(),
                "neither the private spam map nor the clean public map may inflate the spam count");
    }

    @Test
    @DisplayName("findUsersWithSpamMindmaps(): has no date filter, so an old account with fresh spam is returned")
    void findUsersWithSpamMindmapsIgnoresAccountAge() {
        final Account oldAccount = persistAccount(entityManager, "spam-old-no-date-filter", 400);
        persistPublicSpamMaps(oldAccount, "Old account spam", THRESHOLD);

        assertTrue(userIds(mindmapManager.findUsersWithSpamMindmaps(THRESHOLD)).contains(oldAccount.getId()),
                "the single-argument overload must not filter on the creator's creation date");
    }

    // ------------------------------ findUsersWithSpamMindaps(threshold, months)

    @Test
    @DisplayName("findUsersWithSpamMindaps(threshold, monthsBack): filters the creator's creation date, not the map's")
    void findUsersWithSpamMindapsFiltersOnCreatorCreationDate() {
        // Account created well outside the window, but every spam map is fresh.
        final Account oldAccount = persistAccount(entityManager, "spam-old-creator", 400);
        persistPublicSpamMaps(oldAccount, "Fresh spam from old account", THRESHOLD + 2);

        final Account recentAccount = persistAccount(entityManager, "spam-recent-creator", 1);
        persistPublicSpamMaps(recentAccount, "Fresh spam from recent account", THRESHOLD);

        final List<SpamUserResult> windowed = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK);
        final Set<Integer> windowedIds = userIds(windowed);

        assertTrue(windowedIds.contains(recentAccount.getId()),
                "an account created inside the window must be returned");
        assertFalse(windowedIds.contains(oldAccount.getId()),
                "monthsBack filters c.creationDate, so an old account with fresh spam maps must be excluded");

        // The same old account IS visible to the overload without a date filter,
        // which proves the exclusion came from the cutoff and not from the maps.
        assertTrue(userIds(mindmapManager.findUsersWithSpamMindmaps(THRESHOLD)).contains(oldAccount.getId()),
                "the old account's spam maps do qualify; only the creation-date cutoff excluded it");
    }

    @Test
    @DisplayName("findUsersWithSpamMindaps(threshold, monthsBack): >= boundary holds and private spam is ignored")
    void findUsersWithSpamMindapsAppliesThresholdBoundaryAndPublicFilter() {
        final Account atThreshold = persistAccount(entityManager, "windowed-at-threshold");
        persistPublicSpamMaps(atThreshold, "Windowed at threshold", THRESHOLD);

        final Account belowThreshold = persistAccount(entityManager, "windowed-below-threshold");
        persistPublicSpamMaps(belowThreshold, "Windowed below threshold", THRESHOLD - 1);

        final Account privateSpammer = persistAccount(entityManager, "windowed-private");
        persistPrivateSpamMaps(privateSpammer, "Windowed private spam", THRESHOLD + 2);

        final List<SpamUserResult> results = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK);
        final Set<Integer> ids = userIds(results);

        assertTrue(ids.contains(atThreshold.getId()), "exactly the threshold count is enough");
        assertFalse(ids.contains(belowThreshold.getId()), "one below the threshold is not enough");
        assertFalse(ids.contains(privateSpammer.getId()), "private spam maps must not count");
        assertEquals(THRESHOLD, resultFor(results, atThreshold).orElseThrow().getSpamCount());
    }

    // ---------------------------- findUsersWithSpamMindaps(.., offset, limit)

    @Test
    @DisplayName("findUsersWithSpamMindaps(.., offset, limit): pages over an id-ordered, non-overlapping result set")
    void findUsersWithSpamMindapsIsPagedAndOrderedById() {
        final Account first = persistAccount(entityManager, "paged-spammer-a");
        persistPublicSpamMaps(first, "Paged spam a", THRESHOLD);
        final Account second = persistAccount(entityManager, "paged-spammer-b");
        persistPublicSpamMaps(second, "Paged spam b", THRESHOLD);
        final Account third = persistAccount(entityManager, "paged-spammer-c");
        persistPublicSpamMaps(third, "Paged spam c", THRESHOLD);

        final List<SpamUserResult> all = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK, 0, 1000);
        assertTrue(userIds(all).containsAll(Set.of(first.getId(), second.getId(), third.getId())),
                "all three fixture spammers should be in the unpaged window");

        // ORDER BY c.id must be ascending.
        final List<Integer> orderedIds = orderedUserIds(all);
        assertEquals(orderedIds.stream().sorted().collect(Collectors.toList()), orderedIds,
                "results must be ordered by the creator id, ascending");

        final List<SpamUserResult> page = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK, 0, 2);
        assertEquals(2, page.size(), "limit must cap the page size");
        assertEquals(orderedIds.subList(0, 2), orderedUserIds(page),
                "the first page must be the first rows of the ordered result set");

        final List<SpamUserResult> nextPage = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK, 2, 2);
        final Set<Integer> firstPageIds = userIds(page);
        assertTrue(nextPage.stream().noneMatch(r -> firstPageIds.contains(r.getUser().getId())),
                "offset must not re-return rows already served on the first page");
    }

    @Test
    @DisplayName("findUsersWithSpamMindaps(.., offset, limit): an offset past the end yields an empty page")
    void findUsersWithSpamMindapsOffsetPastEndIsEmpty() {
        final Account spammer = persistAccount(entityManager, "paged-offset-past-end");
        persistPublicSpamMaps(spammer, "Offset past end", THRESHOLD);

        final int total = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK, 0, 1000).size();

        assertTrue(mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK, total + 5, 10).isEmpty(),
                "paging beyond the last row must return an empty list rather than fail");
    }

    // ------------------------------------ findUsersWithSpamMindapsCursor(..)

    @Test
    @DisplayName("findUsersWithSpamMindapsCursor(): a null cursor starts from the beginning, a cursor skips past it")
    void findUsersWithSpamMindapsCursorPagesByKeyset() {
        final Account first = persistAccount(entityManager, "cursor-spammer-a");
        persistPublicSpamMaps(first, "Cursor spam a", THRESHOLD);
        final Account second = persistAccount(entityManager, "cursor-spammer-b");
        persistPublicSpamMaps(second, "Cursor spam b", THRESHOLD);

        final List<SpamUserResult> fromStart =
                mindmapManager.findUsersWithSpamMindapsCursor(THRESHOLD, MONTHS_BACK, null, 1000);
        assertTrue(userIds(fromStart).containsAll(Set.of(first.getId(), second.getId())),
                "a null lastUserId must return from the start (:lastUserId IS NULL branch)");

        final int firstId = fromStart.get(0).getUser().getId();
        final List<SpamUserResult> afterFirst =
                mindmapManager.findUsersWithSpamMindapsCursor(THRESHOLD, MONTHS_BACK, firstId, 1000);

        assertFalse(userIds(afterFirst).contains(firstId),
                "passing the first result's id as the cursor must exclude it (c.id > :lastUserId)");
        assertTrue(afterFirst.stream().allMatch(r -> r.getUser().getId() > firstId),
                "every row after the cursor must have a strictly greater creator id");
        assertEquals(fromStart.size() - 1, afterFirst.size(),
                "advancing the cursor by one row must drop exactly that row");
    }

    @Test
    @DisplayName("findUsersWithSpamMindapsCursor(): honours the limit and walks the result set one row at a time")
    void findUsersWithSpamMindapsCursorHonoursLimit() {
        final Account first = persistAccount(entityManager, "cursor-limit-a");
        persistPublicSpamMaps(first, "Cursor limit a", THRESHOLD);
        final Account second = persistAccount(entityManager, "cursor-limit-b");
        persistPublicSpamMaps(second, "Cursor limit b", THRESHOLD);
        final Account third = persistAccount(entityManager, "cursor-limit-c");
        persistPublicSpamMaps(third, "Cursor limit c", THRESHOLD);

        final List<Integer> expected = orderedUserIds(
                mindmapManager.findUsersWithSpamMindapsCursor(THRESHOLD, MONTHS_BACK, null, 1000));

        final List<Integer> walked = new ArrayList<>();
        Integer cursor = null;
        while (true) {
            final List<SpamUserResult> batch =
                    mindmapManager.findUsersWithSpamMindapsCursor(THRESHOLD, MONTHS_BACK, cursor, 1);
            assertTrue(batch.size() <= 1, "limit = 1 must never return more than one row");
            if (batch.isEmpty()) {
                break;
            }
            cursor = batch.get(0).getUser().getId();
            walked.add(cursor);
        }

        assertEquals(expected, walked,
                "keyset paging one row at a time must reproduce the full ordered result set");
    }

    @Test
    @DisplayName("findUsersWithSpamMindapsCursor(): also applies the creation-date cutoff and the public/spam filters")
    void findUsersWithSpamMindapsCursorAppliesFilters() {
        final Account oldAccount = persistAccount(entityManager, "cursor-old-creator", 400);
        persistPublicSpamMaps(oldAccount, "Cursor old creator spam", THRESHOLD + 2);

        final Account privateSpammer = persistAccount(entityManager, "cursor-private");
        persistPrivateSpamMaps(privateSpammer, "Cursor private spam", THRESHOLD + 2);

        final Set<Integer> ids =
                userIds(mindmapManager.findUsersWithSpamMindapsCursor(THRESHOLD, MONTHS_BACK, null, 1000));

        assertFalse(ids.contains(oldAccount.getId()), "the cursor variant must honour the creation-date cutoff");
        assertFalse(ids.contains(privateSpammer.getId()), "the cursor variant must ignore private spam maps");
    }

    // ------------------------------------------ countUsersWithSpamMindaps(..)

    @Test
    @DisplayName("countUsersWithSpamMindaps() agrees with the size of the matching find call")
    void countUsersWithSpamMindapsAgreesWithFind() {
        final Account qualifying = persistAccount(entityManager, "count-spammer-a");
        persistPublicSpamMaps(qualifying, "Count spam a", THRESHOLD);
        final Account alsoQualifying = persistAccount(entityManager, "count-spammer-b");
        persistPublicSpamMaps(alsoQualifying, "Count spam b", THRESHOLD + 1);

        // Noise that must be invisible to both queries.
        final Account belowThreshold = persistAccount(entityManager, "count-below-threshold");
        persistPublicSpamMaps(belowThreshold, "Count below threshold", THRESHOLD - 1);
        final Account oldAccount = persistAccount(entityManager, "count-old-creator", 400);
        persistPublicSpamMaps(oldAccount, "Count old creator", THRESHOLD + 2);

        final List<SpamUserResult> found = mindmapManager.findUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK);
        final long counted = mindmapManager.countUsersWithSpamMindaps(THRESHOLD, MONTHS_BACK);

        assertEquals(found.size(), counted,
                "the count query must agree with the list query for the same arguments");

        final Set<Integer> ids = userIds(found);
        assertTrue(ids.containsAll(Set.of(qualifying.getId(), alsoQualifying.getId())),
                "both qualifying fixture accounts must be listed");
        assertFalse(ids.contains(belowThreshold.getId()), "an account below the threshold must not be listed");
        assertFalse(ids.contains(oldAccount.getId()), "an account older than the cutoff must not be listed");
    }

    @Test
    @DisplayName("countUsersWithSpamMindaps() returns zero when no account reaches an unreachable threshold")
    void countUsersWithSpamMindapsReturnsZeroForUnreachableThreshold() {
        final Account spammer = persistAccount(entityManager, "count-unreachable");
        persistPublicSpamMaps(spammer, "Count unreachable", THRESHOLD);

        final int unreachable = 10_000;
        assertEquals(0L, mindmapManager.countUsersWithSpamMindaps(unreachable, MONTHS_BACK),
                "an unreachable threshold must count zero users, not fail on the empty aggregate");
        assertTrue(mindmapManager.findUsersWithSpamMindaps(unreachable, MONTHS_BACK).isEmpty(),
                "and the matching find call must be empty too");
    }
}
