<?xml version="1.0" encoding="UTF-8" ?>

<!--
        : This code released under the GPL.
        : (http://www.gnu.org/copyleft/gpl.html)
    Document   : mm2text.xsl
    Created on : 01 February 2004, 17:17
    Author     : joerg feuerhake joerg.feuerhake@free-penguin.org
    Description: transforms freemind mm format to html, handles crossrefs and adds numbering. feel free to customize it while leaving the ancient authors
                    mentioned. thank you
    ChangeLog:
    
    See: http://freemind.sourceforge.net/
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" indent="no" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>
    <xsl:key name="refid" match="node" use="@ID"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="node">
        <xsl:call-template name="indent">
            <xsl:with-param name="string" select="'&#009;'"/>
            <xsl:with-param name="times" select="count(ancestor::node())-2"/>
        </xsl:call-template>

        <xsl:variable name="target" select="arrowlink/@DESTINATION"/>
        <xsl:number level="multiple" count="node" format="1"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@TEXT"/>

        <!-- Generate Link -->
        <xsl:if test="@LINK">
            <xsl:text>&#10;</xsl:text>
            <xsl:call-template name="indent">
                <xsl:with-param name="string" select="'&#009;'"/>
                <xsl:with-param name="times" select="count(ancestor::node())-1"/>
            </xsl:call-template>
            <xsl:text>* Link: </xsl:text>
            <xsl:value-of select="@LINK"/>
        </xsl:if>

        <!-- Generate Note -->
        <xsl:apply-templates select="richcontent[@TYPE='NOTE']"/>
        <xsl:text>&#10;</xsl:text>

        <!-- Generate References -->
        <xsl:if test="arrowlink/@DESTINATION != ''">
            <xsl:text> (see:</xsl:text>
            <xsl:for-each select="key('refid', $target)">
                <xsl:value-of select="@TEXT"/>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
        </xsl:if>
        <xsl:apply-templates select="node"/>

    </xsl:template>

    <xsl:template match="richcontent[@TYPE='NOTE']">
        <xsl:text>&#10;</xsl:text>
        <xsl:call-template name="indent">
            <xsl:with-param name="string" select="'&#009;'"/>
            <xsl:with-param name="times" select="count(ancestor::node())-2"/>
        </xsl:call-template>
        <xsl:text>* Note: </xsl:text>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template name="indent">
        <xsl:param name="string" select="''"/>
        <xsl:param name="times" select="1"/>

        <xsl:if test="number($times) &gt; 0">
            <xsl:value-of select="$string"/>
            <xsl:call-template name="indent">
                <xsl:with-param name="string" select="$string"/>
                <xsl:with-param name="times" select="$times - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet> 
