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

mindplot.collaboration.CollaborationManager = new Class({
    initialize:function() {
        this.collaborativeModelReady = false;
        this.collaborativeModelReady = null;
    },

    setCollaborativeFramework : function(framework) {
        this._collaborativeFramework = framework;
    },

    buildMindmap: function() {
        return this._collaborativeFramework.buildMindmap();
    },

    getCollaborativeFramework:function() {
        return this._collaborativeFramework;
    }

});

mindplot.collaboration.CollaborationManager.getInstance = function() {
    if (!$defined(mindplot.collaboration.CollaborationManager.__collaborationManager)) {
        mindplot.collaboration.CollaborationManager.__collaborationManager = new mindplot.collaboration.CollaborationManager();
    }
    return mindplot.collaboration.CollaborationManager.__collaborationManager;
};
mindplot.collaboration.CollaborationManager.getInstance();
