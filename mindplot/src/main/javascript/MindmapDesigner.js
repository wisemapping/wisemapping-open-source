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

mindplot.MindmapDesigner = function(profile, divElement)
{
    core.assert(core.Utils.isDefined(profile.zoom), "zoom must be defined");

    // Undo manager ...
    this._actionRunner = new mindplot.DesignerActionRunner(this);
    mindplot.DesignerActionRunner.setInstance(this._actionRunner);

    // Initial Zoom
    this._zoom = profile.zoom;
    this._viewMode = profile.viewMode;

    // Init Screen manager..
    var screenManager = new mindplot.ScreenManager(profile.width, profile.height, divElement);

    var workspace = new mindplot.Workspace(profile, screenManager, this._zoom);
    this._workspace = workspace;

    //create editor
    var editorClass = mindplot.TextEditorFactory.getTextEditorFromName(mindplot.EditorOptions.textEditor);
    this._editor = new editorClass(this, this._actionRunner);


    // Init layout managers ...
    this._topics = [];
//    var layoutManagerClass = mindplot.layoutManagers.LayoutManagerFactory.getManagerByName(mindplot.EditorOptions.LayoutManager);
//    this._layoutManager = new layoutManagerClass(this);
     this._layoutManager = new mindplot.layoutManagers.OriginalLayoutManager(this);

    // Register handlers..
    this._registerEvents();

    this._relationships={};

    this._events = {};
};

mindplot.MindmapDesigner.prototype._getTopics = function()
{
    return this._topics;
};

mindplot.MindmapDesigner.prototype.getCentralTopic = function()
{
    var topics = this._getTopics();
    return topics[0];
};


mindplot.MindmapDesigner.prototype.addEventListener = function(eventType, listener)
{

    this._events[eventType] = listener;

}

mindplot.MindmapDesigner.prototype._fireEvent = function(eventType, event)
{
    var listener = this._events[eventType];
    if (listener != null)
    {
        listener(event);
    }
}

mindplot.MindmapDesigner.prototype._registerEvents = function()
{
    var mindmapDesigner = this;
    var workspace = this._workspace;
    var screenManager = workspace.getScreenManager();

    if (!core.Utils.isDefined(this._viewMode) || (core.Utils.isDefined(this._viewMode) && !this._viewMode))
    {
        // Initialize workspace event listeners.
        // Create nodes on double click...
        screenManager.addEventListener('click', function(event)
        {
            if(workspace.isWorkspaceEventsEnabled()){
                var t = mindmapDesigner.getEditor().isVisible();
                mindmapDesigner.getEditor().lostFocus();
                // @todo: Puaj hack...
                mindmapDesigner._cleanScreen();
            }
        });

        screenManager.addEventListener('dblclick', function(event)
        {
            if(workspace.isWorkspaceEventsEnabled()){
                mindmapDesigner.getEditor().lostFocus();
                // Get mouse position
                var pos = screenManager.getWorkspaceMousePosition(event);

                // Create a new topic model ...
                var mindmap = mindmapDesigner.getMindmap();
                var model = mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);
                model.setPosition(pos.x, pos.y);

                // Get central topic ...
                var centralTopic = mindmapDesigner.getCentralTopic();
                var centralTopicId = centralTopic.getId();

                // Execute action ...
                var command = new mindplot.commands.AddTopicCommand(model, centralTopicId, true);
                this._actionRunner.execute(command);
            }
        }.bind(this));
    }
    ;
};

mindplot.MindmapDesigner.prototype._buildNodeGraph = function(model)
{
    var workspace = this._workspace;
    var elem = this;

    // Create node graph ...
    var topic = mindplot.NodeGraph.create(model);

    this._layoutManager.addHelpers(topic);

    // Append it to the workspace ...
    var topics = this._topics;
    topics.push(topic);

    // Add Topic events ...
    this._layoutManager.registerListenersOnNode(topic);

    // Connect Topic ...
    var isConnected = model.isConnected();
    if (isConnected)
    {
        // Improve this ...
        var targetTopicModel = model.getParent();
        var targetTopicId = targetTopicModel.getId();
        var targetTopic = null;

        for (var i = 0; i < topics.length; i++)
        {
            var t = topics[i];
            if (t.getModel() == targetTopicModel)
            {
                targetTopic = t;
                // Disconnect the node. It will be connected again later ...
                model.disconnect();
                break;
            }
        }
        core.assert(targetTopic, "Could not find a topic to connect");
        topic.connectTo(targetTopic, workspace);
    }

    return  topic;
};

