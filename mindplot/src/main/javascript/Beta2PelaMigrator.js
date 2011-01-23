/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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
mindplot.Beta2PelaMigrator = function(betaSerializer){
    this._betaSerializer=betaSerializer;
    this._pelaSerializer = new mindplot.XMLMindmapSerializer_Pela();
};

mindplot.Beta2PelaMigrator.prototype.toXML = function(mindmap)
{
    return this._pelaSerializer.toXML(mindmap);
};


mindplot.Beta2PelaMigrator.prototype.loadFromDom = function(dom)
{
    var mindmap = this._betaSerializer.loadFromDom(dom);
    mindmap.setVersion(mindplot.ModelCodeName.PELA);
    return mindmap;
};
