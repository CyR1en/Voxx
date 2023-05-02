<p align="center">
  <img width="100" src="logo@0.5x.png" />
</p>

Voxx is a desktop/command-line non-persistent drop in public chat channel, where users can chat with other connected users with a degree of anonymity. Users could also host their own Voxx server to setup a communication medium for their own use cases.

## Use cases

- **Anonymous support groups**: Voxx can be used as a platform for anonymous support groups where people can connect with others who are going through similar struggles. The fact that messages are not logged or saved can create a sense of
  privacy and safety for users.
  
- **Event-based chat**: Voxx can be used as a platform for event-based chat channels where people can connect and chat with others who are attending the same event. For example, people attending a conference, music festival, or sporting event can use Voxx to chat with each other.
  
- **Study groups:** Voxx can be used as a platform for study groups where students can connect and chat with each other about their coursework. The fact that messages are not logged or saved can create a sense of privacy and security for students who may be concerned about their academic performance.
  
- **Gaming communities**: Voxx can be used as a platform for gaming communities where players can connect and chat with each other about their favorite games. The fact that messages are not logged or saved can create a sense of privacy and security for players who may be concerned about their online reputation.
  
- **Language exchange**: Voxx can be used as a platform for language exchange where people can connect with others who are looking to practice speaking a different language. The fact that messages are not logged or saved can create a sense of privacy and safety for users who may be hesitant to speak in a new language with strangers.

These are just a few potential use cases for Voxx. With some creative thinking, you can likely come up with many more!

## Protocol

This documentation specifies what the server for Project-Voxx is going to accept with its corresponding response and messages that a server could send. Therefore, a client needs to anticipate non-requested messages from the server.

#### Server-Client Communication Overview

At its core, Project-Voxx protocol is going to be transported through `websocket`. The communication is text based, therefore, all the message received and sent by the message is going to be a `String`. To have a clear and easy to parse message, we’ve opted into using the `json` syntax for our messages. However, the `json` syntax needs to be flattened and `must not have any break line`. Use the following regex replacement on your `json` string before sending requests to the server:

**Java**

```java
"<your jason string here>".replaceAll("\\s{2,}|\\n","");
```

**Python**

```python
import re

re.sub(r'\s{2,}|\n', '', "<your jason string here>")
```

#### Server Connection

When a client connects to the server, the connection is not affiliated with any user until the client socket sends a request `ru` or `register user` to the server. If the user with the same username is not registered in the server, the socket connection will be bound to that user and every request from or to the client is in the context of that user.

Once a socket client registers a user, we now have an established `Response-Request` connection to the server. However, there's another optional connection that you can establish which is called the `Update Message` connection.

##### Response-Request Connection

This connection from the client handles all of the request that's coming in from the it to the server. This connection is essentially a blocking connection where a client sends a request to the server and will wait for a response. *<u>If you're implementing you're own client, you may want to set a socket timeout.</u>*

##### Update Message Connection

