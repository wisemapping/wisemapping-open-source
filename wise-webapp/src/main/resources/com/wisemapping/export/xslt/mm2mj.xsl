<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!--
# Simple XSL for the conversion of Mindmaps from Freemind-0.9 to MindManager-8
#
# Version-1.1
#
# Copyright (c) 2009 Christian Lorandi
# http://freemind2mindmanager.fdns.net
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:ap="http://schemas.mindjet.com/MindManager/Application/2003"
                xmlns:cor="http://schemas.mindjet.com/MindManager/Core/2003"
                xmlns:pri="http://schemas.mindjet.com/MindManager/Primitive/2003"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://schemas.mindjet.com/MindManager/Application/2003 http://schemas.mindjet.com/MindManager/Application/2003 http://schemas.mindjet.com/MindManager/Core/2003 http://schemas.mindjet.com/MindManager/Core/2003 http://schemas.mindjet.com/MindManager/Delta/2003 http://schemas.mindjet.com/MindManager/Delta/2003 http://schemas.mindjet.com/MindManager/Primitive/2003 http://schemas.mindjet.com/MindManager/Primitive/2003">
    <xsl:template match="map">
        <xsl:element name="ap:Map">
            <xsl:element name="ap:OneTopic">
                <xsl:apply-templates select="node"/>
            </xsl:element>
            <xsl:element name="ap:Relationships">
                <xsl:apply-templates select="descendant-or-self::arrowlink"></xsl:apply-templates>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="node">
        <xsl:element name="ap:Topic">
            <xsl:attribute name="OId">
                <xsl:value-of
                        select="concat(substring-after(@ID, '_'), substring('rW54nezC90m8NYAi2fjQvw==', string-length(substring-after(@ID, '_'))+1))"
                        />
            </xsl:attribute>
            <xsl:if test="node">
                <xsl:element name="ap:SubTopics">
                    <xsl:apply-templates select="node"/>
                </xsl:element>
            </xsl:if>
            <xsl:apply-templates select="cloud"/>
            <xsl:apply-templates select="richcontent//body//img"/>
            <xsl:element name="ap:Text">
                <xsl:attribute name="PlainText">
                    <xsl:value-of select="@TEXT"/>
                </xsl:attribute>
                <xsl:element name="ap:Font">
                    <xsl:if test="@COLOR">
                        <xsl:attribute name="Color">
                            <xsl:value-of select="concat('ff', substring-after(@COLOR, '#'))"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:apply-templates select="font"/>
                </xsl:element>
            </xsl:element>
            <xsl:if test="not(contains(icon/@BUILTIN, 'full-'))">
                <xsl:apply-templates select="icon"/>
            </xsl:if>
            <xsl:if test="@BACKGROUND_COLOR">
                <xsl:element name="ap:Color">
                    <xsl:attribute name="FillColor">
                        <xsl:value-of select="concat('ff', substring-after(@BACKGROUND_COLOR, '#'))"
                                />
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>

            <xsl:if test="@STYLE">
                <xsl:element name="ap:SubTopicShape">
                    <xsl:if test="@STYLE = 'bubble'">
                        <xsl:attribute name="SubTopicShape"
                                >urn:mindjet:RoundedRectangle
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="@STYLE = 'fork'">
                        <xsl:attribute name="SubTopicShape">urn:mindjet:Line</xsl:attribute>
                    </xsl:if>
                </xsl:element>
            </xsl:if>

            <xsl:if test="not(@STYLE)">
                <xsl:element name="ap:SubTopicShape">
                    <xsl:if test="parent::map">
                        <xsl:attribute name="SubTopicShape"
                                >urn:mindjet:RoundedRectangle
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="not(parent::map)">
                        <xsl:attribute name="SubTopicShape">urn:mindjet:Line</xsl:attribute>
                    </xsl:if>
                    <xsl:for-each select="ancestor::node">
                        <xsl:if test="@STYLE = 'bubble'">
                            <xsl:attribute name="SubTopicShape"
                                    >urn:mindjet:RoundedRectangle
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@STYLE = 'fork'">
                            <xsl:attribute name="SubTopicShape">urn:mindjet:Line</xsl:attribute>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:element>
            </xsl:if>

            <xsl:if test="child::edge/@STYLE|parent::map">
                <xsl:element name="ap:SubTopicsShape">
                    <xsl:if test="contains(child::edge/@STYLE, 'bezier')">
                        <xsl:attribute name="SubTopicsConnectionStyle"
                                >urn:mindjet:Curve
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="contains(child::edge/@STYLE, 'linear')">
                        <xsl:attribute name="SubTopicsConnectionStyle"
                                >urn:mindjet:Straight
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="parent::map">
                        <xsl:attribute name="SubTopicsAlignment">urn:mindjet:Center</xsl:attribute>
                        <xsl:attribute name="SubTopicsGrowth">urn:mindjet:Horizontal</xsl:attribute>
                        <xsl:attribute name="SubTopicsGrowthDirection"
                                >urn:mindjet:AutomaticHorizontal
                        </xsl:attribute>
                        <xsl:attribute name="VerticalDistanceBetweenSiblings">150</xsl:attribute>
                    </xsl:if>
                </xsl:element>
            </xsl:if>
            <xsl:if test="@LINK">
                <xsl:element name="ap:Hyperlink">
                    <xsl:attribute name="Url">
                        <xsl:value-of select="@LINK"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>

            <xsl:if test="contains(icon/@BUILTIN, 'full-')">
                <xsl:apply-templates select="icon"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="font">
        <xsl:if test="@BOLD">
            <xsl:attribute name="Bold">
                <xsl:value-of select="@BOLD"/>
            </xsl:attribute>
        </xsl:if>
        <xsl:if test="@ITALIC">
            <xsl:attribute name="Italic">
                <xsl:value-of select="@ITALIC"/>
            </xsl:attribute>
        </xsl:if>
        <xsl:if test="@SIZE">
            <xsl:attribute name="Size">
                <xsl:value-of select="@SIZE"/>
            </xsl:attribute>
        </xsl:if>
        <xsl:if test="@NAME">
            <xsl:attribute name="Name">
                <xsl:value-of select="@NAME"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template match="richcontent//body//img">
        <xsl:element name="ap:OneImage">
            <xsl:element name="ap:Image">
                <xsl:element name="ap:ImageData">
                    <xsl:attribute name="ImageType">urn:mindjet:PngImage</xsl:attribute>
                    <xsl:attribute name="CustomImageType"></xsl:attribute>
                    <xsl:element name="cor:Uri">
                        <xsl:attribute name="xsi:nil">false</xsl:attribute>
                        <xsl:text>mmarch://bin/</xsl:text>
                        <xsl:value-of select="@src"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="arrowlink">
        <xsl:element name="ap:Relationship">
            <xsl:element name="ap:ConnectionGroup">
                <xsl:attribute name="Index">0</xsl:attribute>
                <xsl:element name="ap:Connection">
                    <xsl:element name="ap:ObjectReference">
                        <xsl:attribute name="OIdRef">
                            <xsl:value-of
                                    select="concat(substring-after(parent::node/@ID, '_'), substring('rW54nezC90m8NYAi2fjQvw==', string-length(substring-after(parent::node/@ID, '_'))+1))"
                                    />
                        </xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
            <xsl:element name="ap:ConnectionGroup">
                <xsl:attribute name="Index">1</xsl:attribute>
                <xsl:element name="ap:Connection">
                    <xsl:element name="ap:ObjectReference">
                        <xsl:attribute name="OIdRef">
                            <xsl:value-of
                                    select="concat(substring-after(@DESTINATION, '_'), substring('rW54nezC90m8NYAi2fjQvw==', string-length(substring-after(@DESTINATION, '_'))+1))"
                                    />
                        </xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
            <xsl:element name="ap:AutoRoute">
                <xsl:attribute name="AutoRouting">true</xsl:attribute>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="cloud">
        <xsl:element name="ap:OneBoundary">
            <xsl:element name="ap:Boundary">
                <xsl:if test="@COLOR">
                    <xsl:element name="ap:Color">
                        <xsl:attribute name="FillColor">
                            <xsl:value-of select="concat('2e', substring-after(@COLOR, '#'))"/>
                        </xsl:attribute>
                        <xsl:attribute name="LineColor">
                            <xsl:value-of select="concat('ff', substring-after(@COLOR, '#'))"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="ap:BoundaryShape">
                    <xsl:attribute name="BoundaryShape">urn:mindjet:CurvedLine</xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="icon">
        <xsl:if test="number(substring-after(@BUILTIN, 'full-')) &lt; 6">
            <xsl:if test="contains(@BUILTIN, 'full-')">
                <xsl:element name="ap:Task">
                    <xsl:attribute name="TaskPriority">urn:mindjet:Prio<xsl:value-of
                            select="substring-after(@BUILTIN, 'full-')"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'help'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:QuestionMark</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'yes'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ExclamationMark</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'messagebox_warning'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:Emergency</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'button_ok'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ThumbsUp</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'button_cancel'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ThumbsDown</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'calendar'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:Calendar</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'up'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ArrowUp</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'down'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ArrowDown</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'forward'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ArrowRight</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'back'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:ArrowLeft</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag-black'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagBlack</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag-green'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagGreen</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagRed</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag-yellow'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagYellow</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag-orange'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagOrange</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag-pink'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagPurple</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'flag-blue'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:FlagBlue</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>

        <xsl:if test="@BUILTIN = 'ksmiletris'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:SmileyHappy</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'smiley-neutral'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:SmileyNeutral</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'smiley-angry'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:SmileyAngry</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@BUILTIN = 'smily_bad'">
            <xsl:element name="ap:IconsGroup">
                <xsl:element name="ap:Icons">
                    <xsl:element name="ap:Icon">
                        <xsl:attribute name="xsi:type">ap:StockIcon</xsl:attribute>
                        <xsl:attribute name="IconType">urn:mindjet:SmileySad</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
