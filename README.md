# Remote key-value storage using RMI
## structure
- MessengerService: 
Interface defining shared behaviors between Server and Client
- MessengerServiceImpl:
Implement key-value storage with HashMap
- Client
Get remote object MessengerService from Registry
- Server
Creat Registry and bind remote object to Registry
Data synchronization with replicas

## compile
javac *.java

## run
To run a server:
```java Server <port of current server> <ports of all server replicas>```
or
```java Server <port of current server>``` in which case the port must within default ports of [8080, 8081, 8082, 8083, 8084]

e.g. ```java Server 8080 8080 8081 8082 8083 8084``` 
or ```java Server 8083 8080 8081 8082 8083 8084```

on Terminal 2:
`java Client <port>`

