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

package com.wisemapping.util;

import com.wisemapping.importer.JaxbCDATAMarshaller;
import org.apache.xml.serialize.XMLSerializer;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JAXBUtils {

    private final static Map<String, JAXBContext> context = new HashMap<String, JAXBContext>();

    public static Object getMapObject(@NotNull InputStream is, @NotNull final String pakage) throws JAXBException {

        final JAXBContext context = getInstance(pakage);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        return unmarshaller.unmarshal(reader);
    }

    private static JAXBContext getInstance(@NotNull String pakage) throws JAXBException {

        JAXBContext result = context.get(pakage);
        if (result == null) {
            synchronized (context) {
                result = JAXBContext.newInstance(pakage);
                context.put(pakage, result);
            }
        }
        return result;

    }

    public static void saveMap(@NotNull com.wisemapping.jaxb.wisemap.Map obj, @NotNull OutputStream out) throws JAXBException {

        final JAXBContext context = getInstance("com.wisemapping.jaxb.wisemap");
        final Marshaller marshaller = context.createMarshaller();

        // get an Apache XMLSerializer configured to generate CDATA
        XMLSerializer serializer = JaxbCDATAMarshaller.createMindmapXMLSerializer(out);

        try {
            // marshal using the Apache XMLSerializer
            marshaller.marshal(obj, serializer.asContentHandler());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void saveMap(@NotNull com.wisemapping.jaxb.freemind.Map map, @NotNull OutputStream out) throws JAXBException {

        final JAXBContext context = getInstance("com.wisemapping.jaxb.freemind");
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "ASCII");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        marshaller.marshal(map, out);
    }
}
