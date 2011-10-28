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

core.LoadingDiaglog = new Class({
    Extends:MooDialog,
    initialize : function() {
        this.parent({
            closeButton:false,
            useEscKey:false
        });
        var panel = this._buildPanel();
        this.setContent(panel);
    },

    _buildPanel : function () {

        var result = new Element('div');
        var content = new Element('p', {text:"sample"});


        result.addEvent('keydown', function(event) {
            event.stopPropagation();
        });

//        waitDialog.activate(true, $("waitDialog"));
//        $(window).addEvent("error", function(event) {
//
//            // Show error dialog ...
//            waitDialog.changeContent($("errorDialog"), false);
//            return false;
//        });

        content.inject(result);
        return result;
    },

    show : function() {
        this.open();
    }

});
