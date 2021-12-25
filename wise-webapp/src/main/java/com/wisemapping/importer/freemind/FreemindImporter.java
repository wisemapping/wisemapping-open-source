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

package com.wisemapping.importer.freemind;

import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.VersionNumber;
import com.wisemapping.jaxb.freemind.*;
import com.wisemapping.jaxb.wisemap.Link;
import com.wisemapping.jaxb.wisemap.RelationshipType;
import com.wisemapping.jaxb.wisemap.TopicType;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.ShapeStyle;
import com.wisemapping.util.JAXBUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FreemindImporter
        implements Importer {
    final private Logger logger = Logger.getLogger(FreemindImporter.class);

    private com.wisemapping.jaxb.wisemap.ObjectFactory mindmapObjectFactory;
    private java.util.Map<String, TopicType> nodesMap = null;
    private List<RelationshipType> relationships = null;

    private int currentId;

    public static void main(String[] argv) {


        // Now, calculate the order it belongs to ...
        // 3 = -100  0
        // 1 = -50   1
        // 0 =  0    2
        // 2 = 50    3
        // 4 = 100   4

        int total = 2;
        int center = (total - 1) / 2;


        for (int i = 0; i < total; i++) {

            int result = i - center + ((total % 2 == 0) ? 0 : 1);
            if (result > 0) {
                result = (result - 1) * 2;
            } else {
                result = (result * -2) + 1;
            }

            System.out.println(i + "->" + result);
        }

    }

    public Mindmap importMap(@NotNull String mapName, @NotNull String description, @NotNull InputStream input) throws ImporterException {

        final Mindmap result = new Mindmap();
        nodesMap = new HashMap<String, TopicType>();
        relationships = new ArrayList<RelationshipType>();
        mindmapObjectFactory = new com.wisemapping.jaxb.wisemap.ObjectFactory();

        try {
            String wiseXml;
            final Map freemindMap = (Map) JAXBUtils.getMapObject(input, "com.wisemapping.jaxb.freemind");

            final String version = freemindMap.getVersion();
            if (version == null || version.startsWith("freeplane")) {
                throw new ImporterException("You seems to be be trying to import a Freeplane map. FreePlane is not supported format.");
            } else {
                final VersionNumber mapVersion = new VersionNumber(version);
                if (mapVersion.isGreaterThan(FreemindConstant.SUPPORTED_FREEMIND_VERSION)) {
                    throw new ImporterException("FreeMind version " + mapVersion.getVersion() + " is not supported.");
                }
            }


            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final com.wisemapping.jaxb.wisemap.Map mindmapMap = mindmapObjectFactory.createMap();
            mindmapMap.setVersion(FreemindConstant.CODE_VERSION);
            currentId = 0;

            final Node freeNode = freemindMap.getNode();
            final TopicType wiseTopic = mindmapObjectFactory.createTopicType();
            wiseTopic.setId(String.valueOf(currentId++));
            wiseTopic.setCentral(true);
            wiseTopic.setPosition("0,0");

            convertNodeProperties(freeNode, wiseTopic);

            wiseTopic.setShape(ShapeStyle.ROUNDED_RECTANGLE.getStyle());
            mindmapMap.getTopic().add(wiseTopic);
            mindmapMap.setName(mapName);

            nodesMap.put(freeNode.getID(), wiseTopic);

            convertChildNodes(freeNode, wiseTopic, 1);
            addRelationships(mindmapMap);

            JAXBUtils.saveMap(mindmapMap, baos);
            wiseXml = baos.toString(FreemindConstant.UTF_8_CHARSET);
            result.setXmlStr(wiseXml);
            result.setTitle(mapName);
            result.setDescription(description);

        } catch (JAXBException |  TransformerException e) {
            logger.debug(e);
            throw new ImporterException(e);
        }
        return result;
    }

    private void addRelationships(@NotNull com.wisemapping.jaxb.wisemap.Map mindmapMap) {
        List<RelationshipType> mapRelationships = mindmapMap.getRelationship();
        for (RelationshipType relationship : relationships) {
            relationship.setId(String.valueOf(currentId++));

            fixRelationshipControlPoints(relationship);

            //Fix dest ID
            String destId = relationship.getDestTopicId();
            TopicType destTopic = nodesMap.get(destId);
            relationship.setDestTopicId(destTopic.getId());
            //Fix src ID
            String srcId = relationship.getSrcTopicId();
            TopicType srcTopic = nodesMap.get(srcId);
            relationship.setSrcTopicId(srcTopic.getId());

            mapRelationships.add(relationship);
        }
    }

    private void fixRelationshipControlPoints(@NotNull RelationshipType rel) {
        //Both relationship node's ids should be freemind ones at this point.
        TopicType srcTopic = nodesMap.get(rel.getSrcTopicId());
        TopicType destTopicType = nodesMap.get(rel.getDestTopicId());

        //Fix x coord
        final String srcCtrlPoint = rel.getSrcCtrlPoint();
        if (srcCtrlPoint != null) {
            final Coord srcCtrlCoord = Coord.parse(srcCtrlPoint);

            if (Coord.parse(srcTopic.getPosition()).isOnLeftSide()) {
                int x = srcCtrlCoord.x * -1;
                rel.setSrcCtrlPoint(x + "," + srcCtrlCoord.y);

                //Fix coord
                if (srcTopic.getOrder() != null && srcTopic.getOrder() % 2 != 0) { //Odd order.
                    int y = srcCtrlCoord.y * -1;
                    rel.setSrcCtrlPoint(srcCtrlCoord.x + "," + y);
                }
            }
        }
        final String destCtrlPoint = rel.getDestCtrlPoint();
        if (destCtrlPoint != null) {
            final Coord destCtrlCoord = Coord.parse(destCtrlPoint);


            if (Coord.parse(destTopicType.getPosition()).isOnLeftSide()) {
                int x = destCtrlCoord.x * -1;
                rel.setDestCtrlPoint(x + "," + destCtrlCoord.y);
            }


            if (destTopicType.getOrder() != null && destTopicType.getOrder() % 2 != 0) { //Odd order.
                int y = destCtrlCoord.y * -1;
                rel.setDestCtrlPoint(destCtrlCoord.x + "," + y);
            }
        }
    }

    private void convertChildNodes(@NotNull Node freeParent, @NotNull TopicType wiseParent, final int depth) throws TransformerException {
        final List<Object> freeChilden = freeParent.getArrowlinkOrCloudOrEdge();
        TopicType currentWiseTopic = wiseParent;

        int order = 0;
        int firstLevelRightOrder = 0;
        int firstLevelLeftOrder = 1;
        for (Object element : freeChilden) {

            if (element instanceof Node) {
                final Node freeChild = (Node) element;
                final TopicType wiseChild = mindmapObjectFactory.createTopicType();

                // Set an incremental id ...
                wiseChild.setId(String.valueOf(currentId++));

                // Lets use freemind id temporarily. This will be fixed when adding relationship to the map.
                nodesMap.put(freeChild.getID(), wiseChild);

                // Set node order ...
                int norder;
                if (depth != 1) {
                    norder = order++;
                } else {
                    if (freeChild.getPOSITION() != null && freeChild.getPOSITION().equals(FreemindConstant.POSITION_LEFT)) {
                        norder = firstLevelLeftOrder;
                        firstLevelLeftOrder = firstLevelLeftOrder + 2;
                    } else {
                        norder = firstLevelRightOrder;
                        firstLevelRightOrder = firstLevelRightOrder + 2;
                    }
                }
                wiseChild.setOrder(norder);

                // Convert node position
                int childrenCountSameSide = getChildrenCountSameSide(freeChilden, freeChild);
                final String position = convertPosition(wiseParent, freeChild, depth, norder, childrenCountSameSide);
                wiseChild.setPosition(position);

                // Convert the rest of the node properties ...
                convertNodeProperties(freeChild, wiseChild);

                convertChildNodes(freeChild, wiseChild, depth + 1);

                if (!wiseChild.equals(wiseParent)) {
                    wiseParent.getTopic().add(wiseChild);
                }
                currentWiseTopic = wiseChild;

            } else if (element instanceof Font) {
                final Font font = (Font) element;
                final String fontStyle = generateFontStyle(freeParent, font);
                if (fontStyle != null) {
                    currentWiseTopic.setFontStyle(fontStyle);
                }
            } else if (element instanceof Edge) {
                final Edge edge = (Edge) element;
                currentWiseTopic.setBrColor(edge.getCOLOR());
            } else if (element instanceof Icon) {
                final Icon freemindIcon = (Icon) element;

                String iconId = freemindIcon.getBUILTIN();
                final String wiseIconId = FreemindIconConverter.toWiseId(iconId);
                if (wiseIconId != null) {
                    final com.wisemapping.jaxb.wisemap.Icon mindmapIcon = new com.wisemapping.jaxb.wisemap.Icon();
                    mindmapIcon.setId(wiseIconId);
                    currentWiseTopic.getIcon().add(mindmapIcon);
                }

            } else if (element instanceof Hook) {
                final Hook hook = (Hook) element;
                final com.wisemapping.jaxb.wisemap.Note mindmapNote = new com.wisemapping.jaxb.wisemap.Note();
                String textNote = hook.getText();
                if (textNote == null) // It is not a note is a BlinkingNodeHook or AutomaticLayout Hook
                {
                    textNote = FreemindConstant.EMPTY_NOTE;
                    mindmapNote.setValue(textNote);
                    currentWiseTopic.setNote(mindmapNote);
                }
            } else if (element instanceof Richcontent) {
                final Richcontent content = (Richcontent) element;
                final String type = content.getTYPE();

                if (type.equals(FreemindConstant.NODE_TYPE)) {
                    String text = html2text(content);
                    currentWiseTopic.setText(text);
                } else {
                    String text = html2text(content);
                    final com.wisemapping.jaxb.wisemap.Note mindmapNote = new com.wisemapping.jaxb.wisemap.Note();
                    text = text != null ? text : FreemindConstant.EMPTY_NOTE;
                    mindmapNote.setValue(text);
                    currentWiseTopic.setNote(mindmapNote);

                }
            } else if (element instanceof Arrowlink) {
                final Arrowlink arrow = (Arrowlink) element;
                RelationshipType relt = mindmapObjectFactory.createRelationshipType();
                String destId = arrow.getDESTINATION();
                // FIXME: invert srcTopic and dstTopic to correct a bug in the wise mind map representation
                relt.setSrcTopicId(destId);
                relt.setDestTopicId(freeParent.getID());
                final String endinclination = arrow.getENDINCLINATION();
                if (endinclination != null) {
                    String[] inclination = endinclination.split(";");
                    relt.setDestCtrlPoint(inclination[0] + "," + inclination[1]);
                }
                final String startinclination = arrow.getSTARTINCLINATION();
                if (startinclination != null) {
                    String[] inclination = startinclination.split(";");
                    relt.setSrcCtrlPoint(inclination[0] + "," + inclination[1]);
                }

                final String endarrow = arrow.getENDARROW();
                if (endarrow != null) {
                    relt.setEndArrow(!endarrow.equalsIgnoreCase("none"));
                }

                final String startarrow = arrow.getSTARTARROW();
                if (startarrow != null) {
                    relt.setStartArrow(!startarrow.equalsIgnoreCase("none"));
                }
                relt.setLineType("3");
                relationships.add(relt);
            }
        }
    }

    private int getChildrenCountSameSide(@NotNull List<Object> freeChildren, Node freeChild) {
        int result = 0;
        String childSide = freeChild.getPOSITION();
        if (childSide == null) {
            childSide = FreemindConstant.POSITION_RIGHT;
        }

        // Count all the nodes of the same side ...
        for (Object child : freeChildren) {
            if (child instanceof Node) {
                Node node = (Node) child;

                String side = node.getPOSITION();
                if (side == null) {
                    side = FreemindConstant.POSITION_RIGHT;
                }
                if (childSide.equals(side)) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Position is (x,y).
     * x values greater than 0 are right axis
     * x values lower than 0 are left axis
     */
    private
    @NotNull
    String convertPosition(@NotNull TopicType wiseParent, @NotNull Node freeChild, final int depth, int order, int childrenCount) {

        // Which side must be the node be positioned ?

        // Calculate X ...

        // Problem on setting X position:
        // Text Size is not taken into account ...
        int x = FreemindConstant.CENTRAL_TO_TOPIC_DISTANCE + ((depth - 1) * FreemindConstant.TOPIC_TO_TOPIC_DISTANCE);
        if (depth == 1) {

            final String side = freeChild.getPOSITION();
            x = x * (side != null && FreemindConstant.POSITION_LEFT.equals(side) ? -1 : 1);
        } else {
            final Coord coord = Coord.parse(wiseParent.getPosition());
            x = x * (coord.isOnLeftSide() ? -1 : 1);
        }


        // Calculate y ...
        int y;
        if (depth == 1) {

            // pair order numbers represent nodes at the right
            // odd order numbers represent nodes at the left
            if (order % 2 == 0) {
                int multiplier = ((order + 1) - childrenCount) * 2;
                y = multiplier * FreemindConstant.ROOT_LEVEL_TOPIC_HEIGHT;
            } else {
                int multiplier = (order - childrenCount) * 2;
                y = multiplier * FreemindConstant.ROOT_LEVEL_TOPIC_HEIGHT;
            }
        } else {

            // Problem: What happen if the node is more tall than what is defined here.
            Coord coord = Coord.parse(wiseParent.getPosition());
            int parentY = coord.y;
            y = parentY - ((childrenCount / 2) * FreemindConstant.SECOND_LEVEL_TOPIC_HEIGHT - (order * FreemindConstant.SECOND_LEVEL_TOPIC_HEIGHT));


        }
        return x + "," + y;


    }

    /**
     * Position is (x,y).
     * x values greater than 0 are right axis
     * x values lower than 0 are left axis
     */
//    private
//    @NotNull
//    String convertPosition(@NotNull TopicType wiseParent, @NotNull Node freeChild, final int depth, int order) {
//
//        // Which side must be the node be positioned ?
//        String result = freeChild.getWcoords();
//        if (result == null) {
//            BigInteger vgap = freeChild.getVSHIFT();
//            BigInteger hgap = freeChild.getHGAP();
//
//            if (hgap == null) {
//                hgap = BigInteger.valueOf(0L);
//            }
//
//            if (vgap == null) {
//                vgap = BigInteger.valueOf(HALF_ROOT_TOPICS_SEPARATION * order);
//            }
//
//
//            final String[] position = wiseParent.getPosition().split(",");
//            BigInteger fix = BigInteger.valueOf(1L);
//            if ((freeChild.getPOSITION() != null && POSITION_LEFT.equals(freeChild.getPOSITION().toLowerCase()))
//                    || freeChild.getPOSITION() == null && isOnLeftSide(wiseParent)) {
//                fix = BigInteger.valueOf(-1L);
//
//            }
//
//            BigInteger firstLevelDistance = BigInteger.valueOf(0L);
//            BigInteger defaultXDistance = BigInteger.valueOf(200L);
//            if (depth == 1) {
//                firstLevelDistance = BigInteger.valueOf(200L);
//                defaultXDistance = BigInteger.valueOf(0L);
//            }
//
//            BigInteger x = BigInteger.valueOf(Integer.valueOf(position[0])).add(hgap.multiply(fix).add(firstLevelDistance.multiply(fix)).add(defaultXDistance.multiply(fix)));
//            BigInteger y = BigInteger.valueOf(Integer.valueOf(position[1])).add(vgap);
//            result = x.toString() + "," + y.toString();
//        }
//        return result;
//    }
    @NotNull
    private String html2text(@NotNull Richcontent content) throws TransformerException {
        final Element html = (Element) content.getHtml();

        // Convert any to HTML piece ...
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(html), new StreamResult(buffer));

        // Keep return lines in place ...
        final Document document = Jsoup.parse(buffer.toString());
        document.select("br").append("\\n");
        document.select("p").prepend("\\n");
        document.select("div").prepend("\\n");
        return document.text().replaceAll("\\\\n", "\n").trim();

    }


    private void convertNodeProperties(@NotNull com.wisemapping.jaxb.freemind.Node freeNode, @NotNull com.wisemapping.jaxb.wisemap.TopicType wiseTopic) {
        final String text = freeNode.getTEXT();
        wiseTopic.setText(text);

        // Background color ...
        final String bgcolor = freeNode.getBACKGROUNDCOLOR();
        wiseTopic.setBgColor(bgcolor);


        final String shape = getShapeFormFromNode(freeNode);
        wiseTopic.setShape(shape);

        // Check for styles ...
        final String fontStyle = generateFontStyle(freeNode, null);
        if (fontStyle != null) {
            wiseTopic.setFontStyle(fontStyle);
        }

        // Is there any link ?
        final String url = freeNode.getLINK();
        if (url != null) {
            final Link link = new Link();
            link.setUrl(url);
            wiseTopic.setLink(link);
        }

        final Boolean folded = Boolean.valueOf(freeNode.getFOLDED());
        wiseTopic.setShrink(folded);
    }


    @Nullable
    private String generateFontStyle(@NotNull Node node, @Nullable Font font) {
        /*
        * MindmapFont format : fontName ; size ; color ; bold; italic;
        * eg: Verdana;10;#ffffff;bold;italic;
        *
        */

        // Font name ...
        final StringBuilder fontStyle = new StringBuilder();
        if (font != null) {
            fontStyle.append(fixFontName(font));
        }
        fontStyle.append(";");

        // Freemind size goes from 10 to 28
        // WiseMapping:
        //  6 Small
        //  8 Normal
        // 10 Large
        // 15 Huge
        if (font != null) {
            final int fontSize = ((font.getSIZE() == null || font.getSIZE().intValue() < 8) ? BigInteger.valueOf(FreemindConstant.FONT_SIZE_NORMAL) : font.getSIZE()).intValue();
            int wiseFontSize = FreemindConstant.FONT_SIZE_SMALL;
            if (fontSize >= 24) {
                wiseFontSize = FreemindConstant.FONT_SIZE_HUGE;
            } else if (fontSize >= 16) {
                wiseFontSize = FreemindConstant.FONT_SIZE_LARGE;
            } else if (fontSize >= 12) {
                wiseFontSize = FreemindConstant.FONT_SIZE_NORMAL;
            }
            fontStyle.append(wiseFontSize);

        }
        fontStyle.append(";");

        // Color ...
        final String color = node.getCOLOR();
        if (color != null && !color.equals("")) {
            fontStyle.append(color);
        }
        fontStyle.append(";");

        // Bold ...
        if (font != null) {
            boolean hasBold = Boolean.parseBoolean(font.getBOLD());
            fontStyle.append(hasBold ? FreemindConstant.BOLD : "");
        }
        fontStyle.append(";");

        // Italic ...
        if (font != null) {
            boolean hasItalic = Boolean.parseBoolean(font.getITALIC());
            fontStyle.append(hasItalic ? FreemindConstant.ITALIC : "");
        }
        fontStyle.append(";");

        final String result = fontStyle.toString();
        return result.equals(FreemindConstant.EMPTY_FONT_STYLE) ? null : result;
    }

    private
    @NotNull
    String fixFontName(@NotNull Font font) {
        String result = com.wisemapping.model.Font.ARIAL.getFontName(); // Default Font
        if (com.wisemapping.model.Font.isValidFont(font.getNAME())) {
            result = font.getNAME();
        }
        return result;
    }

    private
    @NotNull
    String getShapeFormFromNode(@NotNull Node node) {
        String result = node.getSTYLE();
        // In freemind a node without style is a line
        if ("bubble".equals(result)) {
            result = ShapeStyle.ROUNDED_RECTANGLE.getStyle();
        } else {
            if (node.getBACKGROUNDCOLOR() != null) {
                // This the node has background color defined. It's better to change the default shape.
                result = ShapeStyle.RECTANGLE.getStyle();
            } else {
                result = ShapeStyle.LINE.getStyle();
            }
        }
        return result;
    }

    static private class Coord {
        private final int y;
        private final int x;

        private Coord(@NotNull String pos) {
            final String[] split = pos.split(",");
            x = Integer.parseInt(split[0]);
            y = Integer.parseInt(split[1]);
        }

        public static Coord parse(@NotNull String position) {
            return new Coord(position);
        }

        private boolean isOnLeftSide() {
            return x < 0;
        }
    }
}
