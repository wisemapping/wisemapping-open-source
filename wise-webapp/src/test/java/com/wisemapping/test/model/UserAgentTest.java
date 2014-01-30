package com.wisemapping.test.model;

import com.wisemapping.filter.SupportedUserAgent;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class UserAgentTest {


    public void isBrowserSupported() {

        final SupportedUserAgent firefox15 = SupportedUserAgent.create("Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15");
        Assert.assertEquals(firefox15.isBrowserSupported(), true);

        final SupportedUserAgent firefox9 = SupportedUserAgent.create("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:9.0a2) Gecko/20111101 Firefox/9.0a2");
        Assert.assertEquals(firefox9.isBrowserSupported(), false);

        final SupportedUserAgent chrome18 = SupportedUserAgent.create("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/18.6.872.0 Safari/535.2 UNTRUSTED/1.0 3gpp-gba UNTRUSTED/1.0");
        Assert.assertEquals(chrome18.isBrowserSupported(), true);

        final SupportedUserAgent chrome21 = SupportedUserAgent.create("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1");
        Assert.assertEquals(chrome21.isBrowserSupported(), true);

        final SupportedUserAgent ie10 = SupportedUserAgent.create("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)");
        Assert.assertEquals(ie10.isBrowserSupported(), true);

        final SupportedUserAgent ie10_6 = SupportedUserAgent.create("Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0");
        Assert.assertEquals(ie10_6.isBrowserSupported(), true);

        final SupportedUserAgent ie9 = SupportedUserAgent.create("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2; .NET CLR 1.1.4322; .NET4.0C; Tablet PC 2.0)");
        Assert.assertEquals(ie9.isBrowserSupported(), true);

        final SupportedUserAgent ie8 = SupportedUserAgent.create("Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322)");
        Assert.assertEquals(ie8.isBrowserSupported(), true);

        final SupportedUserAgent safari = SupportedUserAgent.create("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; da-dk) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1");
        Assert.assertEquals(safari.isBrowserSupported(), true);

        final SupportedUserAgent safari6 = SupportedUserAgent.create("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25");
        Assert.assertEquals(safari6.isBrowserSupported(), true);

        final SupportedUserAgent safariIpad = SupportedUserAgent.create("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25");
        Assert.assertEquals(safariIpad.isBrowserSupported(), true);

        final SupportedUserAgent googlebot = SupportedUserAgent.create("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        Assert.assertEquals(googlebot.isBrowserSupported(), true);

        final SupportedUserAgent mediapartners = SupportedUserAgent.create("Mediapartners-Google/2.1");
        Assert.assertEquals(mediapartners.isBrowserSupported(), true);

        final SupportedUserAgent ie11 = SupportedUserAgent.create("Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko");
        Assert.assertEquals(ie11.isBrowserSupported(), true);

        final SupportedUserAgent firefox20 = SupportedUserAgent.create("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:20.0) Gecko/20121215 Firefox/20.0 AppEngine-Google; (+http://code.google.com/appengine; appid: slubuntuk)");
        Assert.assertEquals(firefox20.isBrowserSupported(), true);
    }


    public void isGCFRequired() {

        final SupportedUserAgent ie10 = SupportedUserAgent.create("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)");
        Assert.assertEquals(ie10.needsGCF(), false);

        final SupportedUserAgent ie8 = SupportedUserAgent.create("Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322)");
        Assert.assertEquals(ie8.needsGCF(), true);

        final SupportedUserAgent ie8WithGCF = SupportedUserAgent.create("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; SV1) chromeframe/11.0.660.0");
        Assert.assertEquals(ie8WithGCF.needsGCF(), false);

    }

}

