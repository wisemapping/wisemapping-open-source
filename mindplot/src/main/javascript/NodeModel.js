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

mindplot.NodeModel = function(type, mindmap)
{
    core.assert(type, 'Node type can not be null');
    core.assert(mindmap, 'mindmap can not be null');

    this._order = null;
    this._type = type;
    this._children = [];
    this._icons = [];
    this._links = [];
    this._notes = [];
    this._size = {width:50,height:20};
    this._position = null;
    this._id = mindplot.NodeModel._nextUUID();
    this._mindmap = mindmap;
    this._text = null;
    this._shapeType = null;
    this._fontFamily = null;
    this._fontSize = null;
    this._fontStyle = null;
    this._fontWeight = null;
    this._fontColor = null;
    this._borderColor = null;
    this._backgroundColor = null;
    this._areChildrenShrinked = false;
};

mindplot.NodeModel.prototype.clone = function()
{
    var result = new mindplot.NodeModel(this._type, this._mindmap);
    result._order = this._order;
    result._type = this._type;
    result._children = this._children.map(function(item,index)
    {
        var model = item.clone();
        model._parent = result;
        return model;
    });


    result._icons = this._icons;
    result._links = this._links;
    result._notes = this._notes;
    result._size = this._size;
    result._position = this._position;
    result._id = this._id;
    result._mindmap = this._mindmap;
    result._text = this._text;
    result._shapeType = this._shapeType;
    result._fontFamily = this._fontFamily;
    result._fontSize = this._fontSize;
    result._fontStyle = this._fontStyle;
    result._fontWeight = this._fontWeight;
    result._fontColor = this._fontColor;
    result._borderColor = this._borderColor;
    result._backgroundColor = this._backgroundColor;
    result._areChildrenShrinked = this._areChildrenShrinked;
    return result;
};

mindplot.NodeModel.prototype.areChildrenShrinked = function()
{
    return this._areChildrenShrinked;
};

mindplot.NodeModel.prototype.setChildrenShrinked = function(value)
{
    this._areChildrenShrinked = value;
};

mindplot.NodeModel.prototype.getId = function()
{
    return this._id;
};


mindplot.NodeModel.prototype.setId = function(id)
{
    this._id = id;
};

mindplot.NodeModel.prototype.getType = function()
{
    return this._type;
};

mindplot.NodeModel.prototype.setText = function(text)
{
    this._text = text;
};

mindplot.NodeModel.prototype.getText = function()
{
    return this._text;
};

mindplot.NodeModel.prototype.isNodeModel = function()
{
    return true;
};

mindplot.NodeModel.prototype.isConnected = function()
{
    return this._parent != null;
};

mindplot.NodeModel.prototype.createLink = function(url)
{
    core.assert(url, 'Link URL must be specified.');
    return new mindplot.LinkModel(url, this);
};

mindplot.NodeModel.prototype.addLink = function(link)
{
    core.assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
    this._links.push(link);
};

mindplot.NodeModel.prototype._removeLink = function(link)
{
    core.assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
    this._links.remove(link);
};

mindplot.NodeModel.prototype.createNote = function(text)
{
    core.assert(text, 'note text must be specified.');
    return new mindplot.NoteModel(text, this);
};

mindplot.NodeModel.prototype.addNote = function(note)
{
    core.assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
    this._notes.push(note);
};

mindplot.NodeModel.prototype._removeNote = function(note)
{
    core.assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
    this._notes.remove(note);
};

mindplot.NodeModel.prototype.createIcon = function(iconType)
{
    core.assert(iconType, 'IconType must be specified.');
    return new mindplot.IconModel(iconType, this);
};

mindplot.NodeModel.prototype.addIcon = function(icon)
{
    core.assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
    this._icons.push(icon);
};

mindplot.NodeModel.prototype._removeIcon = function(icon)
{
    core.assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
    this._icons.remove(icon);
};

