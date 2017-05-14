/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

var $operations = { 'vs' : 'दृश्य', 'ei' : 'वितरण', 'er' : 'प्राप्तियाँ', 'es' : 'उपलब्ध स्टॉक', 'ew' : 'निष्काषित', 'vo' : ' "मांग",', 'no' : 'नया मांग', 'ts' : 'ट्रांसफर स्टॉक','ne':'स्टोर','vt':'लेन-देन','vp':'रूपरेखा','xi':'निर्यात के लिए सूची', 'tr':'ट्रांसफर'};

var $orderTypes = { 'prc' : 'खरीदी', 'sle' : 'बिक्री' };

var $nMsg = { 'ei' : 'नहीं भेजे गए वितरण', 'er' : 'नहीं भेजे गए प्राप्तियाँ', 'es' : 'नहीं भेजे गए उपलब्ध स्टॉक', 'ew' : 'नहीं भेजे गए निष्काषित स्टॉक',  'no' : 'नहीं भेजे गए मांग', 'ts' : 'नहीं भेजे गए ट्रांसफर स्टॉक','ne': 'नहीं भेजे गए स्टोर्स'};
var $sMsg = { 'ei' : 'आप नहीं भेजे गए वितरण भेजना चाहते हैं', 'er' : 'आप नहीं भेजे गए प्राप्तियाँ भेजना चाहते हैं', 'es' : 'आप नहीं भेजे गए उपलब्ध स्टॉक भेजना चाहते हैं', 'ew' : 'आप नहीं भेजे गए निष्काषित स्टॉक भेजना चाहते हैं',  'no' : 'आप नहीं भेजे गए मांग भेजना चाहते हैं', 'ts' : 'आप नहीं भेजे गए ट्रांसफर स्टॉक भेजना चाहते हैं','ne': 'आप नहीं भेजे गए स्टोर्स भेजना चाहते हैं' };

var $tMsg = { 'ei' : 'वितरित', 'er' : 'प्राप्त', 'es' :  'स्टॉक गणना', 'ew' : 'निष्काषित ',  'ts' : 'रांसफर स्टॉक'};
var $tPMsg = { 'ei' : 'वितरण', 'er' : 'प्राप्तियाँ', 'es' :  'स्टॉक गणना', 'ew' : 'निष्काषित',  'ts' : 'रांसफर स्टॉक'};	

var $networkErrMsg = "सर्वर से कनेक्ट करने में असमर्थ है| नेटवर्क कनेक्शन की जाँच करें और पुन: प्रयास करें|";
	  
		



