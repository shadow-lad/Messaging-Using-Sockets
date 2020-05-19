import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';
import 'package:chat_app_websocket/models/login_model.dart';
import 'package:chat_app_websocket/models/user_details_model.dart';
import 'package:flutter/material.dart';
import '../ChatRoom/chat_room.dart';
import 'registrationScreen.dart';

class LoginScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return new LoginScreenState();
  }
}

class LoginScreenState extends State<LoginScreen> {
  Socket socket;

  StreamSubscription<Uint8List> socketListener;

  void socketInitialize() async {
    print("DEBUG: Connecting to server");
    socket = await Socket.connect('192.168.43.173', 6969);
    socketListener = socket.listen(null);
    print("DEBUG: Connection established to server");
    print(socket.toString());
  }

  String userMail, userName;
  final TextEditingController userEmailController = new TextEditingController();
  final TextEditingController userPasswordController = new TextEditingController();

  void signInUser(String mail, String password) {

    // This piece of code from here
    RegExp emailPattern = RegExp(r'^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$');
    UserDetailsModel userDetails;
    if (emailPattern.hasMatch(mail)) {
      userDetails = UserDetailsModel(emailID: mail, password: password, username: "");
    } else {
      userDetails = UserDetailsModel(emailID: "", password: password, username: mail);
    }
    // to here is used to check whether the user entered an email or password and make the userDetails object appropriately

    LoginModel loginModel = LoginModel(details: userDetails);

    print("DEBUG: Login details entered by user");
    socket.writeln(jsonEncode(loginModel));
    print("DEBUG: Login details sent to server");

    socketListener.onData((data) {
      String recievedMessage = new String.fromCharCodes(data).trim();
      
      print("DEBUG: Message received from server" + recievedMessage);

      Map<String, dynamic> response = jsonDecode(recievedMessage);
      if (response['status'] == 'success') {
        setState(() {
          userMail = response['details']['email'];
          userName = response['details']['username'];  
        });

        Navigator.of(context).pushReplacement(new MaterialPageRoute(
            builder: (BuildContext context) => new ChatRoom(
                  email: userMail,
                  name: userName,
                  socketListener: socketListener,
                  c_rsocket: socket,
                )));
      } else {
        print(response['message'].toString());
      }
    });

    print("DEBUG: Listening on socket");
  }

  @override
  void initState() {
    super.initState();
    socketInitialize();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        body: Container(
      height: MediaQuery.of(context).size.height,
      width: MediaQuery.of(context).size.width,
      child: ListView(
        children: <Widget>[
          //The images
          Container(
            height: MediaQuery.of(context).size.height * (1 / 2),
            width: MediaQuery.of(context).size.width,
            decoration: BoxDecoration(
                image: DecorationImage(
                  image: AssetImage('images/register_bg.jpg'),
                  fit: BoxFit.cover,
                ),
                borderRadius:
                    BorderRadius.only(bottomLeft: Radius.circular(210.0))),
          ),

          Container(
            height: MediaQuery.of(context).size.height -
                (MediaQuery.of(context).size.height * (1 / 2)),
            width: MediaQuery.of(context).size.width,
            margin: EdgeInsets.all(20.0),
            child: ListView(
              children: <Widget>[
                TextField(
                  controller: userEmailController,
                  decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      icon: Icon(
                        Icons.mail,
                        color: Colors.black,
                      ),
                      labelText: "Email",
                      labelStyle:
                          TextStyle(color: Colors.black, fontSize: 16.0)),
                  style: TextStyle(
                      color: Colors.black,
                      letterSpacing: 1.5,
                      fontWeight: FontWeight.w500),
                ),

                SizedBox(
                  height: 20.0,
                ),

                //Another textfield to get the password

                TextField(
                  controller: userPasswordController,
                  decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      icon: Icon(
                        Icons.lock,
                        color: Colors.black,
                      ),
                      labelText: "Password",
                      labelStyle:
                          TextStyle(color: Colors.black, fontSize: 16.0)),
                  style: TextStyle(
                    color: Colors.black,
                    letterSpacing: 1.5,
                    fontWeight: FontWeight.w500,
                  ),
                  obscureText: true,
                ),

                SizedBox(
                  height: 50.0,
                ),

                //Buttons for sign up and sign in

                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: <Widget>[
                    //Sign in button
                    GestureDetector(
                      child: Container(
                        height: 65.0,
                        width: 150.0,
                        decoration: BoxDecoration(
                            color: Colors.black,
                            shape: BoxShape.rectangle,
                            borderRadius:
                                BorderRadius.all(Radius.circular(15.0)),
                            boxShadow: [
                              BoxShadow(blurRadius: 3.0, color: Colors.black54)
                            ]),
                        child: Center(
                          child: Text(
                            "SIGN IN",
                            style: TextStyle(
                                color: Colors.white,
                                fontSize: 18.5,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 1.5),
                          ),
                        ),
                      ),
                      onTap: () {
                        signInUser(userEmailController.text.toString().trim(),
                            userPasswordController.text.toString().trim());
                      },
                    ),

                    //SIGN UP button
                    GestureDetector(
                      child: Container(
                        height: 65.0,
                        width: 150.0,
                        decoration: BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.rectangle,
                            borderRadius:
                                BorderRadius.all(Radius.circular(15.0)),
                            border:
                                Border.all(color: Colors.black54, width: 2.0)),
                        child: Center(
                          child: Text(
                            "SIGN UP",
                            style: TextStyle(
                                color: Colors.black,
                                fontSize: 18.5,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 1.5),
                          ),
                        ),
                      ),
                      onTap: () {
                        Navigator.of(context).push(new MaterialPageRoute(
                            builder: (BuildContext context) =>
                                RegistrationScreen(
                                  socket: socket,
                                )));
                      },
                    )
                  ],
                )
              ],
            ),
          )
        ],
      ),
    ));
  }
}
