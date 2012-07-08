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
    initialize : function(topicId, iconSize) {
        $assert($defined(topicId), "topicId can not be null");
        $assert($defined(iconSize), "iconSize can not be null");

        this._icons = [];
        this._group = new web2d.Group({width: 0, height:iconSize,x: 0, y:0, coordSizeWidth:0,coordSizeHeight:100});
        this._removeTip = new mindplot.IconGroup.RemoveTip(this._group, topicId);
        this.seIconSize(iconSize, iconSize);

        this._registerListeners();

    },

    setPosition : function(x, y) {
        this._group.setPosition(x, y);
    },

    getPosition : function() {
        return this._group.getPosition();
    },

    getNativeElement : function() {
        return this._group;
    },

    getSize : function() {
        return this._group.getSize();
    },

    seIconSize : function(width, height) {
        this._iconSize = {width:width,height:height};
        this._resize(this._icons.length);
    },

    addIcon : function(icon, remove) {
        $defined(icon, "icon is not defined");

        icon.setGroup(this);
        this._icons.push(icon);

        // Adjust group and position ...
        this._resize(this._icons.length);
        this._positionIcon(icon, this._icons.length - 1);

        var imageShape = icon.getImage();
        this._group.appendChild(imageShape);

        // Register event for the group ..
        if (remove) {
            this._removeTip.decorate(this._topicId, icon);
        }
    },

    _findIconFromModel : function(iconModel) {
        var result = null;
        this._icons.each(function(icon) {
            var elModel = icon.getModel();
            if (elModel.getId() == iconModel.getId()) {
                result = icon;
            }
        }, this);

        if (result == null) {
            throw "Icon can no be found.";
        }

        return result;
    },

    removeIconByModel : function(featureModel) {
        $assert(featureModel, "featureModel can not be null");

        var icon = this._findIconFromModel(featureModel);
        this._removeIcon(icon);
    },

    _removeIcon : function(icon) {
        $assert(icon, "icon can not be null");

        this._removeTip.close(0);
        this._group.removeChild(icon.getImage());

        this._icons.erase(icon);
        this._resize(this._icons.length);

        // Add all again ...
        this._icons.each(function(elem, i) {
            this._positionIcon(elem, i);
        }.bind(this));
    },

    moveToFront : function() {
        this._group.moveToFront();
    },

    _registerListeners : function() {
        this._group.addEvent('click', function(event) {
            // Avoid node creation ...
            event.stopPropagation();

        });

        this._group.addEvent('dblclick', function(event) {
            event.stopPropagation();

        });
    },

    _resize : function(iconsLength) {
        this._group.setSize(iconsLength * this._iconSize.width, this._iconSize.height);

        var iconSize = mindplot.Icon.SIZE + (mindplot.IconGroup.ICON_PADDING * 2);
        this._group.setCoordSize(iconsLength * iconSize, iconSize);
    },

    _positionIcon : function(icon, order) {

        var iconSize = mindplot.Icon.SIZE + (mindplot.IconGroup.ICON_PADDING * 2);
        icon.getImage().setPosition(iconSize * order + mindplot.IconGroup.ICON_PADDING, mindplot.IconGroup.ICON_PADDING);
    }
});
mindplot.IconGroup.ICON_PADDING = 5;

mindplot.IconGroup.RemoveTip = new Class({
    initialize : function(container) {
        $assert(container, "group can not be null");
        this._fadeElem = container;
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

            // Register events ...
            var widget = this._buildWeb2d();
            widget.addEvent('click', function() {
                icon.remove();
            });

            widget.addEvent('mouseover', function() {
                this.show(topicId, icon);
            }.bind(this));

            widget.addEvent('mouseout', function() {
                this.hide();
            }.bind(this));

            widget.setPosition(pos.x + 80, pos.y - 50);
            this._fadeElem.appendChild(widget);

            // Setup current element ...
            this._activeIcon = icon;
            this._widget = widget;

        } else {
            clearTimeout(this._closeTimeoutId);
        }
    },

    hide : function() {
        this.close(200);
    },

    close : function(delay) {

        // This is not ok, trying to close the same dialog twice ?
        if (this._closeTimeoutId) {
            clearTimeout(this._closeTimeoutId)
        }

        if (this._activeIcon) {
            var widget = this._widget;
            var close = function() {

                this._activeIcon = null;
                this._fadeElem.removeChild(widget);
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

        // Some events ...
        result.addEvent('mouseover', function() {
            innerRect.setFill('#CC0033');
        });
        result.addEvent('mouseout', function() {
            innerRect.setFill('gray');
        });

        result.setSize(50, 50);
        return result;
    },

    decorate : function(topicId, icon) {

        if (!icon.__remove) {
            icon.addEvent('mouseover', function() {
                this.show(topicId, icon);
            }.bind(this));

            icon.addEvent('mouseout', function() {
                this.hide();
            }.bind(this));
            icon.__remove = true;
        }
    }

});