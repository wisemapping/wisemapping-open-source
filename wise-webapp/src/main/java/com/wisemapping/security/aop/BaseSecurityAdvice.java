/*
*    Copyright [2011] [wisemapping]
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

package com.wisemapping.security.aop;

import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.UnexpectedArgumentException;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import org.aopalliance.intercept.MethodInvocation;

public abstract class BaseSecurityAdvice {
    private MindmapService mindmapService = null;

    public void checkRole(MethodInvocation methodInvocation) throws UnexpectedArgumentException,AccessDeniedSecurityException
    {
        final User user = Utils.getUser();

        final Object argument = methodInvocation.getArguments()[0];

        boolean isAllowed;

        if (argument instanceof MindMap)
        {
            isAllowed = isAllowed(user,(MindMap)  argument);
        }
        else if (argument instanceof Integer)
        {
            isAllowed = isAllowed(user, ((Integer)argument));
        }
        else
        {
            throw new UnexpectedArgumentException("Argument " +argument);
        }

        if (!isAllowed)
        {
            throw new AccessDeniedSecurityException("User not allowed to invoke:" + methodInvocation);
        }
    }

    protected abstract boolean isAllowed(User user, MindMap map);
    protected abstract boolean isAllowed(User user, int mapId);

    protected MindmapService getMindmapService()
    {
        return mindmapService;
    }

    public void setMindmapService(MindmapService service)
    {
        this.mindmapService = service;
    }
}
