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

public class Style {

    private float width;
    private float height;
    private String left;
    private String top;
    private float fontSize;
    private String fontFamily;
    private String color;
    private String fontWidth;
    private boolean isVisible = true;

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public static Style parse(final String styleValue) {
        Style result = new Style();
        String[] strings = styleValue.split(";");

        for (String style : strings) {
            final String key = style.substring(0, style.indexOf(":"));
            String value = style.substring(style.indexOf(":") + 1, style.length());
            value = value.trim();

            if (key.trim().equals("WIDTH")) {
                result.setWidth(parseFloat(value));
            } else if (key.trim().equals("HEIGHT")) {
                result.setHeight(parseFloat(value));
            }
            if (key.trim().equals("TOP")) {
                result.setTop(removeUnit(value));
            } else if (key.trim().equals("LEFT")) {
                result.setLeft(removeUnit(value));
            } else if (key.trim().equals("FONT")) {
                final String[] fontValues = value.split(" ");
                if (fontValues.length == 3) {
                    result.setFontWidth(fontValues[0]);
                    result.setFontSize(parseFloat(fontValues[1]));
                    result.setFontFamily(fontValues[2]);
                } else if (fontValues.length == 2) {
                    result.setFontSize(parseFloat(fontValues[0]));
                    result.setFontFamily(fontValues[1]);
                }
            } else if (key.trim().equals("COLOR")) {
                result.setColor(value);
            } else if (key.trim().equals("VISIBILITY")) {
                result.setVisible(!"hidden".equals(value));
            }

        }
        return result;
    }

    private void setFontWidth(String v) {
        this.fontWidth = v;
    }

    private void setColor(String value) {
        this.color = value;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public static String removeUnit(String value) {
        String number;
        if (value.indexOf("px") != -1 || value.indexOf("pt") != -1) {
            number = value.substring(0, value.length() - 2);
        } else {
            number = value;
        }
        return number;
    }

    public static float parseFloat(String value) {
        String number = removeUnit(value);
        return Float.parseFloat(number);
    }


    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }


    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getColor() {
        return this.color;
    }

    public String getFontWidth() {
        return fontWidth;
    }
}


