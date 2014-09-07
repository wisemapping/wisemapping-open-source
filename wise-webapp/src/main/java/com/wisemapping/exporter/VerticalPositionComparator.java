package com.wisemapping.exporter;

import com.wisemapping.jaxb.wisemap.TopicType;

import java.util.Comparator;

public class VerticalPositionComparator implements Comparator<TopicType> {

    @Override
    public int compare(TopicType o1, TopicType o2) {
        final String myPosition = o1.getPosition();
        final String otherPosition = o2.getPosition();
        int result;
        if (otherPosition == null) {
            result = -1;
        } else if (myPosition == null) {
            result = 1;
        } else {
            int myPositionY = Integer.parseInt(myPosition.split(",")[1]);
            int otherPositionY = Integer.parseInt(otherPosition.split(",")[1]);
            result = myPositionY - otherPositionY;
        }
        return result;
    }

}
