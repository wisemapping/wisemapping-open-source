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

web2d.peer.vml.ElementPeer = function(vmlElement)
{
    this._native = vmlElement;
    this._native.style.position = 'absolute';
    this._position = {x:0,y:0};
    this._strokeWidth = -1;
    this._changeListeners = {};
};

/**
 *  http://www.quirksmode.org/js/events_advanced.html
 *
 * Drawbacks
 *
 * When compared to the W3C model, the Microsoft model has two important drawbacks:

 *    1. Events always bubble, no capturing possibility.
 *    2. The event handling function is referenced, not copied, so the this keyword always refers to the window and is completely useless.
 *
 * The result of these two weaknesses is that when an event bubbles up it is impossible to know which HTML element currently handles the event. I explain this problem more fully on the Event order page.
 * Since the Microsoft event adding model is only supported by Explorer 5 and higher on Windows, it cannot be used for cross�browser scripts. But even for Explorer�on�Windows only applications it�s best not to use it, since the bubbling problem can be quite nasty in complex applications.
 */
web2d.peer.vml.ElementPeer.prototype.addEventListener = function(type, listener)
{
    var element = this.getElementToAttachEvent();
    element.attachEvent("on" + type, listener);
};

web2d.peer.vml.ElementPeer.prototype.getElementToAttachEvent = function()
{
    return this._native;
};

web2d.peer.vml.ElementPeer.prototype.removeEventListener = function(type, listener, useCapture)
{
    var element = this.getElementToAttachEvent();
    element.detachEvent("on" + type, listener);
};

web2d.peer.vml.ElementPeer.prototype.getChangeEventListeners = function(type)
{
    var listeners = this._changeListeners[type];
    if (!listeners)
    {
        listeners = [];
        this._changeListeners[type] = listeners;
    }
    return listeners;
};

web2d.peer.vml.ElementPeer.prototype.attachChangeEventListener = function(type, listener)
{
    var listeners = this.getChangeEventListeners(type);
    if (!listener)
    {
        throw "Listener can not be null";
    }
    listeners.push(listener);
};

web2d.peer.vml.ElementPeer.prototype.setSize = function(width, height)
{
    // First set the size of the group element.
    if (core.Utils.isDefined(width))
    {
        this._native.style.width = parseInt(width);
    }

    if (core.Utils.isDefined(height))
    {
        this._native.style.height = parseInt(height);
    }
};

web2d.peer.vml.ElementPeer.prototype.getChildren = function()
{
    var result = this._children;
    if (!result)
    {
        result = [];
        this._children = result;
    }
    return result;
};

web2d.peer.vml.ElementPeer.prototype.setChildren = function(children)
{
    this._children = children;
};

web2d.peer.vml.ElementPeer.prototype.removeChild = function(elementPeer)
{
    // Store parent and child relationship.
    elementPeer.setParent(null);
    var children = this.getChildren();

    // Remove from children array ...
    var length = children.length;

    children.remove(elementPeer);

    var newLength = children.length;

    if (newLength >= length)
    {
        throw "Could not remove the element.";
    }
    /*var found = false;
    children = children.reject(function(iter)
    {
        var equals = (iter._native === elementPeer._native);
        if (equals)
        {
            found = true;
        }
        return equals;
    });

    // Could found the element ?
    if (!found)
    {
        throw "Could not remove the element.";
    }*/

    // Append element as a child.
    this._native.removeChild(elementPeer._native);
};

web2d.peer.vml.ElementPeer.prototype.appendChild = function(elementPeer)
{
    // Warning: Posible memory leak.
    // Store parent and child relationship.
    elementPeer.setParent(this);

    var children = this.getChildren();
    children.push(elementPeer);

    // Add native element ..
    this._native.appendChild(elementPeer._native);

    // Broadcast events ...
    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "onChangeCoordSize");
};

web2d.peer.vml.ElementPeer.prototype.getSize = function()
{
    return {
        width:parseFloat(this._native.style.width),
        height:parseFloat(this._native.style.height)
    };
};

web2d.peer.vml.ElementPeer.prototype.setPosition = function(x, y)
{
    if (core.Utils.isDefined(x))
    {
        this._position.x = parseInt(x);
        this._native.style.left = x;
    }

    if (core.Utils.isDefined(y))
    {
        this._position.y = parseInt(y);
        this._native.style.top = y;
    }
};

web2d.peer.vml.ElementPeer.prototype.getPosition = function()
{
    return {x:this._position.x, y:this._position.y};
};

