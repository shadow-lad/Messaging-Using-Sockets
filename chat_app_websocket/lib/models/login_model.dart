import '../models/user_details.dart';

class LoginModel {

  UserDetails details;

  LoginModel({this.details})
    :assert(details != null 
    && details.hasPassword()
    && (details.hasEmail() || details.hasUsername()));

  Map<String, dynamic> toJson () => {
    "type": "login",
    "details": details.toJson()
  };

}