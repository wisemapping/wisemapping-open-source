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

var IconPanel = new Class({
    Implements:[Options,Events],
    options:{
        width:253,
        initialWidth:0,
        height:200,
        content:null,
        panel:null,
        button:null,
        onStart:Class.empty,
        state:'close'
    },

    initialize:function(options) {
        this.setOptions(options);
        if ($defined(this.options.button)) {
            this.init();
        }
    },

    setButton:function(button) {
        this.options.button = button;
    },

    init:function() {
        var panel = new Element('div');
        var coord = this.options.button.getCoordinates();
        var top = this.options.button.getTop() + coord.height + 2;
        var left = this.options.button.getLeft();
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

        this.options.content.addEvent('click', function(event) {
            this.close();
        }.bind(this));

        panel.setStyle('opacity', 0);
        panel.inject($(document.body));
        this.registerOpenPanel();
    },

    open:function() {
        if (this.options.state == 'close') {
            if (!$defined(this.options.panel)) {
                this.init();
            }

            var panel = this.options.panel;
            panel.setStyles({border: '1px solid #636163', opacity:100});
            this.fireEvent('onStart');

            // Resize dialog to show a cool effect ;)
            panel.set('morph', {duration: 'long', transition: 'bounce:out'});
            panel.morph({
                height:[0,this.options.height],
                width:[this.options.initialWidth, this.options.width]
            });
            panel.addEvent('complete', function() {
                this.registerClosePanel();
            }.bind(this));

            this.options.state = 'open';

        }
    },

    close:function() {
        if (this.options.state == 'open') {

            // Magic, disappear effect ;)
            var panel = this.options.panel;
            panel.set('morph', {duration: 'long', transition: 'bounce:in'});
            panel.morph({
                height:[this.options.height,0],
                width:[this.options.width, this.options.initialWidth]
            });
            panel.addEvent('complete', function() {
                this.options.panel.setStyles({border: '1px solid transparent', opacity:0});
                this.registerOpenPanel();
            }.bind(this));

            this.options.state = 'close';
        }
    },

    registerOpenPanel:function() {
        this.options.button.removeEvents('click');
        this.options.button.addEvent('click', function(event) {
            this.open();
        }.bindWithEvent(this));
    },

    registerClosePanel:function() {
        this.options.button.removeEvents('click');
        this.options.button.addEvent('click', function(event) {
            this.close();
        }.bindWithEvent(this));
    }
});