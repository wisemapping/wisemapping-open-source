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

mindplot.MindmapDesigner = new Class({
        Extends: Events,
        initialize: function(profile, divElement) {
            $assert(profile, "profile must be defined");
            $assert(profile.zoom, "zoom must be defined");
            $assert(divElement, "divElement must be defined");

            // Dispatcher manager ...
            var commandContext = new mindplot.CommandContext(this);
//            this._actionDispatcher = new mindplot.BrixActionDispatcher(commandContext);
                this._actionDispatcher = new mindplot.LocalActionDispatcher(commandContext);
            this._actionDispatcher.addEvent("modelUpdate", function(event) {
                this.fireEvent("modelUpdate", event);
            }.bind(this));

            mindplot.ActionDispatcher.setInstance(this._actionDispatcher);
            this._model = new mindplot.DesignerModel(profile);

            // Init Screen manager..
            var screenManager = new mindplot.ScreenManager(divElement);
            this._workspace = new mindplot.Workspace(screenManager, this._model.getZoom());
            this._readOnly = profile.readOnly ? true : false;

            // Init layout managers ...
            this._layoutManager = new mindplot.layout.OriginalLayoutManager(this);

            // Register events
            if (!profile.readOnly) {
                this._registerEvents();
            }
        },

        _registerEvents : function() {
            // Register mouse events ...
            this._registerMouseEvents();

            // Register keyboard events ...
            mindplot.DesignerKeyboard.register(this);

            // To prevent the user from leaving the page with changes ...
            $(window).addEvent('beforeunload', function () {
                if (this.needsSave()) {
                    this.save(null, false)
                }
            }.bind(this));
        },

        _registerMouseEvents : function() {
            var workspace = this._workspace;
            var screenManager = workspace.getScreenManager();

            // Initialize workspace event listeners.
            screenManager.addEvent('update', function() {
                // Topic must be set to his original state. All editors must be closed.
                var objects = this.getModel().getObjects();
                objects.forEach(function(object) {
                    object.closeEditors();
                });

                // Clean some selected nodes on event ..
                if (this._cleanScreen)
                    this._cleanScreen();

            }.bind(this));

            // Deselect on click ...
            screenManager.addEvent('click', function(event) {
                this.onObjectFocusEvent(null, event);
            }.bind(this));

            // Create nodes on double click...
            screenManager.addEvent('dblclick', function(event) {
                if (workspace.isWorkspaceEventsEnabled()) {
                    // Get mouse position
                    var pos = screenManager.getWorkspaceMousePosition(event);

                    // Create a new topic model ...
                    var mindmap = this.getMindmap();
                    var model = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);
                    model.setPosition(pos.x, pos.y);

                    // Get central topic ...
                    var centralTopic = this.getModel().getCentralTopic();
                    var centralTopicId = centralTopic.getId();

                    // Execute action ...
                    this._actionDispatcher.addTopic(model, centralTopicId, true);
                }
            }.bind(this));


            $(document).addEvent('mousewheel', function(event) {
                if (event.wheel > 0) {
                    this.zoomIn(1.05);
                }
                else {
                    this.zoomOut(1.05);
                }
            }.bind(this));

        },

        setViewPort : function(size) {
            this._workspace.setViewPort(size);
            var model = this.getModel();
            this._workspace.setZoom(model.getZoom(), true);
        },

        _buildNodeGraph : function(model) {
            var workspace = this._workspace;

            // Create node graph ...
            var topic = mindplot.NodeGraph.create(model);
            this._layoutManager.addHelpers(topic);

            // Append it to the workspace ...
            this.getModel().addTopic(topic);

            // Add Topic events ...
            if (!this._readOnly) {
                // Add drag behaviour ...
                this._layoutManager.registerListenersOnNode(topic);

                // If a node had gained focus, clean the rest of the nodes ...
                topic.addEvent('mousedown', function(event) {
                    this.onObjectFocusEvent(topic, event);
                }.bind(this));
            }

            // Connect Topic ...
            var isConnected = model.isConnected();
            if (isConnected) {
                // Improve this ...
                var targetTopicModel = model.getParent();
                var targetTopic = null;

                var topics = this.getModel().getTopics();
                for (var i = 0; i < topics.length; i++) {
                    var t = topics[i];
                    if (t.getModel() == targetTopicModel) {
                        targetTopic = t;
                        // Disconnect the node. It will be connected again later ...
                        model.disconnect();
                        break;
                    }
                }
                $assert(targetTopic, "Could not find a topic to connect");
                topic.connectTo(targetTopic, workspace);
            }

            return  topic;
        },

        onObjectFocusEvent : function(currentObject, event) {
            // Close node editors ..
            var topics = this.getModel().getTopics();
            topics.forEach(function(topic) {
                topic.closeEditors();
            });

            var model = this.getModel();
            var objects = model.getObjects();
            objects.forEach(function(object) {
                // Disable all nodes on focus but not the current if Ctrl key isn't being pressed
                if (!$defined(event) || (!event.ctrlKey && !event.metaKey)) {
                    if (object.isOnFocus() && object != currentObject) {
                        object.setOnFocus(false);
                    }
                }
            });

        },

        selectAll : function() {
            var model = this.getModel();
            var objects = model.getObjects();
            objects.forEach(function(object) {
                object.setOnFocus(true);
            });
        },

        deselectAll : function() {
            var objects = this.getModel().getObjects();
            objects.forEach(function(object) {
                object.setOnFocus(false);
            });
        },

        zoomOut : function(factor) {
            if (!factor)
                factor = 1.2;

            var model = this.getModel();
            var scale = model.getZoom() * factor;
            if (scale <= 1.9) {
                model.setZoom(scale);
                this._workspace.setZoom(scale);
            }
            else {
                core.Monitor.getInstance().logMessage('Sorry, no more zoom can be applied. \n Why do you need more?');
            }

        },

        zoomIn : function(factor) {
            if (!factor)
                factor = 1.2;

            var model = this.getModel();
            var scale = model.getZoom() / factor;

            if (scale >= 0.3) {
                model.setZoom(scale);
                this._workspace.setZoom(scale);
            }
            else {
                core.Monitor.getInstance().logMessage('Sorry, no more zoom can be applied. \n Why do you need more?');
            }
        },

        getModel : function() {
            return this._model;
        },

        createChildForSelectedNode : function() {

            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. Only one node must be selected.');
                return;

            }
            if (nodes.length > 1) {

                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. One topic must be selected.');
                return;
            }

            // Add new node ...
            var centalTopic = nodes[0];
            var parentTopicId = centalTopic.getId();
            var childModel = centalTopic.createChildModel(this._layoutManager.needsPrepositioning());

            // Execute event ...
            this._actionDispatcher.addTopic(childModel, parentTopicId, true);

        },

        createSiblingForSelectedNode : function() {
            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. Only one node must be selected.');
                return;

            }
            if (nodes.length > 1) {
                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. One topic must be selected.');
                return;
            }

            var topic = nodes[0];
            if (topic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                // Central topic doesn't have siblings ...
                this.createChildForSelectedNode();

            } else {
                var parentTopic = topic.getOutgoingConnectedTopic();
                var siblingModel = topic.createSiblingModel(this._layoutManager.needsPrepositioning());
                var parentTopicId = parentTopic.getId();

                this._actionDispatcher.addTopic(siblingModel, parentTopicId, true);
            }
        },

        addRelationShip : function(event) {
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);
            var selectedTopics = this.getModel().filterSelectedTopics();
            if (selectedTopics.length > 0 &&
                (!$defined(this._creatingRelationship) || ($defined(this._creatingRelationship) && !this._creatingRelationship))) {
                this._workspace.enableWorkspaceEvents(false);
                var fromNodePosition = selectedTopics[0].getPosition();
                this._relationship = new web2d.CurvedLine();
                this._relationship.setStyle(web2d.CurvedLine.SIMPLE_LINE);
                this._relationship.setDashed(2, 2);
                this._relationship.setFrom(fromNodePosition.x, fromNodePosition.y);
                this._relationship.setTo(pos.x, pos.y);
                this._workspace.appendChild(this._relationship);
                this._creatingRelationship = true;
                this._relationshipMouseMoveFunction = this._relationshipMouseMove.bindWithEvent(this);
                this._relationshipMouseClickFunction = this._relationshipMouseClick.bindWithEvent(this, selectedTopics[0]);

                screen.addEvent('mousemove', this._relationshipMouseMoveFunction);
                screen.addEvent('click', this._relationshipMouseClickFunction);
            }
        },

        _relationshipMouseMove : function(event) {
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);
            this._relationship.setTo(pos.x - 1, pos.y - 1); //to prevent click event target to be the line itself
            event.preventDefault();
            event.stop();
            return false;
        },

        _relationshipMouseClick : function (event, fromNode) {
            var target = event.target;
            while (target.tagName != "g" && $defined(target.parentNode)) {
                target = target.parentNode;
            }
            if ($defined(target.virtualRef)) {
                var targetNode = target.virtualRef;
                this.addRelationship(fromNode, targetNode);
            }
            this._workspace.removeChild(this._relationship);
            this._relationship = null;
            this._workspace.getScreenManager().removeEvent('mousemove', this._relationshipMouseMoveFunction);
            this._workspace.getScreenManager().removeEvent('click', this._relationshipMouseClickFunction);
            this._creatingRelationship = false;
            this._workspace.enableWorkspaceEvents(true);
            event.preventDefault();
            event.stop();
            return false;
        },

        addRelationship : function(fromNode, toNode) {
            // Create a new topic model ...
            var mindmap = this.getMindmap();
            var model = mindmap.createRelationship(fromNode.getModel().getId(), toNode.getModel().getId());

            this._actionDispatcher.addRelationship(model, mindmap);

        },

        needsSave : function() {
            return this._actionRunner.hasBeenChanged();
        },

        autoSaveEnabled : function(value) {
            if ($defined(value) && value) {
                var autosave = function() {
                    if (this.needsSave()) {
                        this.save(null, false);
                    }
                };
                autosave.bind(this).periodical(30000);
            }
        },

        save : function(onSavedHandler, saveHistory) {
            var persistantManager = mindplot.PersistanceManager;
            var mindmap = this._mindmap;

            var properties = {zoom:this.getModel().getZoom(), layoutManager:this._layoutManager.getClassName()};
            persistantManager.save(mindmap, properties, onSavedHandler, saveHistory);
            this.fireEvent("save", {type:saveHistory});

            // Refresh undo state...
            this._actionRunner.markAsChangeBase();
        },

        loadFromCollaborativeModel: function(collaborationManager) {
            var mindmap = collaborationManager.buildWiseModel();
            this._loadMap(1, mindmap);

            // Place the focus on the Central Topic
            var centralTopic = this.getModel().getCentralTopic();
            this.goToNode.attempt(centralTopic, this);
        },

        loadFromXML : function(mapId, xmlContent) {
            $assert(xmlContent, 'mindmapId can not be null');
            $assert(xmlContent, 'xmlContent can not be null');

            // Explorer Hack with local files ...
            var domDocument = core.Utils.createDocumentFromText(xmlContent);

            var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
            var mindmap = serializer.loadFromDom(domDocument);

            this._loadMap(mapId, mindmap);

            // Place the focus on the Central Topic
            var centralTopic = this.getModel().getCentralTopic();
            this.goToNode(centralTopic);

        },

        load : function(mapId) {
            $assert(mapId, 'mapName can not be null');

            // Build load function ...
            var persistantManager = mindplot.PersistanceManager;

            // Loading mindmap ...
            var mindmap = persistantManager.load(mapId);

            // Finally, load the map in the editor ...
            this._loadMap(mapId, mindmap);

            // Place the focus on the Central Topic
            var centralTopic = this.getModel().getCentralTopic();
            this.goToNode.attempt(centralTopic, this);
        },

        _loadMap : function(mapId, mindmapModel) {
            var designer = this;
            if (mindmapModel != null) {
                mindmapModel.setId(mapId);
                designer._mindmap = mindmapModel;

                // Building node graph ...
                var branches = mindmapModel.getBranches();
                for (var i = 0; i < branches.length; i++) {
                    // NodeModel -> NodeGraph ...
                    var nodeModel = branches[i];
                    var nodeGraph = this._nodeModelToNodeGraph(nodeModel, false);

                    // Update shrink render state...
                    nodeGraph.setBranchVisibility(true);
                }
                var relationships = mindmapModel.getRelationships();
                for (var j = 0; j < relationships.length; j++) {
                    this._relationshipModelToRelationship(relationships[j]);
                }
            }

            this.getModel().getTopics().forEach(function(topic) {
                delete topic.getModel()._finalPosition;
            });

        },

        getMindmap : function() {
            return this._mindmap;
        },

        undo : function() {
            this._actionRunner.undo();
        },

        redo : function() {
            this._actionRunner.redo();
        },

        _nodeModelToNodeGraph : function(nodeModel, isVisible) {
            $assert(nodeModel, "Node model can not be null");
            var nodeGraph = this._buildNodeGraph(nodeModel);

            if (isVisible)
                nodeGraph.setVisibility(isVisible);

            var children = nodeModel.getChildren().slice();
            children = this._layoutManager.prepareNode(nodeGraph, children);

            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if ($defined(child))
                    this._nodeModelToNodeGraph(child, false);
            }

            var workspace = this._workspace;
            workspace.appendChild(nodeGraph);
            return nodeGraph;
        }
        ,

        _relationshipModelToRelationship : function(model) {
            $assert(model, "Node model can not be null");
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
        }
        ,

        createRelationship : function(model) {
            this._mindmap.addRelationship(model);
            return this._relationshipModelToRelationship(model);
        },

        removeRelationship : function(model) {
            this._mindmap.removeRelationship(model);
            var relationship = this._relationships[model.getId()];
            var sourceTopic = relationship.getSourceTopic();
            sourceTopic.removeRelationship(relationship);
            var targetTopic = relationship.getTargetTopic();
            targetTopic.removeRelationship(relationship);
            this._workspace.removeChild(relationship);
            delete this._relationships[model.getId()];
        },

        _buildRelationship : function (topicModel) {
            var elem = this;

            var fromNodeId = topicModel.getFromNode();
            var toNodeId = topicModel.getToNode();

            var fromTopic = null;
            var toTopic = null;
            var model = this.getModel();
            var topics = model.getTopics();

            for (var i = 0; i < topics.length; i++) {
                var t = topics[i];
                if (t.getModel().getId() == fromNodeId) {
                    fromTopic = t;
                }
                if (t.getModel().getId() == toNodeId) {
                    toTopic = t;
                }
                if (toTopic != null && fromTopic != null) {
                    break;
                }
            }

            // Create node graph ...
            var relationLine = new mindplot.RelationshipLine(fromTopic, toTopic, topicModel.getLineType());
            if ($defined(topicModel.getSrcCtrlPoint())) {
                var srcPoint = topicModel.getSrcCtrlPoint().clone();
                relationLine.setSrcControlPoint(srcPoint);
            }
            if ($defined(topicModel.getDestCtrlPoint())) {
                var destPoint = topicModel.getDestCtrlPoint().clone();
                relationLine.setDestControlPoint(destPoint);
            }


            relationLine.getLine().setDashed(3, 2);
            relationLine.setShowEndArrow(topicModel.getEndArrow());
            relationLine.setShowStartArrow(topicModel.getStartArrow());
            relationLine.setModel(topicModel);

            //Add Listeners
            relationLine.addEvent('onfocus', function(event) {
                elem.onObjectFocusEvent.attempt([relationLine, event], elem);
            });

            // Append it to the workspace ...
            this._relationships[topicModel.getId()] = relationLine;

            return  relationLine;
        },

        _removeNode : function(node) {
            if (node.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE)
            {
                var parent = node._parent;
                node.disconnect(this._workspace);

                //remove children
                while (node._getChildren().length > 0) {
                    this._removeNode(node._getChildren()[0]);
                }

                this._workspace.removeChild(node);
                this.getModel().removeTopic(node);

                // Delete this node from the model...
                var model = node.getModel();
                model.deleteNode();

                if ($defined(parent)) {
                    this.goToNode(parent);
                }
            }
        },

        deleteCurrentNode : function() {

            var validateFunc = function(object) {
                return object.getType() == mindplot.RelationshipLine.type || object.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE
            };
            var validateError = 'Central topic can not be deleted.';

            var model = this.getModel();
            var topicsIds = model.filterTopicsIds(validateFunc, validateError);
            var relIds = model.filterRelationIds(validateFunc, validateError);

            if (topicsIds.length > 0 || relIds.length > 0) {
                this._actionDispatcher.deleteTopics(topicsIds,relIds);
            }

        },

        changeFontFamily : function(font) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontFamilyToTopic(topicsIds, font);

            }
        },

        changeFontStyle : function() {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontStyleToTopic(topicsIds);
            }
        },

        changeFontColor : function(color) {
            $assert(color, "color can not be null");

            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontColorToTopic(topicsIds, color);
            }
        },

        changeBackgroundColor : function(color) {

            var validateFunc = function(topic) {
                return topic.getShapeType() != mindplot.model.NodeModel.SHAPE_TYPE_LINE
            };
            var validateError = 'Color can not be set to line topics.';

            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBackgroundColorToTopic(topicsIds, color);
            }
        },

        changeBorderColor : function(color) {
            var validateFunc = function(topic) {
                return topic.getShapeType() != mindplot.model.NodeModel.SHAPE_TYPE_LINE
            };
            var validateError = 'Color can not be set to line topics.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBorderColorToTopic(topicsIds, color);
            }
        },

        changeFontSize : function(size) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontSizeToTopic(topicsIds, size);
            }
        },

        changeTopicShape : function(shape) {
            var validateFunc = function(topic) {
                return !(topic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE && shape == mindplot.model.NodeModel.SHAPE_TYPE_LINE)
            };
            var validateError = 'Central Topic shape can not be changed to line figure.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeShapeToTopic(topicsIds, shape);
            }
        },

        changeFontWeight : function() {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontWeightToTopic(topicsIds);
            }
        },

        addIconType : function(iconType) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.addIconToTopic(topicsIds[0], iconType);
            }
        },

        addLink2Node : function(url) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.addLinkToTopic(topicsIds[0], url);
            }
        },

        addLink : function() {
            var selectedTopics = this.getModel().filterSelectedTopics();
            var topic = null;
            if (selectedTopics.length > 0) {
                topic = selectedTopics[0];
                if (!$defined(topic._hasLink)) {
                    var msg = new Element('div');
                    var urlText = new Element('div').inject(msg);
                    urlText.innerHTML = "URL:";
                    var formElem = new Element('form', {'action': 'none', 'id':'linkFormId'});
                    var urlInput = new Element('input', {'type': 'text', 'size':30});
                    urlInput.inject(formElem);
                    formElem.inject(msg);

                    var okButtonId = "linkOkButtonId";
                    formElem.addEvent('submit', function(e) {
                        $(okButtonId).fireEvent('click', e);
                        e = new Event(e);
                        e.stop();
                    });


                    var okFunction = function() {
                        var url = urlInput.value;
                        var result = false;
                        if ("" != url.trim()) {
                            this.addLink2Node(url);
                            result = true;
                        }
                        return result;
                    }.bind(this);

                    var dialog = mindplot.LinkIcon.buildDialog(this, okFunction, okButtonId);
                    dialog.adopt(msg).show();

                    // IE doesn't like too much this focus action...
                    if (!Browser.ie) {
                        urlInput.focus();
                    }
                }
            } else {
                core.Monitor.getInstance().logMessage('At least one topic must be selected to execute this operation.');
            }
        },

        addNote : function() {
            var model = this.getModel();
            var topic = model.selectedTopic();
            if (topic != null) {
                topic.showNoteEditor();
            } else {
                core.Monitor.getInstance().logMessage('At least one topic must be selected to execute this operation.');
            }
        },

        goToNode : function(node) {
            node.setOnFocus(true);
            this.onObjectFocusEvent(node);
        },

        getWorkSpace : function() {
            return this._workspace;
        }
    }
);
