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

package com.wisemapping.importer.freemind;

import com.wisemapping.model.MindmapImage;
import com.wisemapping.model.MindmapImagesFactory;
import com.wisemapping.model.ImageFamily;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class FreemindIconMapper {


    private static Map<String,String> freemindToMindmapIcon = new HashMap<String,String>();
    private static final MindmapImage DEFAULT_ICON = MindmapImagesFactory.getImagesByFamily(ImageFamily.BULLET).get(3);

    public FreemindIconMapper()
    {

    }

    public static String getMindmapIcon(String freemindIconId)
    {

        String iconId = freemindToMindmapIcon.get(freemindIconId);

        // The image doesn´t exists in freemind select he default image
        if (iconId == null)
        {
            iconId = DEFAULT_ICON.getId();
        }
        return iconId;
    }

    static {

        List<MindmapImage> images = MindmapImagesFactory.getImagesByFamily(ImageFamily.BULLET);

        freemindToMindmapIcon.put( "full-1",images.get(0).getId());
        freemindToMindmapIcon.put( "full-2",images.get(1).getId());
        freemindToMindmapIcon.put( "full-3",images.get(2).getId());
        freemindToMindmapIcon.put( "full-4",images.get(3).getId());
        freemindToMindmapIcon.put( "full-5",images.get(4).getId());
        freemindToMindmapIcon.put( "full-6",images.get(5).getId());
        freemindToMindmapIcon.put( "full-7",images.get(6).getId());

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.BULB);
        freemindToMindmapIcon.put( "idea",images.get(0).getId());

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.TICK);
        freemindToMindmapIcon.put( "button_ok",images.get(0).getId());
        freemindToMindmapIcon.put( "button_cancel",images.get(1).getId());

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.ARROW);
        freemindToMindmapIcon.put( "back",images.get(2).getId());
        freemindToMindmapIcon.put( "forward",images.get(3).getId());

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.FACE);
        freemindToMindmapIcon.put( "ksmiletris",images.get(3).getId());

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.FLAG);

        final MindmapImage orangeFlag = images.get(2);
        freemindToMindmapIcon.put("flag", orangeFlag.getId());
    }
}
