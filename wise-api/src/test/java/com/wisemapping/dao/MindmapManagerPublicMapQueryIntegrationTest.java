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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wisemapping.dao.DaoTestSupport.daysAgo;
import static com.wisemapping.dao.DaoTestSupport.markAsScannedClean;
import static com.wisemapping.dao.DaoTestSupport.persistAccount;
import static com.wisemapping.dao.DaoTestSupport.persistMindmap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the public-mindmap query family on
 * {@link MindmapManager} / {@link MindmapManagerImpl}, executed against the
 * real in-memory HSQLDB schema.
 *
 * <p>Two of these methods ({@code findPublicMindmaps} and
 * {@code countAllPublicMindmaps}) are backed by <em>named queries</em> declared
 * on the {@code Mindmap} entity. A named query that does not resolve, or whose
 * JPQL is malformed, is a runtime failure that a mock-based unit test can never
 * reach, so actually executing them is the point of this class.
 *
 * <p>The remaining methods are inline JPQL whose paging, inclusive date
 * boundary, and {@code LEFT JOIN m.spamInfo} branches are pinned below.
 *
 * <p>Each test is {@code @Transactional} and therefore rolls back. Seed rows
 * from {@code data-hsqldb.sql} are always present and other fixtures may be
 * public, so assertions are expressed either as deltas against a captured
 * baseline or as membership checks over the ids created by the test itself -
 * never as absolute result-set sizes.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
class MindmapManagerPublicMapQueryIntegrationTest {

    /** Large enough to swallow every row the test database can hold. */
    private static final int ALL = 10_000;

    @Autowired
    private MindmapManager mindmapManager;

    @PersistenceContext
    private EntityManager entityManager;

    // ------------------------------------------------ named: findPublicMindmaps

    @Test
    @DisplayName("findPublicMindmaps() named query resolves, returns public maps and never private ones")
    void findPublicMindmapsNamedQueryReturnsOnlyPublicMaps() {
        final long baseline = mindmapManager.findPublicMindmaps().size();

        final Account creator = persistAccount(entityManager, "find-public");
        final Mindmap publicMap = persistMindmap(entityManager, creator, "public map", true);
        final Mindmap privateMap = persistMindmap(entityManager, creator, "private map", false);

        final List<Mindmap> result = mindmapManager.findPublicMindmaps();

        assertEquals(baseline + 1, result.size(),
                "exactly one of the two new maps - the public one - should be returned");
        final Set<Integer> ids = idsOf(result);
        assertTrue(ids.contains(publicMap.getId()), "the public map must be returned");
        assertFalse(ids.contains(privateMap.getId()), "a private map must never be returned");
        assertTrue(result.stream().allMatch(Mindmap::isPublic), "every returned map must be public");
    }

    @Test
    @DisplayName("findPublicMindmaps() hides public maps whose creator is suspended, while findAllPublicMindmaps() still lists them")
    void findPublicMindmapsExcludesSuspendedCreators() {
        final Account suspended = persistAccount(entityManager, "suspended-creator");
        suspended.setSuspended(true);
        entityManager.flush();

        final Mindmap mapOfSuspended = persistMindmap(entityManager, suspended, "map of suspended user", true);

        final Set<Integer> visible = idsOf(mindmapManager.findPublicMindmaps());
        assertFalse(visible.contains(mapOfSuspended.getId()),
                "the named query joins the creator and filters suspended = false");

        // The paged listing has no such filter - pin the asymmetry so that a
        // change on either side is noticed.
        final Set<Integer> allPublic = idsOf(mindmapManager.findAllPublicMindmaps(0, ALL));
        assertTrue(allPublic.contains(mapOfSuspended.getId()),
                "findAllPublicMindmaps() does not filter by creator suspension");
    }

    // ------------------------------------------ named: countAllPublicMindmaps

    @Test
    @DisplayName("countAllPublicMindmaps() named query counts only public maps and agrees with findAllPublicMindmaps()")
    void countAllPublicMindmapsCountsOnlyPublicMaps() {
        final long baseline = mindmapManager.countAllPublicMindmaps();

        final Account creator = persistAccount(entityManager, "count-public");
        persistMindmap(entityManager, creator, "counted public 1", true);
        persistMindmap(entityManager, creator, "counted public 2", true);
        final Mindmap privateMap = persistMindmap(entityManager, creator, "not counted", false);

        assertEquals(baseline + 2, mindmapManager.countAllPublicMindmaps(),
                "only the two public maps should have moved the count");

        final List<Mindmap> all = mindmapManager.findAllPublicMindmaps(0, ALL);
        assertEquals(mindmapManager.countAllPublicMindmaps(), all.size(),
                "the count must agree with the size of the unpaged find");
        assertFalse(idsOf(all).contains(privateMap.getId()), "a private map must never be listed");
    }

