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

core.Utils = {

};


core.Utils.innerXML = function (node) {
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

/**
 * Cross-browser implementation of creating an XML document object.
 */
core.Utils.createDocument = function () {
    var doc = null;
    if ($defined(window.ActiveXObject)) {
        //http://blogs.msdn.com/b/xmlteam/archive/2006/10/23/using-the-right-version-of-msxml-in-internet-explorer.aspx
        var progIDs = [ 'Msxml2.DOMDocument.6.0', 'Msxml2.DOMDocument.3.0'];
        for (var i = 0; i < progIDs.length; i++) {
            try {
                doc = new ActiveXObject(progIDs[i]);
                break;
            }
            catch (ex) {
            }
        }
    } else if (window.document.implementation && window.document.implementation.createDocument) {
        doc = window.document.implementation.createDocument("", "", null);
    }
    $assert(doc, "Parser could not be instantiated");

    return doc;
};