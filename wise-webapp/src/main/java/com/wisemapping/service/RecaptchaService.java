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
package com.wisemapping.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.validator.Messages;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.jetbrains.annotations.Nullable;

import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecaptchaService {

    final private static Logger logger = LogManager.getLogger();
    final private static String GOOGLE_RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static final String CATCH_ERROR_CODE_TIMEOUT_OR_DUPLICATE = "timeout-or-duplicate";
    public static final String CATCHA_ERROR_CODE_INPUT_RESPONSE = "invalid-input-response";
    private String recaptchaSecret;

    @Nullable
    public String verifyRecaptcha(@NotNull String ip, @NotNull String recaptcha) {

        final List<NameValuePair> build = Form.form()
                .add("secret", recaptchaSecret)
                .add("response", recaptcha)
                .add("remoteip", ip)
                .build();

        // Add logs ...
        logger.debug("Response from remoteip: " + ip);
        logger.debug("Response from recaptchaSecret: " + recaptchaSecret);
        logger.debug("Response from recaptcha: " + recaptcha);

        String result = StringUtils.EMPTY;
        try {
            final byte[] body = Request
                    .Post(GOOGLE_RECAPTCHA_VERIFY_URL)
                    .bodyForm(build)
                    .execute()
                    .returnContent()
                    .asBytes();

            final Map responseBody = objectMapper.readValue(body, HashMap.class);
            logger.debug("Response from recaptcha after parse: " + responseBody);

            final Boolean success = (Boolean) responseBody.get("success");
            if (success != null && !success) {
                final List<String> errorCodes = (List<String>) responseBody.get("error-codes");
                String errorCode = errorCodes.get(0);
                if (errorCode.equals(CATCH_ERROR_CODE_TIMEOUT_OR_DUPLICATE)) {
                    result = Messages.CAPTCHA_TIMEOUT_OUT_DUPLICATE;

                } else if (errorCode.equals("invalid-input-response")) {
                    result = Messages.CAPTCHA_INVALID_INPUT_RESPONSE;
                } else {
                    result = Messages.CAPTCHA_LOADING_ERROR;
                    logger.error("Unexpected error during catch resolution:" + errorCodes);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            result = e.getMessage();
        }

        logger.debug("Captcha Result:" + result);
        return result;

    }

    public void setRecaptchaSecret(String recaptchaSecret) {
        this.recaptchaSecret = recaptchaSecret;
    }
}