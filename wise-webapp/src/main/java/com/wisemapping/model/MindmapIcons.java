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

import java.lang.IllegalStateException;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings({"UnusedDeclaration"})
public class MindmapIcons {

    private static Map<IconFamily, List<MindmapIcon>> iconsByFamily;

    public static final MindmapIcon FACE_PLAIN = new MindmapIcon(IconFamily.FACE, "plain");
    public static final MindmapIcon FACE_SAD = new MindmapIcon(IconFamily.FACE, "sad");
    public static final MindmapIcon FACE_CRYING = new MindmapIcon(IconFamily.FACE, "crying");
    public static final MindmapIcon FACE_SMILE = new MindmapIcon(IconFamily.FACE, "smile");
    public static final MindmapIcon FACE_SURPRISE = new MindmapIcon(IconFamily.FACE, "surprise");
    public static final MindmapIcon FACE_WINK = new MindmapIcon(IconFamily.FACE, "wink");

    public static final MindmapIcon THUMB__UP = new MindmapIcon(IconFamily.THUMB, "thumb_up");
    public static final MindmapIcon THUMB_DOWN = new MindmapIcon(IconFamily.THUMB, "thumb_down");


    public static final MindmapIcon ARROW_UP = new MindmapIcon(IconFamily.ARROW, "up");
    public static final MindmapIcon ARROW_DOWN = new MindmapIcon(IconFamily.ARROW, "down");
    public static final MindmapIcon ARROW_LEFT = new MindmapIcon(IconFamily.ARROW, "left");
    public static final MindmapIcon ARROW_RIGHT = new MindmapIcon(IconFamily.ARROW, "right");

//    public static final MindmapIcon TASK_ONE = new MindmapIcon(IconFamily.TASK, "one");
//    public static final MindmapIcon TASK_TWO = new MindmapIcon(IconFamily.TASK, "two");
//    public static final MindmapIcon TASK_THREE = new MindmapIcon(IconFamily.TASK, "three");
//    public static final MindmapIcon TASK_FOUR = new MindmapIcon(IconFamily.TASK, "four");
//    public static final MindmapIcon TASK_FIVE = new MindmapIcon(IconFamily.TASK, "five");

    //    public static final MindmapIcon ARROWC_UNDO = new MindmapIcon(IconFamily.ARROWC, "undo");
    public static final MindmapIcon ARROWC_ANTICLOCK_WISE = new MindmapIcon(IconFamily.ARROWC, "rotate_anticlockwise");
    public static final MindmapIcon ARROWC_CLOCK_WISE = new MindmapIcon(IconFamily.ARROWC, "rotate_clockwise");
    public static final MindmapIcon ARROWC_LEFT = new MindmapIcon(IconFamily.ARROWC, "turn_left");
    public static final MindmapIcon ARROWC_RIGHT = new MindmapIcon(IconFamily.ARROWC, "turn_right");

    public static final MindmapIcon BULB_LIGHT_ON = new MindmapIcon(IconFamily.BULB, "light_on");
    public static final MindmapIcon BULB_LIGHT__OFF = new MindmapIcon(IconFamily.BULB, "light_off");

    public static final MindmapIcon TICK_TICK = new MindmapIcon(IconFamily.TICK, "tick");
    public static final MindmapIcon TICK_CROSS = new MindmapIcon(IconFamily.TICK, "cross");

    public static final MindmapIcon CHART_BAR = new MindmapIcon(IconFamily.CHART, "bar");
    public static final MindmapIcon CHART_LINE = new MindmapIcon(IconFamily.CHART, "line");
    public static final MindmapIcon CHART_CURVE = new MindmapIcon(IconFamily.CHART, "curve");
    public static final MindmapIcon CHART_PIE = new MindmapIcon(IconFamily.CHART, "pie");
    public static final MindmapIcon CHART_ORGANISATION = new MindmapIcon(IconFamily.CHART, "organisation");

    public static final MindmapIcon ONOFF_CLOCK = new MindmapIcon(IconFamily.ONOFF, "clock");
    public static final MindmapIcon ONOFF_CLOCK__RED = new MindmapIcon(IconFamily.ONOFF, "clock_red");
    public static final MindmapIcon ONOFF_ADD = new MindmapIcon(IconFamily.ONOFF, "add");
    public static final MindmapIcon ONOFF_DELETE = new MindmapIcon(IconFamily.ONOFF, "delete");
    public static final MindmapIcon ONOFF_STATUS_OFFLINE = new MindmapIcon(IconFamily.ONOFF, "status_offline");
    public static final MindmapIcon ONOFF_STATUS_ONLINE = new MindmapIcon(IconFamily.ONOFF, "status_online");


