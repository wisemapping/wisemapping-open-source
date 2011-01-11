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

package com.wisemapping.exporter.freemind;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.Exporter;
import com.wisemapping.model.MindMap;
import com.wisemapping.util.JAXBUtils;
import com.wisemapping.xml.freemind.*;
import com.wisemapping.xml.mindmap.RelationshipType;
import com.wisemapping.xml.mindmap.TopicType;
import com.wisemapping.xml.mindmap.Icon;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreemindExporter
    implements Exporter
{

    private com.wisemapping.xml.freemind.ObjectFactory freemindObjectFactory;
    private Map<String, Node> nodesMap = null;

    public void export(MindMap map, OutputStream outputStream) throws ExportException {
        try {
            export(map.getUnzippedXml().getBytes(),outputStream);
        } catch (IOException e) {
            throw new ExportException(e);
        }
    }

    public void export(byte[] xml, OutputStream outputStream) throws ExportException {

        freemindObjectFactory = new com.wisemapping.xml.freemind.ObjectFactory();
        nodesMap = new HashMap<String, Node>();
        final com.wisemapping.xml.mindmap.Map mindmapMap;

        try {
            final ByteArrayInputStream stream = new ByteArrayInputStream(xml);
            mindmapMap = (com.wisemapping.xml.mindmap.Map) JAXBUtils.getMapObject(stream,"com.wisemapping.xml.mindmap");

            final com.wisemapping.xml.freemind.Map freemindMap = freemindObjectFactory.createMap();

            final List<TopicType> topics = mindmapMap.getTopic();

            // Insolated Topic doesn´t exists in Freemind only take the center topic
            TopicType centerTopic = null;
            if (topics.size() >1)
            {
                for (TopicType topic : topics) {
                    if (topic.isCentral())
                    {
                        centerTopic = topic;
                        break;
                    }
                }
            }
            else
            {
                centerTopic = topics.get(0);
            }

            final Node main = freemindObjectFactory.createNode();
            freemindMap.setNode(main);
            if (centerTopic != null)
            {
                nodesMap.put(centerTopic.getId(), main);
                setTopicPropertiesToNode(main,centerTopic);
                addNodeFromTopic(centerTopic,main);
            }
            List<RelationshipType> relationships = mindmapMap.getRelationship();
            for(RelationshipType relationship : relationships){
                Node srcNode = nodesMap.get(relationship.getSrcTopicId());
                Arrowlink arrowlink = freemindObjectFactory.createArrowlink();
                Node dstNode = nodesMap.get(relationship.getDestTopicId());
                arrowlink.setDESTINATION(dstNode.getID());
                if(relationship.isEndArrow())
                    arrowlink.setENDARROW("Default");
                List<Object> cloudOrEdge = srcNode.getArrowlinkOrCloudOrEdge();
                cloudOrEdge.add(arrowlink);
            }

            JAXBUtils.saveMap(freemindMap,outputStream,"com.wisemapping.xml.freemind");
         } catch (JAXBException e) {
            throw new ExportException(e);
        }
    }

    private void addNodeFromTopic(TopicType mainTopic, Node destNode)
    {
        final List<TopicType> currentTopic = mainTopic.getTopic();

        for (TopicType topicType : currentTopic) {
            final Node newNode = freemindObjectFactory.createNode();
            nodesMap.put(topicType.getId(), newNode);
            setTopicPropertiesToNode(newNode,topicType);
            destNode.getArrowlinkOrCloudOrEdge().add(newNode);
            addNodeFromTopic(topicType,newNode);
        }
    }

    private void setTopicPropertiesToNode(com.wisemapping.xml.freemind.Node freemindNode, com.wisemapping.xml.mindmap.TopicType mindmapTopic)
    {
        freemindNode.setID("ID_"+mindmapTopic.getId());
        freemindNode.setTEXT(mindmapTopic.getText());
        freemindNode.setBACKGROUNDCOLOR(mindmapTopic.getBgColor());
        String style = "line"; // default style for freemind
        if ("rounded rectagle".equals(mindmapTopic.getShape()))
        {
            style = "bubble";
        }
        freemindNode.setSTYLE(style);
        addIconNode(freemindNode,mindmapTopic);
        addLinkNode(freemindNode,mindmapTopic);
        addFontNode(freemindNode,mindmapTopic);
        addEdgeNode(freemindNode,mindmapTopic);
        addNote(freemindNode,mindmapTopic);
        Boolean shrink = mindmapTopic.isShrink();
        if(shrink!=null && shrink)
            freemindNode.setFOLDED(String.valueOf(shrink));
    }

    private void addNote(com.wisemapping.xml.freemind.Node freemindNode,com.wisemapping.xml.mindmap.TopicType mindmapTopic)
    {
        if (mindmapTopic.getNote() != null)
        {
            final Hook note = new Hook();
            String textNote = mindmapTopic.getNote().getText();
            textNote = textNote.replaceAll("%0A","\n");
            note.setNAME("accessories/plugins/NodeNote.properties");
            note.setText(textNote);
            freemindNode.getArrowlinkOrCloudOrEdge().add(note);
        }
    }

    private void addLinkNode(com.wisemapping.xml.freemind.Node freemindNode,com.wisemapping.xml.mindmap.TopicType mindmapTopic)
    {
        if (mindmapTopic.getLink() != null)
        {
            final String url = mindmapTopic.getLink().getUrl();
            freemindNode.setLINK(url); 
        }
    }

    private void addIconNode(com.wisemapping.xml.freemind.Node freemindNode,com.wisemapping.xml.mindmap.TopicType mindmapTopic)
    {
        if (mindmapTopic.getIcon() != null)
        {
            final List<Icon> iconsList = mindmapTopic.getIcon();
            for (Icon icon : iconsList) {
                final String id = icon.getId();
                com.wisemapping.xml.freemind.Icon freemindIcon = new com.wisemapping.xml.freemind.Icon();
                final String freemindIconId = FreemindIconMapper.getFreemindIcon(id);
                freemindIcon.setBUILTIN(freemindIconId);
                freemindNode.getArrowlinkOrCloudOrEdge().add(freemindIcon);
            }
        }
    }

    private void addEdgeNode(com.wisemapping.xml.freemind.Node freemindNode,com.wisemapping.xml.mindmap.TopicType mindmapTopic)
    {
        if (mindmapTopic.getBrColor() != null)
        {
            final Edge edgeNode = freemindObjectFactory.createEdge();
            edgeNode.setCOLOR(mindmapTopic.getBrColor());            
            freemindNode.getArrowlinkOrCloudOrEdge().add(edgeNode);
        }
    }

    /*
     * MindmapFont format : fontName ; size ; color ; bold; italic;
     * eg: Verdana;10;#ffffff;bold;italic;
     *
     */
    private void addFontNode(com.wisemapping.xml.freemind.Node freemindNode,com.wisemapping.xml.mindmap.TopicType mindmapTopic)
    {
        final String fontStyle = mindmapTopic.getFontStyle();
        if (fontStyle!= null && fontStyle.length()!=0)
        {
            final Font font = freemindObjectFactory.createFont();
            final String[] part = fontStyle.split(";",6);
            int countParts = part.length;
            int idx=0;
            // Font name
            if (idx < countParts && part[idx].length()!=0)
            {
                font.setNAME(part[idx]);
            }
            idx++;
            // Font size
            String size = "10"; // default value
            if (idx < countParts && part[idx].length()!=0)
            {
                size = part[idx];
            }

            font.setSIZE(new BigInteger(size));
            idx++;

            // Font Color
            if (idx < countParts && part[idx].length()!=0)
            {
                freemindNode.setCOLOR(part[idx]);
            }
             idx++;
             if (idx < countParts && part[idx].length()!=0)
            {
                font.setBOLD(Boolean.TRUE.toString());
            }
            idx++;
            if (idx < countParts && part[idx].length()!=0)
            {
                font.setITALIC(Boolean.TRUE.toString());
            }
            freemindNode.getArrowlinkOrCloudOrEdge().add(font);
        }
    }
}
