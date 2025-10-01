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
import org.springframework.context.MessageSource;

import java.util.Locale;

abstract public class ClientException extends WiseMappingException {
    private final Severity severity;

    public ClientException(@NotNull String message, @NotNull Severity severity) {
        super(message);
        this.severity = severity;
    }

    protected abstract
    @NotNull
    String getMsgBundleKey();

    public String getMessage(@NotNull final MessageSource messageSource, final @NotNull Locale locale) {
        String message = messageSource.getMessage(this.getMsgBundleKey(), this.getMsgBundleArgs(), locale);
        if(message==null){
            message = this.getMessage();
        }
        return message;
    }

    protected  Object[] getMsgBundleArgs(){
         return null;
    }

    public Severity getSeverity() {
        return this.severity;
    }

    public String getTechInfo() {
        return getMessage();
    }
}
