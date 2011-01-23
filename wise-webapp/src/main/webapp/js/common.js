/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

/******************************************************************/
/*                        MOOdalBox 1.3.b4                        */
/* A modal box (inline popup), used to display remote content     */
/* loaded using AJAX, written for the mootools framework          */
/*         by Razvan Brates, razvan [at] e-magine.ro              */
/******************************************************************/
/*               http://www.e-magine.ro/moodalbox                 */
/******************************************************************/
/*                                                                */
/* MIT style license:                                             */
/* http://en.wikipedia.org/wiki/MIT_License                       */
/*                                                                */
/* mootools found at:                                             */
/* http://mootools.net/                                           */
/*                                                                */
/* Original code based on "Slimbox", by Christophe Beyls:         */
/* http://www.digitalia.be/software/slimbox                       */
/******************************************************************/

// Constants defined here can be changed for easy config / translation
// (defined as vars, because of MSIE's lack of support for const)

var _ERROR_MESSAGE = "Oops.. there was a problem with your request.<br /><br />" +
                     "Please try again.<br /><br />" +
                     "<em>Click anywhere to close.</em>";
// the error message displayed when the request has a problem
var _RESIZE_DURATION = 400;
// Duration of height and width resizing (ms)
var _INITIAL_WIDTH = 250;
// Initial width of the box (px)
var _INITIAL_HEIGHT = 250;
// Initial height of the box (px)
var _CONTENTS_WIDTH = 500;
// Actual width of the box (px)
var _CONTENTS_HEIGHT = 400;
// Actual height of the box (px)
var _DEF_CONTENTS_WIDTH = 500;
// Default width of the box (px) - used for resetting when a different setting was used
var _DEF_CONTENTS_HEIGHT = 400;
// Default height of the box (px) - used for resetting when a different setting was used
var _DEF_FULLSCREEN_WIDTH = 0.95;
// Default fullscreen width (%)
var _DEF_FULLSCREEN_HEIGHT = 0.8;
// Default fullscreen height (%)
var _ANIMATE_CAPTION = true;
// Enable/Disable caption animation
var _EVAL_SCRIPTS = true;
// Option to evaluate scripts in the response text
var _EVAL_RESPONSE = false;
// Option to evaluate the whole response text

