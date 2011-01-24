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

package com.wisemapping.dwr;

import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.WebContext;

import javax.servlet.http.HttpServletRequest;

abstract public class BaseDwrService {
    private MindmapService mindmapService;
    private UserService userService;

    public MindmapService getMindmapService() {
        return mindmapService;
    }

    public void setMindmapService(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public User getUser() {

        WebContext ctx = WebContextFactory.get();
        final HttpServletRequest request = ctx.getHttpServletRequest();

        return Utils.getUser(request);
    }

}
