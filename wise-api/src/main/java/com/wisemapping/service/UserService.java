/*
*    Copyright [2022] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
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

	Account confirmAccountSync(@NotNull String email, @NotNull String code) throws WiseMappingException;

    void changePassword(@NotNull Account user);

    Account getUserBy(String email);

    Account getUserBy(int id);

    void updateUser(Account user);

    RestResetPasswordResponse resetPassword(@NotNull String email) throws InvalidUserEmailException, InvalidAuthSchemaException;

    void removeUser(@NotNull Account user);

    void auditLogin(@NotNull Account user);
    
    Account getCasUserBy(String uid);
}
