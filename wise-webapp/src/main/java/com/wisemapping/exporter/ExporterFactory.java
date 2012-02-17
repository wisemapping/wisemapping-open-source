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

package com.wisemapping.exporter;

import com.wisemapping.model.MindMap;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class ExporterFactory {
    private static final String GROUP_NODE_NAME = "g";
    private static final String RECT_NODE_NAME = "rect";
    private static final String IMAGE_NODE_NAME = "image";


    private ExporterFactory() throws ParserConfigurationException {

    }

    public static void export(@NotNull ExportProperties properties, @NotNull String xml, @NotNull OutputStream output, @NotNull String mapSvg) throws TranscoderException, IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException, JAXBException, ExportException {
        final ExportFormat format = properties.getFormat();

        final String imgPath = properties.getBaseImgPath();
        switch (format) {
            case PNG: {
                // Create a JPEG transcoder
                final Transcoder transcoder = new PNGTranscoder();
                final ExportProperties.ImageProperties imageProperties =
                        (ExportProperties.ImageProperties) properties;
                final ExportProperties.ImageProperties.Size size = imageProperties.getSize();
                transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, size.getWidth());

                // Create the transcoder input.
                final Document document = normalizeSvg(mapSvg, imgPath);
                final String svgString = domToString(document);
                final TranscoderInput input = new TranscoderInput(new CharArrayReader(svgString.toCharArray()));

                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case JPEG: {
                // Create a JPEG transcoder
                final Transcoder transcoder = new JPEGTranscoder();
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.99));

                final ExportProperties.ImageProperties imageProperties =
                        (ExportProperties.ImageProperties) properties;
                final ExportProperties.ImageProperties.Size size = imageProperties.getSize();
                transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, size.getWidth());

                // Create the transcoder input.
                final Document document = normalizeSvg(mapSvg, imgPath);
                final String svgString = domToString(document);
                final TranscoderInput input = new TranscoderInput(new CharArrayReader(svgString.toCharArray()));

                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case PDF: {
                // Create a JPEG transcoder
                final Transcoder transcoder = new PDFTranscoder();

                // Create the transcoder input.
                final Document document = normalizeSvg(mapSvg, imgPath);
                final String svgString = domToString(document);
                final TranscoderInput input = new TranscoderInput(new CharArrayReader(svgString.toCharArray()));

                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case SVG: {
                final Document dom = normalizeSvg(mapSvg, imgPath);
                output.write(domToString(dom).getBytes("UTF-8"));
                break;
            }
            case FREEMIND: {
                final FreemindExporter exporter = new FreemindExporter();
                exporter.export(xml.getBytes(), output);
                break;
            }
            default:
                throw new UnsupportedOperationException("Export method not supported.");
        }
    }

    private static Document normalizeSvg(@NotNull String svgXml, final String imgBaseUrl) throws XMLStreamException, ParserConfigurationException, IOException, SAXException, TransformerException {

        final DocumentBuilder documentBuilder = getDocumentBuilder();
        svgXml = svgXml.replaceFirst("<svg ", "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
        // @Todo: This must not happen...
        svgXml = svgXml.replaceAll("NaN,", "0");
        svgXml = svgXml.replaceAll(",NaN", "0");

        Document document;
        try {
            final Reader in = new CharArrayReader(svgXml.toCharArray());
            final InputSource is = new InputSource(in);

            document = documentBuilder.parse(is);
        } catch (SAXException e) {
            // It must be a corrupted SVG format. Try to hack it and try again ...
            svgXml = svgXml.replaceAll("<image([^>]+)>", "<image$1/>");

            final Reader in = new CharArrayReader(svgXml.toCharArray());
            final InputSource is = new InputSource(in);
            document = documentBuilder.parse(is);
        }


        fitSvg(document);

        fixImageTagHref(document, imgBaseUrl);

        return document;

    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    private static String domToString(@NotNull Document document) throws TransformerException {
        DOMSource domSource = new DOMSource(document);

        // Create a string writer
        final CharArrayWriter result = new CharArrayWriter();

        // Create the stream stream for the transform
        StreamResult stream = new StreamResult(result);

        // Create a Transformer to serialize the document
        TransformerFactory tFactory = TransformerFactory.newInstance();

        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");

        // Transform the document to the stream stream
        transformer.transform(domSource, stream);

        return result.toString();
    }

    private static void fixImageTagHref(Document document, String imgBaseUrl) {
        final Node child = document.getFirstChild();
        fixImageTagHref(document, (Element) child, imgBaseUrl);
    }

    private static void fixImageTagHref(@NotNull Document document, @NotNull Element element, String imgBaseUrl) {

        final NodeList list = element.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            // find all groups
            if (GROUP_NODE_NAME.equals(node.getNodeName())) {
                // Must continue looking ....
                fixImageTagHref(document, (Element) node, imgBaseUrl);

            } else if (IMAGE_NODE_NAME.equals(node.getNodeName())) {

                Element elem = (Element) node;

                // Cook image href ...
                final String imgUrl = elem.getAttribute("href");
                int index = imgUrl.lastIndexOf("/");
                elem.removeAttribute("href");
                if (index != -1) {
                    final String iconName = imgUrl.substring(index+1);
                    // Hack for backward compatibility . This can be removed in 2012. :)
                    String imgPath;
                    if (imgUrl.contains("images")) {
                        imgPath = imgBaseUrl + "/../icons/legacy/" + iconName;
                    } else {
                        imgPath = imgBaseUrl + "/" + imgUrl;
                    }
                    elem.setAttribute("xlink:href", imgPath);
                    elem.appendChild(document.createTextNode(" "));
                }
            }
        }
    }

    private static void fitSvg(Document document) {
        // viewBox size
        int mapWidth = 1024;
        int mapHeight = 768;
        // some browser return width and heigth with precision
        float currentMaxWidth = 0;
        float currentMaxHeight = 0;

        final Element svgNode = (Element) document.getFirstChild();
        final NodeList list = svgNode.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            // find all groups
            if (GROUP_NODE_NAME.equals(node.getNodeName())) {
                final NamedNodeMap groupAttributes = node.getAttributes();

                final String[] transformUnit = getTransformUnit(groupAttributes);

                int groupPositionX = Integer.parseInt(transformUnit[0].trim());
                int groupPositionY = 0;
                if (transformUnit.length > 1) {
                    groupPositionY = Integer.parseInt(transformUnit[1].trim());
                }

                int signumX = Integer.signum(groupPositionX);
                int signumY = Integer.signum(groupPositionY);

                final NodeList groupChildren = node.getChildNodes();
                for (int idx = 0; idx < groupChildren.getLength(); idx++) {
                    final Node rectNode = groupChildren.item(idx);
                    float curentHeight = 0;
                    float curentWidth = 0;

                    // If has a rect use the rect to calcular the real width of the topic
                    if (RECT_NODE_NAME.equals(rectNode.getNodeName())) {
                        final NamedNodeMap rectAttributes = rectNode.getAttributes();

                        final Node attributeHeight = rectAttributes.getNamedItem("height");
                        final Node attributeWidth = rectAttributes.getNamedItem("width");

                        curentHeight = Float.valueOf(attributeHeight.getNodeValue());
                        curentWidth = Float.valueOf(attributeWidth.getNodeValue());
                    }

                    float newMaxWidth = groupPositionX + (curentWidth * signumX);
                    if (Math.abs(currentMaxWidth) < Math.abs(newMaxWidth)) {
                        currentMaxWidth = newMaxWidth;
                    }

                    float newMaxHeight = groupPositionY + curentHeight * signumY;
                    if (Math.abs(currentMaxHeight) < Math.abs(newMaxHeight)) {
                        currentMaxHeight = newMaxHeight;
                    }
                }
            }
        }

        svgNode.setAttribute("viewBox", -Math.abs(currentMaxWidth) + " " + -Math.abs(currentMaxHeight) + " " + Math.abs(currentMaxWidth * 2) + " " + Math.abs(currentMaxHeight * 2));
        svgNode.setAttribute("width", Float.toString(mapWidth / 2));
        svgNode.setAttribute("height", Float.toString(mapHeight / 2));
        svgNode.setAttribute("preserveAspectRatio", "xMinYMin");
    }

    private static String[] getTransformUnit(NamedNodeMap groupAttributes) {
        final String value = groupAttributes.getNamedItem("transform").getNodeValue();
        final int pos = value.indexOf("translate");
        final int initTranslate = value.indexOf("(", pos);
        final int endTranslate = value.indexOf(")", pos);
        final String transate = value.substring(initTranslate + 1, endTranslate);
        return transate.split(",");
    }

}
