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

import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;


public class Colaborator {
    private long id;
    private String email;
    private Calendar creationDate;
    private Set<MindmapUser> mindmapUsers = new HashSet<MindmapUser>();

    public Colaborator() {}

     public Colaborator(Set<MindmapUser> mindmapUsers) {
        this.mindmapUsers = mindmapUsers;
    }

    public void setMindmapUsers(Set<MindmapUser> mindmapUsers)
    {
        this.mindmapUsers = mindmapUsers;
    }

    public void addMindmapUser(MindmapUser mindmaUser)
    {
       mindmapUsers.add(mindmaUser);
    }

    public Set<MindmapUser> getMindmapUsers()
    {
        return mindmapUsers;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }
}
