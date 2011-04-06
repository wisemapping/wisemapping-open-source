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
 *         The inheriting class, or subclass
 * @param {Object} baseClass
 *         The class from which to inherit
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
        wLogger.error(message + "," + stack);
//        core.Logger.logError(message + "," + stack);
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
    if(core.Utils.isDefined(event.$extended)){
        event = event.event;
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
        }
    }
    return null;
};

core.Utils.calculateRelationShipPointCoordinates = function(topic,controlPoint){
    var size = topic.getSize();
    var position = topic.getPosition();
    var m = (position.y-controlPoint.y)/(position.x-controlPoint.x);
    var y,x;
    var gap = 5;
    if(controlPoint.y>position.y+(size.height/2)){
        y = position.y+(size.height/2)+gap;
        x = position.x-((position.y-y)/m);
        if(x>position.x+(size.width/2)){
            x=position.x+(size.width/2);
        }else if(x<position.x-(size.width/2)){
            x=position.x-(size.width/2);
        }
    }else if(controlPoint.y<position.y-(size.height/2)){
        y = position.y-(size.height/2) - gap;
        x = position.x-((position.y-y)/m);
        if(x>position.x+(size.width/2)){
            x=position.x+(size.width/2);
        }else if(x<position.x-(size.width/2)){
            x=position.x-(size.width/2);
        }
    }else if(controlPoint.x<(position.x-size.width/2)){
        x = position.x-(size.width/2) -gap;
        y = position.y-(m*(position.x-x));
    }else{
        x = position.x+(size.width/2) + gap;
        y = position.y-(m*(position.x-x));
    }

    return new core.Point(x,y);
};

core.Utils.calculateDefaultControlPoints = function(srcPos, tarPos){
    var y = srcPos.y-tarPos.y;
    var x = srcPos.x-tarPos.x;
    var m = y/x;
    var l = Math.sqrt(y*y+x*x)/3;
    var fix=1;
    if(srcPos.x>tarPos.x){
        fix=-1;
    }
    
    var x1 = srcPos.x + Math.sqrt(l*l/(1+(m*m)))*fix;
    var y1 = m*(x1-srcPos.x)+srcPos.y;
    var x2 = tarPos.x + Math.sqrt(l*l/(1+(m*m)))*fix*-1;
    var y2= m*(x2-tarPos.x)+tarPos.y;

    return [new core.Point(-srcPos.x + x1,-srcPos.y + y1),new core.Point(-tarPos.x + x2,-tarPos.y + y2)];
};

core.Utils.setVisibilityAnimated = function(elems, isVisible, doneFn){
    core.Utils.animateVisibility(elems, isVisible, doneFn);
};
core.Utils.setChildrenVisibilityAnimated = function(rootElem, isVisible){
    var children = core.Utils._addInnerChildrens(rootElem);
    core.Utils.animateVisibility(children, isVisible);
};

core.Utils.animateVisibility = function (elems, isVisible, doneFn){
    var _fadeEffect=null;
    var _opacity = (isVisible?0:1);
    if(isVisible){
        elems.forEach(function(child, index){
                child.setOpacity(_opacity);
                child.setVisibility(isVisible);
            });
    }
    var fadeEffect = function(index)
    {
        var step = 10;
        if((_opacity<=0 && !isVisible) || (_opacity>=1 && isVisible)){
            $clear(_fadeEffect);
            _fadeEffect = null;
            elems.forEach(function(child, index){

                child.setVisibility(isVisible);

            });
            if(core.Utils.isDefined(doneFn))
                doneFn.attempt();
        }
        else{
            var fix = 1;
            if(isVisible){
                fix = -1;
            }
            _opacity-=(1/step)*fix;
            elems.forEach(function(child, index){
                child.setOpacity(_opacity);
            });
        }

    };
    _fadeEffect =fadeEffect.periodical(10);
};

core.Utils._addInnerChildrens = function(elem){
    var children = [];
    var childs = elem._getChildren();
    for(var i = 0 ; i<childs.length; i++){
        var child = childs[i];
        children.push(child);
        children.push(child.getOutgoingLine());
        var relationships = child.getRelationships();
        children = children.concat(relationships);
        var innerChilds = core.Utils._addInnerChildrens(child);
        children = children.concat(innerChilds);
    }
    return children;
};