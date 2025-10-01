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
import org.jetbrains.annotations.NotNull;

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

}
