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
        Extends:Events,
        initialize:function (options, divElement) {
            $assert(options, "options must be defined");
            $assert(options.zoom, "zoom must be defined");
            $assert(divElement, "divElement must be defined");

            // Set up i18n location ...
            mindplot.Messages.init(options.locale);

            this._options = options;

            // Set full div elem render area ...
            divElement.setStyles(options.size);

            // Dispatcher manager ...
            var commandContext = new mindplot.CommandContext(this);
            if (!$defined(options.collab) || options.collab == 'standalone') {
                this._actionDispatcher = new mindplot.StandaloneActionDispatcher(commandContext);
            } else {
                this._actionDispatcher = new mindplot.BrixActionDispatcher(commandContext);
            }

            this._actionDispatcher.addEvent("modelUpdate", function (event) {
                this.fireEvent("modelUpdate", event);
            }.bind(this));

            mindplot.ActionDispatcher.setInstance(this._actionDispatcher);
            this._model = new mindplot.DesignerModel(options);

            // Init Screen manager..
            var screenManager = new mindplot.ScreenManager(divElement);
            this._workspace = new mindplot.Workspace(screenManager, this._model.getZoom());

            // Init layout manager ...
            this._eventBussDispatcher = new mindplot.layout.EventBusDispatcher(this.getModel());

            // Register events
            if (!this.isReadOnly()) {
                this._registerEvents();
                this._dragManager = this._buildDragManager(this._workspace);
            }

            this._relPivot = new mindplot.RelationshipPivot(this._workspace, this);

            // Set editor working area ...
            this.setViewPort(options.viewPort);

            mindplot.TopicEventDispatcher.configure(this.isReadOnly());
        },

        /**
         * Deactivates the keyboard events so you can enter text into forms
         */
        deactivateKeyboard:function () {
            mindplot.DesignerKeyboard.getInstance().deactivate();
            this.deselectAll();
        },

        /**
         * Activates the keyboard events so you can enter text into forms
         */
        activateKeyboard:function () {
            mindplot.DesignerKeyboard.getInstance().activate();
        },


        _registerEvents:function () {
            // Register mouse events ...
            this._registerMouseEvents();

            // Register keyboard events ...
            mindplot.DesignerKeyboard.register(this);
        },

        addEvent:function (type, listener) {
            if (type == mindplot.TopicEvent.EDIT || type == mindplot.TopicEvent.CLICK) {
                var editor = mindplot.TopicEventDispatcher.getInstance();
                editor.addEvent(type, listener);
            } else {
                this.parent(type, listener);
            }
        },

        _registerMouseEvents:function () {
            var workspace = this._workspace;
            var screenManager = workspace.getScreenManager();

            // Initialize workspace event listeners.
            screenManager.addEvent('update', function () {
                // Topic must be set to his original state. All editors must be closed.
                var topics = this.getModel().getTopics();
                topics.forEach(function (object) {
                    object.closeEditors();
                });

                // Clean some selected nodes on event ..
                if (this._cleanScreen)
                    this._cleanScreen();

            }.bind(this));

            // Deselect on click ...
            screenManager.addEvent('click', function (event) {
                this.onObjectFocusEvent(null, event);
            }.bind(this));

            // Create nodes on double click...
            screenManager.addEvent('dblclick', function (event) {
                if (workspace.isWorkspaceEventsEnabled()) {

                    var mousePos = screenManager.getWorkspaceMousePosition(event);

                    var centralTopic = this.getModel().getCentralTopic();
                    var model = this._createChildModel(centralTopic, mousePos);
                    this._actionDispatcher.addTopic(model, centralTopic.getId());
                }
            }.bind(this));


            $(document).addEvent('mousewheel', function (event) {
                // Change mousewheel handling so we let the default
                //event happen if we are outside the container.
                var coords = screenManager.getContainer().getCoordinates();
                var isOutsideContainer = event.client.y < coords.top ||
                    event.client.y > coords.bottom ||
                    event.client.x < coords.left ||
                    event.client.x > coords.right;

                if (!isOutsideContainer) {
                    if (event.wheel > 0) {
                        this.zoomIn(1.05);
                    }
                    else {
                        this.zoomOut(1.05);
                    }
                    event.preventDefault();
                }
            }.bind(this));

        },

        _buildDragManager:function (workspace) {

            var designerModel = this.getModel();
            var dragConnector = new mindplot.DragConnector(designerModel, this._workspace);
            var dragManager = new mindplot.DragManager(workspace, this._eventBussDispatcher);
            var topics = designerModel.getTopics();

            dragManager.addEvent('startdragging', function () {
                // Enable all mouse events.
                for (var i = 0; i < topics.length; i++) {
                    topics[i].setMouseEventsEnabled(false);
                }
            });

            dragManager.addEvent('dragging', function (event, dragTopic) {
                dragTopic.updateFreeLayout(event);
                if (!dragTopic.isFreeLayoutOn(event)) {
                    // The node is being drag. Is the connection still valid ?
                    dragConnector.checkConnection(dragTopic);

                    if (!dragTopic.isVisible() && dragTopic.isConnected()) {
                        dragTopic.setVisibility(true);
                    }
                }
            });

            dragManager.addEvent('enddragging', function (event, dragTopic) {
                for (var i = 0; i < topics.length; i++) {
                    topics[i].setMouseEventsEnabled(true);
                }
                dragTopic.applyChanges(workspace);
            });

            return dragManager;
        },

        setViewPort:function (size) {
            this._workspace.setViewPort(size);
            var model = this.getModel();
            this._workspace.setZoom(model.getZoom(), true);
        },


        _buildNodeGraph:function (model, readOnly) {
            var workspace = this._workspace;

            // Create node graph ...
            var topic = mindplot.NodeGraph.create(model, {readOnly:readOnly});

            // Append it to the workspace ...
            this.getModel().addTopic(topic);

            // Add Topic events ...
            if (!readOnly) {
                // If a node had gained focus, clean the rest of the nodes ...
                topic.addEvent('mousedown', function (event) {
                    this.onObjectFocusEvent(topic, event);
                }.bind(this));

                // Register node listeners ...
                if (topic.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {

                    // Central Topic doesn't support to be dragged
                    this._dragManager.add(topic);
                }
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

            topic.addEvent('ontblur', function () {
                var topics = this.getModel().filterSelectedTopics();
                var rels = this.getModel().filterSelectedRelationships();

                if (topics.length == 0 || rels.length == 0) {
                    this.fireEvent('onblur');
                }
            }.bind(this));

            topic.addEvent('ontfocus', function () {
                var topics = this.getModel().filterSelectedTopics();
                var rels = this.getModel().filterSelectedRelationships();

                if (topics.length == 1 || rels.length == 1) {
                    this.fireEvent('onfocus');
                }
            }.bind(this));

            return  topic;
        },

        onObjectFocusEvent:function (currentObject, event) {
            // Close node editors ..
            var topics = this.getModel().getTopics();
            topics.forEach(function (topic) {
                topic.closeEditors();
            });

            var model = this.getModel();
            var objects = model.getEntities();
            objects.forEach(function (object) {
                // Disable all nodes on focus but not the current if Ctrl key isn't being pressed
                if (!$defined(event) || (!event.control && !event.meta)) {
                    if (object.isOnFocus() && object != currentObject) {
                        object.setOnFocus(false);
                    }
                }
            });

        },

        selectAll:function () {
            var model = this.getModel();
            var objects = model.getEntities();
            objects.forEach(function (object) {
                object.setOnFocus(true);
            });
        },

        deselectAll:function () {
            var objects = this.getModel().getEntities();
            objects.forEach(function (object) {
                object.setOnFocus(false);
            });
        },

        /**
         * Set the zoom of the map.
         * @param: zoom: number between 0.3 and 1.9
         */
        setZoom:function (zoom) {
            if (zoom > 1.9 || zoom < 0.3) {
                $notify($msg('ZOOM_IN_ERROR'));
                return;
            }
            this.getModel().setZoom(zoom);
            this._workspace.setZoom(zoom);
        },

        zoomOut:function (factor) {
            if (!factor)
                factor = 1.2;

            var model = this.getModel();
            var scale = model.getZoom() * factor;
            if (scale <= 1.9) {
                model.setZoom(scale);
                this._workspace.setZoom(scale);
            }
            else {
                $notify($msg('ZOOM_ERROR'));
            }

        },

        zoomIn:function (factor) {
            if (!factor)
                factor = 1.2;

            var model = this.getModel();
            var scale = model.getZoom() / factor;

            if (scale >= 0.3) {
                model.setZoom(scale);
                this._workspace.setZoom(scale);
            }
            else {
                $notify($msg('ZOOM_ERROR'));
            }
        },

        getModel:function () {
            return this._model;
        },


        shrinkSelectedBranch:function () {
            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0 || nodes.length != 1) {
                // If there are more than one node selected,
                $notify($msg('ONLY_ONE_TOPIC_MUST_BE_SELECTED_COLLAPSE'));
                return;

            }
            // Execute event ...
            var topic = nodes[0];
            this._actionDispatcher.shrinkBranch([topic.getId()], !topic.areChildrenShrunken());

        },

        createChildForSelectedNode:function () {
            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                $notify($msg('ONE_TOPIC_MUST_BE_SELECTED'));
                return;

            }
            if (nodes.length != 1) {

                // If there are more than one node selected,
                $notify($msg('ONLY_ONE_TOPIC_MUST_BE_SELECTED'));
                return;
            }

            // Add new node ...
            var parentTopic = nodes[0];
            var parentTopicId = parentTopic.getId();
            var childModel = this._createChildModel(parentTopic);

            // Execute event ...
            this._actionDispatcher.addTopic(childModel, parentTopicId, true);

        },

        _createChildModel:function (topic, mousePos) {
            // Create a new node ...
            var model = topic.getModel();
            var mindmap = model.getMindmap();
            var childModel = mindmap.createNode();

            // Create a new node ...
            var layoutManager = this._eventBussDispatcher.getLayoutManager();
            var result = layoutManager.predict(topic.getId(), null, mousePos);
            childModel.setOrder(result.order);

            var position = result.position;
            childModel.setPosition(position.x, position.y);

            return childModel;
        },

        addDraggedNode:function (event, model) {
            $assert(event, "event can not be null");
            $assert(model, "model can not be null");

            // Position far from the visual area ...
            model.setPosition(1000, 1000);

            this._actionDispatcher.addTopic(model, null, false);
            var topic = this.getModel().findTopicById(model.getId());

            // Simulate a mouse down event to start the dragging ...
            topic.fireEvent("mousedown", event);
        },

        createSiblingForSelectedNode:function () {
            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // If there are no nodes selected,
                $notify($msg('ONE_TOPIC_MUST_BE_SELECTED'));
                return;

            }
            if (nodes.length > 1) {
                // If there are more than one node selected,
                $notify($msg('ONLY_ONE_TOPIC_MUST_BE_SELECTED'));
                return;
            }

            var topic = nodes[0];
            if (topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                // Central topic doesn't have siblings ...
                this.createChildForSelectedNode();

            } else {
                var parentTopic = topic.getOutgoingConnectedTopic();
                var siblingModel = this._createSiblingModel(topic);

                // Hack: if parent is central topic, add node below not on opposite side.
                // This should be done in the layout
                if (parentTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                    siblingModel.setOrder(topic.getOrder() + 2);
                }

                var parentTopicId = parentTopic.getId();
                this._actionDispatcher.addTopic(siblingModel, parentTopicId, true);
            }
        },

        _createSiblingModel:function (topic) {
            var result = null;
            var parentTopic = topic.getOutgoingConnectedTopic();
            if (parentTopic != null) {

                // Create a new node ...
                var model = topic.getModel();
                var mindmap = model.getMindmap();
                result = mindmap.createNode();

                // Create a new node ...
                var order = topic.getOrder() + 1;
                result.setOrder(order);
                result.setPosition(10, 10);  // Set a dummy pisition ...
            }
            return result;
        },

        showRelPivot:function (event) {

            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // This could not happen ...
                $notify("Could not create relationship. Parent relationship topic must be selected first.");
                return;
            }

            // Current mouse position ....
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);

            // create a connection ...
            this._relPivot.start(nodes[0], pos);
        },

        connectByRelation:function (sourceTopic, targetTopic) {
            $assert(sourceTopic, "sourceTopic can not be null");
            $assert(targetTopic, "targetTopic can not be null");

            // Create a new topic model ...
            var mindmap = this.getMindmap();
            var model = mindmap.createRelationship(sourceTopic.getModel().getId(), targetTopic.getModel().getId());
            this._actionDispatcher.connectByRelation(model);
        },

        needsSave:function () {
            //@Todo: Review all this ...
            return this._actionDispatcher._actionRunner.hasBeenChanged();
        },


        getMindmapProperties:function () {
            return   {zoom:this.getModel().getZoom()};
        },

        loadMap:function (mindmapModel) {
            $assert(mindmapModel, "mindmapModel can not be null");
            this._mindmap = mindmapModel;

//            try {
            // Init layout manager ...
            var size = {width:25, height:25};
            var layoutManager = new mindplot.layout.LayoutManager(mindmapModel.getCentralTopic().getId(), size);
            layoutManager.addEvent('change', function (event) {
                var id = event.getId();
                var topic = this.getModel().findTopicById(id);
                topic.setPosition(event.getPosition());
                topic.setOrder(event.getOrder());
            }.bind(this));
            this._eventBussDispatcher.setLayoutManager(layoutManager);


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

            // Place the focus on the Central Topic
            var centralTopic = this.getModel().getCentralTopic();
            this.goToNode(centralTopic);

            // Finally, sort the map ...
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.DoLayout);

            this.fireEvent('loadSuccess');
