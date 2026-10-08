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
import com.wisemapping.model.SpamRatioUserResult;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.model.SpamUserResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wisemapping.dao.DaoTestSupport.markAsScannedClean;
import static com.wisemapping.dao.DaoTestSupport.markAsSpam;
import static com.wisemapping.dao.DaoTestSupport.persistAccount;
import static com.wisemapping.dao.DaoTestSupport.persistMindmap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the spam-ratio / spam-type query family on
 * {@link MindmapManager}, executed against the real in-memory HSQLDB schema.
 *
 * <p>These are the most intricate statements in the DAO: hand-written JPQL with
 * {@code COUNT(CASE WHEN ...)} projections, a {@code HAVING} clause over a
 * computed ratio, and - for the {@code ...ByType} pair - an {@code IN} list
 * that is concatenated into the query string at runtime. None of it can be
 * verified with a mocked {@code EntityManager}; only the database can tell us
 * whether the ratio arithmetic, the grouping and the boundary comparisons are
 * what the callers in {@code SpamUserSuspensionService} assume.
 *
 * <p>The tests below deliberately pin the behaviour the SQL <em>implements</em>
 * rather than the behaviour the method names suggest. Two deviations are worth
 * calling out, and each has a dedicated test:
 * <ul>
 *   <li>Every query except {@code findUsersWithAnySpamMaps} restricts itself to
 *       {@code m.isPublic = true}, so private maps contribute to neither the
 *       numerator nor the denominator of the "spam ratio".</li>
 *   <li>{@code findUsersWithMinimumMapsAndSpam} uses
 *       {@code HAVING COUNT(m.id) > :minTotalMaps} - strictly greater - so a
 *       user owning exactly {@code minTotalMaps} public maps is excluded.</li>
 *   <li>{@code findUsersWithPublicSpamMapsByType} and its {@code count}
 *       counterpart cannot execute with a non-empty code array at all: they bind
 *       {@code String}s to {@code s.spamTypeCode}, which is a
 *       {@code SpamStrategyType} attribute behind an {@code AttributeConverter},
 *       and Hibernate rejects the binding before the query is issued. The
 *       {@code DEFECT:} test below pins that, so the day the DAO is fixed the
 *       test fails and tells whoever fixed it to assert the real filtering
 *       instead.</li>
 * </ul>
 *
 * <p>The {@code monthsBack} window is applied to the <em>account</em> creation
 * date ({@code c.creationDate}), not to the map creation date.
 *
 * <p>Precision note: {@code COUNT(...) * 1.0 / COUNT(m.id)} is a one-decimal
 * {@code DECIMAL} expression for HSQLDB, and HSQLDB coerces the bound
 * {@code spamRatioThreshold} to that scale, so on this database a threshold of
 * 0.51 behaves exactly like 0.50. The ratio assertions below therefore stay on
 * a 0.1 grid rather than relying on a resolution the test database does not
 * have.
 *
 * <p>Seed rows from {@code data-hsqldb.sql} are always present and other tests
 * may leave public maps behind, so result sets are always narrowed to the
 * fixture accounts created by the test itself, and {@code count*} assertions are
 * expressed as deltas against a baseline captured before the fixtures exist.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
class MindmapManagerSpamRatioQueryIntegrationTest {

    /** All fixture accounts are created "today", so a 1-month window includes them. */
    private static final int MONTHS_BACK = 1;

    /** Comfortably older than the 1-month window used throughout. */
    private static final int OUTSIDE_WINDOW_DAYS = 400;

    private static final int NO_OFFSET = 0;

    /** Large enough that pagination never truncates a fixture away. */
    private static final int ALL_RESULTS = 10_000;

    private static final double EPSILON = 1e-9;

    @Autowired
    private MindmapManager mindmapManager;

    @PersistenceContext
    private EntityManager entityManager;

    // ------------------------------------------------- findUsersWithHighSpamRatio

