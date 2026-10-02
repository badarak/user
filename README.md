# User service
The aim of this service is to manage users having CRUD features.
It is build around the flowing technical stack:
- Java 21 + Maven 3, 
- Architecture hexagonal (domain + application + infrastructure), 
- Spring Boot 3, 
- JPA (Postgres),
- Spring Security OAuth2 Resource Server (JWT),
- AOP logging,
- Pagination, tests MockMvc

Modules
- domain
- application
- infrastructure

Install and Run
- Configure the environment, then start PostgreSQL and RabbitMQ using docker:
```
mvn clean install

cp .env.example .env   # then set the credentials and the JWT settings (see Security)
docker-compose up
```
- Run the application (Spring Boot does not read `.env`, export it first):
```
set -a; source .env; set +a
mvn -pl infrastructure spring-boot:run -Dspring-boot.run.profiles=dev
```
- swagger (`dev` profile only, use the **Authorize** button with a token)
```
http://localhost:8080/user/swagger-ui/index.html#/
```

Security

The API is an OAuth2 resource server: every call needs an `Authorization: Bearer <JWT>` header.
There is no authorization server yet: tokens are signed locally with a development RSA key.

A token is accepted only if:
- it is signed with RS256 by the private key matching `JWT_PUBLIC_KEY`,
- `iss` equals `JWT_ISSUER` and `aud` contains `JWT_AUDIENCE`,
- it is not expired (`exp`) nor used too early (`nbf`), with 60 s of clock skew.

Scopes (`scope` claim, space-separated)

| Endpoint                         | Scope         |
|----------------------------------|---------------|
| `GET /api/v1/users/**`           | `users:read`  |
| `POST, PUT, DELETE /api/v1/users/**` | `users:write` |
| `/actuator/health`, `/actuator/info` | public    |
| `/v3/api-docs`, `/swagger-ui`    | public in `dev` profile, denied otherwise |
| anything else                    | denied        |

Errors are returned as `application/problem+json` with a `WWW-Authenticate` header:
`401` for a missing or invalid token, `403` for an insufficient scope.

Environment variables

| Variable               | Used by          | Description |
|------------------------|------------------|-------------|
| `JWT_PUBLIC_KEY`       | API              | RSA public key PEM, base64-encoded on one line |
| `JWT_ISSUER`           | API, script      | expected `iss` claim |
| `JWT_AUDIENCE`         | API, script      | expected `aud` claim, e.g. `user-api` |
| `CORS_ALLOWED_ORIGINS` | API              | comma-separated browser origins, empty = none |
| `JWT_PRIVATE_KEY`      | script only      | RSA private key PEM, base64-encoded. **Dev only, never on a deployed environment** |

Generate a development key pair (`*.pem` files are git-ignored)
```
openssl genrsa -out jwt-private.pem 2048
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
base64 < jwt-public.pem | tr -d '\n'    # -> JWT_PUBLIC_KEY
base64 < jwt-private.pem | tr -d '\n'   # -> JWT_PRIVATE_KEY
```

Get a token and call the API
```
TOKEN=$(scripts/generate-dev-token.sh -s alice -c "users:read users:write" -t 900)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/user/api/v1/users
```
`scripts/generate-dev-token.sh -h` lists the options.
