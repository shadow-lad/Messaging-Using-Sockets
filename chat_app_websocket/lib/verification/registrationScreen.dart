import 'dart:io';

import 'package:chat_app_websocket/verification/otp_verification_screen.dart';

import '../bloc/client.dart';

import '../models/registration_model.dart';
import '../models/user_details.dart';
import 'package:flutter/material.dart';

class RegistrationScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return new RegistrationScreenState();
  }
}

class RegistrationScreenState extends State<RegistrationScreen> {
  final TextEditingController registerMail = new TextEditingController();
  final TextEditingController registerPassword = new TextEditingController();
  final TextEditingController registerUserName = new TextEditingController();

  // For password visibility
  int vis = 0;

  final Client client = Client.instance;

  @override
  void initState() {
    super.initState();
    client.otpSentState.listen((isOtpSent) {
      if (isOtpSent) {
        showDialog(
            context: context,
            builder: (_) {
              return AlertDialog(
                title: Text("Email Sent"),
                content: Text(client.registrationMessage),
                actions: <Widget>[
                  FlatButton(
                    child: Text("OK"),
                    onPressed: () {
                      Navigator.of(context).pop(); // Dimiss Dialog
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => OtpVerificationScreen(),
                        ), // OTP Screen
                      );
                    },
                  )
                ],
              );
            });
      } else {
        showDialog(
            context: context,
            builder: (_) {
              return AlertDialog(
                title: Row(
                  children: [
                    Padding(
                        padding: EdgeInsets.fromLTRB(8, 0, 8, 0),
                        child: Icon(
                          Icons.error_outline,
                          color: Colors.red,
                        )),
                    Text("Error!")
                  ],
                ),
                content: Text(client.registrationMessage),
                actions: <Widget>[
                  FlatButton(
                      onPressed: () {
                        Navigator.of(context).pop(); // Dismiss Dialog
                      },
                      child: Text("OK"))
                ],
              );
            });
      }
    });
  }

  void signUp() {
    String username = registerUserName.text.toString().trim();
    String usermail = registerMail.text.toString().trim();
    String userpassword = registerPassword.text.toString().trim();

    UserDetails user = UserDetails(username, usermail, userpassword);

    RegistrationModel registrationModel = RegistrationModel(details: user);

    client.sendJson(registrationModel.toJson());
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      body: Container(
        padding: EdgeInsets.all(20.0),
        height: MediaQuery.of(context).size.height,
        width: MediaQuery.of(context).size.width,
        child: Center(
          child: Container(
            margin: EdgeInsets.only(
                top: MediaQuery.of(context).size.height * (1 / 4)),
            alignment: Alignment.center,
            child: Column(
              children: <Widget>[
                TextField(
                  controller: registerUserName,
                  decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      icon: Icon(
                        Icons.mail,
                        color: Colors.black,
                      ),
                      labelText: "Make a username",
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

                TextField(
                  controller: registerMail,
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
                  controller: registerPassword,
                  decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      suffixIcon: (vis == 0)
                          ? IconButton(
                              icon: Icon(Icons.visibility_off),
                              onPressed: () {
                                setState(() {
                                  vis = 1;
                                });
                              },
                            )
                          : IconButton(
                              icon: Icon(Icons.visibility),
                              onPressed: () {
                                setState(() {
                                  vis = 0;
                                });
                              },
                            ),
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
                  obscureText: (vis == 0) ? true : false,
                ),

                SizedBox(
                  height: 50.0,
                ),

                //SIGN UP button
                GestureDetector(
                  child: Container(
                    height: 65.0,
                    width: 150.0,
                    decoration: BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.rectangle,
                        borderRadius: BorderRadius.all(Radius.circular(15.0)),
                        border: Border.all(color: Colors.black54, width: 2.0)),
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
                    signUp();
                  },
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
