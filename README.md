# OpenButler

OpenButler is a CLI-based AI Agent system inspired by Claude Code, built with Java 17, Spring Boot, and Spring AI. It features a local memory system using SQLite and supports tool execution.

## Features

- **CLI Interface**: Interactive command-line interface with streaming responses.
- **AI Integration**: Powered by OpenAI (via Spring AI).
- **Memory System**: Local conversation history stored in SQLite.
- **Tools**: Built-in file system tools (ls, cat).
- **Extensible**: Designed with Plugin and Tool interfaces for future expansion.

## Prerequisites

- JDK 17+
- Maven 3.x
- OpenAI API Key

## Configuration

1. Set your OpenAI API Key as an environment variable:
   ```powershell
   $env:OPENAI_API_KEY = "sk-your-api-key"
   ```
   Or update `src/main/resources/application.yml`.

## Building

```bash
mvn clean package -DskipTests
```

## Running

You can run the application using the provided batch script or directly with Java:

```bash
java -jar target/openbutler-1.0.0.jar
```

## Commands

- `chat`: Start an interactive chat session.
- `new-session`: Start a fresh conversation session.
- `help`: List available commands.

## Architecture

- **Core**: Spring Boot + Spring Shell
- **AI**: Spring AI (OpenAI Chat Client)
- **Database**: SQLite (via MyBatis)