mindplot.MindmapDesigner.prototype.onObjectFocusEvent = function(currentObject, event)
{
    this.getEditor().lostFocus();
    var selectableObjects = this.getSelectedObjects();
    // Disable all nodes on focus but not the current if Ctrl key isn't being pressed
    if (!core.Utils.isDefined(event) || event.ctrlKey == false)
    {
        for (var i = 0; i < selectableObjects.length; i++)
        {
            var selectableObject = selectableObjects[i];
            if (selectableObject.isOnFocus() && selectableObject != currentObject)
            {
                selectableObject.setOnFocus(false);
            }
        }
    }
};

mindplot.MindmapDesigner.prototype.zoomOut = function()
{
    var scale = this._zoom * 1.2;
    if (scale <= 4)
    {
        this._zoom = scale;
        this._workspace.setZoom(this._zoom);
    }
    else
    {
        core.Monitor.getInstance().logMessage('Sorry, no more zoom can be applied. \n Why do you need more?');
    }

};

mindplot.MindmapDesigner.prototype.zoomIn = function()
{
    var scale = this._zoom / 1.2;
    if (scale >= 0.3)
    {
        this._zoom = scale;
        this._workspace.setZoom(this._zoom);
    }
    else
    {
        core.Monitor.getInstance().logMessage('Sorry, no more zoom can be applied. \n Why do you need more?');
    }
};

mindplot.MindmapDesigner.prototype.createChildForSelectedNode = function()
{

    var nodes = this._getSelectedNodes();
    if (nodes.length <= 0)
    {
        // If there are more than one node selected,
        core.Monitor.getInstance().logMessage('Could not create a topic. Only one node must be selected.');
        return;

    }
    if (nodes.length > 1)
    {

        // If there are more than one node selected,
        core.Monitor.getInstance().logMessage('Could not create a topic. One topic must be selected.');
        return;
    }

    // Add new node ...
    var centalTopic = nodes[0];
    var parentTopicId = centalTopic.getId();
    var childModel = centalTopic.createChildModel(this._layoutManager.needsPrepositioning());

    var command = new mindplot.commands.AddTopicCommand(childModel, parentTopicId, true);
    this._actionRunner.execute(command);
};

