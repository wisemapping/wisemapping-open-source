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

function afterCoreLoading()
{
    if (core.UserAgent.isVMLSupported())
    {
        $import("../js/mindplot.vml-min.js");
    } else
    {
        $import("../js/mindplot.svg-min.js");

    }
};

afterCoreLoading();

function afterMindpotLibraryLoading()
{
    buildMindmapDesigner();

    $('zoomIn').addEvent('click', function(event) {
        designer.zoomIn();
    });

    $('zoomOut').addEvent('click', function(event) {
        designer.zoomOut();
    });


    // If not problem has occured, I close the dialod ...
    var closeDialog = function() {

        if (!window.hasUnexpectedErrors)
        {
            waitDialog.deactivate();
        }
    }.delay(500);

}

function setCurrentColorPicker(colorPicker)
{
    this.currentColorPicker = colorPicker;
}


function buildMindmapDesigner()
{

    var container = $('mindplot');

    // Initialize Editor ...

    var screenWidth = window.getWidth();
    var screenHeight = window.getHeight();

    // body margin ...
    editorProperties.width = screenWidth;
    editorProperties.height = screenHeight;
    editorProperties.viewMode = true;

    designer = new mindplot.MindmapDesigner(editorProperties, container);
    designer.loadFromXML(mapId, mapXml);

    // If a node has focus, focus can be move to another node using the keys.
    designer._cleanScreen = function() {
    };
}