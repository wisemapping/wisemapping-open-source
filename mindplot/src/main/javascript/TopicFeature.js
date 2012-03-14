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

mindplot.TopicFeature = {
    Icon: {
        id:mindplot.model.IconModel.FEATURE_TYPE,
        model: mindplot.model.IconModel,
        icon : mindplot.ImageIcon
    },

    Link: {
        id: mindplot.model.LinkModel.FEATURE_TYPE,
        model: mindplot.model.LinkModel,
        icon : mindplot.LinkIcon
    },

    Note: {
        id: mindplot.model.NoteModel.FEATURE_TYPE,
        model: mindplot.model.NoteModel,
        icon : mindplot.NoteIcon
    },

    isSupported : function(id) {
        return mindplot.TopicFeature._featuresMetadataById.some(function(elem) {
            return elem.id == id;
        });
    },

    createModel : function(id, topic, attributes) {
        $assert(id, 'type can not be null');
        $assert(topic, 'topic can not be null');
        $assert(attributes, 'attributes can not be null');

        var model = mindplot.TopicFeature._featuresMetadataById.filter(function(elem) {
            return elem.id == id;
        })[0].model;
        return new model(topic, attributes);
    },

    createIcon : function(topic, model) {
        $assert(topic, 'topic can not be null');
        $assert(model, 'model can not be null');

        var icon = mindplot.TopicFeature._featuresMetadataById.filter(function(elem) {
            return elem.id == model.getType();
        })[0].icon;
        return new icon(topic, model);
    }
};

mindplot.TopicFeature._featuresMetadataById = [mindplot.TopicFeature.Icon,mindplot.TopicFeature.Link,mindplot.TopicFeature.Note];