    public static final MindmapIcon TAG_BLUE = new MindmapIcon(IconFamily.TAG, "blue");
    public static final MindmapIcon TAG_GREEN = new MindmapIcon(IconFamily.TAG, "green");
    public static final MindmapIcon TAG_ORANGE = new MindmapIcon(IconFamily.TAG, "orange");
    public static final MindmapIcon TAG_RED = new MindmapIcon(IconFamily.TAG, "red");
    public static final MindmapIcon TAG_PINK = new MindmapIcon(IconFamily.TAG, "pink");
    public static final MindmapIcon TAG_YELLOW = new MindmapIcon(IconFamily.TAG, "yellow");

//    public static final MindmapIcon NUMBER_ONE = new MindmapIcon(IconFamily.NUMBER, "one");
//    public static final MindmapIcon NUMBER_TWO = new MindmapIcon(IconFamily.NUMBER, "two");
//    public static final MindmapIcon NUMBER_THREE = new MindmapIcon(IconFamily.NUMBER, "three");
//    public static final MindmapIcon NUMBER_FOUR = new MindmapIcon(IconFamily.NUMBER, "four");
//    public static final MindmapIcon NUMBER_FIVE = new MindmapIcon(IconFamily.NUMBER, "five");
//    public static final MindmapIcon NUMBER_SIX = new MindmapIcon(IconFamily.NUMBER, "six");
//    public static final MindmapIcon NUMBER_SEVEN = new MindmapIcon(IconFamily.NUMBER, "seven");
//    public static final MindmapIcon NUMBER_EIGHT = new MindmapIcon(IconFamily.NUMBER, "eight");
//    public static final MindmapIcon NUMBER_NINE = new MindmapIcon(IconFamily.NUMBER, "nine");


    public static final MindmapIcon FUNNY_ANGEL = new MindmapIcon(IconFamily.FUNY, "angel");
    public static final MindmapIcon FUNNY_DEVILISH = new MindmapIcon(IconFamily.FUNY, "devilish");
    public static final MindmapIcon FUNNY_GLASSES = new MindmapIcon(IconFamily.FUNY, "glasses");
    public static final MindmapIcon FUNNY_GRIN = new MindmapIcon(IconFamily.FUNY, "grin");
    public static final MindmapIcon FUNNY_KISS = new MindmapIcon(IconFamily.FUNY, "kiss");
    public static final MindmapIcon FUNNY_MONKEY = new MindmapIcon(IconFamily.FUNY, "monkey");

    public static final MindmapIcon FLAG_BLUE = new MindmapIcon(IconFamily.FLAG, "blue");
    public static final MindmapIcon FLAG_GREEN = new MindmapIcon(IconFamily.FLAG, "green");
    public static final MindmapIcon FLAG_ORANGE = new MindmapIcon(IconFamily.FLAG, "orange");
    public static final MindmapIcon FLAG_PINK = new MindmapIcon(IconFamily.FLAG, "pink");
    public static final MindmapIcon FLAG_PURPLE = new MindmapIcon(IconFamily.FLAG, "purple");
    public static final MindmapIcon FLAG_YELLOW = new MindmapIcon(IconFamily.FLAG, "yellow");

    public static final MindmapIcon BULLET_BLACK = new MindmapIcon(IconFamily.BULLET, "black");
    public static final MindmapIcon BULLET_BLUE = new MindmapIcon(IconFamily.BULLET, "blue");
    public static final MindmapIcon BULLET_BLUEGREEN = new MindmapIcon(IconFamily.BULLET, "green");
    public static final MindmapIcon BULLET_BLUEORANGE = new MindmapIcon(IconFamily.BULLET, "orange");
    public static final MindmapIcon BULLET_BLUERED = new MindmapIcon(IconFamily.BULLET, "red");
    public static final MindmapIcon BULLET_BLUEPINK = new MindmapIcon(IconFamily.BULLET, "pink");
    public static final MindmapIcon BULLET_BLUEPURPLE = new MindmapIcon(IconFamily.BULLET, "purple");

    public static final MindmapIcon MONEY_GENERIC = new MindmapIcon(IconFamily.MONEY, "money");
    public static final MindmapIcon MONEY_DOLLAR = new MindmapIcon(IconFamily.MONEY, "dollar");
    public static final MindmapIcon MONEY_EURO = new MindmapIcon(IconFamily.MONEY, "euro");
    public static final MindmapIcon MONEY_POUND = new MindmapIcon(IconFamily.MONEY, "pound");
    public static final MindmapIcon MONEY_YEN = new MindmapIcon(IconFamily.MONEY, "yen");
    public static final MindmapIcon MONEY_COINS = new MindmapIcon(IconFamily.MONEY, "coins");
    public static final MindmapIcon MONEY_RUBY = new MindmapIcon(IconFamily.MONEY, "ruby");
    public static final MindmapIcon MONEY_CONNECT = new MindmapIcon(IconFamily.CONN, "connect");
    public static final MindmapIcon MONEY_DISCONNECT = new MindmapIcon(IconFamily.CONN, "disconnect");

