class UserDetails {
  bool _loggedIn;
  final String _username;
  final String _emailID;
  final String _password;

  UserDetails(this._username, this._emailID, this._password) {
    this._loggedIn = false;
  }

  Map<String, dynamic> toJson() => {
        "email": this._emailID,
        "password": this._password,
        "username": this._username
  };

  bool hasUsername() {
    return this._username != null && this._username.isNotEmpty;
  }

  bool hasEmail() {
    return this._emailID != null && this._emailID.isNotEmpty;
  }

  bool hasPassword() {
    return this._password != null && this._password.isNotEmpty;
  }

  bool isLoggedIn() {
    return this._loggedIn;
  }

  String get email => this._emailID;

  String get username => this._username;

}
