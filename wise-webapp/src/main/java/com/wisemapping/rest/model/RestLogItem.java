package com.wisemapping.rest.model;


import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "logitem")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestLogItem {

    private String jsStack;
    private String userAgent;
    private String jsErrorMsg;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    private int mapId;

    public String getJsStack() {
        return jsStack;
    }

    public void setJsStack(@NotNull String jsStack) {
        this.jsStack = jsStack;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getJsErrorMsg() {
        return jsErrorMsg;
    }

    public void setJsErrorMsg(String jsErrorMsg) {
        this.jsErrorMsg = jsErrorMsg;
    }
}
