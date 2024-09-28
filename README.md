# Tiny Application with Java Spring Boot

## Introduction
This is my tiny application to demonstrate the basic behaviors of a normal web app using the framework Java Spring Boot. Versions used are:
- **Java 21**
- **Maven 4.0.0**

## Concepts
The concepts that we are going to go through in this app are:
- A simple business flow to signup, login, get user-self, get other users.
- Basic JWT authentication, authorization with Spring Security.
- OAuth2 with Google ([reference](https://medium.com/@sallu-salman/implementing-sign-in-with-google-in-spring-boot-application-5f05a34905a8)).
- Write unit tests using Mockito and set up a CI pipeline on GitHub ([reference](https://blog.tericcabrel.com/springboot-github-actions-ci-cd/)). Note that this pipeline will focus solely on Continuous Integration (CI), as Continuous Deployment (CD) is delegated to cloud auto-build.
- Set up Cache layer using Redis key-value store.

## Start Application
**Notice:** In order for the app to function properly, you have to fill in the values specified in `.env.example` and put them in a file called `.env`.

**Notice:** 
- The `spring-boot-devtools` module can conflict with Redis Cache. To ensure smooth functionality, you may need to **remove** the DevTools dependency from your `pom.xml` when using Redis Cache. You can choose to work with either Redis or the live-reload feature, but not both simultaneously. 
- To enable/disable the caching feature in your application, follow these steps:
	1.  Open the `DemoApplication.java` file.
	2.  Comment out the `@EnableCaching` annotation to disable as shown below:
	```java
	@SpringBootApplication
	// @EnableCaching
	public class DemoApplication {
	    // Application code
	}
	```
- If you want to re-enable the live-reload feature, simply reinstall the `spring-boot-devtools` dependency in your `pom.xml`.

### Native Environment
```sh
# Either install Redis locally or using Docker
docker compose up redis
mvn spring-boot:run
```
In `.env` file:
```
REDIS_HOST  =  localhost
```

### Docker Environment
```sh
docker compose up --build
```
In `.env` file:
```
REDIS_HOST  =  redis
```

## Access Application
### Swagger-UI
http://localhost:8080/api
### OAuth2 Login
http://localhost:8080/login

Feel free to let me know if you need any more adjustments!
