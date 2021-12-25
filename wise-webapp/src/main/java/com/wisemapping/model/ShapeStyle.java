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

package com.wisemapping.model;

public enum ShapeStyle
{
    LINE("line"),
    ROUNDED_RECTANGLE("rounded rectagle"),
    RECTANGLE("rectagle"),
    ELLIPSE("elipse"),
    IMAGE("image");

    private final String style;

    ShapeStyle(String style)
    {
        this.style = style;
    }

    public String getStyle()
    {
        return style;
    }

    public static ShapeStyle fromValue(String value) {
        for (ShapeStyle shapeStyle : ShapeStyle.values()) {
            if (shapeStyle.getStyle().equals(value)) {
                return shapeStyle;
            }
        }
        throw new IllegalArgumentException("Shape value \"" + value + "\" doesn't match with a value shape style.");
    }
}
