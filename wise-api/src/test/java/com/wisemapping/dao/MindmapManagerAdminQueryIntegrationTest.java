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
import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.model.Account;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
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

import static com.wisemapping.dao.DaoTestSupport.markAsScannedClean;
import static com.wisemapping.dao.DaoTestSupport.markAsSpam;
import static com.wisemapping.dao.DaoTestSupport.persistAccount;
import static com.wisemapping.dao.DaoTestSupport.persistHistory;
import static com.wisemapping.dao.DaoTestSupport.persistMindmap;
import static com.wisemapping.dao.DaoTestSupport.uniqueEmail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the admin-facing listing, search, history-trimming and
 * collaboration-removal methods of {@link MindmapManagerImpl}, executed against
 * the real in-memory HSQLDB schema.
 *
 * <p>These methods are reached from {@code AdminController} and the history
 * purge scheduler. They are built from named queries, dynamically concatenated
 * JPQL and Criteria API trees, none of which can be validated by a test that
 * mocks the {@link EntityManager} (as the pre-existing
 * {@code MindmapManagerImplTest} does) -- only a real database round trip
 * catches a bad join, a typo in a query string, or a named query that does not
 * resolve.
 *
 * <p>Tests are {@code @Transactional} so each one rolls back. The seed rows in
 * {@code data-hsqldb.sql} are always present, so assertions are written either
 * as deltas against a captured baseline, or against a search token unique to
 * this test run so that the matching result set contains only our fixtures.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
class MindmapManagerAdminQueryIntegrationTest {

    /** Big enough to swallow the seed data plus anything a test adds. */
    private static final int ALL = 100_000;

    @Autowired
    private MindmapManager mindmapManager;

    @PersistenceContext
    private EntityManager entityManager;

    // ------------------------------------------------------------ unpaged list

    @Test
    @DisplayName("getAllMindmaps() returns every mindmap, including freshly persisted ones")
    void getAllMindmapsReturnsNewlyCreatedMaps() {
        // Asserted as a delta: the baseline is whatever data-hsqldb.sql (and any
        // sibling test in the same JVM) happens to leave behind.
        final List<Mindmap> baseline = mindmapManager.getAllMindmaps();

        final Account creator = persistAccount(entityManager, "all-maps");
        final Mindmap first = persistMindmap(entityManager, creator, "all-maps-one", false);
        final Mindmap second = persistMindmap(entityManager, creator, "all-maps-two", true);

        final List<Mindmap> after = mindmapManager.getAllMindmaps();
        assertEquals(baseline.size() + 2, after.size(), "both new maps must show up");

        final Set<Integer> ids = idsOf(after);
        assertTrue(ids.contains(first.getId()));
        assertTrue(ids.contains(second.getId()));
    }

    @Test
    @DisplayName("countAllMindmaps() agrees with getAllMindmaps() and grows by one per map")
    void countAllMindmapsTracksTheListSize() {
        final long baseline = mindmapManager.countAllMindmaps();
        assertEquals(mindmapManager.getAllMindmaps().size(), baseline);

        final Account creator = persistAccount(entityManager, "count-maps");
        persistMindmap(entityManager, creator, "count-maps-one", false);

        assertEquals(baseline + 1, mindmapManager.countAllMindmaps());
        assertEquals(mindmapManager.getAllMindmaps().size(), mindmapManager.countAllMindmaps());
    }

    // ----------------------------------------------------------------- paging

