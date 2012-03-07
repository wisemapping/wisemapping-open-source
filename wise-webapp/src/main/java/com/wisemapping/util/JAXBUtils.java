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

package com.wisemapping.util;

import com.wisemapping.importer.JaxbCDATAMarshaller;
import org.apache.xml.serialize.XMLSerializer;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class JAXBUtils {

    private final static Map<String, JAXBContext> context = new HashMap<String, JAXBContext>();

    public static Object getMapObject(@NotNull InputStream stream, @NotNull final String pakage) throws JAXBException {

        final JAXBContext context = getInstance(pakage);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        return unmarshaller.unmarshal(stream);
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

    public static void saveMap(@NotNull Object obj, @NotNull OutputStream out, String packag) throws JAXBException {

        final JAXBContext context = getInstance(packag);
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
}
