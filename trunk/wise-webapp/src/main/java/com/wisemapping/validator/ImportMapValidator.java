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
              errors.rejectValue("mapFile",Messages.IMPORT_MAP_ERROR);
        }
    }
}
