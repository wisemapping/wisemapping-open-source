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

mindplot.widget.FloatingTip = new Class({
    Implements: [Options, Events],

    options: {
        position: 'top',
        center: true,
        content: 'title',
        html: false,
        balloon: true,
        arrowSize: 6,
        arrowOffset: 6,
        distance: 7,
        motion: 40,
        motionOnShow: true,
        motionOnHide: true,
        showOn: 'mouseenter',
        hideOn: 'mouseleave',
        showDelay: 500,
        hideDelay: 250,
        className: 'floating-tip',
        offset: {x: 0, y: 0},
        preventHideOnOver: true,
        fx: { 'duration': 'short' }
    },

    initialize: function(element, options) {
        this.setOptions(options);
        this.boundShow = function(event) {
            this.show(event, element);
        }.bind(this);

        this.boundHide = function(event) {
            this.hide(event, element);
        }.bind(this);

        if (!['top', 'right', 'bottom', 'left', 'inside'].contains(this.options.position))
            this.options.position = 'top';
        this.attach(element);
    },

    attach: function(element) {
        if (element.retrieve('hasEvents') !== null) {
            return;
        }
        element.addEvent(this.options.showOn, this.boundShow);
        element.addEvent(this.options.hideOn, this.boundHide);
        element.store('hasEvents', true);
    },

    show: function(event, element) {
        var old = element.retrieve('floatingtip');
        if (old) if (old.getStyle('opacity') == 1) {
            clearTimeout(old.retrieve('timeout'));
            return this;
        }
        var tip = this._create(element);
        if (tip == null)
            return this;
        element.store('floatingtip', tip);
        this._animate(tip, 'in');
        if (this.options.preventHideOnOver) {
            tip.addEvent(this.options.showOn, this.boundShow);
            tip.addEvent(this.options.hideOn, this.boundHide);
        }
        this.fireEvent('show', [tip, element]);
        return this;
    },

    hide: function(event, element) {
        var tip = element.retrieve('floatingtip');
        if (!tip) {
            if (this.options.position == 'inside') {
                try {
                    element = element.getParent().getParent();
                    tip = element.retrieve('floatingtip');
                } catch (x) {
                }
                if (!tip) return this;
            } else {
                return this;
            }
        }
        this._animate(tip, 'out');
        this.fireEvent('hide', [tip, element]);
        return this;
    },

    _create: function(elem) {

        var o = this.options;
        var oc = o.content;
        var opos = o.position;

        if (oc == 'title') {
            oc = 'floatingtitle';
            if (!elem.get('floatingtitle')) elem.setProperty('floatingtitle', elem.get('title'));
            elem.set('title', '');
        }

        var cnt = (typeof(oc) == 'string' ? elem.get(oc) : oc(elem));
        var cwr = new Element('div').addClass(o.className).setStyle('margin', 0);
        var tip = new Element('div').addClass(o.className + '-wrapper').setStyles({ 'margin': 0, 'padding': 0, 'z-index': cwr.getStyle('z-index') }).adopt(cwr);

        if (cnt) {
            if (o.html)
                cnt.inject(cwr);
            else
                cwr.set('text', cnt);
        } else {
            return null;
        }

        var body = document.id(document.body);
        tip.setStyles({ 'position': 'absolute', 'opacity': 0, 'top': 0, 'left': 0 }).inject(body);

        if (o.balloon && !Browser.ie6) {

            var trg = new Element('div').addClass(o.className + '-triangle').setStyles({ 'margin': 0, 'padding': 0 });
            var trgSt = { 'border-color': cwr.getStyle('background-color'), 'border-width': o.arrowSize, 'border-style': 'solid','width': 0, 'height': 0 };

            switch (opos) {
                case 'inside':
                case 'top':
                    trgSt['border-bottom-width'] = 0;
                    break;
                case 'right':
                    trgSt['border-left-width'] = 0;
                    trgSt['float'] = 'left';
                    cwr.setStyle('margin-left', o.arrowSize);
                    break;
                case 'bottom':
                    trgSt['border-top-width'] = 0;
                    break;
                case 'left':
                    trgSt['border-right-width'] = 0;
                    if (Browser.ie7) {
                        trgSt['position'] = 'absolute';
                        trgSt['right'] = 0;
                    } else {
                        trgSt['float'] = 'right';
                    }
                    cwr.setStyle('margin-right', o.arrowSize);
                    break;
            }

            switch (opos) {
                case 'inside':
                case 'top':
                case 'bottom':
                    trgSt['border-left-color'] = trgSt['border-right-color'] = 'transparent';
                    trgSt['margin-left'] = o.center ? tip.getSize().x / 2 - o.arrowSize : o.arrowOffset;
                    break;
                case 'left':
                case 'right':
                    trgSt['border-top-color'] = trgSt['border-bottom-color'] = 'transparent';
                    trgSt['margin-top'] = o.center ? tip.getSize().y / 2 - o.arrowSize : o.arrowOffset;
                    break;
            }

            trg.setStyles(trgSt).inject(tip, (opos == 'top' || opos == 'inside') ? 'bottom' : 'top');

        }

        var tipSz = tip.getSize(), trgC = elem.getCoordinates(body);
        var pos = { x: trgC.left + o.offset.x, y: trgC.top + o.offset.y };

        if (opos == 'inside') {
            tip.setStyles({ 'width': tip.getStyle('width'), 'height': tip.getStyle('height') });
            elem.setStyle('position', 'relative').adopt(tip);
            pos = { x: o.offset.x, y: o.offset.y };
        } else {
            switch (opos) {
                case 'top':
                    pos.y -= tipSz.y + o.distance;
                    break;
                case 'right':
                    pos.x += trgC.width + o.distance;
                    break;
                case 'bottom':
                    pos.y += trgC.height + o.distance;
                    break;
                case 'left':
                    pos.x -= tipSz.x + o.distance;
                    break;
            }
        }

        if (o.center) {
            switch (opos) {
                case 'top':
                case 'bottom':
                    pos.x += (trgC.width / 2 - tipSz.x / 2);
                    break;
                case 'left':
                case 'right':
                    pos.y += (trgC.height / 2 - tipSz.y / 2);
                    break;
                case 'inside':
                    pos.x += (trgC.width / 2 - tipSz.x / 2);
                    pos.y += (trgC.height / 2 - tipSz.y / 2);
                    break;
            }
        }

        tip.set('morph', o.fx).store('position', pos);
        tip.setStyles({ 'top': pos.y, 'left': pos.x });

        return tip;

    },

    _animate: function(tip, d) {

        clearTimeout(tip.retrieve('timeout'));
        tip.store('timeout', (function(t) {

            var o = this.options, din = (d == 'in');
            var m = { 'opacity': din ? 1 : 0 };

            if ((o.motionOnShow && din) || (o.motionOnHide && !din)) {
                var pos = t.retrieve('position');
                if (!pos) return;
                switch (o.position) {
                    case 'inside':
                    case 'top':
                        m['top'] = din ? [pos.y - o.motion, pos.y] : pos.y - o.motion;
                        break;
                    case 'right':
                        m['left'] = din ? [pos.x + o.motion, pos.x] : pos.x + o.motion;
                        break;
                    case 'bottom':
                        m['top'] = din ? [pos.y + o.motion, pos.y] : pos.y + o.motion;
                        break;
                    case 'left':
                        m['left'] = din ? [pos.x - o.motion, pos.x] : pos.x - o.motion;
                        break;
                }
            }

            t.morph(m);
            if (!din) t.get('morph').chain(function() {
                this.dispose();
            }.bind(t));

        }).delay((d == 'in') ? this.options.showDelay : this.options.hideDelay, this, tip));

        return this;

    }

});
