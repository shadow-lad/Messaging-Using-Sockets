import '../models/user_details.dart';

class RegistrationModel {

  UserDetails details;

  RegistrationModel({this.details})
    :assert(details != null 
    && details.hasPassword()
    && details.hasEmail() 
    && details.hasUsername());

  Map<String, dynamic> toJson () => {
    "type": "registration",
    "details": details.toJson()
  };

}