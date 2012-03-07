package com.wisemapping.importer;

import java.io.OutputStream;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

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
                new String[]{"^text"});   //

        // set any other options you'd like
//        of.setPreserveSpace(true);
        of.setIndenting(true);

        // create the serializer
        XMLSerializer result = new XMLSerializer(of);
        result.setOutputByteStream(out);

        return result;
    }

}