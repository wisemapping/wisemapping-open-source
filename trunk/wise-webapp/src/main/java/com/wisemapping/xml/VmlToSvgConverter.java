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

package com.wisemapping.xml;

import com.wisemapping.xml.vmlmap.*;
import com.wisemapping.xml.vmlmap.Polyline;
import com.wisemapping.xml.vmlmap.Line;
import com.wisemapping.xml.vmlmap.Rect;
import com.wisemapping.xml.svgmap.*;
import com.wisemapping.xml.svgmap.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.List;

public class VmlToSvgConverter {
    private ObjectFactory svgObjectFactory;
    private static final int CORRECTION_HARDCODE = 5;
    private Object svgRootElem;

    public VmlToSvgConverter() {
        this.svgObjectFactory = new ObjectFactory();

    }

    public void convert(final Reader vmlDocument) throws JAXBException, IOException {

        final JAXBContext vmlContext = JAXBContext.newInstance("com.wisemapping.xml.vmlmap");
        final Unmarshaller umarshaller = vmlContext.createUnmarshaller();
        final Group rootElem = (Group) umarshaller.unmarshal(vmlDocument);
        this.svgRootElem = convert(rootElem);
    }

    private Svg convert(Group g) {
        final Svg svgElement = svgObjectFactory.createSvg();
        svgElement.setPreserveAspectRatio("none");

        /*
           <v:group style="WIDTH: 1270px; POSITION: absolute; HEIGHT: 705px" coordsize="889,493" coordorigin="-445,-247">
            <svgElement preserveAspectRatio="none" viewBox="-445.9 -221.9 891.8 443.8" height="634" width="1274" id="workspace" focusable="true">
         */

        final String coordorigin = g.getCoordorigin();
        String coordSize = g.getCoordsize();
        svgElement.setViewBox(coordorigin + ", " + coordSize);

        final Style style = Style.parse(g.getStyle());
        float width = style.getWidth();
        svgElement.setWidth(String.valueOf(width));

        float height = style.getHeight();
        svgElement.setHeight(String.valueOf(height));

        // Convert connection lines ...
        final List<Polyline> polylines = g.getPolyline();
        final List<com.wisemapping.xml.svgmap.Polyline> svgPolylines = svgElement.getPolyline();
        for (Polyline vmlPolyline : polylines) {
            final com.wisemapping.xml.svgmap.Polyline svgPolyline = convert(vmlPolyline);
            svgPolylines.add(svgPolyline);
        }

        final List<Line> vmlLines = g.getLine();
        final List<com.wisemapping.xml.svgmap.Line> svgLines = svgElement.getLine();
        for (Line vmlLine : vmlLines) {
            final com.wisemapping.xml.svgmap.Line svgPolyline = convert(vmlLine);
            svgLines.add(svgPolyline);
        }

        // Convert Topics ...
        final List<Group> vmlTopics = g.getGroup();
        final List<G> svgTopics = svgElement.getG();
        for (Group topic : vmlTopics) {
            G svgTopic = convertTopicGroup(topic);
            svgTopics.add(svgTopic);
        }

        // Convert connectors ...
        g.getOval();

        return svgElement;
    }

