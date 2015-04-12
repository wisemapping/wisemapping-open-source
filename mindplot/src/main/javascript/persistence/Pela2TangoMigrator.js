/*
 *    Copyright [2015] [wisemapping]
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
mindplot.persistence.Pela2TangoMigrator = new Class({
    initialize : function(pelaSerializer) {
        this._pelaSerializer = pelaSerializer;
        this._tangoSerializer = new mindplot.persistence.XMLSerializer_Tango();
    },

    toXML : function(mindmap) {
        return this._tangoSerializer.toXML(mindmap);
    },

    loadFromDom : function(dom, mapId) {
        $assert($defined(mapId), "mapId can not be null");
        var mindmap = this._pelaSerializer.loadFromDom(dom, mapId);
        mindmap.setVersion(mindplot.persistence.ModelCodeName.TANGO);
        this._fixOrder(mindmap);
        this._fixPosition(mindmap);
        return mindmap;
    },

    _fixOrder : function(mindmap) {
        // First level node policies has been changed.
        var centralNode = mindmap.getBranches()[0];
        var children = centralNode.getChildren();
        var leftNodes = [];
        var rightNodes = [];
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            var position = child.getPosition();
            if (position.x < 0) {
                leftNodes.push(child);
            } else {
                rightNodes.push(child);
            }
        }
        rightNodes.sort(function(a, b) {
            return a.getOrder() > b.getOrder()
        });
        leftNodes.sort(function(a, b) {
            return a.getOrder() > b.getOrder();
        });

        for (i = 0; i < rightNodes.length; i++) {
            rightNodes[i].setOrder(i * 2);
        }

        for (i = 0; i < leftNodes.length; i++) {
            leftNodes[i].setOrder(i * 2 + 1);
        }
    },

    _fixPosition : function(mindmap) {
        // Position was not required in previous versions. Try to synthesize one .
        var centralNode = mindmap.getBranches()[0];
        var children = centralNode.getChildren();
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            var position = child.getPosition();
            this._fixNodePosition(child, position)

        }
    },
    _fixNodePosition : function(node, parentPosition) {
        // Position was not required in previous versions. Try to synthesize one .
        var position = node.getPosition();
        if (!position) {
            position = {x:parentPosition.x + 30,y:parentPosition.y};
            node.setPosition(position.x, position.y);
        }
        var children = node.getChildren();
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            this._fixNodePosition(child, position);

        }

    }
});
