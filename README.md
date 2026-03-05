# Spring Kafka Hackathon – Event-Driven Microservices

Event-driven microservices built with **Spring Boot**, **Apache Kafka**, **Spring Cloud Config**, and **Swagger/OpenAPI**. All inter-service communication is via Kafka events; no direct REST calls between services.

## Architecture

- **Order Service** → publishes `OrderCreated` on `order-events` → consumed by **Inventory Service** and **Fraud Service**
- **Inventory Service** → publishes `InventoryApproved` / `InventoryRejected` on `inventory-events` → consumed by **Order Service**
- **Fraud Service** → publishes `FraudApproved` / `FraudRejected` on `fraud-events` → consumed by **Order Service**

**Topics:** `order-events`, `inventory-events`, `fraud-events` (message key = `orderId`)

### How they connect via Kafka

- **Bootstrap servers:** Each app (Order, Inventory, Fraud) gets the Kafka broker address from **`KAFKA_BOOTSTRAP_SERVERS`**.
  - **Docker:** In `docker-compose.yml`, all three services have `KAFKA_BOOTSTRAP_SERVERS: kafka:29092`, so they all use the same Kafka container (hostname `kafka`, port `29092`).
  - **Local (no Docker):** Each service uses `spring.kafka.bootstrap-servers` from its config (or env), default `localhost:9092`.
- **Config:** In `application.yml` (and config-repo), `spring.kafka.bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`. So setting the env var overrides the default.
- **Topics:** Order Service produces to `order-events` and consumes from `inventory-events` and `fraud-events`. Inventory consumes `order-events` and produces `inventory-events`. Fraud consumes `order-events` and produces `fraud-events`. All use the same broker(s) from `KAFKA_BOOTSTRAP_SERVERS`.

## Modules

| Module              | Description                                                                                 |
| ------------------- | ------------------------------------------------------------------------------------------- |
| `event-contracts`   | Shared Kafka event DTOs (OrderCreated, InventoryEvent, FraudEvent)                          |
     | `config-server`     | Spring Cloud Config Server – fetches config from a **separate config Git repo** (see below) |
| `order-service`     | REST API (POST order, GET all orders, GET order by ID), publishes OrderCreated, consumes inventory & fraud events |
| `inventory-service` | Consumes OrderCreated, validates stock, publishes InventoryApproved/Rejected                |
| `fraud-service`     | Consumes OrderCreated; amount > 50000 → FraudRejected, else FraudApproved                   |

## Prerequisites

- **Java 17**
- **Maven 3.8+**
- **Docker** (for Kafka) or a running Kafka broker

## How to run the application

**1. Start Kafka**

```bash
docker-compose up -d
```

**2. Build**

```bash
mvn clean install -DskipTests
```

**3. Start Config Server** (port 8888). Set your config repo URL via env (no hardcoding):

```bash
export SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://github.com/your-org/your-config-repo.git
# If repo is private:
export GIT_USERNAME=your-git-username
export GIT_PASSWORD=your-personal-access-token
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
```

**4. Start the three services** (in separate terminals). Point them at Config Server:

```bash
export CONFIG_SERVER_URL=http://localhost:8888
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
```

```bash
export CONFIG_SERVER_URL=http://localhost:8888
java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar
```

```bash
export CONFIG_SERVER_URL=http://localhost:8888
java -jar fraud-service/target/fraud-service-0.0.1-SNAPSHOT.jar
```

**5. Test:** Create an order, fetch one by ID, or list all orders.

```bash
# Create order
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" \
  -d '{"productId":"PRODUCT-001","quantity":2,"amount":15000.00,"customerId":"CUST-001"}'
# Get one order (use the returned orderId)
curl http://localhost:8080/orders/<orderId>
# Get all orders
curl http://localhost:8080/orders
```

**Swagger:** OpenAPI UI is at `swagger-ui.html` or `swagger-ui/index.html`. Order: http://localhost:8080/swagger-ui.html (or /swagger-ui/index.html) | Inventory: http://localhost:8081/swagger-ui.html | Fraud: http://localhost:8082/swagger-ui.html

_Optional:_ To run **without Config Server** (local config only), skip step 3 and do **not** set `CONFIG_SERVER_URL` in step 4; each service uses its own `application.yml`.

---

## Run all services in Docker (lightweight)

All services (Zookeeper, Kafka, Config Server, Order, Inventory, Fraud) can run in Docker. Config repo is assumed **public** (no username/password).

**1. Set your public config repo URL** (optional; default is `https://github.com/gopinadh365/spring-kafka-config.git`):

