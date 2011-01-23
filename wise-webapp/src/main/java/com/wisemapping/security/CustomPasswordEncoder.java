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

package com.wisemapping.security;

import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.springframework.dao.DataAccessException;

public class CustomPasswordEncoder
    implements PasswordEncoder
{
    private PasswordEncoder delegateEncoder;
    private static final String ENC_PREFIX = "ENC:";

    public void setDelegatedEncoder(PasswordEncoder delegateEncoder)
    {
        this.delegateEncoder = delegateEncoder;
    }
    
    public String encodePassword(String rawPass, Object salt) throws DataAccessException {

        String password = rawPass;
        if (!rawPass.startsWith(ENC_PREFIX))
        {
            password = ENC_PREFIX + delegateEncoder.encodePassword(rawPass,salt);
        }

        return password;
    }

    public boolean isPasswordValid(String encPass, String rawPass, Object salt) throws DataAccessException {

        String pass1 = "" + encPass;
        String pass2 = rawPass;

        if (pass1.startsWith(ENC_PREFIX))
        {

            pass2 = encodePassword(rawPass, salt);
        }

        return pass1.equals(pass2);
    }
}
