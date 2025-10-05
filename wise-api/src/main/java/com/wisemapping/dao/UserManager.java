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

import com.wisemapping.model.AccessAuditory;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Account;
import com.wisemapping.model.SuspensionReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;

public interface UserManager {

    List<Account> getAllUsers();

    Account getUserBy(String email);

    Account getUserBy(int id);

    void createUser(Account user);

    void auditLogin(@NotNull AccessAuditory accessAuditory);

    void updateUser(Account user);

    Account getUserByActivationCode(long code);

    Collaborator getCollaboratorBy(String email);

    Account createUser(Account user, Collaborator col);

    void removeUser(@NotNull Account user);

    /**
     * Get all users with pagination support
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of users for the given page
     */
    List<Account> getAllUsers(int offset, int limit);

    /**
     * Count total number of users
     * @return total count of users
     */
    long countAllUsers();

    /**
     * Get users with search filtering and pagination
     * @param search search term for email, firstname, or lastname
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of filtered users for the given page
     */
    List<Account> searchUsers(String search, int offset, int limit);

    /**
     * Count users matching search criteria
     * @param search search term for email, firstname, or lastname
     * @return total count of matching users
     */
    long countUsersBySearch(String search);

    /**
     * Find users who are inactive since a specific date
     * @param cutoffDate users created before this date and with no activity since
     * @param offset starting position for pagination
     * @param limit maximum number of results to return
     * @return list of inactive users
     */
    List<Account> findUsersInactiveSince(Calendar cutoffDate, int offset, int limit);

    /**
     * Count users who are inactive since a specific date
     * @param cutoffDate users created before this date and with no activity since
     * @return total count of inactive users
     */
    long countUsersInactiveSince(Calendar cutoffDate);

    /**
     * Find the last login date for a user
     * @param userId the user ID
     * @return the last login date, or null if no login found
     */
    @Nullable
    Calendar findLastLoginDate(int userId);

    /**
     * Suspend a user with a specific reason
     * @param user the user to suspend
     * @param reason the suspension reason
     */
    void suspendUser(@NotNull Account user, @NotNull SuspensionReason reason);

}
