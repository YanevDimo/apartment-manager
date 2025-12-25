# How to Start the Application

## Prerequisites

1. **Java 17** - Must be installed and in PATH
2. **Maven** - Must be installed and in PATH  
3. **MySQL 8** - Must be running on localhost:3306
4. **Database** - `apartments_manager` database will be created automatically

## Starting the Application

### Option 1: Using Maven (Recommended)

```bash
# Navigate to project directory
cd "C:\Users\dimo\AppData\Local\Temp\apartments-manager"

# Start the application
mvn spring-boot:run
```

### Option 2: Using IDE (IntelliJ IDEA / Eclipse)

1. Open the project in your IDE
2. Find `ApartmentsManagerApplication.java`
3. Right-click → Run 'ApartmentsManagerApplication'
4. Or use the green play button

### Option 3: Build and Run JAR

```bash
# Build the application
mvn clean package

# Run the JAR
java -jar target/apartments-manager-0.0.1-SNAPSHOT.jar
```

## Verify Application Started

Once started, you should see in the console:
```
Started ApartmentsManagerApplication in X.XXX seconds
```

Then visit:
- `http://localhost:8080/` - Home page
- `http://localhost:8080/test` - Test page
- `http://localhost:8080/add-apartment` - Add apartment form

## Common Startup Issues

### 1. Port 8080 Already in Use
**Error**: `Port 8080 is already in use`

**Solution**: 
- Change port in `application.properties`: `server.port=8081`
- Or stop the process using port 8080

### 2. MySQL Connection Failed
**Error**: `Communications link failure` or `Access denied`

**Solution**:
- Ensure MySQL is running
- Check username/password in `application.properties`
- Verify MySQL is on port 3306

### 3. Database Doesn't Exist
**Solution**: The application will create it automatically if `createDatabaseIfNotExist=true` is in the URL

## Configuration

The application is configured in `src/main/resources/application.properties`:
- **Port**: 8080 (default)
- **Database**: `apartments_manager` on localhost:3306
- **Encoding**: UTF-8 (for Bulgarian Cyrillic)

## Environment Variables

You can override database credentials:
```bash
# Windows PowerShell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="yourpassword"
mvn spring-boot:run

# Windows CMD
set DB_USERNAME=root
set DB_PASSWORD=yourpassword
mvn spring-boot:run
```

---

**Note**: The application uses Spring Boot DevTools, so it will auto-reload on code changes.



