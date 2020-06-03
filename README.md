# Industry 4.0 - Model Management Platform - Backend
The backend project of the Industry 4.0 - Model Management Platform.

To run the spring application locally, a PostgreSQL database has to be configured according to the information in the application-local.properties.
A detailed documentation of the REST API can be found [here](http://192.168.209.139:8080/v1/docs/index.html) or as a swagger specification [here](http://192.168.209.139:8080/swagger-ui.html).

# Install

## Clone the repository
Clone the repo but make sure a valid ssh key has been set in gitlab or use https
```bash
$ git clone git@gitlab-as.informatik.uni-stuttgart.de:hirmerpl/Enpro-Industrie_4.0_Model_Management.git
```

go into app's directory
```bash
$ cd Enpro-Industrie_4.0_Model_Management
```

# Build
Build the spring application

```bash
$ ./gradlew build
```

# Run

Run the spring application locally so it is available on [http://localhost:8080](http://localhost:8080)

```bash
$ java -jar build/libs/mmp-backend-boot.jar -Dspring.profiles.active=local
```

## Run in production mode
Run the spring application in production mode so it uses the production database.

```bash
$ java -jar build/libs/mmp-backend-boot.jar -Dspring.profiles.active=production
```

## Run in production mode with sample data
Run the spring application in production mode so it uses the production database and initialize the database with sample data.

```bash
$ java -jar build/libs/mmp-backend-boot.jar -Dspring.profiles.active=productionInit
```

For more information visit
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Gradle](https://gradle.org/)

