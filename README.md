# AF-CRM Server 🚀

A professional backend solution for managing technical service teams, task scheduling, and resource allocation. Designed to streamline operations for technical service providers using a modern, containerized architecture.

## 🛠️ Tech Stack
* **Framework:** Spring Boot 3.x
* **Language:** Java 21 (LTS)
* **Database:** PostgreSQL 16
* **Containerization:** Docker & Docker Compose
* **Persistence:** Spring Data JPA / Hibernate
* **Build Tool:** Maven

## 📋 Key Features (Planned)
- **Team Management:** CRUD operations for technical teams and specialty tracking.
- **Service Scheduling:** Weekly calendar integration for service assignment and field coordination.
- **Task Tracking:** Real-time status updates and reporting for technical tasks.
- **RESTful API:** Clean, documented, and secure endpoints for frontend consumption.

## ⚙️ Quick Start (Docker)

This project is fully containerized. You don't need to install Java or PostgreSQL locally, only **Docker** and **Docker Compose**.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/valeria-jauregui/af-crm-server.git
   cd af-crm-server
   ```

2. **Setup Environment Variables:**
    Create a `.env` file in the root directory (refer to `.env.example`).
    
    ### 🔐 Environment Variables Explained:
    - **`POSTGRES_*`**: Credentials for the isolated Docker PostgreSQL database.
    - **`DB_DOCKER_URL`**: The JDBC bridge URL (`jdbc:postgresql://db:5432/af_crm_db`) allowing the Spring Boot container to talk to the Database container.
    - **`ADMIN_EMAIL` & `ADMIN_PASSWORD`**: The Auto-Seeder uses these to create your initial Master Administrator account on the very first boot. Passwords are automatically BCrypt-hashed.
    - **`JWT_SECRET`**: A highly secure, long Base64 string. The server uses this cryptographic key to "sign" and "verify" user sessions (JSON Web Tokens). If someone tampers with a token, the secret won't match and the server rejects it. 
      > 🔑 **Important:** Do NOT use a hardcoded secret. Generate one securely by running this command in your terminal: 
      > ```bash
      > openssl rand -hex 32
      > ```
      > Paste the output directly into your `.env` file as `JWT_SECRET`.
    - **`GOOGLE_CLIENT_ID`**: Your OAuth2 Google Cloud ID. Our backend needs this to mathematically verify that the "Sign in with Google" `idToken` sent by the Frontend truly belongs to your organization and hasn't been spoofed.

3. **Run the entire stack for Deployment:**
    ```bash
    docker-compose up --build -d
    ```
    The API will be available at `http://localhost:8080` and the database at port `5432`.

## 📂 Project Structure

- `/backend`: Spring Boot source code and Dockerfile.

- `docker-compose.yml`: Orchestration for the API and PostgreSQL.

---
*Developed by Valeria Jauregui Lorda - Computer Engineering Student & Electromechanical Technician.*