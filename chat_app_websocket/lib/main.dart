import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:io';
import 'package:chat_app_websocket/ChatRoom/chat_room.dart';
void main(){
  runApp(
    new MaterialApp(
      title: "Chat App",
      home: ChatRoom(),
      debugShowCheckedModeBanner: false,
    )
  );
}