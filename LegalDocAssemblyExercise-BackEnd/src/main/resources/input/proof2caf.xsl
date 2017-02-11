<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ruleml="http://www.ruleml.org/0.91/xsd">
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:template match="/">
		<xsl:apply-templates select="ruleml:RuleML"/>
	</xsl:template>
	<xsl:template match="ruleml:RuleML">
		<caf>
			<metadata/>
			<statements>
				<xsl:attribute name="id">proof.ruleml</xsl:attribute>
				<xsl:apply-templates select="//*[@rule]" mode="statement"/>
			</statements>
			<arguments>
				<xsl:apply-templates select="//*[@rule]" mode="argument"/>
			</arguments>
		</caf>
	</xsl:template>

	<xsl:template match="//*[@rule]" mode="statement">
		<statement>
			<xsl:attribute name="id"><xsl:value-of select="@rule"/></xsl:attribute>
			<metadata>
			</metadata>
		</statement>
	</xsl:template>

	<xsl:template match="//*[@rule]" mode="argument">
		<xsl:variable name="currentDepth" select="count(ancestor::*/*/*[@rule])"/>
		<xsl:if test="../../*/*//*[@rule][count(ancestor::*/*/*[@rule])=$currentDepth+1]">
			<argument>
				<conclusion>
					<xsl:attribute name="statement"><xsl:value-of select="@rule"/></xsl:attribute>
				</conclusion>
				<premises>
					<xsl:for-each select="../../*/*//*[@rule][count(ancestor::*/*/*[@rule])=$currentDepth+1]">
						<premise>
							<xsl:attribute name="statement"><xsl:value-of select="@rule"/></xsl:attribute>
						</premise>
					</xsl:for-each>
				</premises>
			</argument>
		</xsl:if>
	</xsl:template>
<!--
	<xsl:template match="../../*/*//*[@rule][count(ancestor::*/*/*[@rule])=$currentDepth+1]" mode="premise">
		<premise>
			<xsl:attribute name="statement"><xsl:value-of select="@rule"/></xsl:attribute>
		</premise>
	</xsl:template>
-->

</xsl:stylesheet>
