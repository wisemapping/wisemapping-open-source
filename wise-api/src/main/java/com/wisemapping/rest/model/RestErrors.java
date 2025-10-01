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
package com.wisemapping.rest.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wisemapping.exceptions.Severity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.*;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestErrors {
    @JsonIgnore
    private Errors errors;

    @JsonIgnore
    private List<String> globalError;

    @JsonIgnore
    private  MessageSource messageSource;

    @JsonIgnore
    private Severity globalSeverity;

    @Nullable
    @JsonIgnore
    private String _debugInfo;

    public RestErrors() {

    }

    public RestErrors(@NotNull Errors errors, @NotNull MessageSource messageSource) {

        this.errors = errors;
        this.messageSource = messageSource;
        this.globalError = this.processGlobalErrors(errors);
        this.globalSeverity = Severity.WARNING;
    }

    public RestErrors(@NotNull String errorMsg, @NotNull Severity severity) {

        this(errorMsg, severity, null);
    }

    public RestErrors(@NotNull String errorMsg, @NotNull Severity severity, @Nullable String debugInfo) {
        this._debugInfo = debugInfo;
        this.globalError = new ArrayList<>();
        this.globalError.add(errorMsg);
        this.globalSeverity = severity;
    }

    private List<String> processGlobalErrors(@NotNull Errors errors) {
        final List<String> result = new ArrayList<>();
        final List<ObjectError> globalErrors = errors.getGlobalErrors();
        for (ObjectError globalError : globalErrors) {
            result.add(globalError.getObjectName());
        }
        return result;
    }

    public void setGlobalErrors(List<String> list) {
        // Implemented only for XML serialization contract ...
    }

    public Map<String, String> getFieldErrors() {
        Locale locale = LocaleContextHolder.getLocale();
        final Map<String, String> result = new HashMap<>();
        if (errors != null) {
            final List<FieldError> fieldErrors = errors.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                result.put(fieldError.getField(), messageSource.getMessage(fieldError, locale));
            }
        }
        return result;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        // Implemented only for XML serialization contract ...
    }

    public void setGlobalSeverity(@NotNull String severity) {
        // Implemented only for XML serialization contract ...
    }

    public void setDebugInfo(@Nullable String debugInfo) {
        // Implemented only for XML serialization contract ...
    }


    @Nullable
    public String getGlobalSeverity() {
        return this.globalSeverity.toString();
    }

    @Nullable
    public String getDebugInfo() {
        return _debugInfo;
    }

    public List<String> getGlobalErrors() {
        return globalError;
    }

    @Override
    public String toString() {
        return "RestErrors{" +
                "errors=" + errors +
                ", gErrors=" + globalError +
                ", messageSource=" + messageSource +
                ", gSeverity=" + globalSeverity +
                ", _debugInfo='" + _debugInfo + '\'' +
                '}';
    }
}
