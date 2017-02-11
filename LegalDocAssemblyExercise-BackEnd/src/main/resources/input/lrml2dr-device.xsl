<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns="http://www.ruleml.org/0.91/xsd" xmlns:test="http://www.ruleml.org/0.91/xsd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:ruleml="http://ruleml.org/spec" xmlns:lrml="http://docs.oasis-open.org/legalruleml/ns/v1.0/" xmlns:dd="http://ftn.uns.ac.rs/legal/document-drafting" exclude-result-prefixes="xs fn fo lrml">
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates select="lrml:LegalRuleML"/>
	</xsl:template>
	<xsl:template match="lrml:LegalRuleML">
		<RuleML>
			<xsl:attribute name="proof">proof.ruleml</xsl:attribute>
			<xsl:attribute name="rdf_export">export.rdf</xsl:attribute>
			<xsl:attribute name="rdf_export_classes">committed_art246para1</xsl:attribute>
			<xsl:attribute name="rdf_import">&quot;facts.rdf&quot;</xsl:attribute>
			<xsl:apply-templates select="lrml:Statements"/>
		</RuleML>
	</xsl:template>
	<xsl:template match="lrml:Statements">
		<xsl:apply-templates select="lrml:PrescriptiveStatement"/>
		<xsl:apply-templates select="lrml:ReparationStatement"/>
	</xsl:template>
	<xsl:template match="lrml:PrescriptiveStatement">
		<Assert>
			<Implies>
				<xsl:attribute name="ruletype"><xsl:choose><xsl:when test="ruleml:Rule[@strength = 'defeasible']">defeasiblerule</xsl:when><xsl:otherwise>strictrule</xsl:otherwise></xsl:choose></xsl:attribute>
				<xsl:apply-templates select="ruleml:Rule"/>
			</Implies>
		</Assert>
	</xsl:template>
	<xsl:template match="ruleml:Rule">
		<oid>
			<Ind>
				<xsl:attribute name="uri"><xsl:value-of select="replace(@key,'^(:)','')"/></xsl:attribute>
				<xsl:value-of select="replace(@key,'^(:)','')"/>
			</Ind>
		</oid>
		<xsl:apply-templates select="ruleml:if"/>
		<xsl:apply-templates select="ruleml:then"/>
	</xsl:template>
	<xsl:template match="ruleml:if">
		<body>
			<xsl:apply-templates select="ruleml:And|ruleml:Or|ruleml:Atom"/>
		</body>
	</xsl:template>
	<xsl:template match="ruleml:then">
		<head>
			<xsl:apply-templates select="ruleml:Atom"/>
		</head>
	</xsl:template>
	<xsl:template match="ruleml:And">
		<And>
			<xsl:apply-templates select="ruleml:Atom|ruleml:And|ruleml:Or"/>
		</And>
	</xsl:template>
	<xsl:template match="ruleml:Or">
		<Or>
			<xsl:apply-templates select="ruleml:Atom|ruleml:And|ruleml:Or"/>
		</Or>
	</xsl:template>
	<xsl:template match="ruleml:Atom[ruleml:Expr]">
		<Equal>
			<xsl:apply-templates select="ruleml:Rel"/>
			<Expr>
				<xsl:apply-templates select="ruleml:Expr/ruleml:Fun"/>
				<xsl:apply-templates select="ruleml:Expr/ruleml:Var"/>
				<xsl:apply-templates select="ruleml:Expr/ruleml:Ind"/>
			</Expr>
		</Equal>
	</xsl:template>
	<xsl:template match="ruleml:Atom[ruleml:Rel[@iri] and ruleml:Data]">
		<Atom>
			<op>
				<Rel uri="dd:case"/>
			</op>
			<xsl:apply-templates select="ruleml:Var"/>
			<slot>
				<Ind><xsl:value-of select="replace(ruleml:Rel/@iri,'^(:)','')"/></Ind>
				<xsl:apply-templates select="ruleml:Data"/>
			</slot>
		</Atom>
	</xsl:template>
	<xsl:template match="ruleml:Atom[ruleml:Rel[@iri] and ruleml:Var and not(ruleml:Data)]">
		<Atom>
			<op>
				<Rel uri="dd:case"/>
			</op>
			<xsl:apply-templates select="ruleml:Var"/> <!-- mode="slot-rdfProperty"/> -->
		</Atom>
	</xsl:template>
	<!--
	<xsl:template match="ruleml:Var" mode="slot-rdfProperty">
			<slot>
				<Ind>
					<xsl:attribute name="uri"><xsl:value-of select="@type"/></xsl:attribute>
				</Ind>
				<Var><xsl:value-of select="."/></Var>
			</slot>
	</xsl:template>
