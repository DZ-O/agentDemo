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

REM 不再强制根据代码页设置UTF-8编码，顺应Windows系统的默认代码页（通常是GBK），以避免中文输入法bug
REM 依赖JLine自身的JNA交互来处理编码
java -jar "%JAR_FILE%"