    // ------------------------------------------- findAllPublicMindmaps paging

    @Test
    @DisplayName("findAllPublicMindmaps() pages by ascending id: limit 1 yields one row and offset 1 yields a different row")
    void findAllPublicMindmapsPagesByAscendingId() {
        final Account creator = persistAccount(entityManager, "paged-public");
        persistMindmap(entityManager, creator, "paged public 1", true);
        persistMindmap(entityManager, creator, "paged public 2", true);

        final List<Mindmap> firstPage = mindmapManager.findAllPublicMindmaps(0, 1);
        assertEquals(1, firstPage.size(), "a limit of 1 must return a single row");

        final List<Mindmap> secondPage = mindmapManager.findAllPublicMindmaps(1, 1);
        assertEquals(1, secondPage.size(), "there are at least two public maps, so offset 1 must return a row");
        assertNotEquals(firstPage.get(0).getId(), secondPage.get(0).getId(),
                "offset 1 must skip the first row");

        final List<Mindmap> all = mindmapManager.findAllPublicMindmaps(0, ALL);
        assertEquals(firstPage.get(0).getId(), all.get(0).getId(),
                "the first page is the head of the full list");
        assertEquals(secondPage.get(0).getId(), all.get(1).getId(),
                "the second page is the next row of the full list");
        for (int i = 1; i < all.size(); i++) {
            assertTrue(all.get(i - 1).getId() < all.get(i).getId(), "results must be ordered by ascending id");
        }
    }

    // ----------------------------------------------- *AllPublicMindmapsSince

    @Test
    @DisplayName("findAllPublicMindmapsSince() treats the cutoff date as inclusive (>=) and drops older maps")
    void findAllPublicMindmapsSinceCutoffIsInclusive() {
        final Account creator = persistAccount(entityManager, "since-boundary");

        final Calendar boundary = wholeSecondsDaysAgo(10);
        final Mindmap onBoundary = persistPublicMindmapCreatedAt(creator, "created exactly on the cutoff", boundary);
        final Mindmap older = persistPublicMindmapCreatedAt(creator, "created before the cutoff",
                wholeSecondsDaysAgo(20));

        final Set<Integer> atBoundary = idsOf(mindmapManager.findAllPublicMindmapsSince(copyOf(boundary), 0, ALL));
        assertTrue(atBoundary.contains(onBoundary.getId()),
                "a map created exactly on the cutoff must be included - the comparison is >=");
        assertFalse(atBoundary.contains(older.getId()), "a map created before the cutoff must be excluded");

        final Calendar justAfterBoundary = copyOf(boundary);
        justAfterBoundary.add(Calendar.SECOND, 1);
        final Set<Integer> afterBoundary =
                idsOf(mindmapManager.findAllPublicMindmapsSince(justAfterBoundary, 0, ALL));
        assertFalse(afterBoundary.contains(onBoundary.getId()),
                "one second past its creation time the boundary map must drop out");
    }

    @Test
    @DisplayName("countAllPublicMindmapsSince() agrees with findAllPublicMindmapsSince() and ignores private maps")
    void countAllPublicMindmapsSinceAgreesWithFind() {
        final Calendar cutoff = wholeSecondsDaysAgo(5);
        final long baseline = mindmapManager.countAllPublicMindmapsSince(copyOf(cutoff));

        final Account creator = persistAccount(entityManager, "count-since");
        final Mindmap recentPublic = persistPublicMindmapCreatedAt(creator, "recent public", wholeSecondsDaysAgo(1));
        final Mindmap oldPublic = persistPublicMindmapCreatedAt(creator, "old public", wholeSecondsDaysAgo(30));
        final Mindmap recentPrivate = persistMindmap(entityManager, creator, "recent private", false);

        assertEquals(baseline + 1, mindmapManager.countAllPublicMindmapsSince(copyOf(cutoff)),
                "only the recent public map falls inside the window");

        final List<Mindmap> found = mindmapManager.findAllPublicMindmapsSince(copyOf(cutoff), 0, ALL);
        assertEquals(mindmapManager.countAllPublicMindmapsSince(copyOf(cutoff)), found.size(),
                "the count must agree with the size of the unpaged find");

        final Set<Integer> ids = idsOf(found);
        assertTrue(ids.contains(recentPublic.getId()), "the recent public map must be returned");
        assertFalse(ids.contains(oldPublic.getId()), "maps created before the cutoff must be excluded");
        assertFalse(ids.contains(recentPrivate.getId()), "private maps must never appear");
    }