mindplot.NodeModel.prototype.removeLastIcon = function()
{
    this._icons.pop();
};

mindplot.NodeModel.prototype._appendChild = function(child)
{
    core.assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
    this._children.push(child);
    child._parent = this;
};

mindplot.NodeModel.prototype._removeChild = function(child)
{
    core.assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object.');
    this._children.remove(child);
    child._parent = null;
};

mindplot.NodeModel.prototype.setPosition = function(x, y)
{
    core.assert(core.Utils.isDefined(x), "x coordinate must be defined");
    core.assert(core.Utils.isDefined(y), "y coordinate must be defined");

    if (!core.Utils.isDefined(this._position))
    {
        this._position = new core.Point();
    }
    this._position.x = parseInt(x);
    this._position.y = parseInt(y);
};

mindplot.NodeModel.prototype.getPosition = function()
{
    return this._position;
};

mindplot.NodeModel.prototype.setSize = function(width, height)
{
    this._size.width = width;
    this._size.height = height;
};

mindplot.NodeModel.prototype.getSize = function()
{
    return {width:this._size.width,height:this._size.height};
};

mindplot.NodeModel.prototype.getChildren = function()
{
    return this._children;
};

mindplot.NodeModel.prototype.getIcons = function()
{
    return this._icons;
};

mindplot.NodeModel.prototype.getLinks = function()
{
    return this._links;
};

mindplot.NodeModel.prototype.getNotes = function()
{
    return this._notes;
};

mindplot.NodeModel.prototype.getParent = function()
{
    return this._parent;
};

mindplot.NodeModel.prototype.getMindmap = function()
{
    return this._mindmap;
};

mindplot.NodeModel.prototype.setParent = function(parent)
{
    core.assert(parent != this, 'The same node can not be parent and child if itself.');
    this._parent = parent;
};

mindplot.NodeModel.prototype.canBeConnected = function(sourceModel, sourcePosition, targetTopicHeight)
{
    core.assert(sourceModel != this, 'The same node can not be parent and child if itself.');
    core.assert(sourcePosition, 'childPosition can not be null.');
    core.assert(core.Utils.isDefined(targetTopicHeight), 'childrenWidth can not be null.');

    // Only can be connected if the node is in the left or rigth.
    var targetModel = this;
    var mindmap = targetModel.getMindmap();
    var targetPosition = targetModel.getPosition();
    var result = false;

    if (sourceModel.getType() == mindplot.NodeModel.MAIN_TOPIC_TYPE)
    {
        // Finally, check current node ubication.
        var targetTopicSize = targetModel.getSize();
        var yDistance = Math.abs(sourcePosition.y - targetPosition.y);

        if (yDistance <= targetTopicHeight / 2)
        {
            // Circular connection ?
            if (!sourceModel._isChildNode(this))
            {
                var toleranceDistance = (targetTopicSize.width / 2) + targetTopicHeight;

                var xDistance = sourcePosition.x - targetPosition.x;
                var isTargetAtRightFromCentral = targetPosition.x >= 0;

                if (isTargetAtRightFromCentral)
                {
                    if (xDistance >= 0 && xDistance <= mindplot.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE + (targetTopicSize.width / 2))
                    {
                        result = true;
                    }

                } else
                {
                    if (xDistance <= 0 && Math.abs(xDistance) <= mindplot.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE + (targetTopicSize.width / 2))
                    {
                        result = true;
                    }
                }
            }
        }
    } else
    {
        throw "No implemented yet";
    }
    return result;
};

mindplot.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 60;

mindplot.NodeModel.prototype._isChildNode = function(node)
{
    var result = false;
    if (node == this)
    {
        result = true;
    } else
    {
        var children = this.getChildren();
        for (var i = 0; i < children.length; i++)
        {
            var child = children[i];
            result = child._isChildNode(node);
            if (result)
            {
                break;
            }
        }
    }
    return result;

};

