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

web2d.peer.svg.ElementPeer = function(svgElement)
{
    this._native = svgElement;
    this._dblClickListeners = new Hash();
    this._size = {width:1,height:1};
    this._changeListeners = {};
    // http://support.adobe.com/devsup/devsup.nsf/docs/50493.htm
};

web2d.peer.svg.ElementPeer.prototype.svgNamespace = 'http://www.w3.org/2000/svg';
web2d.peer.svg.ElementPeer.prototype.linkNamespace = 'http://www.w3.org/1999/xlink';

web2d.peer.svg.ElementPeer.prototype.setChildren = function(children)
{
    this._children = children;
};

web2d.peer.svg.ElementPeer.prototype.getChildren = function()
{
    var result = this._children;
    if (!$defined(result))
    {
        result = [];
        this._children = result;
    }
    return result;
};

web2d.peer.svg.ElementPeer.prototype.getParent = function()
{
    return this._parent;
};

web2d.peer.svg.ElementPeer.prototype.setParent = function(parent)
{
    this._parent = parent;
};

web2d.peer.svg.ElementPeer.prototype.appendChild = function(elementPeer)
{
    // Store parent and child relationship.
    elementPeer.setParent(this);
    var children = this.getChildren();
    children.include(elementPeer);

    // Append element as a child.
    this._native.appendChild(elementPeer._native);

    // Broadcast events ...
    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "strokeStyle");
};


web2d.peer.svg.ElementPeer.prototype.removeChild = function(elementPeer)
{
    // Store parent and child relationship.
    elementPeer.setParent(null);
    var children = this.getChildren();

    // Remove from children array ...
    var length = children.length;

    children.erase(elementPeer);

    var newLength = children.length;
    if (newLength >= length)
    {
        throw "Could not remove the element.";
    }
    /*var found = false;
    children = children.reject(function(iter)
    {
        var equals = (iter._native == elementPeer._native);
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

/**
 * http://www.w3.org/TR/DOM-Level-3-Events/events.html
 * http://developer.mozilla.org/en/docs/addEventListener
 */
web2d.peer.svg.ElementPeer.prototype.addEventListener = function(type, listener)
{
    if (type == 'dblclick')
    {
        // This is workaround to support double click...
        var dblListener = function(e)
        {
            if (e.detail >= 2)
            {
                listener.call(this, e);
            }
        };

        this._dblClickListeners[listener] = dblListener;
        this._native.addEventListener(type, dblListener, false);
    } else
    {
        this._native.addEventListener(type, listener, false);
    }
};

web2d.peer.svg.ElementPeer.prototype.removeEventListener = function(type, listener)
{
    if (type == 'dblclick')
    {
        // This is workaround to support double click...
        var dblClickListener = this._dblClickListeners[listener];
        if (dblClickListener == null)
        {
            throw "Could not find listener to remove";
        }
        type = 'click';
        this._native.removeEventListener(type, dblClickListener, false);
        delete this._dblClickListeners[listener];
    } else
    {
        this._native.removeEventListener(type, listener, false);
    }
};

web2d.peer.svg.ElementPeer.prototype.setSize = function(width, height)
{
    if ($defined(width) && this._size.width != parseInt(width))
    {
        this._size.width = parseInt(width);
        this._native.setAttribute('width', parseInt(width));
    }

    if ($defined(height) && this._size.height != parseInt(height))
    {
        this._size.height = parseInt(height);
        this._native.setAttribute('height', parseInt(height));
    }

    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "strokeStyle");
};

web2d.peer.svg.ElementPeer.prototype.getSize = function()
{
    return {width:this._size.width,height:this._size.height};
};

web2d.peer.svg.ElementPeer.prototype.setFill = function(color, opacity)
{
    if ($defined(color))
    {
        this._native.setAttribute('fill', color);
    }
    if ($defined(opacity))
    {
        this._native.setAttribute('fill-opacity', opacity);
    }
};

