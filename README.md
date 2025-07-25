# Shrtn – URL Shortener (Backend)

`shrtn-backend` is the backend service for the **Shrtn  URL – Shortener Application**, built with **Spring Boot**. It provides secure REST APIs for authentication, URL shortening, and analytics tracking. With integrated **Spring Security** and **JWT**, the service ensures a safe and robust environment for users to create and manage short links.

## Features
- **Secure Authentication** – Implemented using Spring Security and JWT.  
- **URL Shortening** – Convert long URLs into short, shareable links.  
- **Redirection Service** – Automatically redirects users from a short URL to its original destination.  
- **Click Analytics** – Track the number of clicks for each short URL.  
- **Date-wise Analytics** – Store and retrieve click count analytics by date for better insights.  
- **User Management** – Each user can create and manage their own set of short URLs.  

## Tech Stack
- **Backend:** Spring Boot (REST APIs)  
- **Security:** Spring Security, JWT (JSON Web Tokens)  
- **Database:** PostgreSQL  
- **ORM:** JPA (Hibernate implementation)  
- **Build Tool:** Maven  
- **Others:** Java 17, Lombok  

## Related Repositories
- **Frontend:** [Shrtn Frontend (React)](https://github.com/balaji259/Shrtn-frontend)

## Project Structure
```text
shrtn-backend/
├── .mvn/                      # Maven wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/shrtn/    # Core application packages (controllers, services, etc.)
│   │   └── resources/         # Application properties, static files, templates
│   └── test/
│       └── java/com/shrtn/    # Unit and integration tests
├── .gitattributes
├── .gitignore
├── Dockerfile                 # Docker configuration file
├── mvnw                       # Maven wrapper script (Linux/Mac)
├── mvnw.cmd                   # Maven wrapper script (Windows)
├── pom.xml                    # Maven build configuration
└── README.md                  # Project documentation
```


## API Highlights
### Home API
- `GET /` – Health check endpoint to verify the backend is running.

### Authentication APIs (Public)
- `POST /api/auth/register` – Register a new user.
- `POST /api/auth/login` – Authenticate a user and return a JWT token.

### URL APIs (Protected – Requires JWT)
- `POST /api/urls/shorten` – Create a short URL from an original URL for the authenticated user.
- `GET /api/urls/myurls` – Retrieve all short URLs created by the authenticated user.
- `GET /api/urls/analytics/{shortUrl}` – Get date-wise click analytics for a specific short URL (requires start and end date as query params).
- `GET /api/urls/totalClicks` – Get total clicks for all URLs of the authenticated user within a given date range.

### Redirection API (Public)
- `GET /{shortUrl}` – Redirects the user to the original URL corresponding to the short URL.

## Getting Started
### Prerequisites
- **Java 17**  
- **Maven 3.8+**  
- **PostgreSQL** (default, or configure MySQL if preferred)  
- **IDE:** IntelliJ IDEA / Eclipse / VS Code

### Installation & Setup

```bash
# Clone the repository
git clone https://github.com/balaji259/Shrtn-backend
cd shrtn-backend

# Build and run
mvn clean install
mvn spring-boot:run
```


## Configuration
Update the `src/main/resources/application.properties` file with your own values:

```properties
spring.application.name=your_app_name

# Database Configuration (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# JPA / Hibernate
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt.secret=your_secret_key
jwt.expiration=expiration_time
```
Note: Ensure PostgreSQL(Or MySQL) is running and the database is created before starting the app.
