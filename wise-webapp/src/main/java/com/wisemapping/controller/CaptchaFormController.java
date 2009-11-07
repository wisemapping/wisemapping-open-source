/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

package com.wisemapping.controller;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

/**
 * Convenient superclass for form controller implementations which need
 * CAPTCHA support.
 */
public class CaptchaFormController extends SimpleFormController {
    /**
     * Default paramter name for CAPTCHA response in <code>{@link HttpServletRequest}</code>
     */
    private static final String DEFAULT_CAPTCHA_RESPONSE_PARAMETER_NAME = "j_captcha_response";

    protected ImageCaptchaService captchaService;
    protected String captchaResponseParameterName = DEFAULT_CAPTCHA_RESPONSE_PARAMETER_NAME;

    /**
     * Delegates request to CAPTCHA validation, subclasses which overrides this
     * method must manually call <code>{@link #validateCaptcha(HttpServletRequest,BindException)}</code>
     * or explicitly call super method.
     *
     * @see #validateCaptcha(HttpServletRequest,BindException)
     * @see org.springframework.web.servlet.mvc.BaseCommandController#onBindAndValidate(javax.servlet.http.HttpServletRequest,java.lang.Object,org.springframework.validation.BindException)
     */
    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        validateCaptcha(request, errors);
    }

    /**
     * Validate CAPTCHA response, if response isn`t valid creates new error object
     * and put him to errors holder.
     *
     * @param request current servlet request
     * @param errors  errors holder
     */
    protected void validateCaptcha(HttpServletRequest request, BindException errors) {
        boolean isResponseCorrect = false;

        //remenber that we need an id to validate!
        String captchaId = request.getSession().getId();
        //retrieve the response
        String response = request.getParameter(captchaResponseParameterName);
        //validate response
        try {
            if (response != null) {
                isResponseCorrect =
                        captchaService.validateResponseForID(captchaId, response);
            }
        } catch (CaptchaServiceException e) {
            //should not happen, may be thrown if the id is not valid
        }

        if (!isResponseCorrect) {
            //prepare object error, captcha response isn`t valid
//	        String objectName = "Captcha";
//			String[] codes = {"invalid"};
//			Object[] arguments = {};
//			String defaultMessage = "Wrong control text!";
//			ObjectError oe = new ObjectError(objectName, codes, arguments, defaultMessage);
//
//            errors.addError(oe);
            errors.rejectValue("captcha", Messages.CAPTCHA_ERROR);
        }
    }

    /**
     * Set captcha service
     *
     * @param captchaService the captchaService to set.
     */
    public void setCaptchaService(ImageCaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * Set paramter name for CAPTCHA response in <code>{@link HttpServletRequest}</code>
     *
     * @param captchaResponseParameterName the captchaResponseParameterName to set.
     */
    public void setCaptchaResponseParameterName(String captchaResponseParameterName) {
        this.captchaResponseParameterName = captchaResponseParameterName;
	}
}