```bash
export SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://github.com/your-org/spring-kafka-config.git
```

**2. Build and start everything:**

```bash
docker-compose up -d --build
```

First run will build the images (a few minutes); later runs are quick. Ports: **8888** (Config Server), **8080** (Order), **8081** (Inventory), **8082** (Fraud), **9092** (Kafka).

**3. Test:**

- Swagger: http://localhost:8080/swagger-ui.html or http://localhost:8080/swagger-ui/index.html (Order), and same pattern for 8081 (Inventory), 8082 (Fraud)
- Create an order via Order Swagger, then GET the order to see status move from PENDING to CONFIRMED/REJECTED

**4. Logs:**

```bash
docker-compose logs -f order-service
docker-compose logs -f fraud-service
# etc.
```

**5. Stop:**

```bash
docker-compose down
```

Each app image uses **eclipse-temurin:17-jre-alpine** for a small runtime; no Git credentials are needed when the config repo is public.

---

## Running each service individually

Run every command from the **project root** (`~/projects/java/spring-kafka-hackathon`), **or** use the “from target” option when you are already in that service’s `target/` folder.

**Config Server** (port 8888)

- From project root: `java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar`
- From config-server/target: `java -jar config-server-0.0.1-SNAPSHOT.jar`

**Order Service** (port 8080)

```bash
export CONFIG_SERVER_URL=http://localhost:8888
# From project root:
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
# Or from order-service/target/:
java -jar order-service-0.0.1-SNAPSHOT.jar
```

**Inventory Service** (port 8081)

```bash
export CONFIG_SERVER_URL=http://localhost:8888
# From project root:
java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar
# Or from inventory-service/target/:
java -jar inventory-service-0.0.1-SNAPSHOT.jar
```

**Fraud Service** (port 8082)

```bash
export CONFIG_SERVER_URL=http://localhost:8888
# From project root:
java -jar fraud-service/target/fraud-service-0.0.1-SNAPSHOT.jar
# Or from fraud-service/target/:
java -jar fraud-service-0.0.1-SNAPSHOT.jar
```

If you are inside a service’s `target/` folder, use only the **JAR filename** (e.g. `fraud-service-0.0.1-SNAPSHOT.jar`), not `fraud-service/target/...`.

---

## Two-repo setup (config in one repo, application in another)

This project is meant to use **two Git repositories**:

| Repository                       | Contents                                                                                                                                |
| -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **Config repo**                  | Only YAML config files (`application.yml`, `order-service.yml`, `inventory-service.yml`, `fraud-service.yml`).                          |
| **Application repo** (this repo) | order-service, inventory-service, fraud-service, config-server, event-contracts. **No config YAMLs** – config lives in the config repo. |