    @Test
    @DisplayName("findUsersWithHighSpamRatio() returns the user whose public spam ratio is above the threshold and drops the one below it")
    void findUsersWithHighSpamRatioSplitsAtTheThreshold() {
        // 2 of 3 public maps are spam -> ratio 0.667, above 0.60
        final Account above = account("ratio-above");
        addPublicSpamMaps(above, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(above, 1);

        // 2 of 5 public maps are spam -> ratio 0.40, below 0.60 (same spam count)
        final Account below = account("ratio-below");
        addPublicSpamMaps(below, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(below, 3);

        final List<SpamRatioUserResult> results =
                mindmapManager.findUsersWithHighSpamRatio(2, 0.60d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);

        final SpamRatioUserResult aboveResult = ratioFor(results, above);
        assertNotNull(aboveResult, "user with a 0.667 public spam ratio must be reported");
        assertEquals(2L, aboveResult.getSpamCount());
        assertEquals(3L, aboveResult.getTotalCount());
        assertEquals(2d / 3d, aboveResult.getSpamRatio(), EPSILON);

        assertNull(ratioFor(results, below), "user with a 0.40 public spam ratio must not be reported");
    }

    @Test
    @DisplayName("findUsersWithHighSpamRatio() treats the ratio threshold as inclusive: exactly 0.5 matches a 0.5 threshold")
    void findUsersWithHighSpamRatioRatioThresholdIsInclusive() {
        // 1 of 2 public maps is spam -> ratio is exactly 0.5.
        final Account exact = account("ratio-exact");
        addPublicSpamMaps(exact, 1, SpamStrategyType.CONTACT_INFO);
        // Scanned-and-clean rather than never-scanned, to exercise the
        // spamDetected = false branch of the COUNT(CASE WHEN ...) projection.
        addPublicScannedCleanMaps(exact, 1);

        final List<SpamRatioUserResult> atThreshold =
                mindmapManager.findUsersWithHighSpamRatio(1, 0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        final SpamRatioUserResult matched = ratioFor(atThreshold, exact);
        assertNotNull(matched, "ratio exactly at the threshold must match (>=)");
        assertEquals(1L, matched.getSpamCount());
        assertEquals(2L, matched.getTotalCount());
        assertEquals(0.5d, matched.getSpamRatio(), EPSILON);

        // Thresholds are kept at one-decimal granularity on purpose: see the
        // HSQLDB precision note in the class javadoc.
        final List<SpamRatioUserResult> aboveThreshold =
                mindmapManager.findUsersWithHighSpamRatio(1, 0.6d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        assertNull(ratioFor(aboveThreshold, exact), "a 0.5 ratio must not match a 0.6 threshold");
    }

    @Test
    @DisplayName("findUsersWithHighSpamRatio() treats minSpamCount as inclusive: exactly minSpamCount spam maps matches")
    void findUsersWithHighSpamRatioMinSpamCountIsInclusive() {
        // 2 of 2 public maps are spam -> ratio 1.0, spam count 2.
        final Account user = account("ratio-minspam");
        addPublicSpamMaps(user, 2, SpamStrategyType.LINK_FARM);

        assertNotNull(ratioFor(
                        mindmapManager.findUsersWithHighSpamRatio(2, 0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "exactly minSpamCount spam maps must match (>=)");
        assertNull(ratioFor(
                        mindmapManager.findUsersWithHighSpamRatio(3, 0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "one spam map short of minSpamCount must not match");
    }

    @Test
    @DisplayName("findUsersWithHighSpamRatio() ignores private maps in both the spam count and the total count")
    void findUsersWithHighSpamRatioCountsPublicMapsOnly() {
        // Public: 1 spam + 1 clean -> 1/2. Private: 2 more spam maps that the
        // query must not see; counting them would give 3/4 instead.
        final Account user = account("ratio-private");
        addPublicSpamMaps(user, 1, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(user, 1);
        addPrivateSpamMaps(user, 2, SpamStrategyType.CONTACT_INFO);

        final SpamRatioUserResult result = ratioFor(
                mindmapManager.findUsersWithHighSpamRatio(1, 0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user);
        assertNotNull(result);
        assertEquals(1L, result.getSpamCount(), "private spam maps must not be counted");
        assertEquals(2L, result.getTotalCount(), "private maps must not enlarge the denominator");
        assertEquals(0.5d, result.getSpamRatio(), EPSILON);
    }

    @Test
    @DisplayName("findUsersWithHighSpamRatio() applies monthsBack to the account creation date, not the map creation date")
    void findUsersWithHighSpamRatioFiltersOnAccountCreationDate() {
        final Account oldAccount = persistAccount(entityManager, "ratio-old", OUTSIDE_WINDOW_DAYS);
        addPublicSpamMaps(oldAccount, 2, SpamStrategyType.CONTACT_INFO);

        assertNull(ratioFor(
                        mindmapManager.findUsersWithHighSpamRatio(1, 0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS),
                        oldAccount),
                "an account created before the cutoff must be excluded even though its maps are brand new");
        assertNotNull(ratioFor(
                        mindmapManager.findUsersWithHighSpamRatio(1, 0.5d, 24, NO_OFFSET, ALL_RESULTS), oldAccount),
                "widening the window to 24 months must bring the same account back");
    }

    @Test
    @DisplayName("countUsersWithHighSpamRatio() grows by exactly the number of users findUsersWithHighSpamRatio() reports")
    void countUsersWithHighSpamRatioAgreesWithFind() {
        final long baseline = mindmapManager.countUsersWithHighSpamRatio(2, 0.60d, MONTHS_BACK);

        final Account matching = account("ratio-count-match");
        addPublicSpamMaps(matching, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(matching, 1);

        final Account notMatching = account("ratio-count-miss");
        addPublicSpamMaps(notMatching, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(notMatching, 3);

        final List<SpamRatioUserResult> found =
                mindmapManager.findUsersWithHighSpamRatio(2, 0.60d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        assertNotNull(ratioFor(found, matching));
        assertNull(ratioFor(found, notMatching));

        assertEquals(baseline + 1, mindmapManager.countUsersWithHighSpamRatio(2, 0.60d, MONTHS_BACK),
                "count must agree with the single additional user returned by find");
    }

    // ------------------------------------------- findUsersWithMinimumMapsAndSpam

    @Test
    @DisplayName("findUsersWithMinimumMapsAndSpam() requires STRICTLY more than minTotalMaps public maps (HAVING COUNT(m.id) > :minTotalMaps)")
    void findUsersWithMinimumMapsAndSpamUsesExclusiveTotalMapsBound() {
        // Exactly 3 public maps, 2 of them spam.
        final Account user = account("mintotal");
        addPublicSpamMaps(user, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(user, 1);

        assertNull(spamFor(
                        mindmapManager.findUsersWithMinimumMapsAndSpam(3, 2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "3 public maps must NOT satisfy minTotalMaps = 3: the query compares with > , not >=");

        final SpamUserResult matched = spamFor(
                mindmapManager.findUsersWithMinimumMapsAndSpam(2, 2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user);
        assertNotNull(matched, "3 public maps must satisfy minTotalMaps = 2");
        assertEquals(2L, matched.getSpamCount());
    }

    @Test
    @DisplayName("findUsersWithMinimumMapsAndSpam() treats minSpamCount as inclusive")
    void findUsersWithMinimumMapsAndSpamMinSpamCountIsInclusive() {
        // 4 public maps, 2 of them spam.
        final Account user = account("mintotal-minspam");
        addPublicSpamMaps(user, 2, SpamStrategyType.FEW_NODES);
        addPublicCleanMaps(user, 2);

        assertNotNull(spamFor(
                        mindmapManager.findUsersWithMinimumMapsAndSpam(3, 2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "exactly minSpamCount spam maps must match (>=)");
        assertNull(spamFor(
                        mindmapManager.findUsersWithMinimumMapsAndSpam(3, 3, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "one spam map short of minSpamCount must not match");
    }

    @Test
    @DisplayName("findUsersWithMinimumMapsAndSpam() counts only public maps, so a user whose spam maps are private is never reported")
    void findUsersWithMinimumMapsAndSpamIgnoresPrivateMaps() {
        // 1 public clean map plus 4 private spam maps: on public maps alone the
        // user has 1 map and 0 spam.
        final Account user = account("mintotal-private");
        addPublicCleanMaps(user, 1);
        addPrivateSpamMaps(user, 4, SpamStrategyType.LINK_FARM);

        assertNull(spamFor(
                        mindmapManager.findUsersWithMinimumMapsAndSpam(0, 1, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "private spam maps must not satisfy the spam requirement");
    }

    @Test
    @DisplayName("countUsersWithMinimumMapsAndSpam() grows by exactly the number of users findUsersWithMinimumMapsAndSpam() reports")
    void countUsersWithMinimumMapsAndSpamAgreesWithFind() {
        final long baseline = mindmapManager.countUsersWithMinimumMapsAndSpam(2, 2, MONTHS_BACK);

        // 3 public maps (> 2) with 2 spam (>= 2): matches.
        final Account matching = account("mintotal-count-match");
        addPublicSpamMaps(matching, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(matching, 1);

        // 3 public maps but only 1 spam: fails the spam requirement.
        final Account notMatching = account("mintotal-count-miss");
        addPublicSpamMaps(notMatching, 1, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(notMatching, 2);

        final List<SpamUserResult> found =
                mindmapManager.findUsersWithMinimumMapsAndSpam(2, 2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        assertNotNull(spamFor(found, matching));
        assertNull(spamFor(found, notMatching));

        assertEquals(baseline + 1, mindmapManager.countUsersWithMinimumMapsAndSpam(2, 2, MONTHS_BACK),
                "count must agree with the single additional user returned by find");
    }

    // ------------------------------------- findUsersWithHighPublicSpamRatio

    @Test
    @DisplayName("findUsersWithHighPublicSpamRatio() splits at the ratio threshold with no minimum spam count")
    void findUsersWithHighPublicSpamRatioSplitsAtTheThreshold() {
        // 2 of 3 public maps spam -> 0.667, above 0.60.
        final Account above = account("public-ratio-above");
        addPublicSpamMaps(above, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(above, 1);

        // 2 of 5 public maps spam -> 0.40, below 0.60.
        final Account below = account("public-ratio-below");
        addPublicSpamMaps(below, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(below, 3);

        // 1 public spam map only -> ratio 1.0. There is no minSpamCount in this
        // variant, so a single spam map is enough.
        final Account single = account("public-ratio-single");
        addPublicSpamMaps(single, 1, SpamStrategyType.CONTACT_INFO);

        // No spam at all -> ratio 0.0, must be excluded by a positive threshold.
        final Account clean = account("public-ratio-clean");
        addPublicCleanMaps(clean, 2);

        final List<SpamRatioUserResult> results =
                mindmapManager.findUsersWithHighPublicSpamRatio(0.60d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);

        final SpamRatioUserResult aboveResult = ratioFor(results, above);
        assertNotNull(aboveResult);
        assertEquals(2L, aboveResult.getSpamCount());
        assertEquals(3L, aboveResult.getTotalCount());

        final SpamRatioUserResult singleResult = ratioFor(results, single);
        assertNotNull(singleResult, "a single spam map is enough when the ratio clears the threshold");
        assertEquals(1L, singleResult.getSpamCount());
        assertEquals(1L, singleResult.getTotalCount());
        assertEquals(1.0d, singleResult.getSpamRatio(), EPSILON);

        assertNull(ratioFor(results, below), "a 0.40 ratio must not match a 0.60 threshold");
        assertNull(ratioFor(results, clean), "a user with no spam maps must not match a positive threshold");
    }

    @Test
    @DisplayName("findUsersWithHighPublicSpamRatio() is inclusive at the threshold and keeps private maps out of the denominator")
    void findUsersWithHighPublicSpamRatioBoundaryIgnoresPrivateMaps() {
        // Public: 1 spam + 1 clean -> exactly 0.5. Private: 3 clean maps that
        // would dilute the ratio to 0.2 if they were counted.
        final Account user = account("public-ratio-boundary");
        addPublicSpamMaps(user, 1, SpamStrategyType.HTML_CONTENT);
        addPublicCleanMaps(user, 1);
        addPrivateCleanMaps(user, 3);

        final SpamRatioUserResult matched = ratioFor(
                mindmapManager.findUsersWithHighPublicSpamRatio(0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user);
        assertNotNull(matched, "a ratio exactly at the threshold must match (>=)");
        assertEquals(1L, matched.getSpamCount());
        assertEquals(2L, matched.getTotalCount(), "only public maps belong in the denominator");

        assertNull(ratioFor(
                        mindmapManager.findUsersWithHighPublicSpamRatio(0.6d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS),
                        user),
                "a 0.5 ratio must not match a 0.6 threshold");
    }

    @Test
    @DisplayName("countUsersWithHighPublicSpamRatio() grows by exactly the number of users findUsersWithHighPublicSpamRatio() reports")
    void countUsersWithHighPublicSpamRatioAgreesWithFind() {
        final long baseline = mindmapManager.countUsersWithHighPublicSpamRatio(0.60d, MONTHS_BACK);

        final Account matching = account("public-ratio-count-match");
        addPublicSpamMaps(matching, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(matching, 1);

        final Account notMatching = account("public-ratio-count-miss");
        addPublicSpamMaps(notMatching, 2, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(notMatching, 3);

        final List<SpamRatioUserResult> found =
                mindmapManager.findUsersWithHighPublicSpamRatio(0.60d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        assertNotNull(ratioFor(found, matching));
        assertNull(ratioFor(found, notMatching));

        assertEquals(baseline + 1, mindmapManager.countUsersWithHighPublicSpamRatio(0.60d, MONTHS_BACK),
                "count must agree with the single additional user returned by find");
    }

    // ------------------------------------------------ findUsersWithAnySpamMaps

    @Test
    @DisplayName("findUsersWithAnySpamMaps() has no isPublic filter, so private spam maps count towards minSpamCount")
    void findUsersWithAnySpamMapsCountsPrivateMapsToo() {
        final Account privateOnly = account("any-spam-private");
        addPrivateSpamMaps(privateOnly, 2, SpamStrategyType.LINK_FARM);

        final SpamUserResult result = spamFor(
                mindmapManager.findUsersWithAnySpamMaps(2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), privateOnly);
        assertNotNull(result, "private spam maps must be visible to this variant");
        assertEquals(2L, result.getSpamCount());

        // Sanity check that the public-only variants disagree on the same data.
        assertNull(ratioFor(
                        mindmapManager.findUsersWithHighSpamRatio(1, 0.5d, MONTHS_BACK, NO_OFFSET, ALL_RESULTS),
                        privateOnly),
                "the public-only ratio query must not see the same private spam maps");
    }

    @Test
    @DisplayName("findUsersWithAnySpamMaps() treats minSpamCount as inclusive and excludes users with fewer spam maps")
    void findUsersWithAnySpamMapsMinSpamCountIsInclusive() {
        // 2 spam maps (one public, one private) plus a clean one.
        final Account user = account("any-spam-boundary");
        addPublicSpamMaps(user, 1, SpamStrategyType.CONTACT_INFO);
        addPrivateSpamMaps(user, 1, SpamStrategyType.CONTACT_INFO);
        addPublicCleanMaps(user, 1);

        final SpamUserResult matched =
                spamFor(mindmapManager.findUsersWithAnySpamMaps(2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user);
        assertNotNull(matched, "exactly minSpamCount spam maps must match (>=)");
        assertEquals(2L, matched.getSpamCount());

        assertNull(spamFor(mindmapManager.findUsersWithAnySpamMaps(3, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), user),
                "one spam map short of minSpamCount must not match");

        // A user that owns maps but none flagged as spam must stay out.
        final Account cleanUser = account("any-spam-clean");
        addPublicScannedCleanMaps(cleanUser, 2);
        assertNull(spamFor(mindmapManager.findUsersWithAnySpamMaps(1, MONTHS_BACK, NO_OFFSET, ALL_RESULTS), cleanUser),
                "a user with zero spam maps must not match minSpamCount = 1");
    }

    @Test
    @DisplayName("countUsersWithAnySpamMaps() grows by exactly the number of users findUsersWithAnySpamMaps() reports")
    void countUsersWithAnySpamMapsAgreesWithFind() {
        final long baseline = mindmapManager.countUsersWithAnySpamMaps(2, MONTHS_BACK);

        final Account matching = account("any-spam-count-match");
        addPrivateSpamMaps(matching, 2, SpamStrategyType.LINK_FARM);

        final Account notMatching = account("any-spam-count-miss");
        addPrivateSpamMaps(notMatching, 1, SpamStrategyType.LINK_FARM);

        final List<SpamUserResult> found =
                mindmapManager.findUsersWithAnySpamMaps(2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        assertNotNull(spamFor(found, matching));
        assertNull(spamFor(found, notMatching));

        assertEquals(baseline + 1, mindmapManager.countUsersWithAnySpamMaps(2, MONTHS_BACK),
                "count must agree with the single additional user returned by find");
    }

    // ----------------------------------- findUsersWithPublicSpamMapsByType

    @Test
    @DisplayName("findUsersWithPublicSpamMapsByType()/count short-circuit to empty results for a null or empty code array")
    void findUsersWithPublicSpamMapsByTypeShortCircuitsOnMissingCodes() {
        final Account user = account("by-type-shortcircuit");
        addPublicSpamMaps(user, 2, SpamStrategyType.CONTACT_INFO);

        assertTrue(mindmapManager.findUsersWithPublicSpamMapsByType(null, MONTHS_BACK, NO_OFFSET, ALL_RESULTS)
                        .isEmpty(),
                "a null code array must short-circuit to an empty list");
        assertTrue(mindmapManager.findUsersWithPublicSpamMapsByType(new String[0], MONTHS_BACK, NO_OFFSET, ALL_RESULTS)
                        .isEmpty(),
                "an empty code array must short-circuit to an empty list");

        assertEquals(0L, mindmapManager.countUsersWithPublicSpamMapsByType(null, MONTHS_BACK),
                "a null code array must short-circuit to 0");
        assertEquals(0L, mindmapManager.countUsersWithPublicSpamMapsByType(new String[0], MONTHS_BACK),
                "an empty code array must short-circuit to 0");
    }

    @Test
    @DisplayName("DEFECT: findUsersWithPublicSpamMapsByType() cannot run at all - binding a String code to the converted s.spamTypeCode attribute is rejected by Hibernate")
    void findUsersWithPublicSpamMapsByTypeRejectsItsOwnStringParameters() {
        // MindmapSpamInfo.spamTypeCode is a SpamStrategyType attribute carrying
        // @Convert(converter = SpamStrategyTypeConverter.class). The DAO builds
        // "... AND s.spamTypeCode IN (:spamType0, ...)" and binds plain Strings,
        // which Hibernate's parameter validator refuses outright - the query never
        // reaches the database. Every non-empty spamTypeCodes array therefore
        // fails, no matter whether the caller passes the single-char code ("C"),
        // the strategy name ("ContactInfo") suggested by the interface javadoc, or
        // the enum constant name ("CONTACT_INFO").
        final Account user = account("by-type-defect");
        addPublicSpamMaps(user, 2, SpamStrategyType.CONTACT_INFO);

        final String[] singleCharCode = {code(SpamStrategyType.CONTACT_INFO)};
        assertStringCodeBindingIsRejected(() -> mindmapManager
                .findUsersWithPublicSpamMapsByType(singleCharCode, MONTHS_BACK, NO_OFFSET, ALL_RESULTS));
        assertStringCodeBindingIsRejected(() -> mindmapManager
                .countUsersWithPublicSpamMapsByType(singleCharCode, MONTHS_BACK));

        final String[] strategyName = {SpamStrategyType.CONTACT_INFO.getStrategyName()};
        assertStringCodeBindingIsRejected(() -> mindmapManager
                .findUsersWithPublicSpamMapsByType(strategyName, MONTHS_BACK, NO_OFFSET, ALL_RESULTS));

        final String[] enumName = {SpamStrategyType.CONTACT_INFO.name()};
        assertStringCodeBindingIsRejected(() -> mindmapManager
                .findUsersWithPublicSpamMapsByType(enumName, MONTHS_BACK, NO_OFFSET, ALL_RESULTS));

        // Several codes concatenated into the IN clause fail the same way, on the
        // very first parameter.
        final String[] severalCodes = {code(SpamStrategyType.CONTACT_INFO), code(SpamStrategyType.LINK_FARM)};
        assertStringCodeBindingIsRejected(() -> mindmapManager
                .findUsersWithPublicSpamMapsByType(severalCodes, MONTHS_BACK, NO_OFFSET, ALL_RESULTS));
    }

    private static void assertStringCodeBindingIsRejected(@NotNull Executable call) {
        final InvalidDataAccessApiUsageException failure =
                assertThrows(InvalidDataAccessApiUsageException.class, call,
                        "the query must still be rejected at parameter-binding time");
        assertTrue(failure.getMessage() != null && failure.getMessage().contains("spamType0"),
                "the rejection must name the first generated spam-type parameter, was: " + failure.getMessage());
        assertTrue(failure.getMessage().contains(SpamStrategyType.class.getName()),
                "the rejection must name the converted SpamStrategyType attribute, was: " + failure.getMessage());
    }

    // -------------------------------------------------------------- pagination

    @Test
    @DisplayName("findUsersWithAnySpamMaps() paginates over a stable id ordering, so offset/limit slice the same list")
    void findUsersWithAnySpamMapsPaginatesConsistently() {
        // Three distinct users, each with two spam maps, so all three qualify.
        for (int i = 0; i < 3; i++) {
            addPrivateSpamMaps(account("any-spam-page-" + i), 2, SpamStrategyType.LINK_FARM);
        }

        final List<SpamUserResult> all =
                mindmapManager.findUsersWithAnySpamMaps(2, MONTHS_BACK, NO_OFFSET, ALL_RESULTS);
        assertTrue(all.size() >= 3, "the three fixture users must be present in the unpaged result");

        final List<SpamUserResult> firstPage = mindmapManager.findUsersWithAnySpamMaps(2, MONTHS_BACK, NO_OFFSET, 2);
        assertEquals(2, firstPage.size());
        assertEquals(userIds(all.subList(0, 2)), userIds(firstPage), "limit must slice the head of the ordered list");

        final List<SpamUserResult> rest = mindmapManager.findUsersWithAnySpamMaps(2, MONTHS_BACK, 2, ALL_RESULTS);
        assertEquals(userIds(all.subList(2, all.size())), userIds(rest),
                "offset must skip exactly the rows the previous page returned");
    }

    // ------------------------------------------------------------------ fixtures

    @NotNull
    private Account account(@NotNull String prefix) {
        return persistAccount(entityManager, prefix, 0);
    }

    private void addPublicSpamMaps(@NotNull Account owner, int count, @NotNull SpamStrategyType type) {
        for (int i = 0; i < count; i++) {
            final Mindmap map = persistMindmap(entityManager, owner, title(owner, "public-spam", i), true);
            markAsSpam(entityManager, map, type, 1);
        }
    }

    private void addPrivateSpamMaps(@NotNull Account owner, int count, @NotNull SpamStrategyType type) {
        for (int i = 0; i < count; i++) {
            final Mindmap map = persistMindmap(entityManager, owner, title(owner, "private-spam", i), false);
            markAsSpam(entityManager, map, type, 1);
        }
    }

    /** Public maps with no spam-info row at all: the LEFT JOIN yields NULL. */
    private void addPublicCleanMaps(@NotNull Account owner, int count) {
        for (int i = 0; i < count; i++) {
            persistMindmap(entityManager, owner, title(owner, "public-clean", i), true);
        }
    }

    private void addPrivateCleanMaps(@NotNull Account owner, int count) {
        for (int i = 0; i < count; i++) {
            persistMindmap(entityManager, owner, title(owner, "private-clean", i), false);
        }
    }

    /** Public maps with a spam-info row whose spamDetected flag is false. */
    private void addPublicScannedCleanMaps(@NotNull Account owner, int count) {
        for (int i = 0; i < count; i++) {
            final Mindmap map = persistMindmap(entityManager, owner, title(owner, "public-scanned", i), true);
            markAsScannedClean(entityManager, map, 1);
        }
    }

    @NotNull
    private static String title(@NotNull Account owner, @NotNull String kind, int index) {
        return kind + "-" + owner.getId() + "-" + index;
    }

    @NotNull
    private static String code(@NotNull SpamStrategyType type) {
        return String.valueOf(type.getCode());
    }

    // ----------------------------------------------------------- result lookups

    /**
     * Narrows a result set to a single fixture account. Returns {@code null} when
     * the account is absent, which is how exclusion is asserted; absolute result
     * sizes are never used because seed data and sibling tests may contribute
     * rows of their own.
     */
    @Nullable
    private static SpamRatioUserResult ratioFor(@NotNull List<SpamRatioUserResult> results, @NotNull Account account) {
        return results.stream()
                .filter(result -> result.getUser() != null && result.getUser().getId() == account.getId())
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private static SpamUserResult spamFor(@NotNull List<SpamUserResult> results, @NotNull Account account) {
        return results.stream()
                .filter(result -> result.getUser() != null && result.getUser().getId() == account.getId())
                .findFirst()
                .orElse(null);
    }

    @NotNull
    private static List<Integer> userIds(@NotNull List<SpamUserResult> results) {
        return results.stream().map(result -> result.getUser().getId()).toList();
    }
}
