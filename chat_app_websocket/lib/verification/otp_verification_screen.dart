import 'dart:convert';
import 'dart:io';

import 'login_screen.dart';
import '../models/otp_verification_model.dart';
import 'package:flutter/material.dart';

class OtpVerificationScreen extends StatefulWidget {
  final Socket socket;

  const OtpVerificationScreen({Key key, this.socket}) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return new OtpVerificationScreenState();
  }
}

class OtpVerificationScreenState extends State<OtpVerificationScreen> {
  final TextEditingController otp = new TextEditingController();

  void register(String credential) {

    widget.socket.listen((data) {
      String recievedMessage = new String.fromCharCodes(data).trim();
      print("DEBUG: Received message from server: " + recievedMessage);

      Map<String, String> response = jsonDecode(recievedMessage);

      if (response['status'] == 'success') {
        Navigator.of(context)
            .pop(); // Popping OTP Page back to Registration Page
        Navigator.of(context)
            .pop(); // Popping Registration Page back to Login Page

        Navigator.of(context).pushReplacement(MaterialPageRoute(
            builder: (BuildContext context) =>
                LoginScreen())); // Replacing Login Page
      } else {
        print('DEBUG: An error occurred' + response['message']);
        Navigator.of(context)
            .pop(); // Popping OTP Page back to Registration Page
      }
    });

    print("DEBUG: Listener initialized");

    print("DEBUG: Got otp from user");
    OtpVerificationModel otpVerificationObject =
        OtpVerificationModel(credential);

    String otpMap = jsonEncode(otpVerificationObject);

    widget.socket.writeln(otpMap);
    print("DEBUG: Sent otp to server");

  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      body: Container(
        height: MediaQuery.of(context).size.height,
        width: MediaQuery.of(context).size.width,
        padding: EdgeInsets.all(20.0),
        child: Container(
          alignment: Alignment.center,
          margin: EdgeInsets.only(
              top: MediaQuery.of(context).size.height * (1 / 3)),
          child: Column(
            children: <Widget>[
              TextField(
                controller: otp,
                decoration: InputDecoration(
                    border: UnderlineInputBorder(),
                    icon: Icon(
                      Icons.mail,
                      color: Colors.black,
                    ),
                    labelText: "Enter OTP",
                    labelStyle: TextStyle(color: Colors.black, fontSize: 16.0)),
                style: TextStyle(
                    color: Colors.black,
                    letterSpacing: 5.0,
                    fontWeight: FontWeight.w500),
              ),
              SizedBox(
                height: 20.0,
              ),
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
                      "REGISTER",
                      style: TextStyle(
                          color: Colors.black,
                          fontSize: 18.5,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 1.5),
                    ),
                  ),
                ),
                onTap: () {
                  String cr = otp.text.toString().trim();
                  register(cr);
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}
