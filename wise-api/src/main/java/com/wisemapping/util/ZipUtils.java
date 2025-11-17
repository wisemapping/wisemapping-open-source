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

package com.wisemapping.util;


import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static byte[] zipToBytes(byte[] zip) throws IOException {
        if (zip == null) {
            return null;
        }
        
        try (ByteArrayInputStream in = new ByteArrayInputStream(zip);
             ZipInputStream zipIn = new ZipInputStream(in)) {
            zipIn.getNextEntry();
            byte[] result = IOUtils.toByteArray(zipIn);
            zipIn.closeEntry();
            return result;
        }
    }

    public static byte[] bytesToZip(@NotNull final byte[] content) throws IOException {
        final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(byteArray)) {
            ZipEntry zEntry = new ZipEntry("content");
            zip.putNextEntry(zEntry);
            IOUtils.write(content, zip);
            zip.closeEntry();
        }
        // Note: ByteArrayOutputStream doesn't need to be closed, but ZipOutputStream must be closed
        // to finalize the zip structure before calling toByteArray()
        return byteArray.toByteArray();
    }
}
