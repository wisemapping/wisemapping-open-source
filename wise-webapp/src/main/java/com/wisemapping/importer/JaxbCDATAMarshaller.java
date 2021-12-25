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

package com.wisemapping.importer;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@SuppressWarnings("deprecation")
public class JaxbCDATAMarshaller {

    public static XMLSerializer createMindmapXMLSerializer(@NotNull OutputStream out) {
        // configure an OutputFormat to handle CDATA
        OutputFormat of = new OutputFormat();

        // specify which of your elements you want to be handled as CDATA.
        // The use of the '^' between the namespaceURI and the localname
        // seems to be an implementation detail of the xerces code.
        // When processing xml that doesn't use namespaces, simply omit the
        // namespace prefix as shown in the third CDataElement below.
        of.setCDataElements(
                new String[]{"^text","^note"});   //

        // set any other options you'd like
//        of.setPreserveSpace(true);
        of.setIndenting(true);
        of.setEncoding("UTF-8");

        // create the serializer
        XMLSerializer result = new XMLSerializer(of) {
            @Override
            public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
                super.startElement(s, s1, s2, new SortedAttributesDecorator(attributes));
            }
        };
        result.setOutputByteStream(out);

        return result;
    }

    private static class SortedAttributesDecorator implements Attributes {

        final Map<Integer, Integer> sortedToUnsorted = new HashMap<Integer, Integer>();
        final Map<Integer, Integer> unsortedToSorted = new HashMap<Integer, Integer>();

        private final Attributes delegated;

        SortedAttributesDecorator(final Attributes delegated) {
            this.delegated = delegated;
            int length = this.getLength();

            // Sort by local part ...
            final Map<String, Integer> sortedMap = new TreeMap<String, Integer>();
            for (int i = 0; i < length; i++) {
                final String localName = delegated.getLocalName(i);
                sortedMap.put(localName, i);
            }

            Set<String> keySet = sortedMap.keySet();
            int sortedIndex = 0;
            for (String key : keySet) {
                final Integer unsortedIndex = sortedMap.get(key);
                sortedToUnsorted.put(sortedIndex, unsortedIndex);
                unsortedToSorted.put(unsortedIndex, sortedIndex);
                sortedIndex++;
            }
        }

        @Override
        public int getLength() {
            return delegated.getLength();
        }

        @Override
        public String getURI(int index) {
            return delegated.getURI(sortedToUnsorted.get(index));
        }

        @Override
        public String getLocalName(int index) {
            return delegated.getLocalName(sortedToUnsorted.get(index));
        }

        @Override
        public String getQName(int index) {
            return delegated.getQName(sortedToUnsorted.get(index));
        }

        @Override
        public String getType(int index) {
            return delegated.getType(sortedToUnsorted.get(index));
        }

        @Override
        public String getValue(int index) {
            return delegated.getValue(sortedToUnsorted.get(index));
        }

        @Override
        public int getIndex(String uri, String localName) {
            int unsorted = delegated.getIndex(uri, localName);
            return unsortedToSorted.get(unsorted);
        }

        @Override
        public int getIndex(String qName) {
            int unsorted = delegated.getIndex(qName);
            return unsortedToSorted.get(unsorted);
        }

        @Override
        public String getType(String uri, String localName) {
            return delegated.getType(uri, localName);
        }

        @Override
        public String getType(String qName) {
            return delegated.getType(qName);
        }

        @Override
        public String getValue(String uri, String localName) {
            return delegated.getValue(uri, localName);
        }

        @Override
        public String getValue(String qName) {
            return delegated.getValue(qName);
        }
    }

}