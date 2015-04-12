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

web2d.Element = new Class({
    initialize : function(peer, attributes) {
        this._peer = peer;
        if (peer == null) {
            throw new Error("Element peer can not be null");
        }

        if ($defined(attributes)) {
            this._initialize(attributes);
        }
    },

    _initialize : function(attributes) {
        var batchExecute = {};

        // Collect arguments ...
        for (var key in attributes) {
            var funcName = this._attributeNameToFuncName(key, 'set');
            var funcArgs = batchExecute[funcName];
            if (!$defined(funcArgs)) {
                funcArgs = [];
            }

            var signature = web2d.Element._propertyNameToSignature[key];
            var argPositions = signature[1];
            if (argPositions != web2d.Element._SIGNATURE_MULTIPLE_ARGUMENTS) {
                funcArgs[argPositions] = attributes[key];
            } else {
                funcArgs = attributes[key].split(' ');
            }
            batchExecute[funcName] = funcArgs;
        }

        // Call functions ...
        for (var key in batchExecute) {
            var func = this[key];
            if (!$defined(func)) {
                throw new Error("Could not find function: " + key);
            }
            func.apply(this, batchExecute[key]);
        }
    },

    setSize : function(width, height) {
        this._peer.setSize(width, height);
    },

    setPosition : function(cx, cy) {
        this._peer.setPosition(cx, cy);
    },

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
    addEvent : function(type, listener) {
        this._peer.addEvent(type, listener);
    },

    trigger : function(type, event) {
        this._peer.trigger(type, event);
    },

    cloneEvents : function(from) {
        this._peer.cloneEvents(from);
    },
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
    removeEvent : function(type, listener) {
        this._peer.removeEvent(type, listener);
    },

    /**
     * /*
     * Returns element type name.
     */
    getType : function() {
        throw new Error("Not implemeneted yet. This method must be implemented by all the inherited objects.");
    },

    /**
     * Todo: Doc
     */
    getFill : function() {
        return this._peer.getFill();
    },

    /**
     * Used to define the fill element color and element opacity.
     * color: Fill color
     * opacity: Opacity of the fill. It must be less than 1.
     */
    setFill : function(color, opacity) {
        this._peer.setFill(color, opacity);
    },

    getPosition : function() {
        return this._peer.getPosition();
    },

    getNativePosition: function() {
        return this._peer.getNativePosition();
    },

    /*
     *  Defines the element stroke properties.
     *  width: stroke width
     *  style: "solid|dot|dash|dashdot|longdash".
     *  color: stroke color
     *  opacity: stroke visibility
     */
    setStroke : function(width, style, color, opacity) {
        if (style != null && style != undefined && style != 'dash' && style != 'dot' && style != 'solid' && style != 'longdash' && style != "dashdot") {
            throw new Error("Unsupported stroke style: '" + style + "'");
        }
        this._peer.setStroke(width, style, color, opacity);
    },


    _attributeNameToFuncName : function(attributeKey, prefix) {
        var signature = web2d.Element._propertyNameToSignature[attributeKey];
        if (!$defined(signature)) {
            throw "Unsupported attribute: " + attributeKey;
        }

        var firstLetter = signature[0].charAt(0);
        return  prefix + firstLetter.toUpperCase() + signature[0].substring(1);

    },

    /**
     * All element properties can be setted using either a method invocation or attribute invocation.
     *  key: size, width, height, position, x, y, stroke, strokeWidth, strokeStyle, strokeColor, strokeOpacity,
     *       fill, fillColor, fillOpacity, coordSize, coordSizeWidth, coordSizeHeight, coordOrigin, coordOriginX, coordOrigiY
     */
    setAttribute : function(key, value) {
        var funcName = this._attributeNameToFuncName(key, 'set');

        var signature = web2d.Element._propertyNameToSignature[key];
        if (signature == null) {
            throw "Could not find the signature for:" + key;
        }

        // Parse arguments ..
        var argPositions = signature[1];
        var args = [];
        if (argPositions !== this._SIGNATURE_MULTIPLE_ARGUMENTS) {
            args[argPositions] = value;
        }
        else if (typeof value == "array") {
            args = value;
        } else {
            var strValue = String(value);
            args = strValue.split(' ');
        }

        // Look up method ...
        var setter = this[funcName];
        if (setter == null) {
            throw "Could not find the function name:" + funcName;
        }
        setter.apply(this, args);

    },

    getAttribute : function(key) {
        var funcName = this._attributeNameToFuncName(key, 'get');

        var signature = web2d.Element._propertyNameToSignature[key];
        if (signature == null) {
            throw "Could not find the signature for:" + key;
        }

        var getter = this[funcName];
        if (getter == null) {
            throw "Could not find the function name:" + funcName;
        }

        var getterResult = getter.apply(this, []);
        var attibuteName = signature[2];
        if (!$defined(attibuteName)) {
            throw "Could not find attribute mapping for:" + key;
        }

        var result = getterResult[attibuteName];
        if (!$defined(result)) {
            throw "Could not find attribute with name:" + attibuteName;
        }

        return result;
    },


    /**
     * Defines the element opacity.
     * Parameters:
     *   opacity: A value between 0 and 1.
     */
    setOpacity : function(opacity) {
        this._peer.setStroke(null, null, null, opacity);
        this._peer.setFill(null, opacity);
    },

    setVisibility : function(isVisible) {
        this._peer.setVisibility(isVisible);
    },


    isVisible : function() {
        return this._peer.isVisible();
    },

    /**
     * Move the element to the front
     */
    moveToFront : function() {
        this._peer.moveToFront();
    },

    /**
     * Move the element to the back
     */
    moveToBack : function() {
        this._peer.moveToBack();
    },

    getStroke : function() {
        return this._peer.getStroke();
    },


    setCursor : function(type) {
        this._peer.setCursor(type);
    },

    getParent : function() {
        return this._peer.getParent();
    }
});

web2d.Element._SIGNATURE_MULTIPLE_ARGUMENTS = -1;
web2d.Element._supportedEvents = ["click","dblclick","mousemove","mouseout","mouseover","mousedown","mouseup"];
web2d.Element._propertyNameToSignature =
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
