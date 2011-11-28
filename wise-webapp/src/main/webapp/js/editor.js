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
Asset.javascript('../js/mindplot-min.js', {
    id: 'MindplotSVGLib',
    onLoad: function() {
        $(document).fireEvent('loadcomplete', 'mind')
    }
});

var designer = null;
/* JavaScript tabs changer */

function setUpToolbar(designer, readOnly) {

    var menu = new mindplot.widget.Menu(designer, 'toolbar', mapId);

    // Autosave ...
    if (!readOnly) {
        (function() {

            if (designer.needsSave()) {
                designer.save(function() {
                    var monitor = core.ToolbarNotifier.getInstance();
                }, false);
            }
        }).periodical(30000);

        // To prevent the user from leaving the page with changes ...
        window.onbeforeunload = function() {
            if (designer.needsSave()) {
                designer.save(null, false)
            }
        }
    }

    //  If a node has focus, focus can be move to another node using the keys.
    designer._cleanScreen = function() {
        menu.clear()
    };


}

function buildDesigner(properties) {
    $assert(properties, "properties can not be null");

    var container = $('mindplot');
    container.setStyles({
        height: parseInt(screen.height),
        width:  parseInt(screen.width)
    });

    designer = new mindplot.Designer(properties, container);
    designer.setViewPort({
        height: parseInt(window.innerHeight - 151), // Footer and Header
        width:  parseInt(window.innerWidth)
    });

    // Toolbar is only loaded if it was defined ...
    if ($('toolbar')) {
        setUpToolbar(designer, properties.readOnly);
    }
    return designer;
}
