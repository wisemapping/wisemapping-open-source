/*
 *    Copyright [2022] [wisemapping]
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

package com.wisemapping.mindmap.serializer;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.parser.MindmapParser;
import org.jetbrains.annotations.NotNull;

/**
 * XML implementation of MindmapSerializer.
 */
public class XmlMindmapSerializerImpl implements MindmapSerializer {
    
    @Override
    @NotNull
    public String serialize(@NotNull MapModel mapModel) throws SerializationException {
        try {
            return XmlMindmapSerializer.serializeToXml(mapModel);
        } catch (XmlMindmapSerializer.SerializationException e) {
            throw new SerializationException("Failed to serialize MapModel to XML", e);
        }
    }
    
    @Override
    @NotNull
    public MapModel deserialize(@NotNull String content) throws SerializationException {
        try {
            return MindmapParser.parseXml(content);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize MapModel from XML", e);
        }
    }
    
    @Override
    @NotNull
    public String getFormatName() {
        return "XML";
    }
    
    @Override
    @NotNull
    public String getMimeType() {
        return "application/xml";
    }
    
    @Override
    public boolean isFormat(@NotNull String content) {
        String trimmed = content.trim();
        return trimmed.startsWith("<map") && trimmed.endsWith("</map>");
    }
}
