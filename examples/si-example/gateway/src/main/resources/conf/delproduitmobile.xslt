<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="/">
        <xsl:element name="SOAP-ENV:suiviconso-requete" namespace="http://www.example.org/SuiviConsoMobile/">
            <xsl:element name="idclient">
                <xsl:value-of select="//suiviconso-requete/idclient"/>
            </xsl:element>
            <xsl:element name="debut">
                <xsl:value-of select="//suiviconso-requete/debut"/>
            </xsl:element>
            <xsl:element name="fin">
                <xsl:value-of select="//suiviconso-requete/fin"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet> 
