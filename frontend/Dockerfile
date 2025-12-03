# ------------------------------
# STAGE 1: Build stage
# ------------------------------
FROM node:18-bullseye-slim AS build
WORKDIR /app

# copia solo i file necessari e installa le dipendenze
COPY package*.json ./
RUN npm install

# copia il resto e builda
COPY . .
ARG VITE_API_URL=http://backend:8080
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# ------------------------------
# STAGE 2: Serve stage
# ------------------------------
FROM node:18-bullseye-slim AS serve
WORKDIR /app

# installa serve globalmente (solo runtime)
RUN npm install -g serve

# copia solo i file buildati (niente node_modules!)
COPY --from=build /app/dist ./dist

EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
