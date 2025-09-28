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

## ⚙️ Requirements

To run the project locally, make sure you have the following installed:

- **Java JDK 17**
- **Maven 3.9.11**
- **Node.js 22.16.0**
- **Docker**

---

## 🧩 Local Setup and Build Instructions

Follow these steps to run the project locally:

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/FrancescoPinto02/Pokedex-Social-2.0.git
cd Pokedex-Social-2.0
```

### 2️⃣ Start the Database (PostgreSQL via Docker)
From the project root, run:
```bash
docker compose up -d
```
> ✅ Ensure the PostgreSQL container starts correctly before continuing.  
> The container will automatically build and populate the database using the scripts and CSV datasets in the `/database` folder.

### 3️⃣ Start the Backend (Spring Boot)
Move to the backend directory and start the Spring Boot application:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
> The backend will be available at http://localhost:8080

### 4️⃣ Start the Frontend (React + Vite)
Move to the frontend directory and start the Vite development server:
```bash
cd ../frontend
npm install
npm run dev
```
> The frontend will be available at http://localhost:5173

### 5️⃣ Access the Application

Once both the **backend** and **frontend** are running:

- 🌐 Open your browser and navigate to **http://localhost:5173**
- 🧩 The frontend will communicate with the backend running at **http://localhost:8080**

If everything is set up correctly, you should now be able to:
- Explore and filter Pokémon from the Pokédex  
- Register and Login As User
- Visit a pretty bad looking user profile page

> ⚠️ **Note:** Some backend features are still under development and might not be fully accessible through the frontend interface.
> For testing or exploring other features, it’s recommended to use **Postman** with the available API endpoints.
