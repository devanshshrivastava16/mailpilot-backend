# MailPilot Backend

This is the backend service for MailPilot, a Spring Boot application designed to simplify and automate sending cold emails for job applications.

## Features

- **User Authentication**: Secure user registration and login using JWT.
- **Sender Account Management**: Add and manage multiple Gmail sender accounts with encrypted app passwords.
- **Email Campaign Management**: Create, preview, and send personalized email campaigns to multiple recipients.
- **Dynamic Placeholders**: Use placeholders like `{{name}}`, `{{company}}`, and `{{email}}` in templates for personalization.
- **Resume Attachment**: Attach a resume (PDF or DOCX) to your email campaigns.
- **Campaign Tracking**: View campaign history and the status (Sent, Failed) of each email.

## Tech Stack

- **Java 21**
- **Spring Boot 3.3**
- **Spring Security** (for JWT authentication)
- **Spring Data JPA**
- **PostgreSQL** (compatible with Supabase)
- **Maven**

## Getting Started

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/devanshshrivastava16/mailpilot-backend.git
    cd mailpilot-backend
    ```
2.  **Set up environment variables:**
    Copy `.env.example` to `.env` and fill in your database credentials, JWT secret, and encryption keys.
3.  **Run the application:**
    ```sh
    ./mvnw spring-boot:run
    ```