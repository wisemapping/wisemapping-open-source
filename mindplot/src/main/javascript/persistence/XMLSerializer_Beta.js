/*    Copyright [2015] [wisemapping]
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
mindplot.persistence.XMLSerializer_Beta = new Class({

    toXML:function (mindmap) {
        $assert(mindmap, "Can not save a null mindmap");

        var document = core.Utils.createDocument();

        // Store map attributes ...
        var mapElem = document.createElement("map");
        var name = mindmap.getId();
        if ($defined(name)) {
            mapElem.setAttribute('name', name);
        }
        document.append(mapElem);

        // Create branches ...
        var topics = mindmap.getBranches();
        for (var i = 0; i < topics.length; i++) {
            var topic = topics[i];
            var topicDom = this._topicToXML(document, topic);
            mapElem.append(topicDom);
        }

        return document;
    },

    _topicToXML:function (document, topic) {
        var parentTopic = document.createElement("topic");

        // Set topic attributes...
        if (topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            parentTopic.setAttribute("central", true);
        } else {
            var parent = topic.getParent();
            if (parent == null || parent.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                var pos = topic.getPosition();
                parentTopic.setAttribute("position", pos.x + ',' + pos.y);
            } else {
                var order = topic.getOrder();
                parentTopic.setAttribute("order", order);
            }
        }

        var text = topic.getText();
        if ($defined(text)) {
            parentTopic.setAttribute('text', text);
        }

        var shape = topic.getShapeType();
        if ($defined(shape)) {
            parentTopic.setAttribute('shape', shape);
        }

        if (topic.areChildrenShrunken()) {
            parentTopic.setAttribute('shrink', true);
        }

        // Font properties ...
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
        var i;
        var icons = topic.getIcons();
        for (i = 0; i < icons.length; i++) {
            var icon = icons[i];
            var iconDom = this._iconToXML(document, icon);
            parentTopic.append(iconDom);
        }

        //LINKS
        var links = topic.getLinks();
        for (i = 0; i < links.length; i++) {
            var link = links[i];
            var linkDom = this._linkToXML(document, link);
            parentTopic.append(linkDom);
        }

        var notes = topic.getNotes();
        for (i = 0; i < notes.length; i++) {
            var note = notes[i];
            var noteDom = this._noteToXML(document, note);
            parentTopic.append(noteDom);
        }

        //CHILDREN TOPICS
        var childTopics = topic.getChildren();
        for (i = 0; i < childTopics.length; i++) {
            var childTopic = childTopics[i];
            var childDom = this._topicToXML(document, childTopic);
            parentTopic.append(childDom);

        }

        return parentTopic;
    },

    _iconToXML:function (document, icon) {
        var iconDom = document.createElement("icon");
        iconDom.setAttribute('id', icon.getIconType());
        return iconDom;
    },

    _linkToXML:function (document, link) {
        var linkDom = document.createElement("link");
        linkDom.setAttribute('url', link.getUrl());
        return linkDom;
    },

    _noteToXML:function (document, note) {
        var noteDom = document.createElement("note");
        noteDom.setAttribute('text', note.getText());
        return noteDom;
    },

    loadFromDom:function (dom, mapId) {
        $assert(dom, "Dom can not be null");
        $assert(mapId, "mapId can not be null");

        // Is a valid object ?
        var documentElement = dom.documentElement;
        $assert(documentElement.nodeName != "parsererror", "Error while parsing: '" + documentElement.childNodes[0].nodeValue);

        // Is a wisemap?.
        $assert(documentElement.tagName == mindplot.persistence.XMLSerializer_Beta.MAP_ROOT_NODE, "This seem not to be a map document. Root Tag: '" + documentElement.tagName + ",',HTML:" +dom.innerHTML + ",XML:"+ core.Utils.innerXML(dom));

        // Start the loading process ...
        var version = documentElement.getAttribute("version");
        version = !$defined(version) ? mindplot.persistence.ModelCodeName.BETA : version;
        var mindmap = new mindplot.model.Mindmap(mapId, version);

        var children = documentElement.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == 1) {
                var topic = this._deserializeNode(child, mindmap);
                mindmap.addBranch(topic);
            }
        }
        mindmap.setId(mapId);
        return mindmap;
    },

    _deserializeNode:function (domElem, mindmap) {
        var type = (domElem.getAttribute('central') != null) ? mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE : mindplot.model.INodeModel.MAIN_TOPIC_TYPE;
        var topic = mindmap.createNode(type);

        // Load attributes...
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
            if (child.nodeType == 1) {
                $assert(child.tagName == "topic" || child.tagName == "icon" || child.tagName == "link" || child.tagName == "note", 'Illegal node type:' + child.tagName);
                if (child.tagName == "topic") {
                    var childTopic = this._deserializeNode(child, mindmap);
                    childTopic.connectTo(topic);
                } else if (child.tagName == "icon") {
                    var icon = this._deserializeIcon(child, topic);
                    topic.addFeature(icon);
                } else if (child.tagName == "link") {
                    var link = this._deserializeLink(child, topic);
                    topic.addFeature(link);
                } else if (child.tagName == "note") {
                    var note = this._deserializeNote(child, topic);
                    topic.addFeature(note);
                }
            }
        }

        return topic;
    },

    _deserializeIcon:function (domElem) {
        var icon = domElem.getAttribute("id");
        icon = icon.replace("images/", "icons/legacy/");
        return  mindplot.TopicFeature.createModel(mindplot.TopicFeature.Icon.id, {id:icon});
    },

    _deserializeLink:function (domElem) {
        return  mindplot.TopicFeature.createModel(mindplot.TopicFeature.Link.id, {url:domElem.getAttribute("url")});
    },

    _deserializeNote:function (domElem) {
        var text = domElem.getAttribute("text");
        return  mindplot.TopicFeature.createModel(mindplot.TopicFeature.Note.id, {text:text == null ? " " : text});
    }});

mindplot.persistence.XMLSerializer_Beta.MAP_ROOT_NODE = 'map';