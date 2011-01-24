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

import java.util.List;
import java.util.ArrayList;

public class MindMapCriteria {
    private String title;
    private String description;
    private List<String> tags = new ArrayList<String>();
    private boolean orConnector = false;
    private int pageNro = 0;

    public MindMapCriteria() {
    }

    public int getPageNro()
    {
        return pageNro;
    }

    public void setPageNro(int page)
    {
        this.pageNro = page;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public void orCriteria() {
        this.orConnector = true;
    }

    public void andCriteria() {
        this.orConnector = false;
    }

    public boolean isOrCriteria() {
        return this.orConnector;
    }

    public boolean isEmpty() {
        return !(getTags() != null && !getTags().isEmpty() || getTitle() != null || getDescription() != null);
    }

    public static MindMapCriteria EMPTY_CRITERIA = new MindMapCriteria();
}
