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
        this._removeTip.decorate(topicId, icon);
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
        this.options.icons.each(function(el) {
            var nativeImage = el.getImage();
            if (nativeImage.getHref() == url) {
                result = el;
            }
        }, this);
        return result;
    },

    getImageIcon : function(icon) {
        var result = null;
        this.options.icons.each(function(el) {
            if (result == null && $defined(el.getModel().isIconModel) && el.getId() == icon.getId() && el.getUiId() == icon.getUiId()) {
                result = el;
            }
        }, this);
        return result;
    },

    findIconFromModel : function(iconModel) {
        var result = null;
        this.options.icons.each(function(el) {
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
        this.options.icons.each(function(icon) {
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
                this.close(0);
            }

            // Now, let move the position the icon...
            var pos = icon.getPosition();
            icon.setSize(15, 15);

            // Register events ...
            var widget = this._buildWeb2d();
            widget.addEvent('click', function() {
                var actionDispatcher = mindplot.ActionDispatcher.getInstance();
                actionDispatcher.removeIconFromTopic(topicId, icon._iconModel);
            });

            widget.addEvent('mouseover', function() {
                this.show(topicId, icon);
            }.bind(this));

            widget.addEvent('mouseout', function() {
                this.hide();
            }.bind(this));

            widget.setPosition(pos.x + 11, pos.y - 11);
            this._container.appendChild(widget);

            // Setup current element ...
            this._activeIcon = icon;
            this._widget = widget;

        } else {
            clearTimeout(this._closeTimeoutId);
        }
    },

    hide : function() {
        this.close(500);
    },

    close : function(delay) {

        // This is not ok, trying to close the same dialog twice ?
        if (this._closeTimeoutId) {
            clearTimeout(this._closeTimeoutId)
        }

        if (this._activeIcon) {
            var icon = this._activeIcon;
            var widget = this._widget;
            var close = function() {

                icon.setSize(12, 12);
                this._activeIcon = null;

                this._container.removeChild(widget);
                this._widget = null;

                this._closeTimeoutId = null;

            }.bind(this);

            if (!$defined(delay) || delay == 0) {
                close();
            }
            else {
                this._closeTimeoutId = close.delay(delay);
            }
        }
    },

    _buildWeb2d : function() {
        var result = new web2d.Group({
            width: 10,
            height:10,
            x: 0,
            y:0,
            coordSizeWidth:10,
            coordSizeHeight:10
        });

        var outerRect = new web2d.Rect(0, {
            x: 0,
            y: 0,
            width:10,
            height:10,
            stroke:'0',
            fillColor:'black'
        });
        result.appendChild(outerRect);
        outerRect.setCursor('pointer');

        var innerRect = new web2d.Rect(0, {
            x: 1,
            y: 1,
            width:8,
            height:8,
            stroke:'1 solid white',
            fillColor:'gray'
        });
        result.appendChild(innerRect);

        var line = new web2d.Line({stroke:'1 solid white'});
        line.setFrom(1, 1);
        line.setTo(9, 9);
        result.appendChild(line);

        var line2 = new web2d.Line({stroke:'1 solid white'});
        line2.setFrom(1, 9);
        line2.setTo(9, 1);
        result.appendChild(line2);

        // Some sily events ...
        result.addEvent('mouseover', function() {
            innerRect.setFill('#CC0033');
        });
        result.addEvent('mouseout', function() {
            innerRect.setFill('gray');
        });
        return result;
    },

    decorate : function(topicId, icon) {
        icon.addEvent('mouseover', function() {
            this.show(topicId, icon);
        }.bind(this));

        icon.addEvent('mouseout', function() {
            this.hide();
        }.bind(this))
    }

});