    @Test
    @DisplayName("getAllMindmaps(offset, limit) honours the window and returns empty past the end")
    void getAllMindmapsPagesTheResultSet() {
        final Account creator = persistAccount(entityManager, "paged-maps");
        persistMindmap(entityManager, creator, "paged-maps-one", false);
        persistMindmap(entityManager, creator, "paged-maps-two", false);
        persistMindmap(entityManager, creator, "paged-maps-three", false);

        final int total = mindmapManager.getAllMindmaps().size();
        assertTrue(total >= 3);

        // The limit caps the page...
        assertEquals(1, mindmapManager.getAllMindmaps(0, 1).size());
        assertEquals(2, mindmapManager.getAllMindmaps(0, 2).size());
        // ...a limit wider than the table returns everything...
        assertEquals(total, mindmapManager.getAllMindmaps(0, ALL).size());
        // ...and the offset really skips rows.
        assertEquals(total - 2, mindmapManager.getAllMindmaps(2, ALL).size());
        assertTrue(mindmapManager.getAllMindmaps(total, ALL).isEmpty(),
                "an offset at the end of the table yields no rows");
    }

    // ----------------------------------------------------------------- search

    @Test
    @DisplayName("searchMindmaps() matches on the title")
    void searchMindmapsMatchesOnTitle() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-title");
        final Mindmap matching = persistMindmapWith(creator, "Roadmap " + token, "unrelated description", false);
        final Mindmap other = persistMindmapWith(creator, "Roadmap untouched", "unrelated description", false);

