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

package com.wisemapping.rest.view;

import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExportProperties;
import com.wisemapping.exporter.ExporterFactory;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.security.Utils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class TransformView extends AbstractView {

    @NonNls
    private static final String DEFAULT_ENCODING = "UTF-8";
    private final String contentType;
    private final ExportFormat exportFormat;
    private final NotificationService notificationService;

    @Autowired
    private Jaxb2Marshaller jaxbMarshaller;

    public TransformView(@NotNull final String contentType, @NotNull NotificationService notificationService) {
        this.contentType = contentType;
        this.notificationService = notificationService;
        this.exportFormat = ExportFormat.fromContentType(contentType);
    }

    @Override
    protected void renderMergedOutputModel(@NotNull Map<String, Object> viewMap, @NotNull HttpServletRequest request, @NotNull final HttpServletResponse response) {

        final String content = (String) viewMap.get("content");
        final String filename = (String) viewMap.get("filename");
        final String version = (String) viewMap.get("version");

        // Build format properties ...
        final ExportProperties properties = ExportProperties.create(exportFormat);
        if (properties instanceof ExportProperties.ImageProperties) {
            final ExportProperties.ImageProperties imageProperties = (ExportProperties.ImageProperties) properties;
            imageProperties.setSize(ExportProperties.ImageProperties.Size.LARGE);
        }
        if (version != null) {
            properties.setVersion(version);
        }

        // Set format content type...
        final String contentType = exportFormat.getContentType();
        response.setContentType(contentType);

        // Set file name...:http://stackoverflow.com/questions/5325322/java-servlet-download-filename-special-characters/13359949#13359949
        final String fileName = (filename != null ? filename : "map") + "." + exportFormat.getFileExtension();
        this.setContentDisposition(request, response, fileName);

        // Change image link URL.
        final ServletContext servletContext = getServletContext();
        final ExporterFactory factory = new ExporterFactory(servletContext);
        try {
            // Write the conversion content ...
            final ServletOutputStream outputStream = response.getOutputStream();
            if (exportFormat == ExportFormat.FREEMIND) {
                response.setContentType(String.format("%s; charset=%s", contentType, "ASCII"));
                factory.export(properties, content, outputStream, null);
            } else if (exportFormat == ExportFormat.WISEMAPPING) {
                response.setContentType(String.format("%s; charset=%s", contentType, DEFAULT_ENCODING));
                final Object mindmap = viewMap.get("mindmap");
                final StreamResult result = new StreamResult(outputStream);
                jaxbMarshaller.marshal(mindmap, result);
            } else if (exportFormat == ExportFormat.MICROSOFT_EXCEL || exportFormat == ExportFormat.TEXT || exportFormat == ExportFormat.OPEN_OFFICE_WRITER || exportFormat == ExportFormat.MINDJET) {

                response.setContentType(String.format("%s; charset=%s", contentType, DEFAULT_ENCODING));
                factory.export(properties, content, outputStream, null);
            } else {
                // Image export ...
                factory.export(properties, null, outputStream, content);
            }
        } catch (Throwable e) {
            notificationService.reportJavaException(e, Utils.getUser(), content, request);
        }
    }

    private void setContentDisposition(HttpServletRequest request, HttpServletResponse response, String fileName) {
        final String userAgent = request.getHeader("user-agent");
        boolean isInternetExplorer = (userAgent.contains("MSIE"));

        String disposition = fileName;
        try {
            byte[] fileNameBytes = fileName.getBytes((isInternetExplorer) ? ("windows-1250") : ("utf-8"));
            final StringBuilder dispositionFileName = new StringBuilder();
            for (byte b : fileNameBytes) {
                dispositionFileName.append((char) (b & 0xff));
            }
            disposition = "attachment; filename=\"" + dispositionFileName + "\"";
        } catch (UnsupportedEncodingException ence) {
            // ... handle exception ...
        }
        response.setHeader("Content-disposition", disposition);
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
