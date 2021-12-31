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

import com.wisemapping.importer.VersionNumber;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public class ExporterFactory {

    private static final String GROUP_NODE_NAME = "g";
    private static final String IMAGE_NODE_NAME = "image";
    private static final int MANGING = 50;
    private final File baseImgDir;

    public ExporterFactory(@NotNull final ServletContext servletContext) {
        this.baseImgDir = new File(servletContext.getRealPath("/"));
    }

    public ExporterFactory(@NotNull final File baseImgDir) {
        this.baseImgDir = baseImgDir;
    }

    public void export(@NotNull ExportProperties properties, @Nullable String xml, @NotNull OutputStream output, @Nullable String mapSvg) throws ExportException, IOException {
        final ExportFormat format = properties.getFormat();


        switch (format) {
            case TEXT: {
                final Exporter exporter = XSLTExporter.create(XSLTExporter.Type.TEXT);
                exporter.export(xml.getBytes(StandardCharsets.UTF_8), output);
                break;
            }
            case MICROSOFT_EXCEL: {
                final Exporter exporter = XSLTExporter.create(XSLTExporter.Type.MICROSOFT_EXCEL);
                exporter.export(xml.getBytes(StandardCharsets.UTF_8), output);
                break;
            }
            case FREEMIND: {
                final FreemindExporter exporter = new FreemindExporter();
                exporter.setVersion(new VersionNumber(properties.getVersion()));
                exporter.export(xml.getBytes(StandardCharsets.UTF_8), output);
                break;
            }
            case MINDJET: {
                final Exporter exporter = XSLTExporter.create(XSLTExporter.Type.MINDJET);
                exporter.export(xml.getBytes(StandardCharsets.UTF_8), output);
                break;
            }
            default:
                throw new UnsupportedOperationException("Export method not supported.");
        }

        output.flush();
        output.close();
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

                final String imgUrl = fixHref(elem);
                if (!imgUrl.isEmpty() && (!imgUrl.startsWith("image/png;base64") || !imgUrl.startsWith("data:image/png;base64"))) {
                    elem.removeAttribute("href");

                    InputStream fis = null;
                    // Obtains file name ...
                    try {
                        final File iconFile = iconFile(imgUrl);
                        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        fis = new FileInputStream(iconFile);
                        Base64.Encoder enc = Base64.getEncoder();
                        bos.write(enc.encode(IOUtils.toByteArray(fis)));

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

    @NotNull
    private String fixHref(@NotNull Element elem) {
        // Fix href attribute ...
        // Hack for IE: If the image is a external URL, embeed it...
        String result = elem.getAttribute("href");
        if (result.isEmpty()) {


            // Bug WISE-422: This seems to be a bug in Safari. For some reason, img add prefixed with NS1
            // <image NS1:href="icons/sign_help.png"
            // Also: Remove replace "xlink:href" to href to uniform ...
            final NamedNodeMap attributes = elem.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node node = attributes.item(i);
                String nodeName = node.getNodeName();
                if(nodeName.contains(":href")){
                    elem.removeAttribute(nodeName);
                    result = node.getNodeValue();
                }
            }

            elem.setAttribute("href", result);
        }
        return result;
    }

    private File iconFile(@NotNull final String imgUrl) throws IOException {
        int index = imgUrl.lastIndexOf("/");
        final String iconName = imgUrl.substring(index + 1);
        final File iconsDir = new File(baseImgDir, "icons");

        File result = new File(iconsDir, iconName);
        if (!result.exists()) {
            // It's not a icon, must be a note, attach image ...
            final File legacyIconsDir = new File(baseImgDir, "images");
            result = new File(legacyIconsDir, iconName);
        }

        if (!result.exists()) {
            final File legacyIconsDir = new File(iconsDir, "legacy");
            result = new File(legacyIconsDir, iconName);
        }

        if (!result.exists() || result.isDirectory()) {

            throw new IOException("Icon could not be found:" + imgUrl);
        }

        return result;
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
}
