#mapování polí - sestavil RZ.

# Mapování polí #

formát záznamu (FMT) - 990

jednotky       (Z30) - 996

podpole:
  * $b - čárový kód
  * $C - signatura
  * $d - popis     (z30-description)
  * $v - roč./sv.  (z30-enumeration-a)
  * $i - číslo     (z30-enumeration-b)
  * $y - rok       (z30-chronological-i)


  * $l - dílčí knih.
  * $r - sbírka
  * $s - status jednotky
  * $n - počet výpůjček    (z30-no-loans)
  * $p - pozn. o xerokopii


# tab\_base.lng: #

NKC-VDK                NKC/VDK              NKC01             NKC01 N

wbs=(01 or 11 or 02) not wtp=(AM or VM or ER)

(pouze veřejné záznamy, ne el. zdroje, audio a videozáznamy)


# tab\_publish #

NKC-VDKM             NKC-VDK              N OAIVM OAI\_MARC21\_XML


# tab\_fix #

! OAI pro VDK
OAIVM expand\_doc\_del\_fields
> LDR,FMT,001,008,015##,020##,022##,0242#,1####,24###,250##,260##,300##,490##,700##

OAIVM expand\_doc\_bib\_z30             TAG=996  ,CONF=expand\_doc\_bib\_z30\_vdk

OAIVM fix\_doc\_do\_file\_08             vdk.fix

OAIVM fix\_doc\_sort

(procedury pro úpravu záznamu mohou být v tab\_fix nebo v tab\_expand)