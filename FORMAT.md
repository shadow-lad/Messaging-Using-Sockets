# Communicating with the Server

This file contains the guidelines to communicate with the server. Proper response, when possible, is sent by the server.

## Table of Contents

- [Communicating with the Server](#communicating-with-the-server)
	- [Table of Contents](#table-of-contents)
	- [Registration](#registration)
		- [Client Registration Request](#client-registration-request)
			- [Begin Registration Process](#begin-registration-process)
			- [Verify OTP](#verify-otp)
		- [Server Registration Response](#server-registration-response)
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
	- [Audio Call](#audio-call)
		- [Placing a call](#placing-a-call)
		- [Receiving a call](#receiving-a-call)
		- [Accepting a Call](#accepting-a-call)
		- [Rejecting a Call](#rejecting-a-call)
		- [Call Time Out](#call-time-out)
		- [Server Voice Call Response](#server-voice-call-response)
			- [If Call Accepted](#if-call-accepted)
			- [If Call Rejected](#if-call-rejected)
		- [If Receiver is not Online](#if-receiver-is-not-online)
	- [Error](#error)

## Registration

- The first message sent must be a [login](#login)/registration request to the server when making a socket connection.
- Failure to do so will result in error.
- It should be a valid JSON message.
- A new request should be made to generate a new OTP.

### Client Registration Request

#### Begin Registration Process

When registering, **NONE OF THE KEYS CAN BE NULL**.

```json
{
    "type": "registration",
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
    "type": "verify",
    "otp": "1234"
}
```

### Server Registration Response

#### OTP Sent

```json
{
    "event": "sent",
    "type": "otp",
    "message": "OTP sent to xyz@abc.com"
}
```

#### User is successfully registered

```json
{
    "event": "success",
    "type": "verify"
}
```

#### Error why the registration failed

```json
{
    "event": "failed",
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
    "type": "login",
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
- The response data will also contain a list of all the users the user has previously contacted or has been contacted by.

```json
{
    "event": "success",
    "type": "login",
    "details": {
        "username": "xyz",
        "email": "xyz@abc.com",
        "friends": [
            {
                "email": "xyz@abc.com",
                "username": "xyz"
            },
            {
                "email": "def@abc.com",
                "username": "def"
            }
        ]
    }
}
```

#### if credentials not found

```json
{
    "event": "failed",
    "type": "login",
    "message": "Reason why user was not logged in"
}
```

## Log-Out

- The server won't end all I/O connections on logout, the connection should terminate ideally when the client application is not running.

### Client Log-Out Request

```json
{
    "type": "logout"
}
```

### Server Log-Out Response

```json
{
    "event": "success",
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
    "event": "global",
    "type": "message",
    "details": {
        "message": "Hello World",
        "time": 1234567890
    }
}
```

##### With media

```json
{
    "event": "global",
    "type": "message",
    "details": {
        "message": "Hello World",
        "media": "https://url-to-firestore/",
        "time": 1234567890
    }
}
```

#### Receiving Message

##### Receiving Messages without Media

```json
{
    "event": "global",
    "type": "message",
    "details": {
        "message": "Hello World",
        "from": "xyz@abc.com",
        "time": 1234567890
    }
}
```

##### Receiving Messages with Media

```json
{
    "event": "global",
    "type": "message",
    "details": {
        "message": "Hello World",
        "media": "https://url-to-firstore",
        "from": "xyz@abc.com",
        "time": 1234567890
    }
}
```

### Personal Message

- Also known as direct messages, these messages are to be sent when the user wants to send the message to only one specific user.

#### Sending Personal Message

##### Sending Personal Message without Media

```json
{
    "event": "personal",
    "type": "message",
    "details": {
        "to": "def@abc.com",
        "message": "Hello World",
        "time": 1234567890
    }
}
```

##### Sending Personal Message with Media

```json
{
    "event": "personal",
    "type": "message",
    "details": {
        "to": "def@abc.com",
        "message": "Hello World",
        "media": "https://url-to-firestore/",
        "time": 1234567890
    }
}
```

#### Receiving Personal Message

##### Receving Personal Message without Media

```json
{
    "event": "personal",
    "type": "message",
    "details": {
        "id": "some-id",
        "message": "Hello World",
        "from": "xyz@abc.com",
        "time": 1234567890
    }
}
```

##### Receiving Personal Message with Media

```json
{
    "event": "personal",
    "type": "message",
    "details": {
        "id": "some-id",
        "message": "Hello World",
        "media": "https://url-to-firestore",
        "from": "xyz@abc.com",
        "time": 1234567890
    }
}
```

### Server Message Response

- This is the standard response from the server when a personal message is sent.
- No response is sent for Global Messages.

```json
{
    "event": "sent",
    "type": "message",
    "id": "id-for-the-message"
}
```

## User List

- This will return a list of all the users registered to the server

### Request

```json
{
    "type": "users"
}
```

### Response

```json
{
    "event": "success",
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

## Audio Call

### Placing a call

- When placing a call, the current user must send the email address of the person he wants to call in the following format.

```json
{
    "type": "voice",
    "event": "call",
    "details": {
        "email": "email@userto.call"
    }
}
```

### Receiving a call

- The server will try to communicate with the recipient of the call by sending the following message.

```json
{
    "type": "voice",
    "event": "request",
    "details": {
        "email": "email@calling.user"
    }
}
```

### Accepting a Call

- For accepting the call, the recipient of the call should send the following response

```json
{
    "type": "voice",
    "event": "accept",
    "details": {
        "email": "email@calling.user"
    }
}
```

### Rejecting a Call

- For accepting the call, the recipient of the call should send the following response

```json
{
    "type": "voice",
    "event": "reject",
    "details": {
        "email": "email@calling.user"
    }
}
```

### Call Time Out

- The server will wait 30 seconds for a response from the recipient of the call.
- If in that time, no response is received, the server will send the following response to the caller:

```json
{
    "type": "voice",
    "reason": "time",
    "details": {
        "email": "email@userto.call"
    }
}
```

- And the following response to the receiver:

```json
{
    "event": "voice",
    "reason": "time",
    "details": {
        "email": "email@calling.user"
    }
}
```

### Server Voice Call Response

#### If Call Accepted

- When the receiver accepts the call, 2 responses are sent from the server
  1. To do the one who makes the call
  2. To the one who receives the call
- The message contains the details of the IP Address and port over to which the UDP packets should be sent.

```json
{
    "type": "voice",
    "event": "accepted",
    "details": {
        "ip": "x.x.x.x",
        "port": "0000"
    }
}
```

#### If Call Rejected

- If the receiver rejects the call the following response is sent to the caller.

```json
{
    "type": "voice",
    "event": "rejected",
    "details": {
        "email": "email@userto.call"
    }
}
```

### If Receiver is not Online

- If the receiver is not available to take your call, the server will send the following response:

```json
{
    "type": "voice",
    "event": "offline",
    "details": {
        "email": "email@userto.call"
    }
}
```

## Error

- Whenever any error occurs, a response in this format will be sent by the server to the person requesting the service.

```json
{
    "event": "invalid or failed",
    "type": "type-of-request-made",
    "message": "A message regarding the event."
}
```