mindplot.NodeModel.prototype.connectTo = function(parent)
{
    var mindmap = this.getMindmap();
    mindmap.connect(parent, this);
    this._parent = parent;
};

mindplot.NodeModel.prototype.disconnect = function()
{
    var mindmap = this.getMindmap();
    mindmap.disconnect(this);
};

mindplot.NodeModel.prototype.getOrder = function()
{
    return this._order;
};

mindplot.NodeModel.prototype.getShapeType = function()
{
    return this._shapeType;
};

mindplot.NodeModel.prototype.setShapeType = function(type)
{
    this._shapeType = type;
};

mindplot.NodeModel.prototype.setOrder = function(value)
{
    this._order = value;
};

mindplot.NodeModel.prototype.setFontFamily = function(value)
{
    this._fontFamily = value;
};

mindplot.NodeModel.prototype.getOrder = function()
{
    return this._order;
};

mindplot.NodeModel.prototype.setFontFamily = function(value)
{
    this._fontFamily = value;
};

mindplot.NodeModel.prototype.getFontFamily = function()
{
    return this._fontFamily;
};

mindplot.NodeModel.prototype.setFontStyle = function(value)
{
    this._fontStyle = value;
};

mindplot.NodeModel.prototype.getFontStyle = function()
{
    return this._fontStyle;
};

mindplot.NodeModel.prototype.setFontWeight = function(value)
{
    this._fontWeight = value;
};

mindplot.NodeModel.prototype.getFontWeight = function()
{
    return this._fontWeight;
};

mindplot.NodeModel.prototype.setFontColor = function(value)
{
    this._fontColor = value;
};

mindplot.NodeModel.prototype.getFontColor = function()
{
    return this._fontColor;
};

mindplot.NodeModel.prototype.setFontSize = function(value)
{
    this._fontSize = value;
};

mindplot.NodeModel.prototype.getFontSize = function()
{
    return this._fontSize;
};

mindplot.NodeModel.prototype.getBorderColor = function()
{
    return this._borderColor;
};

mindplot.NodeModel.prototype.setBorderColor = function(color)
{
    this._borderColor = color;
};

mindplot.NodeModel.prototype.getBackgroundColor = function()
{
    return this._backgroundColor;
};

mindplot.NodeModel.prototype.setBackgroundColor = function(color)
{
    this._backgroundColor = color;
};

mindplot.NodeModel.prototype.deleteNode = function()
{
    var mindmap = this._mindmap;

    // if it has children nodes, Their must be disconnected.
    var lenght = this._children;
    for (var i = 0; i < lenght; i++)
    {
        var child = this._children[i];
        mindmap.disconnect(child);
    }

    var parent = this._parent;
    if (parent)
    {
        // if it is connected, I must remove it from the parent..
        mindmap.disconnect(this);
    }

    // It's an isolated node. It must be a hole branch ...
    var branches = mindmap.getBranches();
    branches.remove(this);

};

/**
 * @todo: This method must be implemented.
 */
mindplot.NodeModel._nextUUID = function()
{
    if (!this._uuid)
    {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
};


mindplot.NodeModel.prototype.inspect = function()
{
    return '(type:' + this.getType() + ' , id: ' + this.getId() + ')';
};

mindplot.NodeModel.CENTRAL_TOPIC_TYPE = 'CentralTopic';
mindplot.NodeModel.MAIN_TOPIC_TYPE = 'MainTopic';
mindplot.NodeModel.DRAGGED_TOPIC_TYPE = 'DraggedTopic';

mindplot.NodeModel.SHAPE_TYPE_RECT = 'rectagle';
mindplot.NodeModel.SHAPE_TYPE_ROUNDED_RECT = 'rounded rectagle';
mindplot.NodeModel.SHAPE_TYPE_ELIPSE = 'elipse';
mindplot.NodeModel.SHAPE_TYPE_LINE = 'line';


