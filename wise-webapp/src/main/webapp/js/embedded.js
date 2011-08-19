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

Asset.javascript('../js/mindplot.svg.js', {
    id: 'MindplotSVGLib',
    onLoad: function() {
        afterMindpotLibraryLoading();
    }
});

function afterMindpotLibraryLoading() {
    buildMindmapDesigner();

    $('zoomIn').addEvent('click', function(event) {
        designer.zoomIn();
    });

    $('zoomOut').addEvent('click', function(event) {
        designer.zoomOut();
    });


    // If not problem has occured, I close the dialod ...
    var closeDialog = function() {

        if (!window.hasUnexpectedErrors) {
            waitDialog.deactivate();
        }
    }.delay(500);

}

function setCurrentColorPicker(colorPicker) {
    this.currentColorPicker = colorPicker;
}


function buildMindmapDesigner() {

    var container = $('mindplot');

    // Initialize Editor ...

    var screenWidth = window.getWidth();
    var screenHeight = window.getHeight();

    // body margin ...
    editorProperties.width = screenWidth;
    editorProperties.height = screenHeight;
    editorProperties.readOnly = true;

    designer = new mindplot.MindmapDesigner(editorProperties, container);
    designer.loadFromXML(mapId, mapXml);

    // If a node has focus, focus can be move to another node using the keys.
    designer._cleanScreen = function() {
    };
}