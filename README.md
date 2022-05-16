# Remote JS execution project

### To run the project
Run Gradle's bootRun command or open project in IntelliJ IDE and run it from there.
Server will be available on the port 8080.

### Description
This project allows for the creation of tasks that execute JavaScript on the server.
It provides api for the client to schedule such tasks, get relevant information 
about their current state and stop them.

### API
Root path to get accessible api via HATEOAS is available at:
- GET localhost:8080/

All api definitions are viewable via Swagger UI at:
- GET localhost:8080/swagger-ui/index.html

### CORS
To test CORS support, go to the swagger-ui with host 127.0.0.1 (http://127.0.0.1:8080/swagger-ui/index.html),
change the server to http://localhost:8080 via ui and make a request via ui, for example to GET /scripts.
It should fail.

### Notes
Only important public methods have comments.\
Tests cover core functionality (use cases).
