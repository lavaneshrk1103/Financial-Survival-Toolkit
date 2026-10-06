# Financial Survival Toolkit - Java OOP Submission

## Run the Java backend

1. Open a terminal in `backend`.
2. Run:

```bat
mvnw.cmd spring-boot:run
```

3. Keep that terminal running.
4. The backend runs at `http://localhost:8080`.

## Run the frontend

Open `index.html` with VS Code Live Server, or open it in a browser. The existing UI is unchanged.

The frontend now calls the Spring Boot backend instead of browser localStorage.

## Test the backend

From the `backend` folder:

```bat
mvnw.cmd test
mvnw.cmd compile
```

## Important

- Work is on the `java-backend-oop` branch.
- The original `main` branch should remain unchanged.
- Backend uses in-memory Collections/Maps, so restarting Spring Boot clears backend data.
