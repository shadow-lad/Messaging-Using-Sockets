# Communicating with the Server

This file contains the guidelines to communicate with the server. Proper response, when possible, is sent by the server.

## Table of Contents

- [Communicating with the Server](#communicating-with-the-server)
  - [Table of Contents](#table-of-contents)
  - [Registration](#registration)
    - [Client Request](#client-request)
      - [Begin Registration Process](#begin-registration-process)
      - [Verify OTP](#verify-otp)
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
    - [Server Log-Out Response](#server-log-out-response)
  - [Messages](#messages)
    - [Global Message (This feature will be deprecated in the future.)](#global-message-this-feature-will-be-deprecated-in-the-future)
      - [Sending Message](#sending-message)
        - [Without media](#without-media)
        - [With media](#with-media)
      - [Receiving Message](#receiving-message)
        - [Receiving Messages without Media](#receiving-messages-without-media)
        - [Receiving Messages with Media](#receiving-messages-with-media)
    - [Personal Message](#personal-message)
      - [Sending Personal Message](#sending-personal-message)
        - [Sending Personal Message without Media](#sending-personal-message-without-media)
        - [Sending Personal Message with Media](#sending-personal-message-with-media)
      - [Receiving Personal Message](#receiving-personal-message)
        - [Receving Personal Message without Media](#receving-personal-message-without-media)
        - [Receiving Personal Message with Media](#receiving-personal-message-with-media)
    - [Server Message Response](#server-message-response)
  - [User List](#user-list)
    - [Request](#request)
    - [Response](#response)
  - [Error](#error)

## Registration

- The first message sent must be a [login](#login)/registration request to the server when making a socket connection.
- Failure to do so will result in error.
- It should be a valid JSON message.
- A new request should be made to generate a new OTP.

### Client Request

#### Begin Registration Process

When registering, **NONE OF THE KEYS CAN BE NULL**.

```json
{
    "request": "registration",
    "details": {
        "email": "xyz@abc.com",
        "password": "md5-hashed-password",
        "username": "xyz"
    }
}
```

#### Verify OTP

```json
{
    "request": "verify",
    "otp": "1234"
}
```

### Server Response

#### OTP Sent

```json
{
    "status": "sent",
    "type": "otp",
    "message": "OTP sent to xyz@abc.com"
}
```

#### User is successfully registered

```json
{
    "status": "success",
    "type": "verify"
}
```

#### Error why the registration failed

```json
{
    "status": "failed",
    "type": "otp or registration or verify",
    "message": "Reason why the user was not registered"
}
```

> **_NOTE: After successful registration, the user should be taken back to the login screen. The user will not be logged in after registration._**

## Login

- The first message sent must be a login/[registration](#registration) request to the server when making a socket connection.
- Failure to do so will result in error.
- It should be a valid JSON message.

### Client Login Request

Either the _"username"_ or the _"email"_&nbsp; key can be null, both cannot be null.

```json
{
    "request": "login",
    "details": {
        "username": "xyz",
        "email": "xyz@abc.com",
        "password": "md5-hashed-password"
    }
}
```

### Server Login Response

#### if credentials found

- The response data will contain the username and email address of the user.

```json
{
    "status": "success",
    "details": {
        "username": "xyz",
        "email": "xyz@abc.com"
    }
}
```

#### if credentials not found

```json
{
    "status": "failed",
    "type": "login",
    "message": "Reason why user was not logged in"
}
```

## Log-Out

- The server won't end all I/O connections on logout, the connection should terminate ideally when the client application is not running.

### Client Log-Out Request

```json
{
    "request": "logout"
}
```

### Server Log-Out Response

```json
{
    "status": "success",
    "type": "logout"
}
```

## Messages

- This is the basic message conversation format.
- The message layout of all messages are similar with some minor but important changes.

### Global Message (This feature will be deprecated in the future.)

- Used to communicate with all the users in the server, these messages are to be sent when the user wants to send the message to everyone.

#### Sending Message

##### Without media

```json
{
    "request": "message",
    "type": "global",
    "details": {
        "message": "Hello World",
        "media": null,
        "time": 1214343465
    }
}
```

##### With media

```json
{
    "request": "message",
    "type": "global",
    "details": {
        "message": "Hello World",
        "media": "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

#### Receiving Message

##### Receiving Messages without Media

```json
{
    "request": "message",
    "type": "global",
    "details": {
        "id": "some-id",
        "from": "xyz",
        "message": "Hello World",
        "media": null,
        "time": 1214343465
    }
}
```

##### Receiving Messages with Media

```json
{
    "request": "message",
    "type": "global",
    "details": {
        "from": "xyz",
        "message": "Hello World",
        "media": "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

### Personal Message

- Also known as direct messages, these messages are to be sent when the user wants to send the message to only one specific user.

#### Sending Personal Message

##### Sending Personal Message without Media

```json
{
    "request": "message",
    "type": "personal",
    "details": {
        "to": "xyz",
        "message": "Hello World",
        "media": null,
        "time": 1214343465
    }
}
```

##### Sending Personal Message with Media

```json
{
    "request": "message",
    "type": "personal",
    "details": {
        "to": "xyz",
        "message": "Hello World",
        "media": "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

#### Receiving Personal Message

##### Receving Personal Message without Media

```json
{
    "request": "message",
    "type": "personal",
    "details": {
        "id": "some-id",
        "from": "xyz",
        "message": "Hello World",
        "media": null,
        "time": 1214343465
    }
}
```

##### Receiving Personal Message with Media

```json
{
    "request": "message",
    "type": "personal",
    "details": {
        "id": "some-id",
        "from": "xyz",
        "message": "Hello World",
        "media": "https://url-to-firestore/",
        "time": 1214343465
    }
}
```

### Server Message Response

- This is the standard response from the server when any kind of message is sent.

```json
{
    "status": "sent",
    "type": "message",
    "id": "id-of-the-message"
}
```

## User List

- This will return a list of all the users registered to the server

### Request

```json
{
    "request": "users"
}
```

### Response

```json
{
    "status": "success",
    "type": "users",
    "details": [
        {
            "username": "xyz",
            "email": "xyz@abc.com"
        },
        "List-of-objects"
    ]
}
```

## Error

- Whenever any error occurs, a response in this format will be sent by the server to the person requesting the service.

```json
{
    "status": "invalid or failed",
    "type": "type-of-request-made",
    "message": "A message regarding the event."
}
```
