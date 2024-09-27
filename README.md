
# Tiny Application with Java Spring Boot

## Introduction
This is my tiny application to demonstrate the basic behaviors of a normal web app using the framework Java Spring Boot. As specified in the Dockerfile, versions used are:
- **Java 21**
- **Maven 3.9.9** (4.0.0 also works but not available in DockerHub)

## Concepts
The concepts that we are going to go through in this app are:
- A simple business flow to signup, login, get user-self, get other users.
- Basic JWT authentication, authorization with Spring Security.
- OAuth2 with Google ([reference](https://medium.com/@sallu-salman/implementing-sign-in-with-google-in-spring-boot-application-5f05a34905a8)).
- Write unit tests with Mockito and set up a GitHub pipeline (TO-DO).

## Start Application
**Notice:** In order for the app to function properly, you have to fill in the values specified in `.env.example` and put them in a file called `.env`.

### Native Environment
```sh
mvn spring-boot:run
```

### Docker Environment
```sh
docker compose up --build
```


Feel free to let me know if you need any more adjustments!
