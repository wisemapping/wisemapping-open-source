/*
 *    Copyright [2012] [wisemapping]
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

mindplot.commands.AddFeatureToTopicCommand = new Class({
    Extends:mindplot.Command,
    initialize:function (topicId, featureType, attributes) {

        $assert($defined(topicId), 'topicId can not be null');
        $assert(featureType, 'featureType can not be null');
        $assert(attributes, 'attributes can not be null');

        this.parent();
        this._topicId = topicId;
        this._featureType = featureType;
        this._attributes = attributes;
        this._featureModel = null;
    },

    execute:function (commandContext) {
        var topic = commandContext.findTopics(this._topicId)[0];

        // Feature must be created only one time.
        if (!this._featureModel) {
            var model = topic.getModel();
            this._featureModel = model.createFeature(this._featureType, this._attributes);
        }
        topic.addFeature(this._featureModel);
    },

    undoExecute:function (commandContext) {
        var topic = commandContext.findTopics(this._topicId)[0];
        topic.removeFeature(this._featureModel);
    }
});