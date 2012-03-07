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

core.Utils = {

};

Math.sign = function(value) {
    return (value >= 0) ? 1 : -1;
};

core.Utils.innerXML = function(/*Node*/node) {
    //	summary:
    //		Implementation of MS's innerXML function.
    if ($defined(node.innerXML)) {
        return node.innerXML;
        //	string
    } else if ($defined(node.xml)) {
        return node.xml;
        //	string
    } else if ($defined(XMLSerializer)) {
        return (new XMLSerializer()).serializeToString(node);
        //	string
    }
};

core.Utils.createDocument = function() {
    //	summary:
    //		cross-browser implementation of creating an XML document object.
    var doc = null;
    var _document = window.document;
    if ($defined(window.ActiveXObject)) {
        var prefixes = [ "MSXML2", "Microsoft", "MSXML", "MSXML3" ];
        for (var i = 0; i < prefixes.length; i++) {
            try {
                doc = new ActiveXObject(prefixes[i] + ".XMLDOM");
            } catch(e) { /* squelch */
            }
            ;

            if ($defined(doc)) {
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
    if (!$defined(mimetype)) {
        mimetype = "text/xml";
    }
    if ($defined(window.DOMParser)) {
        var parser = new DOMParser();
        return parser.parseFromString(str, mimetype);
        //	DOMDocument
    } else if ($defined(window.ActiveXObject)) {
        var domDoc = core.Utils.createDocument();
        if ($defined(domDoc)) {
            domDoc.async = false;
            domDoc.loadXML(str);
            return domDoc;
            //	DOMDocument
        }
    }
    return null;
};

core.Utils.calculateRelationShipPointCoordinates = function(topic, controlPoint) {
    var size = topic.getSize();
    var position = topic.getPosition();
    var m = (position.y - controlPoint.y) / (position.x - controlPoint.x);
    var y,x;
    var gap = 5;
    if (controlPoint.y > position.y + (size.height / 2)) {
        y = position.y + (size.height / 2) + gap;
        x = position.x - ((position.y - y) / m);
        if (x > position.x + (size.width / 2)) {
            x = position.x + (size.width / 2);
        } else if (x < position.x - (size.width / 2)) {
            x = position.x - (size.width / 2);
        }
    } else if (controlPoint.y < position.y - (size.height / 2)) {
        y = position.y - (size.height / 2) - gap;
        x = position.x - ((position.y - y) / m);
        if (x > position.x + (size.width / 2)) {
            x = position.x + (size.width / 2);
        } else if (x < position.x - (size.width / 2)) {
            x = position.x - (size.width / 2);
        }
    } else if (controlPoint.x < (position.x - size.width / 2)) {
        x = position.x - (size.width / 2) - gap;
        y = position.y - (m * (position.x - x));
    } else {
        x = position.x + (size.width / 2) + gap;
        y = position.y - (m * (position.x - x));
    }

    return new core.Point(x, y);
};

core.Utils.calculateDefaultControlPoints = function(srcPos, tarPos) {
    var y = srcPos.y - tarPos.y;
    var x = srcPos.x - tarPos.x;
    var m = y / x;
    var l = Math.sqrt(y * y + x * x) / 3;
    var fix = 1;
    if (srcPos.x > tarPos.x) {
        fix = -1;
    }

    var x1 = srcPos.x + Math.sqrt(l * l / (1 + (m * m))) * fix;
    var y1 = m * (x1 - srcPos.x) + srcPos.y;
    var x2 = tarPos.x + Math.sqrt(l * l / (1 + (m * m))) * fix * -1;
    var y2 = m * (x2 - tarPos.x) + tarPos.y;

    return [new core.Point(-srcPos.x + x1, -srcPos.y + y1),new core.Point(-tarPos.x + x2, -tarPos.y + y2)];
};

core.Utils.animatePosition = function (elems, doneFn, designer) {
    var _moveEffect = null;
    var i = 10;
    var step = 10;
    var moveEffect = function () {
        if (i > 0) {
            var keys = elems.keys();
            for (var j = 0; j < keys.length; j++) {
                var id = keys[j];
                var mod = elems.get(id);
                var allTopics = designer.getModel().getTopics();
                var currentTopic = allTopics.filter(function(node) {
                    return node.getId() == id;
                })[0];
                var xStep = (mod.originalPos.x - mod.newPos.x) / step;
                var yStep = (mod.originalPos.y - mod.newPos.y) / step;
                var newPos = currentTopic.getPosition().clone();
                newPos.x += xStep;
                newPos.y += yStep;
                currentTopic.setPosition(newPos, false);
            }
        } else {
            $clear(_moveEffect);
            var keys = elems.keys();
            for (var j = 0; j < keys.length; j++) {
                var id = keys[j];
                var mod = elems.get(id);
                var allTopics = designer.getModel().getTopics();
                var currentTopic = allTopics.filter(function(node) {
                    return node.getId() == id;
                })[0];
                currentTopic.setPosition(mod.originalPos, false);
            }
            if ($defined(doneFn))
                doneFn.attempt();
        }
        i--;
    };
    _moveEffect = moveEffect.periodical(10);
};
