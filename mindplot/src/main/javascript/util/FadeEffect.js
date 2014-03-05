/*
 *    Copyright [2012] [wisemapping]
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

mindplot.util.FadeEffect = new Class({
//    Extends: Fx,
    initialize: function(elements, isVisible) {
        console.error("Re-impl required ....");
        this.parent({duration:3000,frames:15,transition:'linear'});
        this._isVisible = isVisible;
        this._element = elements;


        this.addEvent('complete', function() {
            this._element.each(function(elem) {
                if(elem){
                    elem.setVisibility(isVisible);
                 }
            });
        });

    },

    start: function(){
      this.parent(this._isVisible ? 0 : 1, this._isVisible ? 1 : 0);
    },

    set: function(now) {
        this._element.each(function(elem) {
           if(elem){
                elem.setOpacity(now);
           }
        });
        return this;
    }
});

