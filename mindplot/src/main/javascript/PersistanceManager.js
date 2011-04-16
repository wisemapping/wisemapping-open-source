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

mindplot.PersistanceManager = {};

mindplot.PersistanceManager.save = function(mindmap, editorProperties, onSavedHandler,saveHistory)
{
    core.assert(mindmap, "mindmap can not be null");
    core.assert(editorProperties, "editorProperties can not be null");

    var mapId = mindmap.getId();

    var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromMindmap(mindmap);
    var xmlMap = serializer.toXML(mindmap);
    var xmlMapStr = core.Utils.innerXML(xmlMap);

    var pref = Json.toString(editorProperties);
    window.MapEditorService.saveMap(mapId, xmlMapStr, pref,saveHistory,
    {
        callback:function(response) {

            if (response.msgCode != "OK")
            {
                monitor.logError("Save could not be completed. Please,try again in a couple of minutes.");
                wLogger.error(response.msgDetails);
            } else
            {
                // Execute on success handler ...
                if (core.Utils.isDefined(onSavedHandler))
                {
                    onSavedHandler();
                }
            }
        },
        errorHandler:function(message) {
            var monitor = core.Monitor.getInstance();
            monitor.logError("Save could not be completed. Please,try again in a couple of minutes.");
            wLogger.error(message);
        },
        verb:"POST",
        async: false
    });

};

mindplot.PersistanceManager.load = function(mapId)
{
    core.assert(mapId, "mapId can not be null");

    var result = {r:null};
    window.MapEditorService.loadMap(mapId, {
        callback:function(response) {

            if (response.msgCode == "OK")
            {
                // Explorer Hack with local files ...
                var xmlContent = response.content;
                var domDocument = core.Utils.createDocumentFromText(xmlContent);
                var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
                var mindmap = serializer.loadFromDom(domDocument);
                mindmap.setId(mapId);

                result.r = mindmap;
            } else
            {
                // Handle error message ...
                var msg = response.msgDetails;
                var monitor = core.Monitor.getInstance();
                monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
                wLogger.error(msg);
            }
        },
        verb:"GET",
        async: false,
        errorHandler:function(msg) {
            var monitor = core.Monitor.getInstance();
            monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
            wLogger.error(msg);
        }
    });

    return result.r;
};


