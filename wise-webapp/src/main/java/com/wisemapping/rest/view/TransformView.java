package com.wisemapping.rest.view;


import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExportProperties;
import com.wisemapping.exporter.ExporterFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class TransformView extends AbstractView {

    private String contentType;
    private ExportFormat exportFormat;

    public TransformView(@NotNull final String contentType) {
        this.contentType = contentType;
        this.exportFormat = ExportFormat.fromContentType(contentType);
    }

    @Override
    protected void renderMergedOutputModel(@NotNull Map<String, Object> viewMap, @NotNull HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {

        final String content = (String) viewMap.get("content");

        // Build format properties ...
        final ExportProperties properties = ExportProperties.create(exportFormat);
        if (properties instanceof ExportProperties.ImageProperties) {
            final String sizeStr = request.getParameter(IMG_SIZE_PARAMETER);
            final ExportProperties.ImageProperties imageProperties = (ExportProperties.ImageProperties) properties;
            if (sizeStr != null) {
                final ExportProperties.ImageProperties.Size size = ExportProperties.ImageProperties.Size.valueOf(sizeStr);
                imageProperties.setSize(size);
            } else {
                imageProperties.setSize(ExportProperties.ImageProperties.Size.LARGE);
            }
        }

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Change image link URL.
        setBaseBaseImgUrl(exportFormat, properties);
        if (exportFormat == ExportFormat.FREEMIND) {
            ExporterFactory.export(properties, content, bos, null);
        } else {
            ExporterFactory.export(properties, null, bos, content);
        }

        // Set format content type...
        final String contentType = exportFormat.getContentType();
        response.setContentType(contentType);

        // Set file name...
        final String fileName = "map" + "." + exportFormat.getFileExtension();
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // Write content ...
        final ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(bos.toByteArray());
    }

    @Override
    public String getContentType() {
        return contentType;
    }


    private void setBaseBaseImgUrl(@NotNull ExportFormat format, @NotNull ExportProperties properties) {

        final String baseUrl;
        if (format == ExportFormat.SVG) {
            baseUrl = "http://www.wisemapping.com/images";
        } else {
            final ServletContext servletContext = this.getServletContext();
            baseUrl = "file://" + servletContext.getRealPath("/icons/") + "/";
        }
        properties.setBaseImagePath(baseUrl);
    }

    private static final String IMG_SIZE_PARAMETER = "imgSize";

}
