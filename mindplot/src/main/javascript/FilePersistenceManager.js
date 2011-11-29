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

mindplot.FilePersitenceManager = new Class({
        Extends:mindplot.PersitenceManager,
        initialize: function() {
            this.parent();
        },

        saveMapXml : function(mapId, mapXml, pref, saveHistory, events) {
            console.log(mapXml);
            events.onSuccess();
        },

        load : function(mapId) {
            $assert(mapId, "mapId can not be null");

            var domDocument;
            var xmlRequest = new Request({
                url: '../maps/' + mapId + '.xml',
                method: 'get',
                async: false,
                onSuccess: function(responseText, responseXML) {
                    domDocument = responseXML;
                }
            });
            xmlRequest.send();
            return  this.loadFromDom(mapId, domDocument);
        }
    }
);