    public static final MindmapIcon SPORT_BASKETBALL = new MindmapIcon(IconFamily.SPORT, "basketball");
    public static final MindmapIcon SPORT_FOOTBALL = new MindmapIcon(IconFamily.SPORT, "football");
    public static final MindmapIcon SPORT_GOLF = new MindmapIcon(IconFamily.SPORT, "golf");
    public static final MindmapIcon SPORT_RAQUET = new MindmapIcon(IconFamily.SPORT, "raquet");
    public static final MindmapIcon SPORT_SHUTTLECLOCK = new MindmapIcon(IconFamily.SPORT, "shuttlecock");
    public static final MindmapIcon SPORT_SOURCE = new MindmapIcon(IconFamily.SPORT, "soccer");
    public static final MindmapIcon SPORT_TENNIS = new MindmapIcon(IconFamily.SPORT, "tennis");

    public static final MindmapIcon TIME_CALENDAR = new MindmapIcon(IconFamily.TIME, "calendar");
    public static final MindmapIcon TIME_CLOCK = new MindmapIcon(IconFamily.TIME, "clock");
    public static final MindmapIcon TIME_HOURGLASS = new MindmapIcon(IconFamily.TIME, "hourglass");


    public static final MindmapIcon OBJECT_BELL = new MindmapIcon(IconFamily.OBJECT, "bell");
    public static final MindmapIcon OBJECT_BOMB = new MindmapIcon(IconFamily.OBJECT, "clanbomber");
    public static final MindmapIcon OBJECT_KEY = new MindmapIcon(IconFamily.OBJECT, "key");
    public static final MindmapIcon OBJECT_PENCIL = new MindmapIcon(IconFamily.OBJECT, "pencil");
    public static final MindmapIcon OBJECT_PHONE = new MindmapIcon(IconFamily.OBJECT, "phone");
    public static final MindmapIcon OBJECT_MAGNIFIER = new MindmapIcon(IconFamily.OBJECT, "magnifier");
    public static final MindmapIcon OBJECT_CLIP = new MindmapIcon(IconFamily.OBJECT, "clip");
    public static final MindmapIcon OBJECT_MUSIC = new MindmapIcon(IconFamily.OBJECT, "music");
    public static final MindmapIcon OBJECT_STAR = new MindmapIcon(IconFamily.OBJECT, "star");
    public static final MindmapIcon OBJECT_WIZARD = new MindmapIcon(IconFamily.OBJECT, "wizard");
    public static final MindmapIcon OBJECT_HOUSE = new MindmapIcon(IconFamily.OBJECT, "house");
    public static final MindmapIcon OBJECT_CAKE = new MindmapIcon(IconFamily.OBJECT, "cake");
    public static final MindmapIcon OBJECT_CAMERA = new MindmapIcon(IconFamily.OBJECT, "camera");
    public static final MindmapIcon OBJECT_PALETTE = new MindmapIcon(IconFamily.OBJECT, "palette");
    public static final MindmapIcon OBJECT_RAINBOW = new MindmapIcon(IconFamily.OBJECT, "rainbow");


    public static final MindmapIcon WEATHER_CLEAR_NIGHT = new MindmapIcon(IconFamily.WEATHER, "clear-night");
    public static final MindmapIcon WEATHER_CLEAR = new MindmapIcon(IconFamily.WEATHER, "clear");
    public static final MindmapIcon WEATHER_FEW_CLOUDS_NIGHT = new MindmapIcon(IconFamily.WEATHER, "few-clouds-night");
    public static final MindmapIcon WEATHER_FEW_CLOUDS = new MindmapIcon(IconFamily.WEATHER, "few-clouds");
    public static final MindmapIcon WEATHER_OVERCAST = new MindmapIcon(IconFamily.WEATHER, "overcast");
    public static final MindmapIcon WEATHER_SEVERE_ALERT = new MindmapIcon(IconFamily.WEATHER, "severe-alert");
    public static final MindmapIcon WEATHER_SHOWERS_SCATTERED = new MindmapIcon(IconFamily.WEATHER, "showers-scattered");
    public static final MindmapIcon WEATHER_SHOWERS = new MindmapIcon(IconFamily.WEATHER, "showers");
    public static final MindmapIcon WEATHER_SNOW = new MindmapIcon(IconFamily.WEATHER, "snow");
    public static final MindmapIcon WEATHER_STORM = new MindmapIcon(IconFamily.WEATHER, "storm");

