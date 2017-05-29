function validateKioskDetails( )
{
	var kioskNameFlag = validateName( document.getElementById( "name" ) , JSMessages.kiosk_name, 1, 50, 'kiosknameStatus', true);
	var countryFlag = validateTextField( document.getElementById( "countries" ), JSMessages.country, 1, 500, 'countryStatus', true );
	var stateFlag = validateTextField( document.getElementById( "states" ), JSMessages.state, 1, 500, 'stateStatus', true );
	var districtFlag = validateTextField( document.getElementById( "districts" ), JSMessages.district, 1, 500, 'districtStatus', false );
	var talukFlag = validateTextField( document.getElementById( "taluks" ), JSMessages.taluk, 1, 500, 'talukStatus', false );
	var cityFlag = validateTextField( document.getElementById( "city" ), JSMessages.village, 1, 30, 'cityStatus', true );
	var pinCodeFlag = validatePincode( document.getElementById( "pincode" ), JSMessages.zipcode, 0, 10, 'pincodeStatus', false );
	var addressFlag = validateTextField( document.getElementById( "street" ) , JSMessages.address, 0, 50, 'addressStatus', false );
	var multipleGeocodes = document.getElementById( "multiplegeocodes" ).value;
	if ( multipleGeocodes == 'true' ) {
		alert( JSMessages.geocodes_selectonemarker + '.' );
		return false;
	}
	var latitudeFlag = validateGeo( document.getElementById( "latitude" ), JSMessages.latitude, 0, 10, 'latitudeStatus', false );
	var longitudeFlag = validateGeo( document.getElementById( "longitude"), JSMessages.longitude, 0, 10, 'longitudeStatus', false );
	var kioskOwnerFlag = validateTextField( document.getElementById( "userids" ), JSMessages.user, 1, 10000, 'usersStatus', true );

	// if( kioskNameFlag && countryFlag && stateFlag && districtFlag && talukFlag && cityFlag && pinCodeFlag && pinCodeFlag && addressFlag && latitudeFlag && longitudeFlag && kioskOwnerFlag )
	if( kioskNameFlag && countryFlag && stateFlag && districtFlag && talukFlag && cityFlag && pinCodeFlag && pinCodeFlag && addressFlag && latitudeFlag && longitudeFlag && kioskOwnerFlag )
		return true;
	else
		return false;

}

// Simple function to validate whether a field has a positive number or not; also, resets the field if not
function validatePositiveNumber( valE ) {
	if ( valE.value != '' && ( isNaN(valE.value) || valE.value < 0 ) ) {
		alert( JSMessages.enternumbergreaterthanzeromsg );
		valE.value = '0.0';
		return false;
	}
	return true;
}

