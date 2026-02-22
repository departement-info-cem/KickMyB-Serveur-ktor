# KtorMyB

Ktor rewrite of [KickMyB-Server](../KickMyB-Server) in pure Kotlin.

## Stack

| Layer | Library |
|-------|---------|
| HTTP server | [Ktor](https://ktor.io) (Netty engine) |
| HTML templating | Ktor HTML DSL (`kotlinx.html`) |
| ORM | [Exposed](https://github.com/JetBrains/Exposed) |
| Database | H2 (file-backed, same as original) |
| Connection pool | HikariCP |
| Auth | Ktor Sessions + HMAC-signed cookie |
| Password hashing | BCrypt (`at.favre.lib:bcrypt`) |
| Push notifications | Firebase Admin SDK |
| Image resizing | imgscalr |
| Scheduling | Kotlin Coroutines |
| Build | Gradle (Kotlin DSL) |

## Prerequisites

- JDK 21+
- (Optional) a `firebase-service-account-key.json` in the project root for push notifications

## Run

```bash
./gradlew run
```

Server starts on **http://localhost:8080**

## Build fat JAR

```bash
./gradlew installDist
./build/install/KtorMyB/bin/KtorMyB
```

## Configuration

All settings live in `src/main/resources/application.conf` (HOCON).  
Override any value with environment variables or a local override file.

| Key | Default | Description |
|-----|---------|-------------|
| `ktor.deployment.port` | `8080` | HTTP port (env: `PORT`) |
| `database.url` | `jdbc:h2:file:./db/ktormyb` | H2 JDBC URL |
| `session.secret` | *(change me)* | HMAC key for session cookie signing |
| `firebase.configPath` | `./firebase-service-account-key.json` | Path to Firebase service account |

## API Endpoints

### Authentication (`/id/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/id/inscription` | ❌ | Register |
| POST | `/id/connexion` | ❌ | Login |
| POST | `/id/deconnexion` | ✅ | Logout |
| POST | `/enregistrer-jeton-notification` | ✅ | Store Firebase token |

### Tasks (`/tache/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/tache/ajout` | ✅ | Add task |
| GET | `/tache/accueil` | ✅ | List tasks |
| GET | `/tache/detail/{id}` | ✅ | Task detail |
| GET | `/tache/progres/{idTache}/{valeur}` | ✅ | Update progress (0–100) |

### Photos
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/fichier` | ✅ | Upload photo (multipart: `file`, `taskID`) |
| GET | `/fichier/{id}?largeur=N` | ✅ | Download photo (optional resize) |
| GET | `/api/accueil/photo` | ✅ | Task list with photo IDs |
| GET | `/api/detail/photo/{id}` | ✅ | Task detail with photo ID |

### Other
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | ❌ | HTML landing page (Ktor HTML DSL) |
| POST | `/test/notifications` | ❌ | Send test push notification |

## Project structure

```
src/main/kotlin/ca/cem/ktormyb/
├── Application.kt          # Entry point + module wiring
├── config/
│   ├── Database.kt         # HikariCP + Exposed init, dbQuery helper
│   └── Plugins.kt          # Sessions, Auth, ContentNegotiation, StatusPages
├── model/
│   └── Tables.kt           # Exposed table definitions
├── dto/
│   ├── Requests.kt         # Incoming JSON DTOs
│   └── Responses.kt        # Outgoing JSON DTOs
├── exception/
│   └── Exceptions.kt       # Domain exceptions (→ HTTP 400)
├── security/
│   └── Encryptor.kt        # AES-128/ECB task-name encryption
├── service/
│   ├── UserService.kt
│   ├── TaskService.kt
│   ├── PhotoService.kt
│   ├── FirebaseService.kt
│   └── NotificationScheduler.kt  # Coroutine-based daily cron at 03:00
└── routes/
    ├── UserRoutes.kt
    ├── TaskRoutes.kt
    ├── PhotoRoutes.kt
    └── MvcRoutes.kt        # HTML DSL landing page
```