mindplot.MindmapDesigner.prototype.createSiblingForSelectedNode = function()
{
    var nodes = this._getSelectedNodes();
    if (nodes.length <= 0)
    {
        // If there are more than one node selected,
        core.Monitor.getInstance().logMessage('Could not create a topic. Only one node must be selected.');
        return;

    }
    if (nodes.length > 1)
    {
        // If there are more than one node selected,
        core.Monitor.getInstance().logMessage('Could not create a topic. One topic must be selected.');
        return;
    }

    var topic = nodes[0];
    if (topic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        // Central topic doesn't have siblings ...
        this.createChildForSelectedNode();

    } else
    {
        var parentTopic = topic.getOutgoingConnectedTopic();
        var siblingModel = topic.createSiblingModel(this._layoutManager.needsPrepositioning());
        var parentTopicId = parentTopic.getId();
        var command = new mindplot.commands.AddTopicCommand(siblingModel, parentTopicId, true);

        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.addRelationShip2SelectedNode = function(event)
{
    var screen = this._workspace.getScreenManager();
    var pos = screen.getWorkspaceMousePosition(event);
    var selectedTopics = this.getSelectedNodes();
    if(selectedTopics.length >0 &&
       (!core.Utils.isDefined(this._creatingRelationship) || (core.Utils.isDefined(this._creatingRelationship) && !this._creatingRelationship))){
        this._workspace.enableWorkspaceEvents(false);
        var fromNodePosition = selectedTopics[0].getPosition();
        this._relationship = new web2d.CurvedLine();
        this._relationship.setStyle(web2d.CurvedLine.SIMPLE_LINE);
        this._relationship.setDashed(2,2);
        this._relationship.setFrom(fromNodePosition.x, fromNodePosition.y);
        this._relationship.setTo(pos.x, pos.y);
        this._workspace.appendChild(this._relationship);
        this._creatingRelationship=true;
        this._relationshipMouseMoveFunction = this._relationshipMouseMove.bindWithEvent(this);
        this._relationshipMouseClickFunction = this._relationshipMouseClick.bindWithEvent(this, selectedTopics[0]);
        this._workspace.getScreenManager().addEventListener('mousemove',this._relationshipMouseMoveFunction);
        this._workspace.getScreenManager().addEventListener('click',this._relationshipMouseClickFunction);
    }
};

mindplot.MindmapDesigner.prototype._relationshipMouseMove = function(event){
    var screen = this._workspace.getScreenManager();
    var pos = screen.getWorkspaceMousePosition(event);
    this._relationship.setTo(pos.x-1, pos.y-1); //to prevent click event target to be the line itself
    event.preventDefault();
    event.stop();
    return false;
};

mindplot.MindmapDesigner.prototype._relationshipMouseClick = function (event, fromNode) {
    var target = event.target;
    while(target.tagName != "g" && core.Utils.isDefined(target.parentNode)){
        target=target.parentNode;
    }
    if(core.Utils.isDefined(target.virtualRef)){
        var targetNode = target.virtualRef;
        this.addRelationship(fromNode, targetNode);
    }
    this._workspace.removeChild(this._relationship);
    this._relationship = null;
    this._workspace.getScreenManager().removeEventListener('mousemove',this._relationshipMouseMoveFunction);
    this._workspace.getScreenManager().removeEventListener('click',this._relationshipMouseClickFunction);
    this._creatingRelationship=false;
    this._workspace.enableWorkspaceEvents(true);
    event.preventDefault();
    event.stop();
    return false;
};

mindplot.MindmapDesigner.prototype.addRelationship= function(fromNode, toNode){
    // Create a new topic model ...
    var mindmap = this.getMindmap();
    var model = mindmap.createRelationship(fromNode.getModel().getId(), toNode.getModel().getId());

    var command = new mindplot.commands.AddRelationshipCommand(model, mindmap);
    this._actionRunner.execute(command);
};

mindplot.MindmapDesigner.prototype.needsSave = function()
{
    return this._actionRunner.hasBeenChanged();
}

mindplot.MindmapDesigner.prototype.autoSaveEnabled = function(value)
{
    if (core.Utils.isDefined(value) && value)
    {
        var autosave = function() {

            if (this.needsSave())
            {
                this.save(null, false);
            }
        };
        autosave.bind(this).periodical(30000);
    }
}

mindplot.MindmapDesigner.prototype.save = function(onSavedHandler, saveHistory)
{
    var persistantManager = mindplot.PersistanceManager;
    var mindmap = this._mindmap;

    var properties = {zoom:this._zoom, layoutManager:this._layoutManager.getClassName()};
    persistantManager.save(mindmap, properties, onSavedHandler, saveHistory);
    this._fireEvent("save", {type:saveHistory});

    // Refresh undo state...
    this._actionRunner.markAsChangeBase();
};

mindplot.MindmapDesigner.prototype.loadFromXML = function(mapId, xmlContent)
{
    core.assert(xmlContent, 'mindmapId can not be null');
    core.assert(xmlContent, 'xmlContent can not be null');

    // Explorer Hack with local files ...
    var domDocument = core.Utils.createDocumentFromText(xmlContent);

    var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
    var mindmap = serializer.loadFromDom(domDocument);

    this._loadMap(mapId, mindmap);

    // Place the focus on the Central Topic
    var centralTopic = this.getCentralTopic();
    this._goToNode.attempt(centralTopic, this);

    this._fireEvent("loadsuccess");

};

mindplot.MindmapDesigner.prototype.load = function(mapId)
{
    core.assert(mapId, 'mapName can not be null');

    // Build load function ...
    var persistantManager = mindplot.PersistanceManager;

    // Loading mindmap ...
    var mindmap = persistantManager.load(mapId);

    // Finally, load the map in the editor ...
    this._loadMap(mapId, mindmap);

    // Place the focus on the Central Topic
    var centralTopic = this.getCentralTopic();
    this._goToNode.attempt(centralTopic, this);

    this._fireEvent("loadsuccess");
};

mindplot.MindmapDesigner.prototype._loadMap = function(mapId, mindmapModel)
{
    var designer = this;
    if (mindmapModel != null)
    {
        mindmapModel.setId(mapId);
        designer._mindmap = mindmapModel;

        // Building node graph ...
        var branches = mindmapModel.getBranches();
        for (var i = 0; i < branches.length; i++)
        {
            // NodeModel -> NodeGraph ...
            var nodeModel = branches[i];
            var nodeGraph = this._nodeModelToNodeGraph(nodeModel);

            // Update shrink render state...
            nodeGraph.setBranchVisibility(true);
        }
        var relationships = mindmapModel.getRelationships();
        for (var j=0; j<relationships.length; j++) {
            var relationship = this._relationshipModelToRelationship(relationships[j]);            
        }
    }
    core.Executor.instance.setLoading(false);
    this._getTopics().forEach(function(topic){
        delete topic.getModel()._finalPosition;
    });
    this._fireEvent("loadsuccess");

};


mindplot.MindmapDesigner.prototype.getMindmap = function()
{
    return this._mindmap;
};

mindplot.MindmapDesigner.prototype.undo = function()
{
    this._actionRunner.undo();
};

mindplot.MindmapDesigner.prototype.redo = function()
{
    this._actionRunner.redo();
};

mindplot.MindmapDesigner.prototype._nodeModelToNodeGraph = function(nodeModel, isVisible)
{
    core.assert(nodeModel, "Node model can not be null");
    var nodeGraph = this._buildNodeGraph(nodeModel);

    if(core.Utils.isDefined(isVisible))
        nodeGraph.setVisibility(isVisible);

    var children = nodeModel.getChildren().slice();

    children = this._layoutManager.prepareNode(nodeGraph, children);

    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];
        if(core.Utils.isDefined(child))
            this._nodeModelToNodeGraph(child);
    }

    var workspace = this._workspace;
    workspace.appendChild(nodeGraph);
    return nodeGraph;
};

