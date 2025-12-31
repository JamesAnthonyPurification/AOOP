@echo off
if not exist "out" mkdir out

echo Compiling...
javac -d out -sourcepath src/main/java src/main/java/com/backup/Main.java

if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)

echo Running Automated Backup Tool...
echo.
java -cp out com.backup.Main
pause