The folder **`config-repo-template/`** in this repo contains the files you push to your **config repository**. See [Config repo setup](#config-repo-setup) below.

---

## Quick Start

### 1. Create the config repository

Create a new Git repo (e.g. `spring-kafka-config`) and push the contents of **`config-repo-template/`**, or use the existing config repo: `https://github.com/gopinadh365/spring-kafka-config.git`

```bash
cd config-repo-template
git init
git add .
git commit -m "Initial config"
git remote add origin https://github.com/gopinadh365/spring-kafka-config.git
git push -u origin main
```

See **config-repo-template/README.md** for details.

### 2. Start Kafka (Docker)

```bash
docker-compose up -d
```

### 3. Build the application repo

```bash
mvn clean install -DskipTests
```

### 4. Run Config Server

Config Server needs the config repo URL via **environment variable** (no hardcoded URI in the app):

```bash
export SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://github.com/your-org/your-config-repo.git
# For private repo:
export GIT_USERNAME=your-username
export GIT_PASSWORD=your-token-or-password
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
```

Config Server runs on **8888** and clones the repo from the URI you set.

### 5. Run Order, Inventory, and Fraud services

Point them at Config Server (they do **not** use a Git URL):

```bash
export CONFIG_SERVER_URL=http://localhost:8888
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
# In other terminals:
java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar
java -jar fraud-service/target/fraud-service-0.0.1-SNAPSHOT.jar
```

Without `CONFIG_SERVER_URL`, each service falls back to its local `application.yml` (e.g. for local dev).

### 6. Create an order

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"PRODUCT-001","quantity":2,"amount":15000.00,"customerId":"CUST-001"}'
```

Then GET the order (or all orders) to see status (PENDING → CONFIRMED or REJECTED after inventory and fraud events):

```bash
curl http://localhost:8080/orders/<orderId>
curl http://localhost:8080/orders   # list all orders
```

- **Fraud:** amount &gt; 50000 → rejected; otherwise approved. Override with `FRAUD_THRESHOLD_AMOUNT`.
- **Inventory:** PRODUCT-001/002/003 have default stock; insufficient quantity → rejected.

## Swagger UI

- Order Service: http://localhost:8080/swagger-ui.html
- Inventory Service: http://localhost:8081/swagger-ui.html
- Fraud Service: http://localhost:8082/swagger-ui.html

---

## How to test via Swagger (problem statement)

Ensure **Kafka**, **Order**, **Inventory**, and **Fraud** are running. Config Server is optional (services use local config if it’s down).

**Flow from problem statement:** User places order → Order Service saves **PENDING** and publishes **OrderCreated** → **Inventory** validates stock → **Fraud** checks amount (reject if &gt; 50000) → Order Service gets both results and sets order to **CONFIRMED** or **REJECTED**. All via Kafka; no REST between services.

### Step 1: Create an order (Order Service Swagger)

1. Open **http://localhost:8080/swagger-ui.html**
2. Expand **Orders** → **POST /orders** → **Try it out**
3. Paste one of the request bodies below into the box
4. Click **Execute**
5. From the response (201), copy the **`id`** (orderId) – you’ll use it in Step 2

### Step 2: See final order status (Order Service Swagger)

1. Still on Order Service Swagger: **GET /orders/{orderId}** or **GET /orders** (all orders).
2. For **GET /orders/{orderId}**: Click **Try it out**, paste the **orderId** from Step 1, click **Execute**.
3. For **GET /orders**: Click **Try it out** → **Execute** to see all orders and their status.
4. First time you may see **`"status": "PENDING"`**. Wait a few seconds and call again – status should become **CONFIRMED** or **REJECTED** after Inventory and Fraud process the event via Kafka.

### Step 3 (optional): Check inventory and fraud config

- **Inventory:** http://localhost:8081/swagger-ui.html → **GET /stock/{productId}** (e.g. `PRODUCT-001`) to see current stock
- **Fraud:** http://localhost:8082/swagger-ui.html → **GET /fraud/threshold** to see the amount limit (default **50000**)

---

### Sample request bodies for POST /orders (copy into Swagger)

**Test 1 – Should end as CONFIRMED** (amount ≤ 50000, product in stock)

```json
{
  "productId": "PRODUCT-001",
  "quantity": 2,
  "amount": 15000.00,
  "customerId": "CUST-001"
}
```

**Test 2 – Should end as REJECTED (Fraud)** – amount &gt; 50000

```json
{
  "productId": "PRODUCT-001",
  "quantity": 1,
  "amount": 60000.00,
  "customerId": "CUST-002"
}
```          

**Test 3 – Should end as REJECTED (Inventory)** – quantity &gt; stock (PRODUCT-002 has 50)

```json
{
  "productId": "PRODUCT-002",
  "quantity": 100,
  "amount": 10000.00,
  "customerId": "CUST-003"
}
```

**Test 4 – Another CONFIRMED**

```json
{
  "productId": "PRODUCT-003",
  "quantity": 5,
  "amount": 25000.50,
  "customerId": "CUST-004"
}
```

**Rules (problem statement):**

| Check      | Rule                          | Result if failed   |
| ---------- | ----------------------------- | ------------------ |
| **Fraud**  | amount &gt; 50000 → reject    | Order **REJECTED** |
| **Inventory** | PRODUCT-001/002/003 have limited stock; insufficient → reject | Order **REJECTED** |
| **Order**  | CONFIRMED only if both approve | Else **REJECTED**  |

### How to check that fraud is working

1. **See the fraud threshold**  
   - Open **Fraud Service** Swagger: http://localhost:8082/swagger-ui.html  
   - **GET /fraud/threshold** → Execute.  
   - You should see something like `{ "amount": 50000 }`. Orders with **amount &gt; this value** are rejected by the Fraud Service.

2. **Test fraud rejection (amount &gt; 50000)**  
   - **Order Service** Swagger: http://localhost:8080/swagger-ui.html  
   - **POST /orders** with body:
     ```json
     { "productId": "PRODUCT-001", "quantity": 1, "amount": 60000.00, "customerId": "CUST-002" }
     ```
   - Execute → copy the **`id`** from the 201 response.  
   - **GET /orders/{orderId}** with that `id` → Execute. First time you may see `"status": "PENDING"`.  
   - Wait a few seconds and call **GET /orders/{orderId}** again. You should see **`"status": "REJECTED"`** (Fraud Service rejected because 60000 &gt; 50000).

3. **Test fraud approval (amount ≤ 50000)**  
   - **POST /orders** with body:
     ```json
     { "productId": "PRODUCT-001", "quantity": 1, "amount": 40000.00, "customerId": "CUST-003" }
     ```
   - Get the order **id**, then **GET /orders/{orderId}** after a few seconds. You should see **`"status": "CONFIRMED"`** (assuming inventory also approves).

So: **Fraud is working** if orders with amount &gt; threshold end as **REJECTED** and orders with amount ≤ threshold (and enough stock) end as **CONFIRMED**.

---

## Expected API responses

### Order Service (port 8080)

| Method | Endpoint | Status | Response |
|--------|----------|--------|----------|
| **POST** | `/orders` | **201 Created** | JSON object: `id`, `productId`, `quantity`, `amount`, `customerId`, `status` (initially `"PENDING"`), `createdAt`, `updatedAt` |
| **GET** | `/orders` | **200 OK** | JSON object: `message`, `orders` (array). When no orders: `message` is `"Data not available"`, `orders` is `[]`. When orders exist: `message` is `"Data retrieved successfully"`, `orders` has the list. |
| **GET** | `/orders/{orderId}` | **200 OK** | Single JSON object; `status` is `PENDING`, `CONFIRMED`, or `REJECTED` (updates after Inventory + Fraud events) |
| **GET** | `/orders/{orderId}` | **404 Not Found** | JSON object: `{ "message": "Data not available" }` when orderId does not exist |

**Example POST /orders response (201):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "productId": "PRODUCT-001",
  "quantity": 2,
  "amount": 15000.00,
  "customerId": "CUST-001",
  "status": "PENDING",
  "createdAt": "2026-03-05T06:00:00.000Z",
  "updatedAt": "2026-03-05T06:00:00.000Z"
}
```

