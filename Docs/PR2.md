# Project Report: 2

Our application `Project-Voxx` is a global chat application where a random user can drop in and chat with other users that are connected and registered to the server.

Project Voxx is managed using `Gradle` and is subdivided into three modules: `voxx-commons`, `voxx-client`, and `voxx-server`. For these modules, `voxx-commons` contains code that both the client and the server can implement. The module `voxx-client` contains the code for the client, and the `voxx-server` module contains the code for the server.

Since the previous project report, server implementation has been modified, and initial work on the java client in `voxx-client` has begun.


# Project-Voxx Server Protocol

This documentation specifies what the server for Project-Voxx is going to accept with its corresponding response and messages that a server could send. Therefore, a client needs to anticipate non-requested messages from the server.

### Server-Client Communication Overview

At its core, Project-Voxx protocol is transported through `websocket`. The communication is text based, therefore, all the messages received and sent will be in the format `String`. To have a clear and easy to parse message, we’ve opted to use the `json` syntax. However, the `json` syntax needs to be flattened and `must not have any break line`. Use the following regex replacement on your `json` string before sending requests to the server:

**Java**

```java
"<your json string here>".replaceAll("\\s{2,}|\\n","");
```

**Python**

```python
import re

re.sub(r'\s{2,}|\n', '', "<your json string here>")
```

#### Server Connection

When a client connects to the server, the connection is not affiliated with any user until the client socket sends a request `ru` or `register user` to the server. If the user is with the same username is not registered, the socket connection will be bound to that user and every request from or to the client is in the context of that user.

#### Request format

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

We can see that the `response-id` is `-1` meaning that the request does not exist. And a body attribute with
a `message`.

For valid requests, each request type will have their own unique response, and they will be specified below.

















### Requests

The following headers will show specifications of each request.

#### Register User

Here is the request body that you must send to the server to have a valid register user request

```json
{
  "request-id": "ru",
  "params": {
    "uname": "{username}"
  }
}
```

Since Project-Voxx is a non-persistent drop in public chat channel, users do not need a password. The only requirement for user registration is a username and that it’s not taken by any other users in the live server.

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

#### Sending a Chat Message

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

















#### Getting User List

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





### Update Messages

Update messages are messages that are sent by the server to the clients to update clients about changes that happens in the server. This is for when a user sends a chat message to the server, a new user registers, or when a user disconnects. The client can do whatever they want to do with the update messages, but they’re there so that the clients can display up-to-date information in the server.

#### New User Update Message

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

#### New Chat Update Message

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











#### User Disconnect Update Message

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
### Client-Connection Overview

Currently, the Voxx java client connection is implemented utilizing the class `ReqResClientConnection` and, optionally, `UpdateMessageConnection`.

`ReqResClientConnection` is used to establish the client connection to the server over a network. It provides methods for sendings requests, as well as checking the status of the connection. The client is able to connect to the server via a TCP socket, where it can send requrests to the server using `json` objects.

`UpdateMessageConnection` is used to establish the client connection to the server over a network. The client connects to the server via a TCP socket, where it then listens for incoming message from the server. This connection is implemented using a while loop that listens persistently, where as stated above, allows for the client to be updated on changes happening in the server such as a list of current users in the server and when a user leaves the server. `UpdateMessageConnection` implements `Runnable`, which executes as a separate thread and allows for the client to receive updates.

When the `run()` method is called, the client enters a loop and reads messages from the server using object `BufferedReader`. In instances where new messages arrive, it is parsed as a `json` object.

### Client-Controller Overview

Currently, the Voxx java client connection is implemented using the classes `ChatController` and `LoginController`. `ChatController` is responsible for controlling the user-interface experience of the application. It handles the display and sending of messages, updating user lists, and connecting to the server to receive updates.

`LoginController` is responsible for controlling the login screen of the application. First, it will check if the username that is entered is valid. We have designated a valid username to be between 4-7 characters, also allowing for numbers and underscores, however the username cannot start with numbers first. It then sends a request to the server to create a new user with the entered username. If the request is successful, the chat window will launch.

It is important to note that the application utilizes the default local host, and the login display initially indicates a red dot in the bottom left corner signifying there is no connection established between the client and server. This dot will display as orange when attempting to connect, and finally green when a connection has been established.

### Previous Issues/Changes Made Since PR1

The uid `asLong()` method was providing a timestamp that was a day ahead of the current time for an event. This was corrected by subtracting an epoch.

#### Protocol change for sending a chat message:

Message-uid as listed in PR1 determined to be redundant, so it was removed. The protocol listed above for sending a chat message reflects what is current.

#### Protocol change for update-message:

The key for update-message was changed to nm, previously ns, for clarity and ease of understanding, where:

<br>nm = new message </br>

<br>nu = new user </br>

<br>ud = user disconnect</br>

### Current issues:

Currently, there is a problem with the disconnect if the application is closed prematurely.

In instances where this occurs, the client does not disconnect. To resolve this issue, we may add a check on the server to verify if a client is still connected, and if not, remove automatically.

### UML Diagram

The following image contains the UML diagram of all the implemented code that we have so far. The server UML has not changed, however we have added the client UML diagram:
![Server UML](D:\Documents\Projects\Java\Project-Voxx\Project-Voxx-Diagram.png)

![Client UML](https://github.com/CyR1en/Project-Voxx/blob/master/Docs/voxx-client-uml-diagram.png)

For an interactive UML diagram, you can go to [draw.io](https://app.diagrams.net/) and download the UML file for the picture above [here](https://drive.google.com/file/d/1NxCu8vF7bGMvkf3cpKZ9IuZ37EwJ4kb1/view?usp=share_link). To open the UML file, click `File > Open from > Device..` and browse your explorer/finder to find the downloaded UML file.