        final List<Mindmap> found = mindmapManager.searchMindmaps(token, null, 0, ALL);
        assertEquals(Set.of(matching.getId()), idsOf(found));
        assertFalse(idsOf(found).contains(other.getId()));
    }

    @Test
    @DisplayName("searchMindmaps() also matches on the description, not only the title")
    void searchMindmapsMatchesOnDescription() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-desc");
        final Mindmap matching = persistMindmapWith(creator, "Title without the term",
                "A description mentioning " + token, false);

        final List<Mindmap> found = mindmapManager.searchMindmaps(token, null, 0, ALL);
        assertEquals(Set.of(matching.getId()), idsOf(found),
                "the OR arm against m.description must match too");
    }

    @Test
    @DisplayName("searchMindmaps() is case-insensitive on both sides of the LIKE")
    void searchMindmapsIsCaseInsensitive() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-case");
        final Mindmap titleMatch = persistMindmapWith(creator, "MiXeD " + token.toUpperCase(), "no term here", false);
        final Mindmap descMatch = persistMindmapWith(creator, "no term here",
                "Lower " + token.toLowerCase(), false);

        final Set<Integer> expected = Set.of(titleMatch.getId(), descMatch.getId());
        assertEquals(expected, idsOf(mindmapManager.searchMindmaps(token.toLowerCase(), null, 0, ALL)));
        assertEquals(expected, idsOf(mindmapManager.searchMindmaps(token.toUpperCase(), null, 0, ALL)));
    }

    @Test
    @DisplayName("searchMindmaps(null, ...) binds no search parameter and returns every mindmap")
    void searchMindmapsWithoutATermReturnsEverything() {
        final Account creator = persistAccount(entityManager, "search-null");
        final Mindmap mindmap = persistMindmap(entityManager, creator, "search-null-one", false);

        final long total = mindmapManager.countAllMindmaps();

        final List<Mindmap> allViaNull = mindmapManager.searchMindmaps(null, null, 0, ALL);
        assertEquals(total, allViaNull.size());
        assertTrue(idsOf(allViaNull).contains(mindmap.getId()));

        // A blank term takes the same "no filter" branch as null.
        assertEquals(total, mindmapManager.searchMindmaps("   ", null, 0, ALL).size());
        assertEquals(total, mindmapManager.countMindmapsBySearch(null, null));
        assertEquals(total, mindmapManager.countMindmapsBySearch("   ", null));
    }

    @Test
    @DisplayName("searchMindmaps() filterPublic selects public, private, or both when null")
    void searchMindmapsFiltersByPublicFlag() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-public");
        final Mindmap publicMap = persistMindmapWith(creator, "Public " + token, "d", true);
        final Mindmap privateMap = persistMindmapWith(creator, "Private " + token, "d", false);

        assertEquals(Set.of(publicMap.getId()),
                idsOf(mindmapManager.searchMindmaps(token, Boolean.TRUE, 0, ALL)));
        assertEquals(Set.of(privateMap.getId()),
                idsOf(mindmapManager.searchMindmaps(token, Boolean.FALSE, 0, ALL)));
        assertEquals(Set.of(publicMap.getId(), privateMap.getId()),
                idsOf(mindmapManager.searchMindmaps(token, null, 0, ALL)));
    }

    @Test
    @DisplayName("searchMindmaps() agrees across the spam-aware and spam-unaware overloads")
    void searchMindmapsOverloadsAgreeWhenNoSpamFilterIsApplied() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-overloads");
        final Mindmap one = persistMindmapWith(creator, "Overload-probe " + token, "d", false);
        final Mindmap two = persistMindmapWith(creator, "Overload-probe " + token + " again", "d", true);

        final Set<Integer> expected = Set.of(one.getId(), two.getId());

        // The two overloads build different JPQL (the spam-aware one adds the
        // LEFT JOIN FETCH on spamInfo), so they are worth pinning against each
        // other: with filterSpam=null they must select exactly the same rows.
        assertEquals(expected, idsOf(mindmapManager.searchMindmaps(token, null, 0, ALL)));
        assertEquals(expected, idsOf(mindmapManager.searchMindmaps(token, null, null, 0, ALL)));

        assertEquals(2L, mindmapManager.countMindmapsBySearch(token, null));
        assertEquals(2L, mindmapManager.countMindmapsBySearch(token, null, null));
    }

    @Test
    @DisplayName("searchMindmaps(offset, limit) pages the matching rows")
    void searchMindmapsPagesTheMatches() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-page");
        persistMindmapWith(creator, "Paged " + token + " a", "d", false);
        persistMindmapWith(creator, "Paged " + token + " b", "d", false);
        persistMindmapWith(creator, "Paged " + token + " c", "d", false);

        assertEquals(3, mindmapManager.searchMindmaps(token, null, 0, ALL).size());
        assertEquals(2, mindmapManager.searchMindmaps(token, null, 0, 2).size());
        assertEquals(1, mindmapManager.searchMindmaps(token, null, 2, 2).size());
        assertTrue(mindmapManager.searchMindmaps(token, null, 3, 2).isEmpty());
    }

    @Test
    @DisplayName("countMindmapsBySearch() agrees with searchMindmaps() for the same arguments")
    void countMindmapsBySearchAgreesWithTheListing() {
        final String token = uniqueToken();
        final Account creator = persistAccount(entityManager, "search-count");
        persistMindmapWith(creator, "Counted " + token, "d", true);
        persistMindmapWith(creator, "Another title", "Counted " + token, false);
        persistMindmapWith(creator, "Counted " + token + " third", "d", false);

        for (Boolean filterPublic : new Boolean[]{null, Boolean.TRUE, Boolean.FALSE}) {
            assertEquals(
                    mindmapManager.searchMindmaps(token, filterPublic, 0, ALL).size(),
                    mindmapManager.countMindmapsBySearch(token, filterPublic),
                    "count must match the listing size for filterPublic=" + filterPublic);
        }

        assertEquals(3L, mindmapManager.countMindmapsBySearch(token, null));
        assertEquals(1L, mindmapManager.countMindmapsBySearch(token, Boolean.TRUE));
        assertEquals(2L, mindmapManager.countMindmapsBySearch(token, Boolean.FALSE));
        assertEquals(0L, mindmapManager.countMindmapsBySearch(token + "-nope", null));
    }

    // ------------------------------------------------------------ spam filter

    @Test
    @DisplayName("getAllMindmaps(filterSpam=true) returns only maps flagged as spam")
    void getAllMindmapsFiltersToSpam() {
        final SpamFixture fixture = new SpamFixture();

        final Set<Integer> found = idsOf(mindmapManager.getAllMindmaps(Boolean.TRUE, 0, ALL));
        assertTrue(found.contains(fixture.spam), "the spam-flagged map must be returned");
        assertFalse(found.contains(fixture.scannedClean), "a scanned-but-clean map is not spam");
        assertFalse(found.contains(fixture.neverScanned), "a map with no spam-info row is not spam");
    }

    @Test
    @DisplayName("getAllMindmaps(filterSpam=false) includes maps with NO spam-info row at all")
    void getAllMindmapsFiltersToNonSpamIncludingUnscanned() {
        final SpamFixture fixture = new SpamFixture();

        final Set<Integer> found = idsOf(mindmapManager.getAllMindmaps(Boolean.FALSE, 0, ALL));
        assertFalse(found.contains(fixture.spam), "the spam-flagged map must be excluded");
        assertTrue(found.contains(fixture.scannedClean), "spamDetected = false must be included");
        assertTrue(found.contains(fixture.neverScanned),
                "the isNull(spamJoin) arm must let unscanned maps through");
    }

    @Test
    @DisplayName("getAllMindmaps(filterSpam=null) applies no spam predicate at all")
    void getAllMindmapsWithoutSpamFilterReturnsEverything() {
        final SpamFixture fixture = new SpamFixture();

        final List<Mindmap> found = mindmapManager.getAllMindmaps(null, 0, ALL);
        final Set<Integer> ids = idsOf(found);
        assertTrue(ids.contains(fixture.spam));
        assertTrue(ids.contains(fixture.scannedClean));
        assertTrue(ids.contains(fixture.neverScanned));
        assertEquals(mindmapManager.countAllMindmaps(), found.size());
    }

    @Test
    @DisplayName("getAllMindmaps(filterSpam, offset, limit) honours the paging window")
    void getAllMindmapsWithSpamFilterPages() {
        new SpamFixture();

        final int total = mindmapManager.getAllMindmaps(null, 0, ALL).size();
        assertEquals(1, mindmapManager.getAllMindmaps(null, 0, 1).size());
        assertEquals(total - 1, mindmapManager.getAllMindmaps(null, 1, ALL).size());
        assertTrue(mindmapManager.getAllMindmaps(null, total, ALL).isEmpty());
    }

    // ------------------------------------------------- last modification time

    @Test
    @DisplayName("getMindmapLastModificationTime() returns the stored timestamp for an existing map")
    void getMindmapLastModificationTimeReturnsStoredValue() {
        final Account creator = persistAccount(entityManager, "last-mod");
        final Mindmap mindmap = persistMindmap(entityManager, creator, "last-mod-map", false);

        final Calendar actual = mindmapManager.getMindmapLastModificationTime(mindmap.getId());
        assertNotNull(actual);
        assertTrue(Math.abs(actual.getTimeInMillis() - mindmap.getLastModificationTime().getTimeInMillis()) < 2000L,
                "expected the persisted edition_date, got " + actual.getTime());
    }

    @Test
    @DisplayName("getMindmapLastModificationTime() returns null for an unknown mindmap id")
    void getMindmapLastModificationTimeReturnsNullForUnknownId() {
        assertNull(mindmapManager.getMindmapLastModificationTime(Integer.MAX_VALUE - 7),
                "an empty result list must surface as null, not an exception");
    }

    // ---------------------------------------------------------- history trim

    @Test
    @DisplayName("removeExcessHistoryByMindmapId() keeps the N most recent rows and reports the delete count")
    void removeExcessHistoryKeepsTheMostRecentEntries() {
        final Account creator = persistAccount(entityManager, "history-trim");
        final Mindmap mindmap = persistMindmap(entityManager, creator, "history-trim-map", false);

        // Oldest first; the two youngest (10 and 20 minutes ago) must survive.
        final MindMapHistory oldest = persistHistory(entityManager, mindmap, creator, 50);
        final MindMapHistory older = persistHistory(entityManager, mindmap, creator, 40);
        final MindMapHistory old = persistHistory(entityManager, mindmap, creator, 30);
        final MindMapHistory recent = persistHistory(entityManager, mindmap, creator, 20);
        final MindMapHistory newest = persistHistory(entityManager, mindmap, creator, 10);

        assertEquals(5, historyIdsOf(mindmap.getId()).size(), "fixture must start with 5 rows");

        final int deleted = mindmapManager.removeExcessHistoryByMindmapId(mindmap.getId(), 2);
        assertEquals(3, deleted, "5 rows minus the 2 kept");

        final Set<Integer> remaining = historyIdsOf(mindmap.getId());
        assertEquals(2, remaining.size());
        assertEquals(Set.of(newest.getId(), recent.getId()), remaining,
                "ORDER BY creationTime DESC must keep the newest rows, not an arbitrary pair");
        assertFalse(remaining.contains(old.getId()));
        assertFalse(remaining.contains(older.getId()));
        assertFalse(remaining.contains(oldest.getId()));
    }

    @Test
    @DisplayName("removeExcessHistoryByMindmapId() is a no-op when the row count is within the limit")
    void removeExcessHistoryIsANoOpBelowTheLimit() {
        final Account creator = persistAccount(entityManager, "history-noop");
        final Mindmap mindmap = persistMindmap(entityManager, creator, "history-noop-map", false);
        persistHistory(entityManager, mindmap, creator, 30);
        persistHistory(entityManager, mindmap, creator, 20);
        persistHistory(entityManager, mindmap, creator, 10);

        final Set<Integer> before = historyIdsOf(mindmap.getId());
        assertEquals(3, before.size());

        // Strictly below the cap...
        assertEquals(0, mindmapManager.removeExcessHistoryByMindmapId(mindmap.getId(), 5));
        // ...and exactly at the cap: totalCount <= maxEntries takes the same branch.
        assertEquals(0, mindmapManager.removeExcessHistoryByMindmapId(mindmap.getId(), 3));
        assertEquals(before, historyIdsOf(mindmap.getId()), "nothing may be deleted");
    }

    @Test
    @DisplayName("removeExcessHistoryByMindmapId() only touches the requested mindmap's history")
    void removeExcessHistoryIsScopedToOneMindmap() {
        final Account creator = persistAccount(entityManager, "history-scope");
        final Mindmap target = persistMindmap(entityManager, creator, "history-scope-target", false);
        final Mindmap bystander = persistMindmap(entityManager, creator, "history-scope-bystander", false);

        persistHistory(entityManager, target, creator, 30);
        persistHistory(entityManager, target, creator, 20);
        persistHistory(entityManager, target, creator, 10);
        final Set<Integer> bystanderHistory = Set.of(
                persistHistory(entityManager, bystander, creator, 30).getId(),
                persistHistory(entityManager, bystander, creator, 20).getId());

        assertEquals(2, mindmapManager.removeExcessHistoryByMindmapId(target.getId(), 1));
        assertEquals(1, historyIdsOf(target.getId()).size());
        assertEquals(bystanderHistory, historyIdsOf(bystander.getId()),
                "the other mindmap's history must be untouched");
    }

    @Test
    @DisplayName("removeExcessHistoryByMindmapId() returns 0 for a mindmap with no history")
    void removeExcessHistoryReturnsZeroWithoutHistory() {
        final Account creator = persistAccount(entityManager, "history-empty");
        final Mindmap mindmap = persistMindmap(entityManager, creator, "history-empty-map", false);

        assertEquals(0, mindmapManager.removeExcessHistoryByMindmapId(mindmap.getId(), 2));
        assertTrue(historyIdsOf(mindmap.getId()).isEmpty());
    }

    // ------------------------------------------------- collaboration removal

    @Test
    @DisplayName("removeCollaborator() deletes the collaborator row")
    void removeCollaboratorDeletesTheRow() {
        final Collaborator collaborator = new Collaborator();
        collaborator.setEmail(uniqueEmail("plain-collaborator"));
        collaborator.setCreationDate(Calendar.getInstance());
        entityManager.persist(collaborator);
        entityManager.flush();

        final int id = collaborator.getId();
        assertNotNull(entityManager.find(Collaborator.class, id));

        mindmapManager.removeCollaborator(collaborator);
        entityManager.flush();

        assertNull(entityManager.find(Collaborator.class, id), "the collaborator must be gone");
    }

    @Test
    @DisplayName("removeCollaboration() tolerates a collaboration that is no longer in the database")
    void removeCollaborationToleratesAnAlreadyDeletedRow() {
        // removeCollaboration() is @Transactional(REQUIRES_NEW), so the suspended
        // test transaction's uncommitted rows are invisible to it and
        // entityManager.find(...) returns null. That is exactly the
        // "already deleted by another transaction" path the method is written to
        // absorb, and it must not raise.
        final Collaboration detached = new Collaboration();
        detached.setId(Integer.MAX_VALUE - 11);

        assertDoesNotThrow(() -> mindmapManager.removeCollaboration(detached));
    }

    // ------------------------------------------------------------- fixtures

    /**
     * Three maps with the three possible spam states: flagged, scanned clean,
     * and never scanned (no {@code mindmap_spam_info} row at all).
     */
    private final class SpamFixture {
        private final int spam;
        private final int scannedClean;
        private final int neverScanned;

        private SpamFixture() {
            final Account creator = persistAccount(entityManager, "spam-filter");
            final Mindmap flagged = persistMindmap(entityManager, creator, "spam-filter-flagged", false);
            final Mindmap clean = persistMindmap(entityManager, creator, "spam-filter-clean", false);
            final Mindmap unscanned = persistMindmap(entityManager, creator, "spam-filter-unscanned", false);

            this.spam = markAsSpam(entityManager, flagged, SpamStrategyType.KEYWORD_PATTERN, 1).getId();
            this.scannedClean = markAsScannedClean(entityManager, clean, 1).getId();
            this.neverScanned = unscanned.getId();

            // The Criteria query LEFT JOIN FETCHes spamInfo; make sure Hibernate
            // reads the rows back from the database rather than from the
            // first-level cache snapshot taken before the merge.
            entityManager.flush();
            entityManager.clear();
        }
    }

    /**
     * {@code DaoTestSupport.persistMindmap} derives the description from the
     * title, which makes it impossible to tell a title match from a description
     * match. This local variant sets both independently.
     */
    @NotNull
    private Mindmap persistMindmapWith(@NotNull Account creator,
                                       @NotNull String title,
                                       @NotNull String description,
                                       boolean isPublic) {
        final Mindmap mindmap = new Mindmap();
        mindmap.setTitle(title);
        mindmap.setDescription(description);
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        mindmap.setPublic(isPublic);
        mindmap.setCreationTime(Calendar.getInstance());
        mindmap.setLastModificationTime(Calendar.getInstance());
        try {
            mindmap.setXmlStr("<map name=\"1\" version=\"tango\">"
                    + "<topic central=\"true\" text=\"search fixture\" id=\"1\"/></map>");
        } catch (InvalidMindmapException e) {
            throw new IllegalStateException("Fixture XML rejected by the model", e);
        }

        entityManager.persist(mindmap);
        entityManager.persist(new Collaboration(CollaborationRole.OWNER, creator, mindmap));
        entityManager.flush();
        return mindmap;
    }

    /**
     * A term that cannot collide with the {@code data-hsqldb.sql} seed rows nor
     * with another test, so the matching result set is exactly our fixtures.
     */
    @NotNull
    private static String uniqueToken() {
        return "zq" + System.nanoTime();
    }

    @NotNull
    private Set<Integer> historyIdsOf(int mindmapId) {
        return Set.copyOf(entityManager.createQuery(
                        "SELECT h.id FROM com.wisemapping.model.MindMapHistory h WHERE h.mindmapId = :id",
                        Integer.class)
                .setParameter("id", mindmapId)
                .getResultList());
    }

    @NotNull
    private static Set<Integer> idsOf(@NotNull List<Mindmap> mindmaps) {
        return mindmaps.stream().map(Mindmap::getId).collect(Collectors.toSet());
    }
}
