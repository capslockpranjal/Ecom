# Zenvy Backend

Spring Boot backend for a role-based e-commerce platform with support for customer and seller accounts, admin moderation, category metadata, product variations, JWT authentication, and local image storage.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL
- Jakarta Validation
- Spring Mail
- springdoc OpenAPI / Swagger UI
- Maven

## Features

- JWT-based authentication with access and refresh tokens
- Role-based authorization for `ADMIN`, `SELLER`, and `CUSTOMER`
- Customer and seller registration flows
- Account activation, password reset, resend activation, and logout
- Admin management for customers, sellers, and product activation
- Category and metadata field management
- Seller product and variation management
- Customer product browsing, product details, and similar products
- Customer and seller profile management
- Customer address management
- File-based image storage for profile and product variation images
- Internationalized response messages

## Project Structure

```text
src/main/java/org/example/zenvybackend
├── admin        # Admin APIs and services
├── bootstrap    # Startup role/admin seeding
├── category     # Category tree and metadata management
├── common       # Shared config, exceptions, responses, utilities, storage
├── config       # Jackson and app configuration
├── product      # Product, variation, and customer product browsing
├── security     # JWT, filters, security config, auth helpers
└── user         # Auth, profile, seller, customer, address flows
```

## Requirements

- Java 17+
- Maven 3.9+
- MySQL 8+

## Configuration

The current application expects MySQL, SMTP, JWT, and image storage settings through Spring properties.

Recommended approach:

1. Copy the existing properties into environment-specific configuration.
2. Replace hardcoded secrets with environment variables or a local untracked config file.
3. Do not commit real database passwords, mail credentials, JWT secrets, or admin passwords.

Important properties used by the app:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/zenvy_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_SMTP_USER
spring.mail.password=YOUR_SMTP_PASSWORD

jwt.secret=YOUR_BASE64_ENCODED_SECRET
jwt.access.expiration=900000
jwt.refresh.expiration=86400000

admin.email=admin@zenvy.com
admin.password=CHANGE_ME

app.images.base-path=uploads
app.images.public-base-url=/files
app.images.storage-type=filesystem
app.images.max-file-size-bytes=5242880

app.images.s3.bucket=YOUR_S3_BUCKET
app.images.s3.region=YOUR_AWS_REGION
app.images.s3.public-base-url=https://YOUR_S3_BUCKET.s3.YOUR_AWS_REGION.amazonaws.com
app.images.s3.endpoint=
```

Notes:

- The application currently auto-creates roles and an admin user at startup.
- Hibernate DDL mode is set to `update`.
- Uploaded files are stored on the local filesystem under `uploads/`.
- Set `app.images.storage-type=s3` to store images in Amazon S3 instead of the local filesystem.
- When S3 storage is enabled, provide AWS credentials through the default AWS credential chain, preferably via an IAM role in production.

## Running Locally

1. Create a MySQL database:

```sql
CREATE DATABASE zenvy_db;
```

2. Configure database, mail, JWT, and admin properties.

3. Start the application:

```bash
./mvnw spring-boot:run
```

Or:

```bash
mvn spring-boot:run
```

4. The API will be available at:

```text
http://localhost:8080
```

### Switch Image Storage To S3

1. Set the following environment variables:

```bash
APP_IMAGES_STORAGE_TYPE=s3
APP_IMAGES_S3_BUCKET=YOUR_S3_BUCKET
APP_IMAGES_S3_REGION=YOUR_AWS_REGION
APP_IMAGES_S3_PUBLIC_BASE_URL=https://YOUR_S3_BUCKET.s3.YOUR_AWS_REGION.amazonaws.com
```

2. Provide AWS credentials:

```bash
AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY
AWS_SECRET_ACCESS_KEY=YOUR_SECRET_KEY
```

3. Ensure the bucket policy allows reads for the image URLs you return to clients, or use a CDN/public bucket URL in `APP_IMAGES_S3_PUBLIC_BASE_URL`.

## Docker

The project includes a multi-stage Docker build and a `docker-compose.yml` for local containerized runs.

### Build the image

```bash
docker build -t YOUR_DOCKERHUB_USERNAME/zenvy-backend:latest .
```

### Run the container against an existing MySQL instance

```bash
docker run -d \
  --name zenvy-backend \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/zenvy_db \
  -e SPRING_DATASOURCE_USERNAME=YOUR_DB_USER \
  -e SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD \
  -e SPRING_MAIL_HOST=smtp.gmail.com \
  -e SPRING_MAIL_PORT=587 \
  -e SPRING_MAIL_USERNAME=YOUR_SMTP_USER \
  -e SPRING_MAIL_PASSWORD=YOUR_SMTP_PASSWORD \
  -e JWT_SECRET=YOUR_BASE64_ENCODED_SECRET \
  -e JWT_ACCESS_EXPIRATION=900000 \
  -e JWT_REFRESH_EXPIRATION=86400000 \
  -e ADMIN_EMAIL=admin@zenvy.com \
  -e ADMIN_PASSWORD=YOUR_STRONG_ADMIN_PASSWORD \
  -e APP_IMAGES_BASE_PATH=/app/uploads \
  pranjalttn/zenvy-backend:latest