var $labeltext = {
    
acquiringlocation: "स्थान की पहचान की जा रही है",
    
added:"जोड़ा गया",
    
additemstoorder: "मांग में सामग्री जोड़ें",
    
addpayment: "मूल्य दर्ज करें",
    
addtoorder: "मांग में जोड़े",

allocated: "आवंटित",
    
availablecredit: "उपलब्ध क्रेडिट",
    
available: "उपलब्ध", 

backordered: "आंशिक रूप से भेजा गया",
    
batch:"बैच",
	
batches: "बैच",
    
batchexpirydate: "समाप्ति तिथि",
    
batchmanufactureddate: "निर्माण तिथि",

by:"द्वारा",

cancel_lower: "रद्द करो",
    
cancelled:"रद्द किया गया",
    
call: "फ़ोन",
    
change: "बदलें",
    
cityvillage: "शहर/गांव",
    
clickhere: "यहाँ क्लिक करें",
    
clicktoaddnew:"नया \'जोड़ने\' के लिए क्लिक करें",
    
closingstock: "समापन स्टॉक",

comments:"टिप्पणियाँ",
	
confirm: "सुनिश्चित करें",

confirm_lower:"सुनिश्चित करें",
    
confirmed:"सुनिश्चित",
    
confirmpassword: "पासवर्ड की पुष्टि करें",
    
consumptionrate: "खपत की दर",
    
created: "बनाया गया",
    
customer: "प्राप्तकर्ता",
    
customers: "प्राप्तकर्ता",
    
daily: "दैनिक",

day: "दिन",
    
days_lower: "दिनों",
    
days: "दिनों",
    
date:"दिनांक",
    
demandforecast: "मांग पूर्वानुमान",
    
discarded:"निष्काषित ",
   
discards: "निष्काषित",

discard: "निष्काषित",
    
districtcounty: "जिला/काउंटी",
    
durationofstock: "drtn का स्टॉक",

earlier:"पहले",

editorder:"मांग बदलने का कारण",
    
email: "ईमेल",
    
entercustomer: "प्राप्तकर्ता दर्ज करें",
    
enterdiscards:"निष्काशन दर्ज करें",
    
enterentitydetails: "स्टोर का विवरण दर्ज करें",
    
enterentityname: "स्टोर का नाम दर्ज करें",
    
enterissues:"वितरण दर्ज करें",
    
enterphysicalstock: "उपलब्ध स्टॉक दर्ज करें",
    
enterpurchases:"खरीदी दर्ज करें",
    
enterreceipts:"प्राप्ति दर्ज करें",
    
entersales:"बिक्री मांग दर्ज करें",
    
enteruserdetails:"उपयोगकर्ता का विवरण दर्ज करें",
    
entervendor: "वितरणकर्ता दर्ज करें",
    
entity:"स्टोर",
    
entities : "स्टोर्स",
    
entitydetails: "स्टोर का विवरण",
    
entityname: "स्टोर का नाम",
	
error: "त्रुटि",
	
error_system: "सर्वर में कोई त्रुटि आई। बाद में कृपया पुन: प्रयास करें।",

estimateddateofarrival:"आगमन की अनुमानित तिथि",
	
exiting: "प्रस्थान",
       
expiringon:"समाप्ति समय",

expiredbatches: "अवधि समाप्त हो चुके बैच",
    
export:"एक्सपोर्ट करें",
    
exportinventory: "वस्तुसूची एक्सपोर्ट करें",
    
exportorder: "मांग एक्सपोर्ट करें",
    
exportorders: "मांग एक्सपोर्ट करें",

exporttransactions: "लेन-देन एक्सपोर्ट करें",

feedback: "प्रतिक्रिया",

feedbacktext: "प्रतिक्रिया सूचना",
    
firstname: "प्रथम नाम",
    
from:"से",

fromdate: "तारीख से",

forgotpassword: "पासवर्ड भूल गए",
    
fulfilled:"पूर्ण किया गया",

fulfil:"पूर्ण करो",

fulfil_lower:"पूर्ण करो",

fulfillmentdiscrepancyreason: "पूर्ती में भेद का कारण",

halfyear_lower: "आधा वर्ष",

halfyear: "आधा वर्ष",
 
imageoptions: "फोटो विकल्प",

incoming: "प्राप्तियाँ",

intransit: "परिवहन में",
    
inventory:"वस्तुसूची",

inventory_lower:"वस्तुसूची",
    
issued:"वितरित",
    
issues:"वितरण",
   
items:"मांग की गयी सामग्री",
    
lastname:  "अंतिम नाम",
    
lastupdated:"पिछ्ला अपडेट",
    
limit: "सीमा",
    
loading:"लोडिंग",
    
loadingmaterials:  "सामग्री लोड हो रहीं हैं",
    
loadingorders:"मांग लोड हो रहा है",
    
location: "स्थान",
    
manufacturedby: "द्वारा निर्मित",
    
materials:"सामग्री",
    
max: "अधिकतम",
    
message:"संदेश",
    
min: "न्यूनतम",

month: "महीना",
    
months_lower: "महिनों",

months: "महिनों",
	    
monthly:  "मासिक",
    
myentity: "मेरा स्टोर",
    
name:  "नाम",
    
needhelptext: "सहायता चाहिए?",
    
newentityname: "नई स्टोर का नाम",
    
newname:"नया नाम",
    
neworder : "नया मांग",
    
neworders: "नये मांग",
    
newtext:"नया",

noofitems: "वस्तुओ की संख्या",
    
notification:"सूचना",
    
notifications: "सूचनाएँ",
    
openingstock:"आरंभिक स्टॉक",
    
operationstext:"संचालन ",
    
of:"का",
    
on: "पर",

oops: "ग़लती",

open:"Open",
    
or:"या",
    
ordernewstock:"नए स्टॉक के मांग",
    
order: "मांग",

order_lower:"मांग",

orderingdiscrepancyreason:"मांग में भेद का कारण",

ordermodificationreason: "मांग बदलने का कारण",

ordered: "आदेश मात्रा",

orders: "मांग",

orders_lower: "मांग",

ordertag:"मांग टैग",

original: "मूल",

originallyordered: "मूल मांग",
    
otherentities: "अन्य स्टोर्स",

others: "अन्य",

outgoing: "वितरित",

overridereason:"प्रत्यादिष्ट करने का कारण",
    
packagesize:"पैकेज का आकार",
    
paid:"भुगतान किया",
    
password: "पासवर्ड",
    
paymentoption:"भुगतान विकल्प",
    
pending:  "पर्यन्त",
    
phone:  "फ़ोन",
   
photo: "फोटो",

photos: "फ़ोटोज़",
    
physicalstock:  "उपलब्ध स्टॉक",
    
pincode:  "पिन कोड",
    
price: "कीमत",
    
profile: "वर्णन",
    
purchaseorder:"खरीदी का मांग",
    
purchases:"खरीदी",
    
quantity: "मात्रा",
   
quantity_lower: "मात्रा",

quantityentered :"दर्ज की गई",

quarter_lower: "त्रिमास",

quarter: "त्रिमास",
    
reasons:"कारण",
    
reconnect:"फिर से कनेक्ट करें",
    
reconnectnow:"अब फिर से कनेक्ट करें",
    
receipts:"प्राप्तियाँ",
    
received:"प्राप्त",

received_upper: "प्राप्त",
    
recommendedquantity: "उपयुक्त मात्रा",

recommendedquantity_lower: "उपयुक्त मात्रा",
    
refreshinginventory: "वस्तुसूची रिफ्रेश हो रही है",
	
refreshingofflinedata: "ऑफ़लाइन डेटा रिफ्रेश हो रहा है",
    
refreshingtransactions:"लेनदेन रिफ्रेश हो रहा है",

referenceid:"रिफरेन्स आईडी",

release:"रिहाई",
    
releases:"रिहाईयां",

reset:"रीसेट",

reopen:"फिर से खोंले",

reopen_lower:"फिर से खोंले",
    
reorder:"पुन: मांग",

requiredbydate: "अपेक्षित तिथि",
    
review:"समीक्षा",
    
routetag:"मार्ग टैग",
    
sales:"बिक्री",
    
salesorder:"बिक्री मांग",

selected:"चुना गया",
    
selectroutetag:"मार्ग टैग का चयन करें",

selecttag:"टैग का चयन करें",

ship:"भेजो",

ship_lower:"भेजो",
    
shipped:"भेजा गयाा",

shipment:"शिप्मन्ट",

shipment_lower: "शिप्मन्ट",

shipments: "शिप्मन्ट",

shipmentid: "शिप्मन्ट आईडी",

shipmentdiscrepancyreason: "भेजने में भेद का कारण",
	
someerrorswereencountered: "कुछ त्रुटियाँ सामना करना पड़ा",
    
state:  "राज्य",

status:"स्थिति",
    
stockcounted:"स्टॉक गणना",

stock:"स्टॉक",

stockonhand: "कुल स्टॉक",

stockatrelatedkid : "relatedkid का स्टॉक",

street: "सड़क",
    
submittingexportrequest: "एक्सपोर्ट के लिए अनुरोध भेजी जा रही है",
    
support:"सहायता",
    
taluk:"तालुक",

tag: "टैग",
    
tax:  "शुलक",
    
to:"को",

todate: "तारीख तक",

topic: "विषय",

total: "कुल संख्या",

trackingid: "ट्रैकिंग आईडी",
    
transaction: "लेन-देन",
    
transactionhistory: "पिछले लेनदेन",
    
transactions: "लेन-देन",

transactions_lower: "लेन-देन",
    
transferstock: "ट्रांसफर स्टॉक",

transfer: "ट्रांसफर",

transfers: "ट्रांसफर",
    
transporter: "ट्रांसपोर्टर",
    
unknown: "अज्ञात",
    
updated: "अपडेट",
    
updatingorder: "मांग अपडेट हो रहा है",
    
userid:  "उपयोगकर्ता की पहचान",

user: "उपयोगकर्ता",
    
users:'उपयोगकर्ता',
    
vendor: "वितरणकर्ता",
    
vendors: "वितरणकर्ता",
    
view: "देखें",

viewcomments:"टिप्पणियाँ देखें",
    
viewdemand:"मांग देखें",
    
viewhistory:"इतिहास देखें",
    
vieworders: "मांग देखें",
    
viewstock:"स्टॉक देखें",

week: "सप्ताह",

weeks_lower: "हफ़तहों",

weeks: "हफ़तहों",
   
weekly: "साप्ताहिक",
    
welcome: "स्वागत",
    
yourcurrentlocationis : "आपका वर्तमान स्थान है"

};



