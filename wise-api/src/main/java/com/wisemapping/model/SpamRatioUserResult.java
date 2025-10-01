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
 * Result class for spam ratio user queries containing user account, spam count, total count, and spam ratio
 */
public class SpamRatioUserResult {
    
    private final Account user;
    private final long spamCount;
    private final long totalCount;
    private final double spamRatio;
    
    public SpamRatioUserResult(Account user, long spamCount, long totalCount) {
        this.user = user;
        this.spamCount = spamCount;
        this.totalCount = totalCount;
        this.spamRatio = totalCount > 0 ? (double) spamCount / totalCount : 0.0;
    }
    
    public Account getUser() {
        return user;
    }
    
    public long getSpamCount() {
        return spamCount;
    }
    
    public long getTotalCount() {
        return totalCount;
    }
    
    public double getSpamRatio() {
        return spamRatio;
    }
    
    /**
     * Get spam ratio as percentage (0.0 to 100.0)
     */
    public double getSpamRatioPercentage() {
        return spamRatio * 100.0;
    }
    
    @Override
    public String toString() {
        return "SpamRatioUserResult{" +
                "user=" + (user != null ? user.getEmail() : "null") +
                ", spamCount=" + spamCount +
                ", totalCount=" + totalCount +
                ", spamRatio=" + String.format("%.2f%%", getSpamRatioPercentage()) +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SpamRatioUserResult that = (SpamRatioUserResult) o;
        
        if (spamCount != that.spamCount) return false;
        if (totalCount != that.totalCount) return false;
        if (Double.compare(that.spamRatio, spamRatio) != 0) return false;
        return user != null ? user.equals(that.user) : that.user == null;
    }
    
    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (int) (spamCount ^ (spamCount >>> 32));
        result = 31 * result + (int) (totalCount ^ (totalCount >>> 32));
        long temp = Double.doubleToLongBits(spamRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