mindplot.MindmapDesigner.prototype._relationshipModelToRelationship = function(model) {
    core.assert(model, "Node model can not be null");
    var relationship = this._buildRelationship(model);
    var sourceTopic = relationship.getSourceTopic();
    sourceTopic.addRelationship(relationship);
    var targetTopic = relationship.getTargetTopic();
    targetTopic.addRelationship(relationship);
    relationship.setVisibility(sourceTopic.isVisible() && targetTopic.isVisible());
    var workspace = this._workspace;
    workspace.appendChild(relationship);
    relationship.redraw();
    return relationship;
};

mindplot.MindmapDesigner.prototype.createRelationship= function(model){
    this._mindmap.addRelationship(model);
    return this._relationshipModelToRelationship(model);
};

mindplot.MindmapDesigner.prototype.removeRelationship = function(model) {
    this._mindmap.removeRelationship(model);
    var relationship = this._relationships[model.getId()];
    var sourceTopic = relationship.getSourceTopic();
    sourceTopic.removeRelationship(relationship);
    var targetTopic = relationship.getTargetTopic();
    targetTopic.removeRelationship(relationship);
    this._workspace.removeChild(relationship);
    delete this._relationships[model.getId()];
};

mindplot.MindmapDesigner.prototype._buildRelationship = function (model) {
  var workspace = this._workspace;
    var elem = this;

    var fromNodeId = model.getFromNode();
    var toNodeId = model.getToNode();

    var fromTopic = null;
    var toTopic = null;
    var topics = this._topics;

        for (var i = 0; i < topics.length; i++)
        {
            var t = topics[i];
            if (t.getModel().getId() == fromNodeId)
            {
                fromTopic= t;
            }
            if (t.getModel().getId() == toNodeId)
            {
                toTopic= t;
            }
            if(toTopic!=null && fromTopic!=null){
                break;
            }
        }
    
    // Create node graph ...
    var relationLine = new mindplot.RelationshipLine(fromTopic, toTopic, model.getLineType());
    if(core.Utils.isDefined(model.getSrcCtrlPoint())){
        var srcPoint = model.getSrcCtrlPoint().clone();
        relationLine.setSrcControlPoint(srcPoint);
    }
    if(core.Utils.isDefined(model.getDestCtrlPoint())){
        var destPoint = model.getDestCtrlPoint().clone();
        relationLine.setDestControlPoint(destPoint);
    }


    relationLine.getLine().setDashed(3,2);
    relationLine.setShowEndArrow(model.getEndArrow());
    relationLine.setShowStartArrow(model.getStartArrow());
    relationLine.setModel(model);

    //Add Listeners
    var elem = this;
    relationLine.addEventListener('onfocus', function(event)
    {
        elem.onObjectFocusEvent.attempt([relationLine, event], elem);
    });

    // Append it to the workspace ...
    this._relationships[model.getId()]=relationLine;

    return  relationLine;
};

mindplot.MindmapDesigner.prototype.getEditor = function()
{
    return this._editor;
};

