/*
*    Copyright [2012] [wisemapping]
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

import com.wisemapping.model.User;
import com.wisemapping.exceptions.WiseMappingException;
import org.jetbrains.annotations.NotNull;

public interface UserService {

    public void activateAccount(long code) throws InvalidActivationCodeException;

    public User createUser(@NotNull User user, boolean emailConfirmEnabled,boolean welcomeEmail) throws WiseMappingException;

    public void changePassword(@NotNull User user);

    public User getUserBy(String email);

    public User getUserBy(long id);

    public void updateUser(User user);

    public void resetPassword(@NotNull String email) throws InvalidUserEmailException;

    public void deleteUser(@NotNull User user);

    public void auditLogin(@NotNull User user);
    
    public User getCasUserBy(String uid);
}
