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

package com.wisemapping.exceptions;


import jakarta.validation.constraints.NotNull;

public class TooManyInactiveAccountsExceptions
        extends ClientException {
    private static final String TOO_MANY_INACTIVE_ACCOUNTS = "TOO_MANY_INACTIVE_ACCOUNTS";
    private final long accounts;
    private final int userId;
    private final String userEmail;

    public TooManyInactiveAccountsExceptions(@NotNull long accounts, @NotNull int userId, @NotNull String userEmail) {
        super("Too many inactive accounts:" + accounts + " for user ID:" + userId + " (" + userEmail + ")", Severity.WARNING);
        this.accounts = accounts;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public long getAccounts() {
        return accounts;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return TOO_MANY_INACTIVE_ACCOUNTS;
    }
}
