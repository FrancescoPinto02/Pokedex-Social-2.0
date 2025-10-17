# ---------------------------
# STAGE 1: Build del frontend con Node
# ---------------------------
FROM node:20 AS build

WORKDIR /app

# Copia i file di progetto
COPY package*.json ./

# Installa le dipendenze
RUN npm install

# Copia il codice sorgente
COPY . .

# Crea la build di produzione
RUN npm run build

# ---------------------------
# STAGE 2: Servizio statico con Nginx
# ---------------------------
FROM nginx:1.27-alpine

# Rimuove la configurazione di default di Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copia i file buildati nello spazio servito da Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copia un file di configurazione custom per Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Espone la porta del frontend
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
