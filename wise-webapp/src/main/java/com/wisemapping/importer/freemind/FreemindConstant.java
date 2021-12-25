package com.wisemapping.importer.freemind;

import com.wisemapping.importer.VersionNumber;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface FreemindConstant {

    String LAST_SUPPORTED_FREEMIND_VERSION = "1.0.1";
    VersionNumber SUPPORTED_FREEMIND_VERSION = new VersionNumber(LAST_SUPPORTED_FREEMIND_VERSION);
    String CODE_VERSION = "tango";

    int SECOND_LEVEL_TOPIC_HEIGHT = 25;
    int ROOT_LEVEL_TOPIC_HEIGHT = SECOND_LEVEL_TOPIC_HEIGHT;
    int CENTRAL_TO_TOPIC_DISTANCE = 200;
    int TOPIC_TO_TOPIC_DISTANCE = 90;

    int FONT_SIZE_HUGE = 15;
    int FONT_SIZE_LARGE = 10;
    int FONT_SIZE_NORMAL = 8;
    int FONT_SIZE_SMALL = 6;

    String NODE_TYPE = "NODE";
    String BOLD = "bold";
    String ITALIC = "italic";
    String EMPTY_FONT_STYLE = ";;;;;";
    String EMPTY_NOTE = "";

    String POSITION_LEFT = "left";
    String POSITION_RIGHT = "right";

    Charset UTF_8_CHARSET = StandardCharsets.UTF_8;

}
