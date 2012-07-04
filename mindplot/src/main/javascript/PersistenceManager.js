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

mindplot.PersistenceManager = new Class({
    initialize: function() {

    },

    save: function(mindmap, editorProperties, saveHistory, events) {
        $assert(mindmap, "mindmap can not be null");
        $assert(editorProperties, "editorProperties can not be null");

        var mapId = mindmap.getId();
        $assert(mapId, "mapId can not be null");

        var serializer = mindplot.persistence.XMLSerializerFactory.getSerializerFromMindmap(mindmap);
        var domMap = serializer.toXML(mindmap);
        var mapXml = core.Utils.innerXML(domMap);

        var pref = JSON.encode(editorProperties);
        try {
            this.saveMapXml(mapId, mapXml, pref, saveHistory, events);
        } catch(e) {
            console.log(e);
            events.onError();
        }
    },

    load: function(mapId) {
        $assert(mapId, "mapId can not be null");
        var domDocument = this.loadMapDom(mapId);
        return  this.loadFromDom(mapId, domDocument);
    },

    discardChanges: function(mapId) {
        throw "Method must be implemented";
    },

    loadMapDom: function(mapId) {
        throw "Method must be implemented";
    },

    saveMapXml : function(mapId, mapXml, pref, saveHistory, events) {
        throw "Method must be implemented";
    },

    loadFromDom : function(mapId, mapDom) {
        $assert(mapId, "mapId can not be null");
        $assert(mapDom, "mapDom can not be null");

        var serializer = mindplot.persistence.XMLSerializerFactory.getSerializerFromDocument(mapDom);
        return serializer.loadFromDom(mapDom, mapId);
    },

    logEntry: function(severity, message) {
        throw "Method must be implemented";
    }
});

mindplot.PersistenceManager.init = function(instance) {
    mindplot.PersistenceManager._instance = instance;
};

mindplot.PersistenceManager.getInstance = function() {
    return mindplot.PersistenceManager._instance;
};

