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

web2d.peer.svg.ElementPeer = new Class({
    initialize:function (svgElement) {
        this._native = svgElement;
        if (!this._native.addEvent) {
            // Hack bug: https://bugzilla.mozilla.org/show_bug.cgi?id=740811
            for (var key in Element) {
                this._native[key] = Element.prototype[key];
            }
        }

        this._size = {width:1, height:1};
        this._changeListeners = {};
        // http://support.adobe.com/devsup/devsup.nsf/docs/50493.htm
    },

    setChildren:function (children) {
        this._children = children;
    },

    getChildren:function () {
        var result = this._children;
        if (!$defined(result)) {
            result = [];
            this._children = result;
        }
        return result;
    },

    getParent:function () {
        return this._parent;
    },

    setParent:function (parent) {
        this._parent = parent;
    },

    append:function (elementPeer) {
        // Store parent and child relationship.
        elementPeer.setParent(this);
        var children = this.getChildren();
        children.include(elementPeer);

        // Append element as a child.
        this._native.appendChild(elementPeer._native);

        // Broadcast events ...
        web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "strokeStyle");
    },


    removeChild:function (elementPeer) {
        // Store parent and child relationship.
        elementPeer.setParent(null);
        var children = this.getChildren();

        // Remove from children array ...
        var oldLength = children.length;

        children.erase(elementPeer);
        $assert(children.length < oldLength, "element could not be removed:" + elementPeer);

        // Append element as a child.
        this._native.removeChild(elementPeer._native);
    },

    /**
     * http://www.w3.org/TR/DOM-Level-3-Events/events.html
     * http://developer.mozilla.org/en/docs/addEvent
     */
    addEvent:function (type, listener) {
        $(this._native).bind(type, listener);
    },

    trigger:function (type, event) {
        $(this._native).trigger(type, event);
    },

    cloneEvents:function (from) {
        this._native.cloneEvents(from);
    },

    removeEvent:function (type, listener) {
        $(this._native).unbind(type, listener);
    },

    setSize:function (width, height) {
        if ($defined(width) && this._size.width != parseInt(width)) {
            this._size.width = parseInt(width);
            this._native.setAttribute('width', parseInt(width));
        }

        if ($defined(height) && this._size.height != parseInt(height)) {
            this._size.height = parseInt(height);
            this._native.setAttribute('height', parseInt(height));
        }

        web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "strokeStyle");
    },

    getSize:function () {
        return {width:this._size.width, height:this._size.height};
    },

    setFill:function (color, opacity) {
        if ($defined(color)) {
            this._native.setAttribute('fill', color);
        }
        if ($defined(opacity)) {
            this._native.setAttribute('fill-opacity', opacity);
        }
    },

    getFill:function () {
        var color = this._native.getAttribute('fill');
        var opacity = this._native.getAttribute('fill-opacity');
        return {color:color, opacity:Number(opacity)};
    },

    getStroke:function () {
        var vmlStroke = this._native;
        var color = vmlStroke.getAttribute('stroke');
        var dashstyle = this._stokeStyle;
        var opacity = vmlStroke.getAttribute('stroke-opacity');
        var width = vmlStroke.getAttribute('stroke-width');
        return {color:color, style:dashstyle, opacity:opacity, width:width};
    },

    setStroke:function (width, style, color, opacity) {
        if ($defined(width)) {
            this._native.setAttribute('stroke-width', width + "px");
        }
        if ($defined(color)) {
            this._native.setAttribute('stroke', color);
        }
        if ($defined(style)) {
            // Scale the dash array in order to be equal to VML. In VML, stroke style doesn't scale.
            var dashArrayPoints = this.__stokeStyleToStrokDasharray[style];
            var scale = 1 / web2d.peer.utils.TransformUtil.workoutScale(this).width;

            var strokeWidth = this._native.getAttribute('stroke-width');
            strokeWidth = parseFloat(strokeWidth);

            var scaledPoints = [];
            for (var i = 0; i < dashArrayPoints.length; i++) {
                // VML scale the stroke based on the stroke width.
                scaledPoints[i] = dashArrayPoints[i] * strokeWidth;

                // Scale the points based on the scale.
                scaledPoints[i] = (scaledPoints[i] * scale) + "px";
            }

            //        this._native.setAttribute('stroke-dasharray', scaledPoints);
            this._stokeStyle = style;
        }

        if ($defined(opacity)) {
            this._native.setAttribute('stroke-opacity', opacity);
        }
    },

    /*
     * style='visibility: visible'
     */
    setVisibility:function (isVisible) {
        this._native.setAttribute('visibility', (isVisible) ? 'visible' : 'hidden');
    },

    isVisible:function () {
        var visibility = this._native.getAttribute('visibility');
        return !(visibility == 'hidden');
    },

    updateStrokeStyle:function () {
        var strokeStyle = this._stokeStyle;
        if (this.getParent()) {
            if (strokeStyle && strokeStyle != 'solid') {
                this.setStroke(null, strokeStyle);
            }
        }
    },

    attachChangeEventListener:function (type, listener) {
        var listeners = this.getChangeEventListeners(type);
        if (!$defined(listener)) {
            throw "Listener can not be null";
        }
        listeners.push(listener);
    },

    getChangeEventListeners:function (type) {
        var listeners = this._changeListeners[type];
        if (!$defined(listeners)) {
            listeners = [];
            this._changeListeners[type] = listeners;
        }
        return listeners;
    },

    /**
     * Move element to the front
     */
    moveToFront:function () {
        this._native.parentNode.appendChild(this._native);
    },

    /**
     * Move element to the back
     */
    moveToBack:function () {
        this._native.parentNode.insertBefore(this._native, this._native.parentNode.firstChild);
    },

    setCursor:function (type) {
        this._native.style.cursor = type;
    }
});


web2d.peer.svg.ElementPeer.prototype.svgNamespace = 'http://www.w3.org/2000/svg';
web2d.peer.svg.ElementPeer.prototype.linkNamespace = 'http://www.w3.org/1999/xlink';
web2d.peer.svg.ElementPeer.prototype.__stokeStyleToStrokDasharray = {solid:[], dot:[1, 3], dash:[4, 3], longdash:[10, 2], dashdot:[5, 3, 1, 3]};

