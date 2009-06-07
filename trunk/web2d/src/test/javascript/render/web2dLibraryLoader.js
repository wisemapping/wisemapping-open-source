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

// Modulue initializer.
web2d = {
    peer: {}
};

web2d.peer =
{
    svg: {},
    vml: {}
};

web2d.peer.utils = {};
web2d.Loader =
{
    load: function(scriptPath, stylePath, jsFileName)
    {
        var headElement = document.getElementsByTagName('head');
        var htmlDoc = headElement.item(0);
        var baseUrl = this.baseUrl(jsFileName);

        if (scriptPath && scriptPath.length > 0)
        {
            for (var i = 0; i < scriptPath.length; i++)
            {

                this.includeScriptNode(baseUrl + scriptPath[i]);
            }
        }
    },
    baseUrl: function(jsFileName)
    {
        var headElement = document.getElementsByTagName('head');
        var htmlDoc = headElement.item(0);
        var headChildren = htmlDoc.childNodes;
        var result = null;
        for (var i = 0; i < headChildren.length; i++)
        {
            var node = headChildren.item(i);
            if (node.nodeName && node.nodeName.toLowerCase() == "script")
            {
                var libraryUrl = node.src;
                if (libraryUrl.indexOf(jsFileName) != -1)
                {
                    var index = libraryUrl.lastIndexOf("/");
                    index = libraryUrl.lastIndexOf("/", index - 1);
                    result = libraryUrl.substring(0, index);
                }
            }
        }

        if (result == null)
        {
            throw "Could not obtain the base url directory.";
        }
        return result;
    },
    includeScriptNode: function(filename) {
        var html_doc = document.getElementsByTagName('head').item(0);
        var js = document.createElement('script');
        js.setAttribute('language', 'javascript');
        js.setAttribute('type', 'text/javascript');
        js.setAttribute('src', filename);
        html_doc.appendChild(js);
        return false;
    },
    includeStyleNode: function(filename) {
        var html_doc = document.getElementsByTagName('head').item(0);
        var js = document.createElement('link');
        js.setAttribute('rel', 'stylesheet');
        js.setAttribute('type', 'text/css');
        js.setAttribute('href', filename);
        html_doc.appendChild(js);
        return false;
    }
};


web2d.JsLoader =
{
    scriptPath: [
            "/render/mootools.js",
            "/../../../../core-js/target/classes/core.js",
            "/../../../src/main/javascript/EventDispatcher.js",
            "/../../../src/main/javascript/peer/vml/ElementPeer.js",
            "/../../../src/main/javascript/peer/svg/ElementPeer.js","" +
            "/../../../src/main/javascript/Element.js",
            "/../../../src/main/javascript/Workspace.js",
            "/../../../src/main/javascript/peer/svg/WorkspacePeer.js",
            "/../../../src/main/javascript/peer/vml/WorkspacePeer.js",
            "/../../../src/main/javascript/Toolkit.js",
            "/../../../src/main/javascript/Elipse.js",
            "/../../../src/main/javascript/peer/svg/ElipsePeer.js",
            "/../../../src/main/javascript/peer/vml/ElipsePeer.js",
            "/../../../src/main/javascript/Line.js",
            "/../../../src/main/javascript/peer/svg/LinePeer.js",
            "/../../../src/main/javascript/peer/vml/LinePeer.js",
            "/../../../src/main/javascript/PolyLine.js",
            "/../../../src/main/javascript/peer/svg/PolyLinePeer.js",
            "/../../../src/main/javascript/peer/vml/PolyLinePeer.js",
            "/../../../src/main/javascript/Group.js",
            "/../../../src/main/javascript/peer/svg/GroupPeer.js",
            "/../../../src/main/javascript/peer/vml/GroupPeer.js",
            "/../../../src/main/javascript/Rect.js",
            "/../../../src/main/javascript/peer/svg/RectPeer.js",
            "/../../../src/main/javascript/peer/vml/RectPeer.js",
            "/../../../src/main/javascript/Text.js",
            "/../../../src/main/javascript/peer/svg/TextPeer.js",
            "/../../../src/main/javascript/peer/vml/TextPeer.js",
            "/../../../src/main/javascript/peer/vml/TextBoxPeer.js",
            "/../../../src/main/javascript/peer/utils/TransformUtils.js",
            "/../../../src/main/javascript/peer/utils/EventUtils.js",
            "/../../../src/main/javascript/Font.js",
            "/../../../src/main/javascript/peer/svg/Font.js",
            "/../../../src/main/javascript/peer/vml/Font.js",
            "/../../../src/main/javascript/peer/svg/TahomaFont.js",
             "/../../../src/main/javascript/peer/svg/TimesFont.js",
            "/../../../src/main/javascript/peer/svg/ArialFont.js",
             "/../../../src/main/javascript/peer/svg/VerdanaFont.js",
            "/../../../src/main/javascript/peer/vml/TahomaFont.js",
            "/../../../src/main/javascript/peer/vml/TimesFont.js",
            "/../../../src/main/javascript/peer/vml/ArialFont.js",
            "/../../../src/main/javascript/peer/vml/VerdanaFont.js"],

    stylePath: [],
    load: function()
    {
        web2d.Loader.load(this.scriptPath, this.stylePath, "web2dLibraryLoader.js");
    }
};

web2d.JsLoader.load();
