# Communicating with the Server
This file contains the guidelines to communicate with the server. Proper response, when possible, is sent by the server.

## Table of Contents
- [Communicating with the Server](#communicating-with-the-server)
  - [Table of Contents](#table-of-contents)
  - [Login](#login)
    - [Client Request](#client-request)
    - [Server Response](#server-response)
      - [if credentials found](#if-credentials-found)
      - [if credentials not found](#if-credentials-not-found)
  - [Log-Out](#log-out)
    - [Client Request](#client-request-1)
  - [Messages](#messages)
    - [Global Message (This feature will be deprecated in the future.)](#global-message-this-feature-will-be-deprecated-in-the-future)
      - [Sending Message](#sending-message)
        - [Without media](#without-media)
        - [With media](#with-media)
      - [Receiving Message](#receiving-message)
        - [Without media](#without-media-1)
        - [With media](#with-media-1)
    - [Private Message](#private-message)
      - [Sending Message](#sending-message-1)
        - [Without media](#without-media-2)
        - [With media](#with-media-2)
      - [Receiving Message](#receiving-message-1)
        - [Without media](#without-media-3)
        - [With media](#with-media-3)
  - [Error (Will be classified in the future.)](#error-will-be-classified-in-the-future)

## Login
- The first message sent must be a login request to the server when making a socket connection. 
- Failure to do so will result in error. 
- It should be a valid JSON message.
- Password can be null at this stage in the build.
- Currently, every login request also acts as a registration request and hence is not viable for commercial use.

### Client Request
```json
{
    "request" : "login",
    "details" : {

        "user_name": "xyz or xyz@abc.com",
        "password" : "md5-hashed-password"    
    }

}
```

### Server Response

#### if credentials found
```json
{
    "status" : "success"
}
```

#### if credentials not found
```json
{
    "status" : "failed",
    "message" : "Reason why user was not logged in"
}
```

## Log-Out
- After sending a logout request,the server will close all the connections and hence the client must close all the I/O Streams and close the socket.
- Failure to do so can result in an exception.

### Client Request

```json
{
    "request":"logout"
}
```

## Messages

- This is the basic message conversation format. 
- It is to be assummed that every message is sent when you have an active connection to the server.
- The message layout of all messages are similar with some minor but important changes.

### Global Message (This feature will be deprecated in the future.)

- Used to communicate with all the users in the server, these messages are to be sent when the user wants to send the message to everyone.

#### Sending Message

##### Without media
```json
{
    "request" : "message",
    "type" : "global",
    "details" : {
        "id" : "some-id",
        "message" : "Hello World",
        "media" : null,
        "time": 1214343465
    }
}
```

##### With media
```json
{
    "request" : "message",
    "type" : "global",
    "details" : {
        "id" : "some-id",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

#### Receiving Message

##### Without media
```json
{
    "request" : "message",
    "type" : "global",
    "details" : {
        "id" : "some-id",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : null,
        "time": 1214343465
    }
}
```

##### With media
```json
{
    "request" : "message",
    "type" : "global",
    "details" : {
        "id" : "some-id",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

### Private Message

- Also known as direct messages, these messages are to be sent when the user wants to send the message to only one specific user.

#### Sending Message

##### Without media
```json
{
    "request" : "message",
    "type" : "private",
    "details" : {
        "id" : "some-id",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : null,
        "time": 1214343465
    }
}
```

##### With media
```json
{
    "request" : "message",
    "type" : "private",
    "details" : {
        "id" : "some-id",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

#### Receiving Message

##### Without media
```json
{
    "request" : "message",
    "type" : "private",
    "details" : {
        "id" : "some-id",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : null,
        "time": 1214343465
    }
}
```

##### With media
```json
{
    "request" : "message",
    "type" : "private",
    "details" : {
        "id" : "some-id",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

## Error (Will be classified in the future.)
- Whenever any error occurs, a response in this format will be sent by the server to the person requesting the service.
```json
{
    "request" : "invalid",
    "message" : "A message regarding the event."
}
```