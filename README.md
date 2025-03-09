🏦 CentralBank – Banking Application

📌 Project Description

CentralBank is a Spring Boot-based banking application that allows users to:
•	Register and log in using JWT authentication.
•	Create bank accounts.
•	Transfer money between accounts.
•	Check account balance and transaction history.
•	Securely access user data.

⸻

🛠️ Technologies Used
•	Java 21
•	Spring Boot 3.4.3
•	Spring Security (JWT)
•	PostgreSQL
•	Hibernate (JPA)
•	Maven
•	Docker (optional)

⸻

🚀 How to Run the Project

1️⃣ Configure the Database

In the application.properties file:

spring.datasource.url=jdbc:postgresql://localhost:5432/centralbank
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

2️⃣ Start the Application

mvn clean install
mvn spring-boot:run

The application will start on http://localhost:8080/.

⸻

🔑 API Usage

1️⃣ Register a New User

curl -X POST http://localhost:8080/auth/register \
-H "Content-Type: application/json" \
-d '{"username": "newUser", "password": "password1234"}'

2️⃣ Log in and Get a JWT Token

curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username": "newUser", "password": "password1234"}'

➡ Returned JWT Token:
eyJhbGciOiJIUzI1NiJ9...

⸻

💳 Banking Operations

3️⃣ Create a Bank Account

curl -X POST http://localhost:8080/account/create \
-H "Authorization: Bearer YOUR_JWT_TOKEN"

4️⃣ Check Account Balance

curl -X GET http://localhost:8080/account/balance \
-H "Authorization: Bearer YOUR_JWT_TOKEN"

5️⃣ Transfer Money

curl -X POST http://localhost:8080/transactions/transfer \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{"recipientUsername": "Blitzo", "amount": 200}'

6️⃣ View Transaction History

curl -X GET http://localhost:8080/transactions/history \
-H "Authorization: Bearer YOUR_JWT_TOKEN"



⸻

🔒 Security Features
•	JWT Authentication – Users can only perform operations on their own accounts.
•	Spring Security – The /account/all endpoint is restricted to administrators only.
•	Password Hashing – User credentials are securely stored.

⸻

🎯 Next Steps

🔹 Deploy the application on a VPS server
🔹 Implement external payment processing (Stripe, PayPal)
🔹 Create a frontend panel (React, Angular) for user management

⸻

📌 Author

Alexander / Jintrt
📅 Project Completion Date: 2025-03-09

⸻