    private G convertTopicGroup(final Group vmlTopic) {
        /**
         *     <v:group style="LEFT: 222px; WIDTH: 100px; CURSOR: move; POSITION: absolute; TOP: -53px; HEIGHT: 100px" coordsize="100,100">
         *    <g transform="translate(225, -52) scale(1, 1)" height="100" width="100" focusable="true" preserveAspectRatio="none">
         */
        final G svgTopic = new G();

        final String styleStr = vmlTopic.getStyle();
        final Style style = Style.parse(styleStr);

        String transform = "translate(" + style.getLeft() + ", " + style.getTop() + ") scale(1, 1)";
        svgTopic.setTransform(transform);

        float width = style.getWidth();
        svgTopic.setWidth(String.valueOf(width));

        float height = style.getHeight();
        svgTopic.setHeight(String.valueOf(height));

        svgTopic.setPreserveAspectRatio("none");

        // Convert InnerShape ...
        final List<Roundrect> roundrects = vmlTopic.getRoundrect();
        float rectWidth = 0;
        float rectHeight = 0;
        for (Roundrect vmlRect : roundrects) {

            // Skip outerShape figure...
            final Fill vmlFill = vmlRect.getFill();
            if (vmlFill == null || !"0".equals(vmlFill.getOpacity())) {
                final com.wisemapping.xml.svgmap.Rect svgRect = convert(vmlRect);
                svgTopic.setRect(svgRect);
                final Style rectStyle = Style.parse(vmlRect.getStyle());

                rectWidth = rectStyle.getWidth();
                rectHeight = rectStyle.getHeight();
            }
        }

        final List<Rect> vmlRects = vmlTopic.getRect();
        for (com.wisemapping.xml.vmlmap.Rect vmlRect : vmlRects) {

            // Skip outerShape figure...
            final Fill vmlFill = vmlRect.getFill();
            if (vmlFill == null || !"0".equals(vmlFill.getOpacity())) {
                final com.wisemapping.xml.svgmap.Rect svgRect = convert(vmlRect);
                svgTopic.setRect(svgRect);
                final Style rectStyle = Style.parse(vmlRect.getStyle());

                rectWidth = rectStyle.getWidth();
                rectHeight = rectStyle.getHeight();
            }
        }

        final List<Line> vmlLines = vmlTopic.getLine();
        for (final Line vmlLine : vmlLines) {

            final String lineStyleStr = vmlLine.getStyle();
            final Style lineStyle = Style.parse(lineStyleStr);
            if (lineStyle.isVisible()) {
                com.wisemapping.xml.svgmap.Line line = convert(vmlLine);
                svgTopic.setLine(line);
            } else {
                // Shape is line...
                final String from = vmlLine.getFrom();
                String[] formPoints = from.split(",");

                final String to = vmlLine.getTo();
                String[] toPoints = to.split(",");

                rectWidth = Float.parseFloat(formPoints[0]) - Float.parseFloat(toPoints[0]);
                rectWidth = Math.abs(rectWidth);

                rectHeight = Float.parseFloat(formPoints[1]);
            }
        }

        // Convert connection ovals..
        final List<Oval> vmlOvals = vmlTopic.getOval();
        for (Oval vmlOval : vmlOvals) {

            // Skip outerShape figure...
            final Ellipse svgElipse = convert(vmlOval);
            if (svgElipse != null) {
                svgTopic.setEllipse(svgElipse);
            }
        }

        // Convert Text ...
        final List<Shape> vmlTextShape = vmlTopic.getShape();
        final Text svgText = convertTextShape(vmlTextShape.get(0), rectWidth, rectHeight);
        svgTopic.setText(svgText);

        return svgTopic;
    }

    private com.wisemapping.xml.svgmap.Rect convert(Rect vmlRect) {
        final com.wisemapping.xml.svgmap.Rect svgRect = new com.wisemapping.xml.svgmap.Rect();
        final Style style = Style.parse(vmlRect.getStyle());

        float width = style.getWidth();
        svgRect.setWidth(String.valueOf(width));

        float height = style.getHeight();
        svgRect.setHeight(height);

        String top = style.getTop();
        svgRect.setY(Float.parseFloat(top));

        String left = style.getLeft();
        svgRect.setX(Float.parseFloat(left));

        // Fill properties ...
        final String fillColor = vmlRect.getFillcolor();
        svgRect.setFill(fillColor);

        // Stroke properties ...
        final String strokeColor = vmlRect.getStrokecolor();
        svgRect.setStroke(strokeColor);
        svgRect.setStrokeWidth("0.5px");
        return svgRect;

    }

