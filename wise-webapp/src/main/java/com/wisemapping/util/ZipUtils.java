/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

package com.wisemapping.util;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

public class ZipUtils {

    public static String zipToString(byte[] zip) throws IOException {

        String result = null;
        if (zip != null)
        {
            final ByteArrayInputStream in = new ByteArrayInputStream(zip);
            final ZipInputStream zipIn = new ZipInputStream(in);
            zipIn.getNextEntry();

            byte[] buffer = new byte[512];

            int len;
            StringBuffer sb_result = new StringBuffer();

            while ((len = zipIn.read(buffer)) > 0) {



                sb_result.append(new String(buffer, 0, len, Charset.forName("UTF-8")));
            }

            zipIn.closeEntry();
            zipIn.close();
            result = sb_result.toString();
        }

        return result;
    }

    public static  byte[] stringToZip(String content) throws IOException {
        ZipOutputStream zip = null;
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

        
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
        try {
            zip = new ZipOutputStream(byteArray);

            ZipEntry zEntry = new ZipEntry("content");
            zip.putNextEntry(zEntry);
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                zip.write(buffer, 0, bytesRead);
            }
            zip.closeEntry();
        }        
        finally
        {
            if (zip != null)
            {
                zip.flush();
                zip.close();
            }
        }

        return byteArray.toByteArray();
    }
}
