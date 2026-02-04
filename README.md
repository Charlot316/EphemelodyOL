# EphemelodyOL

EphemelodyOL is a web-based rhythm game with real-time multiplayer features, chart editor, and comprehensive management system.

## 🚀 Quick Start

### Backend Deployment

The backend is a Spring Boot application using MySQL and Cloudflare R2. 
**For detailed configuration and troubleshooting, please refer to [Deployment Guide](back/rhythm_game/README_DEPLOYMENT.md).**

#### Docker Deployment (Recommended)

1.  **Prepare Environment Variables**
    Ensure a `.env` file exists in the project root containing your database and R2 credentials.

2.  **Build the Docker Image**
    ```bash
    docker build -t ephemelody-backend .
    ```

3.  **Run the Container**
    ```bash
    docker run -d \
      --name ephemelody-backend \
      -p 8090:8090 \
      --env-file .env \
      ephemelody-backend
    ```

#### Local Development

1.  **Navigate to backend directory**
    ```bash
    cd back/rhythm_game
    ```

2.  **Start Application**
    (Ensure system dependencies `ffmpeg` and `webp` are installed)
    ```bash
    export $(cat ../../.env | grep -v '^#' | xargs) && mvn spring-boot:run
    ```

## 🛠 Technologies

*   **Backend**: Spring Boot, MyBatis Plus, WebSocket
*   **Database**: MySQL 8.0
*   **Storage**: Cloudflare R2 (S3 Compatible)
*   **Media Processing**: FFmpeg, WebP

## ⚠️ Migration Note

> **Important**: The system has been migrated to use **UUID** for song IDs and **Cloudflare R2** for all static assets. Local `data/` storage is no longer supported.