    @Test
    @DisplayName("findAllPublicMindmapsSince() pages within the date window")
    void findAllPublicMindmapsSincePages() {
        final Account creator = persistAccount(entityManager, "paged-since");
        final Calendar cutoff = wholeSecondsDaysAgo(3);
        persistPublicMindmapCreatedAt(creator, "in window 1", wholeSecondsDaysAgo(2));
        persistPublicMindmapCreatedAt(creator, "in window 2", wholeSecondsDaysAgo(1));

        final List<Mindmap> first = mindmapManager.findAllPublicMindmapsSince(copyOf(cutoff), 0, 1);
        final List<Mindmap> second = mindmapManager.findAllPublicMindmapsSince(copyOf(cutoff), 1, 1);
        assertEquals(1, first.size(), "a limit of 1 must return a single row");
        assertEquals(1, second.size(), "two maps are inside the window, so offset 1 must return a row");
        assertNotEquals(first.get(0).getId(), second.get(0).getId(), "offset 1 must skip the first row");
    }

    // ------------------------------- *PublicMindmapsNeedingSpamDetection

    @Test
    @DisplayName("findPublicMindmapsNeedingSpamDetection() returns maps with no spam-info row or a stale version, and skips up-to-date ones")
    void findPublicMindmapsNeedingSpamDetectionCoversAllSpamInfoStates() {
        final int currentVersion = 2;
        final Calendar cutoff = wholeSecondsDaysAgo(7);

        final Account creator = persistAccount(entityManager, "needs-spam");

        // 1. public, never scanned at all -> the "s.spamDetectionVersion IS NULL" arm.
        final Mindmap neverScanned = persistPublicMindmapCreatedAt(creator, "never scanned",
                wholeSecondsDaysAgo(3));
        // 2. public, scanned by an older detector -> the "< :currentVersion" arm.
        final Mindmap staleScan = markAsScannedClean(entityManager,
                persistPublicMindmapCreatedAt(creator, "stale scan", wholeSecondsDaysAgo(2)), 1);
        // 3. public, already scanned at the current version -> must be skipped.
        final Mindmap upToDate = markAsScannedClean(entityManager,
                persistPublicMindmapCreatedAt(creator, "up to date", wholeSecondsDaysAgo(2)), currentVersion);
        // 4. private and never scanned -> must be skipped regardless of spam state.
        final Mindmap privateMap = persistMindmap(entityManager, creator, "private never scanned", false);
        // 5. public and never scanned but created before the cutoff -> must be skipped.
        final Mindmap tooOld = persistPublicMindmapCreatedAt(creator, "too old", wholeSecondsDaysAgo(40));

        final List<Mindmap> result =
                mindmapManager.findPublicMindmapsNeedingSpamDetection(copyOf(cutoff), currentVersion, 0, ALL);
        final Set<Integer> ids = idsOf(result);

        assertTrue(ids.contains(neverScanned.getId()), "a public map with no spam-info row needs detection");
        assertTrue(ids.contains(staleScan.getId()), "a public map scanned at an older version needs a rescan");
        assertFalse(ids.contains(upToDate.getId()), "a map already scanned at the current version must be skipped");
        assertFalse(ids.contains(privateMap.getId()), "private maps must never appear");
        assertFalse(ids.contains(tooOld.getId()), "maps created before the cutoff must be skipped");
    }