function validateKioskOwnerDetails( addFunction )
{
	var useridFlag = false;
	var passwordFlag = false;
	var confirmPasswordFlag = false;

	// Mandatory attributes
	// If the functionality is add, then the foloowing flags are set based on the user input in the addkioskowner form
	if( addFunction )
	{
		useridFlag = validateUserId( document.getElementById( "userid" ), JSMessages.userid, 3, 30, 'useridStatus', true );
		passwordFlag = validatePassword( document.getElementById( "password" ), JSMessages.password, 4, 20, 'passwordStatus', true );
		confirmPasswordFlag = validatePassword( document.getElementById( "confirmpassword" ), JSMessages.confirmpassword, 4, 20, 'confirmpasswordStatus', true );
	}
	else // If the functionality is edit, then the user id, password and confirmpassword fields are preset, i.e they are not read from the form.
	{
		useridFlag = true;
		passwordFlag = true;
		confirmPasswordFlag = true;
	}

	var roleFlag = validateSelectBox( document.getElementById("role").options[document.getElementById("role").selectedIndex].value , JSMessages.userrole, 'roleStatus', true );
	var firstNameFlag = validateName( document.getElementById ("firstname"), JSMessages.userfirstname, 1, 40, 'firstnameStatus', true );
	var lastNameFlag = validateName( document.getElementById ("lastname"), JSMessages.userlastname, 0, 40, 'lastnameStatus', false );
	var mobilePhoneFlag = validateMobilephone( document.getElementById("mobilephone"), JSMessages.usermobile, 1, 20, 'mobilephoneStatus', true);
	var timezoneFlag = validateSelectBox( document.getElementById("timezone").options[ document.getElementById("timezone").selectedIndex].value, JSMessages.preferredtimezone, 'timezoneStatus', true );
	var countryFlag = validateTextField( document.getElementById("countries"), JSMessages.country, 1, 500, 'countryStatus', true );
	var stateFlag = validateTextField( document.getElementById("states"), JSMessages.state, 1, 500, 'stateStatus', true );

	// Optional attributes
	var districtFlag = validateTextField( document.getElementById( "districts" ), JSMessages.district, 1, 500, 'districtStatus', false );
	var talukFlag = validateTextField( document.getElementById( "taluks" ), JSMessages.taluk, 1, 500, 'talukStatus', false );

	var cityFlag = validateTextField( document.getElementById( "city" ), JSMessages.village, 0, 30, 'cityStatus', false );
	var pinCodeFlag = validatePincode( document.getElementById( "pincode" ), JSMessages.zipcode, 0, 10, 'pincodeStatus', false);
	var addressFlag = validateTextField( document.getElementById( "street" ) , JSMessages.address, 0, 50, 'streetStatus', false);
	//var phoneBrandFlag = validateTextField( document.getElementById( "phonebrand" ), 'Mobile Phone Brand', 0, 50, 'phonebrandStatus', false );
	//var phoneModelNumberFlag = validateTextField( document.getElementById( "phonemodelnumber" ), 'Mobile Phone Model', 0, 50, 'phonemodelnumberStatus', false );
	//var phoneServiceProviderFlag = validateTextField( document.getElementById( "phoneserviceprovider" ), 'Mobile Phone Operator', 0, 50, 'phoneserviceproviderStatus', false );
	var landlineNumberFlag = true; /// NO NEED TO VALIDATE THIS: validateLandlineNumber( document.getElementById( "landlinenumber" ), JSMessages.userlandline, 0, 20, 'landlinenumberStatus', false );
	// If role is kioskowner email is optional. Otherwise, email is mandatory.
	var emailidth = document.getElementById( "emailheader" );
	var emailidthClassName = emailidth.className;
	var emailFlag = false;
	if ( emailidthClassName == "mandatory" ) {
		emailFlag = validateEmail(document.getElementById("email"), JSMessages.useremail, 0, 50, 'emailStatus', true);
	} else {
		emailFlag = validateEmail(document.getElementById("email"), JSMessages.useremail, 0, 50, 'emailStatus', false);
	}
	var ageFlag = validateAge( document.getElementById( "age" ), JSMessages.userage, 0, 3, 'ageStatus', false );

	if( useridFlag && passwordFlag && confirmPasswordFlag && roleFlag && firstNameFlag && lastNameFlag && mobilePhoneFlag && timezoneFlag && countryFlag && stateFlag && districtFlag && talukFlag && cityFlag && pinCodeFlag && addressFlag && landlineNumberFlag && emailFlag && ageFlag)
		return true;
	else
		return false;
}

/*
 * This method is called on the click of the submit button. Returns true if form is validated. Returns false otherwise.
 */
function validateMaterialDetails()
{
	// Mandatory attribute
	var materialNameFlag = validateName( document.getElementById( "materialname" ) , JSMessages.materialname, 1, 75, 'materialnameStatus', true );
	// Optional attribute
	var descriptionFlag = validateTextField( document.getElementById( "description" ) , JSMessages.description, 0, 80, 'descriptionStatus' , false);
	//var tagsFlag = validateCSV( document.getElementById( "tags" ), 'Tags', 0, 100, 'tagsStatus', false );
	var retailpriceFlag = validatePrice( document.getElementById( "retailprice" ), JSMessages.materialmsrp, 0, 10, 'retailpriceStatus' , false);
	var retailerpriceFlag = validatePrice( document.getElementById( "retailerprice" ),JSMessages.materialretailerprice, 0, 10, 'retailerpriceStatus' , false);

	if( materialNameFlag && tagsFlag && retailpriceFlag && retailerpriceFlag )
		return true;
	else
		return false;
}

/*
 * Generalized name validation function
 * This function can be used to validate text fields.
 */
