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

import com.wisemapping.importer.VersionNumber;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.TransformListParser;
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
import sun.misc.BASE64Encoder;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.regex.Pattern;

public class ExporterFactory {
    private static final String GROUP_NODE_NAME = "g";
    private static final String IMAGE_NODE_NAME = "image";
    public static final int MARGING = 50;
    public static final String UTF_8_CHARSET_NAME = "UTF-8";
    private File baseImgDir;

    public ExporterFactory(@NotNull final ServletContext servletContext) throws ParserConfigurationException {
        this.baseImgDir = new File(servletContext.getRealPath("/"));
    }

    public ExporterFactory(@NotNull final File baseImgDir) throws ParserConfigurationException {
        this.baseImgDir = baseImgDir;
    }

    public void export(@NotNull ExportProperties properties, @Nullable String xml, @NotNull OutputStream output, @Nullable String mapSvg) throws ExportException, IOException, TranscoderException {
        final ExportFormat format = properties.getFormat();

        switch (format) {
            case PNG: {
                // Create a JPEG transcoder
                final Transcoder transcoder = new PNGTranscoder();
                final ExportProperties.ImageProperties imageProperties =
                        (ExportProperties.ImageProperties) properties;
                final ExportProperties.ImageProperties.Size size = imageProperties.getSize();
                transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, size.getWidth());

                // Create the transcoder input.
                final String svgString = normalizeSvg(mapSvg, false);
                final TranscoderInput input = new TranscoderInput(new CharArrayReader(svgString.toCharArray()));

                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case JPG: {
                // Create a JPEG transcoder
                final Transcoder transcoder = new JPEGTranscoder();
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.99));

                final ExportProperties.ImageProperties imageProperties =
                        (ExportProperties.ImageProperties) properties;
                final ExportProperties.ImageProperties.Size size = imageProperties.getSize();
                transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, size.getWidth());

                // Create the transcoder input.
                final String svgString = normalizeSvg(mapSvg, false);
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
                final String svgString = normalizeSvg(mapSvg, false);
                final TranscoderInput input = new TranscoderInput(new CharArrayReader(svgString.toCharArray()));
                TranscoderOutput trascoderOutput = new TranscoderOutput(output);

