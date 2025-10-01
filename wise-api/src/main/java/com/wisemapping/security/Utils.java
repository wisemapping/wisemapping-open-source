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

package com.wisemapping.security;

import com.wisemapping.model.Account;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

final public class Utils {
    private Utils() {
    }

    @SuppressWarnings({"ConstantConditions"})
    @Nullable
    public static Account getUser() {
        return getUser(false);
    }

    public static Account getUser(boolean forceCheck) {
        Account result = null;
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
