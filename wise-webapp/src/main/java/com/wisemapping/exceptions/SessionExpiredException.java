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

package com.wisemapping.exceptions;

import com.wisemapping.model.Collaborator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SessionExpiredException
        extends ClientException {
    public static final String MSG_KEY = "MINDMAP_TIMESTAMP_OUTDATED";
    @Nullable
    private Collaborator lastUpdater;

    public SessionExpiredException(@Nullable Collaborator lastUpdater) {
        super("Map has been updated by " + (lastUpdater != null ? lastUpdater.getEmail() : ""), Severity.FATAL);
        this.lastUpdater = lastUpdater;
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return MSG_KEY;
    }
}
