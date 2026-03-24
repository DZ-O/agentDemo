@echo off
set "JAR_FILE=target\openbutler-1.0.0.jar"

if not exist "%JAR_FILE%" (
    echo JAR file not found. Building...
    call mvn clean package -DskipTests
)

if "%OPENAI_API_KEY%"=="" (
    echo Warning: OPENAI_API_KEY environment variable is not set.
    echo Please set it or configure it in application.yml
)

REM 检测当前控制台代码页
for /f "tokens=2 delims=:" %%a in ('chcp') do set CODEPAGE=%%a
set CODEPAGE=%CODEPAGE: =%

REM 根据代码页设置编码
if "%CODEPAGE%"=="936" (
    set ENCODING=GBK
) else if "%CODEPAGE%"=="65001" (
    set ENCODING=UTF-8
) else (
    set ENCODING=UTF-8
)

echo Starting OpenButler with %ENCODING% encoding...
java -Dfile.encoding=%ENCODING% -Dsun.stdout.encoding=%ENCODING% -Dsun.stderr.encoding=%ENCODING% -jar "%JAR_FILE%"
