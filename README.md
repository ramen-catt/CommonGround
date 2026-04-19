# Common Ground

Common Ground is a Java/Tomcat + MySQL backend with a React/Vite frontend.

## Database setup

Run these scripts in MySQL Workbench against your local MySQL server:

1. `src/database/CommonGround_db.sql`
2. `src/database/seed_listings.sql`

The seed script resets the seeded listings/images, so use it for a fresh demo database.

Demo logins:

- `tester1@gmail.com` / `tester1`
- `tester2@gamil.com` / `tester2`
- `admin@commonground.com` / `admin`
- `adriana_admin@commonground.com` / `admin`

The Java backend expects this local database user:

```sql
CREATE USER IF NOT EXISTS 'cguser'@'localhost' IDENTIFIED BY 'cgpass123';
GRANT ALL PRIVILEGES ON CommonGround_db.* TO 'cguser'@'localhost';
FLUSH PRIVILEGES;
```

## Run locally

Start Tomcat:

```powershell
cd "C:\Users\gglor\OneDrive\Desktop\school software\workplace\apache-tomcat-10.1.36\bin"
.\startup.bat
```

Start the React dev server:

```powershell
cd "C:\Users\gglor\CommonGround"
npm.cmd install
npm.cmd run dev
```

Open the site at:

```text
http://localhost:5173
```

Tomcat serves the backend at:

```text
http://localhost:8080
```

## Rebuild and deploy

```powershell
cd "C:\Users\gglor\CommonGround"
npm.cmd run build
mvn.cmd -q -DskipTests package
Copy-Item -LiteralPath ".\target\ROOT.war" -Destination "C:\Users\gglor\OneDrive\Desktop\school software\workplace\apache-tomcat-10.1.36\webapps\ROOT.war" -Force
```

Or run:

```powershell
.\deploy-root-to-tomcat.bat
```
