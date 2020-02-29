# Communicating with the Server

## Table of Contents
- [Communicating with the Server](#communicating-with-the-server)
  - [Table of Contents](#table-of-contents)
  - [Login](#login)
    - [Client Request](#client-request)
    - [Server Response](#server-response)
      - [if credentials found](#if-credentials-found)
      - [if credentials not found](#if-credentials-not-found)
  - [Messages](#messages)
    - [Global Message](#global-message)
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

## Login
The first message sent must be a login request to the server when making a socket connection. Failure to do so will result in error. It should be a valid JSON message.

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
    "status" : "invalid",
    "message" : "Reason why user was not logged in"
}
```
## Messages

This is the basic message conversation format. It is to be assummed that every message is sent when you have an active connection to the server. The message layout of all messages are similar with some minor important changes.

### Global Message

Used to communicate with all the users in the server, these messages are to be sent when the user wants to send the message to everyone. (This feature will be deprecated in the future.)

#### Sending Message

##### Without media
```json
{
    "request" : "message",
    "type" : "private",
    "details" : {
        "id" : "some-id",
        "type" : "private",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : null
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
        "type" : "private",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/"
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
        "type" : "private",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : null
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
        "type" : "private",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/"
    }
}
```

### Private Message

Also known as direct messages, these messages are to be sent when the user wants to send the message to only one specific user.

#### Sending Message

##### Without media
```json
{
    "request" : "message",
    "type" : "private",
    "details" : {
        "id" : "some-id",
        "type" : "private",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : null
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
        "type" : "private",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/"
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
        "type" : "private",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : null
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
        "type" : "private",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/"
    }
}
```