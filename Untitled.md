# Structure
## MessengerService: 
Interface defining shared behaviors between Server and Client
## MessengerServiceImpl:
Implement key-value storage with HashMap
## Client:
Get remote object MessengerService from Registry
## Server:
Creat Registry and bind remote object to Registry

# Compile
javac *.java

# Usage
## test 5 servers and 1 clients:
```shell
$ java Server 8080 & java Server 8081 & java Server 8082 & java Server 8083 & java Server 8084 & java Client 8080
```
## run a server:
```shell
$ java Server <server port> <replica ports>
```
or
``` shell
$ java Server <port of current server>
```
in which case the port must within default ports of [8080, 8081, 8082, 8083, 8084]:
 
```shell
$ java Server 8080 8080 8081 8082 8083 8084
```
## run a client:
```shell
$ java Client <port>
```
## run a command:
```
GET <key>
PUT <key> <value>
DEL <key>
```

<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE5OTEzNTQ4OTNdfQ==
-->