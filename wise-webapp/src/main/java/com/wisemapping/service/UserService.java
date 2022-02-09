/*
*    Copyright [2015] [wisemapping]
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
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;

public interface UserService {

    void activateAccount(long code) throws InvalidActivationCodeException;

    User createUser(@NotNull User user, boolean emailConfirmEnabled, boolean welcomeEmail) throws WiseMappingException;

    void changePassword(@NotNull User user);

    User getUserBy(String email);

    User getUserBy(int id);

    void updateUser(User user);

    void resetPassword(@NotNull String email) throws InvalidUserEmailException, InvalidAuthSchemaException;

    void removeUser(@NotNull User user);

    void auditLogin(@NotNull User user);
    
    User getCasUserBy(String uid);
}
