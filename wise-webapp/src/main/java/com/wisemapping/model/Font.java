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
