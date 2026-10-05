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
import com.wisemapping.model.InactiveMindmap;
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
import static com.wisemapping.dao.DaoTestSupport.persistAccount;
import static com.wisemapping.dao.DaoTestSupport.persistInactiveMindmap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link InactiveMindmapManagerImpl} executed against the
 * real in-memory HSQLDB schema.
 *
 * <p>Every method on this DAO is built with the JPA Criteria API, which means a
 * wrong attribute name, a bad implicit join, or a mis-typed comparison only
 * blows up when the query is actually compiled and run by Hibernate. Mocking the
 * {@link EntityManager} would assert call sequences and prove nothing about the
 * queries themselves, so these tests drive the database directly.
 *
 * <p>Tests are {@code @Transactional} and therefore roll back. Seed rows from
 * {@code data-hsqldb.sql} are always present, so counts are asserted as deltas
 * against a captured baseline and list assertions are filtered down to the ids
 * created by the test itself.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
class InactiveMindmapManagerIntegrationTest {

    @Autowired
    private InactiveMindmapManager inactiveMindmapManager;

    @PersistenceContext
    private EntityManager entityManager;

    // ------------------------------------------------------------------ write

    @Test
    @DisplayName("addInactiveMindmap() persists the row and makes it readable back")
    void addInactiveMindmapPersistsRow() {
        final Account creator = persistAccount(entityManager, "inactive-add");
        final InactiveMindmap toAdd = newInactiveMindmap(creator, "Added through the DAO", 4, 1);

        inactiveMindmapManager.addInactiveMindmap(toAdd);
        entityManager.flush();

        assertTrue(toAdd.getId() > 0, "persist() should have assigned an identity");

        entityManager.clear();
        final InactiveMindmap reloaded = entityManager.find(InactiveMindmap.class, toAdd.getId());
        assertNotNull(reloaded, "the row should be readable back from the database");
        assertEquals("Added through the DAO", reloaded.getTitle());
        assertEquals(toAdd.getOriginalMindmapId(), reloaded.getOriginalMindmapId());
        assertEquals(creator.getId(), reloaded.getCreator().getId());
    }

