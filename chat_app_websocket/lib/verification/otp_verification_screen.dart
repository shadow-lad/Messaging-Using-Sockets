import '../bloc/client.dart';
import '../models/otp_verification_model.dart';
import 'package:flutter/material.dart';

class OtpVerificationScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return new OtpVerificationScreenState();
  }
}

class OtpVerificationScreenState extends State<OtpVerificationScreen> {
  final client = Client.instance;
  final otp = new TextEditingController();

  @override
  void initState() {
    super.initState();
    client.otpVerifySuccess.listen((verified) {
      if (verified) {
        showDialog(
          context: context,
          builder: (_) => AlertDialog(
            title: Row(
              children: <Widget>[
                Text("Successful")
              ],
            ),
            content: Text("Account creation successful!"),
            actions: <Widget>[
              FlatButton(
                onPressed: () {
                  Navigator.of(context).pop(); // Dismiss Dialog
                  Navigator.of(context).pop(); // Back to Registration Page
                  Navigator.of(context).pop(); // Back to Login Page
                }, 
                child: Text("LOGIN")
              )
            ],
          ),
        );
      } else {
        showDialog(
          context: context,
          builder: (_) => AlertDialog(
            title: Row(
              children: <Widget>[
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
                  Navigator.of(context).pop(); // Back to Registration Page
                }, 
                child: Text("OK")
              )
            ],
          ),
        );
      }
    });
  }

  void register(String credential) {
    print("DEBUG: Listener initialized");

    print("DEBUG: Got otp from user");
    OtpVerificationModel otpVerificationObject =
        OtpVerificationModel(credential);

    client.sendJson(otpVerificationObject.toJson());
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
