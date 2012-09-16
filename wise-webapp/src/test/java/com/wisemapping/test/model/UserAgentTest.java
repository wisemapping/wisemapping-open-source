package com.wisemapping.test.model;

import com.wisemapping.filter.WiseUserAgent;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class UserAgentTest {

    @Test
    public void validations() {

        final WiseUserAgent firefox15 = WiseUserAgent.create("Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15");
        Assert.assertEquals(firefox15.isBrowserSupported(), true);

        final WiseUserAgent firefox9 = WiseUserAgent.create("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:9.0a2) Gecko/20111101 Firefox/9.0a2");
        Assert.assertEquals(firefox9.isBrowserSupported(), false);

        final WiseUserAgent chrome18 = WiseUserAgent.create("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/18.6.872.0 Safari/535.2 UNTRUSTED/1.0 3gpp-gba UNTRUSTED/1.0");
        Assert.assertEquals(chrome18.isBrowserSupported(), true);

        final WiseUserAgent chrome21 = WiseUserAgent.create("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1");
        Assert.assertEquals(chrome21.isBrowserSupported(), true);

        final WiseUserAgent ie10 = WiseUserAgent.create("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)");
        Assert.assertEquals(ie10.isBrowserSupported(), true);

        final WiseUserAgent ie10_6 = WiseUserAgent.create("Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0");
        Assert.assertEquals(ie10_6.isBrowserSupported(), true);

        final WiseUserAgent ie9 = WiseUserAgent.create("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2; .NET CLR 1.1.4322; .NET4.0C; Tablet PC 2.0)");
        Assert.assertEquals(ie9.isBrowserSupported(), true);

        final WiseUserAgent safari = WiseUserAgent.create("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; da-dk) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1");
        Assert.assertEquals(safari.isBrowserSupported(), true);

        final WiseUserAgent safari6 = WiseUserAgent.create("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25");
        Assert.assertEquals(safari6.isBrowserSupported(), true);

        final WiseUserAgent safariIpad = WiseUserAgent.create("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25");
        Assert.assertEquals(safariIpad.isBrowserSupported(), true);

        final WiseUserAgent googlebot = WiseUserAgent.create("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        Assert.assertEquals(googlebot.isBrowserSupported(), true);

        final WiseUserAgent mediapartners = WiseUserAgent.create("Mediapartners-Google/2.1");
        Assert.assertEquals(mediapartners.isBrowserSupported(), true);

    }

}

