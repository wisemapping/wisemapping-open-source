/*
*    Copyright [2011] [wisemapping]
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

import com.sun.istack.internal.NotNull;
import com.wisemapping.util.*;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class WiseUserAgent implements Serializable {
    public static final String USER_AGENT_HEADER = "User-Agent";
    private UserAgent userAgent;
    private String header;

    private WiseUserAgent(@NotNull final String header) {
        this.header = header;
        this.userAgent = new UserAgent(header);

    }

    public static WiseUserAgent create(@NotNull final HttpServletRequest request) {
        return new WiseUserAgent(request.getHeader(USER_AGENT_HEADER));
    }

    public boolean isBrowserSupported() {

        final Browser browser = userAgent.getBrowser();
        final Version version = userAgent.getBrowserVersion();
        final OperatingSystem os = userAgent.getOperatingSystem();

        boolean result = browser == Browser.FIREFOX && Integer.parseInt(version.getMajorVersion()) >= 10;
        result = result || browser == Browser.IE8 || browser == Browser.IE9;
        result = result || browser == Browser.IE && Integer.parseInt(version.getMajorVersion()) >= 8;
        result = result || browser == Browser.OPERA && Integer.parseInt(version.getMajorVersion()) >= 11;
        result = result || browser == Browser.CHROME && Integer.parseInt(version.getMajorVersion()) >= 18;
        result = result || browser == Browser.SAFARI5;
        result = result || browser == Browser.MOBILE_SAFARI && Integer.parseInt(version.getMajorVersion()) >= 5;
        result = result || os.isMobileDevice() && (os == OperatingSystem.ANDROID || os == OperatingSystem.iOS4_IPHONE);
        result = result || browser.getBrowserType() == BrowserType.ROBOT;

        return result;
    }

    public boolean needsGCF() {
        final Browser browser = userAgent.getBrowser();
        final Version version = userAgent.getBrowserVersion();
        final OperatingSystem os = userAgent.getOperatingSystem();


        return browser == Browser.IE && Integer.parseInt(version.getMajorVersion()) >= 8 && os == OperatingSystem.WINDOWS && header.contains("chromeframe");
    }

    public static WiseUserAgent create(@NotNull final String userAgent) {
        return new WiseUserAgent(userAgent);
    }
}
