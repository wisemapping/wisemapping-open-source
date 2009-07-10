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
 /*-----------------------------------------------------------------------------------------------*/


core.WaitDialog = new Class({


    yPos : 0,
    xPos : 0,

    initialize: function() {
    },
    // Turn everything on - mainly the IE fixes
    activate: function(changeCursor, dialogContent)
    {

        this.content = dialogContent;

        this._initLightboxMarkup();

        this.displayLightbox("block");

        // Change to loading cursor.
        if (changeCursor)
        {
            window.document.body.style.cursor = "wait";
        }
    },
    changeContent: function(dialogContent, changeCursor)
    {
        this.content = dialogContent;
        if (!$('lbContent'))
        {
            // Dialog is not activated. Nothing to do ...
            window.document.body.style.cursor = "pointer";
            return;
        }

        this.processInfo();

        // Change to loading cursor.
        if (changeCursor)
        {
            window.document.body.style.cursor = "wait";
        }else
        {
            window.document.body.style.cursor = "auto";
        }
    },
    displayLightbox: function(display) {
        if (display != 'none')
            this.processInfo();
        $('overlay').style.display = display;
        $('lightbox').style.display = display;

    },

    // Display dialog content ...
    processInfo: function() {
        if ($('lbContent'))
            $('lbContent').remove();

        var lbContentElement = new Element('div').setProperty('id', 'lbContent');
        lbContentElement.setHTML(this.content.innerHTML);

        lbContentElement.injectBefore($('lbLoadMessage'));
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
    deactivate: function(time) {

        if ($('lbContent'))
            $('lbContent').remove();

        this.displayLightbox("none");

        window.document.body.style.cursor = "default";
    }
    , _initLightboxMarkup:function()
    {
        // Add overlay element inside body ...
        var bodyElem = document.getElementsByTagName('body')[0];
        var overlayElem = new Element('div').setProperty('id', 'overlay');
        overlayElem.injectInside(bodyElem);

        // Add lightbox element inside body ...
        var lightboxElem = new Element('div').setProperty('id', 'lightbox');
        lightboxElem.addClass('loading');

        var lbLoadMessageElem = new Element('div').setProperty('id', 'lbLoadMessage');
        lbLoadMessageElem.injectInside(lightboxElem);

        lightboxElem.injectInside(bodyElem);

    }
});