                // Save the image.
                transcoder.transcode(input, trascoderOutput);
                break;
            }
            case SVG: {
                final String svgString = normalizeSvg(mapSvg, true);
                output.write(svgString.getBytes(UTF_8_CHARSET_NAME));
                break;
            }
            case TEXT: {
                final Exporter exporter =  XSLTExporter.create(XSLTExporter.Type.TEXT);
                exporter.export(xml.getBytes(UTF_8_CHARSET_NAME), output);
                break;
            }
            case OPEN_OFFICE_WRITER: {
                final Exporter exporter =  XSLTExporter.create(XSLTExporter.Type.OPEN_OFFICE);
                exporter.export(xml.getBytes(UTF_8_CHARSET_NAME), output);
                break;
            }
            case MICROSOFT_EXCEL: {
                final Exporter exporter =  XSLTExporter.create(XSLTExporter.Type.MICROSOFT_EXCEL);
                exporter.export(xml.getBytes(UTF_8_CHARSET_NAME), output);
                break;
            }
            case FREEMIND: {
                final FreemindExporter exporter = new FreemindExporter();
                exporter.setVersion(new VersionNumber(properties.getVersion()));
                exporter.export(xml.getBytes(UTF_8_CHARSET_NAME), output);
                break;
            }
            case MINDJET: {
                final Exporter exporter =  XSLTExporter.create(XSLTExporter.Type.MINDJET);
                exporter.export(xml.getBytes(UTF_8_CHARSET_NAME), output);
                break;
            }
            default:
                throw new UnsupportedOperationException("Export method not supported.");
        }
    }

    private String normalizeSvg(@NotNull String svgXml, boolean embedImg) throws ExportException {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();

            if (!svgXml.contains("xmlns:xlink=\"http://www.w3.org/1999/xlink\"")) {
                svgXml = svgXml.replaceFirst("<svg ", "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
            }

            // Hacks for some legacy cases ....
            svgXml = svgXml.replaceAll("NaN,", "0");
            svgXml = svgXml.replaceAll(",NaN", "0");

            // Bratik do not manage nbsp properly.
            svgXml = svgXml.replaceAll(Pattern.quote("&nbsp;"), " ");

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

            resizeSVG(document);

            final Node child = document.getFirstChild();
            inlineImages(document, (Element) child);

            return domToString(document);
        } catch (ParserConfigurationException e) {
            throw new ExportException(e);
        } catch (IOException e) {
            throw new ExportException(e);
        } catch (SAXException e) {
            throw new ExportException(e);
        } catch (TransformerException e) {
            throw new ExportException(e);
        }

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

    private void inlineImages(@NotNull Document document, @NotNull Element element) {

        final NodeList list = element.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            // find all groups
            if (GROUP_NODE_NAME.equals(node.getNodeName())) {
                // Must continue looking ....
                inlineImages(document, (Element) node);

            } else if (IMAGE_NODE_NAME.equals(node.getNodeName())) {

                Element elem = (Element) node;

                // If the image is a external URL, embeed it...
                String imgUrl = elem.getAttribute("href");
                if (!imgUrl.startsWith("image/png;base64") ||!imgUrl.startsWith("data:image/png;base64") ) {
                    elem.removeAttribute("href");

                    if (imgUrl == null || imgUrl.isEmpty()) {
                        imgUrl = elem.getAttribute("xlink:href"); // Do not support namespaces ...
                        elem.removeAttribute("xlink:href");
                    }
                    FileInputStream fis = null;

                    // Obtains file name ...
                    try {
                        final File iconFile = iconFile(imgUrl);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        fis = new FileInputStream(iconFile);
                        BASE64Encoder encoder = new BASE64Encoder();
                        encoder.encode(fis, bos);

                        elem.setAttribute("xlink:href", "data:image/png;base64," + bos.toString("8859_1"));
                        elem.appendChild(document.createTextNode(" "));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        close(fis);
                    }
                }
            }
        }
    }

    private File iconFile(@NotNull final String imgUrl) throws IOException {
        int index = imgUrl.lastIndexOf("/");
        final String iconName = imgUrl.substring(index + 1);
        final File iconsDir = new File(baseImgDir, "icons");

        File iconFile = new File(iconsDir, iconName);
        if (!iconFile.exists()) {
            // It's not a icon, must be a note, attach image ...
            final File legacyIconsDir = new File(baseImgDir, "images");
            iconFile = new File(legacyIconsDir, iconName);
        }

        if (!iconFile.exists()) {
            final File legacyIconsDir = new File(iconsDir, "legacy");
            iconFile = new File(legacyIconsDir, iconName);
        }

        if (!iconFile.exists()) {
            throw new IOException("Icon could not be found:" + imgUrl);
        }

        return iconFile;
    }


    private void close(Closeable fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                // Ignore ...
            }
        }
    }

    private static void resizeSVG(@NotNull Document document) throws ExportException {

        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/svg/g/rect");

            NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            final int length = nl.getLength();
            double maxX = 0, minX = 0, minY = 0, maxY = 0;


            for (int i = 0; i < length; i++) {
                final Element rectElem = (Element) nl.item(i);
                final Element gElem = (Element) rectElem.getParentNode();


                final TransformListParser p = new TransformListParser();
                final AWTTransformProducer tp = new AWTTransformProducer();
                p.setTransformListHandler(tp);
                p.parse(gElem.getAttribute("transform"));
                final AffineTransform transform = tp.getAffineTransform();

                double yPos = transform.getTranslateY();
                if (yPos > 0) {
                    yPos += Double.parseDouble(rectElem.getAttribute("height"));
                }
                maxY = maxY < yPos ? yPos : maxY;
                minY = minY > yPos ? yPos : minY;

                double xPos = transform.getTranslateX();
                if (xPos > 0) {
                    xPos += Double.parseDouble(rectElem.getAttribute("width"));
                }

                maxX = maxX < xPos ? xPos : maxX;
                minX = minX > xPos ? xPos : minX;
            }

            // Add some extra margin ...
            maxX += MARGING;
            minX += -MARGING;

            maxY += MARGING;
            minY += -MARGING;

            // Calculate dimentions ...
            final double width = maxX + Math.abs(minX);
            final double height = maxY + Math.abs(minY);

            // Finally, update centers ...
            final Element svgNode = (Element) document.getFirstChild();

            svgNode.setAttribute("viewBox", minX + " " + minY + " " + width + " " + height);
            svgNode.setAttribute("width", Double.toString(width));
            svgNode.setAttribute("height", Double.toString(height));
            svgNode.setAttribute("preserveAspectRatio", "xMinYMin");
        } catch (XPathExpressionException e) {
            throw new ExportException(e);
        } catch (ParseException e) {
            throw new ExportException(e);
        } catch (NumberFormatException e) {
            throw new ExportException(e);
        } catch (DOMException e) {
            throw new ExportException(e);
        }
    }

    private static String[] getTransformUnit(NamedNodeMap groupAttributes) {
        final String value = groupAttributes.getNamedItem("transform").getNodeValue();
        final int pos = value.indexOf("translate");
        final int initTranslate = value.indexOf("(", pos);
        final int endTranslate = value.indexOf(")", pos);
        final String transate = value.substring(initTranslate + 1, endTranslate);
        return transate.contains(",") ? transate.split(",") : transate.split(" ");
    }

}
