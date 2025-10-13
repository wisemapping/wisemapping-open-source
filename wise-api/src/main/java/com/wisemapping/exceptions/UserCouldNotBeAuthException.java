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

import org.jetbrains.annotations.NotNull;

public class UserCouldNotBeAuthException extends ClientException {
    
    private static final String MSG_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    private static final String MSG_ACCOUNT_SUSPENDED = "ACCOUNT_SUSPENDED";
    private static final String MSG_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    
    private final String msgKey;
    
    private UserCouldNotBeAuthException(@NotNull String msgKey, @NotNull String debugMessage) {
        super(debugMessage, Severity.WARNING);
        this.msgKey = msgKey;
    }
    
    private UserCouldNotBeAuthException(@NotNull String msgKey, @NotNull String debugMessage, Throwable cause) {
        super(debugMessage, Severity.WARNING);
        this.msgKey = msgKey;
        initCause(cause);
    }
    
    // Factory methods for specific authentication failures
    
    public static UserCouldNotBeAuthException invalidCredentials() {
        return new UserCouldNotBeAuthException(MSG_INVALID_CREDENTIALS, "Invalid credentials");
    }
    
    public static UserCouldNotBeAuthException invalidCredentials(Throwable cause) {
        return new UserCouldNotBeAuthException(MSG_INVALID_CREDENTIALS, "Invalid credentials: " + cause.getMessage(), cause);
    }
    
    public static UserCouldNotBeAuthException accountSuspended() {
        return new UserCouldNotBeAuthException(MSG_ACCOUNT_SUSPENDED, "Account suspended");
    }
    
    public static UserCouldNotBeAuthException accountSuspended(Throwable cause) {
        return new UserCouldNotBeAuthException(MSG_ACCOUNT_SUSPENDED, "Account suspended: " + cause.getMessage(), cause);
    }
    
    public static UserCouldNotBeAuthException accountDisabled() {
        return new UserCouldNotBeAuthException(MSG_ACCOUNT_DISABLED, "Account disabled/not activated");
    }
    
    public static UserCouldNotBeAuthException accountDisabled(Throwable cause) {
        return new UserCouldNotBeAuthException(MSG_ACCOUNT_DISABLED, "Account disabled: " + cause.getMessage(), cause);
    }
    
    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return msgKey;
    }
}