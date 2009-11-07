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

package com.wisemapping.exporter;

public class ExportProperties {
    private ExportFormat format;
    private String baseImgPath;

    public ExportFormat getFormat() {
        return format;
    }

    private ExportProperties(final ExportFormat format) {
        this.format = format;
    }

    public static ExportProperties create(final ExportFormat format) {
        ExportProperties result;
        if (format == ExportFormat.JPEG || format == ExportFormat.PNG) {
            result = new ImageProperties(format);
        } else {
            result = new GenericProperties(format);
        }
        return result;
    }

    public void setBaseImagePath(String baseUrl) {
        this.baseImgPath = baseUrl;
    }

    public String getBaseImgPath() {
        return baseImgPath;
    }

    static public class GenericProperties extends ExportProperties {
        private GenericProperties(ExportFormat format) {
            super(format);
        }
    }

    static public class ImageProperties extends ExportProperties {
        private Size size;

        public Size getSize() {
            return size;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public ImageProperties(ExportFormat format) {
            super(format);
        }

        public enum Size {
            SMALL(100), MEDIUM(800), XMEDIUM(1024), LARGE(2048);
            private int width;

            Size(int width) {
                this.width = width;
            }

            public Float getWidth() {
                return (float) width;
            }
        }
    }

}
