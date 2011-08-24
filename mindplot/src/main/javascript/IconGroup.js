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

mindplot.IconGroup = new Class({
    initialize : function(topic) {
        var offset = topic.getOffset();

        this.options = {
            width:0,
            height:0,
            x:offset.x / 2,
            y:offset.y,
            icons:[],
            topic:topic,
            nativeElem:new web2d.Group({width: 2, height:2,x: offset, y:offset, coordSizeWidth:1,coordSizeHeight:1})
        };
        this.updateIconGroupPosition();
        this.registerListeners();

        this._removeTip = new mindplot.IconGroup.RemoveTip(this.options.nativeElem, this);

    },

    setPosition : function(x, y) {
        this.options.x = x;
        this.options.y = y;
        this.options.nativeElem.setPosition(x, y);
    },

    getPosition : function() {
        return {x:this.options.x, y:this.options.y};
    },

    setSize : function(width, height) {
        this.options.width = width;
        this.options.height = height;
        this.options.nativeElem.setSize(width, height);
        this.options.nativeElem.setCoordSize(width, height);
    },

    getSize : function() {
        return {width:this.options.width, height:this.options.height};
    },

    addIcon : function(icon) {
        $defined(icon, "icon is not defined");
        icon.setGroup(this);
        var newIcon = icon.getImage();
        var nativeElem = this.options.nativeElem;

        var iconSize = newIcon.getSize();
        var size = nativeElem.getSize();
        newIcon.setPosition(size.width, 0);
        this.options.icons.extend([icon]);

        nativeElem.appendChild(newIcon);

        size.width = size.width + iconSize.width;
        if (iconSize.height > size.height) {
            size.height = iconSize.height;
        }

        nativeElem.setCoordSize(size.width, size.height);
        nativeElem.setSize(size.width, size.height);
        this.options.width = size.width;
        this.options.height = size.height;

        // Register event for the group ..
        var topicId = this.options.topic.getId();
        newIcon.addEvent('mouseover', function(event) {
            this._removeTip.show(topicId, icon);
            event.stopPropagation();
        }.bind(this));

        newIcon.addEvent('mouseout', function(event) {
            this._removeTip.hide(newIcon);
        }.bind(this));
    },

    getIcons : function() {
        return this.options.icons;
    },

    removeIcon : function(url) {
        this._removeIcon(this.getIcon(url));
    },

    removeImageIcon : function(icon) {

        var imgIcon = this.getImageIcon(icon);
        this._removeIcon(imgIcon);
    },

    getIcon : function(url) {
        var result = null;
        this.options.icons.each(function(el, index) {
            var nativeImage = el.getImage();
            if (nativeImage.getHref() == url) {
                result = el;
            }
        }, this);
        return result;
    },

    getImageIcon : function(icon) {
        var result = null;
        this.options.icons.each(function(el, index) {
            if (result == null && $defined(el.getModel().isIconModel) && el.getId() == icon.getId() && el.getUiId() == icon.getUiId()) {
                result = el;
            }
        }, this);
        return result;
    },

    findIconFromModel : function(iconModel) {
        var result = null;
        this.options.icons.each(function(el, index) {
            var elModel = el.getModel();
            if (result == null && $defined(elModel.isIconModel) && elModel.getId() == iconModel.getId()) {
                result = el;
            }
        }, this);

        if (result == null) {
            throw "Icon can no be found.";
        }

        return result;
    },

    _removeIcon : function(icon) {
        var nativeImage = icon.getImage();
        this.options.icons.erase(icon);
        var iconSize = nativeImage.getSize();
        var size = this.options.nativeElem.getSize();
        var position = nativeImage.getPosition();
        this.options.icons.each(function(icon, index) {
            var img = icon.getImage();
            var pos = img.getPosition();
            if (pos.x > position.x) {
                img.setPosition(pos.x - iconSize.width, 0);
            }
        }.bind(this));
        size.width = size.width - iconSize.width;
        this.setSize(size.width, size.height);
    },

    getNativeElement : function() {
        return this.options.nativeElem;
    },

    moveToFront : function() {
        this.options.nativeElem.moveToFront();
    },

    registerListeners : function() {
        this.options.nativeElem.addEvent('click', function(event) {
            // Avoid node creation ...
            event.stopPropagation();

        });

        this.options.nativeElem.addEvent('dblclick', function(event) {
            event.stopPropagation();

        });
    },

    getTopic : function() {
        return this.options.topic;
    },

    updateIconGroupPosition : function() {
        var offsets = this._calculateOffsets();
        this.setPosition(offsets.x, offsets.y);
    },

    _calculateOffsets : function() {
        var offset = this.options.topic.getOffset();
        var text = this.options.topic.getTextShape();

        var sizeHeight = text.getHtmlFontSize();
        var yOffset = offset;

        var shape = this.options.topic.getShapeType();
        yOffset = text.getPosition().y + (sizeHeight - 18) / 2 + 1;
        return {x:offset, y:yOffset};
    }
});


mindplot.IconGroup.RemoveTip = new Class({
    initialize : function(container) {
        $assert(container, "group can not be null");
        this._container = container;
    },


    show : function(topicId, icon) {
        $assert(icon, 'icon can not be null');

        // Nothing to do ...
        if (this._activeIcon != icon) {
            // If there is an active icon, close it first ...
            if (this._activeIcon) {
                this.close(icon);
            }

            // Now, let move the position the icon...
            var pos = icon.getPosition();
            icon.setSize(15, 15);

            // Create a new remove widget ...
            var widget = this._buildWeb2d();
            widget.addEvent('click', function() {
                var actionDispatcher = mindplot.ActionDispatcher.getInstance();
                actionDispatcher.removeIconFromTopic(topicId, icon._iconModel);
            });

            widget.setPosition(pos.x + 11, pos.y - 11);
            this._container.appendChild(widget);

            // Setup current element ...
            this._activeIcon = icon;
            this._widget = widget;

        }
    },

    hide : function(icon) {
        this.close(icon, 1000);
    },

    close : function(icon, delay) {
        $assert(icon, 'icon can not be null');


        if (this._activeIcon) {
            var close = function() {
                icon.setSize(12, 12);
                this._container.removeChild(this._widget);
                // Clear state ...
                this._activeIcon = null;
                this._widget = null
            }.bind(this);

            !$defined(delay) ? close() : close.delay(delay);
        }
    },

    _buildWeb2d : function(icon) {
        var result = new web2d.Group({width: 10, height:10,x: 0, y:0, coordSizeWidth:10,coordSizeHeight:10});
        var rect = new web2d.Rect(0, {x: 0, y: 0, width:10, height:10, stroke:'0',fillColor:'black', visibility:true});
        result.appendChild(rect);

        var rect2 = new web2d.Rect(0, {x: 1, y: 1, width:8, height:8, stroke:'1 solid white',fillColor:'#CC0033', visibility:true});
        result.appendChild(rect2);

        var line = new web2d.Line({stroke:'1 solid white'});
        line.setFrom(1, 1);
        line.setTo(9, 9);
        result.appendChild(line);

        var line2 = new web2d.Line({stroke:'1 solid white'});
        line2.setFrom(1, 9);
        line2.setTo(9, 1);
        result.appendChild(line2);

        rect2.setCursor('pointer');


        // Some sily events ...
        result.addEvent('mouseover', function() {
            rect2.setFill('black');
        });
        result.addEvent('mouseout', function() {
            rect2.setFill('#CC0033');
        });
        return result;
    }

});