    @Test
    @DisplayName("removeInactiveMindmap() deletes the row from the inactive table")
    void removeInactiveMindmapDeletesRow() {
        final Account creator = persistAccount(entityManager, "inactive-remove");
        final InactiveMindmap inactive =
                persistInactiveMindmap(entityManager, creator, "To be removed", 3, 1);
        final int id = inactive.getId();

        inactiveMindmapManager.removeInactiveMindmap(inactive);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(InactiveMindmap.class, id),
                "the removed row must no longer exist");
    }

    // ------------------------------------------------------------ by creator

    @Test
    @DisplayName("findByCreator(Account) returns only the maps of that creator")
    void findByCreatorAccountIsScopedToTheCreator() {
        final Account mine = persistAccount(entityManager, "inactive-creator-mine");
        final Account other = persistAccount(entityManager, "inactive-creator-other");

        final InactiveMindmap first = persistInactiveMindmap(entityManager, mine, "Mine one", 5, 2);
        final InactiveMindmap second = persistInactiveMindmap(entityManager, mine, "Mine two", 4, 1);
        final InactiveMindmap foreign = persistInactiveMindmap(entityManager, other, "Not mine", 6, 3);

        final List<InactiveMindmap> found = inactiveMindmapManager.findByCreator(mine);

        final Set<Integer> ids = idsOf(found);
        assertEquals(2, found.size(), "only the two maps of this creator should match");
        assertTrue(ids.contains(first.getId()));
        assertTrue(ids.contains(second.getId()));
        assertFalse(ids.contains(foreign.getId()), "another creator's map must not leak in");
    }

    @Test
    @DisplayName("findByCreator(int) resolves the creator_id join without an Account instance")
    void findByCreatorIdMatchesTheAccountOverload() {
        final Account creator = persistAccount(entityManager, "inactive-creator-id");
        final InactiveMindmap inactive =
                persistInactiveMindmap(entityManager, creator, "By creator id", 2, 1);

        final List<InactiveMindmap> byId = inactiveMindmapManager.findByCreator(creator.getId());

        assertEquals(1, byId.size());
        assertEquals(inactive.getId(), byId.get(0).getId());
        assertEquals(idsOf(inactiveMindmapManager.findByCreator(creator)), idsOf(byId),
                "both overloads must resolve to the same query");
    }

    @Test
    @DisplayName("findByCreator(int) returns an empty list for a creator with no inactive maps")
    void findByCreatorReturnsEmptyListWhenNothingMatches() {
        final Account creator = persistAccount(entityManager, "inactive-creator-empty");

        final List<InactiveMindmap> found = inactiveMindmapManager.findByCreator(creator.getId());

        assertNotNull(found, "an empty result must be an empty list, never null");
        assertTrue(found.isEmpty());
    }

    // ------------------------------------------------- by original mindmap id

    @Test
    @DisplayName("findByOriginalMindmapId() locates the migrated map by its pre-migration id")
    void findByOriginalMindmapIdLocatesTheRow() {
        final Account creator = persistAccount(entityManager, "inactive-original-id");
        final InactiveMindmap inactive =
                persistInactiveMindmap(entityManager, creator, "Has an original id", 3, 1);

        final InactiveMindmap found =
                inactiveMindmapManager.findByOriginalMindmapId(inactive.getOriginalMindmapId());

        assertNotNull(found, "the row should be found by its original mindmap id");
        assertEquals(inactive.getId(), found.getId());
    }

    @Test
    @DisplayName("findByOriginalMindmapId() returns null when no row carries that id")
    void findByOriginalMindmapIdReturnsNullWhenAbsent() {
        assertNull(inactiveMindmapManager.findByOriginalMindmapId(Integer.MAX_VALUE),
                "an unmatched original id must return null rather than throw");
    }

    // -------------------------------------------------------- created before

    @Test
    @DisplayName("findCreatedBefore() filters on the cutoff and orders by creationTime DESC")
    void findCreatedBeforeIsFilteredAndOrderedDescending() {
        final Account creator = persistAccount(entityManager, "inactive-created-before");

        final InactiveMindmap oldest = persistInactiveMindmap(entityManager, creator, "Oldest", 40, 1);
        final InactiveMindmap middle = persistInactiveMindmap(entityManager, creator, "Middle", 30, 1);
        final InactiveMindmap newest = persistInactiveMindmap(entityManager, creator, "Newest", 20, 1);
        final InactiveMindmap afterCutoff =
                persistInactiveMindmap(entityManager, creator, "After cutoff", 1, 1);

        final List<InactiveMindmap> found = inactiveMindmapManager.findCreatedBefore(daysAgo(10));

        assertFalse(idsOf(found).contains(afterCutoff.getId()),
                "a map created after the cutoff must be excluded");

        // Keep only this test's rows so the assertion survives any pre-existing data,
        // then pin the actual ordering rather than mere set membership.
        final List<Integer> mineInOrder = found.stream()
                .map(InactiveMindmap::getId)
                .filter(id -> id == oldest.getId() || id == middle.getId() || id == newest.getId())
                .collect(Collectors.toList());

        assertEquals(List.of(newest.getId(), middle.getId(), oldest.getId()), mineInOrder,
                "results must be ordered by creationTime descending (newest first)");
    }

    @Test
    @DisplayName("findCreatedBefore() is inclusive: a map created exactly at the cutoff is returned")
    void findCreatedBeforeIncludesTheExactBoundary() {
        final Account creator = persistAccount(entityManager, "inactive-boundary-find");
        final InactiveMindmap onBoundary =
                persistInactiveMindmap(entityManager, creator, "Exactly at the cutoff", 15, 1);

        // Read the creation time back from the database so the cutoff is byte-for-byte
        // the stored value; this is what makes the <= boundary meaningful.
        final Calendar cutoff = reloadCreationTime(onBoundary);

        final List<InactiveMindmap> found = inactiveMindmapManager.findCreatedBefore(cutoff);

        assertTrue(idsOf(found).contains(onBoundary.getId()),
                "lessThanOrEqualTo means the row sitting exactly on the cutoff is included");
    }

    @Test
    @DisplayName("countCreatedBefore() counts the same rows findCreatedBefore() returns, boundary included")
    void countCreatedBeforeMatchesTheInclusiveFilter() {
        final Account creator = persistAccount(entityManager, "inactive-count-before");
        final Calendar cutoff = daysAgo(10);

        final long baseline = inactiveMindmapManager.countCreatedBefore(cutoff);

        persistInactiveMindmap(entityManager, creator, "Well before", 40, 1);
        persistInactiveMindmap(entityManager, creator, "Also before", 25, 1);
        persistInactiveMindmap(entityManager, creator, "After cutoff", 2, 1);

        assertEquals(baseline + 2, inactiveMindmapManager.countCreatedBefore(cutoff),
                "only the two rows older than the cutoff should be counted");
        assertEquals(inactiveMindmapManager.findCreatedBefore(cutoff).size(),
                inactiveMindmapManager.countCreatedBefore(cutoff),
                "the count query and the list query must agree");
    }

    @Test
    @DisplayName("countCreatedBefore() includes a map created exactly at the cutoff")
    void countCreatedBeforeIncludesTheExactBoundary() {
        final Account creator = persistAccount(entityManager, "inactive-boundary-count");
        final InactiveMindmap onBoundary =
                persistInactiveMindmap(entityManager, creator, "On the boundary", 12, 1);
        final Calendar cutoff = reloadCreationTime(onBoundary);

        // Everything strictly older than the boundary row, i.e. the baseline the
        // boundary row itself must be added to.
        final Calendar justBefore = (Calendar) cutoff.clone();
        justBefore.add(Calendar.SECOND, -1);
        final long strictlyOlder = inactiveMindmapManager.countCreatedBefore(justBefore);

        assertEquals(strictlyOlder + 1, inactiveMindmapManager.countCreatedBefore(cutoff),
                "the boundary row must be counted by the <= comparison");
    }

    // ----------------------------------------------------------- total count

    @Test
    @DisplayName("countAllInactiveMindmaps() grows by exactly the number of rows inserted")
    void countAllInactiveMindmapsTracksInserts() {
        final long baseline = inactiveMindmapManager.countAllInactiveMindmaps();
        assertTrue(baseline >= 0, "COUNT(*) must never surface as a negative or null value");

        final Account creator = persistAccount(entityManager, "inactive-count-all");
        persistInactiveMindmap(entityManager, creator, "Counted one", 5, 1);
        persistInactiveMindmap(entityManager, creator, "Counted two", 4, 1);

        assertEquals(baseline + 2, inactiveMindmapManager.countAllInactiveMindmaps(),
                "the total count should move by the delta, not to an absolute value");
    }

    // ---------------------------------------------------------------- findAll

    @Test
    @DisplayName("findAll() returns every row ordered by migrationDate DESC")
    void findAllIsOrderedByMigrationDateDescending() {
        final Account creator = persistAccount(entityManager, "inactive-find-all");

        final InactiveMindmap migratedLongAgo =
                persistInactiveMindmap(entityManager, creator, "Migrated long ago", 50, 30);
        final InactiveMindmap migratedMidway =
                persistInactiveMindmap(entityManager, creator, "Migrated midway", 50, 20);
        final InactiveMindmap migratedRecently =
                persistInactiveMindmap(entityManager, creator, "Migrated recently", 50, 5);

        final List<InactiveMindmap> all = inactiveMindmapManager.findAll();

        assertEquals(inactiveMindmapManager.countAllInactiveMindmaps(), all.size(),
                "findAll() must return as many rows as the count query reports");

        final List<Integer> mineInOrder = all.stream()
                .map(InactiveMindmap::getId)
                .filter(id -> id == migratedLongAgo.getId()
                        || id == migratedMidway.getId()
                        || id == migratedRecently.getId())
                .collect(Collectors.toList());

        assertEquals(
                List.of(migratedRecently.getId(), migratedMidway.getId(), migratedLongAgo.getId()),
                mineInOrder,
                "results must be ordered by migrationDate descending (most recently migrated first)");
    }

    @Test
    @DisplayName("findAll(offset, limit) pages through the same DESC ordering without repeats")
    void findAllIsPageable() {
        final Account creator = persistAccount(entityManager, "inactive-paging");
        persistInactiveMindmap(entityManager, creator, "Page row a", 9, 9);
        persistInactiveMindmap(entityManager, creator, "Page row b", 8, 8);
        persistInactiveMindmap(entityManager, creator, "Page row c", 7, 7);

        final List<InactiveMindmap> firstPage = inactiveMindmapManager.findAll(0, 2);
        assertEquals(2, firstPage.size(), "limit should cap the page size");

        final List<InactiveMindmap> secondPage = inactiveMindmapManager.findAll(2, 2);
        assertFalse(secondPage.isEmpty(), "there should be a second page");

        final Set<Integer> firstIds = idsOf(firstPage);
        assertTrue(secondPage.stream().noneMatch(m -> firstIds.contains(m.getId())),
                "offset should not re-return rows from the first page");

        // The page must be the head of the globally ordered result.
        final List<InactiveMindmap> all = inactiveMindmapManager.findAll();
        assertEquals(idsOf(all.subList(0, 2)), firstIds,
                "the first page must be the first two rows of the DESC-ordered result");
    }

    // ----------------------------------------------------------- bulk delete

    @Test
    @DisplayName("deleteOlderThan() removes only rows at or before the cutoff and returns the row count")
    void deleteOlderThanRemovesMatchingRowsOnly() {
        final Account creator = persistAccount(entityManager, "inactive-delete-older");
        final Calendar cutoff = daysAgo(10);

        final InactiveMindmap oldA = persistInactiveMindmap(entityManager, creator, "Old A", 40, 1);
        final InactiveMindmap oldB = persistInactiveMindmap(entityManager, creator, "Old B", 30, 1);
        final InactiveMindmap recent = persistInactiveMindmap(entityManager, creator, "Recent", 2, 1);

        final long expectedDeletions = inactiveMindmapManager.countCreatedBefore(cutoff);
        final long totalBefore = inactiveMindmapManager.countAllInactiveMindmaps();

        final int deleted = inactiveMindmapManager.deleteOlderThan(cutoff);

        // A CriteriaDelete bypasses the persistence context, so stale managed
        // copies have to be evicted before re-reading.
        entityManager.clear();

        assertEquals(expectedDeletions, deleted,
                "the returned row count must match what countCreatedBefore() reported");
        assertEquals(totalBefore - deleted, inactiveMindmapManager.countAllInactiveMindmaps());
        assertEquals(0, inactiveMindmapManager.countCreatedBefore(cutoff),
                "nothing at or before the cutoff should survive the delete");

        assertNull(entityManager.find(InactiveMindmap.class, oldA.getId()), "Old A should be gone");
        assertNull(entityManager.find(InactiveMindmap.class, oldB.getId()), "Old B should be gone");
        assertNotNull(entityManager.find(InactiveMindmap.class, recent.getId()),
                "a row created after the cutoff must survive");
    }

    @Test
    @DisplayName("deleteOlderThan() is inclusive: a map created exactly at the cutoff is deleted")
    void deleteOlderThanDeletesTheExactBoundary() {
        final Account creator = persistAccount(entityManager, "inactive-boundary-delete");
        final InactiveMindmap onBoundary =
                persistInactiveMindmap(entityManager, creator, "On the boundary", 18, 1);
        final InactiveMindmap justAfter =
                persistInactiveMindmap(entityManager, creator, "One second later", 18, 1);

        final Calendar cutoff = reloadCreationTime(onBoundary);
        final Calendar later = (Calendar) cutoff.clone();
        later.add(Calendar.SECOND, 1);
        justAfter.setCreationTime(later);
        entityManager.merge(justAfter);
        entityManager.flush();

        final int deleted = inactiveMindmapManager.deleteOlderThan(cutoff);
        entityManager.clear();

        assertTrue(deleted >= 1, "at least the boundary row should have been deleted");
        assertNull(entityManager.find(InactiveMindmap.class, onBoundary.getId()),
                "lessThanOrEqualTo means the row exactly on the cutoff is deleted");
        assertNotNull(entityManager.find(InactiveMindmap.class, justAfter.getId()),
                "a row one second past the cutoff must survive");
    }

    @Test
    @DisplayName("deleteOlderThan() returns 0 when nothing matches the cutoff")
    void deleteOlderThanReturnsZeroWhenNothingMatches() {
        final Account creator = persistAccount(entityManager, "inactive-delete-none");
        persistInactiveMindmap(entityManager, creator, "Brand new", 0, 0);

        // Clear out anything older first so the no-match case is genuine.
        inactiveMindmapManager.deleteOlderThan(daysAgo(1));
        entityManager.clear();

        assertEquals(0, inactiveMindmapManager.deleteOlderThan(daysAgo(1)),
                "a delete that matches nothing must report zero rows affected");
    }

    // --------------------------------------------------------------- helpers

    /**
     * Builds an unpersisted {@link InactiveMindmap} so that
     * {@link InactiveMindmapManager#addInactiveMindmap} itself does the insert.
     */
    @NotNull
    private InactiveMindmap newInactiveMindmap(@NotNull Account creator,
                                               @NotNull String title,
                                               int createdDaysAgo,
                                               int migratedDaysAgo) {
        final InactiveMindmap inactive = new InactiveMindmap();
        inactive.setTitle(title);
        inactive.setDescription(title + " description");
        inactive.setCreator(creator);
        inactive.setLastEditor(creator);
        inactive.setPublic(false);
        // The column is NOT NULL and carries no unique constraint; a per-creator
        // derived value keeps it collision-free within a test run.
        inactive.setOriginalMindmapId(creator.getId() * 1000 + title.length());
        inactive.setZippedXml(new byte[]{4, 5, 6});
        inactive.setMigrationReason("DAO integration test");
        inactive.setCreationTime(daysAgo(createdDaysAgo));
        inactive.setLastModificationTime(daysAgo(createdDaysAgo));
        inactive.setMigrationDate(daysAgo(migratedDaysAgo));
        return inactive;
    }

    /**
     * Reads {@code creationTime} back from the database so a cutoff can be set to
     * the exact stored value, independent of any timestamp precision the dialect
     * applies on the way in.
     */
    @NotNull
    private Calendar reloadCreationTime(@NotNull InactiveMindmap inactive) {
        entityManager.flush();
        entityManager.clear();
        final InactiveMindmap reloaded = entityManager.find(InactiveMindmap.class, inactive.getId());
        assertNotNull(reloaded, "the fixture row should be readable back");
        return reloaded.getCreationTime();
    }

    @NotNull
    private static Set<Integer> idsOf(@NotNull List<InactiveMindmap> maps) {
        return maps.stream().map(InactiveMindmap::getId).collect(Collectors.toSet());
    }
}
