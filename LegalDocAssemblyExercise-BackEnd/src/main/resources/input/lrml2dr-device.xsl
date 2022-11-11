<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.ruleml.org/0.91/xsd"
	xmlns:lrml="http://docs.oasis-open.org/legalruleml/ns/v1.0/"
    xmlns:ruleml="http://ruleml.org/spec"
	xmlns:lc="http://ftn.uns.ac.rs/legal-case"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    
    <xsl:template match="/">
        <xsl:apply-templates select="lrml:LegalRuleML"/>
    </xsl:template>
    <xsl:template match="lrml:LegalRuleML">
        <RuleML>
            <xsl:attribute name="proof">proof.ruleml</xsl:attribute>
            <xsl:attribute name="rdf_export">export.rdf</xsl:attribute>
            <xsl:attribute name="rdf_export_classes">guilty_for_murder max_imprisonment</xsl:attribute>
            <xsl:attribute name="rdf_import">&quot;facts.rdf&quot;</xsl:attribute>
            <xsl:apply-templates select="lrml:Statements"/>
        </RuleML>
    </xsl:template>

    <xsl:template match="lrml:Statements">
        <Assert>
            <xsl:apply-templates select="lrml:PrescriptiveStatement"/>
            <xsl:apply-templates select="lrml:ReparationStatement"/>
        </Assert>
    </xsl:template>

    <xsl:template match="lrml:PrescriptiveStatement">
        <Implies>
            <xsl:attribute name="ruletype">
                <xsl:choose>
                    <xsl:when test="ruleml:Rule[@strength = 'defeasible']">defeasiblerule</xsl:when>
                    <xsl:otherwise>strictrule</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates select="ruleml:Rule"/>
            <xsl:call-template name="superiority"/>
        </Implies>
    </xsl:template>
    
    <xsl:template match="ruleml:Rule">
        <oid>
            <Ind>
                <xsl:variable name="uri" select="replace(@key,':','')"/>
                <xsl:attribute name="uri"><xsl:value-of select="$uri"/></xsl:attribute>
                <xsl:value-of select="$uri"/>
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
    <xsl:template match="ruleml:then[ruleml:Negation]">
        <head>
            <Neg>
                <xsl:apply-templates select="ruleml:Negation/ruleml:Atom"/>
            </Neg>
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
            <Expr>
                <xsl:apply-templates select="ruleml:Expr/ruleml:Fun"/>
                <xsl:apply-templates select="ruleml:Expr/ruleml:Var"/>
                <xsl:apply-templates select="ruleml:Expr/ruleml:Ind"/>
            </Expr>
        </Equal>
    </xsl:template>
    <!-- atoms related to the case facts of enum type -->
    <xsl:template match="ruleml:Atom[ruleml:Rel[@iri] and ruleml:Ind]">
        <Atom>
            <op><Rel uri="lc:case"/></op>
            <xsl:for-each select="ruleml:Var">
                <slot>
                    <Ind>
                        <xsl:attribute name="uri" select="concat('lc:',lower-case(.))"/>
                    </Ind>
                    <xsl:apply-templates select="."/>
                </slot>
            </xsl:for-each>
            <xsl:for-each select="ruleml:Ind">
                <slot>
                    <Ind>
                        <xsl:attribute name="uri" select="concat('lc:',lower-case(../ruleml:Rel/@iri))"/>
                    </Ind>
                    <Data><xsl:value-of select="."/>
                    </Data>
                </slot>
            </xsl:for-each>
        </Atom>
    </xsl:template>
    <!-- atoms related to the case facts -->
    <xsl:template match="ruleml:Atom[ruleml:Rel[@iri] and not(ruleml:Ind)]">
        <Atom>
            <op><Rel uri="lc:case"/></op>
            <xsl:for-each select="ruleml:Var">
                <slot>
                    <Ind>
                        <xsl:attribute name="uri" select="concat('lc:',lower-case(.))"/>
                    </Ind>
                    <xsl:apply-templates select="."/>
                </slot>
            </xsl:for-each>
            <xsl:if test="ruleml:Data">
                <slot>
                    <Ind>
                        <xsl:attribute name="uri" select="ruleml:Rel/@iri"/>
                    </Ind>
                    <Data>
                        <xsl:attribute name="xsi:type" select="ruleml:Data/@xsi:type"/>
                        <xsl:value-of select="ruleml:Data"/>
                    </Data>
                </slot>
            </xsl:if>
        </Atom>
    </xsl:template>
    <!-- atoms not related to the case facts (inferred facts) -->
    <xsl:template match="ruleml:Atom[ruleml:Rel[not(@iri)]]">
        <Atom>
            <xsl:apply-templates select="ruleml:Rel"/>
            <xsl:for-each select="ruleml:Var">
                <slot>
                    <Ind>
                        <xsl:attribute name="uri" select="lower-case(.)"/>
                    </Ind>
                    <xsl:apply-templates select="."/>
                </slot>
            </xsl:for-each>
            <xsl:for-each select="ruleml:Ind">
                <slot>
                    <Ind>
                        <xsl:attribute name="uri" select="@type"/>
                    </Ind>
                    <Data><xsl:value-of select="."/></Data>
                </slot>
            </xsl:for-each>
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
    <xsl:template match="ruleml:Var">
        <Var>
            <xsl:value-of select="."/>
        </Var>
    </xsl:template>
    <xsl:template match="ruleml:Fun">
        <Fun in="yes"><xsl:value-of select="."/></Fun>
    </xsl:template>
    
    <!-- superiority relations -->
    
    <xsl:template name="superiority">
        <xsl:variable name="currentPrescriptiveStatementKey" select="@key"/>
        <xsl:for-each select="//lrml:Override[@over=concat('#',$currentPrescriptiveStatementKey)]">
            <xsl:variable name="targetPrescriptiveStatementKey" select="replace(@under,'^(#)','')"/>
            <xsl:variable name="targetRuleKey" select="//lrml:PrescriptiveStatement[@key=$targetPrescriptiveStatementKey]/ruleml:Rule/@key"/>
            <superior>
                <Ind>
                    <xsl:attribute name="uri" select="replace($targetRuleKey,':','')"/>
                </Ind>
            </superior>
        </xsl:for-each>
    </xsl:template>
    
    <!-- legal norms' sanction -->
    
    <xsl:template match="lrml:ReparationStatement">
        <xsl:apply-templates select="lrml:Reparation"/>
    </xsl:template>
    
    <xsl:template match="lrml:Reparation">
        <xsl:variable name="penaltyKey" select="replace(lrml:appliesPenalty/@keyref,'^(#)','')"/>
        <xsl:for-each select="lrml:toPrescriptiveStatement">
            <xsl:variable name="targetPrescriptiveStatementKey" select="replace(@keyref,'^(#)','')"/>
            <Implies ruletype="defeasiblerule">
                <oid>
                    <Ind>
                        <xsl:attribute name="uri" select="$penaltyKey"/>
                        <xsl:value-of select="$penaltyKey"/>
                    </Ind>
                </oid>
                <body>
                    <Atom>
                        <op>
                            <Rel><xsl:value-of select="//lrml:PrescriptiveStatement[@key=$targetPrescriptiveStatementKey]//ruleml:then//ruleml:Rel"/></Rel>
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
                            <Ind uri="value"/>
                            <Data xsi:type="xs:integer"><xsl:value-of select="//lrml:PenaltyStatement[@key=$penaltyKey]//ruleml:Ind"/></Data>
                        </slot>
                    </Atom>
                </head>
            </Implies>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>