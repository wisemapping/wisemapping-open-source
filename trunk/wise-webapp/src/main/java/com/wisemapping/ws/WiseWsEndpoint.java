/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

package com.wisemapping.ws;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.apache.log4j.Logger;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.exceptions.NoMapFoundException;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;

/**
 * WiseMapping Web Services API 
 */
@Endpoint
public class WiseWsEndpoint {

    MindmapService mindmapService;
    private UserService userService;
    final static Logger logger = Logger.getLogger("org.wisemapping.ws");
    private JAXBContext jaxbContext;


    public WiseWsEndpoint(MindmapService mindmapService, UserService userService) throws JAXBException {
        this.mindmapService = mindmapService;
        this.userService = userService;
        jaxbContext = JAXBContext.newInstance("com.wisemapping.ws");
    }

    @PayloadRoot(localPart = "loadMindmapRequest", namespace = "http://www.wisemapping.org/ws")
    public LoadMindmapResponse loadMindmap(final LoadMindmapRequest request) throws Throwable {

        logger.debug("Invoking loadMindmap");
        final LoadMindmapResponse result = new LoadMindmapResponse();

        try {
            final MindMap mindmap = mindmapService.getMindmapById((int) request.getMapdId());
            if(mindmap==null)
            {
                throw new NoMapFoundException(request.getMapdId());
            }

            String xml = mindmap.getNativeXml();

            // Hack, we need to unify to only one XSD schema definitions per map ...
            xml = "<map xmlns=\"http://www.wisemapping.org/mindmap\"" + xml.substring(4,xml.length());

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            final StringReader stringReader = new StringReader(xml);
            final StreamSource streamSource = new StreamSource(stringReader);
            final JAXBElement<MapType> mapElement =  unmarshaller.unmarshal(streamSource,MapType.class);

            // Load map data ...
            result.creator = mindmap.getCreator();
            result.setMap(mapElement.getValue());
        } catch (Throwable e) {
            logger.fatal("Unexpexted Exception", e);
            throw e;
        }

        return result;
    }

    @PayloadRoot(localPart = "addMindmapRequest", namespace = "http://www.wisemapping.org/ws")
    public AddMindmapResponse createMindmap(final AddMindmapRequest request) throws Throwable {

        logger.debug("Invoking createMindmap");
        final AddMindmapResponse response = new AddMindmapResponse();
        try {

            final String creator = request.getCreator();
            final User user = userService.getUserBy(creator);
            if(user==null)
            {
              throw new IllegalArgumentException("Invalid addMindmapRequest.' " + creator+"' is not valid wisemapping user.");
            }


            final MindMap mindmap =  new MindMap();
            mindmap.setCreationTime(Calendar.getInstance());

            // Set title ...
            final String title = request.getTitle();
            if(title==null)
            {
                throw new IllegalArgumentException("Invalid addMindmapRequest. Title element can not be null.");
            }
            mindmap.setTitle(title);

            // Set description ...
            final String description = request.getDescription();
            if(description==null)
            {
                throw new IllegalArgumentException("Invalid addMindmapRequest. Description element can not be null.");
            }
            mindmap.setDescription(description);

            // Convert Map to XML
            final MapType mapType = request.getMap();
            if(mapType==null)
            {
                throw new IllegalArgumentException("Invalid addMindmapRequest. Map element can not be null.");
            }

            ObjectFactory factory = new ObjectFactory();
            final Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(factory.createMap(mapType),stringWriter);

            mindmap.setNativeXml(stringWriter.toString());
            mindmapService.addMindmap(mindmap,user);


            // Prepare result ...
            response.setMapId(mindmap.getId());

        } catch (Throwable e) {
            logger.fatal("Unexpexted Exception", e);
            throw e;

        }

        return response;
    }


    public MindmapService getMindmapService() {
        return mindmapService;
    }

    public void setMindmapService(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }
}
