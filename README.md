# SIRS Medical Records

This project was made throughout the first semester of 2019/2020 of the discipline Network and Computer Securirty (SIRS). The project is a software system solution to the Medical Record's problem (gathering Medical Records which store sensitive information about patients which must be kept private for the responsible staff only).

## Installation

To run this project, you need to make sure that both machines (client and server) are running on the same network, otherwise it will not work.
Firstly you need to clone the repository from our git repository [Github](https://github.com/ramiro1003/SIRS_Medical_Records).

```bash
git clone https://github.com/ramiro1003/SIRS_Medical_Records.git
```

Then, go the the 'library' directory and run the following command:

```bash
mvn install
```

Then, to run the server, go to the server directory and type the following command. (First argument is server port, second is the script to populate (optional). We have a populate script sample which you can use (server/resources/populateScript.txt))
Make sure you have a mariadb database running named 'sirs' and fill the file dbconfig.txt under server/resources with the credentials to access mariadb.

```bash
mvn install clean compile exec:java -Dexec.args="[port]:{populateScript.txt}"
```

Then, to run a client, go to the client directory and type the following command.
```bash
mvn install clean compile exec:java -Dexec.args="[serverIp]:[serverPort]:{context}"
```

Please note that context is optional. You can give no context and the system will assume "default" context.

## Have fun
