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

mindplot.DesignerModel = new Class(/** @lends DesignerModel */{
    Implements:[mindplot.Events],
    /**
     * @implements {mindplot.Events}
     * @constructs
     * @param {{readOnly: Boolean, zoom: Number, saveOnLoad: Boolean, size: {width: Number, 
     * height: Number}, viewPort: {width: Number, height: Number}, container: String, 
     * persistenceManager: String, mapId: String, locale: String}} options
     * options loaded from json config 
     * @see {@link ConfigParameters.md}
     * @see {@link editor.html}
     */
    initialize:function (options) {
        this._zoom = options.zoom;
        this._topics = [];
        this._relationships = [];
    },

    /** @return {Number} zoom between 0.3 (largest text) and 1.9 */
    getZoom:function () {
        return this._zoom;
    },

    /** @param {Number} zoom number between 0.3 and 1.9 to set the zoom to */
    setZoom:function (zoom) {
        this._zoom = zoom;
    },

    /** @return {@link mindplot.Topic[]} all topics */
    getTopics:function () {
        return this._topics;
    },

    /** @return {mindplot.Relationship[]} all relationships */
    getRelationships:function () {
        return this._relationships;
    },

    /** @return {mindplot.CentralTopic} the central topic */
    getCentralTopic:function () {
        var topics = this.getTopics();
        return topics[0];
    },

    /** @return {mindplot.Topic[]} selected topics */
    filterSelectedTopics:function () {
        var result = [];
        for (var i = 0; i < this._topics.length; i++) {
            if (this._topics[i].isOnFocus()) {
                result.push(this._topics[i]);
            }
        }
        return result;
    },

    /**
     * @return {mindplot.Relationship[]} selected relationships
     */
    filterSelectedRelationships:function () {
        var result = [];
        for (var i = 0; i < this._relationships.length; i++) {
            if (this._relationships[i].isOnFocus()) {
                result.push(this._relationships[i]);
            }
        }
        return result;
    },

    /**
     * @return {Array.<mindplot.Relationship, mindplot.Topic>} all topics and relationships
     */
    getEntities:function () {
        var result = [].append(this._topics);
        result.append(this._relationships);
        return result;
    },

    /**
     * removes occurrences of the given topic from the topic array
     * @param {mindplot.Topic} topic the topic to remove
     */
    removeTopic:function (topic) {
        $assert(topic, "topic can not be null");
        this._topics.erase(topic);
    },

    /**
     * removes occurrences of the given relationship from the relationship array
     * @param {mindplot.Relationship} rel the relationship to remove
     */
    removeRelationship:function (rel) {
        $assert(rel, "rel can not be null");
        this._relationships.erase(rel);
    },

    /**
     * adds the given topic to the topic array
     * @param {mindplot.Topic} topic the topic to add
     * @throws will throw an error if topic is null or undefined
     * @throws will throw an error if the topic's id is not a number
     */
    addTopic:function (topic) {
        $assert(topic, "topic can not be null");
        $assert(typeof  topic.getId() == "number", "id is not a number:" + topic.getId());
        this._topics.push(topic);
    },

    /**
     * adds the given relationship to the relationship array
     * @param {mindplot.Relationship} rel the relationship to add
     * @throws will throw an error if rel is null or undefined
     */
    addRelationship:function (rel) {
        $assert(rel, "rel can not be null");
        this._relationships.push(rel);
    },

    /**
     * @param {Function=} validate a function to validate nodes
     * @param {String=} errorMsg an error message to display if the validation fails
     * @return {String} returns an array of the selected (and, if applicable, valid) topics' ids
     */
    filterTopicsIds:function (validate, errorMsg) {
        var result = [];
        var topics = this.filterSelectedTopics();


        var isValid = true;
        for (var i = 0; i < topics.length; i++) {
            var selectedNode = topics[i];
            if ($defined(validate)) {
                isValid = validate(selectedNode);
            }

            // Add node only if it's valid.
            if (isValid) {
                result.push(selectedNode.getId());
            } else {
                $notify(errorMsg);
            }
        }
        return result;
    },

    /**
     * @return {mindplot.Topic} the first selected topic if one or more are found by the 
     * filterSelectedTopics function, null otherwise
     */
    selectedTopic:function () {
        var topics = this.filterSelectedTopics();
        return (topics.length > 0) ? topics[0] : null;
    },

    /**
     * @param {String} id the id of the topic to be retrieved
     * @return {mindplot.Topic} the topic with the respective id
     */
    findTopicById:function (id) {
        var result = null;
        for (var i = 0; i < this._topics.length; i++) {
            var topic = this._topics[i];
            if (topic.getId() == id) {
                result = topic;
                break;
            }
        }
        return result;

    }
});