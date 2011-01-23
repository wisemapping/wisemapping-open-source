/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

public interface UserService {

    public void activateAcount(long code) throws InvalidActivationCodeException;

    public void createUser(User user) throws WiseMappingException;

    public void changePassword(User user);

    public User getUserBy(String email);

    public User getUserBy(long id);

    public User getUserByUsername(String username);

    public void updateUser(User user);

    public void sendEmailPassword(String email) throws InvalidUserEmailException;

    public User reloadUser(final User user);
}
