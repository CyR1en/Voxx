# Voxx Run Parameter and Configuration

Instructions on how to run every Voxx component with or without an IDE. A documentation on how to build it for distribution is also provided below.

## Running on IDE

#### Voxx client and server on IntelliJ

- Clone project from VCS using the following git repo

  ```
  https://github.com/CyR1en/Voxx.git
  ```

- This should automatically open a new editor window and import the project as a Gradle project

- To run the server:

  - Locate `voxx-server\src\main\java\com.cyr1en.voxx.server\Launcer.java` on the project hierarchy 
  - Click the play button next to the main method.

- To run the client:

  - Locate `voxx-client\src\main\java\com.cyr1en.voxx.client\VoxxApplication.java` on the project hierarchy 
  - Click the play button next to the main method.

- For the voxx cli client, I recommend not using PyCharm to run it since it’s a command line application.

#### Voxx CLI

- Clone the voxx cli repo

  ```
  git clone https://github.com/CyR1en/voxx-client-cli.git
  ```

- Change directory to voxx-client-cli

  ```
  cd voxx-client-cli
  ```

- Make a virtual environment

  ```
  python -m venv venv
  ```

- Activate the venv

  - For windows:

    ```
    .\venv\Scripts\activate
    ```

  - For mac:

    ```
    source ./venv/bin/activate
    ```

- Install requirements

  ```
  pip install -U -r requirements.txt
  ```

- Run voxx module (not installing voxx-cli)

  ```
  python -m voxx -h
  ```

## Building Voxx

- To build and run Voxx server (Assuming the repo is cloned)

  ```
  ./gradlew clean :voxx-server:build
  
  java -jar build/libs/voxx-server-<version>.jar
  ```

- To build the client without installer

  ```
  ./gradlew clean :voxx-client:jpackage -PskipInstaller=True
  ```

  This will build an executable file in `voxx-client/builds/jpackage`

- To build command line executable for `voxx-cli` (assuming the voxx-cli-client is cloned, a venv exists, and the requirements are installed)

  ```
  python setup.py install
  
  voxx-cli -h
  ```

​		(Only gonna be available when venv is active)