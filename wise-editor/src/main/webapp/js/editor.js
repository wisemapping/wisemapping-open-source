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

function buildDesigner(options) {

    var container = $('mindplot');
    designer = new mindplot.Designer(options, container);

    // Configure default persistence manager ...
    var persistence;
    if (options.persistenceManager) {
        persistence = eval("new " + options.persistenceManager + "()");

    } else {
        persistence = new mindplot.LocalStorageManager();
    }
    mindplot.PersistenceManager.init(persistence);


    if (!options.readOnly) {
        if ($('toolbar')) {
            var menu = new mindplot.widget.Menu(designer, 'toolbar');

            //  If a node has focus, focus can be move to another node using the keys.
            designer._cleanScreen = function() {
                menu.clear()
            };
        }
    }
    return designer;
}


function loadDesignerOptions() {
    // Load map options ...
    var uri = new URI(window.location);
    var query = String.parseQueryString(uri.get('query'));
    var jsonConf = query.confUrl;
    var result;
    if (jsonConf) {

        var request = new Request.JSON({
                url: jsonConf,
                async:false,
                onSuccess:
                    function(options) {
                        this.options = options;

                    }.bind(this)
            }
        );
        request.get();
        result = this.options;
    }
    else {
        // Set workspace screen size as default. In this way, resize issues are solved.
        var containerSize = {
            height: parseInt(screen.height),
            width:  parseInt(screen.width)
        };

        var viewPort = {
            height: parseInt(window.innerHeight - 70), // Footer and Header
            width:  parseInt(window.innerWidth)
        };
        result = {readOnly:false,zoom:0.85,saveOnLoad:true,size:containerSize,viewPort:viewPort};
    }
    return result;
}


Asset.javascript("../js/mindplot-min.js");