    private Ellipse convert(final Oval vmlOval) {

        /**
         * <v:oval style="LEFT: 5px; VISIBILITY: hidden; WIDTH: 6px; POSITION: absolute; TOP: 5px; HEIGHT: 6px"
         *       coordsize="21600,21600" fillcolor="#e0e5ef" stroked="t" strokecolor="#023bb9" strokeweight="6762emu">
         *   <v:stroke dashstyle="solid"></v:stroke>
         *   <v:fill></v:fill>
         * </v:oval>
         *
         *
         * SVG:
         *   <ellipse stroke="#023BB9" visibility="hidden" fill="#E0E5EF" stroke-width="0.5px" cy="3" cx="3" ry="3" rx="3"
         *        height="6" width="6"></ellipse>
         */
        final Style style = Style.parse(vmlOval.getStyle());
        Ellipse svgElipse = null;
        if (style.isVisible()) {
            svgElipse = new Ellipse();

            float width = style.getWidth();
            svgElipse.setWidth(width);
            svgElipse.setRx(width / 2);

            float height = style.getHeight();
            svgElipse.setHeight(height);
            svgElipse.setRy(height / 2);

            String top = style.getTop();
            svgElipse.setCy(Float.parseFloat(top) + (width / 2));

            String left = style.getLeft();
            svgElipse.setCx(Float.parseFloat(left) + (height / 2));

            // Fill properties ...
            final String fillColor = vmlOval.getFillcolor();
            svgElipse.setFill(fillColor);

            // Stroke properties ...
            final String strokeColor = vmlOval.getStrokecolor();
            svgElipse.setStroke(strokeColor);
            svgElipse.setStrokeWidth("0.5px");
        }
        return svgElipse;
    }

    private com.wisemapping.xml.svgmap.Line convert(final Line vmlLine) {
        /**
         * VML:
         * <v:line style="POSITION: absolute" from="0,0" to="157,-150" fillcolor="white" stroked="t" strokecolor="#495879"
         *       strokeweight="1px">
         * <:stroke dashstyle="solid"></v:stroke>
         * </v:line>
         *
         * SVG:
         *  <line y2="14" x2="49" y1="14" x1="-1" visibility="hidden" style="cursor: move;" stroke="#495879"
         stroke-width="1px"></line>
         */

        com.wisemapping.xml.svgmap.Line svgLine = new com.wisemapping.xml.svgmap.Line();
        final String from = vmlLine.getFrom();
        final String[] fromPoints = from.split(",");

        svgLine.setX1(Float.parseFloat(fromPoints[0]));
        svgLine.setY1(Float.parseFloat(fromPoints[1]));

        final String to = vmlLine.getTo();
        final String[] toPoints = to.split(",");

        svgLine.setX2(Float.parseFloat(toPoints[0]));
        svgLine.setY2(Float.parseFloat(toPoints[1]));

        String strokeweight = vmlLine.getStrokeweight();
        svgLine.setStrokeWidth(strokeweight);

        String stokeColor = vmlLine.getStrokecolor();
        svgLine.setStroke(stokeColor);

        return svgLine;
    }

    private Text convertTextShape(Shape vmlTextShape, float boxWidth, float boxHeigth) {
        /**
         * <v:shape
         *        style="Z-INDEX: 10; LEFT: 9px; WIDTH: 130px; CURSOR: default; POSITION: absolute; TOP: 9px; HEIGHT: 1px; antialias: true"
         *        coordsize="100,100">
         *   <v:textbox
         *            style="MARGIN-TOP: 0.187pt; LEFT: auto; FONT: bold 19px verdana; MARGIN-LEFT: 0.25pt; OVERFLOW: visible; WIDTH: 138.531pt; COLOR: #023bb9; POSITION: absolute; TOP: auto; HEIGHT: 0.343pt"
         *            xFontScale="1.4" xTextSize="100.1,16.0" inset="0,0,0,0">
         *        <SPAN style="WIDTH: 100%; HEIGHT: 100%">
         *            <SPAN>Central Topic</SPAN>
         *        </SPAN>
         *    </v:textbox>
         * </v:shape>
         * SVG:
         *  <text x="9" y="19" style="cursor: default;" fill="#023BB9" font-weight="bold" font-style="normal"
         *     font-size="13.4375" font-family="verdana" focusable="true">Central Topic
         *  </text>
         */
        final Text svgText = new Text();
        Textbox vmlTextBox = vmlTextShape.getTextbox();
        final String textBoxStyleStr = vmlTextBox.getStyle();
        final Style textBoxStyle = Style.parse(textBoxStyleStr);

        String fontStyle = svgText.getFontStyle();
        svgText.setFontStyle(fontStyle);

        // @todo: Take this hardcode from javascript ...
        float fontSize = textBoxStyle.getFontSize();
        float scale = vmlTextBox.getXFontScale();
        float svgFontSize = fontSize / scale;

        svgText.setFontSize(svgFontSize);

        // Set text properties...
        final String textValue = vmlTextBox.getSPAN().getSPAN();
        svgText.setContent(textValue);

        final String color = textBoxStyle.getColor();
        svgText.setFill(color);

        final String style = textBoxStyle.getFontWidth();
        svgText.setFontWeight(style);

        // Positionate font...
        final String textSize = vmlTextBox.getXTextSize();
        final String[] split = textSize.split(",");
        float textWidth = Float.valueOf(split[0]);
        float textHeight = Float.valueOf(split[1]);

        svgText.setX(boxWidth - textWidth);
        svgText.setY(boxHeigth - textHeight + CORRECTION_HARDCODE);

        return svgText;

    }

