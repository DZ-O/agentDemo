# GraalVM Native Image Build Guide

This document describes how to build OpenButler as a native executable using GraalVM Native Image.

## Prerequisites

1. **GraalVM**: Install GraalVM 21 or later
   - Download from: https://www.graalvm.org/downloads/
   - Set `JAVA_HOME` to GraalVM installation directory
   - Add GraalVM `bin` directory to `PATH`

2. **Native Image**: Install the Native Image component
   ```bash
   gu install native-image
   ```

3. **Build Tools**: Ensure you have the required build tools for your platform
   - **Windows**: Visual Studio 2019 or later with C++ build tools
   - **Linux**: gcc, g++, zlib1g-dev
   - **macOS**: Xcode Command Line Tools

## Building Native Image

### Build Command

To build the native executable, run:

```bash
mvn clean package -Pnative
```

This will:
1. Compile the Java source code
2. Run tests
3. Create a native executable in `target/openbutler` (or `target/openbutler.exe` on Windows)

### Build Options

The native profile includes the following optimizations:

- `--no-fallback`: Ensures pure native image without JVM fallback
- `-O3`: Maximum optimization level
- `--gc=G1`: Use G1 garbage collector for better performance
- `-H:+RemoveUnusedSymbols`: Remove unused symbols to reduce size
- `-H:+AddAllCharsets`: Include all character sets for cross-platform encoding support

### Platform-Specific Builds

**Windows:**
```bash
mvn clean package -Pnative
```
Output: `target/openbutler.exe`

**Linux:**
```bash
mvn clean package -Pnative
```
Output: `target/openbutler`

**macOS:**
```bash
mvn clean package -Pnative
```
Output: `target/openbutler`

## Verifying the Build

### Check Executable Size

After building, verify the executable size:

**Windows:**
```powershell
Get-Item target/openbutler.exe | Select-Object Name, @{Name="Size(MB)";Expression={[math]::Round($_.Length/1MB, 2)}}
```

**Linux/macOS:**
```bash
ls -lh target/openbutler
```

**Expected size**: < 100MB (requirement 5.9)

### Test the Executable

Run the native executable:

**Windows:**
```bash
target\openbutler.exe
```

**Linux/macOS:**
```bash
./target/openbutler
```

The application should:
1. Start in < 2 seconds
2. Display the welcome banner
3. Show the command prompt
4. Respond to commands

## Troubleshooting

### Build Fails with "Class not found"

If you encounter class not found errors during native image build:
1. Check `reflect-config.json` includes all required classes
2. Run with `--verbose` flag to see detailed error messages
3. Add missing classes to reflection configuration

### Build Fails with "Resource not found"

If resources are missing:
1. Check `resource-config.json` includes required resource patterns
2. Verify resources exist in `src/main/resources`
3. Add missing resource patterns to configuration

### Executable Size > 100MB

If the executable exceeds size requirements:
1. Review dependencies in `pom.xml` for unnecessary libraries
2. Add exclusions for unused transitive dependencies
3. Consider using `--enable-url-protocols` selectively
4. Remove unused features from Spring Boot

### Runtime Errors

If the native executable crashes or behaves incorrectly:
1. Check initialization configuration (build-time vs run-time)
2. Verify all reflection configurations are correct
3. Test with `-H:+ReportExceptionStackTraces` for detailed errors
4. Compare behavior with JAR version

## Configuration Files

### Reflection Configuration
Location: `src/main/resources/META-INF/native-image/reflect-config.json`

Contains reflection metadata for:
- Command classes
- Model classes (Session, CLIConfig, etc.)
- Service classes
- Spring framework classes

### Resource Configuration
Location: `src/main/resources/META-INF/native-image/resource-config.json`

Includes:
- Application configuration files (application.yml)
- Logging configuration (logback-spring.xml)
- Spring metadata files
- JLine and Jansi resources

## Performance Benchmarks

Expected performance metrics:

| Metric | Target | Measurement |
|--------|--------|-------------|
| Startup time | < 2s | Time from launch to prompt |
| Input response | < 100ms | Time from keypress to display |
| Command parsing | < 500ms | Time to parse and route command |
| Executable size | < 100MB | File size of native binary |

## Continuous Integration

For CI/CD pipelines, use:

```bash
# Install GraalVM
sdk install java 21-graal

# Build native image
mvn clean package -Pnative -DskipTests

# Verify size
SIZE=$(stat -f%z target/openbutler 2>/dev/null || stat -c%s target/openbutler)
if [ $SIZE -gt 104857600 ]; then
  echo "Error: Executable size exceeds 100MB"
  exit 1
fi
```

## Additional Resources

- [GraalVM Native Image Documentation](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Spring Boot Native Image Support](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
- [Native Build Tools](https://graalvm.github.io/native-build-tools/latest/index.html)
