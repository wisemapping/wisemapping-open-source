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

mindplot.DwrPersitenceManager = new Class({
        Extends:mindplot.PersitenceManager,
        initialize: function() {
            this.parent();
        },

        saveMapXml : function(mapId, mapXml, pref, saveHistory, events) {
            window.MapEditorService.saveMap(mapId, mapXml, pref, saveHistory, {
                    callback:function(response) {
                        if (response.msgCode != "OK") {
                            events.onError(response);
                        } else {
                            events.onSuccess(response);
                        }
                    },

                    errorHandler:function(message) {
                        events.onError(message);
                    },
                    verb:"POST",
                    async: true
                }
            )
        },

        loadMapDom : function(mapId) {
            $assert(mapId, "mapId can not be null");
            throw "This must be implemented";

//            var result = {r:null};
//            window.MapEditorService.loadMap(mapId, {
//                callback:function(response) {
//
//                    if (response.msgCode == "OK") {
//                        // Explorer Hack with local files ...
//                        var xmlContent = response.content;
//                        var domDocument = core.Utils.createDocumentFromText(xmlContent);
//                        var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
//                        var mindmap = serializer.loadFromDom(domDocument);
//                        mindmap.setId(mapId);
//
//                        result.r = mindmap;
//                    } else {
//                        // Handle error message ...
//                        var msg = response.msgDetails;
//                        var monitor = core.ToolbarNotifier.getInstance();
//                        monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
////                wLogger.error(msg);
//                    }
//                },
//                verb:"GET",
//                async: false,
//                errorHandler:function(msg) {
//                    var monitor = core.ToolbarNotifier.getInstance();
//                    monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
////            wLogger.error(msg);
//                }
//            });
//
//            return result.r;
        }
    }
);