mindplot.MindmapDesigner.prototype._removeNode = function(node)
{
    if (node.getTopicType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        var parent = node._parent;
        node.disconnect(this._workspace);

        //remove children
        while (node._getChildren().length > 0)
        {
            this._removeNode(node._getChildren()[0]);
        }

        this._workspace.removeChild(node);
        this._topics.erase(node);

        // Delete this node from the model...
        var model = node.getModel();
        model.deleteNode();

        if (core.Utils.isDefined(parent))
        {
            this._goToNode(parent);
        }
    }
};

mindplot.MindmapDesigner.prototype.deleteCurrentNode = function()
{

    var validateFunc = function(selectedObject) {
        return selectedObject.getType() == mindplot.RelationshipLine.type || selectedObject.getTopicType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE
    };
    var validateError = 'Central topic can not be deleted.';
    var selectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);
    if (selectedObjects.nodes.length > 0 || selectedObjects.relationshipLines.length>0)
    {
        var command = new mindplot.commands.DeleteTopicCommand(selectedObjects);
        this._actionRunner.execute(command);
    }

};

mindplot.MindmapDesigner.prototype.setFont2SelectedNode = function(font)
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic, font)
        {
            var result = topic.getFontFamily();
            topic.setFontFamily(font, true);

            core.Executor.instance.delay(topic.updateNode, 0,topic);
            /*var updated = function() {
                topic.updateNode();
            };
            updated.delay(0);*/
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, font, topicsIds);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.setStyle2SelectedNode = function()
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic)
        {
            var result = topic.getFontStyle();
            var style = (result == "italic") ? "normal" : "italic";
            topic.setFontStyle(style, true);
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, "", topicsIds);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.setFontColor2SelectedNode = function(color)
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic, color)
        {
            var result = topic.getFontColor();
            topic.setFontColor(color, true);
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, color, topicsIds);
        command.discartDuplicated = "fontColorCommandId";
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.setBackColor2SelectedNode = function(color)
{

    var validateFunc = function(topic) {
        return topic.getShapeType() != mindplot.NodeModel.SHAPE_TYPE_LINE
    };
    var validateError = 'Color can not be setted to line topics.';
    var validSelectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);;
    var topicsIds = validSelectedObjects.nodes;

    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic, color)
        {
            var result = topic.getBackgroundColor();
            topic.setBackgroundColor(color);
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, color, topicsIds);
        command.discartDuplicated = "backColor";
        this._actionRunner.execute(command);
    }
};


mindplot.MindmapDesigner.prototype._getValidSelectedObjectsIds = function(validate, errorMsg)
{
    var result = {"nodes":[],"relationshipLines":[]};
    var selectedNodes = this._getSelectedNodes();
    var selectedRelationshipLines = this.getSelectedRelationshipLines();
    if (selectedNodes.length == 0 && selectedRelationshipLines.length == 0)
    {
        core.Monitor.getInstance().logMessage('At least one element must be selected to execute this operation.');
    } else
    {
        var isValid = true;
        for (var i = 0; i < selectedNodes.length; i++)
        {
            var selectedNode = selectedNodes[i];
            if (core.Utils.isDefined(validate))
            {
                isValid = validate(selectedNode);
            }

            // Add node only if it's valid.
            if (isValid)
            {
                result.nodes.push(selectedNode.getId());
            } else
            {
                core.Monitor.getInstance().logMessage(errorMsg);
            }
        }
        for( var j = 0; j< selectedRelationshipLines.length; j++){
            var selectedLine = selectedRelationshipLines[j];
            isValid = true;
            if(core.Utils.isDefined(validate)){
                isValid = validate(selectedLine);
            }

            if(isValid){
                result.relationshipLines.push(selectedLine.getId());
            } else
            {
                core.Monitor.getInstance().logMessage(errorMsg);
            }
        }
    }
    return result;
}