    @Test
    @DisplayName("countPublicMindmapsNeedingSpamDetection() agrees with the size of the matching find")
    void countPublicMindmapsNeedingSpamDetectionAgreesWithFind() {
        final int currentVersion = 2;
        final Calendar cutoff = wholeSecondsDaysAgo(7);
        final long baseline =
                mindmapManager.countPublicMindmapsNeedingSpamDetection(copyOf(cutoff), currentVersion);

        final Account creator = persistAccount(entityManager, "count-needs-spam");
        persistPublicMindmapCreatedAt(creator, "never scanned", wholeSecondsDaysAgo(3));
        markAsScannedClean(entityManager,
                persistPublicMindmapCreatedAt(creator, "stale scan", wholeSecondsDaysAgo(2)), 1);
        markAsScannedClean(entityManager,
                persistPublicMindmapCreatedAt(creator, "up to date", wholeSecondsDaysAgo(2)), currentVersion);
        persistMindmap(entityManager, creator, "private never scanned", false);
        persistPublicMindmapCreatedAt(creator, "too old", wholeSecondsDaysAgo(40));

        final long count = mindmapManager.countPublicMindmapsNeedingSpamDetection(copyOf(cutoff), currentVersion);
        assertEquals(baseline + 2, count, "only the unscanned and the stale map should have moved the count");
        assertEquals(count,
                mindmapManager.findPublicMindmapsNeedingSpamDetection(copyOf(cutoff), currentVersion, 0, ALL).size(),
                "the count must agree with the size of the unpaged find");
    }

    @Test
    @DisplayName("findPublicMindmapsNeedingSpamDetection() treats the cutoff as inclusive (>=) and pages the result")
    void findPublicMindmapsNeedingSpamDetectionCutoffIsInclusiveAndPages() {
        final int currentVersion = 2;
        final Account creator = persistAccount(entityManager, "needs-spam-boundary");

        final Calendar boundary = wholeSecondsDaysAgo(6);
        final Mindmap onBoundary = persistPublicMindmapCreatedAt(creator, "on the cutoff", boundary);
        final Mindmap afterBoundary = persistPublicMindmapCreatedAt(creator, "after the cutoff",
                wholeSecondsDaysAgo(1));

        final Set<Integer> atBoundary = idsOf(mindmapManager
                .findPublicMindmapsNeedingSpamDetection(copyOf(boundary), currentVersion, 0, ALL));
        assertTrue(atBoundary.contains(onBoundary.getId()),
                "a map created exactly on the cutoff must be included - the comparison is >=");
        assertTrue(atBoundary.contains(afterBoundary.getId()), "a newer map must also be included");

        final Calendar justAfter = copyOf(boundary);
        justAfter.add(Calendar.SECOND, 1);
        assertFalse(idsOf(mindmapManager
                        .findPublicMindmapsNeedingSpamDetection(justAfter, currentVersion, 0, ALL))
                        .contains(onBoundary.getId()),
                "one second past its creation time the boundary map must drop out");

        final List<Mindmap> first = mindmapManager
                .findPublicMindmapsNeedingSpamDetection(copyOf(boundary), currentVersion, 0, 1);
        final List<Mindmap> second = mindmapManager
                .findPublicMindmapsNeedingSpamDetection(copyOf(boundary), currentVersion, 1, 1);
        assertEquals(1, first.size(), "a limit of 1 must return a single row");
        assertEquals(1, second.size(), "at least two maps match, so offset 1 must return a row");
        assertNotEquals(first.get(0).getId(), second.get(0).getId(), "offset 1 must skip the first row");
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Persists a public mindmap and then forces its creation time, which
     * {@code DaoTestSupport.persistMindmap} always sets to "now". Milliseconds
     * are already zeroed by {@link #wholeSecondsDaysAgo(int)} so that the
     * inclusive-boundary assertions are immune to timestamp rounding.
     */
    @NotNull
    private Mindmap persistPublicMindmapCreatedAt(@NotNull Account creator,
                                                  @NotNull String title,
                                                  @NotNull Calendar createdAt) {
        final Mindmap mindmap = persistMindmap(entityManager, creator, title, true);
        mindmap.setCreationTime(copyOf(createdAt));
        entityManager.flush();
        return mindmap;
    }

    @NotNull
    private static Calendar wholeSecondsDaysAgo(int days) {
        final Calendar result = daysAgo(days);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    @NotNull
    private static Calendar copyOf(@NotNull Calendar source) {
        return (Calendar) source.clone();
    }

    @NotNull
    private static Set<Integer> idsOf(@NotNull List<Mindmap> mindmaps) {
        return mindmaps.stream().map(Mindmap::getId).collect(Collectors.toSet());
    }
}
