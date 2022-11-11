<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"    
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:output method="xml" version="1.0" indent="yes"/>
    
    <xsl:template match="akomaNtoso">
        <fo:root font-family="Arial">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21.0cm" margin="1cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simpleA4">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block>
                        <xsl:apply-templates select="doc/mainBody"/>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
    
    <xsl:template match="mainBody">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="p">
        <fo:block space-before="10pt" space-after="10pt">
            <fo:inline>
                <xsl:apply-templates />
            </fo:inline>
        </fo:block>
    </xsl:template>
    <xsl:template match="p[contains(@style,'center')]">
        <fo:block space-before="10pt" space-after="10pt" text-align="center">
            <fo:inline>
                <xsl:apply-templates />
            </fo:inline>
        </fo:block>
    </xsl:template>
    
    <xsl:template match="div">
        <fo:block>
            <xsl:apply-templates />
        </fo:block>
    </xsl:template>
    <xsl:template match="span[contains(@style,'display')]"> <!-- display: none -->
    </xsl:template>
    <xsl:template match="span">
        <fo:inline>
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    <xsl:template match="span[contains(.,'&lt;&lt;')]">  <!-- <<xyz>> -->
        <fo:inline color="gray">
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    
    <xsl:template match="br">
        <fo:block white-space="pre">
            <xsl:text disable-output-escaping="yes">&#10;</xsl:text>
        </fo:block>
    </xsl:template>
    <xsl:template match="h1">
        <fo:block font-size="180%" font-weight="bold">
            <xsl:apply-templates />
        </fo:block>
    </xsl:template>
    <xsl:template match="h2">
        <fo:block font-size="160%" font-weight="bold">
            <xsl:apply-templates />
        </fo:block>
    </xsl:template>
    <xsl:template match="h3">
        <fo:block font-size="140%" font-weight="bold">
            <xsl:apply-templates />
        </fo:block>
    </xsl:template>
</xsl:stylesheet>