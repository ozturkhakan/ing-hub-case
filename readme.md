# Brokerage System Documentation

## Overview
A Spring Boot-based brokerage system that allows customers to manage stock orders and assets. The system supports basic order operations (create, list, cancel), asset management (deposit, withdraw), and includes role-based access control.

## Core Features

### 1. Authentication and Authorization
- Two user roles: ADMIN and USER
- Users can only access their own data (linked via customerId)
- Admins have full system access
- Basic authentication implemented

### 2. Order Management
- Create buy/sell orders
- List orders with optional filters
- Cancel pending orders
- Admin-only order matching

### 3. Asset Management
- Deposit/withdraw TRY (Turkish Lira)
- Track asset balances
- Automatic balance reservation for orders
- Usable balance management

## Test Cases and API Endpoints

### Authentication Setup
```bash
# Admin credentials
Username: admin
Password: admin123

# User credentials
Username: user
Password: user123 (linked to CUST1)
```

### 1. Asset Management Tests

#### a) Deposit Money
```bash
# Deposit 10000 TRY
curl -X POST "http://localhost:8080/api/assets/deposit" \
-H "Content-Type: application/json" \
-H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
-d '{
    "customerId": "CUST1",
    "amount": 10000.0
}'
```
Expected: Creates or updates TRY asset with 10000 balance

#### b) Check Balance
```bash
curl -X GET "http://localhost:8080/api/assets?customerId=CUST1" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz"
```
Expected: Returns list of customer's assets with balances

#### c) Withdraw Money
```bash
curl -X POST "http://localhost:8080/api/assets/withdraw" \
-H "Content-Type: application/json" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz" \
-d '{
    "customerId": "CUST1",
    "amount": 1000.0,
    "iban": "TR123456789"
}'
```
Expected: Reduces TRY balance if sufficient funds available

### 2. Order Management Tests

#### a) Create Buy Order
```bash
curl -X POST "http://localhost:8080/api/orders" \
-H "Content-Type: application/json" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz" \
-d '{
    "customerId": "CUST1",
    "assetName": "AAPL",
    "side": "BUY",
    "size": 10.0,
    "price": 150.0
}'
```
Expected: 
- Creates order in PENDING status
- Reserves required TRY amount (size * price)
- Updates usable balance

#### b) List Orders
```bash
# List all orders (admin only)
curl -X GET "http://localhost:8080/api/orders" \
-H "Authorization: Basic YWRtaW46YWRtaW4xMjM="

# List customer specific orders
curl -X GET "http://localhost:8080/api/orders?customerId=CUST1" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz"

# List with date range
curl -X GET "http://localhost:8080/api/orders?customerId=CUST1&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz"
```
Expected: Returns filtered list of orders based on criteria

#### c) Cancel Order
```bash
curl -X DELETE "http://localhost:8080/api/orders/{orderId}" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz"
```
Expected:
- Changes order status to CANCELED
- Returns reserved amounts to usable balance
- Only works for PENDING orders

### 3. Admin Operations Tests

#### Match Order (Admin Only)
```bash
curl -X POST "http://localhost:8080/api/admin/orders/{orderId}/match" \
-H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```
Expected:
- Changes order status to MATCHED
- Updates asset balances accordingly
- For BUY: Reduces TRY, Increases asset
- For SELL: Increases TRY, Reduces asset

## Business Rules

1. **Order Creation**
   - Must have sufficient balance
   - BUY orders reserve TRY
   - SELL orders reserve asset amount

2. **Order Cancellation**
   - Only PENDING orders can be canceled
   - Releases reserved balances

3. **Order Matching**
   - Admin only operation
   - Updates final balances
   - Cannot match non-PENDING orders

4. **Asset Management**
   - TRY is treated as an asset
   - Assets track total and usable amounts
   - Cannot withdraw more than usable balance

5. **Authorization**
   - Users can only access their own data
   - Admin can access all data
   - Order matching restricted to admin

## Error Cases

1. **Insufficient Balance**
```bash
# Trying to create order without sufficient funds
curl -X POST "http://localhost:8080/api/orders" \
-H "Content-Type: application/json" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz" \
-d '{
    "customerId": "CUST1",
    "assetName": "AAPL",
    "side": "BUY",
    "size": 1000.0,
    "price": 150.0
}'
```
Expected: 400 Bad Request - "Insufficient TRY balance for order"

2. **Unauthorized Access**
```bash
# User trying to access another customer's data
curl -X GET "http://localhost:8080/api/orders?customerId=CUST2" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz"
```
Expected: 403 Forbidden - "Not authorized to access this data"

3. **Invalid Order Cancellation**
```bash
# Trying to cancel matched order
curl -X DELETE "http://localhost:8080/api/orders/{matchedOrderId}" \
-H "Authorization: Basic dXNlcjp1c2VyMTIz"
```
Expected: 400 Bad Request - "Only pending orders can be canceled"
