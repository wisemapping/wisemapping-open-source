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

import com.wisemapping.util.ZipUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MindMap {
    private static final String UTF_8 = "UTF-8";

    //~ Instance fields ......................................................................................

    private Calendar creationTime;
    private String creator;
    private String description;

    private int id;
    private boolean isPublic;
    private Calendar lastModificationTime;
    private String lastModifierUser;

    private Set<MindmapUser> mindmapUsers = new HashSet<MindmapUser>();
    private User owner;
    private String properties;
    private String tags;
    private String title;
    private byte[] xml;

    //~ Constructors .........................................................................................

    public MindMap() {
    }

    public MindMap(Set<MindmapUser> mindmapUsers) {
        this.mindmapUsers = mindmapUsers;
    }

    //~ Methods ..............................................................................................

    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    public void setXmlStr(@NotNull String xml)
            throws IOException {
        this.xml = xml.getBytes(UTF_8);
    }

    public byte[] getXml() {
        return xml;
    }

    public String getXmlStr() throws UnsupportedEncodingException {
        String result = null;
        if (this.xml != null) {
            result = new String(this.xml, UTF_8);
        }
        return result;
    }

    public byte[] getZippedXml()
            throws IOException {
        byte[] result = this.xml;
        if (result != null) {
          result =  ZipUtils.stringToZip(new String(result, UTF_8));
        }
        return result;
    }

    public void setZippedXml(byte[] xml)
            throws IOException {
        this.xml = ZipUtils.zipToString(xml).getBytes(UTF_8);
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getProperties() {
        String ret;
        if (properties == null) {
            ret = "{zoom:0.85,saveOnLoad:true}";
        } else {
            ret = properties;
        }

        return ret;
    }

    public Set<MindmapUser> getMindmapUsers() {
        return mindmapUsers;
    }

    public void setMindmapUsers(Set<MindmapUser> mindmapUsers) {
        this.mindmapUsers = mindmapUsers;
    }

    public void addMindmapUser(MindmapUser mindmapUser) {
        mindmapUsers.add(mindmapUser);
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Calendar getLastModificationTime() {
        return lastModificationTime;
    }

    public Date getLastModificationDate() {
        return new Date();
    }

    public void setLastModificationTime(Calendar lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public String getLastModifierUser() {
        return lastModifierUser;
    }

    public void setLastModifierUser(String lastModifierUser) {
        this.lastModifierUser = lastModifierUser;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creatorUser) {
        this.creator = creatorUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXmlAsJsLiteral()
            throws IOException {
        String xml = this.getXmlStr();
        if (xml != null) {
            xml = xml.replace("'", "\\'");
            xml = xml.replaceAll("\\r|\\n", "");
            xml = xml.trim();
        }
        return xml;
    }


    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public static String getDefaultMindmapXml(@NotNull final String title) {

        final StringBuilder result = new StringBuilder();
        result.append("<map version=\"tango\">");
        result.append("<topic central=\"true\" text=\"");
        result.append(title);
        result.append("\"/></result>");
        return result.toString();
    }

    public MindMap shallowClone() {
        final MindMap result = new MindMap();
        result.setDescription(this.getDescription());
        result.setTitle(this.getTitle());
        result.setProperties(this.getProperties());
        result.setXml(this.getXml());
        result.setTags(this.getTags());

        return result;
    }
}
