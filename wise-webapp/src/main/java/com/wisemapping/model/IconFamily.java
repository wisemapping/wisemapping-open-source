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

public enum IconFamily {
    FLAG("FLAG","flag_"),
    BULLET("BULLET","bullet_"),
    TAG("TAG","tag_"),
    NUMBER("NUMBER","number_"),
    SMILEY("FACE","face_"),
    ARROW("ARROW","arrow_"),
    ARROWC("ARROWC","arrowc_"),
    CONN("CONN","conn_"),
    BULB("BULB","bulb_"),
    THUMB("THUMB","thumb_"),
    TICK("TICK","tick_"),
    ONOFF("ONOFF","onoff_"),
    MONEY("MONEY","money_"),
    CHART("CHART","chart_"),
    TASK("TASK","task_");

    private String prefix;
    private String name;

    IconFamily(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }
}
