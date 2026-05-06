@echo off
echo ============================================
echo  Compilando Sistema de Gestion de Videojuegos
echo ============================================

if not exist "out" mkdir out

javac -d out src\videojuegos\Videojuego.java src\videojuegos\EventClient.java src\videojuegos\VideojuegosCRUD.java src\videojuegos\Main.java

if %errorlevel% neq 0 (
    echo ERROR: Fallo la compilacion.
    pause
    exit /b 1
)

echo Compilacion exitosa. Iniciando aplicacion...
echo.
java -cp out videojuegos.Main
pause
