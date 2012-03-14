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

import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.UserRole;
import com.wisemapping.model.User;

import java.text.DateFormat;
import java.util.*;

public class MindMapBean {
    private MindMap mindMap;
    private List<ColaboratorBean> viewers;
    private List<ColaboratorBean> colaborators;

    public MindMapBean(final MindMap mindmap) {
        this.mindMap = mindmap;
        this.colaborators = getColaboratorBy(mindmap.getMindmapUsers(), UserRole.COLLABORATOR);
        this.viewers = getColaboratorBy(mindmap.getMindmapUsers(), UserRole.VIEWER);
    }

    public MindMap getMindMap() {
        return mindMap;
    }

    public boolean getPublic() {
        return mindMap.isPublic();
    }

    public String getTitle() {
        return mindMap.getTitle();
    }

    public String getDescription() {
        return mindMap.getDescription();
    }

    public int getId() {
        return mindMap.getId();
    }

    public List<ColaboratorBean> getViewers() {
        return viewers;
    }

    public List<ColaboratorBean> getCollaborators() {
        return colaborators;
    }

    public String getLastEditor() {
        return mindMap.getLastModifierUser();
    }

    public String getLastEditDate() {
        String result = "";
        Calendar lastEditTime = Calendar.getInstance();
        lastEditTime.setTime(mindMap.getLastModificationTime().getTime());
        Calendar now = Calendar.getInstance();
        int dayDiff = getDaysBetween(now, lastEditTime);
        if (dayDiff > 0) {
            result = dayDiff + " days ago";
        } else if (dayDiff == 0) {
            result = "today";
        }
        return result;
    }

    public Calendar getLastEditTime() {
        return mindMap.getLastModificationTime();
    }

    public String getCreationTime() {
        return DateFormat.getInstance().format(mindMap.getCreationTime().getTime());
    }

    public String getCreationUser() {
        return mindMap.getCreator();
    }

    public String getTags() {
        return mindMap.getTags();
    }

    private List<ColaboratorBean> getColaboratorBy(Set<MindmapUser> source, UserRole role) {
        List<ColaboratorBean> col = new ArrayList<ColaboratorBean>();
        if (source != null) {
            for (MindmapUser mu : source) {
                if (mu.getRole() == role) {
                    col.add(new ColaboratorBean(mu.getCollaborator(), mu.getRole()));
                }
            }
        }
        return col;
    }

    private static int getDaysBetween(java.util.Calendar d1, java.util.Calendar d2) {
        if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
            java.util.Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }
        int days = d2.get(java.util.Calendar.DAY_OF_YEAR) -
                d1.get(java.util.Calendar.DAY_OF_YEAR);
        int y2 = d2.get(java.util.Calendar.YEAR);
        if (d1.get(java.util.Calendar.YEAR) != y2) {
            d1 = (java.util.Calendar) d1.clone();
            do {
                days += d1.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
                d1.add(java.util.Calendar.YEAR, 1);
            } while (d1.get(java.util.Calendar.YEAR) != y2);
        }
        return days;
    }

    public static class MindMapBeanComparator implements Comparator<MindMapBean> {

        public int compare(MindMapBean o1, MindMapBean o2) {
            return o1.getLastEditTime().compareTo(o2.getLastEditTime());
        }
    }

    public int getCountColaborators() {
        return colaborators != null ? colaborators.size() : 0;
    }

    public int getCountViewers() {
        return viewers != null ? viewers.size() : 0;
    }

    public int getCountShared() {
        return getCountColaborators() + getCountViewers();
    }

    public boolean isShared() {
        return getCountShared() > 0;
    }

    public void setTitle(String t) {
        mindMap.setTitle(t);
    }

    public void setDescription(String d) {
        mindMap.setDescription(d);
    }

    public User getOwner() {
        return mindMap.getOwner();
    }
}
