mindplot.XMLMindmapSerializer_Beta = function()
{

};

mindplot.XMLMindmapSerializer_Beta.prototype.toXML = function(mindmap)
{
    core.assert(mindmap, "Can not save a null mindmap");

    var document = core.Utils.createDocument();

    // Store map attributes ...
    var mapElem = document.createElement("map");
    var name = mindmap.getId();
    if (name)
    {
        mapElem.setAttribute('name', name);
    }
    document.appendChild(mapElem);

    // Create branches ...
    var topics = mindmap.getBranches();
    for (var i = 0; i < topics.length; i++)
    {
        var topic = topics[i];
        var topicDom = this._topicToXML(document, topic);
        mapElem.appendChild(topicDom);
    }

    return document;
};

mindplot.XMLMindmapSerializer_Beta.prototype._topicToXML = function(document, topic)
{
    var parentTopic = document.createElement("topic");

    // Set topic attributes...
    if (topic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        parentTopic.setAttribute("central", true);
    } else
    {
        var parent = topic.getParent();
        if (parent == null || parent.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            var pos = topic.getPosition();
            parentTopic.setAttribute("position", pos.x + ',' + pos.y);
        } else
        {
            var order = topic.getOrder();
            parentTopic.setAttribute("order", order);
        }
    }

    var text = topic.getText();
    if (text) {
        parentTopic.setAttribute('text', text);
    }

    var shape = topic.getShapeType();
    if (shape) {
        parentTopic.setAttribute('shape', shape);
    }

    if(topic.areChildrenShrinked())
    {
        parentTopic.setAttribute('shrink',true);
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

    if (fontFamily || fontSize || fontColor || fontWeight || fontStyle)
    {
        parentTopic.setAttribute('fontStyle', font);
    }

    var bgColor = topic.getBackgroundColor();
    if (bgColor) {
        parentTopic.setAttribute('bgColor', bgColor);
    }

    var brColor = topic.getBorderColor();
    if (brColor) {
        parentTopic.setAttribute('brColor', brColor);
    }

    //ICONS
    var icons = topic.getIcons();
    for (var i = 0; i < icons.length; i++)
    {
        var icon = icons[i];
        var iconDom = this._iconToXML(document, icon);
        parentTopic.appendChild(iconDom);
    }

    //LINKS
    var links = topic.getLinks();
    for (var i = 0; i < links.length; i++)
    {
        var link = links[i];
        var linkDom = this._linkToXML(document, link);
        parentTopic.appendChild(linkDom);
    }

    var notes = topic.getNotes();
    for (var i = 0; i < notes.length; i++)
    {
        var note = notes[i];
        var noteDom = this._noteToXML(document, note);
        parentTopic.appendChild(noteDom);
    }

    //CHILDREN TOPICS
    var childTopics = topic.getChildren();
    for (var i = 0; i < childTopics.length; i++)
    {
        var childTopic = childTopics[i];
        var childDom = this._topicToXML(document, childTopic);
        parentTopic.appendChild(childDom);

    }

    return parentTopic;
};

mindplot.XMLMindmapSerializer_Beta.prototype._iconToXML = function(document, icon)
{
    var iconDom = document.createElement("icon");
    iconDom.setAttribute('id', icon.getIconType());
    return iconDom;
};

mindplot.XMLMindmapSerializer_Beta.prototype._linkToXML = function(document, link)
{
    var linkDom = document.createElement("link");
    linkDom.setAttribute('url', link.getUrl());
    return linkDom;
};

mindplot.XMLMindmapSerializer_Beta.prototype._noteToXML = function(document, note)
{
    var noteDom = document.createElement("note");
    noteDom.setAttribute('text', note.getText());
    return noteDom;
};

mindplot.XMLMindmapSerializer_Beta.prototype.loadFromDom = function(dom)
{
    core.assert(dom, "Dom can not be null");
    var rootElem = dom.documentElement;

    // Is a wisemap?.
    core.assert(rootElem.tagName == mindplot.XMLMindmapSerializer_Beta.MAP_ROOT_NODE, "This seem not to be a map document.");

    // Start the loading process ...
    var mindmap = new mindplot.Mindmap();    

    var children = rootElem.childNodes;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];
        if (child.nodeType == 1)
        {
            var topic = this._deserializeNode(child, mindmap);
            mindmap.addBranch(topic);
        }
    }
    return mindmap;
};

mindplot.XMLMindmapSerializer_Beta.prototype._deserializeNode = function(domElem, mindmap)
{
    var type = (domElem.getAttribute('central') != null) ? mindplot.NodeModel.CENTRAL_TOPIC_TYPE : mindplot.NodeModel.MAIN_TOPIC_TYPE;
    var topic = mindmap.createNode(type);

    // Load attributes...
    var text = domElem.getAttribute('text');
    if (text) {
        topic.setText(text);
    }

    var order = domElem.getAttribute('order');
    if (order) {
        topic.setOrder(order);
    }

    var shape = domElem.getAttribute('shape');
    if (shape) {
        topic.setShapeType(shape);
    }

    var isShrink = domElem.getAttribute('shrink');
    if(isShrink)
    {
        topic.setChildrenShrinked(isShrink);
    }

    var fontStyle = domElem.getAttribute('fontStyle');
    if (fontStyle) {
        var font = fontStyle.split(';');

        if (font[0])
        {
            topic.setFontFamily(font[0]);
        }

        if (font[1])
        {
            topic.setFontSize(font[1]);
        }

        if (font[2])
        {
            topic.setFontColor(font[2]);
        }

        if (font[3])
        {
            topic.setFontWeight(font[3]);
        }

        if (font[4])
        {
            topic.setFontStyle(font[4]);
        }
    }

    var bgColor = domElem.getAttribute('bgColor');
    if (bgColor) {
        topic.setBackgroundColor(bgColor);
    }

    var borderColor = domElem.getAttribute('brColor');
    if (borderColor) {
        topic.setBorderColor(borderColor);
    }

    var position = domElem.getAttribute('position');
    if (position) {
        var pos = position.split(',');
        topic.setPosition(pos[0], pos[1]);
    }

    //Creating icons and children nodes
    var children = domElem.childNodes;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];
        if (child.nodeType == 1)
        {
            core.assert(child.tagName == "topic" || child.tagName == "icon" || child.tagName == "link" || child.tagName == "note", 'Illegal node type:' + child.tagName);
            if (child.tagName == "topic") {
                var childTopic = this._deserializeNode(child, mindmap);
                childTopic.connectTo(topic);
            } else if(child.tagName == "icon") {
                var icon = this._deserializeIcon(child, topic);
                topic.addIcon(icon);
            } else if(child.tagName == "link") {
                var link = this._deserializeLink(child, topic);
                topic.addLink(link);
            } else if(child.tagName == "note") {
                var note = this._deserializeNote(child, topic);
                topic.addNote(note);
            }
        }
    }
    ;
    return topic;
};

mindplot.XMLMindmapSerializer_Beta.prototype._deserializeIcon = function(domElem, topic)
{
    return topic.createIcon(domElem.getAttribute("id"));
};

mindplot.XMLMindmapSerializer_Beta.prototype._deserializeLink = function(domElem, topic)
{
    return topic.createLink(domElem.getAttribute("url"));
};

mindplot.XMLMindmapSerializer_Beta.prototype._deserializeNote = function(domElem, topic)
{
    return topic.createNote(domElem.getAttribute("text"));
};

mindplot.XMLMindmapSerializer_Beta.MAP_ROOT_NODE = 'map';