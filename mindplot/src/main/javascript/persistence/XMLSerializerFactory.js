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

/**
 * @class mindplot.persistence.XMLSerializerFactory
 */
mindplot.persistence.XMLSerializerFactory = {};

/** 
 * @param {mindplot.model.IMindmap} mindmap
 * @return {mindplot.persistence.XMLSerializer_Beta|mindplot.persistence.XMLSerializer_Pela|
 * mindplot.persistence.XMLSerializer_Tango} serializer corresponding to the mindmap's version
 */
mindplot.persistence.XMLSerializerFactory.getSerializerFromMindmap = function(mindmap) {
    return mindplot.persistence.XMLSerializerFactory.getSerializer(mindmap.getVersion());
};

/**
 * @param domDocument
 * @return serializer corresponding to the mindmap's version
 */
mindplot.persistence.XMLSerializerFactory.getSerializerFromDocument = function(domDocument) {
    var rootElem = domDocument.documentElement;
    return mindplot.persistence.XMLSerializerFactory.getSerializer(rootElem.getAttribute("version"))
};

/**
 * retrieves the serializer for the mindmap's version and migrates to the current version,
 * e.g. for a Beta mindmap and current version Tango: 
 * serializer = new Pela2TangoMigrator(new Beta2PelaMigrator(new XMLSerializer_Beta()))
 * @param {String} version the version name
 * @return serializer
 */
mindplot.persistence.XMLSerializerFactory.getSerializer = function(version) {
    if (!$defined(version)) {
        version = mindplot.persistence.ModelCodeName.BETA;
    }
    var codeNames = mindplot.persistence.XMLSerializerFactory._codeNames;
    var found = false;
    var serializer = null;
    for (var i = 0; i < codeNames.length; i++) {
        if (!found) {
            found = codeNames[i].codeName == version;
            if (found)
                serializer = new (codeNames[i].serializer)();
        } else {
            var migrator = codeNames[i].migrator;
            serializer = new migrator(serializer);
        }
    }

    return serializer;
};

mindplot.persistence.XMLSerializerFactory._codeNames =
    [
        {
            codeName:mindplot.persistence.ModelCodeName.BETA,
            serializer: mindplot.persistence.XMLSerializer_Beta,
            migrator:function() {
            }
        },
        {
            codeName:mindplot.persistence.ModelCodeName.PELA,
            serializer:mindplot.persistence.XMLSerializer_Pela,
            migrator:mindplot.persistence.Beta2PelaMigrator
        },
        {
            codeName:mindplot.persistence.ModelCodeName.TANGO,
            serializer:mindplot.persistence.XMLSerializer_Tango,
            migrator:mindplot.persistence.Pela2TangoMigrator
        }
    ];