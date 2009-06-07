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
