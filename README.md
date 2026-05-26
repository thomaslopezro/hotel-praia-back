# Hotel Praia — Backend 🏨

API REST robusta para la gestión integral de un hotel. Incluye autenticación JWT, gestión de habitaciones, reservas, usuarios, pagos, reseñas y panel administrativo.

🔗 **Repositorio del frontend:** [hotel-praia-front](https://github.com/thomaslopezro/hotel-praia-front)
🔗 **Demo en vivo (frontend):** [hotel-praia-front.vercel.app](https://hotel-praia-front.vercel.app/)

---

## 🛠️ Stack Tecnológico

### Framework principal
- **Spring Boot** + **Java**
- **Maven** como build tool

### Módulos de Spring
- **Spring Security** — autenticación y autorización
- **Spring Data JPA** — capa de persistencia
- **Spring Validation** — validación de datos
- **Spring Mail** — envío de correos
- **Spring Actuator** — monitoreo y métricas
- **Thymeleaf** — plantillas para correos

### Seguridad
- **Autenticación JWT** (JSON Web Tokens)
- Roles y permisos por usuario

### Base de datos
- **H2 Database**
  - Archivo en desarrollo
  - En memoria en producción

### Arquitectura
- **API REST** con 13 controllers
- Arquitectura por capas: Controllers → Services → Repositories → Entities

---

## 📋 Funcionalidades

- 🔐 Autenticación y autorización con JWT
- 🏠 CRUD de habitaciones
- 📅 Gestión de reservas con validación de disponibilidad
- 👥 Gestión de usuarios (clientes y administradores)
- 💳 Sistema de pagos
- ⭐ Gestión de reseñas y calificaciones
- 📧 Notificaciones por correo (confirmación de reservas, etc.)
- 📊 Endpoints de monitoreo con Actuator

---

## 🚀 Cómo correr el proyecto

### Prerrequisitos
- Java 17+
- Maven 3.8+

### Instalación

```bash
git clone https://github.com/thomaslopezro/hotel-praia-back.git
cd hotel-praia-back
./mvnw spring-boot:run
```

El backend estará disponible en `http://localhost:8080`

### Consola de H2 (en desarrollo)
Disponible en `http://localhost:8080/h2-console`

---

## 🏗️ Arquitectura

​```mermaid
graph TD
    A[Frontend - Angular 16<br/>Vercel] -->|HTTP / JWT| B[Controllers<br/>13 REST endpoints]
    B --> C[Services<br/>Lógica de negocio]
    C --> D[Repositories<br/>Spring Data JPA]
    D --> E[(Database<br/>H2)]

    style A fill:#DD0031,stroke:#fff,color:#fff
    style B fill:#6DB33F,stroke:#fff,color:#fff
    style C fill:#6DB33F,stroke:#fff,color:#fff
    style D fill:#6DB33F,stroke:#fff,color:#fff
    style E fill:#005C84,stroke:#fff,color:#fff
​```

## 👥 Equipo

Proyecto desarrollado en equipo de 4 personas para la materia **Desarrollo Web** (sexto semestre) en la **Pontificia Universidad Javeriana**, bajo metodología **Scrum**.

---

## 📄 Contexto académico

Trabajo final de la materia de Desarrollo Web — Ingeniería de Sistemas, Pontificia Universidad Javeriana.
