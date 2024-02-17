/*
 *    Copyright [2022] [wisemapping]
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

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import com.wisemapping.service.LockManager;
import org.jetbrains.annotations.NotNull;


public class LockException
        extends ClientException {
    private static final String MSG_KEY = "MINDMAP_IS_LOCKED";

    public LockException(@NotNull String message) {
        super(message, Severity.INFO);
    }

    public static LockException createLockLost(@NotNull Mindmap mindmap, @NotNull Account user, @NotNull LockManager manager) {
        return new LockException("Lock can not be granted to " + user.getEmail() + ". The lock is assigned to " + manager.getLockInfo(mindmap));
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return MSG_KEY;
    }
}
