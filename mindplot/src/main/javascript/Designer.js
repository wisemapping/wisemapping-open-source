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

mindplot.Designer = new Class({
        Extends: Events,
        initialize: function(profile, divElement) {
            $assert(profile, "profile must be defined");
            $assert(profile.zoom, "zoom must be defined");
            $assert(divElement, "divElement must be defined");

            // Dispatcher manager ...
            var commandContext = new mindplot.CommandContext(this);
            if (profile.collab == 'standalone') {
                this._actionDispatcher = new mindplot.StandaloneActionDispatcher(commandContext);
            } else {
                this._actionDispatcher = new mindplot.BrixActionDispatcher(commandContext);
            }

            this._actionDispatcher.addEvent("modelUpdate", function(event) {
                this.fireEvent("modelUpdate", event);
            }.bind(this));

            mindplot.ActionDispatcher.setInstance(this._actionDispatcher);
            this._model = new mindplot.DesignerModel(profile);

            // Init Screen manager..
            var screenManager = new mindplot.ScreenManager(divElement);
            this._workspace = new mindplot.Workspace(screenManager, this._model.getZoom());
            this._readOnly = profile.readOnly ? true : false;


            // Register events
            if (!profile.readOnly) {
                this._registerEvents();
            }

            this._relPivot = new mindplot.RelationshipPivot(this._workspace, this);

            // Init layout manager ...
            this._eventBussDispatcher = new mindplot.nlayout.EventBusDispatcher(this.getModel());

            // @todo: To be removed ...
            this._layoutManager = new mindplot.layout.OriginalLayoutManager(this);


        },

        _registerEvents : function() {
            // Register mouse events ...
            this._registerMouseEvents();

            // Register keyboard events ...
            mindplot.DesignerKeyboard.register(this);
        },

        _registerMouseEvents : function() {
            var workspace = this._workspace;
            var screenManager = workspace.getScreenManager();

            // Initialize workspace event listeners.
            screenManager.addEvent('update', function() {
                // Topic must be set to his original state. All editors must be closed.
                var topics = this.getModel().getTopics();
                topics.forEach(function(object) {
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
                    var model = mindmap.createNode(mindplot.model.INodeModel.MAIN_TOPIC_TYPE);
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

            topic.addEvent('ontblur', function() {
                var topics = this.getModel().filterSelectedTopics();
                var rels = this.getModel().filterSelectedRelations();

                if (topics.length == 0 || rels.length == 0) {
                    this.fireEvent('onblur');
                }
            }.bind(this));

            topic.addEvent('ontfocus', function() {
                var topics = this.getModel().filterSelectedTopics();
                var rels = this.getModel().filterSelectedRelations();

                if (topics.length == 1 || rels.length == 1) {
                    this.fireEvent('onfocus');
                }
            }.bind(this));

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
                if (!$defined(event) || (!event.control && !event.meta)) {
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
                $notify('No more zoom can be applied');
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
                $notify('No more zoom can be applied');
            }
        },

        getModel : function() {
            return this._model;
        },

        createChildForSelectedNode : function() {

            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                $notify('Could not create a topic. Only one node must be selected.');
                return;

            }
            if (nodes.length != 1) {

                // If there are more than one node selected,
                $notify('Could not create a topic. One topic must be selected.');
                return;
            }

            // Add new node ...
            var parentTopic = nodes[0];
            var parentTopicId = parentTopic.getId();
            var childModel = parentTopic.createChildModel(this._layoutManager.needsPrepositioning());

            // Execute event ...
            this._actionDispatcher.addTopic(childModel, parentTopicId, true);

        },

        createSiblingForSelectedNode : function() {
            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                $notify('Could not create a topic. Only one node must be selected.');
                return;

            }
            if (nodes.length > 1) {
                // If there are more than one node selected,
                $notify('Could not create a topic. One topic must be selected.');
                return;
            }

            var topic = nodes[0];
            if (topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                // Central topic doesn't have siblings ...
                this.createChildForSelectedNode();

            } else {
                var parentTopic = topic.getOutgoingConnectedTopic();
                var siblingModel = topic.createSiblingModel(this._layoutManager.needsPrepositioning());
                var parentTopicId = parentTopic.getId();

                this._actionDispatcher.addTopic(siblingModel, parentTopicId, true);
            }
        },

        showRelPivot : function(event) {
            // Current mouse position ....
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);
            var selectedTopic = this.getModel().selectedTopic();

            // create a connection ...
            this._relPivot.start(selectedTopic, pos);
        },

        connectByRelation : function(sourceTopic, targetTopic) {
            $assert(sourceTopic, "sourceTopic can not be null");
            $assert(targetTopic, "targetTopic can not be null");

            // Create a new topic model ...
            // @Todo: Model should not be modified from here ...
            var mindmap = this.getMindmap();
            var model = mindmap.createRelationship(sourceTopic.getModel().getId(), targetTopic.getModel().getId());

            this._actionDispatcher.connectByRelation(model);
        },

        needsSave : function() {
            //@Todo: Review all this ...
            return this._actionDispatcher._actionRunner.hasBeenChanged();
        },


        getMindmapProperties : function() {
            return   {zoom:this.getModel().getZoom(), layoutManager:this._layoutManager.getClassName()};
        },

        loadMap : function(mindmapModel) {
            $assert(mindmapModel, "mindmapModel can not be null");
            this._mindmap = mindmapModel;

            // Building node graph ...
            var branches = mindmapModel.getBranches();
            for (var i = 0; i < branches.length; i++) {
                // NodeModel -> NodeGraph ...
                var nodeModel = branches[i];
                var nodeGraph = this._nodeModelToNodeGraph(nodeModel, false);

                // Now, refresh UI changes ...
                nodeGraph.enableUICache(false);

                // Update shrink render state...
                nodeGraph.setBranchVisibility(true);
            }


            var relationships = mindmapModel.getRelationships();
            for (var j = 0; j < relationships.length; j++) {
                this._relationshipModelToRelationship(relationships[j]);
            }

            // Place the focus on the Central Topic
            var centralTopic = this.getModel().getCentralTopic();
            this.goToNode(centralTopic);

            // Finally, sort the map ...
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.DoLayout);
        },

        getMindmap : function() {
            return this._mindmap;
        },

        undo : function() {
            // @Todo: This is a hack...
            this._actionDispatcher._actionRunner.undo();
        },

        redo : function() {
            this._actionDispatcher._actionRunner.redo();
        },

        _nodeModelToNodeGraph : function(nodeModel, isVisible) {
            $assert(nodeModel, "Node model can not be null");
            var nodeGraph = this._buildNodeGraph(nodeModel);

            if (isVisible)
                nodeGraph.setVisibility(isVisible);

            var children = nodeModel.getChildren().slice();
            children = this._layoutManager.prepareNode(nodeGraph, children);

            var workspace = this._workspace;
            workspace.appendChild(nodeGraph);

            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if ($defined(child))
                    this._nodeModelToNodeGraph(child, false);
            }


            return nodeGraph;
        },

        _relationshipModelToRelationship : function(model) {
            $assert(model, "Node model can not be null");

            var relationship = this._buildRelationship(model);
            var sourceTopic = relationship.getSourceTopic();
            sourceTopic.connectByRelation(relationship);

            var targetTopic = relationship.getTargetTopic();
            targetTopic.connectByRelation(relationship);
            relationship.setVisibility(sourceTopic.isVisible() && targetTopic.isVisible());

            var workspace = this._workspace;
            workspace.appendChild(relationship);
            relationship.redraw();
            return relationship;
        },

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

        _buildRelationship : function (model) {
            var elem = this;

            var fromNodeId = model.getFromNode();
            var toNodeId = model.getToNode();

            var sourceTopic = null;
            var targetTopic = null;
            var dmodel = this.getModel();
            var topics = dmodel.getTopics();

            for (var i = 0; i < topics.length; i++) {
                var t = topics[i];
                if (t.getModel().getId() == fromNodeId) {
                    sourceTopic = t;
                }
                if (t.getModel().getId() == toNodeId) {
                    targetTopic = t;
                }
                if (targetTopic != null && sourceTopic != null) {
                    break;
                }
            }

            // Create node graph ...
            var relationLine = new mindplot.RelationshipLine(sourceTopic, targetTopic, model.getLineType());
            if ($defined(model.getSrcCtrlPoint())) {
                var srcPoint = model.getSrcCtrlPoint().clone();
                relationLine.setSrcControlPoint(srcPoint);
            }
            if ($defined(model.getDestCtrlPoint())) {
                var destPoint = model.getDestCtrlPoint().clone();
                relationLine.setDestControlPoint(destPoint);
            }


            relationLine.getLine().setDashed(3, 2);
            relationLine.setShowEndArrow(model.getEndArrow());
            relationLine.setShowStartArrow(model.getStartArrow());
            relationLine.setModel(model);

            //Add Listeners
            relationLine.addEvent('onfocus', function(event) {
                elem.onObjectFocusEvent(relationLine, event);
            });

            // Append it to the workspace ...
            dmodel.addRelationship(model.getId(), relationLine);

            return  relationLine;
        },

        _removeNode : function(node) {
            if (node.getTopicType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
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
                return object.getType() == mindplot.RelationshipLine.type || object.getTopicType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE
            };
            var validateError = 'Central topic can not be deleted.';

            var model = this.getModel();
            var topicsIds = model.filterTopicsIds(validateFunc, validateError);
            var relIds = model.filterRelationIds(validateFunc, validateError);

            if (topicsIds.length > 0 || relIds.length > 0) {
                this._actionDispatcher.deleteTopics(topicsIds, relIds);
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
                return topic.getShapeType() != mindplot.model.INodeModel.SHAPE_TYPE_LINE
            };
            var validateError = 'Color can not be set to line topics.';

            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBackgroundColorToTopic(topicsIds, color);
            }
        },

        changeBorderColor : function(color) {
            var validateFunc = function(topic) {
                return topic.getShapeType() != mindplot.model.INodeModel.SHAPE_TYPE_LINE
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
                return !(topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE && shape == mindplot.model.INodeModel.SHAPE_TYPE_LINE)
            };

            var validateError = 'Central Topic shape can not be changed to line figure.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeShapeTypeToTopic(topicsIds, shape);
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

        addLink : function() {
            var model = this.getModel();
            var topic = model.selectedTopic();
            topic.showLinkEditor();
        },

        addNote : function() {
            var model = this.getModel();
            var topic = model.selectedTopic();
            topic.showNoteEditor();
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
