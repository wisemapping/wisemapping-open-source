/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

import java.util.Calendar;
import java.util.Locale;
import java.text.DateFormat;

public class HistoryBean
{
    private Calendar historyTime;
    private String author;
    private int mindmapId;
    private int historyId;

    public HistoryBean() {}

     public HistoryBean(int mindmapId,int historyId,String author, Calendar time)
     {
         this.mindmapId = mindmapId;
         this.author = author;
         this.historyTime = time;
         this.historyId = historyId;
     }

    public Calendar getHistoryTime() {
        return historyTime;
    }

    public String getCreation(Locale locale)
    {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT,locale).format(historyTime.getTime());
    }

    public void setHistoryTime(Calendar historyTime) {
        this.historyTime = historyTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getMindMapId()
    {
        return mindmapId;
    }

    public int getHistoryId()
    {
        return historyId;
    }
}
