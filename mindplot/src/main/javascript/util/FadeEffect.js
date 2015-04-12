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

//FIXME: this Class should be reimplemented
mindplot.util.FadeEffect = new Class(/** @lends FadeEffect */{
    Extends: mindplot.Events,
    /**
     * @extends mindplot.Events
     * @constructs
     * @param elements
     * @param isVisible
     */
    initialize: function(elements, isVisible) {
        this._isVisible = isVisible;
        this._element = elements;
    },

    /** */
    start: function(){
        var visible = this._isVisible;
        _.each(this._element, function(elem) {
            if(elem){
                elem.setVisibility(visible);
            }
        });
        this._isVisible = !visible;
        this.fireEvent('complete');
    }
});

