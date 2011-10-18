/*
*    Copyright [2011] [wisemapping]
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

package com.wisemapping.controller;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExporterFactory;
import com.wisemapping.model.MindMap;
import com.wisemapping.service.MindmapService;
import com.wisemapping.view.MindMapBean;
import com.wisemapping.exporter.ExportProperties;
import com.wisemapping.filter.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.xml.sax.SAXException;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExportController extends BaseMultiActionController {
    private static final String IMG_EXPORT_FORMAT = "IMG_EXPORT_FORMAT";
    private static final String MAP_ID_PARAMETER = "mapId";
    private static final String MAP_SVG_PARAMETER = "mapSvg";
    private static final String EXPORT_FORMAT_PARAMETER = "exportFormat";
    private static final String IMG_SIZE_PARAMETER = "imgSize";
    private static final String MAP_XML_PARAM = "mapXml";


    public ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException noSuchRequestHandlingMethodException, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        logger.info("Mindmap Controller: EXPORT action");
        final MindMap mindmap = getMindmapFromRequest(httpServletRequest);
        return new ModelAndView("mindmapExport", "mindmap", new MindMapBean(mindmap));
    }

    public ModelAndView export(HttpServletRequest request, HttpServletResponse response) throws TranscoderException, IOException, JAXBException {
        logger.info("Export Controller: exporting WiseMap action");

        final String mapIdStr = request.getParameter(MAP_ID_PARAMETER);
        if (mapIdStr != null) {
            final String mapSvg = request.getParameter(MAP_SVG_PARAMETER);
            try {

                int mindmapId = Integer.parseInt(mapIdStr);

                logger.debug("SVG Map to export:" + mapSvg);
                if (mapSvg == null || mapSvg.isEmpty()) {
                    throw new IllegalArgumentException("SVG map could not be null");
                }

                String formatStr = request.getParameter(EXPORT_FORMAT_PARAMETER);
                if (IMG_EXPORT_FORMAT.endsWith(formatStr)) {
                    formatStr = request.getParameter("imgFormat");
                }

                final MindmapService service = getMindmapService();
                final MindMap mindMap = service.getMindmapById(mindmapId);

                // Build format properties ...
                final ExportFormat format = ExportFormat.valueOf(formatStr);
                final ExportProperties properties = ExportProperties.create(format);
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
                setBaseBaseImgUrl(format, properties);
                ExporterFactory.export(properties, mindMap, bos, mapSvg);

                // If the export goes ok, write the map to the stream ...

                // Set format content type...
                final String contentType = format.getContentType();
                response.setContentType(contentType);

                // Set file name...
                final String fileName = mindMap.getTitle() + "." + format.getFileExtension();
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

                // Write content ...
                final ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(bos.toByteArray());


            } catch (Throwable e) {
                logger.error("Unexpexted error during export process", e);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.error("map: " + mapSvg);
            }
        } else {
            logger.warn("mapIdStr is null.Image could not be imported. UserAgent:" + request.getHeaders(UserAgent.USER_AGENT_HEADER));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return null;
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

    public ModelAndView print(HttpServletRequest request, HttpServletResponse response) throws TranscoderException, IOException, JAXBException {
        logger.info("Export Controller: printing WiseMap action");

        final String mapIdStr = request.getParameter(MAP_ID_PARAMETER);
        int mindmapId = Integer.parseInt(mapIdStr);
        final MindmapService service = getMindmapService();
        final MindMap mindmap = service.getMindmapById(mindmapId);
        final String mapSvg = request.getParameter(MAP_SVG_PARAMETER);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            exportImage(response, mapSvg, bos, false);
        } catch (Throwable e) {
            logger.error("Unexpexted error generating the image", e);
            logger.error("map: " + mapSvg);
        }

        BASE64Encoder encoder = new BASE64Encoder();
        String content = encoder.encode(bos.toByteArray());
        final String exportContent = "data:image/png;base64," + content;
        bos.close();

        ModelAndView view = new ModelAndView("mindmapPrint", "mindmap", new MindMapBean(mindmap));
        final String xmlMap = mindmap.getNativeXmlAsJsLiteral();
        view.addObject(MAP_XML_PARAM, xmlMap);
        view.addObject(MAP_SVG_PARAMETER, exportContent);

        return view;

    }

    public ModelAndView image(HttpServletRequest request, HttpServletResponse response) throws TranscoderException, IOException, JAXBException {
        logger.info("Export Controller: generating image WiseMap action");

        final String mapIdStr = request.getParameter(MAP_ID_PARAMETER);
        final String mapSvg = request.getParameter(MAP_SVG_PARAMETER);
        try {
            final ServletOutputStream outputStream = response.getOutputStream();

            exportImage(response, mapSvg, outputStream, true);


        } catch (Throwable e) {
            logger.error("Unexpexted error generating the image", e);
            logger.error("map: " + mapSvg);
        }
        return null;
    }

    private void exportImage(HttpServletResponse response, String mapSvg, OutputStream outputStream, boolean setOutput) throws TranscoderException, IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException, JAXBException, ExportException {

        //Image Format
        ExportFormat imageFormat = ExportFormat.PNG;

        // Build format properties ...
        final ExportProperties.ImageProperties imageProperties = new ExportProperties.ImageProperties(imageFormat);
        imageProperties.setSize(ExportProperties.ImageProperties.Size.XMEDIUM);

        // Change image link URL.
        setBaseBaseImgUrl(imageFormat, imageProperties);

        // Set format content type...
        if (setOutput)
            response.setContentType(imageFormat.getContentType());

        // Write content ...
        ExporterFactory.export(imageProperties, null, outputStream, mapSvg);
    }
}