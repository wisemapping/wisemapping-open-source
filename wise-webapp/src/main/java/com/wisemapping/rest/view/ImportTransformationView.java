package com.wisemapping.rest.view;


import com.wisemapping.importer.ImportFormat;
import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.ImporterFactory;
import com.wisemapping.model.MindMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class ImportTransformationView extends AbstractView {

    private String contentType;
    private Importer importer;

    public ImportTransformationView(@NotNull final String contentType) throws ImporterException {
        ImporterFactory exporterFactory = ImporterFactory.getInstance();
        importer = exporterFactory.getImporter(ImportFormat.FREEMIND);
        this.contentType = contentType;
    }

    @Override
    protected void renderMergedOutputModel(@NotNull Map<String, Object> viewMap, @NotNull HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
        final String content = (String) viewMap.get("content");
        final String filename = (String) viewMap.get("filename");

        // Convert to map ...
        final InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        final MindMap mindMap = importer.importMap("filename", "filename", is);

        // Set file name...
        final String fileName = (filename != null ? filename : "map") + "." + "xwise";
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // Write the conversion content ...
        final ServletOutputStream outputStream = response.getOutputStream();
        outputStream.print(mindMap.getXmlStr());
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