The update message connection is a supplemental connection that a client sets up to receive update messages from the server. This connection must be setup correctly and a `Response-Request` connection must be established before this connection is setup. Specification for setting up this connection could be found under the heading [Update Message](#update-message)

### Request format

Before we can list down all the valid request that a client can make to the server, let’s talk about the format of a request. The format of a request is pretty straight forward and looks like the following:

```json
{
  "request-id": "request key",
  "params": {
    "param-1": "Some parameter 1",
    "param-2": "Some parameter 2"
  }
}
```

As we can see, the format is in the `json` syntax. This makes it so that we can serialize and deserialize an object through a `websocket` easily. The attribute named `request-id` is the name of the request that we are making to the server. Depending on the parameters that the request accepts, we need to provide the right amount of parameter that goes with that request, and we must put them in the attribute `params` and attribute params must match the attribute name defined in the request documentation.

For undefined requests the server is going to respond with the following message:

```json
{
  "response-id": -1,
  "body": {
    "message": "{request-id} is not a valid request"
  }
}
```

We can see that the `response-id` is `-1` meaning that the request does not exist. And a body attribute with a `message`.

For valid requests, each request type will have their own unique response, and they will be specified below.

### Requests

The following headers will show specifications of each request.

##### Register User

Here is the request body that you must send to the server to have a valid register user request

```json
{
  "request-id": "ru",
  "params": {
    "uname": "{username}"
  }
}
```

Since Project-Voxx is a non-persistent drop in public chat channel, user do not need a password. The only requirement for user registration is a username and that it’s not taken by any other users in the live server.

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

If the user registration is successful, the server will respond with the `UID` for the user. A UID is a unique identifier that could be associated with a user or a message. Since the UID contains the timestamp, it allows us to sort users based on when they registered to the server. The following is an example of a successful user registration:

```json
{
  "response-id": 1,
  "body": {
    "user": {
      "uid": 6884583347257344,
      "uname": "{username}"
    }
  }
}
```

##### Sending a Chat Message

When sending a message to the server, the client socket must have a bound `user` first. Therefore, the client socket must request an `ru` (register user) first. With that being said, here’s an example request for sending a message:

```json
{
  "request-id": "sm",
  "params": {
    "message": "{message}"
  }
}
```

However, nothing can stop a client form sending a send message request even without sending a user registration request first. If this is the case, the server will respond with the following invalid request response:

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
      "uid": 6884583351369728,
      "content": "{message sent}"
    }
  }
}
```

##### Getting User List

A request that the client can make to get all the registered user in the server.

The request body for getting the user list is super simple and does not require any parameter:

```json
{
  "request-id": "ul"
}
```

Since this request does not require any parameter/argument, the response will always be a `1`. And here's an example of a possible response from the server:

```json
{
  "response-id": 1,
  "body": {
    "users": [
      {
        "uid": 6884583351369729,
        "uname": "{username1}"
      },
      {
        "uid": 6884583351373824,
        "uname": "{username2}"
      },
      {
        "uid": 6884583355506688,
        "uname": "{username3}"
      },
      {
        "uid": 6884583355506689,
        "uname": "{username4}"
      },
      {
        "uid": 6884583355510784,
        "uname": "{username5}"
      },
      {
        "uid": 6884583359643648,
        "uname": "{username6}"
      }
    ]
  }
}
```

As you can see, the response body has one attribute named `users` that contains a `json` array of users. If there is no user in the server, the array will be an empty array.

### Update Message

Update messages are messages that are sent by the server to the clients to update clients about changes that happens in the server. This is for when a user sends a chat message to the server, a new user registers, or when a user disconnects. The client can do whatever they want to do with the update messages, but they’re there so that the clients can display up-to-date information from the server. 

Before the client can establish a proper `Update-Message` connection. The `Response-Request` **must be established first and have a registered user**. Once established, we need to make another socket connection to connect to the server to serve as an `Update-Message` connection. It is also important to note that this new socket connection **needs to send keep alive messages**. The keep alive configuration is flexible as long as it is sending it. To set up this new keep alive connection as an `Update-Message` connection, we must send the following request using this new socket connection **not the response-request** connection:

```json
{
	"request-id": "su", 
	"params": {
		"main-user": "<main-username>"
	}
}
```

As you can see, this request needs a `main-username` as a parameter. This is why the `Response-Request` needs to be established first and a user needs to be registered (which returns the generated user). 

When this request is sent and the main user exists, this connection will be set as a `supplemental` connection of the `Response-Request` connection and the server will send update messages to it.

#### Messages

The following are the messages that a client must anticipate from the server.

##### New User Update Message

This update message is sent to each of the client when a new user is registered so that clients can update their user list (if being tracked). Here’s what the update message looks:

```json
{
  "update-message": "nu",
  "body": {
    "user": {
      "uid": 6884583359643649,
      "uname": "{username}"
    }
  }
}
```

##### New Chat Update Message

This is sent by the server to all the clients when a user sends a new chat message. This excludes the sender of the message.

```json
{
  "update-message": "nm",
  "body": {
    "sender": {
      "uid": 6884583359643650,
      "uname": "{sender username}"
    },
    "message": {
      "uid": 6884583363784704,
      "content": "{some message}"
    }
  }
}
```

##### User Disconnect Update Message

This update message is sent by the server when a user (client) disconnects from the server.

```json
{
  "update-message": "ud",
  "body": {
    "user": {
      "uid": 6884583363784705,
      "uname": "{username}"
    }
  }
}
```