**Example GET /orders/{orderId} response (200) after processing:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "productId": "PRODUCT-001",
  "quantity": 2,
  "amount": 15000.00,
  "customerId": "CUST-001",
  "status": "CONFIRMED",
  "createdAt": "2026-03-05T06:00:00.000Z",
  "updatedAt": "2026-03-05T06:00:05.123Z"
}
```

**Example GET /orders response (200) – when orders exist:**
```json
{
  "message": "Data retrieved successfully",
  "orders": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "productId": "PRODUCT-001",
      "quantity": 2,
      "amount": 15000.00,
      "customerId": "CUST-001",
      "status": "CONFIRMED",
      "createdAt": "2026-03-05T06:00:00.000Z",
      "updatedAt": "2026-03-05T06:00:05.123Z"
    }
  ]
}
```

**Example GET /orders response (200) – when no orders:**
```json
{
  "message": "Data not available",
  "orders": []
}
```

**Example GET /orders/{orderId} response (404) – order not found:**
```json
{
  "message": "Data not available"
}
```

**Invalid POST body (e.g. missing required field):** **400 Bad Request** (validation error).

---

### Inventory Service (port 8081)

| Method | Endpoint | Status | Response |
|--------|----------|--------|----------|
| **GET** | `/stock/{productId}` | **200 OK** | Plain number (integer): current stock for that product, e.g. `100` |

**Example GET /stock/PRODUCT-001 response (200):**  
Response body: `100` (or whatever the current stock is; default PRODUCT-001 = 100, PRODUCT-002 = 50, PRODUCT-003 = 200).

---

### Fraud Service (port 8082)

| Method | Endpoint | Status | Response |
|--------|----------|--------|----------|
| **GET** | `/fraud/threshold` | **200 OK** | JSON object: `{ "amount": 50000 }` (or configured threshold) |

**Example GET /fraud/threshold response (200):**
```json
{
  "amount": 50000
}
```

---

## Config repo setup

- **Config repository:** Create a **new** Git repo (e.g. `spring-kafka-config`). Copy the contents of **`config-repo-template/`** from this repo into that new repo and push. That repo contains **only** the YAML files (no Java code).
- **This repository (application):** Push as-is. It contains **no** config YAMLs at runtime – only the **template** folder so you know what to put in the config repo.

Config Server reads from the config repo using **`SPRING_CLOUD_CONFIG_SERVER_GIT_URI`** (set when starting; no hardcoded URI). Order, Inventory, and Fraud services only need **`CONFIG_SERVER_URL`** (Config Server’s HTTP URL), not a Git URL.

## Environment variables

| Variable                                                             | Description                                                                                                                                         |
| -------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `SPRING_CLOUD_CONFIG_SERVER_GIT_URI`                                  | **Required** for Config Server. Git URL of your config repo (e.g. `https://github.com/your-org/spring-kafka-config.git`). Set when starting the JAR.   |
| `GIT_USERNAME` / `GIT_PASSWORD`                                      | For **private** config repo. Set when starting Config Server. Use a Personal Access Token as password for GitHub.                                   |
| `CONFIG_SERVER_URL`                                                  | Config Server HTTP URL (e.g. `http://localhost:8888`). Set on order/inventory/fraud services. Optional – they use local `application.yml` if unset.  |
| `KAFKA_BOOTSTRAP_SERVERS`                                            | Kafka brokers (default `localhost:9092`).                                                                                                           |
| `SERVER_PORT`                                                        | Override server port per service (8080, 8081, 8082).                                                                                                |
| `FRAUD_THRESHOLD_AMOUNT`                                             | Fraud rejection threshold (default 50000).                                                                                                          |
| `ORDER_EVENTS_TOPIC`, `INVENTORY_EVENTS_TOPIC`, `FRAUD_EVENTS_TOPIC` | Topic names (defaults: order-events, inventory-events, fraud-events).                                                                               |

