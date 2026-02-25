# Bookstore Microservices

Mikroservisna aplikacija za upravljanje knjizarom, razvijena kao DevOps projekat.

## Tehnologije

- **Java 21** + **Spring Boot 3.3.5**
- **Spring Cloud Gateway** (API Gateway)
- **PostgreSQL** (DB per service)
- **RabbitMQ** (Message Queue)
- **RxJava 3** (Reaktivna komunikacija)
- **Micrometer + Prometheus + Zipkin** (Observability)
- **Docker + Docker Compose**
- **GitHub Actions** (CI/CD)

## Servisi

| Servis | Port | Opis |
|--------|------|------|
| api-gateway | 8080 | Spring Cloud Gateway - jedina tacka ulaza |
| user-service | 8081 | Upravljanje korisnicima (REST CRUD) |
| book-service | 8082 | Katalog knjiga (REST + RxJava pretraga) |
| order-service | 8083 | Porudzbine (REST + RabbitMQ publisher) |
| review-service | 8084 | Recenzije (REST + RabbitMQ consumer) |

## Komunikacija izmedju servisa

- **REST API**: Gateway rutira sve zahteve; Order/Review servis poziva Book servis
- **Message Queue**: Order servis publishes `OrderCreatedEvent` → RabbitMQ → Review servis
- **RxJava (Reactive)**: Book servis koristi `Observable` za reaktivnu pretragu knjiga

## Pokretanje lokalno

### Preduslovi

- Java 21
- Maven 3.9+
- Docker + Docker Compose
- PostgreSQL (ili koristiti Docker Compose)
- RabbitMQ (ili koristiti Docker Compose)

### Pokretanje sa Docker Compose

```bash
docker-compose up -d
```

### Pokretanje servisa individualno (development)

```bash
# Build svih modula
mvn clean install -DskipTests

# Pokretanje pojedinacnog servisa
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl book-service
mvn spring-boot:run -pl order-service
mvn spring-boot:run -pl review-service
mvn spring-boot:run -pl api-gateway
```

### Pokretanje testova

```bash
# Svi testovi
mvn test

# Testovi jednog servisa
mvn test -pl user-service
```

## API Endpointi (kroz Gateway na portu 8080)

### Users
```
POST   /api/users
GET    /api/users/{id}
GET    /api/users
DELETE /api/users/{id}
```

### Books
```
POST   /api/books
GET    /api/books/{id}
GET    /api/books
GET    /api/books/search?title=...&author=...
PATCH  /api/books/{id}/stock?quantity=...
DELETE /api/books/{id}
```

### Orders
```
POST   /api/orders
GET    /api/orders/{id}
GET    /api/orders/user/{userId}
GET    /api/orders
```

### Reviews
```
POST   /api/reviews
GET    /api/reviews
GET    /api/reviews/book/{bookId}
GET    /api/reviews/user/{userId}
GET    /api/reviews/book/{bookId}/average-rating
```

## Observability

- **Metrics**: `GET /actuator/prometheus` (svaki servis)
- **Health**: `GET /actuator/health` (svaki servis)
- **Traces**: Zipkin UI na `http://localhost:9411`
- **Dashboards**: Grafana na `http://localhost:3000`

## Git Workflow

- `main` grana sadrzi samo stabilan kod
- Svaka nova funkcionalnost se razvija na `feature/<naziv>` grani
- Svaka izmena ide kroz Pull Request pre merge-a u main
