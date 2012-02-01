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

function buildDesigner(viewMode) {

    var container = $('mindplot');
    container.setStyles({
        height: parseInt(screen.height),
        width:  parseInt(screen.width)
    });

    var editorProperties = {zoom:0.85,saveOnLoad:true,collab:'standalone',readOnly:viewMode};
    designer = new mindplot.Designer(editorProperties, container);
    designer.setViewPort({
        height: parseInt(window.innerHeight - 70), // Footer and Header
        width:  parseInt(window.innerWidth)
    });

    if (!viewMode) {
        if ($('toolbar')) {
            var menu = new mindplot.widget.Menu(designer, 'toolbar', mapId);

            //  If a node has focus, focus can be move to another node using the keys.
            designer._cleanScreen = function() {
                menu.clear()
            };
        }
    }
    return designer;
}

Asset.javascript("../js/mindplot-min.js");
