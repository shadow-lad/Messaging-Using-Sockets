import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:chat_app_websocket/models/user_details.dart';

class Client {

  static Client _instance;

  UserDetails _details;
  Socket _socket;
  bool _loggedIn;

  // Explicitly for logging in
  String _loginErrorMessage;
  final _loginStateController = StreamController<bool>();
  StreamSink<bool>  get _login => _loginStateController.sink;
  Stream<bool> get loginState => _loginStateController.stream;
  bool get isLoggedIn => this._loggedIn;
  UserDetails get details => this._details;
  String get loginErrorMessage => this._loginErrorMessage;

  // Explicitly for User List
  List<UserDetails> _userList;
  final _userListStateController = StreamController<List<UserDetails>>();
  StreamSink<List<UserDetails>> get _users => _userListStateController.sink;
  Stream<List<UserDetails>> get userList => _userListStateController.stream;

  static Client get instance {
    if (_instance == null) {
      _instance = Client._();
    }
    return _instance;
  }

  Client._() {
    initializeSocket();
    this._loggedIn = false;
    this._userList = [];
  }

  initializeSocket() async {
    print("CLIENT: Connecting to Server");
    _socket = await Socket.connect("192.168.29.52", 6969);
    print("CLIENT: Connected to server");
    _socket.listen((data) => handleInput(String.fromCharCodes(data)));
    print("CLIENT: Listening for input from server");
  }

  sendJson(Map<String, dynamic> json) async {
    _socket.writeln(jsonEncode(json));
  }

  handleInput(String socketInput) {

    Map<String, dynamic> response = jsonDecode(socketInput);

    String event = response['event'];
    String type = response['type'];
    print('CLIENT: handleInput: Handling event $event of type $type');
    switch (type) {
      case 'login':
        switch (event) {
          case 'success':
            Map<String, dynamic> details = response['details'];
            this._details = UserDetails(details['username'], details['email'], null);
            _loggedIn = true;
            requestUsers();
            _login.add(isLoggedIn);
            break;
          case 'failed':
            _loginErrorMessage = response['message'];
            _loggedIn = false;
            _login.add(this._loggedIn);
          break;
        }
        break;
      case 'users':
        switch(event) {
          case 'success':
            List<dynamic> details = response['details'];
            this._userList.clear();
            print('CLIENT: Empty User List: ${this._userList}');
            for (dynamic user in details) {
              this._userList.add(UserDetails(user['username'], user['email'], null));
            }
            print('CLIENT: User List after processing: ${this._userList}');
            _users.add(this._userList);
            break;
        }
        break;
    }
  }

  sendUserRequest() {
    Map<String, dynamic> getUsers = {'type': 'users'};
    sendJson(getUsers);
    print('CLIENT: Request for User List sent');
  }

  requestUsers() {
    sendUserRequest();
    Timer.periodic(Duration(hours: 1), (timer) {
      sendUserRequest();
    });
  }

  // Ignore for now
  destroy() {
    _loginStateController.close();
    _userListStateController.close();
  }

}
