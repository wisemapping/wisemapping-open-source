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

package com.wisemapping.exporter.freemind;

import com.wisemapping.model.MindmapImagesFactory;
import com.wisemapping.model.ImageFamily;
import com.wisemapping.model.MindmapImage;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class FreemindIconMapper {


    private static Map<String,String> mindmapImageToFreemind = new HashMap<String,String>();
    private static final String DEFAULT_ICON = "button_ok";

    public FreemindIconMapper()
    {

    }

    public static String getFreemindIcon(String mindmapImageId)
    {

        String freemindIconId = mindmapImageToFreemind.get(mindmapImageId);

        // The image doesn´t exists in freemind select he default image
        if (freemindIconId == null)
        {
            freemindIconId = DEFAULT_ICON;
        }
        return freemindIconId;
    }

    static {

        List<MindmapImage> images = MindmapImagesFactory.getImagesByFamily(ImageFamily.BULLET);

        for (int idx=0; idx < images.size() ; idx++)
        {
            mindmapImageToFreemind.put(images.get(idx).getId(), "full-"+(idx+1));
        }

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.FLAG);
        for (MindmapImage mindmapImage : images) {
            mindmapImageToFreemind.put(mindmapImage.getId(), "flag");
        }

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.BULB);
        mindmapImageToFreemind.put(images.get(0).getId(), "idea");

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.TICK);
        mindmapImageToFreemind.put(images.get(0).getId(), "button_ok");
        mindmapImageToFreemind.put(images.get(1).getId(), "button_cancel");

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.ARROW);
        mindmapImageToFreemind.put(images.get(2).getId(), "back");
        mindmapImageToFreemind.put(images.get(3).getId(), "forward");

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.FACE);
        mindmapImageToFreemind.put(images.get(3).getId(), "ksmiletris");

        images = MindmapImagesFactory.getImagesByFamily(ImageFamily.FLAG);
        for (MindmapImage mindmapImage : images) {
            mindmapImageToFreemind.put(mindmapImage.getId(), "flag");
        }
    }
}
