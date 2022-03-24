/*
 *    Copyright [2022] [wisemapping]
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

package com.wisemapping.service;

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.LockException;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class LockManagerImpl implements LockManager {
    private static final int ONE_MINUTE_MILLISECONDS = 1000 * 60;
    private final Map<Integer, LockInfo> lockInfoByMapId;
    private final static Timer expirationTimer = new Timer();
    final private static Logger logger = Logger.getLogger(LockManagerImpl.class);

    @Override
    public boolean isLocked(@NotNull Mindmap mindmap) {
        return this.getLockInfo(mindmap) != null;
    }

    @Override
    public LockInfo getLockInfo(@NotNull Mindmap mindmap) {
        return lockInfoByMapId.get(mindmap.getId());
    }

    @Override
    public void unlockAll(@NotNull final User user) throws LockException, AccessDeniedSecurityException {
        final Set<Integer> mapIds = lockInfoByMapId.keySet();
        for (final Integer mapId : mapIds) {
            final LockInfo lockInfo = lockInfoByMapId.get(mapId);
            if (lockInfo.getUser().identityEquality(user)) {
                unlock(mapId);
            }
        }
    }

    @Override
    public void unlock(@NotNull Mindmap mindmap, @NotNull User user) throws LockException, AccessDeniedSecurityException {
        verifyHasLock(mindmap, user);
        this.unlock(mindmap.getId());
    }

    private void unlock(int mapId) {
        logger.debug("Unlock map id:" + mapId);
        lockInfoByMapId.remove(mapId);
    }

    @Override
    public boolean isLockedBy(@NotNull Mindmap mindmap, @NotNull User collaborator) {
        boolean result = false;
        final LockInfo lockInfo = this.getLockInfo(mindmap);
        if (lockInfo != null && lockInfo.getUser().identityEquality(collaborator)) {
            result = true;
        }
        return result;
    }


    @Override
    public long generateSession() {
        return System.nanoTime();
    }

    @NotNull
    @Override
    public LockInfo lock(@NotNull Mindmap mindmap, @NotNull User user) throws LockException {
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw LockException.createLockLost(mindmap, user, this);
        }

        // Do I need to create a new lock ?
        LockInfo result = lockInfoByMapId.get(mindmap.getId());
        if (result == null) {
            logger.debug("Creating new lock for map id:" + mindmap.getId());
            result = new LockInfo(user, mindmap);
            lockInfoByMapId.put(mindmap.getId(), result);
        }

        // Update timestamp ...
        logger.debug("Updating timeout:" + result);
        result.updateTimeout();

        return result;
    }

    private void verifyHasLock(@NotNull Mindmap mindmap, @NotNull User user) throws LockException, AccessDeniedSecurityException {
        // Only editor can have lock ...
        if (!mindmap.hasPermissions(user, CollaborationRole.EDITOR)) {
            throw new AccessDeniedSecurityException(mindmap.getId(), user);
        }

        // Is the lock assigned to the user ...
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw LockException.createLockLost(mindmap, user, this);
        }
    }

    public LockManagerImpl() {
        lockInfoByMapId = new ConcurrentHashMap<>();
        expirationTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                synchronized (this) {
                    logger.debug("Lock expiration scheduler started. Current locks:" + lockInfoByMapId.keySet());
                    // Search for expired sessions and remove them ....
                    lockInfoByMapId.
                            keySet().
                            stream().
                            filter(mapId -> lockInfoByMapId.get(mapId).isExpired()).
                            forEach(mapId -> unlock(mapId));
                }

            }
        }, ONE_MINUTE_MILLISECONDS, ONE_MINUTE_MILLISECONDS);
    }
}
