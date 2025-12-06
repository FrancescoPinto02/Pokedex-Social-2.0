# 🧠 Pokedex-Social-2.0

**Pokedex-Social-2.0** is a personal **for-fun project** designed to bring together Pokémon fans and data lovers.  
It allows users to explore and filter Pokémon from the Pokédex, register and create their own **custom Pokémon Teams**, and even use a **Genetic Algorithm** to get recommendations for **optimized Pokémon Teams**.

---

## 🚀 Features

- 🔍 **Explore and filter** all Pokémon from the Pokédex (gen. VII) 
- 👤 **User registration and authentication**  
- 🧩 **Create and manage Pokémon Teams**  
- 🧬 **Get optimized team suggestions** via a **Genetic Algorithm**  
- 🛠️ Built for fun and learning — expect ongoing development!

> ⚠️ **Note:** Not all backend features are yet connected to the frontend.  
> For full testing of API features, it’s recommended to use **Postman**.

---

## 🧰 Technologies Used

- **Backend:** Java **Spring Boot**  
- **Frontend:** **React (Vite)**  
- **Database:** **PostgreSQL**  
- **Containerization:** **Docker**
---

## 🐳 Installation with Docker Compose (Recommended)
### ⚙️ Requirements
- **Docker**

### 1️⃣ Download docker-compose.yml
Follow the link and download the file

👉[Download docker-compose.yml](https://github.com/FrancescoPinto02/Pokedex-Social-2.0/blob/main/docker-compose.yml)

### 2️⃣ Configure the `JWT_SECRET` Environment Variable
Before running the application, you must set the `JWT_SECRET` environment variable inside the `docker-compose.yml` file.

👉[Generate a secret](https://jwtsecrets.com/)

> ⚠️ **Important:** Make sure to use a 64 characters secret!

### 3️⃣ Start the Application Using Docker Compose
Once the `JWT_SECRET` variable is configured and Docker is Up, you can start the entire application using Docker Compose.
Run the following command from the same directory of the docker-compose.yml file:

```bash
docker compose up -d --build
```

### 4️⃣ Access the Application
Once both the **backend** and **frontend** are running:

- 🌐 Open your browser and navigate to **http://localhost:5173**
- 🧩 The frontend will communicate with the backend running at **http://localhost:8080**

If everything is set up correctly, you should now be able to:
- Explore and filter Pokémon from the Pokédex  
- Register and Login As User
- Visit a pretty bad looking user profile page

> ⚠️ **Note:** Some backend features are still under development and might not be fully accessible through the frontend interface.
> For testing or exploring other features, it’s recommended to use **Postman** with the available API endpoints.


