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
package com.wisemapping.rest.model;


import com.wisemapping.exceptions.Severity;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "errors")
@XmlAccessorType(XmlAccessType.PROPERTY)
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
    private List<String> gErrors;

    @JsonIgnore
    MessageSource messageSource;

    @JsonIgnore
    Severity gSeverity;

    public RestErrors() {

    }

    public RestErrors(@NotNull Errors errors, @NotNull MessageSource messageSource) {

        this.errors = errors;
        this.messageSource = messageSource;
        this.gErrors = this.processGlobalErrors(errors, messageSource);
        this.gSeverity = Severity.WARNING;
    }

    public RestErrors(@NotNull String errorMsg, @NotNull Severity severity) {
        gErrors = new ArrayList<String>();
        gErrors.add(errorMsg);
        this.gSeverity = severity;
    }

    private List<String> processGlobalErrors(@NotNull Errors errors, @NotNull MessageSource messageSource) {
        final List<String> result = new ArrayList<String>();
        final List<ObjectError> globalErrors = errors.getGlobalErrors();
        for (ObjectError globalError : globalErrors) {
            result.add(globalError.getObjectName());
        }
        return result;
    }

    public List<String> getGlobalErrors() {
        return gErrors;
    }

    public void setGlobalErrors(List<String> list) {
        // Implemented only for XML serialization contract ...
    }

    public Map<String, String> getFieldErrors() {
        Locale locale = LocaleContextHolder.getLocale();
        final Map<String, String> result = new HashMap<String, String>();
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

    public String getGlobalSeverity() {
        return this.gSeverity.toString();
    }
}
