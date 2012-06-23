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

import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class UserAgent implements Serializable {
    public static final String USER_AGENT_HEADER = "User-Agent";
    private int versionMajor = -1;
    private int versionVariation = -1;
    private Product product;
    private OS os;
    private final org.apache.commons.logging.Log logger = LogFactory.getLog(UserAgent.class.getName());
    private boolean hasGCFInstalled = false;

    public static void main(final String argv[]) {
        UserAgent explorer = UserAgent.create("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
//        UserAgent firefox = UserAgent.create("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050302 Firefox/0.9.6");
        UserAgent safari = UserAgent.create("iCab/2.9.5 (Macintosh; U; PPC; Mac OS X)");
        UserAgent opera = UserAgent.create("Opera/9.21 (Windows NT 5.1; U; en)");


        UserAgent firefox = UserAgent.create("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050302 Firefox/1.9.6");
        assert firefox.isBrowserSupported();


        firefox = UserAgent.create("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7 Creative ZENcast v1.02.08 FirePHP/0.0.5.13");
        assert firefox.isBrowserSupported();

        firefox = UserAgent.create("Mozilla/5.0 (X11; U; Linux i686; es-ES; rv:1.8.0.12) Gecko/20070731 Ubuntu/dapper-security Firefox/1.5.0.12");
        assert firefox.isBrowserSupported();

        firefox = UserAgent.create("'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 firefox 2.0'");
        assert firefox.isBrowserSupported();

        firefox = UserAgent.create("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.12) Gecko/20080129 Iceweasel/2.0.0.12 (Debian-2.0.0.12-0etch1)");
        assert firefox.isBrowserSupported();

    }


    public boolean isVersionGreatedOrEqualThan(final int mayor, final int variation) {
        return this.versionMajor > mayor || (mayor == this.versionMajor && this.versionVariation >= variation);
    }

    public boolean isVersionLessThan(final int mayor) {
        return this.versionMajor < mayor;
    }

    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionVariation() {
        return versionVariation;
    }

    public Product getProduct() {
        return product;
    }

    public OS getOs() {
        return os;
    }

    public enum Product {
        EXPLORER, FIREFOX, CAMINO, NETSCAPE, OPERA, SAFARI, CHROME, KONQUEOR, KMELEON, MOZILLA, LYNX, ROBOT;
    }

    public enum OS {
        WINDOWS, LINUX, MAC, KNOWN
    }


    private UserAgent(final String header) {
        parse(header);
    }

    private void parse(String userAgentHeader) {
        // Format ApplicationName/ApplicationVersion ();

        try {
            int detailStart = userAgentHeader.indexOf('(');
            int detailEnd = userAgentHeader.indexOf(')');

            // Parse base format = application (productDetails) productAddition
            String application = userAgentHeader.substring(0, detailStart);
            application = application.trim();

            String productDetails = userAgentHeader.substring(detailStart + 1, detailEnd);
            productDetails = productDetails.trim();

            String productAddition = userAgentHeader.substring(detailEnd + 1, userAgentHeader.length());
            productAddition = productAddition.trim();

            this.os = parseOS(productDetails);

            if (userAgentHeader.contains("MSIE")) {
                // Explorer Browser : http://msdn2.microsoft.com/en-us/library/ms537503.aspx
                // Format: Mozilla/MozVer (compatible; MSIE IEVer[; Provider]; Platform[; Extension]*) [Addition]
                // SampleTest: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; Google Wireless Transcoder;)

                // Parse version ...
                int index = productDetails.indexOf("MSIE") + 4;
                int lastIndex = productDetails.indexOf(';', index);

                final String versionStr = productDetails.substring(index + 1, lastIndex);
                parseVersion(versionStr);

                // Explorer Parse ...
                this.product = Product.EXPLORER;
                this.hasGCFInstalled = productDetails.contains("chromeframe");
            } else if (userAgentHeader.contains("iCab") || userAgentHeader.contains("Safari")) {
                // Safari:
                //Formats:  Mozilla/5.0 (Windows; U; Windows NT 5.1; en) AppleWebKit/522.13.1 (KHTML, like Gecko) Version/3.0.2 Safari/522.13.1
                //Chrome:
                //Formats: "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.44 Safari/534.7"
                String versionStr = "";
                if (userAgentHeader.contains("Chrome")) {
                    this.product = Product.CHROME;
                    versionStr = userAgentHeader.substring(userAgentHeader.indexOf("Chrome") + 7, userAgentHeader.lastIndexOf(" "));
                } else {
                    this.product = Product.SAFARI;
                    versionStr = userAgentHeader.substring(userAgentHeader.indexOf("Version") + 8, userAgentHeader.lastIndexOf(" "));
                }

                parseVersion(versionStr);

            } else if (userAgentHeader.contains("Konqueror")) {
                this.product = Product.KONQUEOR;
            } else if (userAgentHeader.contains("KMeleon")) {
                this.product = Product.KMELEON;
            } else if (userAgentHeader.contains("Gecko")) {
                // Firefox/Mozilla/Camino:
                // Mozilla/MozVer (Platform; Security; SubPlatform; Language; rv:Revision[; Extension]*) Gecko/GeckVer [Product/ProdVer]
                // SampleTest: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050302 Firefox/0.9.6
                // Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7 Creative ZENcast v1.02.08 FirePHP/0.0.5.13
                // 'Mozilla/5.0 (Windows; U; Windows NT 5.1; es-ES; rv:1.7.12) Gecko/20050915'
                // 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 firefox 2.0'
                // 'Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/20060601 Firefox/2.0.0.6 (Ubuntu-edgy)'
                // 'Mozilla/5.0 (X11; U; Linux i686; es-ES; rv:1.8.0.12) Gecko/20070731 Ubuntu/dapper-security Firefox/1.5.0.12'
                // "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.12) Gecko/20080129 Iceweasel/2.0.0.12 (Debian-2.0.0.12-0etch1)

                // Remove gecko string
                // Debian Firefox is remamed to  iceweasel.
                productAddition = productAddition.replace("Iceweasel", "firefox");


                productAddition = productAddition.substring(productAddition.indexOf(' ') + 1);
                productAddition = productAddition.toLowerCase();
                String prodDesc = null;
                if (productAddition.contains("firefox")) {
                    this.product = Product.FIREFOX;
                    prodDesc = "firefox";
                } else if (productAddition.contains("netscape")) {
                    this.product = Product.NETSCAPE;
                    prodDesc = "netscape";
                } else if (productAddition.contains("camino")) {
                    this.product = Product.CAMINO;
                    prodDesc = "camino";
                } else {
                    this.product = Product.MOZILLA;
                    // @todo: How it can get the mozilla vesion?
                }

                // Now, parse product version ...
                if (prodDesc != null) {
                    int sI = productAddition.indexOf(prodDesc) + prodDesc.length() + 1;
                    int eI = productAddition.indexOf(' ', sI);
                    if (eI == -1) {
                        eI = productAddition.length();
                    }

                    final String productVersion = productAddition.substring(sI, eI);
                    parseVersion(productVersion);

                }

            } else if (userAgentHeader.contains("Opera")) {
                // Opera:
                // Samples: Opera/9.0 (Windows NT 5.1; U; en)
                // Opera/8.5 (Macintosh; PPC Mac OS X; U; en)
                // Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.5
                // Mozilla/4.0 (compatible; MSIE 6.0; Mac_PowerPC Mac OS X; en) Opera 8.5
                // Opera/9.21 (Windows NT 5.1; U; en)
                this.product = Product.OPERA;
                String productVersion;
                if (application.startsWith("Opera")) {
                    productVersion = application.substring(application.indexOf('/') + 1, application.length());

                } else {
                    productVersion = productAddition.substring(application.lastIndexOf(' ') + 1, application.length());
                }
                parseVersion(productVersion);

            } else if (userAgentHeader.contains("4.7")) {
                this.product = Product.NETSCAPE;
            } else if (userAgentHeader.contains("Lynx")) {
                this.product = Product.LYNX;
            } else {
                // It's a robot ..
                for (String botAgent : botAgents) {
                    if (userAgentHeader.contains(botAgent)) {
                        // set a key in the session, so the next time we don't have to manually
                        // detect the robot again
                        this.product = Product.ROBOT;
                        break;
                    }
                }
                logger.info("UserAgent could not be detected: '" + userAgentHeader + "'");
            }
        } catch (Throwable e) {
            logger.error("Could not detect the browser based on the user agent: '" + userAgentHeader + "'");
            // Mark as an unsupported browser...
            this.product = Product.ROBOT;
        }
    }

    private OS parseOS(String details) {
        OS result;
        if (details.contains("Windows"))
            result = OS.WINDOWS;
        else if (details.contains("Mac") || details.contains("Macintosh"))
            result = OS.MAC;
        else if (details.contains("X11"))
            result = OS.LINUX;
        else
            result = OS.KNOWN;
        return result;
    }

    public static UserAgent create(final HttpServletRequest request) {
        final String userAgent = request.getHeader(USER_AGENT_HEADER);
        return new UserAgent(userAgent);
    }


    public static UserAgent create(final String userAgent) {
        return new UserAgent(userAgent);
    }

    private void parseVersion(final String version) {
        final int index = version.indexOf('.');
        final String vm = version.substring(0, index);
        final String vv = version.substring(index + 1, version.length());
        this.versionMajor = Integer.parseInt(vm);
        char c = vv.charAt(0);
        this.versionVariation = Integer.valueOf(String.valueOf(c));
    }

    /**
     * All known robot user-agent headers (list can be found
     * <a href="http://www.robotstxt.org/wc/activel">here</a>).
     * <p/>
     * <p>NOTE: To avoid bad detection:</p>
     * <p/>
     * <ul>
     * <li>Robots with ID of 2 letters only were removed</li>
     * <li>Robot called "webs" were removed</li>
     * <li>directhit was changed in direct_hit (its real id)</li>
     * </ul>
     */
    private static final String[] botAgents = {
            "acme.spider", "ahoythehomepagefinder", "alkaline", "appie", "arachnophilia",
            "architext", "aretha", "ariadne", "aspider", "atn.txt", "atomz", "auresys",
            "backrub", "bigbrother", "bjaaland", "blackwidow", "blindekuh", "bloodhound",
            "brightnet", "bspider", "cactvschemistryspider", "calif", "cassandra",
            "cgireader", "checkbot", "churl", "cmc", "collective", "combine", "conceptbot",
            "core", "cshkust", "cusco", "cyberspyder", "deweb", "dienstspider", "diibot",
            "direct_hit", "dnabot", "download_express", "dragonbot", "dwcp", "ebiness",
            "eit", "emacs", "emcspider", "esther", "evliyacelebi", "fdse", "felix",
            "ferret", "fetchrover", "fido", "finnish", "fireball", "fish", "fouineur",
            "francoroute", "freecrawl", "funnelweb", "gazz", "gcreep", "getbot", "geturl",
            "golem", "googlebot", "grapnel", "griffon", "gromit", "gulliver", "hambot",
            "harvest", "havindex", "hometown", "wired-digital", "htdig", "htmlgobble",
            "hyperdecontextualizer", "ibm", "iconoclast", "ilse", "imagelock", "incywincy",
            "informant", "infoseek", "infoseeksidewinder", "infospider", "inspectorwww",
            "intelliagent", "iron33", "israelisearch", "javabee", "jcrawler", "jeeves",
            "jobot", "joebot", "jubii", "jumpstation", "katipo", "kdd", "kilroy",
            "ko_yappo_robot", "labelgrabber.txt", "larbin", "legs", "linkscan",
            "linkwalker", "lockon", "logo_gif", "lycos", "macworm", "magpie", "mediafox",
            "merzscope", "meshexplorer", "mindcrawler", "moget", "momspider", "monster",
            "motor", "muscatferret", "mwdsearch", "myweb", "netcarta", "netmechanic",
            "netscoop", "newscan-online", "nhse", "nomad", "northstar", "nzexplorer",
            "occam", "octopus", "orb_search", "packrat", "pageboy", "parasite", "patric",
            "perignator", "perlcrawler", "phantom", "piltdownman", "pioneer", "pitkow",
            "pjspider", "pka", "plumtreewebaccessor", "poppi", "portalb", "puu", "python",
            "raven", "rbse", "resumerobot", "rhcs", "roadrunner", "robbie", "robi",
            "roverbot", "safetynetrobot", "scooter", "search_au", "searchprocess",
            "senrigan", "sgscout", "shaggy", "shaihulud", "sift", "simbot", "site-valet",
            "sitegrabber", "sitetech", "slurp", "smartspider", "snooper", "solbot",
            "spanner", "speedy", "spider_monkey", "spiderbot", "spiderman", "spry",
            "ssearcher", "suke", "sven", "tach_bw", "tarantula", "tarspider", "tcl",
            "techbot", "templeton", "titin", "titan", "tkwww", "tlspider", "ucsd",
            "udmsearch", "urlck", "valkyrie", "victoria", "visionsearch", "voyager",
            "vwbot", "w3index", "w3m2", "wanderer", "webbandit", "webcatcher", "webcopy",
            "webfetcher", "webfoot", "weblayers", "weblinker", "webmirror", "webmoose",
            "webquest", "webreader", "webreaper", "websnarf", "webspider", "webvac",
            "webwalk", "webwalker", "webwatch", "wget", "whowhere", "wmir", "wolp",
            "wombat", "worm", "wwwc", "wz101", "xget", "nederland.zoek"
    };

    public boolean isBrowserSupported() {
        // Is it a supported browser ?.
        final UserAgent.Product product = this.getProduct();
        boolean result = product == UserAgent.Product.FIREFOX && this.isVersionGreatedOrEqualThan(10, 0);
        result = result || product == UserAgent.Product.EXPLORER && this.isVersionGreatedOrEqualThan(7, 0) && this.getOs() == UserAgent.OS.WINDOWS;
        result = result || product == UserAgent.Product.OPERA && this.isVersionGreatedOrEqualThan(11, 0);
        result = result || product == UserAgent.Product.CHROME && this.isVersionGreatedOrEqualThan(19, 0);
        result = result || product == UserAgent.Product.SAFARI && this.isVersionGreatedOrEqualThan(5, 0);
        return result;
    }

    public boolean needsGCF() {
        final UserAgent.Product product = this.getProduct();
        return product == UserAgent.Product.EXPLORER && this.isVersionLessThan(9) && this.getOs() == UserAgent.OS.WINDOWS && !this.hasGCFInstalled;
    }
}
