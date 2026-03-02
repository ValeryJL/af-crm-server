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
    Create a `.env` file in the root directory (refer to `.env.example` if available) with your database credentials.

3. **Run the entire stack:**
    ```bash
    docker-compose up --build
    ```
    The API will be available at `http://localhost:8080` and the database at port `5432`.

## 📂 Project Structure

- `/backend`: Spring Boot source code and Dockerfile.

- `docker-compose.yml`: Orchestration for the API and PostgreSQL.

---
*Developed by Valeria Jauregui Lorda - Computer Engineering Student & Electromechanical Technician.*