### Local dev without a config repo

To run Config Server from local files (e.g. `config-repo-template`), use the **native** profile:

```bash
export SPRING_PROFILES_ACTIVE=native
export CONFIG_REPO_PATH=./config-repo-template
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
```

---

## Where to check errors

| Where | What to look at |
|-------|------------------|
| **Terminal where each service runs** | All logs (INFO, WARN, ERROR) and stack traces. When you run `java -jar order-service/...`, errors for that service appear in **that** terminal. Same for Config Server, Inventory, Fraud. |
| **Config Server terminal** | Git clone/fetch failures (`InvalidRemoteException`, `NoSuchRepositoryException`), missing `spring.cloud.config.server.git.uri`, auth errors for private repos. |
| **Order Service terminal** | Order creation, Kafka publish errors, and errors when consuming inventory/fraud events or updating order status. |
| **Inventory Service terminal** | Kafka consumer errors (e.g. `order-events`), deserialization issues, and any errors when publishing to `inventory-events`. |
| **Fraud Service terminal** | Kafka consumer errors for `order-events`, and errors when publishing to `fraud-events`. |
| **Kafka / Docker** | If Kafka is down or unreachable, each service will log connection errors (e.g. `Connection to node -1 could not be established`, `LEADER_NOT_AVAILABLE`) in **its own** terminal. Run `docker ps` and `docker logs <kafka-container>` to check Kafka. |
| **Swagger / API response** | HTTP status (400, 404, 500) and response body. Validation errors (400) and “order not found” (404) show in the response; 500 and other server errors are also logged in the **Order Service** (or the service that handled the request) terminal. |

**Summary:** For each JAR you run, watch **that JAR’s terminal** for errors and stack traces. There is no single log file by default; everything goes to stdout of the process you started.

---

## Summary of changes (what we did)

This section summarizes the main features and fixes in this project so the README gives complete details.

| Area | What we did |
|------|-------------|
| **Order API** | **POST /orders** – create order (PENDING), publish `OrderCreated` to Kafka. **GET /orders** – return all orders with current status. **GET /orders/{orderId}** – return single order; status updates to CONFIRMED/REJECTED after Inventory and Fraud events. |
| **Event flow** | Order Service publishes to `order-events`; Inventory and Fraud consume it. Inventory publishes `InventoryApproved`/`InventoryRejected` on `inventory-events`; Fraud publishes `FraudApproved`/`FraudRejected` on `fraud-events`. Order Service consumes both and updates order status. No REST between services. |
| **Config** | Two-repo setup: Config Server reads from a Git repo via **`SPRING_CLOUD_CONFIG_SERVER_GIT_URI`** (no hardcoded URI). Order/Inventory/Fraud use **`CONFIG_SERVER_URL`** (optional). Public repo needs no credentials. |
| **Docker** | `docker-compose.yml` runs Zookeeper, Kafka, Config Server, Order, Inventory, Fraud. Each app has a multi-stage Dockerfile (Maven build → eclipse-temurin:17-jre-alpine). Kafka single-broker with replication factor 1. |
| **Swagger** | springdoc-openapi (2.3.0) on all three app services. Swagger UI at **/swagger-ui.html** or **/swagger-ui/index.html**. Explicit `springdoc.swagger-ui.path` and `api-docs.path` in each service’s `application.yml` so the UI opens reliably. |
| **Build** | Parent POM with `spring-boot-maven-plugin` on config-server and all three services so `java -jar` works. Dockerfiles copy full reactor POMs so Maven resolves multi-module tree. |

---

## License

MIT (or as per your project).
