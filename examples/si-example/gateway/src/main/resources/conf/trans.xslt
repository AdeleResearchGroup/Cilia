<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="/">
        <requete>
            <xsl:call-template name="output-tokens">
                <xsl:with-param name="list">
                    <xsl:value-of select="//suiviconso-requete/produit"/>
                </xsl:with-param>
            </xsl:call-template>
        </requete>
    </xsl:template>

    <xsl:template name="output-tokens">

        <xsl:param name="list"/>
        <xsl:variable name="first" select="substring-before($list, ',')"/>
        <xsl:variable name="remaining" select="substring-after($list, ',')"/>

        <xsl:choose>
            <xsl:when test="$remaining">
                <suiviconso-requete produit="{$first}">
                    <xsl:element name="idclient">
                        <xsl:value-of select="//suiviconso-requete/idclient"/>
                    </xsl:element>
                    <xsl:element name="debut">
                        <xsl:value-of select="//suiviconso-requete/debut"/>
                    </xsl:element>
                    <xsl:element name="fin">
                        <xsl:value-of select="//suiviconso-requete/fin"/>
                    </xsl:element>
                </suiviconso-requete>
                <xsl:call-template name="output-tokens">
                    <xsl:with-param name="list" select="$remaining"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>

                <suiviconso-requete produit="{$list}">
                    <xsl:element name="idclient">
                        <xsl:value-of select="//suiviconso-requete/idclient"/>
                    </xsl:element>
                    <xsl:element name="debut">
                        <xsl:value-of select="//suiviconso-requete/debut"/>
                    </xsl:element>
                    <xsl:element name="fin">
                        <xsl:value-of select="//suiviconso-requete/fin"/>
                    </xsl:element>
                </suiviconso-requete>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet> 

