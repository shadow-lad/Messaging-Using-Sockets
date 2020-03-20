# Communicating with the Server

This file contains the guidelines to communicate with the server. Proper response, when possible, is sent by the server.

## Table of Contents

- [Communicating with the Server](#communicating-with-the-server)
  - [Table of Contents](#table-of-contents)
  - [Registration](#registration)
    - [Client Request](#client-request)
    - [Server Response](#server-response)
      - [OTP Sent](#otp-sent)
      - [User is successfully registered](#user-is-successfully-registered)
      - [Error why the registration failed](#error-why-the-registration-failed)
  - [Login](#login)
    - [Client Login Request](#client-login-request)
    - [Server Login Response](#server-login-response)
      - [if credentials found](#if-credentials-found)
      - [if credentials not found](#if-credentials-not-found)
  - [Log-Out](#log-out)
    - [Client Log-Out Request](#client-log-out-request)
  - [Messages](#messages)
    - [Global Message (This feature will be deprecated in the future.)](#global-message-this-feature-will-be-deprecated-in-the-future)
      - [Sending Message](#sending-message)
        - [Without media](#without-media)
        - [With media](#with-media)
      - [Receiving Message](#receiving-message)
        - [Receiving Messages without Media](#receiving-messages-without-media)
        - [Receiving Messages with Media](#receiving-messages-with-media)
    - [Private Message](#private-message)
      - [Sending Private Message](#sending-private-message)
        - [Sending Private Message without Media](#sending-private-message-without-media)
        - [Sending Private Message with Media](#sending-private-message-with-media)
      - [Receiving Private Message](#receiving-private-message)
        - [Receving Private Message without Media](#receving-private-message-without-media)
        - [Receiving Private Message with Media](#receiving-private-message-with-media)
  - [Error (Will be classified in the future.)](#error-will-be-classified-in-the-future)

## Registration

- The first message sent must be a [login](#login)/registration request to the server when making a socket connection.
- Failure to do so will result in error.
- It should be a valid JSON message.

### Client Request

When registering, **NONE OF THE KEYS CAN BE NULL**.

```json
{
    "request":"registration",
    "details": {
        "email":"xyz@abc.com",
        "password":"md5-hashed-password",
        "username":"xyz"
    }
}
```

### Server Response

#### OTP Sent

```json
{
    "status": "sent",
    "message": "OTP sent to xyz@abc.com"
}
```

#### User is successfully registered

```json
{
    "status" : "success"
}
```

#### Error why the registration failed

```json
{
    "status" : "failed",
    "message" : "Reason why the user was not registered"
}
```

>***NOTE: User will be logged in once the registration is complete.***

## Login

- The first message sent must be a login/[registration](#registration) request to the server when making a socket connection.
- Failure to do so will result in error.
- It should be a valid JSON message.

### Client Login Request

Either the *"username"* or the *"email"*&nbsp; key can be null, both cannot be null.

```json
{
    "request" : "login",
    "details" : {

        "username": "xyz",
        "email" : "xyz@abc.com",
        "password" : "md5-hashed-password"
    }

}
```

### Server Login Response

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

### Client Log-Out Request

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

##### Receiving Messages without Media

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

##### Receiving Messages with Media

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

#### Sending Private Message

##### Sending Private Message without Media

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

##### Sending Private Message with Media

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

#### Receiving Private Message

##### Receving Private Message without Media

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

##### Receiving Private Message with Media

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
