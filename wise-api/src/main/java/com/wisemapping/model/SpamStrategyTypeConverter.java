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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for SpamStrategyType enum to/from char database column.
 * Converts the enum to a single character for database storage.
 */
@Converter
public class SpamStrategyTypeConverter implements AttributeConverter<SpamStrategyType, Character> {

    @Override
    public Character convertToDatabaseColumn(SpamStrategyType spamStrategyType) {
        if (spamStrategyType == null) {
            return null;
        }
        return spamStrategyType.getCode();
    }

    @Override
    public SpamStrategyType convertToEntityAttribute(Character code) {
        if (code == null) {
            return null;
        }
        return SpamStrategyType.fromCode(code);
    }
}
