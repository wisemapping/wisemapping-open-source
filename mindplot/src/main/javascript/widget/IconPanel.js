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

mindplot.widget.IconPanel = new Class({
    Extends:mindplot.widget.ToolbarItem,
    Implements:[Options],
    options:{
        width:253,
        initialWidth:0,
        height:200,
        panel:null,
        onStart:Class.empty,
        state:'close'
    },

    initialize:function(buttonId, model) {
        this.parent(buttonId, model);
        this.options.content = this._build();
        this.init();
    },

    init:function() {
        var panel = new Element('div');
        var buttonElem = this.getButtonElem();

        var coord = buttonElem.getCoordinates();
        var top = buttonElem.getTop() + coord.height + 2;
        var left = buttonElem.getLeft();

        panel.setStyles({
                width:this.options.initialWidth,
                height:0,position:'absolute',
                top:top,
                left:left,
                background:'#e5e5e5',
                border:'1px solid #BBB4D6',
                zIndex:20,
                overflow:'hidden'}
        );

        this.options.panel = panel;
        this.options.content.inject(panel);

        this.options.content.addEvent('click', function() {
            this.hide();
        }.bind(this));

        panel.setStyle('opacity', 0);
        panel.inject($(document.body));
        this.registerOpenPanel();
    },

    show:function() {
        this.parent();
        if (this.options.state == 'close') {
            if (!$defined(this.options.panel)) {
                this.init();
            }

            var panel = this.options.panel;
            panel.setStyles({
                border: '1px solid #636163',
                opacity:100,
                height:this.options.height,
                width:this.options.width
            });
            this.fireEvent('onStart');
            this.registerClosePanel();
            this.options.state = 'open';
        }
    },

    hide:function() {
        this.parent();
        if (this.options.state == 'open') {
            // Magic, disappear effect ;)
            this.options.panel.setStyles({border: '1px solid transparent', opacity:0});
            this.registerOpenPanel();
            this.options.state = 'close';
        }
    },

    registerOpenPanel:function() {
        this.getButtonElem().removeEvents('click');
        this.getButtonElem().addEvent('click', function() {
            this.show();
        }.bind(this));
    },

    registerClosePanel:function() {
        this.getButtonElem().removeEvents('click');
        this.getButtonElem().addEvent('click', function() {
            this.hide();
        }.bind(this));
    } ,

    _build : function() {
        var content = new Element('div').setStyles({width:253,height:200,padding:5});
        var count = 0;
        for (var i = 0; i < mindplot.ImageIcon.prototype.ICON_FAMILIES.length; i = i + 1) {
            var familyIcons = mindplot.ImageIcon.prototype.ICON_FAMILIES[i].icons;
            for (var j = 0; j < familyIcons.length; j = j + 1) {
                // Separate icons by line ...
                var familyContent;
                if ((count % 12) == 0) {
                    familyContent = new Element('div').inject(content);
                }

                var iconId = familyIcons[j];
                var img = new Element('img').setStyles({width:16,height:16,padding:"0px 2px"}).inject(familyContent);
                img.id = iconId;
                img.src = mindplot.ImageIcon.prototype._getImageUrl(iconId);

                img.addEvent('click', function() {
                    this.getModel().setValue(img.id);
                }.bind(this));

                count = count + 1;
            }
        }
        return content;
    }

});