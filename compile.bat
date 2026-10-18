@echo off
echo ========================================================
echo Compiling HRMS Project (Java)...
echo ========================================================
if not exist "bin" mkdir bin
javac -cp ".;lib/*" -d bin src/com/hrms/*.java
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation completed without errors!
    echo Output directory: bin\
) else (
    echo.
    echo [ERROR] Compilation failed. Please check errors above.
)
