/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

mindplot.Mindmap = function()
{
    this._branches = [];
    this._name = null;
    this._description = null;
    this._version=null;
    this._relationships=[];
};

mindplot.Mindmap.prototype.getCentralTopic = function()
{
    return this._branches[0];
};

mindplot.Mindmap.prototype.getDescription = function()
{
    return this._description;
};

mindplot.Mindmap.prototype.getId = function()
{
    return this._iconType;
};


mindplot.Mindmap.prototype.setId = function(id)
{
    this._iconType = id;
};


mindplot.Mindmap.prototype.getVersion = function()
{
    return this._version;
};


mindplot.Mindmap.prototype.setVersion = function(version)
{
    this._version = version;
};



mindplot.Mindmap.prototype.addBranch = function(nodeModel)
{
    core.assert(nodeModel && nodeModel.isNodeModel(), 'Add node must be invoked with model objects');
    if (this._branches.length == 0)
    {
        core.assert(nodeModel.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE, "First element must be the central topic");
        nodeModel.setPosition(0, 0);
    } else
    {
        core.assert(nodeModel.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE, "Mindmaps only have one cental topic");
    }

    this._branches.push(nodeModel);
};

mindplot.Mindmap.prototype.getBranches = function()
{
    return this._branches;
};

mindplot.Mindmap.prototype.getRelationships = function() {
    return this._relationships;
};

mindplot.Mindmap.prototype.connect = function(parent, child)
{
    // Child already has a parent ?
    var branches = this.getBranches();
    core.assert(!child.getParent(), 'Child model seems to be already connected');

    //  Connect node...
    parent._appendChild(child);

    // Remove from the branch ...
    branches.remove(child);
};

mindplot.Mindmap.prototype.disconnect = function(child)
{
    var parent = child.getParent();
    core.assert(child, 'Child can not be null.');
    core.assert(parent, 'Child model seems to be already connected');

    parent._removeChild(child);

    var branches = this.getBranches();
    branches.push(child);

};

mindplot.Mindmap.prototype.hasAlreadyAdded = function(node)
{
    var result = false;

    // Check in not connected nodes.
    var branches = this._branches;
    for (var i = 0; i < branches.length; i++)
    {
        result = branches[i]._isChildNode(node);
        if (result)
        {
            break;
        }
    }
};

mindplot.Mindmap.prototype.createNode = function(type)
{
    core.assert(type, "node type can not be null");
    return this._createNode(type);
};

mindplot.Mindmap.prototype._createNode = function(type)
{
    core.assert(type, 'Node type must be specified.');
    var result = new mindplot.NodeModel(type, this);
    return result;
};

mindplot.Mindmap.prototype.createRelationship = function(fromNode, toNode){
    core.assert(fromNode, 'from node cannot be null');
    core.assert(toNode, 'to node cannot be null');

    return new mindplot.RelationshipModel(fromNode, toNode);
};

mindplot.Mindmap.prototype.addRelationship = function(relationship) {
    this._relationships.push(relationship);
};

mindplot.Mindmap.prototype.removeRelationship = function(relationship) {
    this._relationships.remove(relationship);
};

mindplot.Mindmap.prototype.inspect = function()
{
    var result = '';
    result = '{ ';

    var branches = this.getBranches();
    for (var i = 0; i < branches.length; i++)
    {
        var node = branches[i];
        if (i != 0)
        {
            result = result + ', ';
        }

        result = result + this._toString(node);
    }

    result = result + ' } ';

    return result;
};

mindplot.Mindmap.prototype._toString = function(node)
{
    var result = node.inspect();
    var children = node.getChildren();

    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (i == 0)
        {
            result = result + '-> {';
        } else
        {
            result = result + ', ';
        }

        result = result + this._toString(child);

        if (i == children.length - 1)
        {
            result = result + '}';
        }
    }

    return result;
};