mindplot.MindmapDesigner.prototype.setBorderColor2SelectedNode = function(color)
{
    var validateFunc = function(topic) {
        return topic.getShapeType() != mindplot.NodeModel.SHAPE_TYPE_LINE
    };
    var validateError = 'Color can not be setted to line topics.';
    var validSelectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);;
    var topicsIds = validSelectedObjects.nodes;

    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic, color)
        {
            var result = topic.getBorderColor();
            topic.setBorderColor(color);
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, color, topicsIds);
        command.discartDuplicated = "borderColorCommandId";
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.setFontSize2SelectedNode = function(size)
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic, size)
        {
            var result = topic.getFontSize();
            topic.setFontSize(size, true);

            core.Executor.instance.delay(topic.updateNode, 0,topic);
            /*var updated = function() {
                topic.updateNode();
            };
            updated.delay(0);*/
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, size, topicsIds);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.setShape2SelectedNode = function(shape)
{
    var validateFunc = function(topic) {
        return !(topic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE && shape == mindplot.NodeModel.SHAPE_TYPE_LINE)
    };
    var validateError = 'Central Topic shape can not be changed to line figure.';
    var validSelectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);
    var topicsIds = validSelectedObjects.nodes;

    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic, size)
        {
            var result = topic.getShapeType();
            topic.setShapeType(size, true);
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, shape, topicsIds);
        this._actionRunner.execute(command);
    }
};


