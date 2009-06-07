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

package com.wisemapping.importer.freemind;

import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.ShapeStyle;
import com.wisemapping.model.MindMapNative;
import com.wisemapping.util.JAXBUtils;
import com.wisemapping.xml.freemind.*;
import com.wisemapping.xml.mindmap.TopicType;
import com.wisemapping.xml.mindmap.Link;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.math.BigInteger;

public class FreemindImporter
    implements Importer
{

    private com.wisemapping.xml.mindmap.ObjectFactory mindmapObjectFactory;
    private static final String POSITION_LEFT = "left";
    private static final String BOLD = "bold";
    private static final String ITALIC = "italic";
    private static final String EMPTY_NOTE = "";

    public MindMap importMap(String mapName,String description,InputStream input) throws ImporterException {

        final MindMap map;
        mindmapObjectFactory = new com.wisemapping.xml.mindmap.ObjectFactory();
        try {
            final Map freemindMap = (Map) JAXBUtils.getMapObject(input,"com.wisemapping.xml.freemind");

            final com.wisemapping.xml.mindmap.Map mindmapMap = mindmapObjectFactory.createMap();

            final Node centralNode = freemindMap.getNode();
            final TopicType centralTopic = mindmapObjectFactory.createTopicType();
            centralTopic.setCentral(true);

            setNodePropertiesToTopic(centralTopic,centralNode);
            centralTopic.setShape(ShapeStyle.ROUNDED_RETAGLE.getStyle());
            mindmapMap.getTopic().add(centralTopic);
            mindmapMap.setName(mapName);

            addTopicFromNode(centralNode,centralTopic);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JAXBUtils.saveMap(mindmapMap,out,"com.wisemapping.xml.mindmap");

            map = new MindMap();
            map.setNativeXml(out.toString());
            map.setTitle(mapName);
            map.setDescription(description);
            map.setNativeBrowser(new MindMapNative());

        } catch (JAXBException e) {
            throw new ImporterException(e);
        } catch (IOException e) {
            throw new ImporterException(e);
        }

        return map;
    }

    private void addTopicFromNode(Node mainNode, TopicType topic)
    {
        final List<Object> freemindNodes = mainNode.getArrowlinkOrCloudOrEdge();
        TopicType currentTopic = topic;
        int order = 0;
        for (Object freemindNode : freemindNodes) {

            if (freemindNode instanceof Node)
            {
                final Node node = (Node) freemindNode;
                TopicType newTopic = mindmapObjectFactory.createTopicType();
                newTopic.setOrder(order++);
                String url = node.getLINK();
                if (url != null)
                {
                    final Link link = new Link();
                    link.setUrl(url);
                    newTopic.setLink(link);
                }
                setNodePropertiesToTopic(newTopic, node);
                addTopicFromNode(node,newTopic);
                if (!newTopic.equals(topic))
                {
                    topic.getTopic().add(newTopic);
                }
                currentTopic = newTopic;
            }
            else if (freemindNode instanceof Font)
            {
                final Font font = (Font)freemindNode;
                final String fontStyle = generateFontStyle(mainNode, font);
                currentTopic.setFontStyle(fontStyle);
            }
            else if (freemindNode instanceof Edge)
            {
                final Edge edge = (Edge)freemindNode;
                currentTopic.setBrColor(edge.getCOLOR());
            }
            else if (freemindNode instanceof Icon)
            {
                final Icon freemindIcon = (Icon)freemindNode;
                final com.wisemapping.xml.mindmap.Icon mindmapIcon = new com.wisemapping.xml.mindmap.Icon();
                final String mindmapIconId = FreemindIconMapper.getMindmapIcon(freemindIcon.getBUILTIN());
                mindmapIcon.setId(mindmapIconId);
                currentTopic.getIcon().add(mindmapIcon);
            }
            else if (freemindNode instanceof Hook)
            {
                final Hook hook = (Hook)freemindNode;
                final com.wisemapping.xml.mindmap.Note mindmapNote = new com.wisemapping.xml.mindmap.Note();
                String textNote = hook.getText();
                if (textNote == null) // It is not a note is a BlinkingNodeHook or AutomaticLayout Hook
                {
                    textNote = textNote != null ? textNote.replaceAll("\n","%0A") : EMPTY_NOTE;
                    mindmapNote.setText(textNote);
                    currentTopic.setNote(mindmapNote);
                }
            }
        }
    }

    private void setNodePropertiesToTopic( com.wisemapping.xml.mindmap.TopicType mindmapTopic,com.wisemapping.xml.freemind.Node freemindNode)
    {
        mindmapTopic.setText(freemindNode.getTEXT());
        mindmapTopic.setBgColor(freemindNode.getBACKGROUNDCOLOR());

        final String shape = getShapeFormFromNode(freemindNode);
        mindmapTopic.setShape(shape);
        int pos = 1;
        if (POSITION_LEFT.equals(freemindNode.getPOSITION()))
        {
            pos = -1;
        }
        Integer orderPosition = mindmapTopic.getOrder() != null ? mindmapTopic.getOrder() : 0;
        int position = pos * 200 + (orderPosition +1)*10;

        mindmapTopic.setPosition( position+","+200 * orderPosition);
        generateFontStyle(freemindNode,null);
    }

    private String generateFontStyle(Node node,Font font)
    {
        /*
        * MindmapFont format : fontName ; size ; color ; bold; italic;
        * eg: Verdana;10;#ffffff;bold;italic;
        *
        */
        StringBuilder fontStyle = new StringBuilder();
        if (font != null)
        {
            fontStyle.append(fixFontName(font));
            fontStyle.append(";");
            BigInteger bigInteger = font.getSIZE().intValue() < 8 ? BigInteger.valueOf(8) : font.getSIZE();
            fontStyle.append(bigInteger);
            fontStyle.append(";");
            fontStyle.append(node.getCOLOR());
            fontStyle.append(";");

            boolean hasBold = Boolean.parseBoolean(font.getBOLD());

            fontStyle.append(hasBold ? BOLD : null);
            fontStyle.append(";");

            boolean hasItalic = Boolean.parseBoolean(font.getITALIC());
            fontStyle.append(hasItalic ? ITALIC : null);
            fontStyle.append(";");
        }
        else
        {
            fontStyle.append(";");
            fontStyle.append(";");
            fontStyle.append(node.getCOLOR());
            fontStyle.append(";");
            fontStyle.append(";");
            fontStyle.append(";");
        }

        return fontStyle.toString();
    }

    private String fixFontName(Font font)
    {
        String fontName = com.wisemapping.model.Font.ARIAL.getFontName(); // Default Font
        if (com.wisemapping.model.Font.isValidFont(font.getNAME()))
        {
            fontName = font.getNAME();
        }
        return fontName;
    }
    
    private String getShapeFormFromNode(Node node)
    {
        String shape = node.getSTYLE();
        // In freemind a node without style is a line
        if ("bubble".equals(shape))
        {
            shape= ShapeStyle.ROUNDED_RETAGLE.getStyle();
        }
        else
        {
            shape=ShapeStyle.LINE.getStyle();
        }
        return shape;
    }
}
