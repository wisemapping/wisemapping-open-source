/*
*    Copyright [2011] [wisemapping]
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

package com.wisemapping.validator;

import com.wisemapping.controller.Messages;
import com.wisemapping.importer.ImportFormat;
import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.ImporterFactory;
import com.wisemapping.model.MindMap;
import com.wisemapping.view.ImportMapBean;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.io.ByteArrayInputStream;

public class ImportMapValidator extends MapInfoValidator {

   public boolean supports(final Class clazz) {
        return clazz.equals(ImportMapBean.class);
    }

    public void validate(Object obj, Errors errors) {
        ImportMapBean bean = (ImportMapBean) obj;

        super.validate(obj,errors);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mapFile", Messages.FIELD_REQUIRED);
        try {
            final Importer importer = ImporterFactory.getInstance().getImporter(ImportFormat.FREEMIND);
            final ByteArrayInputStream stream = new ByteArrayInputStream(bean.getMapFile().getBytes());
            final MindMap map = importer.importMap(bean.getTitle(),bean.getDescription(),stream);

            bean.setImportedMap(map);

        } catch (ImporterException e) {
              Object[] errorArgs  = new Object[]{e.getMessage()};
              errors.rejectValue("mapFile", Messages.IMPORT_MAP_ERROR,errorArgs,"FreeMind could not be imported.");
        }
    }
}
