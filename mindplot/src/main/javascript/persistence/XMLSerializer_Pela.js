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

/**
 * @class
 */
mindplot.persistence.XMLSerializer_Pela = new Class(/** @lends XMLSerializer_Pela */{

    /**
     * @param mindmap
     * @throws will throw an error if mindmap is null or undefined
     * @return the created XML document (using the cross-browser implementation in core)
     */
    toXML: function (mindmap) {
        $assert(mindmap, "Can not save a null mindmap");

        var document = core.Utils.createDocument();

        // Store map attributes ...
        var mapElem = document.createElement("map");
        var name = mindmap.getId();
        if ($defined(name)) {
            mapElem.setAttribute('name', this.rmXmlInv(name));
        }
        var version = mindmap.getVersion();
        if ($defined(version)) {
            mapElem.setAttribute('version', version);
        }

        document.appendChild(mapElem);

        // Create branches ...
        var topics = mindmap.getBranches();
        for (var i = 0; i < topics.length; i++) {
            var topic = topics[i];
            var topicDom = this._topicToXML(document, topic);
            mapElem.appendChild(topicDom);
        }

        // Create Relationships
        var relationships = mindmap.getRelationships();
        if (relationships.length > 0) {

            for (var j = 0; j < relationships.length; j++) {
                var relationship = relationships[j];
                if (mindmap.findNodeById(relationship.getFromNode()) !== null && mindmap.findNodeById(relationship.getToNode()) !== null) {
                    // Isolated relationships are not persisted ....
                    var relationDom = this._relationshipToXML(document, relationship);
                    mapElem.appendChild(relationDom);
                }
            }
        }

        return document;
    },

    _topicToXML: function (document, topic) {
        var parentTopic = document.createElement("topic");

        // Set topic attributes...
        if (topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            parentTopic.setAttribute('central', 'true');
        } else {

            var pos = topic.getPosition();
            parentTopic.setAttribute("position", pos.x + ',' + pos.y);

            var order = topic.getOrder();
            if (typeof order === 'number' && isFinite(order))
                parentTopic.setAttribute("order", order);
        }

        var text = topic.getText();
        if ($defined(text)) {
            this._noteTextToXML(document, parentTopic, text);
        }

        var shape = topic.getShapeType();
        if ($defined(shape)) {
            parentTopic.setAttribute('shape', shape);

            if (shape == mindplot.model.TopicShape.IMAGE) {
                parentTopic.setAttribute('image', topic.getImageSize().width + "," + topic.getImageSize().height + ":" + topic.getImageUrl());
            }

        }

        if (topic.areChildrenShrunken() && topic.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            parentTopic.setAttribute('shrink', 'true');
        }

        // Font properties ...
        var id = topic.getId();
        parentTopic.setAttribute('id', id);

        var font = "";

        var fontFamily = topic.getFontFamily();
        font += (fontFamily ? fontFamily : '') + ';';

        var fontSize = topic.getFontSize();
        font += (fontSize ? fontSize : '') + ';';

        var fontColor = topic.getFontColor();
        font += (fontColor ? fontColor : '') + ';';

        var fontWeight = topic.getFontWeight();
        font += (fontWeight ? fontWeight : '') + ';';

        var fontStyle = topic.getFontStyle();
        font += (fontStyle ? fontStyle : '') + ';';

        if ($defined(fontFamily) || $defined(fontSize) || $defined(fontColor)
            || $defined(fontWeight) || $defined(fontStyle)) {
            parentTopic.setAttribute('fontStyle', font);
        }

        var bgColor = topic.getBackgroundColor();
        if ($defined(bgColor)) {
            parentTopic.setAttribute('bgColor', bgColor);
        }

        var brColor = topic.getBorderColor();
        if ($defined(brColor)) {
            parentTopic.setAttribute('brColor', brColor);
        }

        var metadata = topic.getMetadata();
        if ($defined(metadata)) {
            parentTopic.setAttribute('metadata', metadata);
        }

        // Serialize features ...
        var features = topic.getFeatures();
        for (var i = 0; i < features.length; i++) {
            var feature = features[i];

            var featureType = feature.getType();
            var featureDom = document.createElement(featureType);
            var attributes = feature.getAttributes();

            for (var key in attributes) {
                var value = attributes[key];
                if (key == 'text') {
                    var cdata = document.createCDATASection(this.rmXmlInv(value));
                    featureDom.appendChild(cdata);
                } else {
                    featureDom.setAttribute(key, this.rmXmlInv(value));
                }
            }
            parentTopic.appendChild(featureDom);
        }

        //CHILDREN TOPICS
        var childTopics = topic.getChildren();
        for (var j = 0; j < childTopics.length; j++) {
            var childTopic = childTopics[j];
            var childDom = this._topicToXML(document, childTopic);
            parentTopic.appendChild(childDom);

        }
        return parentTopic;
    },

    _noteTextToXML: function (document, elem, text) {
        if (text.indexOf('\n') == -1) {
            elem.setAttribute('text', this.rmXmlInv(text));
        } else {
            var textDom = document.createElement("text");
            var cdata = document.createCDATASection(this.rmXmlInv(text));
            textDom.appendChild(cdata);
            elem.appendChild(textDom);
        }
    },

    _relationshipToXML: function (document, relationship) {
        var result = document.createElement("relationship");
        result.setAttribute("srcTopicId", relationship.getFromNode());
        result.setAttribute("destTopicId", relationship.getToNode());


        var lineType = relationship.getLineType();
        result.setAttribute("lineType", lineType);
        if (lineType == mindplot.ConnectionLine.CURVED || lineType == mindplot.ConnectionLine.SIMPLE_CURVED) {
            if ($defined(relationship.getSrcCtrlPoint())) {
                var srcPoint = relationship.getSrcCtrlPoint();
                result.setAttribute("srcCtrlPoint", Math.round(srcPoint.x) + "," + Math.round(srcPoint.y));
            }
            if ($defined(relationship.getDestCtrlPoint())) {
                var destPoint = relationship.getDestCtrlPoint();
                result.setAttribute("destCtrlPoint", Math.round(destPoint.x) + "," + Math.round(destPoint.y));
            }
        }
        result.setAttribute("endArrow", relationship.getEndArrow());
        result.setAttribute("startArrow", relationship.getStartArrow());
        return result;
    },

    /**
     * @param dom
     * @param mapId
     * @throws will throw an error if dom is null or undefined
     * @throws will throw an error if mapId is null or undefined
     * @throws will throw an error if the document element is not consistent with a wisemap's root
     * element
     */
    loadFromDom: function (dom, mapId) {
        $assert(dom, "dom can not be null");
        $assert(mapId, "mapId can not be null");

        var rootElem = dom.documentElement;

        // Is a wisemap?.
        $assert(rootElem.tagName == mindplot.persistence.XMLSerializer_Pela.MAP_ROOT_NODE, "This seem not to be a map document.");

        this._idsMap = {};
        // Start the loading process ...
        var version = rootElem.getAttribute("version");

        var mindmap = new mindplot.model.Mindmap(mapId, version);
        var children = rootElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == 1) {
                switch (child.tagName) {
                    case "topic":
                        var topic = this._deserializeNode(child, mindmap);
                        mindmap.addBranch(topic);
                        break;
                    case "relationship":
                        var relationship = this._deserializeRelationship(child, mindmap);
                        if (relationship != null)
                            mindmap.addRelationship(relationship);
                        break;
                }
            }
        }
        this._idsMap = null;
        mindmap.setId(mapId);
        return mindmap;
    },

    _deserializeNode: function (domElem, mindmap) {
        var type = (domElem.getAttribute('central') != null) ? mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE : mindplot.model.INodeModel.MAIN_TOPIC_TYPE;

        // Load attributes...
        var id = domElem.getAttribute('id');
        if ($defined(id)) {
            id = parseInt(id);
        }

        if (this._idsMap[id]) {
            id = null;
        } else {
            this._idsMap[id] = domElem;
        }

        var topic = mindmap.createNode(type, id);

        // Set text property is it;s defined...
        var text = domElem.getAttribute('text');
        if ($defined(text) && text) {
            topic.setText(text);
        }

        var fontStyle = domElem.getAttribute('fontStyle');
        if ($defined(fontStyle) && fontStyle) {
            var font = fontStyle.split(';');

            if (font[0]) {
                topic.setFontFamily(font[0]);
            }

            if (font[1]) {
                topic.setFontSize(font[1]);
            }

            if (font[2]) {
                topic.setFontColor(font[2]);
            }

            if (font[3]) {
                topic.setFontWeight(font[3]);
            }

            if (font[4]) {
                topic.setFontStyle(font[4]);
            }
        }

        var shape = domElem.getAttribute('shape');
        if ($defined(shape)) {
            topic.setShapeType(shape);

            if (shape == mindplot.model.TopicShape.IMAGE) {
                var image = domElem.getAttribute('image');
                var size = image.substring(0, image.indexOf(':'));
                var url = image.substring(image.indexOf(':') + 1, image.length);
                topic.setImageUrl(url);

                var split = size.split(',');
                topic.setImageSize(split[0], split[1]);
            }
        }


        var bgColor = domElem.getAttribute('bgColor');
        if ($defined(bgColor)) {
            topic.setBackgroundColor(bgColor);
        }

        var borderColor = domElem.getAttribute('brColor');
        if ($defined(borderColor)) {
            topic.setBorderColor(borderColor);
        }

        var order = domElem.getAttribute('order');
        if ($defined(order) && order != "NaN") { // Hack for broken maps ...
            topic.setOrder(parseInt(order));
        }

        var isShrink = domElem.getAttribute('shrink');
        // Hack: Some production maps has been stored with the central topic collapsed. This is a bug.
        if ($defined(isShrink) && type != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            topic.setChildrenShrunken(isShrink);
        }

        var position = domElem.getAttribute('position');
        if ($defined(position)) {
            var pos = position.split(',');
            topic.setPosition(pos[0], pos[1]);
        }

        var metadata = domElem.getAttribute('metadata');
        if ($defined(metadata)) {
            topic.setMetadata(metadata);
        }

        //Creating icons and children nodes
        var children = domElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == Node.ELEMENT_NODE) {
                if (child.tagName == "topic") {
                    var childTopic = this._deserializeNode(child, mindmap);
                    childTopic.connectTo(topic);
                } else if (mindplot.TopicFeature.isSupported(child.tagName)) {

                    // Load attributes ...
                    var namedNodeMap = child.attributes;
                    var attributes = {};
                    for (var j = 0; j < namedNodeMap.length; j++) {
                        var attribute = namedNodeMap.item(j);
                        attributes[attribute.name] = attribute.value;
                    }

                    // Has text node ?.
                    var textAttr = this._deserializeTextAttr(child);
                    if (textAttr) {
                        attributes['text'] = textAttr;
                    }

                    // Create a new element ....
                    var featureType = child.tagName;
                    var feature = mindplot.TopicFeature.createModel(featureType, attributes);
                    topic.addFeature(feature);

                } else if (child.tagName == "text") {
                    var nodeText = this._deserializeNodeText(child);
                    topic.setText(nodeText);
                }
            }
        }
        return topic;
    },

    _deserializeTextAttr: function (domElem) {
        var value = domElem.getAttribute("text");
        if (!$defined(value)) {
            var children = domElem.childNodes;
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if (child.nodeType == Node.CDATA_SECTION_NODE) {
                    value = child.nodeValue;
                }
            }
        } else {
            // Notes must be decoded ...
            value = unescape(value);

            // Hack for empty nodes ...
            if (value == "") {
                value = " ";
            }
        }

        return value;
    },

    _deserializeNodeText: function (domElem) {
        var children = domElem.childNodes;
        var value = null;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == Node.CDATA_SECTION_NODE) {
                value = child.nodeValue;
            }
        }
        return value;
    },

    _deserializeRelationship: function (domElement, mindmap) {
        var srcId = domElement.getAttribute("srcTopicId");
        var destId = domElement.getAttribute("destTopicId");
        var lineType = domElement.getAttribute("lineType");
        var srcCtrlPoint = domElement.getAttribute("srcCtrlPoint");
        var destCtrlPoint = domElement.getAttribute("destCtrlPoint");
        var endArrow = domElement.getAttribute("endArrow");
        var startArrow = domElement.getAttribute("startArrow");
        //If for some reason a relationship lines has source and dest nodes the same, don't import it.
        if (srcId == destId) {
            return null;
        }
        // Is the connections points valid ?. If it's not, do not load the relationship ...
        if (mindmap.findNodeById(srcId) == null || mindmap.findNodeById(destId) == null) {
            return null;
        }

        var model = mindmap.createRelationship(srcId, destId);
        model.setLineType(lineType);
        if ($defined(srcCtrlPoint) && srcCtrlPoint != "") {
            model.setSrcCtrlPoint(core.Point.fromString(srcCtrlPoint));
        }
        if ($defined(destCtrlPoint) && destCtrlPoint != "") {
            model.setDestCtrlPoint(core.Point.fromString(destCtrlPoint));
        }
        model.setEndArrow('false');
        model.setStartArrow('true');
        return model;
    },
    
    /**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    rmXmlInv: function (str) {

        if (str == null || str == undefined)
            return null;

        var result = "";
        for (var i=0;i<str.length;i++){
            var c = str.charCodeAt(i);
            if ((c == 0x9) || (c == 0xA) || (c == 0xD)
                || ((c >= 0x20) && (c <= 0xD7FF))
                || ((c >= 0xE000) && (c <= 0xFFFD))
                || ((c >= 0x10000) && (c <= 0x10FFFF))) {
                result = result + str.charAt(i);
            }

        }
        return result;

    }
});

/**
 * a wisemap's root element tag name
 * @constant
 * @type {String}
 * @default
 */
mindplot.persistence.XMLSerializer_Pela.MAP_ROOT_NODE = 'map';