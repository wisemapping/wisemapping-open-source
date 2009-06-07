/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

/*
Created By: Chris Campbell
Website: http://particletree.com
Date: 2/1/2006

Inspired by the lightbox implementation found at http://www.huddletogether.com/projects/lightbox/
*/

/*-------------------------------GLOBAL VARIABLES------------------------------------*/

var detect = navigator.userAgent.toLowerCase();
var OS,browser,version,total,thestring;

/*-----------------------------------------------------------------------------------------------*/

//Browser detect script origionally created by Peter Paul Koch at http://www.quirksmode.org/

function getBrowserInfo(evt) {
    if (checkIt('konqueror')) {
        browser = "Konqueror";
        OS = "Linux";
    }
    else if (checkIt('safari')) browser = "Safari"
    else if (checkIt('omniweb')) browser = "OmniWeb"
    else if (checkIt('opera')) browser = "Opera"
    else if (checkIt('webtv')) browser = "WebTV";
    else if (checkIt('icab')) browser = "iCab"
    else if (checkIt('msie')) browser = "Internet Explorer"
    else if (!checkIt('compatible')) {
        browser = "Netscape Navigator"
        version = detect.charAt(8);
    }
    else browser = "An unknown browser";

    if (!version) version = detect.charAt(place + thestring.length);

    if (!OS) {
        if (checkIt('linux')) OS = "Linux";
        else if (checkIt('x11')) OS = "Unix";
        else if (checkIt('mac')) OS = "Mac"
        else if (checkIt('win')) OS = "Windows"
        else OS = "an unknown operating system";
    }
}

function checkIt(string) {
    place = detect.indexOf(string) + 1;
    thestring = string;
    return place;
}

/*-----------------------------------------------------------------------------------------------*/

$(window).addEvent('load', getBrowserInfo);

core.WaitDialog = new Class({

    initialize: function(contentId) {
        this.content = $(contentId);
    },

    yPos : 0,

    xPos : 0,

// Turn everything on - mainly the IE fixes
    activate: function(changeCursor)
    {
        //		if (browser == 'Internet Explorer'){
        ////			this.getScroll();
        //			this.prepareIE('100%', 'hidden');
        ////			this.setScroll(0,0);
        ////			this.hideSelects('hidden');
        //		}
        this.displayLightbox("block");

        // Change to loading cursor.
        if (changeCursor)
        {
            window.document.body.style.cursor = "wait";
        }
    },

// Ie requires height to 100% and overflow hidden or else you can scroll down past the lightbox
    prepareIE: function(height, overflow) {
        bod = document.getElementsByTagName('body')[0];
        bod.style.height = height;
        bod.style.overflow = overflow;

        htm = document.getElementsByTagName('html')[0];
        htm.style.height = height;
        htm.style.overflow = overflow;
    },

// In IE, select elements hover on top of the lightbox
    hideSelects: function(visibility) {
        selects = document.getElementsByTagName('select');
        for (i = 0; i < selects.length; i++) {
            selects[i].style.visibility = visibility;
        }
    },

// Taken from lightbox implementation found at http://www.huddletogether.com/projects/lightbox/
    getScroll: function() {
        if (self.pageYOffset) {
            this.yPos = self.pageYOffset;
        } else if (document.documentElement && document.documentElement.scrollTop) {
            this.yPos = document.documentElement.scrollTop;
        } else if (document.body) {
            this.yPos = document.body.scrollTop;
        }
    },

    setScroll: function(x, y) {
        window.scrollTo(x, y);
    },

    displayLightbox: function(display) {
        $('overlay').style.display = display;
        $('lightbox').style.display = display;
        if (display != 'none')
            this.processInfo();
    },

// Display Ajax response
    processInfo: function() {
        var info = new Element('div').setProperty('id', 'lbContent');
        info.setHTML(this.content.innerHTML);
        info.injectBefore($('lbLoadMessage'));
        $('lightbox').className = "done";
    },

// Search through new links within the lightbox, and attach click event
    actions: function() {
        lbActions = document.getElementsByClassName('lbAction');

        for (i = 0; i < lbActions.length; i++) {
            $(lbActions[i]).addEvent('click', function() {
                this[lbActions[i].rel].pass(this)
            }.bind(this));
            lbActions[i].onclick = function() {
                return false;
            };
        }

    },

// Example of creating your own functionality once lightbox is initiated
    insert: function(e) {
        var event = new Event(e);
        link = event.target;
        if ($('lbContent'))
            $('lbContent').remove();

        var myAjax = new Ajax.Request(
                link.href,
        {method: 'post', parameters: "", onComplete: this.processInfo.pass(this)}
                );

    },

// Example of creating your own functionality once lightbox is initiated
    deactivate: function(time) {

        if ($('lbContent'))
            $('lbContent').remove();
        //
        //		if (browser == "Internet Explorer"){
        //			this.setScroll(0,this.yPos);
        //			this.prepareIE("auto", "auto");
        //			this.hideSelects("visible");
        //		}
        this.displayLightbox("none");

        window.document.body.style.cursor = "default";
    }
});

/*-----------------------------------------------------------------------------------------------*/

// Onload, make all links that need to trigger a lightbox active
function initialize() {
    addLightboxMarkup();
    valid = new core.WaitDialog($('sampleDialog'));
}

// Add in markup necessary to make this work. Basically two divs:
// Overlay holds the shadow
// Lightbox is the centered square that the content is put into.
function addLightboxMarkup() {
    var body = document.getElementsByTagName('body')[0];
    overlay = new Element('div').setProperty('id', 'overlay').injectInside(document.body);
    var lb = new Element('div').setProperty('id', 'lightbox');
    lb.addClass('loading');
    var tmp = new Element('div').setProperty('id', 'lbLoadMessage').injectInside(lb);
    lb.injectInside(document.body);
}