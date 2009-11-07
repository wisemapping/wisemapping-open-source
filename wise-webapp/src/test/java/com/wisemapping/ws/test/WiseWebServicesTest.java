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

package com.wisemapping.ws.test;

import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.net.MalformedURLException;
import java.io.StringWriter;
import java.util.List;

import com.wisemapping.ws.*;


@Test(groups = {"wsintegration"})
public class WiseWebServicesTest  {


    @Test(dependsOnMethods = "addMapTest")
    public void loadMapTest() throws MalformedURLException, JAXBException {

        final WiseServicesPortTypeService portTypeService = new WiseServicesPortTypeService();
        final WiseServicesPortType servicesPortType = portTypeService.getWiseServicesPortTypeSoap11();

        final LoadMindmapRequest request = new LoadMindmapRequest();
        request.setMapdId(1);
        LoadMindmapResponse response = servicesPortType.loadMindmap(request);

        JAXBContext jc = JAXBContext.newInstance("com.wisemapping.ws.test");
        Marshaller marshaller = jc.createMarshaller();

        final StringWriter xmlContext = new StringWriter();
        marshaller.marshal(response,xmlContext);
        System.out.println("Response:"+xmlContext);

    }

    public void addMapTest() throws MalformedURLException, JAXBException {

        final WiseServicesPortTypeService portTypeService = new WiseServicesPortTypeService();
        final WiseServicesPortType servicesPortType = portTypeService.getWiseServicesPortTypeSoap11();

        final AddMindmapRequest request = new AddMindmapRequest();

        request.setCreator("test@wisemapping.org");

        request.setTitle("MyFirstMap");
        request.setDescription("My First Map Description");

        // Set Map ...
        MapType sampleMap = createMockMap();
        request.setMap(sampleMap);


        AddMindmapResponse response = servicesPortType.addMindmap(request);

        JAXBContext jc = JAXBContext.newInstance("com.wisemapping.ws.test");
        Marshaller marshaller = jc.createMarshaller();

        final StringWriter xmlContext = new StringWriter();
        marshaller.marshal(response,xmlContext);
        System.out.println("Response:"+xmlContext);

    }

    private MapType createMockMap() {
        ObjectFactory factory = new ObjectFactory();
        MapType mapType = factory.createMapType();
        mapType.setName("map name");

        TopicType topicType = factory.createTopicType();
        topicType.setCentral(true);
        topicType.setText("Central topic value");

        List<TopicType> topics = mapType.getTopic();
        topics.add(topicType);

        return mapType;
    }

}