var $buttontext = {
    
add: "जोड़ें",

addentity:"स्टोर जोड़ें",

addnewbatch: "नए बैच जोड़ें",

allocate: "आबंटन करना",
    
back:"पीछे जाए",
    
cancel: "रद्द करो",

cancelorder: "<order> रद्द करो",
    
clear: "मिटायें",
    
clearall: "सभी मिटायें",

clearbatches:  "बैच मिटायें",

delete:"हटाना",
    
edit: "बदलें",
    
editentity: "स्टोर बदलें",

edituser: "उपयोगकर्ता बदलें",
    
enter: "दर्ज करें",
    
exporttext: "एक्सपोर्ट",

hidebatches: "बैच छिपाना",
    
home:"मुख्य पृष्ठ",
    
login:"लॉग इन",
    
logout:"लॉग आउट",
   
next: "अगला",
    
no: "नहीं",
    
ok: "ठीक है",
    
options:"विकल्प",
    
previous: "पूर्व",
    
print:"प्रिंट करें",
    
refresh: "रिफ्रेश करें",

resendOTP: "पुनः ओटीपी भेजें",

resetpassword: "रीसेट पासवर्ड",
    
save: "सेव करें",
    
send: "भेजें",
    
sendnow: "अभी भेजेँ",

sendOTP: "ओटीपी भेजें",

skip: "लंघन",

submit: "भेजना",
    
upload: "अपलोड",

viewbatches: "बैच का अवलोकन",
    
yes:"हाँ"

};


