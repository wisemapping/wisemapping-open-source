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

import com.wisemapping.dao.LabelManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("labelService")
@Transactional(propagation = Propagation.REQUIRED)
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelManager labelManager;

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'WRITE')")
    public void addLabel(@NotNull final Label label, @NotNull final User user) {

        label.setCreator(user);
        labelManager.addLabel(label);
    }

    @NotNull
    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    public List<Label> getAll(@NotNull final User user) {
        return labelManager.getAllLabels(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    public Label findLabelById(int id, @NotNull final User user) {
        return labelManager.getLabelById(id, user);
    }

    @Nullable
    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'READ')")
    public Label getLabelByTitle(@NotNull String title, @NotNull final User user) {
        return labelManager.getLabelByTitle(title, user);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') && hasPermission(#user, 'WRITE')")
    public void removeLabel(@NotNull Label label, @NotNull User user) throws WiseMappingException {
        if (label.getCreator().equals(user)) {
            labelManager.removeLabel(label);
        } else {
            throw new WiseMappingException("User: "+ user.getFullName()  + "has no ownership on label " + label.getTitle());

        }
    }
}
