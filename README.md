# AgriMate Backend (Spring Boot)

The main backend for AgriMate. Handles auth, business logic, all CRUD, and orchestrates
the scan flow (Cloudinary upload → ML service → knowledge-base lookup → persist). It is
the **only** service the mobile app and admin dashboard talk to.

- Java 25 · Spring Boot 4 · Spring Security + JWT · Spring Data JPA
- PostgreSQL · Jackson 3

## Run
```bash
createdb agrimate   # once, if it doesn't already exist
DB_USERNAME=postgres DB_PASSWORD=12345 ./mvnw spring-boot:run
# → http://localhost:8082
```
`DB_URL`/`DB_USERNAME`/`DB_PASSWORD` default to a local `agrimate` database on `localhost:5432`
with the standard `postgres` role — override via env vars for any other environment.

## Seeded on first run
- The 5-disease knowledge base (EN/SI/TA) — *content needs expert review before production*.
- A default admin: phone `0700000000`, password `admin123` (override via `ADMIN_*`). **Change in production.**

## Key endpoints
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/register` `/login` `/refresh` | public | JWT access + refresh |
| GET/PATCH | `/api/users/me` | any | profile |
| CRUD | `/api/farms`, `/api/farms/{id}/crops`, `/api/crops/{id}` | farmer | farm/crop mgmt |
| POST | `/api/scans` | any | **multipart** `image` + `farmId`/`cropId`/`latitude`/`longitude` |
| GET | `/api/scans`, `/api/scans/{id}` | any | history (paged, `?disease=`) |
| GET | `/api/diseases`, `/api/diseases/{key}` | public | knowledge base |
| POST/GET | `/api/questions`, `/api/questions/{id}/answers` | role-aware | ask-an-agronomist |
| GET | `/api/admin/outbreaks` `/users` `/analytics`, PATCH `/users/{id}` | ADMIN | dashboard |

## Scan response (critical path)
Returns `predictedDisease`, `confidence`, `top3[]`, full `disease` treatment info,
plus `lowConfidence` (top < 0.6 or top-2 close → UI should suggest an agronomist) and
`modelMocked` (true while the ML service has no trained model yet).

## Config
All secrets come from environment variables — see `.env.example`. Nothing is hard-coded.

## Architecture
```
controller → service → repository (JPA)            client/MlClient → FastAPI ML
                    ↘ service/StorageService → Cloudinary | local disk (/uploads)
security: JwtAuthenticationFilter → SecurityContext (principal = User entity)
```
