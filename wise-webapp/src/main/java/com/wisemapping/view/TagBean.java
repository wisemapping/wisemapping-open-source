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

package com.wisemapping.view;

import java.util.Set;

public class TagBean
{
    private Set<String> userTags;
    private String mindmapTitle;
    private int mindmapId;
    private String mindmapTags;

    public TagBean(){}

    public Set<String> getUserTags() {
        return userTags;
    }

    public void setUserTags(Set<String> tags) {
        this.userTags = tags;
    }

    public String getMindmapTags() {
        return mindmapTags;
    }

    public void setMindmapTags(String tags) {
        this.mindmapTags = tags;
    }

    public String getMindmapTitle()
    {
        return mindmapTitle;
    }

    public void setMindmapTitle(String title)
    {
        this.mindmapTitle = title;
    }

    public int getMindmapId()
    {
        return mindmapId;
    }

    public void setMindmapId(int id)
    {
        this.mindmapId = id;
    }
}
