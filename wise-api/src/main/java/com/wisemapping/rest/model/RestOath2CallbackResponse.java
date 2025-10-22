package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestOath2CallbackResponse {

    private String email;
    private Boolean oauthSync;
    private String syncCode;
    private String jwtToken;

    public RestOath2CallbackResponse(@NotNull Account user, @Nullable String jwtToken) {
        this.setEmail(user.getEmail());
        this.setOauthSync(user.getOauthSync());
        this.setSyncCode(user.getSyncCode());
        this.setJwtToken(jwtToken);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getOauthSync() {
        return oauthSync;
    }

    public void setOauthSync(Boolean oauthSync) {
        this.oauthSync = oauthSync;
    }

    public String getSyncCode() {
        return syncCode;
    }

    public void setSyncCode(String syncCode) {
        this.syncCode = syncCode;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
