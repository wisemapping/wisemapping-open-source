/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.model;

/**
 * Result class for spam user queries containing user account and spam count information
 */
public class SpamUserResult {
    
    private final Account user;
    private final long spamCount;
    
    public SpamUserResult(Account user, long spamCount) {
        this.user = user;
        this.spamCount = spamCount;
    }
    
    public Account getUser() {
        return user;
    }
    
    public long getSpamCount() {
        return spamCount;
    }
    
    @Override
    public String toString() {
        return "SpamUserResult{" +
                "user=" + (user != null ? user.getEmail() : "null") +
                ", spamCount=" + spamCount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SpamUserResult that = (SpamUserResult) o;
        
        if (spamCount != that.spamCount) return false;
        return user != null ? user.equals(that.user) : that.user == null;
    }
    
    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (int) (spamCount ^ (spamCount >>> 32));
        return result;
    }
}