    public static final MindmapIcon MAIL_ENVELOP = new MindmapIcon(IconFamily.MAIL, "envelop");
    public static final MindmapIcon MAIL_MAILBOX = new MindmapIcon(IconFamily.MAIL, "mailbox");
    public static final MindmapIcon MAIL_EDIT = new MindmapIcon(IconFamily.MAIL, "edit");
    public static final MindmapIcon MAIL_LIST = new MindmapIcon(IconFamily.MAIL, "list");


    public static final MindmapIcon SIGN_WARNING = new MindmapIcon(IconFamily.SIGN, "warning");
    public static final MindmapIcon SIGN_INFO = new MindmapIcon(IconFamily.SIGN, "info");
    public static final MindmapIcon SIGN_STOP = new MindmapIcon(IconFamily.SIGN, "stop");
    public static final MindmapIcon SIGN_HELP = new MindmapIcon(IconFamily.SIGN, "help");
    public static final MindmapIcon SIGN_CANCEL = new MindmapIcon(IconFamily.SIGN, "cancel");

    public static final MindmapIcon HARD_CD = new MindmapIcon(IconFamily.HARD, "cd");
    public static final MindmapIcon HARD_COMPUTER = new MindmapIcon(IconFamily.HARD, "computer");
    public static final MindmapIcon HARD_CONTROLLER = new MindmapIcon(IconFamily.HARD, "controller");
    public static final MindmapIcon HARD_DRIVER_DISK = new MindmapIcon(IconFamily.HARD, "driver_disk");
    public static final MindmapIcon HARD_IPOD = new MindmapIcon(IconFamily.HARD, "ipod");
    public static final MindmapIcon HARD_KEYBOARD = new MindmapIcon(IconFamily.HARD, "keyboard");
    public static final MindmapIcon HARD_MOUSE = new MindmapIcon(IconFamily.HARD, "mouse");
    public static final MindmapIcon HARD_PRINTER = new MindmapIcon(IconFamily.HARD, "printer");

    public static final MindmapIcon SOFT_BUG = new MindmapIcon(IconFamily.SOFT, "bug");
    public static final MindmapIcon SOFT_CURSOR = new MindmapIcon(IconFamily.SOFT, "cursor");
    public static final MindmapIcon SOFT_DATABASE_TABLE = new MindmapIcon(IconFamily.SOFT, "database_table");
    public static final MindmapIcon SOFT_DATABASE = new MindmapIcon(IconFamily.SOFT, "database");
    public static final MindmapIcon SOFT_FEED = new MindmapIcon(IconFamily.SOFT, "feed");
    public static final MindmapIcon SOFT_FOLDER_EXPORE = new MindmapIcon(IconFamily.SOFT, "folder_explore");
    public static final MindmapIcon SOFT_RSS = new MindmapIcon(IconFamily.SOFT, "rss");
    public static final MindmapIcon SOFT_PENGUIN = new MindmapIcon(IconFamily.SOFT, "penguin");

    public static final MindmapIcon PEOPLE_GROUP = new MindmapIcon(IconFamily.PEOPLE, "group");
    public static final MindmapIcon PEOPLE_MALE = new MindmapIcon(IconFamily.PEOPLE, "male1");
    public static final MindmapIcon PEOPLE_MALE2 = new MindmapIcon(IconFamily.PEOPLE, "male2");
    public static final MindmapIcon PEOPLE_FEMALE = new MindmapIcon(IconFamily.PEOPLE, "female1");
    public static final MindmapIcon PEOPLE_FEMALE2 = new MindmapIcon(IconFamily.PEOPLE, "female2");


    public static List<MindmapIcon> getIconByFamily(@NotNull IconFamily family) {

        load();
        return iconsByFamily.get(family);
    }

    private static void load() {
        try {
            if (iconsByFamily == null) {
                iconsByFamily = new TreeMap<IconFamily, List<MindmapIcon>>();

                Field[] fields = MindmapIcons.class.getDeclaredFields();
                for (Field field : fields) {
                    final Object object = field.get(null);

                    if (object instanceof MindmapIcon) {

                        final MindmapIcon icon = (MindmapIcon) object;
                        final IconFamily iconFamily = icon.getFamily();
                        List<MindmapIcon> mindmapIcons = iconsByFamily.get(iconFamily);
                        if (mindmapIcons == null) {
                            mindmapIcons = new ArrayList<MindmapIcon>();
                            iconsByFamily.put(iconFamily, mindmapIcons);
                        }
                        mindmapIcons.add(icon);
                    }
                }


            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MindmapIcon findById(final @NotNull String id) {
        for (IconFamily imageFamily : iconsByFamily.keySet()) {
            final List<MindmapIcon> mindmapIcons = iconsByFamily.get(imageFamily);
            for (MindmapIcon mindmapIcon : mindmapIcons) {
                if (mindmapIcon.getId().equals(id)) {
                    return mindmapIcon;
                }
            }
        }

        throw new IllegalArgumentException("Image could not be found. Id:" + id);
    }
}
