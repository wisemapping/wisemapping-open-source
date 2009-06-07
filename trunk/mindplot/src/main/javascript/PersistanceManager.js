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

mindplot.PersistanceManager = function(editorService)
{
    this._editorService = editorService;
    this._serializer = new mindplot.XMLMindmapSerializer();
};

mindplot.PersistanceManager.prototype.save = function(mindmap, chartType, xmlChart, editorProperties, onSavedHandler,saveHistory)
{
    core.assert(mindmap, "mindmap can not be null");
    core.assert(chartType, "chartType can not be null");
    core.assert(xmlChart, "xmlChart can not be null");
    core.assert(editorProperties, "editorProperties can not be null");

    var mapId = mindmap.getId();

    var xmlMap = this._serializer.toXML(mindmap);
    var xmlMapStr = core.Utils.innerXML(xmlMap);

    var pref = Json.toString(editorProperties);
    this._editorService.saveMap(mapId, xmlMapStr, chartType, xmlChart, pref,saveHistory,
    {
        callback:function(response) {

            if (response.msgCode != "OK")
            {
                monitor.logError("Save could not be completed. Please,try again in a couple of minutes.");
                core.Logger.logError(response.msgDetails);
            } else
            {
                // Execute on success handler ...
                if (onSavedHandler)
                {
                    onSavedHandler();
                }
            }
        },
        errorHandler:function(message) {
            var monitor = core.Monitor.getInstance();
            monitor.logError("Save could not be completed. Please,try again in a couple of minutes.");
            core.Logger.logError(message);
        },
        verb:"POST",
        async: false
    });

};

mindplot.PersistanceManager.prototype.load = function(mapId)
{
    core.assert(mapId, "mapId can not be null");

    var deserializer = this;
    var result = {r:null};
    var serializer = this._serializer;
    this._editorService.loadMap(mapId, {
        callback:function(response) {

            if (response.msgCode == "OK")
            {
                // Explorer Hack with local files ...
                var xmlContent = response.content;
                var domDocument = core.Utils.createDocumentFromText(xmlContent);
                var mindmap = serializer.loadFromDom(domDocument);
                mindmap.setId(mapId);

                result.r = mindmap;
            } else
            {
                // Handle error message ...
                var msg = response.msgDetails;
                var monitor = core.Monitor.getInstance();
                monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
                core.Logger.logError(msg);
            }
        },
        verb:"GET",
        async: false,
        errorHandler:function(msg) {
            var monitor = core.Monitor.getInstance();
            monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
            core.Logger.logError(msg);
        }
    });

    return result.r;
};


