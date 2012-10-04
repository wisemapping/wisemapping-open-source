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

package com.wisemapping.rest.view;

import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExportProperties;
import com.wisemapping.exporter.ExporterFactory;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.security.Utils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;
import java.util.Map;

public class TransformView extends AbstractView {

    private String contentType;
    private ExportFormat exportFormat;
    private NotificationService notificationService;

    @Autowired
    private Jaxb2Marshaller jaxbMarshaller;

    public TransformView(@NotNull final String contentType, @NotNull NotificationService notificationService) {
        this.contentType = contentType;
        this.notificationService = notificationService;
        this.exportFormat = ExportFormat.fromContentType(contentType);
    }

    @Override
    protected void renderMergedOutputModel(@NotNull Map<String, Object> viewMap, @NotNull HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {

        final String content = (String) viewMap.get("content");
        final String filename = (String) viewMap.get("filename");

        // Build format properties ...
        final ExportProperties properties = ExportProperties.create(exportFormat);
        if (properties instanceof ExportProperties.ImageProperties) {
            final String sizeStr = request.getParameter(IMG_SIZE_PARAMETER);
            final ExportProperties.ImageProperties imageProperties = (ExportProperties.ImageProperties) properties;
            if (sizeStr != null) {
                final ExportProperties.ImageProperties.Size size = ExportProperties.ImageProperties.Size.valueOf(sizeStr);
                imageProperties.setSize(size);
            } else {
                imageProperties.setSize(ExportProperties.ImageProperties.Size.MEDIUM);
            }
        }

        // Set format content type...
        final String contentType = exportFormat.getContentType();
        response.setContentType(contentType);

        // Set file name...
        final String fileName = (filename != null ? filename : "map") + "." + exportFormat.getFileExtension();
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // Change image link URL.
        final ServletContext servletContext = request.getSession().getServletContext();
        final ExporterFactory factory = new ExporterFactory(servletContext);
        try {
            // Write the conversion content ...
            final ServletOutputStream outputStream = response.getOutputStream();
            if (exportFormat == ExportFormat.FREEMIND) {
                factory.export(properties, content, outputStream, null);
            } else if (exportFormat == ExportFormat.WISEMAPPING) {
                final Object mindmap = viewMap.get("mindmap");
                final StreamResult result = new StreamResult(outputStream);
                jaxbMarshaller.marshal(mindmap, result);
            } else {
                factory.export(properties, null, outputStream, content);
            }
        } catch (Throwable e) {
            notificationService.reportJavaException(e, Utils.getUser(), content, request);
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    private static final String IMG_SIZE_PARAMETER = "imgSize";
}
