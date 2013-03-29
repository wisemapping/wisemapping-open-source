/*
*    Copyright [2012] [wisemapping]
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

import org.jetbrains.annotations.NotNull;

public enum ExportFormat {
    SVG("image/svg+xml", "svg"),
    JPG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    PDF("application/pdf", "pdf"),
    FREEMIND("application/freemind", "mm"),
    TEXT("text/plain", "txt"),
    MICROSOFT_EXCEL("application/vnd.ms-excel", "xls"),
    MICROSOFT_WORD("application/msword", "doc"),
    OPEN_OFFICE_WRITER("application/vnd.oasis.opendocument.text", "odt"),
    MINDJET("application/vnd.mindjet.mindmanager", "mmap"),
    WISEMAPPING("application/wisemapping+xml", "wxml");


    private String contentType;
    private String fileExtension;

    ExportFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public static ExportFormat fromContentType(@NotNull final String contentType) {
        final ExportFormat[] values = ExportFormat.values();
        for (ExportFormat value : values) {
            if (value.getContentType().equals(contentType)) {
                return value;
            }
        }
        throw new IllegalStateException("ComponentType could not be mapped:" + contentType);
    }
}
