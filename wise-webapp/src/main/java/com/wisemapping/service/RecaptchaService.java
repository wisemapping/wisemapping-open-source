package com.wisemapping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.validator.Messages;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecaptchaService {

    final private static Logger logger = Logger.getLogger(RecaptchaService.class);
    final private static String GOOGLE_RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    private final static ObjectMapper objectMapper = new ObjectMapper();
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
            logger.warn("Response from recaptcha after parse: " + responseBody);

            final Boolean success = (Boolean) responseBody.get("success");
            if (success != null && !success) {
                final List<String> errorCodes = (List<String>) responseBody.get("error-codes");
                if (errorCodes.get(0).equals("timeout-or-duplicate")) {
                    result = Messages.CAPTCHA_TIMEOUT_OUT_DUPLICATE;
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