    private com.wisemapping.xml.svgmap.Rect convert(Roundrect vmlRect) {

        /*
         * VML:
         *  <v:roundrect style="LEFT: 0px; WIDTH: 62px; CURSOR: move; POSITION: absolute; TOP: 0px; HEIGHT: 23px"
         *            arcsize="9830f" coordsize="21600,21600" fillcolor="#e0e5ef" stroked="t" strokecolor="#023bb9"
         *            strokeweight="6762emu">
         *       <v:stroke dashstyle="solid"></v:stroke>
         *       <v:fill></v:fill>
         *   </v:roundrect>
         *
         * SVG:
         *   <rect style="cursor: move;" fill="#E0E5EF" stroke="#023BB9" stroke-width="0.5px" y="0" x="0" ry="2.7" rx="2.7"
         *      height="18" width="68"></rect>
         *
        */
        final com.wisemapping.xml.svgmap.Rect svgRect = new com.wisemapping.xml.svgmap.Rect();
        final Style style = Style.parse(vmlRect.getStyle());
        svgRect.setRy(2.7F);
        svgRect.setRx(2.7F);

        float width = style.getWidth();
        svgRect.setWidth(String.valueOf(width));

        float height = style.getHeight();
        svgRect.setHeight(height);

        String top = style.getTop();
        svgRect.setY(Float.parseFloat(top));

        String left = style.getLeft();
        svgRect.setX(Float.parseFloat(left));

        // Fill properties ...
        final String fillColor = vmlRect.getFillcolor();
        svgRect.setFill(fillColor);

        // Stroke properties ...
        final String strokeColor = vmlRect.getStrokecolor();
        svgRect.setStroke(strokeColor);
        svgRect.setStrokeWidth("0.5px");

        return svgRect;

    }

    private com.wisemapping.xml.svgmap.Polyline convert(Polyline vmlPolyline) {

        /*
         *     <v:polyline style="POSITION: absolute" rPoints="167.1, 100.0 177.1, 100.0 177.1, 130.5 182.1, 135.5 230.6, 135.5"
         *       filled="f" strokecolor="#495879" strokeweight=".75pt">
         *       <v:stroke opacity="1" dashstyle="solid"></v:stroke>
         *       </v:polyline>
         *
         *     <polyline points="173.5,100 183.5,100 183.5,129 188.5,134 240,134" stroke-opacity="1" stroke="#495879"
         *     stroke-width="1px" fill="none"></polyline>
         */
        final com.wisemapping.xml.svgmap.Polyline svgPolyline = svgObjectFactory.createPolyline();

        final String rPoints = vmlPolyline.getXPoints();
        svgPolyline.setPoints(rPoints);

        final String strokeColor = vmlPolyline.getStrokecolor();
        svgPolyline.setStroke(strokeColor);

        // @todo: Take from SVG.
        svgPolyline.setFill("none");
        svgPolyline.setStrokeWidth("1px");
        svgPolyline.setStrokeOpacity("1");

        return svgPolyline;
    }


    public void toXml(OutputStream os) throws JAXBException {
        final JAXBContext svgContext = JAXBContext.newInstance("com.wisemapping.xml.svgmap");
        Marshaller m = svgContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(svgRootElem, os);
    }

    public void toXml(Writer os) throws JAXBException {
        final JAXBContext svgContext = JAXBContext.newInstance("com.wisemapping.xml.svgmap");
        Marshaller m = svgContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(svgRootElem, os);
    }

}
