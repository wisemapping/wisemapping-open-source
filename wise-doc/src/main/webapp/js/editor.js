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

var designer = null;

function buildDesigner() {

    var container = $('mindplot');
    container.setStyles({
        height: parseInt(screen.height),
        width:  parseInt(screen.width)
    });

    var editorProperties = {zoom:0.85,saveOnLoad:true,collab:collab};
    designer = new mindplot.Designer(editorProperties, container);
    designer.setViewPort({
        height: parseInt(window.innerHeight-70), // Footer and Header
        width:  parseInt(window.innerWidth)
    });

    var menu = new mindplot.widget.Menu(designer, 'toolbar',mapId);

    //  If a node has focus, focus can be move to another node using the keys.
    designer._cleanScreen = function() {
        menu.clear()
    };
    return designer;
}

//######################### Libraries Loading ##################################
function JSPomLoader(pomUrl, callback) {
    console.log("POM Load URL:" + pomUrl);
    var jsUrls;
    var request = new Request({
        url: pomUrl,
        method: 'get',
        onRequest: function() {
            console.log("loading ...");
        },
        onSuccess: function(responseText, responseXML) {

            // Collect JS Urls ...
            var concatRoot = responseXML.getElementsByTagName('concat');
            var fileSetArray = Array.filter(concatRoot[0].childNodes, function(elem) {
                return elem.nodeType == Node.ELEMENT_NODE
            });

            jsUrls = new Array();
            Array.each(fileSetArray, function(elem) {
                    var jsUrl = elem.getAttribute("dir") + elem.getAttribute("files");
                    jsUrls.push(jsUrl.replace("${basedir}", pomUrl.substring(0, pomUrl.lastIndexOf('/'))));
                }
            );

            // Load all JS dynamically ....
            jsUrls = jsUrls.reverse();

            function jsRecLoad(urls) {
                if (urls.length == 0) {
                    if ($defined(callback))
                        callback();
                } else {
                    var url = urls.pop();
                    Asset.javascript(url, {
                        onLoad: function() {
                            jsRecLoad(urls)
                        }
                    });
                }
            }

            jsRecLoad(jsUrls);
        },
        onFailure: function() {
            console.log('Sorry, your request failed :(');
        }
    });
    request.send();

}

var localEnv = true;
if (localEnv) {
    Asset.javascript("../../../../../web2d/target/classes/web2d.svg-min.js", {
        onLoad: function() {
            JSPomLoader('../../../../../mindplot/pom.xml', function() {
                $(document).fireEvent('loadcomplete', 'mind')
            });
        }
    });
} else {
    Asset.javascript("../js/mindplot-min.js", {
        onLoad:function() {
            $(document).fireEvent('loadcomplete', 'mind')
        }
    });
}