# Algoritmus generovani kodu pro slouceni zaznamu. #

Pokud existuje cCNB generuje systém MD5 z jeho hodnoty.

Není-li pole cCNB vyplněné generuje systém MD5 z normalizovaných poli title, autor, mistovydani, datumvydani






&lt;xsl:variable name="title" select="concat(marc:datafield[@tag=245]/marc:subfield[@code='a'],marc:datafield[@tag=245]/marc:subfield[@code='b'])"/&gt;





&lt;xsl:variable name="autor" select="marc:datafield[@tag=700]/marc:subfield[@code='a']"/&gt;





&lt;xsl:variable name="mistovydani" select="marc:datafield[@tag=260]/marc:subfield[@code='a']"/&gt;





&lt;xsl:variable name="datumvydani" select="marc:datafield[@tag=260]/marc:subfield[@code='c']"/&gt;






&lt;xsl:variable name="md5" select="exts:generateNormalizedMD5($xslfunctions, concat($title, $autor, $mistovydani, $datumvydani))"/&gt;
