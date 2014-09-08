package com.wisemapping.importer.freemind;

import com.wisemapping.importer.VersionNumber;

import java.nio.charset.Charset;

public interface FreemindConstant {

    public static final String LAST_SUPPORTED_FREEMIND_VERSION = "1.0.1";
    public static final VersionNumber SUPPORTED_FREEMIND_VERSION = new VersionNumber(LAST_SUPPORTED_FREEMIND_VERSION);
    public static final String CODE_VERSION = "tango";

    public static final int SECOND_LEVEL_TOPIC_HEIGHT = 25;
    public static final int ROOT_LEVEL_TOPIC_HEIGHT = SECOND_LEVEL_TOPIC_HEIGHT;
    public static final int CENTRAL_TO_TOPIC_DISTANCE = 200;
    public static final int TOPIC_TO_TOPIC_DISTANCE = 90;

    public static final int FONT_SIZE_HUGE = 15;
    public static final int FONT_SIZE_LARGE = 10;
    public static final int FONT_SIZE_NORMAL = 8;
    public static final int FONT_SIZE_SMALL = 6;

    public static final String NODE_TYPE = "NODE";
    public static final String BOLD = "bold";
    public static final String ITALIC = "italic";
    public static final String EMPTY_FONT_STYLE = ";;;;;";
    public static final String EMPTY_NOTE = "";

    public static final String POSITION_LEFT = "left";
    public static final String POSITION_RIGHT = "right";

    public final static Charset UTF_8_CHARSET = Charset.forName("UTF-8");

}
