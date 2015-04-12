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

mindplot.model.LinkModel = new Class(/** @lends LinkModel */{
    Extends:mindplot.model.FeatureModel,
    /**
     * @constructs
     * @param attributes
     * @extends mindplot.model.FeatureModel
     */
    initialize:function (attributes) {
        this.parent(mindplot.model.LinkModel.FEATURE_TYPE);
        this.setUrl(attributes.url);
    },

    /** @return {String} the url attribute value */
    getUrl:function () {
        return this.getAttribute('url');
    },

    /** 
     * @param {String} url a URL provided by the user to set the link to
     * @throws will throw an error if url is null or undefined
     */
    setUrl:function (url) {
        $assert(url, 'url can not be null');

        var fixedUrl = this._fixUrl(url);
        this.setAttribute('url', fixedUrl);

        var type = fixedUrl.contains('mailto:') ? 'mail' : 'url';
        this.setAttribute('urlType', type);

    },

    //url format is already checked in LinkEditor.checkUrl
    _fixUrl:function (url) {
        var result = url;
        if (!result.contains('http://') && !result.contains('https://') && !result.contains('mailto://')) {
            result = "http://" + result;
        }
        return result;
    },

    /**
     * @param {String} urlType the url type, either 'mail' or 'url'
     * @throws will throw an error if urlType is null or undefined 
     */
    setUrlType:function (urlType) {
        $assert(urlType, 'urlType can not be null');
        this.setAttribute('urlType', urlType);
    }
});

/**
 * @constant
 * @type {String}
 * @default
 */
mindplot.model.LinkModel.FEATURE_TYPE = 'link';