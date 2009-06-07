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

web2d.Element = function(peer, attributes)
{
    this._peer = peer;
    if (peer == null)
    {
        throw "Element peer can not be null";
    }

    this._dispatcherByEventType = new Hash({});
    if (core.Utils.isDefined(attributes))
    {
        this._initialize(attributes);
    }
};

web2d.Element.prototype._SIGNATURE_MULTIPLE_ARGUMENTS = -1;

web2d.Element.prototype._initialize = function(attributes)
{
    var batchExecute = {};

    // Collect arguments ...
    for (var key in attributes)
    {
        var funcName = this._attributeNameToFuncName(key, 'set');
        var funcArgs = batchExecute[funcName];
        if (!funcArgs)
        {
            funcArgs = [];
        }

        var signature = this._propertyNameToSignature[key];
        var argPositions = signature[1];
        if (argPositions != this._SIGNATURE_MULTIPLE_ARGUMENTS)
        {
            funcArgs[argPositions] = attributes[key];
        } else
        {
            funcArgs = attributes[key].split(' ');
        }
        batchExecute[funcName] = funcArgs;
    }

    // Call functions ...
    for (var key in batchExecute)
    {
        var func = this[key];
        if (!func)
        {
            throw "Could not find function: " + key;
        }
        func.apply(this, batchExecute[key]);
    }
};

web2d.Element.prototype.setSize = function(width, height)
{
    this._peer.setSize(width, height);
};

web2d.Element.prototype.setPosition = function(cx, cy)
{
    this._peer.setPosition(cx, cy);
};

web2d.Element.prototype._supportedEvents = ["click","dblclick","mousemove","mouseout","mouseover","mousedown","mouseup"];

/**
 * Allows the registration of event listeners on the event target.
 * type
 *     A string representing the event type to listen for.
 * listener
 *     The object that receives a notification when an event of the specified type occurs. This must be an object implementing the EventListener interface, or simply a function in JavaScript.
 *
 * The following events types are supported:
 *
 */
web2d.Element.prototype.addEventListener = function(type, listener)
{
    if (!this._supportedEvents.include(type))
    {
        throw "Unsupported event type: " + type;
    }

    // Concat previous event listeners for a given type.
    if (!this._dispatcherByEventType[type])
    {
        this._dispatcherByEventType[type] = new web2d.EventDispatcher(this);

        var eventListener = this._dispatcherByEventType[type].eventListener;
        this._peer.addEventListener(type, eventListener);
    }

    this._dispatcherByEventType[type].addListener(type, listener);
};
/**
 *
 * Allows the removal of event listeners from the event target.
 *
 * Parameters:
 * type
 *    A string representing the event type being registered.
 * listener
 *     The listener parameter takes an interface implemented by the user which contains the methods to be called when the event occurs.
 *     This interace will be invoked passing an event as argument and the 'this' referece in the function will be the element.
 */
web2d.Element.prototype.removeEventListener = function(type, listener)
{
    var dispatcher = this._dispatcherByEventType[type];
    if (dispatcher == null)
    {
        throw "There is no listener previously registered";
    }
    var result = dispatcher.removeListener(type, listener);

    // If there is not listeners, EventDispatcher must be removed.
    if (dispatcher.getListenersCount() <= 0)
    {
        this._peer.removeEventListener(type, dispatcher.eventListener);
        this._dispatcherByEventType[type] = null;
    }
};

/**
 * /*
 * Returns element type name.
 */
web2d.Element.prototype.getType = function()
{
    throw "Not implemeneted yet. This method must be implemented by all the inherited objects.";
};

/**
 * Todo: Doc
 */
web2d.Element.prototype.getFill = function()
{
    return this._peer.getFill();
};

/**
 * Used to define the fill element color and element opacity.
 * color: Fill color
 * opacity: Opacity of the fill. It must be less than 1.
 */
web2d.Element.prototype.setFill = function(color, opacity)
{
    this._peer.setFill(color, opacity);
};

web2d.Element.prototype.getPosition = function()
{
    return this._peer.getPosition();
};

/*
*  Defines the element stroke properties.
*  width: stroke width
*  style: "solid|dot|dash|dashdot|longdash".
*  color: stroke color
*  opacity: stroke visibility
*/
web2d.Element.prototype.setStroke = function(width, style, color, opacity)
{
    if (style != null && style != undefined && style != 'dash' && style != 'dot' && style != 'solid' && style != 'longdash' && style != "dashdot")
    {
        throw "Unsupported stroke style: '" + style + "'";
    }
    this._peer.setStroke(width, style, color, opacity);
};

