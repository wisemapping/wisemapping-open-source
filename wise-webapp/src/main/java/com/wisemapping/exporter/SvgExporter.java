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

import com.wisemapping.exporter.freemind.FreemindExporter;
import com.wisemapping.model.MindMap;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
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

public class SvgExporter {
    private static final String GROUP_NODE_NAME = "g";
    private static final String RECT_NODE_NAME = "rect";
    private static final String IMAGE_NODE_NAME = "image";

    private SvgExporter() {
    }

    public static void export(ExportProperties properties, MindMap map, OutputStream output, String mapSvg) throws TranscoderException, IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException, JAXBException, ExportException {
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
                char[] xml = map.generateSvgXml(mapSvg);
                xml = normalizeSvg(xml, imgPath);
                final CharArrayReader is = new CharArrayReader(xml);
                TranscoderInput input = new TranscoderInput(is);
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
                final char[] xml = map.generateSvgXml(mapSvg);
                char[] svgXml = normalizeSvg(xml, imgPath);
                final CharArrayReader is = new CharArrayReader(svgXml);
                TranscoderInput input = new TranscoderInput(is);
                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case PDF: {
                // Create a JPEG transcoder
                final Transcoder transcoder = new PDFTranscoder();

                // Create the transcoder input.
                final char[] xml = map.generateSvgXml(mapSvg);
                char[] svgXml = normalizeSvg(xml, imgPath);
                final CharArrayReader is = new CharArrayReader(svgXml);
                TranscoderInput input = new TranscoderInput(is);
                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case SVG: {
                final char[] xml = map.generateSvgXml(mapSvg);
                char[] svgXml = normalizeSvg(xml, imgPath);
                output.write(new String(svgXml).getBytes("UTF-8"));
                break;
            }
            case FREEMIND: {
                final FreemindExporter exporter = new FreemindExporter();
                exporter.export(map.getUnzippedXml().getBytes(), output);
                break;
            }
            default:
                throw new UnsupportedOperationException("Export method not supported.");
        }
    }

    private static char[] normalizeSvg(final char[] svgXml, final String imgBaseUrl) throws XMLStreamException, ParserConfigurationException, IOException, SAXException, TransformerException {
        final Reader in = new CharArrayReader(svgXml);

        // Load document ...
        final InputSource is = new InputSource(in);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        final Document svgDocument = documentBuilder.parse(is);

        fitSvg(svgDocument);

        fixImageTagHref(svgDocument, imgBaseUrl);

        DOMSource domSource = new DOMSource(svgDocument);

        // Save document ...

        // Create a string writer
        final CharArrayWriter outDocument = new CharArrayWriter();

        // Create the result stream for the transform
        StreamResult result = new StreamResult(outDocument);

        // Create a Transformer to serialize the document
        TransformerFactory tFactory = TransformerFactory.newInstance();

        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");

        // Transform the document to the result stream
        transformer.transform(domSource, result);

        return outDocument.toCharArray();

    }

    private static void fixImageTagHref(Document svgDocument, String imgBaseUrl) {
        final Node child = svgDocument.getFirstChild();
        fixImageTagHref((Element) child, imgBaseUrl);
    }

    private static void fixImageTagHref(Element element, String imgBaseUrl) {

        final NodeList list = element.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            // find all groups
            if (GROUP_NODE_NAME.equals(node.getNodeName())) {
                // Must continue looking ....
                fixImageTagHref((Element) node, imgBaseUrl);

            } else if (IMAGE_NODE_NAME.equals(node.getNodeName())) {

                Element elem = (Element) node;

                // Cook image href ...
                String imgName = elem.getAttribute("href");
                int index = imgName.lastIndexOf("/");
                elem.removeAttribute("href");
                if (index != -1)
                {
                    imgName = imgName.substring(index);
                    final String imgPath = imgBaseUrl + imgName;                    
                    elem.setAttribute("xlink:href", imgPath);
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
                    float curentHeight = 0 ;
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
