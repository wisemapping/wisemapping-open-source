package com.wisemapping.security;

import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;


public class MapAccessPermissionEvaluation implements PermissionEvaluator {
    final private static Logger logger = LogManager.getLogger();

    private MapPermissionsSecurityAdvice readAdvice;

    private MapPermissionsSecurityAdvice updateAdvice;

    public MapAccessPermissionEvaluation(final @NotNull MapPermissionsSecurityAdvice readAdvice, final @NotNull MapPermissionsSecurityAdvice updateAdvice) {
        this.readAdvice = readAdvice;
        this.updateAdvice = updateAdvice;
    }

    @Override
    public boolean hasPermission(
            @NotNull Authentication auth, @NotNull Object targetDomainObject, @NotNull Object permission) {

        logger.log(Level.DEBUG, "auth: " + auth + ",targetDomainObject:" + targetDomainObject + ",permission:" + permission);
        if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)) {
            logger.debug("Permissions could not be validated, illegal parameters.");
            return false;
        }

        boolean result;
        final User user = Utils.getUser();
        final MapAccessPermission perm = MapAccessPermission.valueOf((permission.toString().toUpperCase()));
        if (targetDomainObject instanceof Integer) {
            // Checking permissions by mapId ...
            final int mapId = (Integer) targetDomainObject;
            result = hasPrivilege(mapId, perm);
        } else if (targetDomainObject instanceof Mindmap) {
            final Mindmap map = (Mindmap) targetDomainObject;
            result = hasPrivilege(map, perm);
        } else if (targetDomainObject instanceof Collaborator collab) {
            // Read only operations checks ...
            result = user.identityEquality(collab) || readAdvice.getMindmapService().isAdmin(user);
        } else {
            throw new IllegalArgumentException("Unsupported check control of permissions");
        }

        if (!result) {
            logger.debug("User '" + (user != null ? user.getEmail() : "none") + "' not allowed to invoke");
        }
        return result;
    }


    @Override
    public boolean hasPermission(
            @NotNull Authentication auth, Serializable targetId, @NotNull String targetType, @NotNull Object
            permission) {
        logger.log(Level.FATAL, "Unsupported privilege: auth: " + auth + ",targetId:" + targetType + ",targetType:" + targetType + ", permission:" + permission);
        return false;
    }

    private boolean hasPrivilege(@NotNull int mapId, @NotNull MapAccessPermission permission) {
        boolean result;
        final User user = Utils.getUser();
        if (MapAccessPermission.READ == permission) {
            result = readAdvice.isAllowed(user, mapId);
        } else {
            result = updateAdvice.isAllowed(user, mapId);
        }
        return result;
    }

    private boolean hasPrivilege(@NotNull Mindmap map, @NotNull MapAccessPermission permission) {
        boolean result;
        final User user = Utils.getUser();
        if (MapAccessPermission.READ == permission) {
            result = readAdvice.isAllowed(user, map);
        } else {
            result = updateAdvice.isAllowed(user, map);
        }
        return result;
    }
}