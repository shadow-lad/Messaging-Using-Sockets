import 'package:chat_app_websocket/models/user_details_model.dart';

class RegistrationModel {

  UserDetailsModel details;

  RegistrationModel({this.details})
    :assert(details != null 
    && details.password != null 
    && details.emailID != null 
    && details.username != null);

  Map<String, dynamic> toJson () => {
    "request": "registration",
    "details": details
  };

}