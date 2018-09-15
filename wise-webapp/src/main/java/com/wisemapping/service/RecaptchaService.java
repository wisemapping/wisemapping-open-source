package com.wisemapping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

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

    public String verifyRecaptcha(String ip, String recaptchaResponse) {

        final List<NameValuePair> build = Form.form()
                .add("secret", recaptchaSecret)
                .add("response", recaptchaResponse)
                .add("remoteip", ip)
                .build();

        // Add logs ...
        logger.debug("Response from remoteip: " + ip);
        logger.debug("Response from recaptchaSecret: " + recaptchaSecret);
        logger.debug("Response from recaptchaResponse: " + recaptchaResponse);

        String result = StringUtils.EMPTY;
        HashMap bodyJson;
        try {
            final byte[] body = Request
                    .Post(GOOGLE_RECAPTCHA_VERIFY_URL)
                    .bodyForm(build)
                    .execute()
                    .returnContent()
                    .asBytes();

            bodyJson = objectMapper
                    .readValue(body, HashMap.class);

            logger.debug("Response from recaptcha after parse: " + bodyJson);

            final Boolean success = (Boolean) bodyJson.get("success");
            if (!success) {
                final List<String> errorCodes = (List<String>) bodyJson
                        .get("error-codes");
                result = RecaptchaUtil.RECAPTCHA_ERROR_CODE.get(errorCodes.get(0));
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

class RecaptchaUtil {

    static final Map<String, String>
            RECAPTCHA_ERROR_CODE = new HashMap<>();

    static {
        RECAPTCHA_ERROR_CODE.put("missing-input-secret",
                "The secret parameter is missing");
        RECAPTCHA_ERROR_CODE.put("invalid-input-secret",
                "The secret parameter is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("missing-input-response",
                "The response parameter is missing");
        RECAPTCHA_ERROR_CODE.put("invalid-input-response",
                "The response parameter is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("bad-request",
                "The request is invalid or malformed");
    }
}