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

import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.model.AccessAuditory;
import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.InactiveMindmap;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.MindmapSpamInfo;
import com.wisemapping.model.SpamStrategyType;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixture builders shared by the DAO integration tests.
 *
 * <p>These tests run against the real in-memory HSQLDB schema and deliberately
 * bypass the REST layer, so every query is executed by the database rather than
 * asserted against a mocked {@link EntityManager}. That is the only way a typo
 * in a JPQL string, a wrong join, or a bad named-query reference can be caught.
 *
 * <p>The seed data in {@code data-hsqldb.sql} is always present, so assertions
 * must be written relative to a captured baseline (see
 * {@code assertGrewBy}-style usage in the tests) rather than against absolute
 * row counts.
 */
final class DaoTestSupport {

    /** Keeps generated emails unique within a JVM run. */
    private static final AtomicInteger SEQ = new AtomicInteger();

    private DaoTestSupport() {
    }

    @NotNull
    static String uniqueEmail(@NotNull String prefix) {
        return prefix + "-" + System.nanoTime() + "-" + SEQ.incrementAndGet() + "@dao-test.example.org";
    }

    /**
     * Persists an active DATABASE account. {@code createdDaysAgo} drives the
     * creation date, which several queries filter on.
     */
    @NotNull
    static Account persistAccount(@NotNull EntityManager em, @NotNull String emailPrefix, int createdDaysAgo) {
        final Account account = new Account();
        account.setEmail(uniqueEmail(emailPrefix));
        account.setFirstname("Dao");
        account.setLastname("Test");
        account.setPassword("daoTestPassword123");
        account.setAuthenticationType(AuthenticationType.DATABASE);

        final Calendar created = Calendar.getInstance();
        created.add(Calendar.DAY_OF_YEAR, -createdDaysAgo);
        account.setCreationDate(created);
        // An activation date is what marks the account active; several queries
        // require it to be non-null.
        account.setActivationDate(Calendar.getInstance());

        em.persist(account);
        em.flush();
        return account;
    }

    @NotNull
    static Account persistAccount(@NotNull EntityManager em, @NotNull String emailPrefix) {
        return persistAccount(em, emailPrefix, 0);
    }

    /**
     * Persists a mindmap owned by {@code creator}, together with its OWNER
     * collaboration so that ownership-sensitive queries behave realistically.
     */
    @NotNull
    static Mindmap persistMindmap(@NotNull EntityManager em,
                                  @NotNull Account creator,
                                  @NotNull String title,
                                  boolean isPublic) {
        final Mindmap mindmap = new Mindmap();
        mindmap.setTitle(title);
        mindmap.setDescription(title + " description");
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        mindmap.setPublic(isPublic);
        mindmap.setCreationTime(Calendar.getInstance());
        mindmap.setLastModificationTime(Calendar.getInstance());
        try {
            mindmap.setXmlStr("<map name=\"1\" version=\"tango\"><topic central=\"true\" text=\""
                    + title + "\" id=\"1\"/></map>");
        } catch (InvalidMindmapException e) {
            throw new IllegalStateException("Fixture XML rejected by the model", e);
        }

        em.persist(mindmap);
        em.persist(new Collaboration(CollaborationRole.OWNER, creator, mindmap));
        em.flush();
        return mindmap;
    }

    /**
     * Attaches spam info to an already-persisted mindmap. {@code spamInfo} is
     * mapped with {@code cascade = ALL}, so a merge propagates it.
     */
    @NotNull
    static Mindmap markAsSpam(@NotNull EntityManager em,
                              @NotNull Mindmap mindmap,
                              @Nullable SpamStrategyType type,
                              int detectionVersion) {
        final MindmapSpamInfo spamInfo = new MindmapSpamInfo(mindmap);
        spamInfo.setSpamDetected(true);
        spamInfo.setSpamDescription("flagged by DaoTestSupport");
        spamInfo.setSpamDetectionVersion(detectionVersion);
        spamInfo.setSpamTypeCode(type);
        mindmap.setSpamInfo(spamInfo);

        final Mindmap merged = em.merge(mindmap);
        em.flush();
        return merged;
    }

    /**
     * Attaches a non-spam {@link MindmapSpamInfo} row. Needed to exercise the
     * {@code spamDetectionVersion} branches, which only apply when a spam-info
     * row exists but is stale.
     */
    @NotNull
    static Mindmap markAsScannedClean(@NotNull EntityManager em,
                                      @NotNull Mindmap mindmap,
                                      int detectionVersion) {
        final MindmapSpamInfo spamInfo = new MindmapSpamInfo(mindmap);
        spamInfo.setSpamDetected(false);
        spamInfo.setSpamDetectionVersion(detectionVersion);
        mindmap.setSpamInfo(spamInfo);

        final Mindmap merged = em.merge(mindmap);
        em.flush();
        return merged;
    }

    @NotNull
    static MindMapHistory persistHistory(@NotNull EntityManager em,
                                         @NotNull Mindmap mindmap,
                                         @NotNull Account editor,
                                         int minutesAgo) {
        final MindMapHistory history = new MindMapHistory();
        history.setMindmapId(mindmap.getId());
        history.setEditor(editor);
        final Calendar when = Calendar.getInstance();
        when.add(Calendar.MINUTE, -minutesAgo);
        history.setCreationTime(when);
        history.setZippedXml(mindmap.getZippedXml());

        em.persist(history);
        em.flush();
        return history;
    }

    @NotNull
    static AccessAuditory persistLogin(@NotNull EntityManager em, @NotNull Account user, int daysAgo) {
        final AccessAuditory audit = new AccessAuditory();
        audit.setUser(user);
        final Calendar when = Calendar.getInstance();
        when.add(Calendar.DAY_OF_YEAR, -daysAgo);
        audit.setLoginDate(when);

        em.persist(audit);
        em.flush();
        return audit;
    }

    @NotNull
    static InactiveMindmap persistInactiveMindmap(@NotNull EntityManager em,
                                                  @NotNull Account creator,
                                                  @NotNull String title,
                                                  int createdDaysAgo,
                                                  int migratedDaysAgo) {
        final InactiveMindmap inactive = new InactiveMindmap();
        inactive.setTitle(title);
        inactive.setDescription(title + " description");
        inactive.setCreator(creator);
        inactive.setLastEditor(creator);
        inactive.setPublic(false);
        inactive.setOriginalMindmapId(Math.abs(SEQ.incrementAndGet() + (int) (System.nanoTime() % 100000)));
        inactive.setZippedXml(new byte[]{1, 2, 3});
        inactive.setMigrationReason("DAO integration test");

        final Calendar created = Calendar.getInstance();
        created.add(Calendar.DAY_OF_YEAR, -createdDaysAgo);
        inactive.setCreationTime(created);
        inactive.setLastModificationTime(created);

        final Calendar migrated = Calendar.getInstance();
        migrated.add(Calendar.DAY_OF_YEAR, -migratedDaysAgo);
        inactive.setMigrationDate(migrated);

        em.persist(inactive);
        em.flush();
        return inactive;
    }

    @NotNull
    static Calendar daysAgo(int days) {
        final Calendar result = Calendar.getInstance();
        result.add(Calendar.DAY_OF_YEAR, -days);
        return result;
    }
}
