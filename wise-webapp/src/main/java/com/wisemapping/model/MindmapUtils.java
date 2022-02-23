package com.wisemapping.model;


import com.wisemapping.exceptions.InvalidMindmapException;
import org.jetbrains.annotations.Nullable;

abstract public class MindmapUtils {

    private static final int MAX_SUPPORTED_NODES = 500;

    public static void verifyMindmap(@Nullable String xmlDoc) throws InvalidMindmapException {
        if (xmlDoc == null || xmlDoc.trim().isEmpty()) {
            // Perform basic structure validation. Must have a map node and
            throw InvalidMindmapException.emptyMindmap();
        }

        // Perform basic structure validation without parsing the XML.
        if (!xmlDoc.trim().endsWith("</map>") || !xmlDoc.trim().startsWith("<map")) {
            throw InvalidMindmapException.invalidFormat(xmlDoc);
        }

        // Validate that the number of nodes is not bigger 500 nodes.
        if (xmlDoc.split("<topic").length > MAX_SUPPORTED_NODES) {
            throw InvalidMindmapException.tooBigMindnap();
        }
    }
}
