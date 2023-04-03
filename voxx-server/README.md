# Project-Voxx Server Protocol

At its core, Project-Voxx protocol is going to be transported through `websocket`. This documentation specifies what the
server for Project-Voxx is going to accept with it’s corresponding response and messages that a server could send and
therefore client needs to make anticipate non-requested messages from the server.

#### Server Connection

When a client connects to the server, the connection is not affiliated with any user until the client socket sends a
request `ru` or `register user` to the server. If the user is with the same username is not registered, the socket
connection will be bound to that user and every request from or to the client is in the context of that user.

### Request format

Before we can list down all the valid request that a client can make to the server, let’s talk about the format of a
request. The format of a request is pretty straight forward and looks like the following:

```json
{
  "request-id": "request key",
  "params": {
    "param-1": "Some parameter 1",
    "param-2": "Some parameter 2"
  }
}
```

As we can see, the format is in the `json` syntax. This makes it so that we can serialize and deserialize an object
through a `websocket` easily. The attribute named `request-id` is the name of the request that we are making to the
server. Depending on the parameters that the request accepts, we need to provide the right amount of parameter that goes
with that request and we must put them in the attribute `params` and attribute params must match the attribute name
defined in the request documentation.

For undefined requests the server is going to respond with a the following message:

```json
{
  "response-id": -1,
  "body": {
    "message": "{request-id} is not a valid request"
  }
}
```

We can see that the `response-id` is `-1` meaning that the request does not exist. And a body attribute with
a `message`.

For valid requests, each request type will have their own unique response and they will be specified below.

## Requests

The following headers will show specifications of each requests.

### Register User

Here is the request body that you must send to the server to have a valid register user request

```json
{
  "request-id": "ru",
  "params": {
    "uname": "{username}"
  }
}
```

Since Project-Voxx is a non-persistent drop in public chat channel, user do not need a password. The only requirement
for user registration is a username and that it’s not taken by any other users in the live server.

If the client socket sends a request with a username that’s already taken the server will respond with the following:

```json
{
  "response-id": 0,
  "body": {
    "message": "{request username} is already taken"
  }
}
```

A response ID `0` means that the request is invalid due to improper parameters or unsatisfied requirements.

If the user registration is successful, the server will respond with the `UID` for the user. A UID is a unique
identifier that could be associated with a user or a message. Since the UID contains the timestamp, it allows us to sort
users based on when they registered to the server. The following is an example of a successful user registration:

```json
{
  "response-id": 1,
  "body": {
    "user": {
      "uid": 66123123123123123,
      "uname": "{username}"
    }
  }
}
```

### Sending a Chat Message

When sending a message to the server, the client socket must have a bound `user` first. Therefore, the client socket
must request an `ru` (register user) first. With that being said, here’s an example request for sending a message:

```json
{
  "request-id": "sm",
  "params": {
    "message": "{message}"
  }
}
```

However, nothing can stop a client form sending a send message
request even without sending a user registration request first. If this is the case, the server will respond with the
following invalid request response:

```json
{
  "response-id": 0,
  "body": {
    "message": "User registration is required before sending a message!"
  }
}
```

When a chat message request was handled properly the server should respond with the following message:

```json
{
  "response-id": 1,
  "body": {
    "message": {
      "message-uid": 6123123123123122,
      "message-content": "{message sent}"
    }
  }
}
```

## Update Messages

Update messages are messages that are sent by the server to the clients to update clients about changes that happens in
the server. This is for when a user sends a chat message to the server, a new user registers, or when a user
disconnects. The
client can do whatever they want to do with the update messages, but they’re there so that the clients can display
up-to-date information in the server.

### New User Update Message

This update message is sent to each of the client when a new user is registered so that clients can update their user
list (if being tracked). Here’s what the update message looks:

```json
{
  "update-message": "nu",
  "body": {
    "user": {
      "uid": 6465245245424,
      "uname": "{username}"
    }
  }
}
```

### New Chat Update Message

This is sent by the server to all the clients when a user sends a new chat message. This excludes the sender of the
message.

```json
{
  "update-message": "ns",
  "body": {
    "sender": {
      "uid": 6465245245424,
      "uname": "{sender username}"
    },
    "message": {
      "uid": 6747807899832,
      "content": "{some message}"
    }
  }
}
```

###	User Disconnect Update Message

This update message is sent by the server when a user (client) disconnects from the server.

```json
{
  "update-message": "ud",
  "body": {
    "user": {
      "uid": 6465245245424,
      "uname": "{username}"
    }
  }
}
```