-->
	<xsl:template match="ruleml:Atom">
		<Atom>
			<xsl:apply-templates select="ruleml:Rel"/>
<!--			<slot> -->
				<xsl:apply-templates select="ruleml:Var"/>
<!--				<xsl:apply-templates select="ruleml:Ind"/> -->
<!--			</slot> -->
		</Atom>
	</xsl:template>
	<xsl:template match="ruleml:Rel">
		<op>
			<Rel><xsl:value-of select="."/></Rel>
		</op>
	</xsl:template>
	<xsl:template match="ruleml:Ind">
		<Ind><xsl:value-of select="."/></Ind>
	</xsl:template>
	<xsl:template match="ruleml:Var[@type]">
		<slot>
			<Ind>
				<xsl:attribute name="uri"><xsl:value-of select="replace(@type,'^(:)','')"/></xsl:attribute>
			</Ind>
			<Var><xsl:value-of select="."/></Var>
		</slot>
	</xsl:template>
	<xsl:template match="ruleml:Var">
		<Var>
			<xsl:value-of select="."/>
		</Var>
	</xsl:template>
	<xsl:template match="ruleml:Fun">
		<Fun in="yes"><xsl:value-of select="."/></Fun>
	</xsl:template>
	<xsl:template match="ruleml:Data">
		<Data>
			<xsl:attribute name="xsi:type"><xsl:value-of select="@xsi:type"/></xsl:attribute>
			<xsl:value-of select="."/>
		</Data>
	</xsl:template>
	
	<xsl:template match="lrml:ReparationStatement">
		<xsl:apply-templates select="lrml:Reparation"/>
	</xsl:template>

	<xsl:template match="lrml:Reparation">
		<xsl:variable name="penaltyKey" select="replace(current()/lrml:appliesPenalty/@keyref,'^(#)','')"/>
		<xsl:for-each select="current()/lrml:toPrescriptiveStatement">
			<Implies ruletype="defeasiblerule">
				<oid>
					<Ind>
						<xsl:attribute name="uri"><xsl:value-of select="$penaltyKey"/></xsl:attribute>
						<xsl:value-of select="$penaltyKey"/>
					</Ind>
				 </oid>
					<body>
						<Atom>
							<op>
								<Rel><xsl:value-of select="//lrml:PrescriptiveStatement[@key=replace(current()/@keyref,'^(#)','')]//ruleml:then//ruleml:Rel"/></Rel>
							</op>
							<slot>
								<Ind uri="defendant"/>
								<Var>Defendant</Var>
							</slot>
						</Atom>
					</body>
					<head>
						<Atom>
							<op>
								<Rel><xsl:value-of select="replace(//lrml:PenaltyStatement[@key=$penaltyKey]//ruleml:Rel/@iri,'^(:)','')"/></Rel>
							</op>
							<slot>
								<Ind uri="years"/>
								<Data xsi:type="xs:integer"><xsl:value-of select="//lrml:PenaltyStatement[@key=$penaltyKey]//ruleml:Ind"/></Data>
							</slot>
						</Atom>
					</head>
				</Implies>
		</xsl:for-each>

	</xsl:template>
</xsl:stylesheet>
