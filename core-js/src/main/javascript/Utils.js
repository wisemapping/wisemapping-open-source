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
