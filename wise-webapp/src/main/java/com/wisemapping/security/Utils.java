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

package com.wisemapping.security;

import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;

final public class Utils {
    private Utils() {
    }

    @SuppressWarnings({"ConstantConditions"})
    @Nullable
    public static User getUser() {
        return getUser(false);
    }

    @Nullable
    public static User getUser(boolean forceCheck) {
        User result = null;
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() != null)
        {
            final Object principal = auth.getPrincipal();
            if (principal != null && principal instanceof UserDetails) {
                result = ((UserDetails)principal).getUser();
            }
        }

        if(result==null && forceCheck){
            throw new IllegalStateException("User could not be retrieved");
        }
        return result;
    }
}
