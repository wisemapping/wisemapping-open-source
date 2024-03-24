/*
*    Copyright [2022] [wisemapping]
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

package com.wisemapping.rest.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmapHistory {

    static private final SimpleDateFormat sdf;
    private  int id;
    private  Calendar creation;
    private  String creator;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public RestMindmapHistory() {
    }
    
    public RestMindmapHistory(@NotNull MindMapHistory history) {
        this.id = history.getId();
        this.creation = history.getCreationTime();
        final Account editor = history.getEditor();
        this.creator = editor != null ? editor.getFullName() : "";
    }

    public String getCreationTime() {
        return this.toISO8601(creation.getTime());
    }

    public void setCreationTime() {
    }

    
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
    }

    public void setId(int id) {
        this.id=id;
    }

    private String toISO8601(@NotNull Date date) {
        return sdf.format(date) + "Z";
    }

    public int getId() {
        return id;
    }
}
