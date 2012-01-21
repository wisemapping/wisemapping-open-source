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

mindplot.XMLMindmapSerializer_Pela = new Class({

    toXML : function(mindmap) {
        $assert(mindmap, "Can not save a null mindmap");

        var document = core.Utils.createDocument();

        // Store map attributes ...
        var mapElem = document.createElement("map");
        var name = mindmap.getId();
        if ($defined(name)) {
            mapElem.setAttribute('name', name);
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
//        var relationshipDom=document.createElement("relationships");
//        mapElem.appendChild(relationshipDom);
            for (var j = 0; j < relationships.length; j++) {
                var relationDom = this._relationshipToXML(document, relationships[j]);
                mapElem.appendChild(relationDom);
            }
        }

        return document;
    },

    _topicToXML : function(document, topic) {
        var parentTopic = document.createElement("topic");

        // Set topic attributes...
        if (topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            parentTopic.setAttribute("central", true);
        } else {

            var pos = topic.getPosition();
            parentTopic.setAttribute("position", pos.x + ',' + pos.y);

            var order = topic.getOrder();
            if (!isNaN(order))
                parentTopic.setAttribute("order", order);
        }

        var text = topic.getText();
        if ($defined(text)) {
            this._noteTextToXML(document, parentTopic, text);
        }

        var shape = topic.getShapeType();
        if ($defined(shape)) {
            parentTopic.setAttribute('shape', shape);
        }

        if (topic.areChildrenShrunken()) {
            parentTopic.setAttribute('shrink', true);
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

        //ICONS
        var icons = topic.getIcons();
        for (var i = 0; i < icons.length; i++) {
            var icon = icons[i];
            var iconDom = this._iconToXML(document, icon);
            parentTopic.appendChild(iconDom);
        }

        //LINKS
        var links = topic.getLinks();
        for (var i = 0; i < links.length; i++) {
            var link = links[i];
            var linkDom = this._linkToXML(document, link);
            parentTopic.appendChild(linkDom);
        }

        var notes = topic.getNotes();
        for (var i = 0; i < notes.length; i++) {
            var note = notes[i];
            var noteDom = this._noteToXML(document, note);
            parentTopic.appendChild(noteDom);
        }

        //CHILDREN TOPICS
        var childTopics = topic.getChildren();
        for (var i = 0; i < childTopics.length; i++) {
            var childTopic = childTopics[i];
            var childDom = this._topicToXML(document, childTopic);
            parentTopic.appendChild(childDom);

        }

        return parentTopic;
    },

    _iconToXML : function(document, icon) {
        var iconDom = document.createElement("icon");
        iconDom.setAttribute('id', icon.getIconType());
        return iconDom;
    },

    _linkToXML : function(document, link) {
        var linkDom = document.createElement("link");
        linkDom.setAttribute('url', link.getUrl());
        return linkDom;
    },

    _noteToXML : function(document, note) {
        var noteDom = document.createElement("note");
        var cdata = document.createCDATASection(note.getText());
        noteDom.appendChild(cdata);
        return noteDom;
    },

    _noteTextToXML : function(document, elem, text) {
        if (text.indexOf('\n') == -1) {
            elem.setAttribute('text', text);
        } else {
            var textDom = document.createElement("text");
            var cdata = document.createCDATASection(text);
            textDom.appendChild(cdata);
            elem.appendChild(textDom);
        }
    },

    _relationshipToXML : function(document, relationship) {
        var relationDom = document.createElement("relationship");
        relationDom.setAttribute("srcTopicId", relationship.getFromNode());
        relationDom.setAttribute("destTopicId", relationship.getToNode());
        var lineType = relationship.getLineType();
        relationDom.setAttribute("lineType", lineType);
        if (lineType == mindplot.ConnectionLine.CURVED || lineType == mindplot.ConnectionLine.SIMPLE_CURVED) {
            if ($defined(relationship.getSrcCtrlPoint())) {
                var srcPoint = relationship.getSrcCtrlPoint();
                relationDom.setAttribute("srcCtrlPoint", Math.round(srcPoint.x) + "," + Math.round(srcPoint.y));
            }
            if ($defined(relationship.getDestCtrlPoint())) {
                var destPoint = relationship.getDestCtrlPoint();
                relationDom.setAttribute("destCtrlPoint", Math.round(destPoint.x) + "," + Math.round(destPoint.y));
            }
        }
        relationDom.setAttribute("endArrow", relationship.getEndArrow());
        relationDom.setAttribute("startArrow", relationship.getStartArrow());
        return relationDom;
    },

    loadFromDom : function(dom, mapId) {
        $assert(dom, "dom can not be null");
        $assert(mapId, "mapId can not be null");

        var rootElem = dom.documentElement;

        // Is a wisemap?.
        $assert(rootElem.tagName == mindplot.XMLMindmapSerializer_Pela.MAP_ROOT_NODE, "This seem not to be a map document.");

        this._idsMap = new Hash();
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

    _deserializeNode : function(domElem, mindmap) {
        var type = (domElem.getAttribute('central') != null) ? mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE : mindplot.model.INodeModel.MAIN_TOPIC_TYPE;

        // Load attributes...
        var id = domElem.getAttribute('id');
        if ($defined(id)) {
            id = parseInt(id);
        }

        if (this._idsMap.has(id)) {
            id = null;
        } else {
            this._idsMap.set(id, domElem);
        }

        var topic = mindmap.createNode(type, id);

        var text = domElem.getAttribute('text');
        if ($defined(text)) {
            topic.setText(text);
        }

        var order = domElem.getAttribute('order');
        if ($defined(order)) {
            topic.setOrder(parseInt(order));
        }

        var shape = domElem.getAttribute('shape');
        if ($defined(shape)) {
            topic.setShapeType(shape);
        }

        var isShrink = domElem.getAttribute('shrink');
        if ($defined(isShrink)) {
            topic.setChildrenShrunken(isShrink);
        }

        var fontStyle = domElem.getAttribute('fontStyle');
        if ($defined(fontStyle)) {
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

        var bgColor = domElem.getAttribute('bgColor');
        if ($defined(bgColor)) {
            topic.setBackgroundColor(bgColor);
        }

        var borderColor = domElem.getAttribute('brColor');
        if ($defined(borderColor)) {
            topic.setBorderColor(borderColor);
        }

        var position = domElem.getAttribute('position');
        if ($defined(position)) {
            var pos = position.split(',');
            topic.setPosition(pos[0], pos[1]);
        }

        //Creating icons and children nodes
        var children = domElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == Node.ELEMENT_NODE) {
                $assert(child.tagName == "topic" || child.tagName == "icon" || child.tagName == "link" || child.tagName == "note" || child.tagName == "text", 'Illegal node type:' + child.tagName);
                if (child.tagName == "topic") {
                    var childTopic = this._deserializeNode(child, mindmap);
                    childTopic.connectTo(topic);
                } else if (child.tagName == "icon") {
                    var icon = this._deserializeIcon(child, topic);
                    topic.addIcon(icon);
                } else if (child.tagName == "link") {
                    var link = this._deserializeLink(child, topic);
                    topic.addLink(link);
                } else if (child.tagName == "note") {
                    var note = this._deserializeNote(child, topic);
                    topic.addNote(note);
                } else if (child.tagName == "text") {
                    var nodeText = this._deserializeNodeText(child);
                    topic.setText(nodeText);
                }

            }
        }
        return topic;
    },

    _deserializeIcon : function(domElem, topic) {
        var icon = domElem.getAttribute("id");
        icon = icon.replace("images/", "icons/legacy/");
        return topic.createIcon(icon);
    },

    _deserializeLink : function(domElem, topic) {
        return topic.createLink(domElem.getAttribute("url"));
    },

    _deserializeNote : function(domElem, topic) {
        var value = domElem.getAttribute("text");
        if (!$defined(value)) {
            var children = domElem.childNodes;
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if (child.nodeType == Node.CDATA_SECTION_NODE) {
                    value = child.nodeValue;
                }
            }
        }
        return topic.createNote(value);
    },

    _deserializeNodeText: function(domElem) {

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

    _deserializeRelationship : function(domElement, mindmap) {
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
        var model = mindmap.createRelationship(srcId, destId);
        model.setLineType(lineType);
        if ($defined(srcCtrlPoint) && srcCtrlPoint != "") {
            model.setSrcCtrlPoint(core.Point.fromString(srcCtrlPoint));
        }
        if ($defined(destCtrlPoint) && destCtrlPoint != "") {
            model.setDestCtrlPoint(core.Point.fromString(destCtrlPoint));
        }
        model.setEndArrow(endArrow == "true");
        model.setStartArrow(startArrow == "true");
        return model;
    }
});

mindplot.XMLMindmapSerializer_Pela.MAP_ROOT_NODE = 'map';