```

### Run with Docker Compose

1. Copy `.env.docker.example` to `.env` and replace every placeholder value with real secrets before startup.
2. Start the stack:

```bash
docker compose up --build
```

This starts:

- `mysql` on port `3306`
- `app` on port `8080`

Uploads are persisted through the local `uploads/` directory.

If you switch to S3 storage, set `APP_IMAGES_STORAGE_TYPE=s3` and the `APP_IMAGES_S3_*` variables in `.env`. The local `uploads/` volume will then be unused.

### Push to Docker Hub

```bash
docker login
docker build -t pranjalttn/zenvy-backend:latest .
docker push pranjalttn/zenvy-backend:latest
```

Important:

- Do not publish real credentials inside `application.properties`.
- Rotate any secrets that were previously committed.
- Prefer environment variables for database, mail, JWT, and admin credentials.
- `JWT_SECRET` must be a strong base64-encoded value, not plain text.
- Set `SPRING_JPA_HIBERNATE_DDL_AUTO` explicitly for the target environment. The compose file uses `update` for local development, while the app default is `none`.
- For S3 storage, prefer IAM roles over static AWS access keys in production.

## API Documentation

Swagger UI and springdoc API docs are currently disabled by configuration.

## Authentication and Roles

Public auth endpoints are under `/auth/**`.

Roles in the system:

- `ADMIN`
- `SELLER`
- `CUSTOMER`

Authorization is enforced with Spring Security and method-level `@PreAuthorize`.

Use the bearer token returned by the login endpoint:

```http
Authorization: Bearer <access-token>
```

## Main API Areas

### Auth

- `POST /auth/register/customer`
- `POST /auth/register/seller`
- `PUT /auth/activate`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/resend-activation`
- `POST /auth/forgot-password`
- `PUT /auth/reset-password`
- `POST /auth/logout`

### Admin

- `GET /admin/customers`
- `GET /admin/sellers`
- `PATCH /admin/customers/{id}/activate`
- `PATCH /admin/customers/{id}/deactivate`
- `PATCH /admin/sellers/{id}/activate`
- `PATCH /admin/sellers/{id}/deactivate`
- `GET /admin/products`
- `PUT /admin/products/{id}/activate`
- `PUT /admin/products/{id}/deactivate`

### Categories

- `POST /categories`
- `GET /categories`
- `PUT /categories/{categoryId}`
- `GET /categories/metadata-field`
- `POST /categories/metadata-field`
- `POST /categories/{categoryId}/metadata`
- `GET /categories/seller`
- `GET /categories/customer`
- `GET /categories/customer/{categoryId}/filters`

### Seller Products

- `POST /products`
- `POST /products/variation`
- `PUT /products/{productId}`
- `PUT /products/variation/{variationId}`
- `DELETE /products/{productId}`
- `GET /products`
- `GET /products/{productId}/variations`

### Customer Product Browsing

- `GET /customer/products`
- `GET /customer/products/{productId}`
- `GET /customer/products/{productId}/similar`

### Profiles and Address

- `GET /customer/profile`
- `PATCH /customer/profile`
- `PATCH /customer/password`
- `POST /customer/address`
- `GET /customer/address`
- `PATCH /customer/address/{id}`
- `DELETE /customer/address/{id}`
- `GET /seller/profile`
- `PATCH /seller/profile`
- `PATCH /seller/password`
- `PATCH /seller/address`

### File Access

- `GET /files/users/{userId}/profile`
- `GET /files/products/{productId}/variations/{variationId}/primary`
- `GET /files/products/{productId}/variations/{variationId}/secondary/{index}`

## Product Variation Metadata Rules

Variation metadata follows category metadata constraints.

- At least one metadata field is required.
- Each provided field must be valid for the product category.
- Each provided value must be one of the allowed values for that field.
- All variations of the same product must use the same metadata key structure.
- Duplicate variations with the same metadata are rejected.

Example:

```json
{
  "productId": "PRODUCT_UUID",
  "quantityAvailable": 10,
  "price": 19999.0,
  "metadata": {
    "color": "Blue",
    "size": "M"
  }
}
```

## Images

The backend supports:

- Customer and seller profile images
- Product variation primary images
- Product variation secondary images

Current image constraints in code:

- Allowed extensions: `jpg`, `jpeg`, `png`, `bmp`
- File size limit: 5 MB
- Images are stored on disk under `uploads/`

## Testing

Run the test suite with:

```bash
./mvnw test
```

Existing tests currently cover:

- admin service logic
- category service logic
- product service logic
- application context loading

## Notes for Improvement

- Move secrets out of `application.properties`
- Add environment-specific config profiles
- Add database migration tooling such as Flyway or Liquibase
- Add Docker support for the app and MySQL
- Expand controller/integration test coverage
- Add rate limiting and stronger audit logging around auth-sensitive flows