web2d.Element.prototype._propertyNameToSignature =
{
// Format: [attribute name, argument position on setter, attribute name on getter]
    size: ['size',-1],
    width: ['size',0,'width'],
    height: ['size',1,'height'],

    position: ['position',-1],
    x: ['position',0,'x'],
    y: ['position',1,'y'],

    stroke:['stroke',-1],
    strokeWidth:['stroke',0,'width'],
    strokeStyle:['stroke',1,'style'],
    strokeColor:['stroke',2,'color'],
    strokeOpacity:['stroke',3,'opacity'],

    fill:['fill',-1],
    fillColor:['fill',0,'color'],
    fillOpacity:['fill',1,'opacity'],

    coordSize:['coordSize',-1],
    coordSizeWidth:['coordSize',0,'width'],
    coordSizeHeight:['coordSize',1,'height'],

    coordOrigin:['coordOrigin',-1],
    coordOriginX:['coordOrigin',0,'x'],
    coordOriginY:['coordOrigin',1,'y'],

    visibility:['visibility',0],
    opacity:['opacity',0]
};

web2d.Element.prototype._attributeNameToFuncName = function(attributeKey, prefix)
{
    var signature = this._propertyNameToSignature[attributeKey];
    if (!signature)
    {
        throw "Unsupported attribute: " + attributeKey;
    }

    var firstLetter = signature[0].charAt(0);
    return  prefix + firstLetter.toUpperCase() + signature[0].substring(1);

};

/**
 * All element properties can be setted using either a method invocation or attribute invocation.
 *  key: size, width, height, position, x, y, stroke, strokeWidth, strokeStyle, strokeColor, strokeOpacity,
 *       fill, fillColor, fillOpacity, coordSize, coordSizeWidth, coordSizeHeight, coordOrigin, coordOriginX, coordOrigiY
 */
web2d.Element.prototype.setAttribute = function(key, value)
{
    var funcName = this._attributeNameToFuncName(key, 'set');

    var signature = this._propertyNameToSignature[key];
    if (signature == null)
    {
        throw "Could not find the signature for:" + key;
    }

    // Parse arguments ..
    var argPositions = signature[1];
    var args = [];
    if (argPositions !== this._SIGNATURE_MULTIPLE_ARGUMENTS)
    {
        args[argPositions] = value;
    }
    else if (typeof value == "array")
    {
        args = value;
    } else
    {
        var strValue = String(value);
        args = strValue.split(' ');
    }

    // Look up method ...
    var setter = this[funcName];
    if (setter == null)
    {
        throw "Could not find the function name:" + funcName;
    }
    setter.apply(this, args);

};

web2d.Element.prototype.getAttribute = function(key)
{
    var funcName = this._attributeNameToFuncName(key, 'get');

    var signature = this._propertyNameToSignature[key];
    if (signature == null)
    {
        throw "Could not find the signature for:" + key;
    }

    var getter = this[funcName];
    if (getter == null)
    {
        throw "Could not find the function name:" + funcName;
    }

    var getterResult = getter.apply(this, []);
    var attibuteName = signature[2];
    if (!attibuteName)
    {
        throw "Could not find attribute mapping for:" + key;
    }

    var result = getterResult[attibuteName];
    if (!result)
    {
        throw "Could not find attribute with name:" + attibuteName;
    }

    return result;
};


/**
 * Defines the element opacity.
 * Parameters:
 *   opacity: A value between 0 and 1.
 */
web2d.Element.prototype.setOpacity = function(opacity)
{
    this._peer.setStroke(null, null, null, opacity);
    this._peer.setFill(null, opacity);
};

web2d.Element.prototype.setVisibility = function(isVisible)
{
    this._peer.setVisibility(isVisible);
};


web2d.Element.prototype.isVisible = function()
{
    return this._peer.isVisible();
};

/**
 * Move the element to the front
 */
web2d.Element.prototype.moveToFront = function()
{
    this._peer.moveToFront();
};

/**
 * Move the element to the back
 */
web2d.Element.prototype.moveToBack = function()
{
    this._peer.moveToBack();
};

web2d.Element.prototype.getStroke = function()
{
    return this._peer.getStroke();
};


web2d.Element.prototype.setCursor = function(type)
{
    this._peer.setCursor(type);
};

web2d.Element.prototype.getParent = function(){
    return this._peer.getParent();
}