// The MOOdalBox object in its beauty
var MOOdalBox = {

// init the MOOdalBox
    init: function (options) {

        // init default options
        this.options = Object.extend({
            resizeDuration:     _RESIZE_DURATION,
            initialWidth:         _INITIAL_WIDTH,
            initialHeight:         _INITIAL_HEIGHT,
            contentsWidth:         _CONTENTS_WIDTH,
            contentsHeight:     _CONTENTS_HEIGHT,
            defContentsWidth:     _DEF_CONTENTS_WIDTH,
            defContentsHeight:     _DEF_CONTENTS_HEIGHT,
            defFullscreenWidth:    _DEF_FULLSCREEN_WIDTH,
            defFullscreenHeight:_DEF_FULLSCREEN_HEIGHT,
            animateCaption:     _ANIMATE_CAPTION,
            evalScripts:         _EVAL_SCRIPTS,
            evalResponse:         _EVAL_RESPONSE
        }, options || {});

        // scan anchors for those opening a MOOdalBox
        this.anchors = [];
        this.scanAnchors(document.body, false);
        // scan forms for those opening a MOOdalBox
        this.forms = [];
        this.scanForms(document.body, false);
        // add event listeners
        this.eventKeyDown = this.keyboardListener.bindWithEvent(this);
        this.eventPosition = this.position.bind(this);

        // init the HTML elements
        // the overlay (clickable to close)
        this.overlay = new Element('div').setProperty('id', 'mb_overlay').injectInside(document.body);

        this.overlayDiv = new Element('div').setOpacity(0).setStyles({position: 'absolute', border: 0, width: '100%', backgroundColor: "black", zIndex: 98}).injectInside(document.body);
        // the center element
        this.center = new Element('div').setProperty('id', 'mb_center').setStyles({width: this.options.initialWidth + 'px', height: this.options.initialHeight + 'px', marginLeft: '-' + (this.options.initialWidth / 2) + 'px', display: 'none'}).injectInside(document.body);
        // the actual page contents
        this.contents = new Element('div').setProperty('id', 'mb_contents').injectInside(this.center);

        // the bottom part (caption / close)
        this.bottom = new Element('div').setProperty('id', 'mb_bottom').setStyle('display', 'none').injectInside(document.body);
        this.closelink = new Element('a').setProperties({id: 'mb_close_link', href: '#'}).injectInside(this.bottom);
        this.caption = new Element('div').setProperty('id', 'mb_caption').injectInside(this.bottom);
        new Element('div').setStyle('clear', 'both').injectInside(this.bottom);

        this.error = new Element('div').setProperty('id', 'mb_error').setHTML(_ERROR_MESSAGE);

        // attach the close event to the close button / the overlay
        this.closelink.onclick = this.overlay.onclick = this.close.bind(this);

        // init the effects
        var nextEffect = this.nextEffect.bind(this);
        this.fx = {
            overlay:     this.overlay.effect('opacity', { duration: 500 }).hide(),
            resize:     this.center.effects({ onComplete: nextEffect }),
            contents:     this.contents.effect('opacity', { duration: 500, onComplete: nextEffect }),
            bottom:     this.bottom.effects({ duration: 400, onComplete: nextEffect })
        };

        // AJAX related options
        var ajaxFailure = this.ajaxFailure.bind(this);
        this.ajaxOptions = {
            update:         this.contents,
            evalScripts:     this.options.evalScripts,
            evalResponse:     this.options.evalResponse,
            onComplete:     nextEffect,
            onFailure:         ajaxFailure,
            encoding: 'utf-8'
        };

        this.ajaxRequest = Class.empty;

    },

    click: function(link) {
        return this.open(link.href, link.title, link.rel, false);
    },

    open: function(sLinkHref, sLinkTitle, sLinkRel, oForm) {
        //Remove window key event listeners.
        this.eventContainer = new Element('div');
        this.eventContainer.cloneEvents($(document), 'keydown');
        $(document).removeEvents('keydown');

        this.href = sLinkHref;
        this.title = sLinkTitle;
        this.rel = sLinkRel;

        if (oForm) {
            if (oForm.method == 'get') {
                this.href += "?" + oForm.toQueryString();
                this.ajaxOptions = Object.extend(this.ajaxOptions, {method: 'get', postBody: ''});
            } else {
                this.ajaxOptions = Object.extend(this.ajaxOptions, {method: 'post', postBody: oForm});
            }
        } else {
            this.ajaxOptions = Object.extend(this.ajaxOptions, {method: 'get', postBody: ''});
        }
        this.ajaxOptions.encoding = 'utf-8';

        this.position();
        this.setup(true);
        this.top = Window.getScrollTop() + (Window.getHeight() / 15);
        this.center.setStyles({top: this.top + 'px', display: ''});
        this.fx.overlay.custom(0.8);
        return this.loadContents(sLinkHref);
    },

    position: function() {
        this.overlay.setStyles({top: Window.getScrollTop() + 'px', height: Window.getHeight() + 'px'});
        this.overlayDiv.setStyles({top: Window.getScrollTop() + 'px', height: Window.getHeight() + 'px'});
    },

    scanAnchors: function(oWhere, bForce) {

        // scan anchors for those opening a MOOdalBox
        $$($(oWhere).getElements('a')).each(function(el) {
            // we use a regexp to check for links that
            // have a rel attribute starting with "moodalbox"
            if (el.href && ((el.rel && el.rel.test('^moodalbox', 'i')) || (bForce && !el.onclick))) {
                if (bForce && !el.rel) {
                    // if we're forcing links to open in a moodalbox, we're keeping the current size
                    el.rel = "moodalbox " + this.options.contentsWidth + "px " + this.options.contentsHeight + "px";
                    if (this.wizardMode) el.rel += " wizard";
                }
                el.onclick = this.click.pass(el, this);
                this.anchors.push(el);
            }
        }, this);
    },

    scanForms: function (oWhere, bForce) {
        // scan forms for those opening a MOOdalBox
        $$($(oWhere).getElements('form')).each(function(el) {
            // we use a regexp to check for links that
            // have a rel attribute starting with "moodalbox"
            el.rel = el.getProperty('rel');
            if ((el.rel && el.rel.test('^moodalbox', 'i')) || bForce) {
                if (bForce && !el.rel) {
                    // if we're forcing links to open in a moodalbox, we're keeping the current size
                    el.rel = "moodalbox " + this.options.contentsWidth + "px " + this.options.contentsHeight + "px";
                    if (this.wizardMode) el.rel += " wizard";
                }
                el.onsubmit = this.open.pass([el.action, el.title, el.rel, el], this);
                this.forms.push(el);
            }
        }, this);
    },

    setup: function(open) {
        //var elements = $A($$('object'));
        //elements.extend($$(window.ActiveXObject ? 'select' : 'embed'));
        //elements.each(function(el){ el.style.visibility = open ? 'hidden' : ''; });

        // use an iframe to show under everything

        var fn = open ? 'addEvent' : 'removeEvent';
        window[fn]('scroll', this.eventPosition)[fn]('resize', this.eventPosition);
        document[fn]('keydown', this.eventKeyDown);
        this.step = 0;
    },

    loadContents: function() {

        if (this.step) return false;
        this.step = 1;

        // check to see if there are specified dimensions
        // if not, fall back to default values

        // fullscreen switch concept originally by Lennart Pilon (http://ljpilon.nl/)

        // we check for a "fullscreen" switch
        if (this.rel.test("fullscreen")) {

            this.options.contentsWidth = this.options.defFullscreenWidth * window.getWidth();
            this.options.contentsHeight = this.options.defFullscreenHeight * window.getHeight();

        } else { // we check for other specified dimensions (px or %)

            var aDim = this.rel.match(/[0-9.]+(px|%)/g);

            if (aDim && aDim[0]) { //first dimension is interpreted as width

                var w = aDim[0].toInt();

                if (aDim[0].test("%")) {
                    this.options.contentsWidth = (w > 0) ? 0.01 * w * window.getWidth() : this.options.defFullscreenWidth * window.getWidth();
                } else {
                    this.options.contentsWidth = (w > 0) ? w : this.options.defContentsWidth;
                }
            } else { // we switch to defaults if there aren't any dimensions specified
                this.options.contentsWidth = this.options.defContentsWidth;
                this.options.contentsHeight = this.options.defContentsHeight;
            }

            if (aDim && aDim[1]) { // we have a second dimension specified, which we'll interpret as height

                var h = aDim[1].toInt();

                if (aDim[1].test("%")) {
                    this.options.contentsHeight = (h > 0) ? 0.01 * h * window.getHeight() : this.options.defFullscreenHeight * window.getHeight();
                } else {
                    this.options.contentsHeight = (h > 0) ? h : this.options.defContentsHeight;
                }
            } else if (aDim && aDim[0]) {
                // we have the first dimension specified, but not the second
                // so we interpret as width = height = the given value
                if (aDim[0].test("%")) {
                    this.options.contentsHeight = (w > 0) ? 0.01 * w * window.getHeight() : this.options.defFullscreenHeight * window.getHeight();
                } else {
                    this.options.contentsHeight = (w > 0) ? w : this.options.defContentsHeight;
                }
            }
            // correct a little approximation bug (size flickers)
            this.options.contentsWidth = Math.floor(this.options.contentsWidth);
            this.options.contentsHeight = Math.floor(this.options.contentsHeight);

        }

        this.wizardMode = this.rel.test("wizard");

        // this is where we'll check for other options passed via the rel attribute

        this.bottom.setStyles({opacity: '0', height: '0px', display: 'none'});
        this.center.className = 'mb_loading';

        this.fx.contents.hide();

        // AJAX call here
        this.ajaxRequest = new Ajax(this.href, this.ajaxOptions).request();

        // make the local timeout call here

        return false;
    },

    ajaxFailure: function () {
        this.contents.setHTML('');
        this.error.clone().injectInside(this.contents);
        this.nextEffect();
        this.center.setStyle('cursor', 'pointer');
        this.bottom.setStyle('cursor', 'pointer');
        this.center.onclick = this.bottom.onclick = this.close.bind(this);
    },


// make the local timeout function here


    nextEffect: function() {
        switch (this.step++) {
            case 1:
            // remove previous styling from the elements
            // (e.g. styling applied in case of an error)
                this.center.className = '';
                this.center.setStyle('cursor', 'default');
                this.bottom.setStyle('cursor', 'default');
                this.center.onclick = this.bottom.onclick = '';
                this.caption.setHTML(this.title);

                this.contents.setStyles({width: this.options.contentsWidth + "px", height: this.options.contentsHeight + "px"});

                if (this.center.clientHeight != this.contents.offsetHeight) {
                    this.fx.resize.options.duration = this.options.resizeDuration;
                    this.fx.resize.custom({height: [this.center.clientHeight, this.contents.offsetHeight]});
                    break;
                }
                this.step++;



            case 2:
                if (this.center.clientWidth != this.contents.offsetWidth) {
                    this.fx.resize.custom({width: [this.center.clientWidth, this.contents.offsetWidth], marginLeft: [-this.center.clientWidth / 2, -this.contents.offsetWidth / 2]});
                    break;
                }
                this.step++;

            case 3:
                this.bottom.setStyles({top: (this.top + this.center.clientHeight) + 'px', width: this.contents.style.width, marginLeft: this.center.style.marginLeft, display: ''});

            // check to see if in wizard mode and parse links
                if (this.wizardMode) this.scanAnchors(this.contents, true);
                if (this.wizardMode) this.scanForms(this.contents, true);

                this.fx.contents.custom(0, 1);
                break;

            case 4:
                if (this.options.animateCaption) {
                    this.fx.bottom.custom({opacity: [0, 1], height: [0, this.bottom.scrollHeight]});
                    break;
                }
                this.bottom.setStyles({opacity: '1', height: this.bottom.scrollHeight + 'px'});

            case 5:
                this.step = 0;
        }
    },


    keyboardListener: function(event) {
        // close the MOOdalBox when the user presses CTRL + W, CTRL + X, ESC
        if ((event.control && event.key == 'w') || (event.control && event.key == 'x') || (event.key == 'esc')) {
            this.close();
            event.stop();
        }
    },

    close: function() {
        //restore key event listeners
        $(document).cloneEvents(this.eventContainer, 'keydown');
        this.eventContainer.removeEvents('keydown');
        if (this.step < 0) return;
        this.step = -1;
        for (var f in this.fx) this.fx[f].clearTimer();
        this.center.style.display = this.bottom.style.display = 'none';
        this.center.className = 'mb_loading';
        this.fx.overlay.chain(this.setup.pass(false, this)).custom(0);
        this.overlayDiv.style.display = "none";
        return false;
    },

    config: function(options) {
        this.options = Object.extend(this.options, options || {});
        return false;
        // to be used on links
    }

};

// Moodal Dialog startup
Window.onDomReady(MOOdalBox.init.bind(MOOdalBox));


function displayLoading()
{
    $('headerLoading').style.visibility = 'visible';
}