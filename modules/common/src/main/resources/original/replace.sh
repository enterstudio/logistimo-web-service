#!/usr/bin/env bash
cd $1
file=( BackendMessages_en.properties.orig HelpMessages_en.properties.orig Messages_en.properties.orig JSMessages_en.properties.orig )
rs="receiving store"
RS="Receiving store"
is="issuing store"
IS="Issuing Store"
inu="issues\/net utilization"
INU="Issues\/Net Utilization"
str="store"
STR="Store"
strs="stores"
STRS="Stores"
astr="a store"
ASTR="A Store"
aSTR="a Store"
Astr="A store"
ais="an issuing store"
Ais="An issuing store"
aIS="an Issuing store"
AIS="An Issuing store"

for xyz in "${file[@]}"
do
echo ' ' >> ${xyz}
cat ${xyz} | while read p
do
	IFS='=' read var1 var2 <<< "${p}"
	varF="$(echo ${var2} | sed -e 's/customer/'"$rs"'/g' | sed -e 's/Customer/'"$RS"'/g' |
	sed -e 's/vendor/'"$is"'/g' | sed -e 's/Vendor/'"$IS"'/g' | sed -e 's/Issues/'"$INU"'/g' |
	sed -e 's/issues/'"$inu"'/g' | sed -e 's/Entities/'"$STRS"'/g' | sed -e 's/entities/'"$strs"'/g' |
	sed -e 's/Entity/'"$STR"'/g' | sed -e 's/entity/'"$str"'/g' | sed -e 's/an store/'"$astr"'/g' |
	sed -e 's/an Store/'"$aSTR"'/g' | sed -e 's/An Store/'"$ASTR"'/g' | sed -e 's/An store/'"$Astr"'/g' |
	sed -e 's/a issuing store/'"$ais"'/g' | sed -e 's/A issuing store/'"$Ais"'/g' |
	sed -e 's/a Issuing store/'"$aIS"'/g' | sed -e 's/A Issuing store/'"$AIS"'/g')"
    if [[ ${var2} != ${varF} ]];then
    	echo ${var1}=${varF}
    fi
echo ${var1}=${varF} >> "$xyz".prop
done
sed -i.ext '$d' "$xyz".prop
done  
rm -rf BackendMessages_en.properties.orig HelpMessages_en.properties.orig Messages_en.properties.orig JSMessages_en.properties.orig
mv BackendMessages_en.properties.orig.prop.ext BackendMessages_en.properties.orig
mv HelpMessages_en.properties.orig.prop.ext HelpMessages_en.properties.orig
mv Messages_en.properties.orig.prop.ext  Messages_en.properties.orig
mv JSMessages_en.properties.orig.prop.ext  JSMessages_en.properties.orig