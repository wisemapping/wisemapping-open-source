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
        $import("../js/mindplot.vml.js");
    } else
    {
        $import("../js/mindplot.svg.js");

    }
}
;

function afterWisemapLoading()
{
    buildMindmapDesigner();


    $('zoomIn').addEvent('click', function(event) {
        designer.zoomIn();
    });

    $('zoomOut').addEvent('click', function(event) {
        designer.zoomOut();
    });

    // Disable loading dialog ...
    setTimeout("loadingDialog.deactivate();", 500);
}

function setCurrentColorPicker(colorPicker)
{
    this.currentColorPicker = colorPicker;
}


function buildMindmapDesigner()
{

    // Initialize logger...
    core.Logger.init(window.LoggerService);

    var container = $('mindplot');
    var footer = $('embFooter');
    if (core.UserAgent.isIframeWorkaroundRequired())
    {
        var iframe = document.createElement('iframe');
        iframe.id = "mindplotIFrame";
        var top = container.offsetTop;
        var bottom = footer.offsetTop;
        iframe.setStyle('width', "100%");
        iframe.setStyle('height', bottom - top + "px");
        iframe.setStyle('overflow', "hidden");
        iframe.setStyle('border', "none");
        container.appendChild(iframe);
        var mapContainer = "<div id='mindplot' style='background: url( ../images/grid.gif ) bottom left repeat !important;'></div><script>function styleMe() {" +
                           "var small_head = document.getElementsByTagName('head').item(0);" +
                           "var thestyle = document.createElement('link');" +
                           "thestyle.setAttribute('rel', 'stylesheet');thestyle.setAttribute('type', 'text/css');thestyle.setAttribute('href', '../css/bubble.css');small_head.appendChild(thestyle);}; styleMe();</script>";
        var doc = iframe.contentDocument;
        if (doc == undefined || doc == null)
            doc = iframe.contentWindow.document;
        doc.open();
        doc.write(mapContainer);
        doc.close();
        $(doc.body).setStyle('margin', '0px');
        container = doc.getElementById('mindplot');

    }

    // Initialize Editor ...
    var persistantManager = new mindplot.PersistanceManager(window.MapEditorService);

    var screenWidth = window.getWidth();
    var screenHeight = window.getHeight();

    // Positionate node ...
    // header - footer
    screenHeight = screenHeight;

    // body margin ...
    editorProperties.width = screenWidth;
    editorProperties.height = screenHeight;
    editorProperties.viewMode = true;

    designer = new mindplot.MindmapDesigner(editorProperties, container, persistantManager);
    designer.loadFromXML(mapId, mapXml);

    // If a node has focus, focus can be move to another node using the keys.
    designer._cleanScreen = function() {
    };
}