web2d.peer.svg.ElementPeer.prototype.getFill = function()
{
    var color = this._native.getAttribute('fill');
    var opacity = this._native.getAttribute('fill-opacity');
    return {color:color, opacity:Number(opacity)};
};

web2d.peer.svg.ElementPeer.prototype.getStroke = function()
{
    var vmlStroke = this._native;
    var color = vmlStroke.getAttribute('stroke');
    var dashstyle = this._stokeStyle;
    var opacity = vmlStroke.getAttribute('stroke-opacity');
    var width = vmlStroke.getAttribute('stroke-width');
    return {color: color, style: dashstyle, opacity: opacity, width: width};
};

web2d.peer.svg.ElementPeer.prototype.__stokeStyleToStrokDasharray = {solid:[],dot:[1,3],dash:[4,3],longdash:[10,2],dashdot:[5,3,1,3]};
web2d.peer.svg.ElementPeer.prototype.setStroke = function(width, style, color, opacity)
{
    if ($defined(width))
    {
        this._native.setAttribute('stroke-width', width + "px");
    }
    if ($defined(color))
    {
        this._native.setAttribute('stroke', color);
    }
    if ($defined(style))
    {
        // Scale the dash array in order to be equal to VML. In VML, stroke style doesn't scale.
        var dashArrayPoints = this.__stokeStyleToStrokDasharray[style];
        var scale = 1 / web2d.peer.utils.TransformUtil.workoutScale(this).width;

        var strokeWidth = this._native.getAttribute('stroke-width');
        strokeWidth = parseFloat(strokeWidth);

        var scaledPoints = [];
        for (var i = 0; i < dashArrayPoints.length; i++)
        {
            // VML scale the stroke based on the stroke width.
            scaledPoints[i] = dashArrayPoints[i] * strokeWidth;

            // Scale the points based on the scale.
            scaledPoints[i] = (scaledPoints[i] * scale) + "px";
        }

        //        this._native.setAttribute('stroke-dasharray', scaledPoints);
        this._stokeStyle = style;
    }

    if ($defined(opacity))
    {
        this._native.setAttribute('stroke-opacity', opacity);
    }
};

/*
* style='visibility: visible'
*/
web2d.peer.svg.ElementPeer.prototype.setVisibility = function(isVisible)
{
    this._native.setAttribute('visibility', (isVisible) ? 'visible' : 'hidden');
};

web2d.peer.svg.ElementPeer.prototype.isVisible = function()
{
    var visibility = this._native.getAttribute('visibility');
    return !(visibility == 'hidden');
};

web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle = function()
{
    var strokeStyle = this._stokeStyle;
    if (this.getParent())
    {
        if (strokeStyle && strokeStyle != 'solid')
        {
            this.setStroke(null, strokeStyle);
        }
    }
};

web2d.peer.svg.ElementPeer.prototype.attachChangeEventListener = function(type, listener)
{
    var listeners = this.getChangeEventListeners(type);
    if (!$defined(listener))
    {
        throw "Listener can not be null";
    }
    listeners.push(listener);
};

web2d.peer.svg.ElementPeer.prototype.getChangeEventListeners = function(type)
{
    var listeners = this._changeListeners[type];
    if (!$defined(listeners))
    {
        listeners = [];
        this._changeListeners[type] = listeners;
    }
    return listeners;
};

/**
 * Move element to the front
 */
web2d.peer.svg.ElementPeer.prototype.moveToFront = function()
{
    this._native.parentNode.appendChild(this._native);
};

/**
 * Move element to the back
 */
web2d.peer.svg.ElementPeer.prototype.moveToBack = function()
{
    this._native.parentNode.insertBefore(this._native, this._native.parentNode.firstChild);
};

web2d.peer.svg.ElementPeer.prototype.setCursor = function(type)
{
    this._native.style.cursor = type;
};
