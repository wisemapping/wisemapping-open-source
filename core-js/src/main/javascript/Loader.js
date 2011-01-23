/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

core.Loader =
{
    load: function(scriptPath, stylePath,jsFileName)
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
        if (stylePath && stylePath.length > 0)
        {
            for (var i = 0; i < stylePath.length; i++)
            {
                this.includeStyleNode(baseUrl + stylePath[i]);
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
