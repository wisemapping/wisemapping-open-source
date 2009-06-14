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

core.Utils =
{
    isDefined: function(val)
    {
        return val !== null && val !== undefined;
    },
    escapeInvalidTags: function (text)
    {
        //todo:Pablo. scape invalid tags in a text
        return text;
    }
};

/**
 * http://kevlindev.com/tutorials/javascript/inheritance/index.htm
 * A function used to extend one class with another
 *
 * @param {Object} subClass
 * 		The inheriting class, or subclass
 * @param {Object} baseClass
 * 		The class from which to inherit
 */
objects = {};
objects.extend = function(subClass, baseClass) {
    function inheritance() {
    }
    inheritance.prototype = baseClass.prototype;

    subClass.prototype = new inheritance();
    subClass.prototype.constructor = subClass;
    subClass.baseConstructor = baseClass;
    subClass.superClass = baseClass.prototype;
};

core.assert = function(assert, message)
{
    if (!assert)
    {
        var stack;
        try
        {
            null.eval();
        } catch(e)
        {
            stack = e;
        }
        core.Logger.logError(message + "," + stack);
    }

};


Math.sign = function(value)
{
    return (value >= 0) ? 1 : -1;
};


// Extensions ....
function $import(src) {
    var scriptElem = document.createElement('script');
    scriptElem.setAttribute('src', src);
    scriptElem.setAttribute('type', 'text/javascript');
    document.getElementsByTagName('head')[0].appendChild(scriptElem);
}

/**
 *  Retrieve the mouse position.
 */
core.Utils.getMousePosition = function(event)
{
    var xcoord = -1;
    var ycoord = -1;

    if (!event) {
        if (window.event) {
            //Internet Explorer
            event = window.event;
        } else {
            //total failure, we have no way of referencing the event
            throw "Could not obtain mouse position";
        }
    }
    if (typeof( event.pageX ) == 'number') {
        //most browsers
        xcoord = event.pageX;
        ycoord = event.pageY;
    } else if (typeof( event.clientX ) == 'number') {
        //Internet Explorer and older browsers
        //other browsers provide this, but follow the pageX/Y branch
        xcoord = event.clientX;
        ycoord = event.clientY;
        var badOldBrowser = ( window.navigator.userAgent.indexOf('Opera') + 1 ) ||
                            ( window.ScriptEngine && ScriptEngine().indexOf('InScript') + 1 ) ||
                            ( navigator.vendor == 'KDE' );
        if (!badOldBrowser) {
            if (document.body && ( document.body.scrollLeft || document.body.scrollTop )) {
                //IE 4, 5 & 6 (in non-standards compliant mode)
                xcoord += document.body.scrollLeft;
                ycoord += document.body.scrollTop;
            } else if (document.documentElement && ( document.documentElement.scrollLeft || document.documentElement.scrollTop )) {
                //IE 6 (in standards compliant mode)
                xcoord += document.documentElement.scrollLeft;
                ycoord += document.documentElement.scrollTop;
            }
        }
    } else {
        throw "Could not obtain mouse position";
    }


    return {x:xcoord,y:ycoord};
};


/**
 * Calculate the position of the passed element.
 */
core.Utils.workOutDivElementPosition = function(divElement)
{
    var curleft = 0;
    var curtop = 0;
    if (divElement.offsetParent) {
        curleft = divElement.offsetLeft;
        curtop = divElement.offsetTop;
        while (divElement = divElement.offsetParent) {
            curleft += divElement.offsetLeft;
            curtop += divElement.offsetTop;
        }
    }
    return {x:curleft,y:curtop};
};


core.Utils.innerXML = function(/*Node*/node) {
    //	summary:
    //		Implementation of MS's innerXML function.
    if (node.innerXML) {
        return node.innerXML;
        //	string
    } else if (node.xml) {
        return node.xml;
        //	string
    } else if (typeof XMLSerializer != "undefined") {
        return (new XMLSerializer()).serializeToString(node);
        //	string
    }
};

core.Utils.createDocument = function() {
    //	summary:
    //		cross-browser implementation of creating an XML document object.
    var doc = null;
    var _document = window.document;
    if (window.ActiveXObject) {
        var prefixes = [ "MSXML2", "Microsoft", "MSXML", "MSXML3" ];
        for (var i = 0; i < prefixes.length; i++) {
            try {
                doc = new ActiveXObject(prefixes[i] + ".XMLDOM");
            } catch(e) { /* squelch */
            }
            ;

            if (doc) {
                break;
            }
        }
    } else if ((_document.implementation) &&
               (_document.implementation.createDocument)) {
        doc = _document.implementation.createDocument("", "", null);
    }

    return doc;
    //	DOMDocument
};

core.Utils.createDocumentFromText = function(/*string*/str, /*string?*/mimetype) {
    //	summary:
    //		attempts to create a Document object based on optional mime-type,
    //		using str as the contents of the document
    if (!mimetype) {
        mimetype = "text/xml";
    }
    if (window.DOMParser)
    {
        var parser = new DOMParser();
        return parser.parseFromString(str, mimetype);
        //	DOMDocument
    } else if (window.ActiveXObject) {
        var domDoc = core.Utils.createDocument();
        if (domDoc) {
            domDoc.async = false;
            domDoc.loadXML(str);
            return domDoc;
            //	DOMDocument
        } else {
            //dojo.debug("toXml didn't work?");
        }
    } else {
        //		var _document = dojo.doc();
        //		if(_document.createElement){
        //			// FIXME: this may change all tags to uppercase!
        //			var tmp = _document.createElement("xml");
        //			tmp.innerHTML = str;
        //			if(_document.implementation && _document.implementation.createDocument){
        //				var xmlDoc = _document.implementation.createDocument("foo", "", null);
        //				for(var i = 0; i < tmp.childNodes.length; i++) {
        //					xmlDoc.importNode(tmp.childNodes.item(i), true);
        //				}
        //				return xmlDoc;	//	DOMDocument
        //			}
        //			// FIXME: probably not a good idea to have to return an HTML fragment
        //			// FIXME: the tmp.doc.firstChild is as tested from IE, so it may not
        //			// work that way across the board
        //			return ((tmp.document)&&
        //				(tmp.document.firstChild ?  tmp.document.firstChild : tmp));	//	DOMDocument
        //		}
    }
    return null;
};
