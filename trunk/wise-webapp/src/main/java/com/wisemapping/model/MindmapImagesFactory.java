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

package com.wisemapping.model;

import java.util.*;

public class MindmapImagesFactory {

    private static Map<ImageFamily,List<MindmapImage>> images = new HashMap<ImageFamily,List<MindmapImage>>();

    static {
        images.put(ImageFamily.BULLET, getImagesBullet());
        images.put(ImageFamily.FLAG,getImagesFlag());
        images.put(ImageFamily.NUMBER,getImagesNumber());
        images.put(ImageFamily.TAG,getImagesTag());
        images.put(ImageFamily.TASK,getImagesTask());
        images.put(ImageFamily.FACE,getImagesFaces());
        images.put(ImageFamily.BULB,getImagesBulb());
        images.put(ImageFamily.ARROW,getImagesArrow());
        images.put(ImageFamily.ARROWC,getImagesArrowC());
        images.put(ImageFamily.CONN,getImagesConn());
        images.put(ImageFamily.THUMB,getImagesThumbs());
        images.put(ImageFamily.TICK,getImagesTick());
        images.put(ImageFamily.ONOFF,getImagesOnOff());
        images.put(ImageFamily.MONEY,getImagesMoney());
        images.put(ImageFamily.CHART,getImagesChart());
    }

    private static List<MindmapImage> getImagesFaces()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("plain",ImageFamily.FACE));
        images.add(new MindmapImage("sad",ImageFamily.FACE));
        images.add(new MindmapImage("crying",ImageFamily.FACE));
        images.add(new MindmapImage("smile",ImageFamily.FACE));
        images.add(new MindmapImage("surprise",ImageFamily.FACE));
        images.add(new MindmapImage("wink",ImageFamily.FACE));
        return images;
    }

    private static List<MindmapImage> getImagesArrow()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("up",ImageFamily.ARROW));
        images.add(new MindmapImage("down",ImageFamily.ARROW));
        images.add(new MindmapImage("left",ImageFamily.ARROW));
        images.add(new MindmapImage("right",ImageFamily.ARROW));
        return images;
    }

     private static List<MindmapImage> getImagesArrowC()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("undo",ImageFamily.ARROWC));
        images.add(new MindmapImage("rotate_anticlockwise",ImageFamily.ARROWC));
        images.add(new MindmapImage("rotate_clockwise",ImageFamily.ARROWC));
        images.add(new MindmapImage("turn_left",ImageFamily.ARROWC));
        images.add(new MindmapImage("turn_right",ImageFamily.ARROWC));
        return images;
    }

    private static List<MindmapImage> getImagesBulb()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("light_on",ImageFamily.BULB));
        images.add(new MindmapImage("light_off",ImageFamily.BULB));        
        return images;
    }

    private static List<MindmapImage> getImagesTick()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("tick",ImageFamily.TICK));
        images.add(new MindmapImage("cross",ImageFamily.TICK));
        return images;
    }

    private static List<MindmapImage> getImagesChart()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("bar",ImageFamily.CHART));
        images.add(new MindmapImage("line",ImageFamily.CHART));
        images.add(new MindmapImage("curve",ImageFamily.CHART));
        images.add(new MindmapImage("pie",ImageFamily.CHART));
        images.add(new MindmapImage("organisation",ImageFamily.CHART));
        return images;
    }

    private static List<MindmapImage> getImagesOnOff()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("clock",ImageFamily.ONOFF));
        images.add(new MindmapImage("clock_red",ImageFamily.ONOFF));
        images.add(new MindmapImage("add",ImageFamily.ONOFF));
        images.add(new MindmapImage("delete",ImageFamily.ONOFF));
        return images;
    }

    private static List<MindmapImage> getImagesMoney()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("money",ImageFamily.MONEY));
        images.add(new MindmapImage("dollar",ImageFamily.MONEY));
        images.add(new MindmapImage("euro",ImageFamily.MONEY));
        images.add(new MindmapImage("pound",ImageFamily.MONEY));
        images.add(new MindmapImage("yen",ImageFamily.MONEY));
        images.add(new MindmapImage("coins",ImageFamily.MONEY));
        images.add(new MindmapImage("ruby",ImageFamily.MONEY));
        return images;
    }

    private static List<MindmapImage> getImagesThumbs()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("thumb_up",ImageFamily.THUMB));
        images.add(new MindmapImage("thumb_down",ImageFamily.THUMB));
        return images;
    }

    private static List<MindmapImage> getImagesConn()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("connect",ImageFamily.CONN));
        images.add(new MindmapImage("disconnect",ImageFamily.CONN));
        return images;
    }

    private static List<MindmapImage> getImagesBullet()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("black",ImageFamily.BULLET));
        images.add(new MindmapImage("blue",ImageFamily.BULLET));
        images.add(new MindmapImage("green",ImageFamily.BULLET));
        images.add(new MindmapImage("orange",ImageFamily.BULLET));
        images.add(new MindmapImage("red",ImageFamily.BULLET));
        images.add(new MindmapImage("pink",ImageFamily.BULLET));
        images.add(new MindmapImage("purple",ImageFamily.BULLET));
        return images;
    }

    private static List<MindmapImage> getImagesFlag()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("blue",ImageFamily.FLAG));
        images.add(new MindmapImage("green",ImageFamily.FLAG));
        images.add(new MindmapImage("orange",ImageFamily.FLAG));
        images.add(new MindmapImage("pink",ImageFamily.FLAG));
        images.add(new MindmapImage("purple",ImageFamily.FLAG));
        images.add(new MindmapImage("yellow",ImageFamily.FLAG));
        return images;
    }

    private static List<MindmapImage> getImagesNumber()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("one",ImageFamily.NUMBER));
        images.add(new MindmapImage("two",ImageFamily.NUMBER));
        images.add(new MindmapImage("three",ImageFamily.NUMBER));
        images.add(new MindmapImage("four",ImageFamily.NUMBER));
        images.add(new MindmapImage("five",ImageFamily.NUMBER));
        images.add(new MindmapImage("six",ImageFamily.NUMBER));
        images.add(new MindmapImage("seven",ImageFamily.NUMBER));
        images.add(new MindmapImage("eight",ImageFamily.NUMBER));
        images.add(new MindmapImage("nine",ImageFamily.NUMBER));
        return images;
    }

    private static List<MindmapImage> getImagesTag()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("blue",ImageFamily.TAG));
        images.add(new MindmapImage("green",ImageFamily.TAG));
        images.add(new MindmapImage("orange",ImageFamily.TAG));
        images.add(new MindmapImage("red",ImageFamily.TAG));
        images.add(new MindmapImage("pink",ImageFamily.TAG));
        images.add(new MindmapImage("yellow",ImageFamily.TAG));
        return images;
    }

    private static List<MindmapImage> getImagesTask()
    {
        final List<MindmapImage> images = new ArrayList<MindmapImage>();
        images.add(new MindmapImage("one",ImageFamily.TASK));
        images.add(new MindmapImage("two",ImageFamily.TASK));
        images.add(new MindmapImage("three",ImageFamily.TASK));
        images.add(new MindmapImage("four",ImageFamily.TASK));
        images.add(new MindmapImage("five",ImageFamily.TASK));
        return images;
    }

    public static Collection<List<MindmapImage>> getAllImages()
    {
        return images.values();
    }

    public static List<MindmapImage> getImagesByFamily(ImageFamily family)
    {
        return images.get(family);
    }
}
