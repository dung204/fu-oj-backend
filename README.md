# FPT University Online Judge (FU-OJ) Backend

The REST API for FPT University Online Judge (FU-OJ) - a platform for practicing programming exercises, made for FPT University. This project is the Capstone Project for the Software Engineering program at FPT University.

## 👥 Authors

The project is developed by group 128 of the SEP490 (Capstone Project) course at FPT University. The team members are:

- Hồ Anh Dũng - HE181529 - [@dung204](https://github.com/dung204)
- Hoàng Gia Trọng - HE172557 - [@GiaTrongHocBe](https://github.com/GiaTrong2003)
- Phạm Ngọc Tùng Lâm - HE173556 - [@lampnthe173556](https://github.com/lampnthe173556)
- Lê Đức Đạt - HE171371 - [@LeDatFPT](https://github.com/LeDatFPT)
- Lê Minh Chiến - HE141150 - [@MilkOCD](https://github.com/MilkOCD)

## 📋 Features

> Working in progress... 🚧

## 🛠️ Prerequisites

You need to install all of these before continuing:

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi) (if not installed, you can use `mvnw` instead)
- [Node.js](https://nodejs.org/en/download) (if using `npm`, `yarn` or `pnpm`) or [Bun](https://bun.sh/) (if using `bun`)
- [Docker & Docker Compose](https://docs.docker.com/get-docker/)

The following tools will be downloaded & started by Docker Compose. If not using Docker Compose, you will need to install them manually in your environment:

- [MinIO](https://min.io/download)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Redis](https://redis.io/download)

## 🚀 Getting started

1. Clone the repository

```bash
git clone https://github.com/dung204/fu-oj-backend.git
```

2. Change directory into the project folder

```bash
cd fu-oj-backend
```

3. Create a `.env` file in the root directory of the project. You can use the [`.env.example`](.env.example) file as a template. Make sure to fill in the required environment variables, **and delete all comments**. These variables are required for Docker Compose

4. Install `husky`, `commitlint`, `prettier` & `lint-staged` using the following command (you only need to do this once):

> 📝 You can replace `npm` with `yarn` or `pnpm` or `bun` if you prefer

```
npm install
```

5. Start the application

```bash
mvn spring-boot:run
```

6. Open `http://localhost:4000/api/v1/docs` to see the OpenAPI documentation of this REST API. You can configure the server port in `application-dev.yml`.

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
mvn clean test -Dgroups=unit
```

- To run integration tests, use the following command:

```bash
mvn clean test -Dgroups=integration
```
