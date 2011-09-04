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

mindplot.CentralTopic = new Class({

    Extends:mindplot.Topic,
    initialize: function(model, options) {
        this.parent(model, options);
    },

    _registerEvents : function() {
        this.parent();

        // This disable the drag of the central topic. But solves the problem of deselecting the nodes when the screen is clicked.
        this.addEvent('mousedown', function(event) {
            event.stopPropagation();
        });
    },

    workoutIncomingConnectionPoint : function(sourcePosition) {
        return this.getPosition();
    },

    _getInnerPadding : function() {
        return 11;
    },

    getTopicType : function() {
        return mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE;
    },

    setCursor : function(type) {
        type = (type == 'move') ? 'default' : type;
        this.parent(type);
    },

    isConnectedToCentralTopic : function() {
        return false;
    },

    createChildModel : function(prepositionate) {
        // Create a new node ...
        var model = this.getModel();
        var mindmap = model.getMindmap();
        var childModel = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);

        if (prepositionate) {
            if (!$defined(this.___siblingDirection)) {
                this.___siblingDirection = 1;
            }

            // Position following taking into account this internal flag ...
            if (this.___siblingDirection == 1) {

                childModel.setPosition(150, 0);
            } else {
                childModel.setPosition(-150, 0);
            }
            this.___siblingDirection = -this.___siblingDirection;
        }
        // Create a new node ...
        childModel.setOrder(0);

        return childModel;
    },

    _defaultShapeType : function() {
        return  mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT;
    },


    updateTopicShape : function() {

    },

    _updatePositionOnChangeSize : function(oldSize, newSize, updatePosition) {

        // Center main topic ...
        var zeroPoint = new core.Point(0, 0);
        this.setPosition(zeroPoint);
    },

    _defaultText : function() {
        return "Central Topic";
    },

    _defaultBackgroundColor : function() {
        return "#f7f7f7";
    },

    _defaultBorderColor : function() {
        return "#023BB9";
    },

    _defaultFontStyle : function() {
        return {
            font:"Verdana",
            size: 10,
            style:"normal",
            weight:"bold",
            color:"#023BB9"
        };
    }
});