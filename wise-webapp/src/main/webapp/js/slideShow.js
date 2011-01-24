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

/*
var _START_TRANSITION_TIME = 2000;
var _WAITING_PERIOD = 2800;
var _TRANSITION_TIME = 800;

slideShow = {
    init: function (options) {
        // init default options
        this.options = Object.extend({
            startTransitionTime: _START_TRANSITION_TIME,
            waitingPeriod: _WAITING_PERIOD,
            transitionTime: _TRANSITION_TIME
        }, options || {});
        this.scanScreen();
        this.img = new Element('img').injectInside(this.screen);

        var nextEffect = this.nextEffect.bind(this);

        this.imgEffect = this.img.effect('opacity', { duration: this.options.startTransitionTime, onComplete: nextEffect }).hide();
        this.steps = 0;
        this.nextEffect();

    },
    scanScreen: function () {
        var el = $E('img[rel]', $(window.body));
        el.rel = el.getProperty('rel');
        el.srcs = el.getProperty('srcs');
        if (el.rel.test('^slideShow', 'i'))
        {
            var parent = el.getParent();
            el.remove();
            var aDim = el.rel.match(/[0-9.]+(px|%)/g);
            var width = "500px";
            var height = "200px";
            if (aDim && aDim[0])
            {
                var w = aDim[0].toInt();
                if (w > 0)
                    width = w;
            }
            if (aDim && aDim[1])
            {
                var h = aDim[1].toInt();
                if (h > 0)
                    height = h;
            }
            this.screen = new Element('div').setProperties({width:width, height:height}).injectInside(parent);
            if (el.srcs)
                this.screen.srcs = el.srcs;
        }
    },
    nextEffect: function () {
        switch (this.steps++)
                {
            case 0:
                if (this.showing)
                    this.imgEffect.options.duration = this.options.transitionTime;
                var srcs = this.screen.srcs.split(',');
                if (!$defined(this.index))
                {
                    this.index = 0;
                }
                else
                {
                    this.index++;
                    if (this.index >= srcs.length)
                        this.index = 0;
                }
                this.img.src = srcs[this.index];
                this.showing = true;
                this.imgEffect.start(0, 1);
                break;
            case 1:
                this.nextEffect.delay(this.options.waitingPeriod, this);
                break;
            case 2:
                this.steps = 0;
                this.imgEffect.start(1, 0);
                break;
        }
    }
};

window.onDomReady(slideShow.init.bind(slideShow));
*/
var SlideShow = new Class({
    options: {
        id: 'SlideShow',
        startTransitionTime: 2000,
        waitingPeriod: 6000,
        transitionTime: 800
    },

    initialize: function(el, options) {
        // init default options
        this.setOptions(options);
        this.container = el;
        this.buildScreen();

        var nextEffect = this.nextEffect.bind(this);

        this.imgEffect = this.container.effect('opacity', { duration: this.options.startTransitionTime, onComplete: nextEffect }).hide();
        this.steps = 0;
        this.nextEffect();

    },
    buildScreen: function () {
        this.container.rel = this.container.getProperty('rel');
        this.container.srcs = this.container.getProperty('srcs');
        if (this.container.rel.test('^slideShow', 'i'))
        {
            var aDim = this.container.rel.match(/[0-9.]+(px|%)/g);
            var width = "500px";
            var height = "200px";
            if (aDim && aDim[0])
            {
                var w = aDim[0].toInt();
                if (w > 0)
                    width = w;
            }
            if (aDim && aDim[1])
            {
                var h = aDim[1].toInt();
                if (h > 0)
                    height = h;
            }
            this.container.setProperties({width:width, height:height});
            if (this.container.srcs)
                this.srcs = this.container.srcs.split(',');
        }
    },
    nextEffect: function () {
        switch (this.steps++)
                {
            case 0:
                if (this.showing)
                    this.imgEffect.options.duration = this.options.transitionTime;
                if (!$defined(this.index))
                {
                    this.index = 0;
                }
                else
                {
                    this.index++;
                    if (this.index >= this.srcs.length)
                        this.index = 0;
                    var firstElem = this.container.getFirst();
                    firstElem.remove();
                    firstElem.setStyle('display', 'none');
                    firstElem.injectInside(this.slidesContainer);
                }

                var nextElem = $(this.srcs[this.index]);
                if (!$defined(this.slidesContainer))
                    this.slidesContainer = nextElem.getParent();
                nextElem.injectInside(this.container);
                nextElem.setStyle('display', 'block');
                this.showing = true;
                this.imgEffect.start(0, 1);
                break;
            case 1:
                this.nextEffect.delay(this.options.waitingPeriod, this);
                break;
            case 2:
                this.steps = 0;
                this.imgEffect.start(1, 0);
                break;
        }
    }
});
SlideShow.implement(new Options);