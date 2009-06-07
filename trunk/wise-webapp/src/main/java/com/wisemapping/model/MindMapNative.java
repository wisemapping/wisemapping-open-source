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

package com.wisemapping.model;

import com.wisemapping.util.ZipUtils;

import java.io.IOException;

/**
 * This class contains the SVG and VML representation of the MindMap
 */
public class MindMapNative {
    private int id;

    private byte[] svgXml;
    private byte[] vmlXml;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getSvgXml() {
        return svgXml;
    }

    public void setSvgXml(byte[] svgXml) {
        this.svgXml = svgXml;
    }

    public byte[] getVmlXml() {
        return vmlXml;
    }

    public void setVmlXml(byte[] vmlXml) {
        this.vmlXml = vmlXml;
    }

    public String getUnzippedVmlXml()
            throws IOException
    {
        return ZipUtils.zipToString(vmlXml);
    }

    public String getUnzippedSvgXml()
            throws IOException
    {
        return ZipUtils.zipToString(svgXml);
    }

    public void setVmlXml(String xml) throws IOException {
        // compress and set
        vmlXml = ZipUtils.stringToZip(xml);
    }

     public void setSvgXml(String xml) throws IOException {
        // compress and set
        svgXml = ZipUtils.stringToZip(xml);
    }    
}