mindplot.MindmapDesigner.prototype.setWeight2SelectedNode = function()
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var commandFunc = function(topic)
        {
            var result = topic.getFontWeight();
            var weight = (result == "bold") ? "normal" : "bold";
            topic.setFontWeight(weight, true);

            core.Executor.instance.delay(topic.updateNode, 0,topic);
            /*var updated = function() {
                topic.updateNode();
            };
            updated.delay(0);*/
            return result;
        }
        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, "", topicsIds);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.addImage2SelectedNode = function(iconType)
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {

        var command = new mindplot.commands.AddIconToTopicCommand(topicsIds[0], iconType);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.addLink2Node = function(url)
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var command = new mindplot.commands.AddLinkToTopicCommand(topicsIds[0], url);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.addLink2SelectedNode = function()
{
    var selectedTopics = this.getSelectedNodes();
        var topic = null;
        if (selectedTopics.length > 0)
        {
            topic = selectedTopics[0];
            if (!$chk(topic._hasLink)) {
                var msg = new Element('div');
                var urlText = new Element('div').inject(msg);
                urlText.innerHTML = "URL:"
                var formElem = new Element('form', {'action': 'none', 'id':'linkFormId'});
                var urlInput = new Element('input', {'type': 'text', 'size':30});
                urlInput.inject(formElem);
                formElem.inject(msg)

                var okButtonId = "linkOkButtonId";
                formElem.addEvent('submit', function(e)
                {
                    $(okButtonId).fireEvent('click', e);
                    e = new Event(e);
                    e.stop();
                });


                var okFunction = function() {
                    var url = urlInput.value;
                    var result = false;
                    if ("" != url.trim())
                    {
                        this.addLink2Node(url);
                        result = true;
                    }
                    return result;
                }.bind(this);
                var dialog = mindplot.LinkIcon.buildDialog(this, okFunction, okButtonId);
                dialog.adopt(msg).show();

                // IE doesn't like too much this focus action...
                if(!core.UserAgent.isIE())
                {
                    urlInput.focus();
                }
            }
        } else
        {
            core.Monitor.getInstance().logMessage('At least one topic must be selected to execute this operation.');
        }
};

mindplot.MindmapDesigner.prototype.addNote2Node = function(text)
{
    var validSelectedObjects = this._getValidSelectedObjectsIds();
    var topicsIds = validSelectedObjects.nodes;
    if (topicsIds.length > 0)
    {
        var command = new mindplot.commands.AddNoteToTopicCommand(topicsIds[0], text);
        this._actionRunner.execute(command);
    }
};

mindplot.MindmapDesigner.prototype.addNote2SelectedNode = function()
{
    var selectedTopics = this.getSelectedNodes();
        var topic = null;
        if (selectedTopics.length > 0)
        {
            topic = selectedTopics[0];
            if (!$chk(topic._hasNote)) {
                var msg = new Element('div');
                var text = new Element('div').inject(msg);
                var formElem = new Element('form', {'action': 'none', 'id':'noteFormId'});
                var textInput = new Element('textarea').setStyles({'width':280, 'height':50});
                textInput.inject(formElem);
                formElem.inject(msg);

                var okButtonId = "noteOkButtonId";
                formElem.addEvent('submit', function(e)
                {
                    $(okButtonId).fireEvent('click', e);
                    e = new Event(e);
                    e.stop();
                });


                var okFunction = function() {
                    var text = textInput.value;
                    var result = false;
                    if ("" != text.trim())
                    {
                        this.addNote2Node(text);
                        result = true;
                    }
                    return result;
                }.bind(this);
                var dialog = mindplot.Note.buildDialog(this, okFunction, okButtonId);
                dialog.adopt(msg).show();

                // IE doesn't like too much this focus action...
                if(!core.UserAgent.isIE())
                {
                    textInput.focus();
                }
            }
        } else
        {
            core.Monitor.getInstance().logMessage('At least one topic must be selected to execute this operation.');
        }
};

mindplot.MindmapDesigner.prototype.removeLastImageFromSelectedNode = function()
{
    var nodes = this._getSelectedNodes();
    if (nodes.length == 0)
    {
        core.Monitor.getInstance().logMessage('A topic must be selected in order to execute this operation.');
    } else
    {
        var elem = nodes[0];
        elem.removeLastIcon(this);
        core.Executor.instance.delay(elem.updateNode, 0,elem);
        /*var executor = function(editor)
        {
            return function()
            {
                elem.updateNode();
            };
        };

        setTimeout(executor(this), 0);*/
    }
};


mindplot.MindmapDesigner.prototype._getSelectedNodes = function()
{
    var result = new Array();
    for (var i = 0; i < this._topics.length; i++)
    {
        if (this._topics[i].isOnFocus())
        {
            result.push(this._topics[i]);
        }
    }
    return result;
};

mindplot.MindmapDesigner.prototype.getSelectedRelationshipLines = function(){
    var result = new Array();
    for (var id in this._relationships)
    {
        var relationship = this._relationships[id];
        if (relationship.isOnFocus())
        {
            result.push(relationship);
        }
    }
    return result;
};

mindplot.MindmapDesigner.prototype.getSelectedNodes = function()
{
    return this._getSelectedNodes();
};

mindplot.MindmapDesigner.prototype.getSelectedObjects = function()
{
    var selectedNodes = this.getSelectedNodes();
    var selectedRelationships = this.getSelectedRelationshipLines();
    selectedRelationships.extend(selectedNodes);
    return selectedRelationships;
};

mindplot.MindmapDesigner.prototype.keyEventHandler = function(event)
{
    if(this._workspace.isWorkspaceEventsEnabled()){
    var evt = (event) ? event : window.event;

    if (evt.keyCode == 8)
    {
        if (core.Utils.isDefined(event))
        {
            if (core.Utils.isDefined(event.preventDefault)) {
                event.preventDefault();
            } else {
                event.returnValue = false;
            }
            new Event(event).stop();
        }
        else
            evt.returnValue = false;
    }
    else
    {
        evt = new Event(event);
        var key = evt.key;
        if (!this._editor.isVisible())
        {
            if (((evt.code >= 65 && evt.code <= 90) || (evt.code >= 48 && evt.code <= 57)) && !(evt.control || evt.meta))
            {
                if($chk(evt.shift)){
                    key = key.toUpperCase();
                }
                this._showEditor(key);
            }
            else
            {
                switch (key)
                        {
                    case 'delete':
                        this.deleteCurrentNode();
                        break;
                    case 'enter':
                        if (!evt.meta)
                        {
                            this.createSiblingForSelectedNode();
                            break;
                        }
                    case 'insert':
                        this.createChildForSelectedNode();
                        break;
                    case 'right':
                        var nodes = this._getSelectedNodes();
                        if (nodes.length > 0)
                        {
                            var node = nodes[0];
                            if (node.getTopicType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
                            {
                                this._goToSideChild(node, 'RIGHT');
                            }
                            else
                            {
                                if (node.getPosition().x < 0)
                                {
                                    this._goToParent(node);
                                }
                                else if (!node.areChildrenShrinked())
                                {
                                    this._goToChild(node);
                                }
                            }
                        }
                        break;
                    case 'left':
                        var nodes = this._getSelectedNodes();
                        if (nodes.length > 0)
                        {
                            var node = nodes[0];
                            if (node.getTopicType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
                            {
                                this._goToSideChild(node, 'LEFT');
                            }
                            else
                            {
                                if (node.getPosition().x > 0)
                                {
                                    this._goToParent(node);
                                }
                                else if (!node.areChildrenShrinked())
                                {
                                    this._goToChild(node);
                                }
                            }
                        }
                        break;
                    case'up':
                        var nodes = this._getSelectedNodes();
                        if (nodes.length > 0)
                        {
                            var node = nodes[0];
                            if (node.getTopicType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
                            {
                                this._goToBrother(node, 'UP');
                            }
                        }
                        break;
                    case 'down':
                        var nodes = this._getSelectedNodes();
                        if (nodes.length > 0)
                        {
                            var node = nodes[0];
                            if (node.getTopicType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
                            {
                                this._goToBrother(node, 'DOWN');
                            }
                        }
                        break;
                    case 'f2':
                        this._showEditor();
                        break;
                    case 'space':

                        var nodes = this._getSelectedNodes();
                        if (nodes.length > 0)
                        {
                            var topic = nodes[0];

                            var model = topic.getModel();
                            var isShrink = !model.areChildrenShrinked();
                            topic.setChildrenShrinked(isShrink);
                        }
                        break;
                    case 'backspace':
                        evt.preventDefault();
                        break;
                    case 'esc':
                        var nodes = this._getSelectedNodes();
                        for (var i = 0; i < nodes.length; i++)
                        {
                            var node = nodes[i];
                            node.setOnFocus(false);
                        }
                        break;
                    case 'z':
                        if (evt.control || evt.meta)
                        {
                            if (evt.shift)
                            {
                                this.redo();
                            }
                            else
                            {
                                this.undo();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            evt.stop();
        }
    }
    }
};

mindplot.MindmapDesigner.prototype._showEditor = function(key)
{
    var nodes = this._getSelectedNodes();
    if (nodes.length == 1)
    {
        var node = nodes[0];
        if (key && key != "")
        {
            this._editor.setInitialText(key);
        }
        this._editor.getFocusEvent.attempt(node, this._editor);
    }
};

mindplot.MindmapDesigner.prototype._goToBrother = function(node, direction)
{
    var brothers = node._parent._getChildren();
    var target = node;
    var y = node.getPosition().y;
    var x = node.getPosition().x;
    var dist = null;
    for (var i = 0; i < brothers.length; i++)
    {
        var sameSide = (x * brothers[i].getPosition().x) >= 0;
        if (brothers[i] != node && sameSide)
        {
            var brother = brothers[i];
            var brotherY = brother.getPosition().y;
            if (direction == "DOWN" && brotherY > y)
            {
                var distancia = y - brotherY;
                if (distancia < 0)
                {
                    distancia = distancia * (-1);
                }
                if (dist == null || dist > distancia)
                {
                    dist = distancia;
                    target = brothers[i];
                }
            }
            else if (direction == "UP" && brotherY < y)
            {
                var distancia = y - brotherY;
                if (distancia < 0)
                {
                    distancia = distancia * (-1);
                }
                if (dist == null || dist > distancia)
                {
                    dist = distancia;
                    target = brothers[i];
                }
            }
        }
    }
    this._goToNode(target);
};

mindplot.MindmapDesigner.prototype._goToNode = function(node)
{
    node.setOnFocus(true);
    this.onObjectFocusEvent.attempt(node, this);
};

mindplot.MindmapDesigner.prototype._goToSideChild = function(node, side)
{
    var children = node._getChildren();
    if (children.length > 0)
    {
        var target = children[0];
        var top = null;
        for (var i = 0; i < children.length; i++)
        {
            var child = children[i];
            var childY = child.getPosition().y;
            if (side == 'LEFT' && child.getPosition().x < 0)
            {
                if (top == null || childY < top)
                {
                    target = child;
                    top = childY;
                }
            }
            if (side == 'RIGHT' && child.getPosition().x > 0)
            {
                if (top == null || childY < top)
                {
                    target = child;
                    top = childY;
                }
            }
        }

        this._goToNode(target);
    }
};

mindplot.MindmapDesigner.prototype._goToParent = function(node)
{
    var parent = node._parent;
    this._goToNode(parent);
};

mindplot.MindmapDesigner.prototype._goToChild = function(node)
{
    var children = node._getChildren();
    if (children.length > 0)
    {
        var target = children[0];
        var top = target.getPosition().y;
        for (var i = 0; i < children.length; i++)
        {
            var child = children[i];
            if (child.getPosition().y < top)
            {
                top = child.getPosition().y;
                target = child;
            }
        }
        this._goToNode(target);
    }
};

mindplot.MindmapDesigner.prototype.getWorkSpace = function()
{
    return this._workspace;
};

mindplot.MindmapDesigner.prototype.findRelationShipsByTopicId = function(topicId){
    var result = [];
    for(var relationshipId in this._relationships){
        var relationship = this._relationships[relationshipId];
        if(relationship.getModel().getFromNode()==topicId || relationship.getModel().getToNode()==topicId){
            result.push(relationship);
        }
    }
    return result;
};
