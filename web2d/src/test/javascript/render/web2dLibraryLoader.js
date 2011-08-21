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

// Modulue initializer.
web2d = {
    peer: {}
};

web2d.peer =
{
    svg: {}
};

web2d.peer.utils = {};
web2d.Loader =
{
    load: function(scriptPath, stylePath, jsFileName, callbackFn)
    {
        var headElement = document.getElementsByTagName('head');
        var htmlDoc = headElement.item(0);
        var baseUrl = this.baseUrl(jsFileName);
        this.files = scriptPath;
        this.callbackFn = callbackFn;

        if (scriptPath && scriptPath.length > 0)
        {
            for (var i = 0; i < scriptPath.length; i++)
            {
                var file = scriptPath[i];
                this.includeScriptNode(baseUrl + file);
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

        function calltheCBcmn() {
            web2d.Loader.checkLoaded(filename);
        }

        if(typeof(js.addEvent) != 'undefined') {
            /* The FF, Chrome, Safari, Opera way */
            js.addEvent('load',calltheCBcmn,false);
        }
        else {
            /* The MS IE 8+ way (may work with others - I dunno)*/
            var ret = js.onreadystatechange= function handleIeState() {
                if(js.readyState == 'loaded' || js.readyState == 'complete'){
                    calltheCBcmn();
                }
            };
        }

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
    },
    checkLoaded:function(name) {
        var index = -1;
        for(var i = 0 ; i<this.files.length; i++){
            var names = this.files[i].split('/');
            var chkname = name.split('/');
            if(names[names.length-1]==chkname[chkname.length-1]){
                index = i;
                break;
            }
        }
        if(i!=-1){
            this.files.splice(i,1);
            if(this.files.length==0){
                this.callbackFn();
            }
        }
    }
};


web2d.JsLoader =
{
    scriptPath: [
            "/render/mootools.js",
            "../../../../../core-js/target/classes/core.js",
            "/../../../src/main/javascript/EventDispatcher.js",
            "/../../../src/main/javascript/peer/svg/ElementPeer.js",
            "/../../../src/main/javascript/Element.js",
            "/../../../src/main/javascript/Workspace.js",
            "/../../../src/main/javascript/peer/svg/WorkspacePeer.js",
            "/../../../src/main/javascript/Toolkit.js",
            "/../../../src/main/javascript/Elipse.js",
            "/../../../src/main/javascript/peer/svg/ElipsePeer.js",
            "/../../../src/main/javascript/Line.js",
            "/../../../src/main/javascript/peer/svg/LinePeer.js",
            "/../../../src/main/javascript/PolyLine.js",
            "/../../../src/main/javascript/peer/svg/PolyLinePeer.js",
            "/../../../src/main/javascript/Group.js",
            "/../../../src/main/javascript/peer/svg/GroupPeer.js",
            "/../../../src/main/javascript/Rect.js",
            "/../../../src/main/javascript/peer/svg/RectPeer.js",
            "/../../../src/main/javascript/Text.js",
            "/../../../src/main/javascript/peer/svg/TextPeer.js",
            "/../../../src/main/javascript/peer/utils/TransformUtils.js",
            "/../../../src/main/javascript/peer/utils/EventUtils.js",
            "/../../../src/main/javascript/Font.js",
            "/../../../src/main/javascript/peer/svg/Font.js",
            "/../../../src/main/javascript/peer/svg/TahomaFont.js",
             "/../../../src/main/javascript/peer/svg/TimesFont.js",
            "/../../../src/main/javascript/peer/svg/ArialFont.js",
             "/../../../src/main/javascript/peer/svg/VerdanaFont.js"],

    stylePath: [],
    load: function(callbackFn)
    {
        web2d.Loader.load(this.scriptPath, this.stylePath, "web2dLibraryLoader.js", callbackFn);
    }
};

//web2d.JsLoader.load();
