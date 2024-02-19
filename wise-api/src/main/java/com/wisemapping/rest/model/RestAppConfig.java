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

package com.wisemapping.rest.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;


@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestAppConfig {
    private String apiBaseUrl;
    private String googleOauth2Url;
    private boolean registrationEnabled;
    private boolean recaptcha2Enabled;
    private String recaptcha2SiteKey;
    private String analyticsAccount;

    RestAppConfig() {

    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getGoogleOauth2Url() {
        return googleOauth2Url;
    }

    public void setGoogleOauth2Url(String googleOauth2Url) {
        this.googleOauth2Url = googleOauth2Url;
    }

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public boolean isRecaptcha2Enabled() {
        return recaptcha2Enabled;
    }

    public void setRecaptcha2Enabled(boolean recaptcha2Enabled) {
        this.recaptcha2Enabled = recaptcha2Enabled;
    }

    public String getRecaptcha2SiteKey() {
        return recaptcha2SiteKey;
    }

    public void setRecaptcha2SiteKey(String recaptcha2SiteKey) {
        this.recaptcha2SiteKey = recaptcha2SiteKey;
    }

    public String getAnalyticsAccount() {
        return analyticsAccount;
    }

    public void setAnalyticsAccount(String analyticsAccount) {
        this.analyticsAccount = analyticsAccount;
    }

    public static class RestAppConfigBuilder {
        private String apiBaseUrl;
        private String googleOauth2Url;
        private boolean registrationEnabled;
        private boolean isCatchaEnabled = false;
        private String captchaSiteKey;
        private String analyticsAccount;

        public RestAppConfigBuilder setCaptchaSiteKey(@NotNull String captchaSiteKey) {
            this.captchaSiteKey = captchaSiteKey;
            this.isCatchaEnabled = true;
            return this;
        }

        public RestAppConfigBuilder setApiUrl(@NotNull String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            return this;
        }

        public RestAppConfigBuilder setGoogleOauth2Url(@NotNull String googleOauth2Url) {
            this.googleOauth2Url = googleOauth2Url;
            return this;
        }

        private void setGoogleAnalyticsAccount(@NotNull String analyticsAccount) {
            this.analyticsAccount = analyticsAccount;
        }

        public RestAppConfigBuilder setRegistrationEnabled(@NotNull boolean registrationEnabled) {
            this.registrationEnabled = registrationEnabled;
            return this;
        }

        public RestAppConfigBuilder setAnalyticsAccount(@NotNull String analyticsAccount) {
            this.analyticsAccount = analyticsAccount;
            return this;
        }

        @NotNull
        public RestAppConfig build() {
            final RestAppConfig result = new RestAppConfig();
            result.googleOauth2Url = googleOauth2Url;
            result.recaptcha2SiteKey = captchaSiteKey;
            result.recaptcha2Enabled = isCatchaEnabled;
            result.apiBaseUrl = apiBaseUrl;
            result.registrationEnabled = registrationEnabled;
            result.analyticsAccount = analyticsAccount;
            return result;
        }
    }

}
