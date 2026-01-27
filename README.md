# SUTO-java (Smart Urban Transport Optimizer)

This is a Spring Boot based backend application for the Smart Urban Transport Optimizer.

## Prerequisites

- **Java Development Kit (JDK) 17** or higher
- **Maven** (optional, if you use the Maven Wrapper usually included, otherwise install Maven)

## How to Run Locally

Since this is a backend application, running "locally" means the server runs on your own computer (localhost).

### 1. Clone the Repository
First, download the code to your machine using Git:

```bash
git clone https://github.com/saidulislam151688-cmyk/SUTO-java.git
cd SUTO-java
```

### 2. Build and Run

#### Using Maven directly:
If you have Maven installed, you can simply run:

```bash
mvn spring-boot:run
```

#### Using Java JAR:
Alternatively, you can build the executable JAR file and run it:

```bash
mvn clean package
java -jar target/suto-java-0.0.1-SNAPSHOT.jar
```

### 3. Accessing the Application
Once the application starts, it will be accessible at:
http://localhost:8080

(Note: The port may vary if configured differently in `src/main/resources/application.properties`).

## Data Storage
This application uses local JSON files for data storage. Ensure you have write permissions in the running directory so it can create/update the `data/` folder.
