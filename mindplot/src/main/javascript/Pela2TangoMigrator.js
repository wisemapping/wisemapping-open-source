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
mindplot.Pela2TangoMigrator = new Class({
    initialize : function(pelaSerializer) {
        this._pelaSerializer = pelaSerializer;
        this._tangoSerializer = new mindplot.XMLMindmapSerializer_Tango();
    },

    toXML : function(mindmap) {
        return this._tangoSerializer.toXML(mindmap);
    },

    loadFromDom : function(dom, mapId) {
        $assert($defined(mapId), "mapId can not be null");
        var mindmap = this._pelaSerializer.loadFromDom(dom, mapId);
        mindmap.setVersion(mindplot.ModelCodeName.TANGO);

        // @todo: Cocinar los ordenes ....

        return mindmap;
    }
});