web2d.peer.vml.ElementPeer.prototype.getVMLFill = function()
{
    if (!this._vmlFill)
    {
        this._vmlFill = window.document.createElement('v:fill');
        this._native.appendChild(this._vmlFill);
    }
    return this._vmlFill;
};

web2d.peer.vml.ElementPeer.prototype.setFill = function(color, opacity)
{
    var vmlFill = this.getVMLFill();
    if (core.Utils.isDefined(color))
    {
        vmlFill.setAttribute('color', color);
    }

    if (core.Utils.isDefined(opacity))
    {
        vmlFill.setAttribute('opacity', opacity);
    }

};

web2d.peer.vml.ElementPeer.prototype.getFill = function()
{
    var vmlFill = this.getVMLFill();
    var color = vmlFill.getAttribute('color');

    var opacity = vmlFill.getAttribute('opacity');
    opacity = opacity.toFixed(1);

    return {color:String(color), opacity:Number(opacity)};
};

web2d.peer.vml.ElementPeer.prototype.getVMLStroke = function()
{
    if (!this._vmlStroke)
    {
        this._vmlStroke = window.document.createElement('v:stroke');
        this._native.appendChild(this._vmlStroke);
    }
    return this._vmlStroke;
};

web2d.peer.vml.ElementPeer.prototype.getStroke = function()
{
    var vmlStroke = this.getVMLStroke();
    var color = vmlStroke.getAttribute('color');
    var dashstyle = vmlStroke.getAttribute('dashstyle');
    var opacity = vmlStroke.getAttribute('opacity');
    var width = this._strokeWidth;
    if (width == -1)
    {
        width = 0;
    }
    return {color: color, style: dashstyle, opacity: opacity, width: width};
};

/*
* http://msdn.microsoft.com/workshop/author/VML/ref/adv2.asp
* 	"solid|dot|dash|dashdot|longdash|longdashdot|longdashdotdot".
* /**
* 	The opacity of the entire shape. A fraction between 0 (completely transparent) and 1 (completely opaque.)
*/
web2d.peer.vml.ElementPeer.prototype.setStroke = function(width, style, color, opacity)
{
    var vmlStroke = this.getVMLStroke();
    if (core.Utils.isDefined(color))
    {
        vmlStroke.setAttribute('color', color);
    }

    if (core.Utils.isDefined(style))
    {
        vmlStroke.setAttribute('dashstyle', style);
    }

    if (core.Utils.isDefined(opacity))
    {
        vmlStroke.setAttribute('opacity', opacity);
    }

    if (core.Utils.isDefined(width))
    {
        var scaleStrokeWidth = 0;
        if (width !== 0)
        {
            this._strokeWidth = width;
            var scale = web2d.peer.utils.TransformUtil.workoutScale(this);

            scaleStrokeWidth = scale.width * this._strokeWidth;
            scaleStrokeWidth = scaleStrokeWidth.toFixed(2);
            vmlStroke.setAttribute('weight', scaleStrokeWidth + "px");
            vmlStroke.setAttribute('on', 'true');
        }
        else
        {
            vmlStroke.setAttribute('strokeweight', 0);
            vmlStroke.setAttribute('on', 'false');
        }
    }
};

/**
 * 	If hidden the shape is not rendered and does not generate mouse events.
 */
web2d.peer.vml.ElementPeer.prototype.setVisibility = function(isVisible)
{
    this._native.style.visibility = (isVisible) ? 'visible' : 'hidden';
};

web2d.peer.vml.ElementPeer.prototype.isVisible = function(isVisible)
{
    var visibility = this._native.style.visibility;
    return !(visibility == 'hidden');
};
;

web2d.peer.vml.ElementPeer.prototype._updateStrokeWidth = function()
{
    if (this.getParent())
    {
        this.setStroke(this._strokeWidth);
    }
};

web2d.peer.vml.ElementPeer.prototype.getParent = function()
{
    return this._parent;
};

web2d.peer.vml.ElementPeer.prototype.setParent = function(parent)
{
    this._parent = parent;
};

/**
 * Move element to the front
 */
web2d.peer.vml.ElementPeer.prototype.moveToFront = function()
{
    this._native.parentNode.appendChild(this._native);
};

/**
 * Move element to the back
 */
web2d.peer.vml.ElementPeer.prototype.moveToBack = function()
{
    this._native.parentNode.insertBefore(this._native, this._native.parentNode.firstChild);
};

web2d.peer.vml.ElementPeer.prototype.setCursor = function(type)
{
    this._native.style.cursor = type;
};