//            } catch(e) {
//                this.fireEvent('loadError', e);
//            }
        },

        getMindmap:function () {
            return this._mindmap;
        },

        undo:function () {
            // @Todo: This is a hack...
            this._actionDispatcher._actionRunner.undo();
        },

        redo:function () {
            this._actionDispatcher._actionRunner.redo();
        },

        isReadOnly:function () {
            return this._options.readOnly;
        },

        _nodeModelToNodeGraph:function (nodeModel, isVisible) {
            $assert(nodeModel, "Node model can not be null");
            var children = nodeModel.getChildren().slice();
            children = children.sort(function (a, b) {
                return a.getOrder() - b.getOrder()
            });

            var nodeGraph = this._buildNodeGraph(nodeModel, this.isReadOnly());

            if (isVisible) {
                nodeGraph.setVisibility(isVisible);
            }

            this._workspace.appendChild(nodeGraph);
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if ($defined(child))
                    this._nodeModelToNodeGraph(child, false);
            }

            return nodeGraph;
        },

        _relationshipModelToRelationship:function (model) {
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

        createRelationship:function (model) {
            this._mindmap.addRelationship(model);
            return this._relationshipModelToRelationship(model);
        },

        _deleteRelationship:function (relationship) {
            var sourceTopic = relationship.getSourceTopic();
            sourceTopic.deleteRelationship(relationship);

            var targetTopic = relationship.getTargetTopic();
            targetTopic.deleteRelationship(relationship);

            this._workspace.removeChild(relationship);

            this.getModel().removeRelationship(relationship);
        },

        _buildRelationship:function (model) {
            var dmodel = this.getModel();

            var sourceTopicId = model.getFromNode();
            var sourceTopic = dmodel.findTopicById(sourceTopicId);

            var targetTopicId = model.getToNode();
            var targetTopic = dmodel.findTopicById(targetTopicId);

            // Build relationship line ....
            var relationship = new mindplot.Relationship(sourceTopic, targetTopic, model);
            relationship.addEvent('onfocus', function (event) {
                this.onObjectFocusEvent(relationship, event);
            }.bind(this));

            // Append it to the workspace ...
            dmodel.addRelationship(relationship);
            return  relationship;
        },

        _removeTopic:function (node) {
            if (node.getTopicType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                var parent = node._parent;
                node.disconnect(this._workspace);

                //remove children
                while (node.getChildren().length > 0) {
                    this._removeTopic(node.getChildren()[0]);
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

        deleteSelectedEntities:function () {

            var topics = this.getModel().filterSelectedTopics();
            var relation = this.getModel().filterSelectedRelationships();
            if (topics.length <= 0 && relation.length <= 0) {
                // If there are more than one node selected,
                $notify($msg('ENTITIES_COULD_NOT_BE_DELETED'));
                return;
            }

            // Filter the lists ...
            var validateFunc = function (object) {
                return object.getType() == mindplot.Relationship.type || object.getTopicType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE
            };
            var validateError = $msg('CENTRAL_TOPIC_CAN_NOT_BE_DELETED');
            var model = this.getModel();
            var topicIds = model.filterTopicsIds(validateFunc, validateError);
            var relIds = model.filterSelectedRelationships().map(function (rel) {
                return rel.getId();
            });

            // Finally delete the topics ...
            if (topicIds.length > 0 || relIds.length > 0) {
                this._actionDispatcher.deleteEntities(topicIds, relIds);
            }

        },

        changeFontFamily:function (font) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontFamilyToTopic(topicsIds, font);

            }
        },

        changeFontStyle:function () {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontStyleToTopic(topicsIds);
            }
        },

        changeFontColor:function (color) {
            $assert(color, "color can not be null");

            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontColorToTopic(topicsIds, color);
            }
        },

        changeBackgroundColor:function (color) {

            var validateFunc = function (topic) {
                return topic.getShapeType() != mindplot.model.TopicShape.LINE;
            };
            var validateError = 'Color can not be set to line topics.';

            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBackgroundColorToTopic(topicsIds, color);
            }
        },

        changeBorderColor:function (color) {
            var validateFunc = function (topic) {
                return topic.getShapeType() != mindplot.model.TopicShape.LINE;
            };
            var validateError = 'Color can not be set to line topics.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBorderColorToTopic(topicsIds, color);
            }
        },

        changeFontSize:function (size) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontSizeToTopic(topicsIds, size);
            }
        },

        changeTopicShape:function (shape) {
            var validateFunc = function (topic) {
                return !(topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE && shape == mindplot.model.TopicShape.LINE)
            };

            var validateError = 'Central Topic shape can not be changed to line figure.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeShapeTypeToTopic(topicsIds, shape);
            }
        },

        changeFontWeight:function () {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontWeightToTopic(topicsIds);
            }
        },

        addIconType:function (iconType) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.addFeatureToTopic(topicsIds[0], mindplot.TopicFeature.Icon.id, {id:iconType});
            }
        },

        addLink:function () {
            var model = this.getModel();
            var topic = model.selectedTopic();
            topic.showLinkEditor();
        },

        addNote:function () {
            var model = this.getModel();
            var topic = model.selectedTopic();
            topic.showNoteEditor();
        },

        goToNode:function (node) {
            node.setOnFocus(true);
            this.onObjectFocusEvent(node);
        },

        getWorkSpace:function () {
            return this._workspace;
        }
    }
);
