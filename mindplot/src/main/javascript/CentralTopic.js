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
    initialize:function (model, options) {
        this.parent(model, options);
    },

    _registerEvents:function () {
        this.parent();

        // This disable the drag of the central topic. But solves the problem of deselecting the nodes when the screen is clicked.
        this.addEvent('mousedown', function (event) {
            event.stopPropagation();
        });
    },

    workoutIncomingConnectionPoint:function () {
        return this.getPosition();
    },

    _getInnerPadding:function () {
        return 11;
    },

    getTopicType:function () {
        return mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE;
    },

    setCursor:function (type) {
        type = (type == 'move') ? 'default' : type;
        this.parent(type);
    },

    isConnectedToCentralTopic:function () {
        return false;
    },


    _defaultShapeType:function () {
        return  mindplot.model.TopicShape.ROUNDED_RECT;
    },


    updateTopicShape:function () {

    },

    _updatePositionOnChangeSize:function () {

        // Center main topic ...
        var zeroPoint = new core.Point(0, 0);
        this.setPosition(zeroPoint);
    },

    _defaultText:function () {
        return $msg('CENTRAL_TOPIC');
    },

    _defaultBackgroundColor:function () {
        return "rgb(80,157,192)";
    },

    _defaultBorderColor:function () {
        return "rgb(57,113,177)";
    },

    _defaultFontStyle:function () {
        return {
            font:"Verdana",
            size:10,
            style:"normal",
            weight:"bold",
            color:"#ffffff"
        };
    },

    getShrinkConnector:function () {
        return null;
    },

    workoutOutgoingConnectionPoint:function (targetPosition) {
        $assert(targetPosition, 'targetPoint can not be null');
        var pos = this.getPosition();
        var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, pos);
        var size = this.getSize();
        return   mindplot.util.Shape.calculateRectConnectionPoint(pos, size, !isAtRight);
    }
});