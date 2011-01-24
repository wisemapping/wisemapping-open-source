/*
*    Copyright [2011] [wisemapping]
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

public enum Font {

    VERDANA("Verdana"),
    TIMES("Times"),
    TAHOMA("Tahoma"),
    ARIAL("Arial");

    private String fontName;

    Font (String name)
    {
        this.fontName = name;
    }

    public String getFontName()
    {
        return fontName;
    }

    public static boolean isValidFont(String font) {
        boolean isValid = false;
        try {
            if (font != null) {
                isValid = Font.valueOf(font.toUpperCase()) != null;
            }
        }
        catch (IllegalArgumentException ignote) {
        }

        return isValid;
    }
}
