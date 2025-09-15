# Spring REST API Starter Template

A production-ready Spring Boot template for building modern RESTful APIs.
It provides a clean project structure, recommended libraries, and best practices for development, testing, and deployment.
This template is designed to help you quickly start new backend projects and streamline your workflow.

## 🚀 Key features

- 🔒 **Authentication and Authorization** with Spring Security & JWT (JSON Web Token)
- 📂 **File Storage** with MinIO
- 🚀 **Deployment** with Docker Compose
- 🎨 **Code formatting** with Prettier
- 🧪 **Testing** with JUnit, Mockito & Testcontainers
- 🔄 **CI/CD workflows** with GitHub Actions (currently including testing workflow only)

## 🛠️ Prerequisites

You need to install all of these before continuing:

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi) (if not installed, you can use `mvnw` instead)
- [Node.js](https://nodejs.org/en/download)
- [Docker & Docker Compose](https://docs.docker.com/get-docker/)

The following tools will be downloaded & started by Docker Compose. If not using Docker Compose, you will need to install them manually in your environment:

- [MinIO](https://min.io/download)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Redis](https://redis.io/download)

## 🚀 Getting started

1. Clone the repository

```bash
git clone https://github.com/dung204/spring-rest-api-starter-template.git
```

2. Change directory into the project folder

```bash
cd spring-rest-api-starter-template
```

3. Prepare the environment variables:

3.1. If using Docker Compose, create a `.env` file in the root directory of the project. You can use the `.env.example` file as a template. Make sure to fill in the required environment variables. These variables is required for Docker Compose

3.2. Create a configuration file, named `application-dev.yml`, place it at `src/resources/`. The content of this file should follow the example file (`application-dev-example.yml`)

4. Use `npm` to install `husky`, `commitlint`, `prettier` & `lint-staged`:

```
npm install
```

5. Install the dependencies & build the project

```bash
mvn clean install
```

6. Start the application

```bash
mvn spring-boot:run
```

7. Open `http://localhost:4000/api/v1/docs` to see the OpenAPI documentation of this REST API. You can configure the server port in `application-dev.yml`.

## 📦 Libraries (dependencies)

Core libraries:

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Web](https://spring.io/guides/gs/serving-web-content/)
- [Spring Security](https://spring.io/guides/gs/securing-web/): handling authentication and authorization
- [JJWT](https://github.com/jwtk/jjwt): JSON Web Token for Java
- [Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/): creating entities (SQL tables) and repositories
- [Spring DevTools](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-devtools)
- [Lombok](https://projectlombok.org/): generating boilerplate code (constructors, getters, setters, etc.)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [Jedis](https://github.com/redis/jedis): Redis Java client
- [MinIO Java SDK](https://github.com/minio/minio-java)

---

Testing libraries:

- [JUnit 5](https://junit.org/junit5/): unit testing framework
- [Mockito](https://site.mockito.org/): mocking framework for unit tests
- [Testcontainers](https://www.testcontainers.org/): providing throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container
- [Spring Boot Test](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-testing): testing utilities for Spring Boot applications

---

Formatters & Linters & Misc. tools:

- [Prettier](https://prettier.io/): code formatting (require Node.js)
- [Husky](https://github.com/typicode/husky): managing Git hooks (require Node.js)
- [lint-staged](https://github.com/okonet/lint-staged): run linters on pre-committed files (require Node.js)
- [commitlint](https://commitlint.js.org/#/): lint commit messages (require Node.js)

## 🧪 Testing

- To run unit tests, use the following command:

```bash
mvn test -Dgroups=unit
```

- To run integration tests, use the following command:

```bash
mvn test -Dgroups=integration
```