function validateTextField( element, displayName, minLength, maxLength, statusId, mandatory )
{
	var elementValue = element.value;
	var tempStatusString = displayName;
	// var fieldRegex = new RegExp("^(([a-zA-Z\\d\\s\\-\#\\,\\.\\/])*){3,50}$");

	// Trim the elementValue
	if ( elementValue )
		elementValue = trim( elementValue );

	// If the value being validated is mandatory, then do the null check.
	if ( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( elementValue, statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if ( (!mandatory ) &&  elementValue == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}
	// Check for min-max.
	if ( elementValue != '' && ( elementValue.length < minLength || elementValue.length > maxLength ) ) {
		setStatusMsg( statusId, tempStatusString + ' ' + JSMessages.isinvalid + '. ' + minLength + '-' + maxLength + ' ' + JSMessages.charactersallowed, 2 );
		return false;
	}

	// Set valid status
	setStatusMsg( statusId,'', 1 );

	return true;
}

/*
 * General validation function for name.
 *
 */
function validateName( nameElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var name = nameElement.value;
	var tempStatusString = displayName;

	// var nameRegex = new RegExp("^([a-zA-Z\\d\\s\\.]+(([\\'\\,\\.\\-\\d\\s\\/])?[a-zA-Z]*)*){"+ minLength + "," + maxLength + "}$");

	// Trim the name
	if( name )
		name = trim( name );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( name, statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  name == '' ) {
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check for min-max.
	if ( name != '' && ( name.length < minLength || name.length > maxLength ) ) {
		setStatusMsg( statusId, tempStatusString + ' ' + JSMessages.isinvalid + '. ' + minLength + '-' + maxLength + ' ' + JSMessages.charactersallowed, 2 );
		return false;
	}

	// Name is valid
	setStatusMsg( statusId,'', 1 );

	return true;

}

function validateMobilephones(mobilephoneElement, displayName, minLength, maxLength, statusId, mandatory) {
	var inputString = mobilephoneElement.value;

	var parsedString = inputString.split(",");

	for (var i = 0; i < parsedString.length; i++) {
		var status = validateMobilephone(parsedString[i], displayName, minLength, maxLength, statusId, mandatory);
		if (!status)
			return status;
	}

	return true;
}

/*
 * General validation function for mobile number.
 *
 */
function validateMobilephone( mobilephoneElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var mobilephone = mobilephoneElement;

	if (toString.call(mobilephoneElement) != '[object String]')
		mobilephone = mobilephoneElement.value;

	var tempStatusString = displayName;

	var mobilephoneRegex = new RegExp("^(\\+{1})\\d{1,4}\\s\\d{" + minLength + "," + maxLength + "}$");

	// Trim the mobilephone number
	if( mobilephone )
		mobilephone = trim( mobilephone );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( mobilephone , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the field value is null, do not display any message.
	// Return true.
	if ( (!mandatory ) &&  mobilephone == '' ) {
		setStatusMsg( statusId,'', 0 );
		return true;
	}


	// Check if mobilephone number matches with the regular expression.
	if ( !mobilephone.match(mobilephoneRegex) ) {
		// Check if mobilephone number contains a space. If not, display an appropriate error message.
		if ( mobilephone.search( /\s/ ) == -1) {
			setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.phoneformatmsg + '.', 2);
		} else {
			setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.followformatmsg + '.', 2);
		}
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

function validateMobilephonesForTempMonitoring(mobilephoneElement, displayName, minLength, maxLength, statusId, mandatory) {
    var inputString = mobilephoneElement.value;

    var parsedString = inputString.split(",");

    for (var i = 0; i < parsedString.length; i++) {
        var status = validateMobilephoneForTempMonitoring(parsedString[i], displayName, minLength, maxLength, statusId, mandatory);
        if (!status)
            return status;
    }

    return true;
}

/*
 * General validation function for mobile number.
 *
 */
function validateMobilephoneForTempMonitoring( mobilephoneElement, displayName, minLength, maxLength, statusId, mandatory )
{
    var mobilephone = mobilephoneElement;

    if (toString.call(mobilephoneElement) != '[object String]')
        mobilephone = mobilephoneElement.value;

    var tempStatusString = displayName;

    var mobilephoneRegex = new RegExp("^(\\+{1})\\d{" + minLength + "," + maxLength + "}$");

    // Trim the mobilephone number
    if( mobilephone )
        mobilephone = trim( mobilephone );

    // If the value being validated is mandatory, then do the null check.
    if( mandatory ){
        // Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
        if(!validateIfNull( mobilephone , statusId, tempStatusString ))
            return false;
    }

    // If the field being validated is optional, and the field value is null, do not display any message.
    // Return true.
    if ( (!mandatory ) &&  mobilephone == '' ) {
        setStatusMsg( statusId,'', 0 );
        return true;
    }


    // Check if mobilephone number matches with the regular expression.
    if ( !mobilephone.match(mobilephoneRegex) ) {
        // Check if mobilephone number contains a space. If not, display an appropriate error message.
        setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.followformatmsg + '.', 2);
        return false;
    }

    setStatusMsg( statusId,'', 1 );

    return true;

}

/*
 * General validation function for landline phone number.
 *
 */
function validateLandlineNumber( landlinenumberElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var landlinenumber = landlinenumberElement.value;
	var tempStatusString = displayName;

	var landlinenumberRegex = new RegExp("^(\\+{1})\\d{1,4}\\s\\d{" + minLength + "," + maxLength + "}$"); // +<country-code> <area-code> <phone-number>

	// Trim the landlinenumber
	if( landlinenumber )
		landlinenumber = trim( landlinenumber);

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( landlinenumber , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  landlinenumber == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if landlinenumber matches with the regular expression.
	if ( !landlinenumber.match(landlinenumberRegex) ) {
		// Check if landlinenumber contains a space. If not, display an appropriate error message.
		if ( landlinenumber.search( /\s/ ) == -1) {
			setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.phoneformatmsg + '.', 2);
		} else {
			setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.followformatmsg + '.', 2);
		}
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * General validation function for Pincode.
 *
 */
function validatePincode( pincodeElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var pincode = pincodeElement.value;
	var tempStatusString = displayName;

	//var pincodeRegex = new RegExp("^[A-Za-z\\d\\s\\-]{" + minLength + "," + maxLength + "}$");
	var pincodeRegex = new RegExp("^[A-Za-z\\xBF-\\xFF\\d\\s\\-]{" + minLength + "," + maxLength + "}$");

	// Trim the pincode
	if( pincode )
		pincode = trim( pincode );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( pincode , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  pincode == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if pincode matches with the regular expression.
	if(!pincode.match(pincodeRegex) ){
		setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.lettersnumbershyphenallowed + '.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * General validation function for Age.
 *
 */
function validateAge( ageElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var age = ageElement.value;
	var tempStatusString = displayName;

	var ageRegex = new RegExp("^\\d{" + minLength + "," + maxLength + "}$");

	// Trim the age
	if( age )
		age = trim( age );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( age , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  age == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if age matches with the regular expression.
	if(!age.match(ageRegex) ){
		setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.useragevalidationmsg + '.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * General validation function for Email.
 *
 */
function validateEmail( emailElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var email = emailElement.value;
	var tempStatusString = displayName;

	//var emailRegex = new RegExp("^([a-zA-Z\\d]+(([\\@\\.\\-\\_ ])?[a-zA-Z]*)*){" + minLength + "," + maxLength + "}$");
	var emailRegex = new RegExp("^([a-zA-Z0-9\\xBF-\\xFF\\d]+(([\\@\\.\\-\\_ ])?[a-zA-Z0-9\\xBF-\\xFF]*)*){" + minLength + "," + maxLength + "}$");

	// Trim the email
	if( email )
		email = trim( email );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( email , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the field value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  email == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if email matches with the regular expression.
	if(!email.match(emailRegex) ){
		setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.useridvalidationmsg + '.', 2);
		//setStatusMsg( statusId , tempStatusString + ' is invalid. Only letters, numbers or characters @ or . or _ or - is allowed.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * General validation function for Geo.
 *
 */
function validateGeo( geoElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var geo = geoElement.value;
	var tempStatusString = displayName;
	// var geoRegex = new RegExp("^[-+]?\\d+(\\.\\d+)?$");

	// Trim the geo value
	if( geo )
		geo = trim( geo );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( geo , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the field value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  geo == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if geo matches with the regular expression.
	// if(!geo.match(geoRegex) ){
	if( isNaN( geo ) ){
		setStatusMsg( statusId , tempStatusString + ' is invalid. Only numbers are allowed.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * General validation function for price.
 */
function validatePrice( priceElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var price = priceElement.value;
	var tempStatusString = displayName;

	// var priceRegex = new RegExp("^\\d+(\\.\\d+)?$");

	// Trim the price
	if( price )
		price = trim( price );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( price , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  price == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

    // Check if price matches with the regular expression.
	// if(!price.match( priceRegex ) ){
	if( isNaN( price ) ){
		setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.onlynumbersallowed + '.', 2);
		// setStatusMsg( statusId , tempStatusString + ' is invalid. Only numbers are allowed.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * General validation function for Comma separated values.
 */
function validateCSV( csvElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var csv = csvElement.value;
	var tempStatusString = displayName;

	//var csvRegex = new RegExp("^([a-z0-9\\s\\.])+(,[a-z0-9\\s\\.]+)*$");
	var csvRegex = new RegExp("^([a-zA-Z0-9\\xBF-\\xFF\\s\\.])+(,[a-zA-Z0-9\\xBF-\\xFF\\s\\.]+)*$");

	// Trim the csv
	if( csv )
		csv = trim( csv );

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( csv , statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  csv == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if csv matches with the regular expression.
	if(!csv.match( csvRegex ) ){
		setStatusMsg( statusId , tempStatusString + ' is invalid. Alpha-numeric words separated by comma is allowed.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * User Id is a mandatory field. So, validation is required. User Id should be between 3 and 20 characters in length.
 */
function validateUserId( useridElement , displayName, minLength, maxLength , statusId , mandatory )
{
	var tempStatusString = displayName;
	var userid = useridElement.value ;

	//var useridRegex = new RegExp ("^([a-zA-Z0-9]+(([\\_\\.\\-\\@])?[a-zA-Z0-9]*)*){" + minLength + "," + maxLength + "}$"); // minimum of 3 characters, maximum of 20 characters.
	// Regex that allows accentuated characters as well
	var useridRegex = new RegExp ("^([a-zA-Z0-9\\xBF-\\xFF]+(([\\_\\.\\-\\@])?[a-zA-Z0-9\\xBF-\\xFF]*)*){" + minLength + "," + maxLength + "}$"); // minimum of 3 characters, maximum of 20 characters.

	// Trim and lowercase the userid and set it back to the element
	if( userid ) {
		userid = trim( userid );
		userid = userid.toLowerCase();
		useridElement.value = userid;
	}

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( userid, statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  userid == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check for min-max.
	if ( userid != '' && ( userid.length < minLength || userid.length > maxLength ) ) {
		setStatusMsg( statusId, tempStatusString + ' ' + JSMessages.isinvalid + '. ' + minLength + '-' + maxLength + ' ' + JSMessages.charactersallowed, 2 );
		return false;
	}

	if(!userid.match(useridRegex) ){
		setStatusMsg( statusId , tempStatusString + ' ' + JSMessages.isinvalid + '. ' + JSMessages.useridvalidationmsg + '.', 2);
		return false;
	}

	setStatusMsg( statusId,'', 1 );

	return true;

}

/*
 * Password is a mandatory field. So, validation is required. Password should not be less than 4 characters and not more than 10 characters long.
 * It can include special characters.
 * The same function is used to validate confirm password also. The same rules of validation apply to confirm password also.
 * One additional check that is done when the confirm password is checked is, that password and confirm password should match. Otherwise, an error message is displayed.
 */
function validatePassword( passwordElement, displayName, minLength, maxLength, statusId, mandatory )
{
	var passwordValue = passwordElement.value;
	var id = passwordElement.id;
	var tempStatusString = (id == "password")? displayName : JSMessages.confirmpassword ;
	var compValue = ( id == "password") ? document.getElementById("confirmpassword").value : document.getElementById("password").value;

	// If the value being validated is mandatory, then do the null check.
	if( mandatory ){
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if(!validateIfNull( passwordValue, statusId, tempStatusString ))
			return false;
	}

	// If the field being validated is optional, and the field value is null, do not display any message.
	// Return true.
	if( (!mandatory ) &&  password == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// If password is less than minLength or more than maxLength characters long, display an error message.
	if( passwordValue.length < minLength || passwordValue.length  > maxLength ){
		setStatusMsg( statusId, id == "password" ? JSMessages.password + " " + JSMessages.isinvalid + ". " + JSMessages.passwordvalidationmsg + ": " + minLength + "-" + maxLength  : JSMessages.confirmpasswordmsg, 2);
		return false;
	}

	if( id == "confirmpassword"){ // Only if confirm password is validated
		if (passwordValue != compValue) {
			setStatusMsg('confirmpasswordStatus', JSMessages.confirmpasswordmsg, 2);
			return false;
		}
	}

	setStatusMsg( statusId ,'', 1 );
	return true;

}

/*
 * Validate select box
 */
function validateSelectBox(optionValue, displayName, statusId, mandatory)
{
	// As this function can be called by any select box that needs validation, in order to set the appropriate error message,
	// set the temporaryStatusString to the display name of the select box.

	var tempStatusString = displayName;

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if( (!mandatory ) && optionValue == '' ){
		setStatusMsg( statusId,'', 0 );
		return true;
	}

	// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
	if(!validateIfNull( optionValue, statusId, tempStatusString ))
		return false;

	// Otherwise, a tick mark is displayed next to the field. And return true.
	setStatusMsg( statusId,'',1);
	return true;
}

function validateIfNull( fieldValue, fieldStatusId, tempStatusString  )
{
	// If the field value is null, display a cross and an error message. And return false.
	if( fieldValue == '') {
		setStatusMsg( fieldStatusId, tempStatusString + ' ' + JSMessages.isrequired, 2);
		return false;
	}
	// fieldValue is not null. So, return true.
	return true;

}

/*
 * This is a utility function to trim a string on either sides.
 */
function trim( stringToTrim ) {
	if( stringToTrim )
		return stringToTrim.replace(/^\s+|\s+$/g,"");
	else
		return "";
}

function setStatusMsg(element, msg, status)
{
	var valid = false;
	var div;
	div = document.getElementById( element );
	if( status == 0 || status == 1 ) {
        // Do not display the div element
		div.style.display = 'none'; // This setting hides the div element
		valid = true;
	} else {
		// Display span element with a cross
		div.style.display = ''; // This setting displays the div element
		div.innerHTML = '<font color = "red"><b>'+ msg + '</b></font>';
		valid = false;
	}
	return valid;
}

function isAlphaNumeric( str, allowSpace ) {
	if ( !str || str == null || str == '' ) {
		return true;
	}
	var alphanum=/^[0-9a-zA-Z]+$/; // This contains A to Z , 0 to 9, a to z and space
	if ( allowSpace )
		alphanum=/^[0-9a-zA-Z\s]+$/;
	if ( str.match( alphanum ) ) {
		return true;
	} else {
		return false;
	}
}

// Clean \n, \r and \t, and replace with whitespace
function cleanSpaces( str ) {
	if ( !str || str == '' )
		return str;
	var s = str.replace(/\n/g, " " );
	s = s.replace(/\r/g, " " );
	s = s.replace(/\t/g, " " );
	return s;
}

/*
 * This method is called on the click of the submit button. Returns true if form is validated. Returns false otherwise.
 */
function validateDeviceConfigurationDetails() {
	if (document.getElementById("temp_no").checked) {
		return true;
	}

	var cfgurlFlag = true, almurlFlag = true, statsurlFlag = true, devryurlFlag = true;
	//var commchnlFlag = validateName(document.getElementById("chnl"), JSMessages.temperature_device_config_commchnl, 1, 1, 'chnlStatus', true);
	var tmpurlFlag = validateURL(document.getElementById("tmpurl"), JSMessages.temperature_device_config_tmpurl, 10, 500, 'tmpurlStatus', true);
	cfgurlFlag = validateURL(document.getElementById("cfgurl"), JSMessages.temperature_device_config_cfgurl, 10, 500, 'cfgurlStatus', false);
    almurlFlag = validateURL(document.getElementById("almurl"), JSMessages.temperature_device_config_almurl, 10, 500, 'almurlStatus', true);

	if (document.getElementById("statsnotify").checked)
		statsurlFlag = validateURL(document.getElementById("statsurl"), JSMessages.temperature_device_config_statsurl, 10, 500, 'statsurlStatus', true);
	else
		statsurlFlag = validateURL(document.getElementById("statsurl"), JSMessages.temperature_device_config_statsurl, 10, 500, 'statsurlStatus', false);

	devryurlFlag = validateURL(document.getElementById("devryurl"), JSMessages.temperature_device_config_devryurlurl, 10, 500, 'devryurlStatus', false);

	var smsGyPhnFlag = validateMobilephoneForTempMonitoring(document.getElementById("smsgyph"), JSMessages.temperature_device_config_smsGyPh, 4, 20, 'smsgyphStatus', true);
	var samplingIntFlag = validateNumberWithRange(document.getElementById("samplingint"), JSMessages.temperature_device_config_sint, 1, 1440, 'samplingintStatus', true);
	var pushIntFlag = validateNumberWithRange(document.getElementById("pushint"), JSMessages.temperature_device_config_pint, 1, 1440, 'pushintStatus', true);
	var usrPhonesFlag = validateMobilephonesForTempMonitoring(document.getElementById("usrphones"), JSMessages.temperature_device_config_usrphones, 4, 20, 'usrphonesStatus', false);
	//var timezoneFlag = validateNumberWithRange(document.getElementById("timezone"), JSMessages.timezone, -12, 12, 'timezoneStatus', false);
	//var countryFlag = validateAlpha(document.getElementById("country"), JSMessages.country, 2, 2, 'countryStatus', false);
	//var languageFlag = validateBinary(document.getElementById("language"), JSMessages.language, 0, 1, 'languageStatus', false);
    var highAlarmTempFlag = validateNumberWithRange(document.getElementById("highalarmtemp"), JSMessages.temperature, -100, 100, 'highalarmtempStatus', true);
    var lowAlarmTempFlag = validateNumberWithRange(document.getElementById("lowalarmtemp"), JSMessages.temperature, -100, 100, 'lowalarmtempStatus', true);
    //var highWarnTempFlag = validateNumberWithRange(document.getElementById("highwarntemp"), JSMessages.temperature, -100, 100, 'highwarntempStatus', false);
    //var lowWarnTempFlag = validateNumberWithRange(document.getElementById("lowwarntemp"), JSMessages.temperature, -100, 100, 'lowwarntempStatus', false);
    var highAlarmDurFlag = validateNumberWithRange(document.getElementById("highalarmdur"), JSMessages.duration, 1, 1440, 'highalarmdurStatus', true);
    var lowAlarmDurFlag = validateNumberWithRange(document.getElementById("lowalarmdur"), JSMessages.duration, 1, 1440, 'lowalarmdurStatus', true);
    //var highWarnDurFlag = validateNumberWithRange(document.getElementById("highwarndur"), JSMessages.duration, 1, 1440, 'highwarndurStatus', false);
    //var lowWarnDurFlag = validateNumberWithRange(document.getElementById("lowwarndur"), JSMessages.duration, 1, 1440, 'lowwarndurStatus', false);
    var excursionFilterFlag = validateNumberWithRange(document.getElementById("excursionFilterDur"), JSMessages.temperature_device_config_filter_excursion, 0, 14400, 'excursionFilterDurStatus', false);
    var alarmFilterFlag = validateNumberWithRange(document.getElementById("alarmFilterDur"), JSMessages.temperature_device_config_filter_alarms, 0, 14400, 'alarmFilterDurStatus', false);
    var noDataFilterFlag = validateNumberWithRange(document.getElementById("noDataFilterDur"), JSMessages.temperature_device_config_filter_nodata, 0, 14400, 'noDataFilterDurStatus', false);

	if (tmpurlFlag && cfgurlFlag && almurlFlag && statsurlFlag && devryurlFlag && smsGyPhnFlag && samplingIntFlag && pushIntFlag
        && usrPhonesFlag && highAlarmTempFlag && lowAlarmTempFlag
        && highAlarmDurFlag && lowAlarmDurFlag
        && excursionFilterFlag && alarmFilterFlag && noDataFilterFlag)
		return true;
	else
		return false;
}

function validateBinary(nameElement, displayName, min, max, statusId, mandatory) {
	var inputNumber = nameElement.value;
	var statusName = displayName;

	// Trim the number
	if (inputNumber)
		inputNumber = trim(inputNumber);

	// If the value being validated is mandatory, then do the null check.
	if (mandatory) {
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if (!validateIfNull(inputNumber, statusId, statusName))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if ((!mandatory ) && inputNumber == '') {
		setStatusMsg(statusId, '', 0);
		return true;
	}

	if (isNaN(inputNumber)) {
		setStatusMsg(statusId, statusName + ' ' + JSMessages.isinvalid + '. ' + JSMessages.entervalidnumbermsg + '.', 2);
		return false;
	}

	// Check if number matches with the regular expression.
	if (inputNumber < min || inputNumber > max) {
		setStatusMsg(statusId, statusName + ' ' + JSMessages.isinvalid + '. ' + JSMessages.entervalidnumbermsg + '(0/1).', 2);
		return false;
	}

	setStatusMsg(statusId, '', 1);

	return true;
}

function validateURL(nameElement, displayName, min, max, statusId, mandatory) {
	var urlRegex = /^(?:(?:https?):\/\/)(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?$/i;

	var inputString = nameElement.value;
	var statusName = displayName;

	// Trim the number
	if (inputString)
		inputString = trim(inputString);

	// If the value being validated is mandatory, then do the null check.
	if (mandatory) {
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if (!validateIfNull(inputString, statusId, statusName))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if ((!mandatory ) && inputString == '') {
		setStatusMsg(statusId, '', 0);
		return true;
	}

	// Check if number matches with the regular expression.
	if (inputString.length < min || inputString.length > max || !inputString.match(urlRegex)) {
		setStatusMsg(statusId, statusName + ' ' + JSMessages.isinvalid + '. ', 2);
		return false;
	}

	setStatusMsg(statusId, '', 1);

	return true;
}

function validateNumberWithRange(nameElement, displayName, min, max, statusId, mandatory) {
	var inputNumber = nameElement.value;
	var statusName = displayName;

	// Trim the number
	if (inputNumber)
		inputNumber = trim(inputNumber);

	// If the value being validated is mandatory, then do the null check.
	if (mandatory) {
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if (!validateIfNull(inputNumber, statusId, statusName))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if ((!mandatory ) && inputNumber == '') {
		setStatusMsg(statusId, '', 0);
		return true;
	}

	if (isNaN(inputNumber)) {
		setStatusMsg(statusId, statusName + ' ' + JSMessages.isinvalid + '. ' + JSMessages.entervalidnumbermsg + '.', 2);
		return false;
	}

	// Check if number matches with the regular expression.
	if (inputNumber < min || inputNumber > max) {
		setStatusMsg(statusId, statusName + ' ' + JSMessages.isinvalid + '. ' + JSMessages.entervalidnumbermsg + '.', 2);
		return false;
	}

	setStatusMsg(statusId, '', 1);

	return true;
}


/*
 * General validation function for name.
 *
 */
function validateAlpha(nameElement, displayName, minLength, maxLength, statusId, mandatory) {
	var name = nameElement.value;
	var tempStatusString = displayName;
	var alphaRegex = /^[a-zA-Z]+$/;

	// var nameRegex = new RegExp("^([a-zA-Z\\d\\s\\.]+(([\\'\\,\\.\\-\\d\\s\\/])?[a-zA-Z]*)*){"+ minLength + "," + maxLength + "}$");

	// Trim the name
	if (name)
		name = trim(name);

	// If the value being validated is mandatory, then do the null check.
	if (mandatory) {
		// Check if the field value is null. If it null, then a cross mark is displayed next to that field. And return false.
		if (!validateIfNull(name, statusId, tempStatusString))
			return false;
	}

	// If the field being validated is optional, and the filed value is null, do not display any message.
	// Return true.
	if ((!mandatory ) && name == '') {
		setStatusMsg(statusId, '', 0);
		return true;
	}

	// Check for min-max.
	if (name != '' && ( name.length < minLength || name.length > maxLength )) {
		setStatusMsg(statusId, tempStatusString + ' ' + JSMessages.isinvalid + '.' + maxLength + ' ' + JSMessages.charactersallowed, 2);
		return false;
	}

	if (name != '' && !name.match(alphaRegex)) {
		setStatusMsg(statusId, tempStatusString + ' ' + JSMessages.isinvalid, 2);
		return false;
	}

	// Name is valid
	setStatusMsg(statusId, '', 1);

	return true;

}