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

package com.wisemapping.exporter;

public class ExportProperties {
    private ExportFormat format;
    private String version;

    public ExportFormat getFormat() {
        return format;
    }

    private ExportProperties(final ExportFormat format) {
        this.format = format;
    }

    public static ExportProperties create(final ExportFormat format) {
        ExportProperties result;
        if (format == ExportFormat.JPG || format == ExportFormat.PNG) {
            result = new ImageProperties(format);
        } else {
            result = new GenericProperties(format);
        }
        return result;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
