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

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import com.wisemapping.service.MindmapService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class MapPermissionsSecurityAdvice {
    @Autowired private MindmapService mindmapService;

    protected abstract boolean isAllowed(@Nullable Account user, Mindmap map);

    protected abstract boolean isAllowed(@Nullable Account user, int mapId);

    protected MindmapService getMindmapService() {
        return mindmapService;
    }
}
