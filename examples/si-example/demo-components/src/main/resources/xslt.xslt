<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8"
		indent="yes" />
	<xsl:template match="/">
		<xsl:element name="account">
			<xsl:element name="client">
				<xsl:element name="contrat-type">fix</xsl:element>
				<xsl:for-each select="client">
					<xsl:element name="name">
						<xsl:value-of select="name" />
					</xsl:element>
					<xsl:element name="address">
						<xsl:value-of select="address" />
					</xsl:element>
					<xsl:element name="city">
						<xsl:value-of select="city" />
					</xsl:element>
					<xsl:element name="department">
						<xsl:value-of select="department" />
					</xsl:element>
					<xsl:element name="dob">
						<xsl:value-of select="dob" />
					</xsl:element>
					<xsl:for-each select="fix">
						<xsl:element name="type">
							<xsl:value-of select="type" />
						</xsl:element>
						<xsl:element name="country-free">
							<xsl:value-of select="country-free" />
						</xsl:element>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:element>

			<xsl:element name="client">
				<xsl:element name="contrat-type">mobile</xsl:element>
				<xsl:for-each select="client">
					<xsl:element name="name">
						<xsl:value-of select="name" />
					</xsl:element>
					<xsl:element name="address">
						<xsl:value-of select="address" />
					</xsl:element>
					<xsl:element name="city">
						<xsl:value-of select="city" />
					</xsl:element>
					<xsl:element name="department">
						<xsl:value-of select="department" />
					</xsl:element>
					<xsl:element name="dob">
						<xsl:value-of select="dob" />
					</xsl:element>
					<xsl:for-each select="mobile">
						<xsl:element name="type">
							<xsl:value-of select="type" />
						</xsl:element>
						<xsl:element name="minutes">
							<xsl:value-of select="minutes" />
						</xsl:element>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:element>
			<xsl:element name="client">
				<xsl:element name="contrat-type">internet</xsl:element>
				<xsl:for-each select="client">
					<xsl:element name="name">
						<xsl:value-of select="name" />
					</xsl:element>
					<xsl:element name="address">
						<xsl:value-of select="address" />
					</xsl:element>
					<xsl:element name="city">
						<xsl:value-of select="city" />
					</xsl:element>
					<xsl:element name="department">
						<xsl:value-of select="department" />
					</xsl:element>
					<xsl:element name="dob">
						<xsl:value-of select="dob" />
					</xsl:element>
					<xsl:for-each select="internet">
						<xsl:element name="type">
							<xsl:value-of select="type" />
						</xsl:element>
						<xsl:element name="bandwidth">
							<xsl:value-of select="bandwidth" />
						</xsl:element>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>