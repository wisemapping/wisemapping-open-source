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
mindplot.TopicStyle = new Class({
    Static: {
        _getStyles: function (topic) {
            $assert(topic, "topic can not be null");

            var result;
            if (topic.isCentralTopic()) {
                result = mindplot.TopicStyle.STYLES.CENTRAL_TOPIC;
            } else {
                var targetTopic = topic.getOutgoingConnectedTopic();
                if ($defined(targetTopic)) {
                    if (targetTopic.isCentralTopic()) {
                        result = mindplot.TopicStyle.STYLES.MAIN_TOPIC;
                    } else {
                        result = mindplot.TopicStyle.STYLES.SUB_TOPIC;
                    }
                } else {
                    result = mindplot.TopicStyle.STYLES.ISOLATED_TOPIC;
                }
            }
            return result;
        },

        defaultText: function (topic) {
            var msgKey = this._getStyles(topic).msgKey;
            return $msg(msgKey);
        },

        defaultFontStyle: function (topic) {
            return this._getStyles(topic).fontStyle;
        },

        defaultBackgroundColor: function (topic) {
            return this._getStyles(topic).backgroundColor;
        },

        defaultBorderColor: function (topic) {
            return this._getStyles(topic).borderColor;
        },

        getInnerPadding: function (topic) {
            return this._getStyles(topic).innerPadding;
        },

        defaultShapeType: function (topic) {
            return this._getStyles(topic).shapeType;
        }

    }
});

mindplot.TopicStyle.STYLES =
{
    CENTRAL_TOPIC: {
        borderColor: 'rgb(57,113,177)',
        backgroundColor: 'rgb(80,157,192)',
        fontStyle: {
            font: "Verdana",
            size: 10,
            style: "normal",
            weight: "bold",
            color: "#ffffff"
        },
        msgKey: 'CENTRAL_TOPIC',
        innerPadding: 11,
        shapeType: mindplot.model.TopicShape.ROUNDED_RECT
    },

    MAIN_TOPIC: {
        borderColor: 'rgb(2,59,185)',
        backgroundColor: 'rgb(224,229,239)',
        fontStyle: {
            font: "Arial",
            size: 8,
            style: "normal",
            weight: "normal",
            color: "rgb(82,92,97)"
        },
        msgKey: 'MAIN_TOPIC',
        innerPadding: 3,
        shapeType: mindplot.model.TopicShape.LINE

    },

    SUB_TOPIC: {
        borderColor: 'rgb(2,59,185)',
        backgroundColor: 'rgb(224,229,239)',
        fontStyle: {
            font: "Arial",
            size: 6,
            style: "normal",
            weight: "normal",
            color: "rgb(82,92,97)"
        },
        msgKey: 'SUB_TOPIC',
        innerPadding: 3,
        shapeType: mindplot.model.TopicShape.LINE
    },

    ISOLATED_TOPIC: {
        borderColor: 'rgb(2,59,185)',
        backgroundColor: 'rgb(224,229,239)',
        fontStyle: {
            font: "Verdana",
            size: 8,
            style: "normal",
            weight: "normal",
            color: "rgb(82,92,97)"
        },
        msgKey: 'ISOLATED_TOPIC',
        innerPadding: 4,
        shapeType: mindplot.model.TopicShape.LINE
    }
};

