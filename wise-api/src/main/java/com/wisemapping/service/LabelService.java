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
package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.MindmapLabel;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LabelService {

    void addLabel(@NotNull final MindmapLabel label, @NotNull final Account user) throws WiseMappingException;

    @NotNull List<MindmapLabel> getAll(@NotNull final Account user);

    @Nullable
    MindmapLabel findLabelById(int id, @NotNull final Account user);

    MindmapLabel getLabelByTitle(@NotNull String title, @NotNull final Account user);

    void removeLabel(@NotNull final MindmapLabel label, @NotNull final Account user) throws WiseMappingException;
}
