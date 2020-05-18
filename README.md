# Messaging Application Using Flutter and Java

## Table of Contents

- [Messaging Application Using Flutter and Java](#messaging-application-using-flutter-and-java)
  - [Table of Contents](#table-of-contents)
  - [Java Backend](#java-backend)
  - [Flutter App](#flutter-app)
  - [Working](#working)

## [Java Backend](Java%20Backend)

This project uses Java to create a backend application to handle messages and requests to and from clients.

The most famous Internet Messaging (IM) protocol is open XMPP (Xtensible Messaging and Presence Protocol). It uses XML to communicate messages between the sever and client over TCP.

Our server deviates from this protocol and uses JSON instead of XML as

1. JSON is less CPU intensive to parse
2. It is human readable hence debugging is easy
3. It is supported by major programming languages out of the box.

## [Flutter App](chat_app_websocket)

For the client to interact with the server, an application is created using the Flutter SDK for Dart to create cross platform mobile applications.

## [Working](FORMAT.md)

**TCP Sockets** are used to communicate between the server and the client because TCP provides for the recovery of segments that get lost, are damaged, or received out of their correct order. It also requires for an acknowledgement message to be returned after transmitting data.

**UDP Sockets** are used to make audio calls as UDP is faster than TCP as no error checking, error correction or acknowledgement is done and to make audio calls as latency free as possible.

A detailed explanation of how the client communicates with the server is given [here](FORMAT.md).
