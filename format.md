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
    - [Private Message](#private-message)
      - [Sending Message](#sending-message)
        - [Without media](#without-media)
        - [With media](#with-media)
    - [Receiving Message](#receiving-message)
      - [Without media](#without-media-1)
      - [With media](#with-media-1)

## Login
### Client Request
```json
{
    "request" : "login",
    "details" : {

        "user_name": "xyz" or "xyz@abc.com",
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

### Private Message

#### Sending Message

##### Without media
```json
{
    "request" : "message",
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
    "details" : {
        "id" : "some-id",
        "type" : "private",
        "to" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/"
    }
}
```

### Receiving Message
#### Without media
```json
{
    "request" : "message",
    "details" : {
        "id" : "some-id",
        "type" : "private",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : null
    }
}
```

#### With media
```json
{
    "request" : "message",
    "details" : {
        "id" : "some-id",
        "type" : "private",
        "from" : "xyz",
        "message" : "Hello World",
        "media" : "https://url-to-firestore/"
    }
}
```