package com.wisemapping.rest.model;


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

    public RestErrors() {

    }

    public RestErrors(@NotNull Errors errors, @NotNull MessageSource messageSource) {

        this.errors = errors;
        this.messageSource = messageSource;
        this.gErrors = this.processGlobalErrors(errors, messageSource);
    }

    public RestErrors(@NotNull String errorMsg) {
        gErrors = new ArrayList<String>();
        gErrors.add(errorMsg);
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


}
