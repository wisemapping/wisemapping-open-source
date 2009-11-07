/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2007-08-01 19:08:21 (-0300), by: imanzano. $Revision$
// ...........................................................................................................

package com.wisemapping.dwr;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindMapNative;
import com.wisemapping.model.User;
import com.wisemapping.service.MindmapService;
import com.wisemapping.exceptions.WiseMappingException;

public class MapEditorService
        extends BaseDwrService {

    //~ Methods ..............................................................................................

    public ResponseMessage draftMap(final int mapId, final String nativeXml) {
        final ResponseMessage response = new ResponseMessage();
        response.setMsgCode(ResponseMessage.Code.OK.name());
        response.setMsgDetails("Map Saved Successfully");
        return response;
    }

    public ResponseMessage saveMap(final int mapId, final String nativeXml, final String chartType,
                                   String chartXml, final String editorProperties,boolean saveHistory)
            throws IOException, WiseMappingException {
        final MindmapService serservice = getMindmapService();
        final MindMap mindMap = serservice.getMindmapById(mapId);
        final User user = this.getUser();


        MindMapNative nativeBrowser = mindMap.getNativeBrowser();

        if (nativeBrowser == null) {
            nativeBrowser = new MindMapNative();
        }

        if ("SVG".equals(chartType)) {
            nativeBrowser.setSvgXml(chartXml);
            nativeBrowser.setVmlXml((byte[]) null);
        } else {
            nativeBrowser.setVmlXml(chartXml);
            nativeBrowser.setSvgXml((byte[]) null);
        }

        mindMap.setNativeBrowser(nativeBrowser);
        mindMap.setProperties(editorProperties);

        final Calendar now = Calendar.getInstance();
        mindMap.setLastModificationTime(now);
        mindMap.setLastModifierUser(user.getUsername());

        final Calendar lastModification = Calendar.getInstance();
        lastModification.setTime(new Date());
        mindMap.setLastModificationTime(lastModification);

        mindMap.setNativeXml(nativeXml);
        serservice.updateMindmap(mindMap,saveHistory);

        final ResponseMessage response = new ResponseMessage();
        response.setMsgCode(ResponseMessage.Code.OK.name());
        response.setMsgDetails("Map Saved Successfully");

        return response;
    }
}
