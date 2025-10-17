# Immagine ufficiale PostgreSQL
FROM postgres:15

# Copia gli script SQL e i dataset nel punto di inizializzazione
COPY db_scripts/ /docker-entrypoint-initdb.d/
COPY dataset/ /docker-entrypoint-initdb.d/dataset/