var $messagetext={

actualdatenotinfuture: "वास्तविक तिथि भविष्य में नहीं हो सक्ति",
	
adding: "जोड़ना",
    
allocationcannotbegreaterthanstock: "आवंटित स्टॉक उपलब्ध स्टॉक से अधिक नहीं हो सकता है(sq)",

allocationscannotbegreaterthanordered: " आवंटित स्टॉक मांग किया गया मात्रा से अधिक नहीं हो सकता है",

allocatebeforeshipping:"आर्डर को भेजने से पहले कृपया आइटम्स पर स्टॉक आवंटित करें ",

allocatefullybeforeshipping: "आर्डर को भेजने से पहले कृपया आइटम्स पर स्टॉक पूरा आवंटित करें",

allitemscannotbezero:"सभी एटमों का स्टॉक 0 नहीं हो सकता है",
  
allitemsinorder: "इस मांग में सभी सामग्री पहले से हीें हैं",

areallowedtobeuploaded:"अपलोड करने के लिए अनुमति दी जाती है|",
    
areyousureyouwantomarkorder: "क्या आप इस मांग को <status> करना चाहते ह?ैं",

areyousureyouwantomarkorder: "क्या आप इस <order> को <status> करना चाहते ह?ैं",

areyousureyouwantomarkorderasfulfilled:"क्या आप इन सामग्रियों का प्राप्त की मात्रा डालकर इस <order> पात्र को पूर्ण करना चाहेंगे?",
    
assigncurrentlocation: "इस संस्था के लिए अपने वर्तमान स्थान आवंटित करें",
    
authenticating:"प्रमाणित हो रहा हैै",
    
automaticallymanagerofentity: "आप इस स्टोर के लिए स्वचालित रूप से प्रबंधक हो जाएंगे",

availablestockiszero: "<material> का उपलब्ध स्टॉक शून्य है | स्टॉक को आवंटित करने से पहले कृपया स्टॉक उपलब्ध करें |",

availablestockisless: "\"<material>\" का उपलब्ध स्टॉक (<st>) मांग की मात्रा (<oq>) से काम है |",
    
batchdatarequired: "बैच मेटाडाटा सामग्री संचालन के लिए आवश्यक है",

batchshouldnothavequotes:"बैच में क्वोट(\") शामिल नहीं हो सकता हे",
    
batchmanagementisdisabled: "बैच प्रबंधन अनुपलब्ध है ।",
	
batchalreadyexists: "यह बैच पहले से ही मौजूद है",

cannotbetransferred: "ट्रांसफर नहीं किया जा सकता |",
    
cannotexceedcurrentstock: "मौजूदा स्टॉक से अधिक नहीं हो सकता है", 

cannotexceedavailablestock: "उपलब्ध स्टॉक से अधिक नहीं हो सकता ह",

changesmadepleasesave:"आपने कुछ परिवर्तन किया है| क्या आप इसको 'सेव' करना चाहते हैं?",
    
changedorderquantityfromrecommendedquantity: "आपके द्वारा मांग की गयी मात्रा उपयुक्त मात्रा से अलग है",
       
changingstatusoforder: "मांग की स्थिति बदलने से",

chooseshipment:"कृपया एक शिपमेंट चुनें",
    
checkthisforagent: "(आप किसी भी लेन देन की जाँच तभी करे जब आप उस स्टोर से सम्बंधित एजेंट हैं)|",

clearcustomerselected: "आपने वितरण के लिए एक प्राप्तकर्ता का चयन किया था। प्राप्तकर्ता की जानकारी अब सामग्री से हटा दी जाएगी। क्या आप जारी रखना चाहते हैं?",
    
clearvendorselected: "आपने पहले प्राप्ति के लिए एक वितरणकर्ता का चयन किया था। वितरणकर्ता की जानकारी अब सामग्री से हटा दी जाएगी। क्या आप जारी रखना चाहते हैं?",

clearvendorselectedpo: "आपने पहले खरीदी का मांग के लिए एक वितरणकर्ता का चयन किया था। वितरणकर्ता की जानकारी अब सामग्री से हटा दी जाएगी। क्या आप जारी रखना चाहते हैं?",
    
clearenterdiscards:"आपने \"निष्काशन दर्ज करें\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterdiscardsformaterial:"आपने इस सामग्री के लिए \"निष्काशन दर्ज करें \"  के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterissues:"आपने \"वितरण दर्ज करें\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterissuesformaterial:"आपने इस सामग्री के लिए \"वितरण दर्ज करें\" के अंतर्गत डेटा दर्ज किया है|  किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterphysicalstock: "आपने \"उपलब्ध स्टॉक दर्ज करें\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterphysicalstockformaterial:  "आपने इस सामग्री के लिए \"उपलब्ध स्टॉक दर्ज करें\"  के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterreceipts:"आपने \"्प्राप्ति दर्ज करें\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearenterreceiptsformaterial:"आपने इस सामग्री के लिए \"्प्राप्ति दर्ज करें\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearentertransferstock:"आपने \"ट्रांसफर स्टॉक\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clearentertransferstockformaterial:"आपने इस सामग्री के लिए \"ट्रांसफर स्टॉक\" के अंतर्गत डेटा दर्ज किया है| किसी भी नए ऑपरेशन से पहले कृपया दर्ज किया हुआ डेटा को भेजें या मिटायें|",
    
clickheretoenterOTP: "आप के पास पहले से ओन टाइम पासवर्ड (OTP) मौजूद हो तो यहाँ क्लिक करें ",

confirmclearbatches: "यह सभी बैच मात्रा को शून्य कर देगा । क्या आप आगे बढ़ना चाहते हैं?",

contactusifyouneedhelp: "यदि आपको सहायता की आवश्यकता हो",

correctandresend: "ठीक करें और पुन: भेजें",

couldnotrefreshinventory: "वस्तुसूची का रिफ्रेश नहीं हो पाया|",

couldnotrefreshorders: "मांग का रिफ्रेश नहीं हो पाया|",

couldnotrefreshtransactions: "लेन-देन का रिफ्रेश नहीं हो पाया|",

couldnotstoreentitylocally:", स्थानीय स्तर पर परिवर्तन संग्रहीत नहीं हो पाया| कृपया लोग आउट करके रिफ्रेश अथवा ताजा कीजिये|",
    
couldnotstorelocally: ", स्थानीय स्तर पर परिवर्तन संग्रहीत नहीं हो पाया| कृपया लोग आउट करके रिफ्रेश अथवा ताजा कीजिये|",
    
couldnotstoreorderlocally: ", स्थानीय स्तर पर मांग का संग्रहीत नहीं हो पाया| कृपया लोग आउट करके रिफ्रेश अथवा ताजा कीजिये|",
    
creditavailableafterorderships:"इस मांग को भेजने के बाद क्रेडिट उपलब्द होगा",
    
creditavailablenow:  "क्रेडिट अब उपलब्ध है",

dateofactualissue: "वितरण की वास्तविक तिथि",

dateofactualreceipt: "प्राप्ति की वास्तविक तिथि",
    
dateofactualphysicalstock: "उपलब्ध स्टॉक की वास्तविक तिथि",
    
dateofactualdiscard: "रद्द करने की वास्तविक तिथि",
    
dateofactualtransfer: "ट्रांसफर की वास्तविक तिथि",

deleteoneofthem:"आप एक नई फोटो  अपलोड करना चाहते हैं तो उनमें से एक को रद्द करो|",
    
didnotreceivecorrectresponse:"सही जवाब नहीं मिला, कुछ सर्वर त्रुटि के कारण| फिर से भेजने का प्रयास करें|",
    
differentfromrecommendedqty: "उपयुक्त मात्रा से अलग है",
    
done:"किया ",
    
donewitherrors:"किया - त्रुटियों के साथ",
    
doyouwanttologout:"क्या आप लॉग आउट करना चाहते हैं?",
    
doyouwanttocontinue: "क्या आप जारी रखना चाहते हैं?",
   
emptyordercannotbe:"मांग खाली नहीं हो सकता",
	
enablelocationservice: "लोकेशन सर्विस सक्षम नहीं है। अब इसे सक्षम करें?",

enteractualdate: "वास्तविक तिथि दर्ज करें",
    
enteraentityname: "स्टोर का नाम दर्ज करें",
    
enteramaterialname:  "सामग्री का नाम दर्ज करें",
    
enteramobilephonenumber: "मोबाइल फोन नंबर दर्ज करें",
    
enteramount: "मात्रा दर्ज करें",
    
enteranumber: "नंबर दर्ज करें",
    
enterbatchnumber: "बैच संख्या दर्ज करें",

enterbatchexpirydate : "बैच समाप्ति तिथि दर्ज करें",

enterbatchmanufactureddate: "बैच निर्माण तिथि दर्ज करें",
   
enteremailaddress:"अपना ईमेल पता दर्ज करें",
    
enteremailaddresstoexportinventory: "वस्तुसूची एक्सपोर्ट करने के लिए एक ईमेल पता दर्ज करें ",

enteremailaddresstoexportorder: "मांग एक्सपोर्ट करने के लिए एक ईमेल पता दर्ज करें ",

enteremailaddresstoexporttransactions: "लेनदेन एक्सपोर्ट करने के लिए एक ईमेल पता दर्ज करें ",
   
entermanufacturername:  "निर्माता का नाम दर्ज करें",
    
enternameforuser:"उपयोगकर्ता का नाम दर्ज करें",
    
enterordernumber: "मांग का नंबर दर्ज करें",

enterordertag:"मांग टैग दर्ज करें",

enterotpmessage:"ओटीपी प्राप्त होते ही कृप्या अपना उपयोगकर्ता नाम और ओटीपी दर्ज करें और \'रीसेट पासवर्ड\' पर क्लिक करें। एक नया पासवर्ड आपके फोन पर भेजा जाएगा।",

enterotp: "ओटीपी दर्ज करें",
   
enterpassword:"पासवर्ड दर्ज करें",

enterreason: "कारण दर्ज करें",

enteruniqueentityname: "स्टोर के लिए एक अनोखा नाम दर्ज करें",
    
enterusername:"उपयोगकर्ता का नाम दर्ज करें",
    
entityerrorencountered:"इस स्टोर का अद्यतन करते समय एक गलती हुआ|",
    
entitymappedlocation: "स्टोर को इस स्थान पर मैप की गई है|",
    
entityupdatedonserver:"स्टोर को सर्वर पर अपडेट की गई",
    
errorsinquantities: "दर्ज की गई मात्रा में कुछ त्रुटियां पायी गई हैं। नीचे दी गई त्रुटियों को ठीक करें और पुन: प्रयास करें|",

expirydatecannotbegreaterthantoday: "समाप्ति तिथि वर्तमान तिथि के पश्चात संभव नहीं है",
    
exportinitiated: "एक्सपोर्ट शुरू की",

feedbacknote: "ध्यान दें: इस फार्म का उपयोग एप्लीकेशन सुविधाओं पर प्रतिक्रिया प्रदान करने के लिए करें। एप्लीकेशन के उपयोग से संबंधित किसी भी सहायता के लिए सहायता फ़ोन या ईमेल का प्रयोग करें।",
       
feedbackacknowledge:"आपके बहुमूल्य प्रतिक्रिया के लिए शुक्रिया. हमारी टीम आपके सुझावों पर विचार करेगी|",

fefomessage: "आपने पुराने बैचों को पूरी तरह से आवंटित नहीं किया है। आपसे सिफारिश है कि उन बैचो को पूरी तरह से आवंटित करें जिनकी समय सीमा पहले समाप्त होने वाली है । क्या आप मौजूदा आवंटन को जारी रखना चाहते हैं ?",      

fetchstockatrelatedentity: "mn का स्टॉक मात्रा relatedkid से लाऊँ ?",

fieldsmarkedmandatory:"(* से चिन्हित फील्ड अनिवार्य हैं)",

forgotpasswordmessage:"यूजर आईडी दर्ज करें। एक बार ओटीपी उत्पन्न होता है और उपयोगकर्ता के पंजीकृत मोबाइल फोन के एसएमएस पर भेजा जाएगा। ओटीपी प्राप्त हो जाने के बाद , बाद में दिखाया जाएगा कि ओटीपी क्षेत्र में इस पेज में ओटीपी दर्ज करें।",
    
fromthelistbelow: "नीचे दी गई सूची से|",

generateotpmessage: "एसएमएस पर ओटीपी प्राप्त करने में कुछ समय लग सकता है। अगर 15 मिनट के बाद भी ओटीपी प्राप्त नहीं हुआ तो \'पुन: ओटीपी भेजें\' पर क्लिक करें।",
    
invalidemail:"अमान्य ईमेल",
    
invalidnameorpassword: "नाम अथवा पासवर्ड गलत है|",
    
invalidnameorpasswordverify: "नाम या पासवर्ड गलत है| अपना पासवर्ड फिर से जांच करें और लोग इन करें| अथवा प्रवेश पाने के लिए सर्वर के साथ रीकनेक्ट करें|",

inventoryrefreshedfromserver: "वस्तुसूची को सर्वर से रिफ्रेश किया गया",
    
inventorysuccessfullyupdated:"वस्तुसूची सफलतापूर्वक अपडेट हुआ|",
	
isrequired: "कि ज़रूरत है",
    
lessthanquantityof: "का आदेश मात्रा से कम है",
    
limitexceeded:"सीमा पार कर",
	
loginsessionhasexpired: "आप का लोगिन का समय पूरा हो गया | कृपया फिर से लोगिन कीजिये |",
    
newquantityafteroperation:"ऑपरेशन के बाद नई मात्रा होगी - ",
    
makememanager:"मुझे इस स्टोर का प्रबंधक बनाओ।",

manufactureddatecannotbelaterthantoday: "निर्माण तिथि वर्तमान तिथि से पूर्व संभव नहीं है",
    
markorderasfulfilled:"इस मांग को पूर्ण चिन्ह दे",
	
materialdoesnotexistdestentity: "सामग्री गंतव्य की स्टोर में मौजूद नहीं है",
	
materialsexcessofstock: "इन सामग्री का मात्रा उपलब्ध स्टॉक से ज्यादा है| कृपया इनको फिर से जांच कीजिये और ठीक मात्रा की एंट्री कीजिये|",
 
materialsexcessofavailablestock: "इन सामग्री का मात्रा उपलब्ध स्टॉक से ज्यादा है| कृपया इनको फिर से जांच कीजिये और ठीक मात्रा की एंट्री कीजिये|",
 
mustbepositiveinteger: "एक सकारात्मक पूर्णांक होना चाहिए",

newversionofappavailable: "एक नया मोबाइल एप्लीकेशन उपलब्ध है| X दिन के पहले प्लेस्टोरे से अपग्रेड कीजिये|" , 
    
nobatchesavailable: "कोई बैच उपलब्ध नहीं है|",
    
nocamera:"कोई कैमरा नहीं है",
    
nocameradetected:"खेद हैं की आपके यंत्र में कोई कैमरा का पता नहीं लगाया जा सका ",
    
nochanges: "कोई बदलाव नहीं",
    
nochangestosend: "आपके द्वारा मांग की गयी मात्रा को संपादित करें और उसके बाद भेजें",

nocustomers: "कोई प्राप्तकर्ता नहीं|",
    
nocustomerorvendor:"स्थांतरण स्थांतरण करने के लिए कोई प्राप्तकर्ता या वितरणकर्ता नहीं मिला|",
    
nocustomerorvendortoorder:"मांग करने के लिए कोई प्राप्तकर्ता या वितरणकर्ता नहीं मिला|",
    
noitems:"कोई सामग्री नहीं",
    
noitemshavebeenedited: "इस आदेश मैं कोई सामग्री नहीं बदला गया|",
    
noitemsinorder: "इस मांग में कोई सामग्री नहीं है|",

noitemstoreview: "सुनिश्चित करने के लिए कोई सामग्री नही है|",
    
nooperationsenabled: "कोई आपरेशन सक्षम नहीं हैं",
    
noorders: "कोई मांग नहीं हैं",
    
noordersavailable:"कोई मांग उपलब्ध नहीं हैं| मांग लोड करने के लिए रिफ्रेश करें|",
    
nomaterials:"कोई सामग्री नहीं हैं",
	
nonetworkconnection: "नेटवर्क कनेक्शन नहीं है|",
    
nothingtosend:"भेजने के लिए कुछ नहीं है| प्रासंगिक डेटा दर्ज करें और पुन: प्रयास करें|",
    
notinteger: "एक पूर्णांक ही दर्ज करें।",
    
notpositivenumber:"एक सकारात्मक संख्या दर्ज करें",

nostockavailable: "कोई स्टॉक नहीं है|",
	
nosuchmaterial: "इस स्टोर मई ऐसा कोई सामग्री नहीं है",
    
notransactions:"कोई लेन-देन नहीं है|",
    
notvalidnumber:"आपने एक मान्य संख्या दर्ज नहीं की है|",

novendors: "कोई वितरणकर्ता नहीं है|",
    
novendorspecified:"कोई भी वितरणकर्ता इस मांग के साथ संबंधित नहीं हैं| कृपया अपने प्रबंधक से संपर्क करें ओर सुनिश्चित करें की इस मांग के साथ एक वितरणकर्ता जोड़ें|",
    
numunitsdoesnotmatch:"mn की मात्रा, hnm की इकाई के गुणकों से मेल नहीं हो रहा है । यह hq mn के गुणकों में होना चाहिए।",

operatorassignedtothisentity:"ऑपरेटर इस स्टोर के साथ आवंटित किया गया|",

orderedcannotbelessthanallocated:"मांग की मात्रा आवंटित मात्रा से कम नहीं हो सकता है |",

orderedcannotbelessthanshipped:"मांग की मात्रा भेजा गयाा मात्रा से कम नहीं हो सकता है |",
    
orderitems: "मांग की गयी सामग्री",
    
orderquantitydifferentfromrecommendedquantity:"कुछ सामग्री का मात्रा उपयुक्त मात्रा से अलग है| क्या आप ऐसे ही जारी रखना चाहते हैं?",
    
ordersuccessfullycreatewithid:"मांग सफलतापूर्वक ये आईडी के साथ बनाई गई",
    
ordersuccessfullyupdated: "मांग सफलतापूर्वक अपडेट हुआ|",
	
ordersrefreshedfromserver: "मांग को सर्वर से रिफ्रेश किया गया",

otpsenttomobile: "ओटीप एसएमएस के माध्यम से अपने फोन पर भेजा गया है|",
    
passwordsdonotmatch: "पासवर्ड नहीं मिल रहें हैं| फिर से वही पासवर्ड दर्ज करें|",

passwordsenttomobile:"एक नया पासवर्ड एसएमएस के माध्यम से अपने फोन पर भेजा गया है|",
    
physicallyatentitynow: "(यदि आप इस स्टोर पर मौजूद हैं)",
    
pleaseadditemstoorder:"कृपया मांग में सामग्री जोड़ें|",
    
pleasechooseanothermaterial:"कृपया सामग्री दूसरा चुनें|",

pleaseenterfeedback:"कृप्या एप्लीकेशन सुविधाओं पर अपनी प्रतिक्रिया दर्ज करें।",

pleaseentertransporter:"कृपया एक ट्रांसपोर्टर दर्ज करें|",
    
pleaseentervalidcityname:"1-200 अक्षरों मैं एक वैध शहर / गांव का नाम दर्ज करें",
    
pleaseentervalidentityname: "1-200 अक्षरों मैं एक वैध स्टोर का नाम दर्ज करें",
    
pleaseentervalidpassword: "एक वैध पासवर्ड (4-20 अक्षर) दर्ज करें|",
    
pleaseentervalidphone:"एक वैध मोबाइल फोन नंबर (8-15 संख्या एवं किसी भी रिक्त स्थान / अक्षर के बिना) दर्ज करें [जैसे 9834568420] |",
    
pleaseentervalidstatename:"1-200 अक्षरों मैं एक मान्य राज्य का नाम दर्ज करें",
    
pleaseentervaliduserid: "4-50 अल्फा-न्यूमेरिक अक्षरों मैं एक वैध यूजर आईडी दर्ज करें|",
    
pleaseentervalidusername:"1-20 अक्षरों मैं एक मान्य उपयोगकर्ता का नाम दर्ज करें",
    
pleasenteryourusernameandlogin:"अपने उपयोगकर्ता का नाम और लॉग इन दर्ज करें|",

pleaseenteryourusernameandsend:"अपने उपयोगकर्ता का नाम दर्ज करें और भेज दें। ओटीप एसएमएस के माध्यम से आपके फोन पर भेजा जाएगा।",

pleaseloginbeforesendingfeedback: "कृप्या प्रतिक्रिया भेजने से पहले लॉगिन करें।",

pleaseselectamaterialstatus: "एक सामग्री का स्थिति का चयन करें",

pleaseselectareason:"एक कारण का चयन करें",

positivenumbergreaterthanzero:"0 से बड़ी संख्या दर्ज करें",
    
positivenumbergreatenthanorequaltozero: "0 या 0 से बड़ी संख्या दर्ज करें",

quantityallocateddoesnotmatchordered:"आवंटित allocq मात्रा आदेश दिया मात्रा orderq मैच नहीं होता हैें। क्या आप मौजूदा आवंटन को जारी रखना चाहते हैं ?",  
        
receiveemailat: "आप ईमेल के माध्यम से डाटा प्राप्त करेंगे",
    
reconnectoserverrequired:"सर्वर से पुनः कनेक्ट करें|",
    
refreshlogintext:"सर्वर से पुनः कनेक्ट करने के लिए अपना नाम और पासवर्ड दर्ज करें और नीचे क्लिक करें|",

requireddatecannotbeearlierthantoday: "अपेक्षित तिथिवर्तमान तिथि के पश्चात संभव नहीं है",

resetbatchconfirm:"क्या आप इस बैच का उपलब्ध स्टॉक 0 करना चाहते हैं?",
    
reviewandconfirm: "समीक्षा और सुनिश्चित करें",
    
savedchanges: "अप्डेट्स को सेव किया |",
    
select: "एक चयन करें - ",

selectacustomer:"नीचे दी गई सूची से एक प्राप्तकर्ता का चयन करें|",

selectreason:"एक कारण का चयन करें",
    
selectstatus:"एक स्थिति का चयन करें",

selectavendor:"नीचे दी गई सूची से एक वितरणकर्ता का चयन करें|",
    
selectroutetagtext: "यदि लागू हो तो मार्ग टैग का चयन करें| यह स्टोर उस टैग के तहत आपके खाते में रखी जाएगी।",
    
selecttypeofentity: "ट्रांसफर के लिए अपनी इच्छानुसार किसी स्टोर का चयन करें",
    
selecttypeoforder: "मांग के प्रकार चयन करें",
    
sending: "भेजा जा रहा है",
    
sendlogininfosms: "एसएमएस या फोन पर उपयोगकर्ता के लोगिन की जानकारी भेजें",
	
serviceisdown: "इस समय सेवा उपलब्ध नहीं है। कृप्या कुछ समय बाद फिर से कोशिश करें या अपने एडमिनिस्ट्रेटर से संपर्क करें।",
	    
somedataenterednotsentlogout:"आपके द्वारा प्रवेश कुछ डेटा सर्वर को नहीं भेजा गया | क्या आप फिर भी लॉग आउट करना चाहेंगे?",
	
somedataenterednotsentlogout_loginasreconnect: "आप अभी लॉग आउट कर रहें है |<br/><br/>आप के द्वारा प्रवेश कुछ डेटा सर्वर को नहीं भेजा गया | कृपया सर्वर को डेटा भेजने (सेंड) के बाद लॉग आउट कीजिए | अभी आप लॉग आउट करेंगे तो आपको फिर से डेटा एंट्री करना पड़ेगा |<br/><br/>क्या आप फिर भी लॉग आउट करना चाहेंगे?",
    
somedataenterednotsentrefresh:"आप के द्वारा दर्ज किया गया डेटा सर्वर को नहीं भेजा गया|  रिफ्रेश करने से दर्ज किया हुआ डेटा मिट जाएगा | आप लॉगिन करके डेटा को भेज सकते हैं | क्या आप फिर भी रिफ्रेश करना चाहते हैं?",

sorryonly:"खेद केवल",

specifyreasonforcancellingorder: "मांग पात्र को कैन्सेल करने का कारण बताएं",

specifyreasonforeditingquantity: "मांग की मात्रा को एडिट करने का कारण बताएं",
    
specifyreasonforpartialfulfilment: "मांग को अधूरा पूर्ण करने का कारण बताएं",
    
specifyreasonforpartialshipment: "मांग को अधूरा भेजने का कारण बताएंt",

specifyreasonrecommendedquantity: "उपयुक्त मात्रा से विचलन करने का कारण",
    
success:"कार्य सफल हुआ",
    
successfullycreated: "सफलतापूर्वक बनाया गया",
    
successfullyupdated: "सफलतापूर्वक अपडेट",
    
switchservermobile: "सर्वर से डेटा प्राप्त हो रहा है। कृपया प्रतीक्षा करें...",
    
switchserverweb: "आप को एक नया सर्वर पर भेजा जाएगा। कृप्या फिर से लॉगिन करें।",
    
thisordercannotbe:"यह मांग नहीं हो सकता",

transactionsrefreshedfromserver: "लेनदेन को सर्वर से रिफ्रेश किया गया",
	
transferfailedfrombatchenabledentity: "एक गैर-बैच सामग्री से एक बैच सामग्री को स्थानांतरण नहीं हो सकती",

unabletofindorderid:"यह मांग आईडी मौजूद नहीं है|",
   
unabletofindentitydata:"स्टोर का डेटा प्राप्त नहीं हो पा रहा है| कृप्या रिफ्रेश करके पुन: प्रयास करें|",

unabletofindshipmenid: "इस आईडी का शिपमेंट नहीं मिला",
   
unabletofinduserdata:"उपयोगकर्ता का डेटा / विवरण प्राप्त नहीं हो पा रहा है| कृप्या रिफ्रेश करके पुन: प्रयास करें|",
    
updating:"अद्यतन",

upgradeappnow:"एक नया मोबाइल एप्लीकेशन उपलब्ध है| अभी अपग्रेड कीजिये|",
 
uploadingphoto:"फ़ोटो अपलोड हो रहा है|",

unsentpurchaseorder:"आपने कुछ खरीदी की मांग नहीं भेजी है| कृपया बिक्री मांग बनाने से पहले अपनी मांग \"भेजे\" या \"मिटायें\"|",  

unsentsalesorder:"आपने कुछ बिक्री मांग नहीं भेजी है| कृपया खरीदी की मांग बनाने से पहले अपनी मांग \"भेजे\" या \"मिटायें\"|", 

userhasassetonlypermission: "उपयोगकर्ता को सिर्फ संपत्ति अवलोकन की अनुमति है।",
    
useridmustbeunique:"यूजर आईडी अनोखी होना चाहिए|",
    
useridpasswordgenerated: "एक यूजर आईडी और पासवर्ड स्वत: इस ऑपरेटर के लिए उत्पन्न हो जाएगा| क्या आप स्वयं इन्हें प्रवेश करना चाहते हैं?",

vendorstocknotavailable:"स्टॉक आवंटित करने के लिए वेंडर का स्टॉक ज़रुरत है | सर्वर से वेंडर का स्टॉक लाएं?",

vendorstocknotavailableforcheck:"उपलब्ध स्टॉक जाँच करने के लिए, वेंडर का स्टॉक ज़रुरत है | सर्वर से वेंडर का स्टॉक लाएं? \n नोट: वितरणकर्ता का स्टॉक लाने की बाद, कृपया मांग पात्र को फिर से भेजें|",
    
verifybeforeexporting:"(एक्सपोर्ट से पहले ईमेल सत्यापित करें)",

viewstockatrelatedentity: "relatedkid का स्टॉक दिखाएँ",
    
warning: "चेतावनी",
	
willsendlater: "मैं बाद में भेजूंगा",
    
wishtoaddoperator:  "एक नया ऑपरेटर जोड़ने के लिया नीचे उसका विवरण दर्ज करें ",

    
yourcurrentlocationis: "आपका वर्तमान स्थान है"

};
