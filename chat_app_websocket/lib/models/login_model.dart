import 'package:chat_app_websocket/models/user_details_model.dart';

class LoginModel {

  UserDetailsModel details;

  LoginModel({this.details})
    :assert(details != null 
    && details.password != null 
    && (details.emailID != null || details.username != null));

  Map<String, dynamic> toJson () => {
    "request": "login",
    "details": details
  };

}