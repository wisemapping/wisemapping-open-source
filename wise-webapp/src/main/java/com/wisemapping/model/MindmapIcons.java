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

package com.wisemapping.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MindmapIcons {

    private static Map<IconFamily, List<MindmapIcon>> images = new HashMap<IconFamily, List<MindmapIcon>>();

    public static final MindmapIcon FACE_PLAIN = new MindmapIcon(IconFamily.SMILEY, "plain");
    public static final MindmapIcon FACE_SAD = new MindmapIcon(IconFamily.SMILEY, "sad");
    public static final MindmapIcon FACE_CRYING = new MindmapIcon(IconFamily.SMILEY, "crying");
    public static final MindmapIcon FACE_SMILE = new MindmapIcon(IconFamily.SMILEY, "smile");
    public static final MindmapIcon FACE_SURPRISE = new MindmapIcon(IconFamily.SMILEY, "surprise");
    public static final MindmapIcon FACE_WINK = new MindmapIcon(IconFamily.SMILEY, "wink");

    public static final MindmapIcon THUMB__UP = new MindmapIcon(IconFamily.THUMB, "thumb_up");
    public static final MindmapIcon THUMB_DOWN = new MindmapIcon(IconFamily.THUMB, "thumb_down");

    public static final MindmapIcon ARROW_UP = new MindmapIcon(IconFamily.ARROW, "up");
    public static final MindmapIcon ARROW_DOWN = new MindmapIcon(IconFamily.ARROW, "down");
    public static final MindmapIcon ARROW_LEFT = new MindmapIcon(IconFamily.ARROW, "left");
    public static final MindmapIcon ARROW_RIGHT = new MindmapIcon(IconFamily.ARROW, "right");

    static {
        images.put(IconFamily.BULLET, getImagesBullet());
        images.put(IconFamily.FLAG, getImagesFlag());
        images.put(IconFamily.NUMBER, getImagesNumber());
        images.put(IconFamily.TAG, getImagesTag());
        images.put(IconFamily.TASK, getImagesTask());
        images.put(IconFamily.SMILEY, getImagesFaces());
        images.put(IconFamily.BULB, getImagesBulb());
        images.put(IconFamily.ARROW, getImagesArrow());
        images.put(IconFamily.ARROWC, getImagesArrowC());
        images.put(IconFamily.CONN, getImagesConn());
        images.put(IconFamily.THUMB, getImagesThumbs());
        images.put(IconFamily.TICK, getImagesTick());
        images.put(IconFamily.ONOFF, getImagesOnOff());
        images.put(IconFamily.MONEY, getImagesMoney());
        images.put(IconFamily.CHART, getImagesChart());
    }

    private static List<MindmapIcon> getImagesFaces() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(FACE_PLAIN);
        images.add(FACE_SAD);
        images.add(FACE_CRYING);
        images.add(FACE_SMILE);
        images.add(FACE_SURPRISE);
        images.add(FACE_WINK);
        return images;
    }

    private static List<MindmapIcon> getImagesArrow() {
        final List<MindmapIcon> result = new ArrayList<MindmapIcon>();
        result.add(ARROW_UP);
        result.add(ARROW_DOWN);
        result.add(ARROW_LEFT);
        result.add(ARROW_RIGHT);
        return result;
    }

    private static List<MindmapIcon> getImagesArrowC() {
        final List<MindmapIcon> result = new ArrayList<MindmapIcon>();
        result.add(new MindmapIcon(IconFamily.ARROWC, "undo"));
        result.add(new MindmapIcon(IconFamily.ARROWC, "rotate_anticlockwise"));
        result.add(new MindmapIcon(IconFamily.ARROWC, "rotate_clockwise"));
        result.add(new MindmapIcon(IconFamily.ARROWC, "turn_left"));
        result.add(new MindmapIcon(IconFamily.ARROWC, "turn_right"));
        return result;
    }

    private static List<MindmapIcon> getImagesBulb() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.BULB, "light_on"));
        images.add(new MindmapIcon(IconFamily.BULB, "light_off"));
        return images;
    }

    private static List<MindmapIcon> getImagesTick() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.TICK, "tick"));
        images.add(new MindmapIcon(IconFamily.TICK, "cross"));
        return images;
    }

    private static List<MindmapIcon> getImagesChart() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.CHART, "bar"));
        images.add(new MindmapIcon(IconFamily.CHART, "line"));
        images.add(new MindmapIcon(IconFamily.CHART, "curve"));
        images.add(new MindmapIcon(IconFamily.CHART, "pie"));
        images.add(new MindmapIcon(IconFamily.CHART, "organisation"));
        return images;
    }

    private static List<MindmapIcon> getImagesOnOff() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.ONOFF, "clock"));
        images.add(new MindmapIcon(IconFamily.ONOFF, "clock_red"));
        images.add(new MindmapIcon(IconFamily.ONOFF, "add"));
        images.add(new MindmapIcon(IconFamily.ONOFF, "delete"));
        return images;
    }

    private static List<MindmapIcon> getImagesMoney() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.MONEY, "money"));
        images.add(new MindmapIcon(IconFamily.MONEY, "dollar"));
        images.add(new MindmapIcon(IconFamily.MONEY, "euro"));
        images.add(new MindmapIcon(IconFamily.MONEY, "pound"));
        images.add(new MindmapIcon(IconFamily.MONEY, "yen"));
        images.add(new MindmapIcon(IconFamily.MONEY, "coins"));
        images.add(new MindmapIcon(IconFamily.MONEY, "ruby"));
        return images;
    }

    private static List<MindmapIcon> getImagesThumbs() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(THUMB__UP);
        images.add(THUMB_DOWN);
        return images;
    }

    private static List<MindmapIcon> getImagesConn() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.CONN, "connect"));
        images.add(new MindmapIcon(IconFamily.CONN, "disconnect"));
        return images;
    }

    private static List<MindmapIcon> getImagesBullet() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.BULLET, "black"));
        images.add(new MindmapIcon(IconFamily.BULLET, "blue"));
        images.add(new MindmapIcon(IconFamily.BULLET, "green"));
        images.add(new MindmapIcon(IconFamily.BULLET, "orange"));
        images.add(new MindmapIcon(IconFamily.BULLET, "red"));
        images.add(new MindmapIcon(IconFamily.BULLET, "pink"));
        images.add(new MindmapIcon(IconFamily.BULLET, "purple"));
        return images;
    }

    private static List<MindmapIcon> getImagesFlag() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.FLAG, "blue"));
        images.add(new MindmapIcon(IconFamily.FLAG, "green"));
        images.add(new MindmapIcon(IconFamily.FLAG, "orange"));
        images.add(new MindmapIcon(IconFamily.FLAG, "pink"));
        images.add(new MindmapIcon(IconFamily.FLAG, "purple"));
        images.add(new MindmapIcon(IconFamily.FLAG, "yellow"));
        return images;
    }

    private static List<MindmapIcon> getImagesNumber() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.NUMBER, "one"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "two"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "three"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "four"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "five"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "six"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "seven"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "eight"));
        images.add(new MindmapIcon(IconFamily.NUMBER, "nine"));
        return images;
    }

    private static List<MindmapIcon> getImagesTag() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.TAG, "blue"));
        images.add(new MindmapIcon(IconFamily.TAG, "green"));
        images.add(new MindmapIcon(IconFamily.TAG, "orange"));
        images.add(new MindmapIcon(IconFamily.TAG, "red"));
        images.add(new MindmapIcon(IconFamily.TAG, "pink"));
        images.add(new MindmapIcon(IconFamily.TAG, "yellow"));
        return images;
    }

    private static List<MindmapIcon> getImagesTask() {
        final List<MindmapIcon> images = new ArrayList<MindmapIcon>();
        images.add(new MindmapIcon(IconFamily.TASK, "one"));
        images.add(new MindmapIcon(IconFamily.TASK, "two"));
        images.add(new MindmapIcon(IconFamily.TASK, "three"));
        images.add(new MindmapIcon(IconFamily.TASK, "four"));
        images.add(new MindmapIcon(IconFamily.TASK, "five"));
        return images;
    }


    public static List<MindmapIcon> getIconByFamily(IconFamily family) {
        return images.get(family);
    }

    public static MindmapIcon findById(final @NotNull String id) {
        for (IconFamily imageFamily : images.keySet()) {
            final List<MindmapIcon> mindmapIcons = images.get(imageFamily);
            for (MindmapIcon mindmapIcon : mindmapIcons) {
                if (mindmapIcon.getId().equals(id)) {
                    return mindmapIcon;
                }
            }
        }

        throw new IllegalArgumentException("Image could not be found. Id:" + id);
    }
}
