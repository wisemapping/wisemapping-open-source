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

package com.wisemapping.rest;

import com.wisemapping.rest.model.RestAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restful/app")
public class AppController {

    @Autowired
    private Environment environment;

    @Value("${app.registration.enabled:true}")
    private Boolean isRegistrationEnabled;

    @Value("${app.registration.captcha.enabled:false}")
    private Boolean isCaptchaEnabled;

    @Value("${app.registration.captcha.siteKey:}")
    private String captchaSiteKey;

    @Value("${app.site.api-base-url:}")
    private String apiBaseUrl;

    @Value("${app.site.ui-base-url:}")
    private String uiBaseUrl;

    @Value("${app.analytics.account:}")
    private String analyticsAccount;

    @Value("${app.jwt.expirationMin:10080}")
    private int jwtExpirationMin;

    @RequestMapping(method = RequestMethod.GET, value = "/config")
    @ResponseStatus(value = HttpStatus.OK)
    public RestAppConfig appConfig() {
        // Detect enabled OAuth2 providers based on existing client registrations
        boolean googleEnabled = isRegistrationPresent("google");
        boolean facebookEnabled = isRegistrationPresent("facebook");
        
        RestAppConfig.RestAppConfigBuilder builder = new RestAppConfig.RestAppConfigBuilder()
                .setApiUrl(apiBaseUrl)
                .setUiUrl(uiBaseUrl)
                .setCaptchaSiteKey(captchaSiteKey)
                .setCaptchaEnabled(isCaptchaEnabled)
                .setGoogleOauth2Enabled(googleEnabled)
                .setFacebookOauth2Enabled(facebookEnabled)
                .setAnalyticsAccount(analyticsAccount)
                .setRegistrationEnabled(isRegistrationEnabled)
                .setJwtExpirationMin(jwtExpirationMin);
        
        // Only set OAuth2 URLs if providers are configured
        if (googleEnabled) {
            builder.setGoogleOauth2Url(apiBaseUrl + "/oauth2/authorization/google");
        }
        if (facebookEnabled) {
            builder.setFacebookOauth2Url(apiBaseUrl + "/oauth2/authorization/facebook");
        }
        
        return builder.build();
    }

    private boolean isRegistrationPresent(String registrationId) {
        // Check if spring.security.oauth2.client.registration.{registrationId}.client-id is configured
        String clientIdProperty = String.format("spring.security.oauth2.client.registration.%s.client-id", registrationId);
        String clientId = environment.getProperty(clientIdProperty);
        // Registration is present if client-id is configured and not empty
        return clientId != null && !clientId.trim().isEmpty();
    }


    public Boolean getRegistrationEnabled() {
        return isRegistrationEnabled;
    }

    public void setRegistrationEnabled(Boolean registrationEnabled) {
        isRegistrationEnabled = registrationEnabled;
    }

    public Boolean getCaptchaEnabled() {
        return isCaptchaEnabled;
    }

    public void setCaptchaEnabled(Boolean captchaEnabled) {
        isCaptchaEnabled = captchaEnabled;
    }

    public String getCaptchaSiteKey() {
        return captchaSiteKey;
    }

    public void setCaptchaSiteKey(String captchaSiteKey) {
        this.captchaSiteKey = captchaSiteKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
}
