# AF-CRM Server 🚀

A professional backend solution for managing technical service teams, task scheduling, and resource allocation. Designed to streamline operations for technical service providers using a modern, containerized architecture.

## 🔗 Related Projects
* **Frontend:** [af-crm-client](https://github.com/valeryjl/af-crm-client)

## 🛠️ Tech Stack
* **Framework:** Spring Boot 4.x
* **Language:** Java 21 (LTS)
* **Database:** PostgreSQL 16
* **Containerization:** Docker & Docker Compose
* **Persistence:** Spring Data JPA / Hibernate
* **Documentation:** Swagger/OpenAPI 3

## 📋 Key Features
- **User Unification:** Unified model for Admins and Technicians with preference tracking (`theme`, `customConfiguration`).
- **Service Scheduling:** Automate task generation and calendar management.
- **Task Tracking:** Real-time reporting and status updates for technical services.
- **Interactive Documentation:** Native Swagger UI for effortless API exploration and testing.

## 📖 API Documentation
Once the server is running, you can access the documentation at:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Docs:** `http://localhost:8080/v3/api-docs`

## ⚙️ Quick Start (Docker)

This project is fully containerized. You only need **Docker** and **Docker Compose**.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ValeryJL/af-crm-server.git
   cd af-crm-server
   ```

2. **Setup Environment Variables:**
    Create a `.env` file in the root directory (refer to `.env.example`).
    
    ### 🔐 Environment Variables Explained:
    - **`POSTGRES_*`**: Credentials for the isolated Docker PostgreSQL database.
    - **`DB_DOCKER_URL`**: JDBC bridge URL (`jdbc:postgresql://db:5432/af_crm_db`).
    - **`ADMIN_EMAIL` & `ADMIN_PASSWORD`**: Auto-Seeder credentials for the master account.
    - **`JWT_SECRET`**: Signature key for JWT sessions. Generate with `openssl rand -hex 32`.
    - **`GOOGLE_CLIENT_ID`**: OAuth2 ID for Google Login verification.

3. **Run the entire stack:**
    ```bash
    docker compose up --build -d
    ```
    The API will be available at `http://localhost:8080`.

## 📂 Project Structure

- `/backend`: Spring Boot source code and Dockerfile.
- `docker-compose.yml`: Orchestration for the API and PostgreSQL.

---
*Developed by Valeria Jauregui Lorda - Computer Engineering Student & Electromechanical Technician.*