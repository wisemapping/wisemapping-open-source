/*
*    Copyright [2015] [wisemapping]
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

package com.wisemapping.filter;

import com.wisemapping.util.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class SupportedUserAgent implements Serializable {
    public static final String USER_AGENT_HEADER = "User-Agent";
    transient private UserAgent userAgent;
    private String header;

    private SupportedUserAgent(@NotNull final String header) {
        this.header = header;
    }

    public static SupportedUserAgent create(@NotNull final HttpServletRequest request) {
        return new SupportedUserAgent(request.getHeader(USER_AGENT_HEADER));
    }

    public boolean isBrowserSupported() {

//        final UserAgent userAgent = this.getUserAgent();
//        final Browser browser = userAgent.getBrowser();
//        final OperatingSystem os = userAgent.getOperatingSystem();
//        final Version version = userAgent.getBrowserVersion();
//        final int majorVersion = version != null ? Integer.parseInt(version.getMajorVersion()) : -1;
//
//        boolean result = browser == Browser.FIREFOX && majorVersion >= 10;
//        result = result || browser == Browser.FIREFOX2 && majorVersion >= 17;
//        result = result || browser == Browser.FIREFOX3 && majorVersion >= 29;
//        result = result || browser == Browser.FIREFOX4 && majorVersion >= 40;
//        result = result || browser == Browser.IE8 || browser == Browser.IE9 || browser == Browser.IE11 ;
//        result = result || browser == Browser.IE && majorVersion >= 8;
//        result = result || browser == Browser.OPERA10 && majorVersion >= 11;
//        result = result || browser == Browser.CHROME && majorVersion >= 18;
//        result = result || browser == Browser.SAFARI5;
//        result = result || browser == Browser.SAFARI && majorVersion >= 5;
//        result = result || browser == Browser.MOBILE_SAFARI;
//        result = result || os.isMobileDevice() && (os == OperatingSystem.ANDROID || os == OperatingSystem.iOS4_IPHONE);
//        result = result || browser.getBrowserType() == BrowserType.ROBOT;

        return true;
    }

    @NotNull
    synchronized
    private UserAgent getUserAgent() {
        if (userAgent == null) {
            userAgent = new UserAgent(header);
        }
        return userAgent;
    }

    public boolean needsGCF() {
        final UserAgent userAgent = this.getUserAgent();
        final Browser browser = userAgent.getBrowser();
        final Version version = userAgent.getBrowserVersion();

        return (browser == Browser.IE8 || browser == Browser.IE && Integer.parseInt(version.getMajorVersion()) == 8) && !header.contains("chromeframe");
    }

    public static SupportedUserAgent create(@NotNull final String userAgent) {
        return new SupportedUserAgent(userAgent);
    }


}
