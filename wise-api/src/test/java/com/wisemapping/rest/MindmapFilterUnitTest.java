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

package com.wisemapping.rest;

import com.wisemapping.model.Account;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Mindmap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Direct unit coverage for the defensive null branches of
 * {@link MindmapFilter#STARRED}, which cannot be reached over HTTP: the
 * listing endpoint always pairs a mindmap with a collaboration, and a
 * {@code Collaboration} always carries properties. The behaviour-level
 * coverage of every filter lives in
 * {@code com.wisemapping.test.rest.RestMindmapFilterTest}.
 *
 * <p>This test lives in {@code com.wisemapping.rest} because
 * {@code MindmapFilter.accept} is package-private.
 */
class MindmapFilterUnitTest {

    @Test
    @DisplayName("STARRED treats a mindmap with no collaboration for the user as not starred")
    void starredRejectsAMissingCollaboration() {
        final Account user = account(1);
        final Mindmap mindmap = mindmap(user);

        assertFalse(MindmapFilter.STARRED.accept(mindmap, user, null),
                "Without a collaboration there is no starred flag to read");
    }

    @Test
    @DisplayName("STARRED treats a collaboration without properties as not starred")
    void starredRejectsACollaborationWithoutProperties() {
        final Account user = account(1);
        final Mindmap mindmap = mindmap(user);

        final Collaboration collaboration = new Collaboration(CollaborationRole.OWNER, user, mindmap);
        collaboration.setCollaborationProperties(null);

        assertFalse(MindmapFilter.STARRED.accept(mindmap, user, collaboration),
                "A collaboration whose properties row is missing must not be reported as starred");
    }

    @Test
    @DisplayName("SHARED_WITH_ME is the exact negation of MY_MAPS for the same inputs")
    void sharedWithMeNegatesMyMaps() {
        final Account owner = account(1);
        final Account other = account(2);
        final Mindmap mindmap = mindmap(owner);

        assertTrue(MindmapFilter.MY_MAPS.accept(mindmap, owner, null), "The creator owns the map");
        assertFalse(MindmapFilter.SHARED_WITH_ME.accept(mindmap, owner, null),
                "A map the user created is never 'shared with me'");

        assertFalse(MindmapFilter.MY_MAPS.accept(mindmap, other, null), "A non-creator does not own the map");
        assertTrue(MindmapFilter.SHARED_WITH_ME.accept(mindmap, other, null),
                "A map created by somebody else is 'shared with me'");
    }

    @NotNull
    private static Account account(int id) {
        final Account account = new Account();
        account.setId(id);
        account.setEmail("user-" + id + "@example.org");
        return account;
    }

    @NotNull
    private static Mindmap mindmap(@NotNull Account creator) {
        final Mindmap mindmap = new Mindmap();
        mindmap.setId(100);
        mindmap.setTitle("filter unit test map");
        mindmap.setCreator(creator);
        return mindmap;
    }
}
