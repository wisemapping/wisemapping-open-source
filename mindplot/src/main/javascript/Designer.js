/*
 *    Copyright [2015] [wisemapping]
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

mindplot.Designer = new Class(/** @lends Designer */{
        Extends: mindplot.Events,
        /**
         * @constructs
         * @param {Object} options
         * @param {HTMLElement} divElement
         * @extends mindplot.Events
         */
        initialize: function (options, divElement) {
            $assert(options, "options must be defined");
            $assert(options.zoom, "zoom must be defined");
            $assert(options.size, "size must be defined");
            $assert(divElement, "divElement must be defined");

            // Set up i18n location ...
            mindplot.Messages.init(options.locale);

            this._options = options;

            // Set full div elem render area ...
            divElement.css(options.size);

            // Dispatcher manager ...
            var commandContext = new mindplot.CommandContext(this);
            this._actionDispatcher = new mindplot.StandaloneActionDispatcher(commandContext);

            var me = this;
            this._actionDispatcher.addEvent("modelUpdate", function (event) {
                me.fireEvent("modelUpdate", event);
            });

            mindplot.ActionDispatcher.setInstance(this._actionDispatcher);
            this._model = new mindplot.DesignerModel(options);

            // Init Screen manager..
            var screenManager = new mindplot.ScreenManager(divElement);
            this._workspace = new mindplot.Workspace(screenManager, this._model.getZoom());

            // Init layout manager ...
            this._eventBussDispatcher = new mindplot.layout.EventBusDispatcher(this.getModel());

            // Register events
            if (!this.isReadOnly()) {
                // Register mouse events ...
                this._registerMouseEvents();

                // Register keyboard events ...
                mindplot.DesignerKeyboard.register(this);

                this._dragManager = this._buildDragManager(this._workspace);
            }
            this._registerWheelEvents();

            this._relPivot = new mindplot.RelationshipPivot(this._workspace, this);

            // Set editor working area ...
            this.setViewPort(options.viewPort);

            mindplot.TopicEventDispatcher.configure(this.isReadOnly());
            this._clipboard = [];
        },

        /**
         * @private
         */
        _registerWheelEvents: function () {
            var zoomFactor = 1.006;
            var me = this;
            // Zoom In and Zoom Out must active event
            $(document).on('mousewheel', function (event) {
                if (event.deltaY > 0) {
                    me.zoomIn(zoomFactor);
                } else {
                    me.zoomOut(zoomFactor);
                }
                event.preventDefault();
            });
        },

        /**
         * @param {String} type the event type
         * @param {Function} listener
         * forwards to the TopicEventDispatcher or the parent Events class, depending on the type
         */
        addEvent: function (type, listener) {
            if (type == mindplot.TopicEvent.EDIT || type == mindplot.TopicEvent.CLICK) {
                var editor = mindplot.TopicEventDispatcher.getInstance();
                editor.addEvent(type, listener);
            } else {
                this.parent(type, listener);
            }
        },

        /**
         * @private
         */
        _registerMouseEvents: function () {
            var workspace = this._workspace;
            var screenManager = workspace.getScreenManager();
            var me = this;
            // Initialize workspace event listeners.
            screenManager.addEvent('update', function () {
                // Topic must be set to his original state. All editors must be closed.
                var topics = me.getModel().getTopics();
                _.each(topics, function (object) {
                    object.closeEditors();
                });

                // Clean some selected nodes on event ..
                if (me._cleanScreen)
                    me._cleanScreen();
            });

            // Deselect on click ...
            screenManager.addEvent('click', function (event) {
                me.onObjectFocusEvent(null, event);
            });

            // Create nodes on double click...
            screenManager.addEvent('dblclick', function (event) {
                if (workspace.isWorkspaceEventsEnabled()) {
                    var mousePos = screenManager.getWorkspaceMousePosition(event);
                    var centralTopic = me.getModel().getCentralTopic();
                    var model = me._createChildModel(centralTopic, mousePos);
                    this._actionDispatcher.addTopics([model], [centralTopic.getId()]);
                }
            }.bind(this));

            // Register mouse drag and drop event ...
            function noopHandler(evt) {
                evt.stopPropagation();
                evt.preventDefault();
            }
        },

        /**
         * @private
         * @param {mindplot.Workspace} workspace
         * @return {mindplot.DragManager} the new dragManager for the workspace with events
         * registered
         */
        _buildDragManager: function (workspace) {

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

        /**
         * @param {{width:Number, height:Number}} size
         * sets width and height of the workspace
         */
        setViewPort: function (size) {
            this._workspace.setViewPort(size);
            var model = this.getModel();
            this._workspace.setZoom(model.getZoom(), true);
        },

        /**
         * @private
         * @param {mindplot.model.NodeModel} model
         * @param {Boolean} readOnly
         * @return {mindplot.CentralTopic|mindplot.MainTopic} the topic to the given model,
         * connected, added to the drag manager, with events registered - complying type & read mode
         */
        _buildNodeGraph: function (model, readOnly) {
            
            // Create node graph ...
            var topic = mindplot.NodeGraph.create(model, {readOnly: readOnly});
            this.getModel().addTopic(topic);
            var me = this;
            // Add Topic events ...
            if (!readOnly) {
                // If a node had gained focus, clean the rest of the nodes ...
                topic.addEvent('mousedown', function (event) {
                    me.onObjectFocusEvent(topic, event);
                });

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
                topic.connectTo(targetTopic, this._workspace);
            }

            topic.addEvent('ontblur', function () {
                var topics = me.getModel().filterSelectedTopics();
                var rels = me.getModel().filterSelectedRelationships();

                if (topics.length == 0 || rels.length == 0) {
                    me.fireEvent('onblur');
                }
            });

            topic.addEvent('ontfocus', function () {
                var topics = me.getModel().filterSelectedTopics();
                var rels = me.getModel().filterSelectedRelationships();

                if (topics.length == 1 || rels.length == 1) {
                    me.fireEvent('onfocus');
                }
            });

            return topic;
        },

        /**
         * @param {?mindplot.Topic} currentObject
         * @param {Event=} event
         * sets focus to the given currentObject and removes it from any other objects if not
         * triggered with Ctrl pressed
         */
        onObjectFocusEvent: function (currentObject, event) {
            // Close node editors ..
            var topics = this.getModel().getTopics();
            _.each(topics, function (topic) {
                topic.closeEditors();
            });

            var model = this.getModel();
            var objects = model.getEntities();
            _.each(objects, function (object) {
                // Disable all nodes on focus but not the current if Ctrl key isn't being pressed
                if (!$defined(event) || (!event.ctrlKey && !event.metaKey)) {
                    if (object.isOnFocus() && object != currentObject) {
                        object.setOnFocus(false);
                    }
                }
            });

        },

        /** sets focus to all model entities, i.e. relationships and topics */
        selectAll: function () {
            var model = this.getModel();
            var objects = model.getEntities();
            _.each(objects, function (object) {
                object.setOnFocus(true);
            });
        },

        /** removes focus from all model entities, i.e. relationships and topics */
        deselectAll: function () {
            var objects = this.getModel().getEntities();
            _.each(objects, function (object) {
                object.setOnFocus(false);
            });
        },

        /**
         * Set the zoom of the map
         * @param {Number} zoom number between 0.3 and 1.9
         */
        setZoom: function (zoom) {
            if (zoom > 1.9 || zoom < 0.3) {
                $notify($msg('ZOOM_IN_ERROR'));
                return;
            }
            this.getModel().setZoom(zoom);
            this._workspace.setZoom(zoom);
        },

        /**
         * @param {Number=} factor
         * zoom out by the given factor, or 1.2, if undefined
         */
        zoomOut: function (factor) {
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

        /**
         * @param {Number=} factor
         * zoom in by the given factor, or 1.2, if undefined
         */
        zoomIn: function (factor) {
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

        /** copy selected topics to a private clipboard */
        copyToClipboard: function () {
            var topics = this.getModel().filterSelectedTopics();
            if (topics.length <= 0) {
                // If there are more than one node selected,
                $notify($msg('AT_LEAST_ONE_TOPIC_MUST_BE_SELECTED'));
                return;
            }

            // Exclude central topic ..
            topics = topics.filter(function (topic) {
                return !topic.isCentralTopic();
            });

            this._clipboard = topics.map(function (topic) {
                var nodeModel = topic.getModel().deepCopy();

                // Change position to make the new topic evident...
                var pos = nodeModel.getPosition();
                nodeModel.setPosition(pos.x + (60 * Math.sign(pos.x)), pos.y + 30);

                return nodeModel;
            });

            $notify($msg('SELECTION_COPIED_TO_CLIPBOARD'));
        },

        /** paste clipboard contents to the mindmap */
        pasteClipboard: function () {
            if (this._clipboard.length == 0) {
                $notify($msg('CLIPBOARD_IS_EMPTY'));
                return;
            }
            this._actionDispatcher.addTopics(this._clipboard);
            this._clipboard = [];
        },

        /** @return {mindplot.DesignerModel} model */
        getModel: function () {
            return this._model;
        },

        /** collapse the subtree of the selected topic */
        shrinkSelectedBranch: function () {
            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0 || nodes.length != 1) {
                // If there are more than one node selected,
                $notify($msg('ONLY_ONE_TOPIC_MUST_BE_SELECTED_COLLAPSE'));
                return;

            }
            // Execute event ...
            var topic = nodes[0];
            if (topic.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                this._actionDispatcher.shrinkBranch([topic.getId()], !topic.areChildrenShrunken());
            }
        },

        /** create a NodeModel for the selected node's child and add it via the ActionDispatcher */
        createChildForSelectedNode: function () {
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
            this._actionDispatcher.addTopics([childModel], [parentTopicId]);

        },

        /**
         * @private
         */
        _copyNodeProps: function (sourceModel, targetModel) {

            // I don't copy the font size if the target is the source is the central topic.
            if (sourceModel.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                var fontSize = sourceModel.getFontSize();
                if (fontSize) {
                    targetModel.setFontSize(fontSize)
                }
            }

            var fontFamily = sourceModel.getFontFamily();
            if (fontFamily) {
                targetModel.setFontFamily(fontFamily)
            }

            var fontColor = sourceModel.getFontColor();
            if (fontColor) {
                targetModel.setFontColor(fontColor)
            }

            var fontWeight = sourceModel.getFontWeight();
            if (fontWeight) {
                targetModel.setFontWeight(fontWeight)
            }

            var fontStyle = sourceModel.getFontStyle();
            if (fontStyle) {
                targetModel.setFontStyle(fontStyle)
            }

            var shape = sourceModel.getShapeType();
            if (shape) {
                targetModel.setShapeType(shape)
            }

            var borderColor = sourceModel.getBorderColor();
            if (borderColor) {
                targetModel.setBorderColor(borderColor)
            }

            var backgroundColor = sourceModel.getBackgroundColor();
            if (backgroundColor) {
                targetModel.setBackgroundColor(backgroundColor)
            }
        },

        /**
         * @private
         * @param {mindplot.Topic} topic the parent topic of the child to create the NodeModel for
         * @param {core.Point} mousePos the mouse position
         * @return {mindplot.NodeModel} the node model for the new child
         */
        _createChildModel: function (topic, mousePos) {
            // Create a new node ...
            var parentModel = topic.getModel();
            var mindmap = parentModel.getMindmap();
            var childModel = mindmap.createNode();

            // Create a new node ...
            var layoutManager = this._eventBussDispatcher.getLayoutManager();
            var result = layoutManager.predict(topic.getId(), null, mousePos);
            childModel.setOrder(result.order);

            var position = result.position;
            childModel.setPosition(position.x, position.y);

            this._copyNodeProps(parentModel, childModel);

            return childModel;
        },

        /**
         * @param {Events} event
         * @param {mindplot.model.NodeModel} model
         * @todo not used
         */
        addDraggedNode: function (event, model) {
            $assert(event, "event can not be null");
            $assert(model, "model can not be null");

            // Position far from the visual area ...
            model.setPosition(1000, 1000);

            this._actionDispatcher.addTopics([model]);
            var topic = this.getModel().findTopicById(model.getId());

            // Simulate a mouse down event to start the dragging ...
            topic.fireEvent("mousedown", event);
        },

        /**
         * creates a sibling or child node of the selected node, if the selected node is the
         * central topic
         */
        createSiblingForSelectedNode: function () {
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
            if (!topic.getOutgoingConnectedTopic()) { // Central topic and isolated topics ....
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
                this._actionDispatcher.addTopics([siblingModel], [parentTopicId]);
            }
        },

        /**
         * @private
         * @param {mindplot.Topic} topic the topic to create the sibling to
         * @return {mindplot.NodeModel} the node model of the sibling
         */
        _createSiblingModel: function (topic) {
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
                result.setPosition(10, 10);  // Set a dummy position ...
            }

            this._copyNodeProps(model, result);

            return result;
        },

        /**
         * @param {Event} event
         */
        showRelPivot: function (event) {

            var nodes = this.getModel().filterSelectedTopics();
            if (nodes.length <= 0) {
                // This could not happen ...
                $notify($msg('RELATIONSHIP_COULD_NOT_BE_CREATED'));
                return;
            }

            // Current mouse position ....
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);

            // create a connection ...
            this._relPivot.start(nodes[0], pos);
        },

        /** @return {{zoom:Number}} the zoom */
        getMindmapProperties: function () {
            var model = this.getModel();
            return {zoom: model.getZoom()};
        },

        /**
         * @param {mindplot.Mindmap} mindmapModel
         * @throws will throw an error if mindmapModel is null or undefined
         */
        loadMap: function (mindmapModel) {
            $assert(mindmapModel, "mindmapModel can not be null");
            this._mindmap = mindmapModel;

            // Init layout manager ...
            var size = {width: 25, height: 25};
            var layoutManager = new mindplot.layout.LayoutManager(mindmapModel.getCentralTopic().getId(), size);
            var me = this;
            layoutManager.addEvent('change', function (event) {
                var id = event.getId();
                var topic = me.getModel().findTopicById(id);
                topic.setPosition(event.getPosition());
                topic.setOrder(event.getOrder());
            });
            this._eventBussDispatcher.setLayoutManager(layoutManager);


            // Building node graph ...
            var branches = mindmapModel.getBranches();
            for (var i = 0; i < branches.length; i++) {
                // NodeModel -> NodeGraph ...
                var nodeModel = branches[i];
                var nodeGraph = this.nodeModelToNodeGraph(nodeModel);

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
        },

        /** */
        getMindmap: function () {
            return this._mindmap;
        },

        /** */
        undo: function () {
            // @Todo: This is a hack...
            this._actionDispatcher._actionRunner.undo();
        },

        /** */
        redo: function () {
            this._actionDispatcher._actionRunner.redo();
        },

        /** */
        isReadOnly: function () {
            return this._options.readOnly;
        },

        /**
         * @param {mindplot.model.NodeModel} nodeModel
         * @return {mindplot.Topic} the topic (extends mindplot.NodeGraph) created to the model
         */
        nodeModelToNodeGraph: function (nodeModel) {
            $assert(nodeModel, "Node model can not be null");
            var children = nodeModel.getChildren().slice();
            children = children.sort(function (a, b) {
                return a.getOrder() - b.getOrder()
            });

            var nodeGraph = this._buildNodeGraph(nodeModel, this.isReadOnly());
            nodeGraph.setVisibility(false);

            this._workspace.append(nodeGraph);
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if ($defined(child))
                    this.nodeModelToNodeGraph(child);
            }

            return nodeGraph;
        },

        /**
         * @private
         * @param {mindplot.model.RelationshipModel} model
         * @return {mindplot.Relationship} the relationship created to the model
         * @throws will throw an error if model is null or undefined
         */
        _relationshipModelToRelationship: function (model) {
            $assert(model, "Node model can not be null");

            var result = this._buildRelationshipShape(model);

            var sourceTopic = result.getSourceTopic();
            sourceTopic.addRelationship(result);

            var targetTopic = result.getTargetTopic();
            targetTopic.addRelationship(result);

            result.setVisibility(sourceTopic.isVisible() && targetTopic.isVisible());

            this._workspace.append(result);
            return result;
        },

        /**
         * @param {mindplot.model.RelationshipModel} model
         * @return {mindplot.Relationship} the relationship added to the mindmap
         */
        addRelationship: function (model) {
            var mindmap = this.getMindmap();
            mindmap.addRelationship(model);
            return this._relationshipModelToRelationship(model);
        },

        /**
         * deletes the relationship from the linked topics, DesignerModel, Workspace and Mindmap
         * @param {mindplot.Relationship} rel the relationship to delete
         */
        deleteRelationship: function (rel) {
            var sourceTopic = rel.getSourceTopic();
            sourceTopic.deleteRelationship(rel);

            var targetTopic = rel.getTargetTopic();
            targetTopic.deleteRelationship(rel);

            this.getModel().removeRelationship(rel);
            this._workspace.removeChild(rel);

            var mindmap = this.getMindmap();
            mindmap.deleteRelationship(rel.getModel());
        },

        /**
         * @private
         * @param {mindplot.model.RelationshipModel} model
         * @return {mindplot.Relationship} the new relationship with events registered
         * @throws will throw an error if the target topic cannot be found
         */
        _buildRelationshipShape: function (model) {
            var dmodel = this.getModel();

            var sourceTopicId = model.getFromNode();
            var sourceTopic = dmodel.findTopicById(sourceTopicId);

            var targetTopicId = model.getToNode();
            var targetTopic = dmodel.findTopicById(targetTopicId);
            $assert(targetTopic, "targetTopic could not be found:" + targetTopicId + dmodel.getTopics().map(function (e) {
                return e.getId()
            }));

            // Build relationship line ....
            var result = new mindplot.Relationship(sourceTopic, targetTopic, model);
            var me = this;

            result.addEvent('ontblur', function () {
                var topics = me.getModel().filterSelectedTopics();
                var rels = me.getModel().filterSelectedRelationships();

                if (topics.length == 0 || rels.length == 0) {
                    me.fireEvent('onblur');
                }
            });

            result.addEvent('ontfocus', function () {
                var topics = me.getModel().filterSelectedTopics();
                var rels = me.getModel().filterSelectedRelationships();

                if (topics.length == 1 || rels.length == 1) {
                    me.fireEvent('onfocus');
                }
            });

            // Append it to the workspace ...
            dmodel.addRelationship(result);

            return result;
        },

        /**
         * @param {mindplot.Topic} node the topic to remove
         * removes the given topic and its children from Workspace, DesignerModel and NodeModel
         */
        removeTopic: function (node) {
            if (!node.isCentralTopic()) {
                var parent = node._parent;
                node.disconnect(this._workspace);

                //remove children
                while (node.getChildren().length > 0) {
                    this.removeTopic(node.getChildren()[0]);
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

        /**
         * @private
         */
        _resetEdition: function () {
            var screenManager = this._workspace.getScreenManager();
            screenManager.fireEvent("update");
            screenManager.fireEvent("mouseup");
            this._relPivot.dispose();
        },

        /** */
        deleteSelectedEntities: function () {
            // Is there some action in progress ?.
            this._resetEdition();

            var topics = this.getModel().filterSelectedTopics();
            var relation = this.getModel().filterSelectedRelationships();
            if (topics.length <= 0 && relation.length <= 0) {
                // If there are more than one node selected,
                $notify($msg('ENTITIES_COULD_NOT_BE_DELETED'));
                return;
            } else if (topics.length == 1 && topics[0].isCentralTopic()) {
                $notify($msg('CENTRAL_TOPIC_CAN_NOT_BE_DELETED'));
                return;
            }

            // If the central topic has been selected, I must filter ir
            var topicIds = topics.filter(function (topic) {
                return !topic.isCentralTopic();
            }).map(function (topic) {
                return topic.getId()
            });


            var relIds = relation.map(function (rel) {
                return rel.getId();
            });

            // Finally delete the topics ...
            if (topicIds.length > 0 || relIds.length > 0) {
                this._actionDispatcher.deleteEntities(topicIds, relIds);
            }

        },

        /** */
        changeFontFamily: function (font) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontFamilyToTopic(topicsIds, font);

            }
        },

        /** */
        changeFontStyle: function () {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontStyleToTopic(topicsIds);
            }
        },

        /** */
        changeFontColor: function (color) {
            $assert(color, "color can not be null");

            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontColorToTopic(topicsIds, color);
            }
        },

        /** */
        changeBackgroundColor: function (color) {

            var validateFunc = function (topic) {
                return topic.getShapeType() != mindplot.model.TopicShape.LINE;
            };
            var validateError = 'Color can not be set to line topics.';

            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBackgroundColorToTopic(topicsIds, color);
            }
        },

        /** */
        changeBorderColor: function (color) {
            var validateFunc = function (topic) {
                return topic.getShapeType() != mindplot.model.TopicShape.LINE;
            };
            var validateError = 'Color can not be set to line topics.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeBorderColorToTopic(topicsIds, color);
            }
        },

        /** */
        changeFontSize: function (size) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontSizeToTopic(topicsIds, size);
            }
        },

        /** */
        changeTopicShape: function (shape) {
            var validateFunc = function (topic) {
                return !(topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE && shape == mindplot.model.TopicShape.LINE)
            };

            var validateError = 'Central Topic shape can not be changed to line figure.';
            var topicsIds = this.getModel().filterTopicsIds(validateFunc, validateError);
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeShapeTypeToTopic(topicsIds, shape);
            }
        },

        /** */
        changeFontWeight: function () {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.changeFontWeightToTopic(topicsIds);
            }
        },

        /** */
        addIconType: function (iconType) {
            var topicsIds = this.getModel().filterTopicsIds();
            if (topicsIds.length > 0) {
                this._actionDispatcher.addFeatureToTopic(topicsIds[0], mindplot.TopicFeature.Icon.id, {id: iconType});
            }
        },

        /**
         * lets the selected topic open the link editor where the user can define or modify an
         * existing link
         */
        addLink: function () {
            var model = this.getModel();
            var topic = model.selectedTopic();
            if (topic) {
                topic.showLinkEditor();
                this.onObjectFocusEvent();
            }
        },

        /** */
        addNote: function () {
            var model = this.getModel();
            var topic = model.selectedTopic();
            if (topic) {
                topic.showNoteEditor();
                this.onObjectFocusEvent();
            }
        },

        /**
         * @param {mindplot.Topic} node
         * sets the focus to the given node
         */
        goToNode: function (node) {
            node.setOnFocus(true);
            this.onObjectFocusEvent(node);
        },

        /** @return {mindplot.Workspace} */
        getWorkSpace: function () {
            return this._workspace;
        }
    }
);
