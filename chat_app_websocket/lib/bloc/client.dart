import 'dart:async';
import 'dart:collection';
import 'dart:convert';
import 'dart:io';

import 'package:chat_app_websocket/database/database.dart';
import 'package:chat_app_websocket/models/message_model.dart';
import 'package:chat_app_websocket/models/user_details.dart';
import 'package:flutter/cupertino.dart';

class Client {

  static Client _instance;

  UserDetails _details;
  Socket _socket;
  bool _loggedIn;

  //Registration and otp verification
  String _registrationMessage;
  final _otpSentStateController = StreamController<bool>();
  StreamSink<bool> get _inOtpSent => _otpSentStateController.sink;
  Stream<bool> get otpSentState => _otpSentStateController.stream;
  final _otpVerificationStateController = StreamController<bool>();
  StreamSink<bool> get _inOtpVerify => _otpVerificationStateController.sink;
  Stream<bool> get otpVerifySuccess => _otpVerificationStateController.stream;
  String get registrationMessage => this._registrationMessage;

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

  //Explicitly for private chats
  final _chatStateController = StreamController<List<Message>>();
  StreamSink<List<Message>> get _chatCounter => _chatStateController.sink;
  Stream<List<Message>> get chats => _chatStateController.stream;

  //To get the list of messages
  List<Message> msgs;
//  Message lastSent;
  Queue<Message> lastSent = new Queue();
//
//  final _chatEventController = StreamController<chat_event>();
//  Sink<chat_event> get chatEventsink => _chatEventController.sink;

  static Client get instance {
    if (_instance == null) {
      _instance = Client._();
    }
    return _instance;
  }

  void getPreviousMessages(String userToSend) async{
    msgs = await DatabaseProvider.db.getMessagesByUser(userToSend);
    _chatCounter.add(msgs);
  }

  Client._() {
    initializeSocket();
    this._loggedIn = false;
    this._userList = [];
    msgs = [];
  }

  initializeSocket() async {
    print("CLIENT: Connecting to Server");
    _socket = await Socket.connect("127.0.0.1", 6969);
    print("CLIENT: Connected to server");
    _socket.listen((data) => handleJson(String.fromCharCodes(data)));
    print("CLIENT: Listening for input from server");
  }

  sendJson(Map<String, dynamic> json) async {
    _socket.writeln(jsonEncode(json));
  }

  sendPrivateMessage(Map<String,dynamic> message) async{
    _socket.writeln(jsonEncode(message));
    //Save to database
    Message obj = Message.fromJson(message);
    obj.from = _details.email;
    lastSent.addLast(obj);
  }

  handleJson(String input){
    var inputs = input.split("\n");
    for(String inp in inputs) {
      handleInput(inp);
    }
  }

  handleInput(String socketInput) {

    Map<String, dynamic> response = json.decode(socketInput);

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
      case 'message':
        print(response);
        switch(event){
          case 'personal':
//            print("Recieved message : ${response}");
            Message msg = Message.fromJson(response);
//            print("Recieved message : ${msg}");
            msgs.add(msg);
            _chatCounter.add(msgs);
            break;
          case 'sent':
            Message msg = lastSent.removeFirst();
            msg.id = response['id'];
            //Now save it to the database
            DatabaseProvider.db.insertMessage(msg);
            //Add to the message list
            msgs.add(msg);
            //Send the list to the sink
            _chatCounter.add(msgs);
            break;
        }
       break;
        case 'registration':
          registrationFailed(response);
          _inOtpSent.add(false);
          break;
        case 'otp':
          if (event == 'sent') {
            _registrationMessage = response['message'];
            _inOtpSent.add(true);
          } else {
            registrationFailed(response);
          }
          break;
        case 'verify':
          if (event == 'success') {
            _inOtpVerify.add(true);
          } else {
            _registrationMessage = response['message'];
            _inOtpVerify.add(false);
          }
          break;
    }
  }

  registrationFailed(Map<String, dynamic> response) {
    _registrationMessage = response['message'];
    _inOtpSent.add(false);
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
    _chatStateController.close();
  }

}
