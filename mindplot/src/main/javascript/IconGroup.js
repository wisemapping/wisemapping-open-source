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

mindplot.IconGroup = function(topic) {
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
};

mindplot.IconGroup.prototype.setPosition = function(x, y) {
    this.options.x = x;
    this.options.y = y;
    this.options.nativeElem.setPosition(x, y);
};

mindplot.IconGroup.prototype.getPosition = function() {
    return {x:this.options.x, y:this.options.y};
};

mindplot.IconGroup.prototype.setSize = function(width, height) {
    this.options.width = width;
    this.options.height = height;
    this.options.nativeElem.setSize(width, height);
    this.options.nativeElem.setCoordSize(width, height);
};

mindplot.IconGroup.prototype.getSize = function()
{
    return {width:this.options.width, height:this.options.height};
};

mindplot.IconGroup.prototype.addIcon = function(icon) {
    icon.setGroup(this);
    var newIcon = icon.getImage();
    var nativeElem = this.options.nativeElem;
    var iconSize = newIcon.getSize();
    var size = nativeElem.getSize();
    newIcon.setPosition(size.width, 0);
    this.options.icons.extend([icon]);

    nativeElem.appendChild(newIcon);

    size.width = size.width + iconSize.width;
    if (iconSize.height > size.height)
    {
        size.height = iconSize.height;
    }

    nativeElem.setCoordSize(size.width, size.height);
    nativeElem.setSize(size.width, size.height);
    this.options.width = size.width;
    this.options.height = size.height;
};
mindplot.IconGroup.prototype.getIcons = function() {
    return this.options.icons;
};
mindplot.IconGroup.prototype.removeIcon = function(url) {
    this._removeIcon(this.getIcon(url));
};

mindplot.IconGroup.prototype.removeImageIcon = function(icon) {

    var imgIcon = this.getImageIcon(icon);
    this._removeIcon(imgIcon);
};

mindplot.IconGroup.prototype.getIcon = function(url) {
    var result = null;
    this.options.icons.each(function(el, index) {
        var nativeImage = el.getImage();
        if (nativeImage.getHref() == url)
        {
            result = el;
        }
    }, this);
    return result;
};

mindplot.IconGroup.prototype.getImageIcon=function(icon){
    var result = null;
    this.options.icons.each(function(el,index){
        if(result == null && $chk(el.getModel().isIconModel) && el.getId()==icon.getId() && el.getUiId() == icon.getUiId())
        {
            result = el;
        }
    },this);
    return result;
};

mindplot.IconGroup.prototype.findIconFromModel=function(iconModel){
    var result = null;
    this.options.icons.each(function(el,index){
        var elModel = el.getModel();
        if(result == null && $chk(elModel.isIconModel) && elModel.getId()==iconModel.getId())
        {
            result = el;
        }
    },this);

    if(result==null)
    {
        throw "Icon can no be found.";
    }

    return result;
};


mindplot.IconGroup.prototype._removeIcon = function(icon) {
    var nativeImage = icon.getImage();
    this.options.icons.remove(icon);
    var iconSize = nativeImage.getSize();
    var size = this.options.nativeElem.getSize();
    var position = nativeImage.getPosition();
    var childs = this.options.nativeElem.removeChild(nativeImage);
    this.options.icons.each(function(icon,index){
        var img = icon.getImage();
        var pos = img.getPosition();
        if(pos.x > position.x){
            img.setPosition(pos.x-iconSize.width, 0);
        }
    }.bind(this));
    size.width = size.width - iconSize.width;
    this.setSize(size.width, size.height);
};
mindplot.IconGroup.prototype.getNativeElement = function() {
    return this.options.nativeElem;
};
mindplot.IconGroup.prototype.moveToFront = function() {
    this.options.nativeElem.moveToFront();
}
mindplot.IconGroup.prototype.registerListeners = function() {
    this.options.nativeElem.addEventListener('click', function(event) {
        // Avoid node creation ...
        if (event.stopPropagation)
        {
            event.stopPropagation(true);
        } else
        {
            event.cancelBubble = true;
        }

    });
    this.options.nativeElem.addEventListener('dblclick', function(event)
    {
        // Avoid node creation ...
        if (event.stopPropagation)
        {
            event.stopPropagation(true);
        } else
        {
            event.cancelBubble = true;
        }

    });
};
mindplot.IconGroup.prototype.getTopic = function() {
    return this.options.topic;
};

mindplot.IconGroup.prototype.updateIconGroupPosition = function() {
    var offsets = this._calculateOffsets();
    this.setPosition(offsets.x, offsets.y);
};

mindplot.IconGroup.prototype._calculateOffsets = function() {
    var offset = this.options.topic.getOffset();
    var text = this.options.topic.getTextShape();
    var sizeHeight = text.getHtmlFontSize();
    var yOffset = offset;
    var shape = this.options.topic.getShapeType();
    yOffset = text.getPosition().y + (sizeHeight - 18)/2;
    return {x:offset, y:yOffset};
};