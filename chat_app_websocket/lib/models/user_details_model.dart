class UserDetailsModel {
  final String username;
  final String emailID;
  final String password;

  UserDetailsModel({this.username, this.emailID, this.password});

  Map<String, dynamic> toJson() =>
      {"email": emailID, "password": password, "username": username};
}
