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

mindplot.model.RelationshipModel = new Class(/** @lends RelationshipModel */{
    Static:{
        _nextUUID:function () {
            if (!$defined(mindplot.model.RelationshipModel._uuid)) {
                mindplot.model.RelationshipModel._uuid = 0;
            }
            mindplot.model.RelationshipModel._uuid = mindplot.model.RelationshipModel._uuid + 1;
            return mindplot.model.RelationshipModel._uuid;
        }
    },

    /**
     * @constructs
     * @param sourceTopicId
     * @param targetTopicId
     * @throws will throw an error if sourceTopicId is null or undefined
     * @throws will throw an error if targetTopicId is null or undefined
     */
    initialize:function (sourceTopicId, targetTopicId) {
        $assert($defined(sourceTopicId), 'from node type can not be null');
        $assert($defined(targetTopicId), 'to node type can not be null');

        this._id = mindplot.model.RelationshipModel._nextUUID();
        this._sourceTargetId = sourceTopicId;
        this._targetTopicId = targetTopicId;
        this._lineType = mindplot.ConnectionLine.SIMPLE_CURVED;
        this._srcCtrlPoint = null;
        this._destCtrlPoint = null;
        this._endArrow = true;
        this._startArrow = false;
    },

    /** */
    getFromNode:function () {
        return this._sourceTargetId;
    },

    /** */
    getToNode:function () {
        return this._targetTopicId;
    },

    /** */
    getId:function () {
        $assert(this._id, "id is null");
        return this._id;
    },

    /** */
    getLineType:function () {
        return this._lineType;
    },

    /** */
    setLineType:function (lineType) {
        this._lineType = lineType;
    },

    /** */
    getSrcCtrlPoint:function () {
        return this._srcCtrlPoint;
    },

    /** */
    setSrcCtrlPoint:function (srcCtrlPoint) {
        this._srcCtrlPoint = srcCtrlPoint;
    },

    /** */
    getDestCtrlPoint:function () {
        return this._destCtrlPoint;
    },

    /** */
    setDestCtrlPoint:function (destCtrlPoint) {
        this._destCtrlPoint = destCtrlPoint;
    },

    /** */
    getEndArrow:function () {
        return this._endArrow;
    },

    /** */
    setEndArrow:function (endArrow) {
        this._endArrow = endArrow;
    },

    /** */
    getStartArrow:function () {
        return this._startArrow;
    },

    /** */
    setStartArrow:function (startArrow) {
        this._startArrow = startArrow;
    },

    /**
     * @return a clone of the relationship model
     */
    clone:function () {
        var result = new mindplot.model.RelationshipModel(this._sourceTargetId, this._targetTopicId);
        result._id = this._id;
        result._lineType = this._lineType;
        result._srcCtrlPoint = this._srcCtrlPoint;
        result._destCtrlPoint = this._destCtrlPoint;
        result._endArrow = this._endArrow;
        result._startArrow = this._startArrow;
        return result;
    },

    /**
     * @return {String} textual information about the relationship's source and target node
     */
    inspect:function () {
        return '(fromNode:' + this.getFromNode().getId() + ' , toNode: ' + this.getToNode().getId() + ')';
    }
});


