/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;

public class JAXBUtils {

    public static Object getMapObject(InputStream stream,String pakage) throws JAXBException {

        final JAXBContext context = JAXBContext.newInstance(pakage);
        final Unmarshaller unmarshaller = context.createUnmarshaller() ;

        return unmarshaller.unmarshal (stream) ;
    }

    public static void saveMap(Object obj, OutputStream out,String pakage) throws JAXBException {

        final JAXBContext context = JAXBContext.newInstance(pakage);
        final Marshaller marshaller =   context.createMarshaller();

        marshaller.marshal(obj, out) ;
    }
}
