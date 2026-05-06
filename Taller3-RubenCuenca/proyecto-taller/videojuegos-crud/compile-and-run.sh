#!/bin/bash
echo "============================================"
echo " Compilando Sistema de Gestión de Videojuegos"
echo "============================================"

mkdir -p out

javac -d out src/videojuegos/Videojuego.java \
             src/videojuegos/EventClient.java \
             src/videojuegos/VideojuegosCRUD.java \
             src/videojuegos/Main.java

if [ $? -ne 0 ]; then
  echo "ERROR: Falló la compilación."
  exit 1
fi

echo "Compilación exitosa. Iniciando aplicación..."
echo ""
java -cp out videojuegos.Main
