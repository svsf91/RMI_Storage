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

## compile
javac *.java

## run
on Terminal 1:
```java Server <port>```

on Terminal 2:
`java Client <port>`

## example
