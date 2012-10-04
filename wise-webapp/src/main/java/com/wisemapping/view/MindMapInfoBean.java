/*
*    Copyright [2012] [wisemapping]
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

package com.wisemapping.view;

import com.wisemapping.model.Mindmap;


public class MindMapInfoBean {
    private String title;
    private String description;
    private Mindmap mindMap;

    public MindMapInfoBean(Mindmap map) {
        this.title = map.getTitle();
        this.description = map.getDescription();

        this.mindMap = map;
    }

    public MindMapInfoBean() {
        this.title = "";
        this.description = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Mindmap getMindMap() {
        if (mindMap != null) {
            mindMap.setTitle(title);
            mindMap.setDescription(description);
        }
        return mindMap;
    }
}
