/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

mindplot.ShirinkConnector = function(topic)
{
    var elipse = new web2d.Elipse(mindplot.Topic.prototype.INNER_RECT_ATTRIBUTES);
    this._elipse = elipse;
    elipse.setFill('#f7f7f7');

    elipse.setSize(mindplot.Topic.CONNECTOR_WIDTH, mindplot.Topic.CONNECTOR_WIDTH);
    var shrinkConnector = this;
    elipse.addEventListener('click', function(event)
    {
        var model = topic.getModel();
        var isShrink = !model.areChildrenShrinked();

        var actionRunner = mindplot.DesignerActionRunner.getInstance();
        var topicId = topic.getId();

        var commandFunc = function(topic, isShrink)
        {
            topic.setChildrenShrinked(isShrink);
            return !isShrink;
        }

        var command = new mindplot.commands.GenericFunctionCommand(commandFunc, isShrink, [topicId]);
        actionRunner.execute(command)

        new Event(event).stop();

    });

    elipse.addEventListener('click', function(event)
    {
        // Avoid node creation ...
        new Event(event).stop();
    });

    elipse.addEventListener('dblclick', function(event)
    {
        // Avoid node creation ...
        new Event(event).stop();

    });

    elipse.addEventListener('mouseover', function(event)
    {
        this.setFill('#009900');
    });

    elipse.addEventListener('mouseout', function(event)
    {
        var color = topic.getBackgroundColor();
        this.setFill(color);
    });

    elipse.setCursor('default');
    this._fillColor = '#f7f7f7';
    var model = topic.getModel();
    this.changeRender(model.areChildrenShrinked());

};

mindplot.ShirinkConnector.prototype.changeRender = function(isShrink)
{
    var elipse = this._elipse;
    if (isShrink)
    {
        elipse.setStroke('2', 'solid');
    } else
    {
        elipse.setStroke('1', 'solid');
    }
}


mindplot.ShirinkConnector.prototype.setVisibility = function(value)
{
    this._elipse.setVisibility(value);
}

mindplot.ShirinkConnector.prototype.setFill = function(color)
{
    this._fillColor = color;
    this._elipse.setFill(color);
}

mindplot.ShirinkConnector.prototype.setAttribute = function(name, value)
{
    this._elipse.setAttribute(name, value);
}

mindplot.ShirinkConnector.prototype.addToWorkspace = function(group)
{
    group.appendChild(this._elipse);
}


mindplot.ShirinkConnector.prototype.setPosition = function(x, y)
{
    this._elipse.setPosition(x, y);
}
