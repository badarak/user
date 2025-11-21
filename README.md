# User service
The aim of this service is to manage users having CRUD features.
It is build around the flowing technical stack:
- Java 21 + Maven 3, 
- Architecture hexagonal (domain + infrastructure), 
- Spring Boot 3, 
- JPA (Postgres),
- AOP logging,
- Pagination, tests MockMvc

Modules
- domain
- infrastructure

Install and Run
- Configure PostgreSQL using docker:
```
mvn clean install

docker run --name badarak-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=users -d -p 5432:5432 postgres
```
- swagger
```
http://localhost:8080/user/swagger-ui/index.html#/
```
