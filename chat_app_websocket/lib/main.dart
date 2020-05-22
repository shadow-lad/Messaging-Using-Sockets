import 'package:flutter/material.dart';

import './verification/login_screen.dart';
void main(){
  runApp(
    new MaterialApp(
      title: "Chat App",
      home: LoginScreen(),
      debugShowCheckedModeBanner: false,
    )
  );
}