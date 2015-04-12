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

mindplot.widget.KeyboardShortcutTooltip = new Class({
    Extends: mindplot.widget.FloatingTip,

    initialize : function(buttonElem, text) {
        $assert(buttonElem, "buttonElem can not be null");
        $assert(text, "text can not be null");
        this._text = text;

        var children = buttonElem.children().first();
        var tipElemId = buttonElem.attr('id') + "Tip";
        var tipDiv = $('<div></div>').attr('id', tipElemId);
        tipDiv.append(children);
        buttonElem.append(tipDiv);

        this.parent(tipDiv, {
            //Content can also be a function of the target element!
            content: this._buildContent(),
            html: true,
            placement: 'bottom',
            className: 'keyboardShortcutTip',
            template: '<div class="popover popoverBlack" role="tooltip"><div class="arrow arrowBlack"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
        });

        tipDiv.on('click', function(e) {
            tipDiv.trigger('mouseleave', e);
        });
    },

    _buildContent : function() {
        var result = $('<div></div>');
        result.css({
            padding:'3px 0px',
            width:'100%',
            color: "white"
        });

        var textContainer = $('<div></div>').text(this._text);
        textContainer.css({
            width: '100%',
            'font-size': "90%",
            textAlign: 'center',
            'font-weight':'bold'
        });

        result.append(textContainer);
        return result;
    }
});