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

public class MindmapUser {

    private int id;
    private int roleId;
    private MindMap mindMap;   
    private Colaborator colaborator;

    public MindmapUser(){ }

    public MindmapUser(int role, Colaborator colaborator , MindMap mindmap)
    {
        this.roleId = role;
        this.mindMap =mindmap;
        this.colaborator = colaborator;

        // Guarantee referential integrity
		mindmap.addMindmapUser(this);
		colaborator.addMindmapUser(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public UserRole getRole() {
        return UserRole.values()[roleId];
    }

    public boolean isOwner() {
        return getRole() == UserRole.OWNER;
    }

    public boolean isCollaborator() {
        return getRole() == UserRole.COLLABORATOR;
    }

    public boolean isViewer() {
        return getRole() == UserRole.VIEWER;
    }

    public MindMap getMindMap() {
        return mindMap;
    }

    public void setMindMap(MindMap mindMap) {
        this.mindMap = mindMap;
    }

    public Colaborator getColaborator() {
        return colaborator;
    }

    public void setColaborator(Colaborator colaborator) {
        this.colaborator = colaborator;
    }    
}
