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
import com.wisemapping.exceptions.SessionExpiredException;
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
    public LockInfo updateExpirationTimeout(@NotNull Mindmap mindmap, @NotNull User user) {
        if (!this.isLocked(mindmap)) {
            throw new IllegalStateException("Lock lost for map. No update possible.");
        }

        final LockInfo result = this.getLockInfo(mindmap);
        if (!result.getUser().identityEquality(user)) {
            throw new IllegalStateException("Could not update map lock timeout if you are not the locking user. User:" + result.getUser() + ", " + user);
        }

        result.updateTimeout();
        result.updateTimestamp(mindmap);
        logger.debug("Timeout updated for:" + mindmap.getId());
        return result;
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
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw new LockException("Lock can be only revoked by the locker.");
        }

        if (!mindmap.hasPermissions(user, CollaborationRole.EDITOR)) {
            throw new AccessDeniedSecurityException(mindmap.getId(), user);
        }

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
    public LockInfo lock(@NotNull Mindmap mindmap, @NotNull User user, long session) throws LockException {
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw LockException.createLockLost(mindmap, user, this);
        }

        LockInfo result = lockInfoByMapId.get(mindmap.getId());
        if (result != null) {
            // Update timeout only...
            logger.debug("Update timestamp:" + mindmap.getId());
            updateExpirationTimeout(mindmap, user);
            // result.setSession(session);
        } else {
            logger.debug("Lock map id:" + mindmap.getId());
            result = new LockInfo(user, mindmap, session);
            lockInfoByMapId.put(mindmap.getId(), result);
        }
        return result;
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

    public long verifyAndUpdateLock(@NotNull Mindmap mindmap, @NotNull User user, @Nullable long session, @NotNull long timestamp) throws LockException, SessionExpiredException {
        synchronized (this) {
            // Could the map be updated ?
            verifyLock(mindmap, user, session, timestamp);

            // Update timestamp for lock ...
            final LockInfo lockInfo = this.updateExpirationTimeout(mindmap, user);
            return lockInfo.getTimestamp();
        }
    }

    private void verifyLock(@NotNull Mindmap mindmap, @NotNull User user, long session, long timestamp) throws LockException, SessionExpiredException {

        // The lock was lost, reclaim as the ownership of it.
        final boolean lockLost = this.isLocked(mindmap);
        if (!lockLost) {
            this.lock(mindmap, user, session);
        }

        final LockInfo lockInfo = this.getLockInfo(mindmap);
        if (lockInfo.getUser().identityEquality(user)) {
            long savedTimestamp = mindmap.getLastModificationTime().getTimeInMillis();
            final boolean outdated = savedTimestamp > timestamp;

            if (lockInfo.getSession() == session) {
                // Timestamp might not be returned to the client. This try to cover this case, ignoring the client timestamp check.
                final User lastEditor = mindmap.getLastEditor();
                boolean editedBySameUser = lastEditor == null || user.identityEquality(lastEditor);
                if (outdated && !editedBySameUser) {
                    throw new SessionExpiredException("Map has been updated by " + (lastEditor.getEmail()) + ",Timestamp:" + timestamp + "," + savedTimestamp + ", User:" + lastEditor.getId() + ":" + user.getId() + ",Mail:'" + lastEditor.getEmail() + "':'" + user.getEmail(), lastEditor);
                }
            } else if (outdated) {
                logger.warn("Sessions:" + session + ":" + lockInfo.getSession() + ",Timestamp: " + timestamp + ": " + savedTimestamp);
                // @Todo: Temporally disabled to unblock save action. More research needed.
//                throw new MultipleSessionsOpenException("Sessions:" + session + ":" + lockInfo.getSession() + ",Timestamp: " + timestamp + ": " + savedTimestamp);
            }
        } else {
            throw new SessionExpiredException("Different Users.", lockInfo.getUser());
        }
    }

}
