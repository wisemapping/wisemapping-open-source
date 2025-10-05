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

package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestResetPasswordResponse;

import org.jetbrains.annotations.NotNull;

public interface UserService {

    void activateAccount(long code) throws InvalidActivationCodeException;

    Account createUser(@NotNull Account user, boolean emailConfirmEnabled, boolean welcomeEmail) throws WiseMappingException;

	Account createAndAuthUserFromGoogle(@NotNull String callbackCode) throws WiseMappingException;

	Account createAndAuthUserFromFacebook(@NotNull String callbackCode) throws WiseMappingException;

	Account confirmGoogleAccountSync(@NotNull String email, @NotNull String code) throws WiseMappingException;

    void changePassword(@NotNull Account user);

    Account getUserBy(String email);

    Account getUserBy(int id);

    java.util.List<Account> getAllUsers();

    void updateUser(Account user);

    RestResetPasswordResponse resetPassword(@NotNull String email) throws InvalidUserEmailException, InvalidAuthSchemaException;

    void removeUser(@NotNull Account user);

    void auditLogin(@NotNull Account user);
    
    Account getCasUserBy(String uid);

    /**
     * Get all users with pagination support (admin only)
     * @param page page number (0-based)
     * @param pageSize number of users per page
     * @return list of users for the given page
     */
    java.util.List<Account> getAllUsers(int page, int pageSize);

    /**
     * Count total number of users (admin only)
     * @return total count of users
     */
    long countAllUsers();

    /**
     * Search users with pagination support (admin only)
     * @param search search term for email, firstname, or lastname
     * @param page page number (0-based)
     * @param pageSize number of users per page
     * @return list of filtered users for the given page
     */
    java.util.List<Account> searchUsers(String search, int page, int pageSize);

    /**
     * Count users matching search criteria (admin only)
     * @param search search term for email, firstname, or lastname
     * @return total count of matching users
     */
    long countUsersBySearch(String search);

    /**
     * Unsuspend a user and restore their mindmaps if they were suspended for inactivity
     * @param user the user to unsuspend
     * @return number of mindmaps restored, or 0 if none were restored
     */
    int unsuspendUser(@NotNull Account user);
}
