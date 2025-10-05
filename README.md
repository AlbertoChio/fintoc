# Fintoc Account Validation Logger

A Java 8 Spring Boot application focused exclusively on logging **Fintoc API v2 account validation calls** and their responses.

## Features

- 🎯 **Account Validation Focused** - Only logs `/accounts/{id}/validate` calls
- 🚀 **Fintoc API v2 Integration** - Uses the latest Fintoc API version
- 📊 **Detailed Validation Logging** - Stores validation requests, responses, and results
- 🔒 **Secure HMAC-SHA256 Authentication** - Proper Fintoc API authentication
- 📈 **Validation Analytics** - Track success rates, performance, and trends
- 🗃️ **SQL Server Database** - Optimized for validation data storage
- ⚡ **Built with Java 8 and Spring Boot 2.7.x**ount Validation Logger

A Java 8 Spring Boot application focused exclusively on logging **Fintoc account validation API calls** and their responses.

## Features

- � **Account Validation Focused** - Only logs `/accounts/{id}/validate` calls
- 📊 **Detailed Validation Logging** - Stores validation requests, responses, and results
- 🔒 **Secure HMAC-SHA256 Authentication** - Proper Fintoc API authentication
- 📈 **Validation Analytics** - Track success rates, performance, and trends
- 🗃️ **SQL Server Database** - Optimized for validation data storage
- ⚡ **Built with Java 8 and Spring Boot 2.7.x**

## API Endpoints

### Validation Endpoint (with logging):
- `POST /api/fintoc/accounts/{accountId}/validate` - Validate account (logged to database)

### Validation Logs & Analytics:
- `GET /api/validation-logs` - Get all validation logs with pagination
- `GET /api/validation-logs/account/{accountId}` - Get logs for specific account
- `GET /api/validation-logs/type/{validationType}` - Get logs by validation type
- `GET /api/validation-logs/failed` - Get failed validation attempts
- `GET /api/validation-logs/pending` - Get pending validations
- `GET /api/validation-logs/search` - Search validation logs by criteria
- `GET /api/validation-logs/stats/summary` - Get validation summary statistics
- `GET /api/validation-logs/count` - Get validation counts

## Authentication

This application implements **proper Fintoc API authentication** according to their official documentation:

### **HMAC-SHA256 Signature Authentication**

Every API request includes:

1. **Authorization Header**: Your Fintoc API key
2. **Fintoc-Signature Header**: HMAC-SHA256 signature 
### **Signature Generation Process**

The signature is generated using the following message format:
```
timestamp + HTTP_METHOD + endpoint + request_body
```

**Example:**
```
1633024800POST/accounts/acc_123/validate{"validation_type":"ownership"}
```

This message is then signed using HMAC-SHA256 with your API secret and Base64 encoded.

### **API Version**

This application uses **Fintoc API v2** (`https://api.fintoc.com/v2`) for all API calls, ensuring compatibility with the latest Fintoc features and improvements.

### **Implementation Details**

- ✅ **Automatic signature generation** for every API call
- ✅ **Timestamp-based security** prevents replay attacks  
- ✅ **Secure credential handling** - API secrets are never logged
- ✅ **Error handling** for authentication failures

## Prerequisites

- Java 8 or higher
- Maven 3.6+
- SQL Server 2012+ (or use H2 for testing)

## Setup

1. **Clone and navigate to the project:**
   ```bash
   cd /path/to/fintoc-account-validation-logger
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your actual credentials
   ```

3. **Configure database:**
   - Create a SQL Server database named `FintocApiLogger`
   - Run the database creation script: `database/sqlserver/create_database.sql`
   - Update `application.yml` with your database credentials
   
4. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   Or for SQL Server:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=sqlserver
   ```

## Configuration

### Environment Variables

Create a `.env` file or set environment variables:

```bash
FINTOC_API_KEY=your_fintoc_api_key
FINTOC_API_SECRET=your_fintoc_api_secret
DB_USERNAME=fintoc_user
DB_PASSWORD=your_secure_password
```

## Database Schema

### Tables Created

#### 1. `account_validation_logs`
Stores detailed information about each account validation:

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT IDENTITY | Primary key |
| `account_id` | NVARCHAR(100) | Account being validated |
| `validation_type` | NVARCHAR(50) | Type of validation (ownership, balance, etc.) |
| `validation_result` | NVARCHAR(20) | SUCCESS, FAILED, or PENDING |
| `request_headers` | NTEXT | Request headers JSON |
| `request_body` | NTEXT | Request body |
| `response_status` | INT | HTTP response status |
| `response_headers` | NTEXT | Response headers JSON |
| `response_body` | NTEXT | Response body |
| `execution_time_ms` | BIGINT | Execution time in milliseconds |
| `created_at` | DATETIME2(7) | Timestamp (UTC) |
| `success` | BIT | Success flag |
| `link_id` | NVARCHAR(100) | Associated Fintoc link ID |

#### 2. `validation_usage_stats`
Stores aggregated validation statistics:

| Column | Type | Description |
|--------|------|-------------|
| `validation_type` | NVARCHAR(50) | Validation type |
| `validation_count` | BIGINT | Total validations |
| `success_count` | BIGINT | Successful validations |
| `failed_count` | BIGINT | Failed validations |
| `pending_count` | BIGINT | Pending validations |
| `avg_execution_time_ms` | FLOAT | Average execution time |

## Usage Examples

### Account Validation (Logged)

```bash
# This call will be logged to the database
curl -X POST http://localhost:8080/api/fintoc/accounts/acc_123/validate \
  -H "Content-Type: application/json" \
  -d '{"validation_type": "ownership"}'
```

### Accessing Validation Logs

```bash
# Get recent validation logs
curl http://localhost:8080/api/validation-logs?page=0&size=10

# Get failed validations
curl http://localhost:8080/api/validation-logs/failed

# Get validations for a specific account
curl http://localhost:8080/api/validation-logs/account/acc_123

# Get validation statistics
curl http://localhost:8080/api/validation-logs/stats/summary
```

## Project Structure

```
src/
├── main/java/com/fintoc/logger/
│   ├── FintocApiLoggerApplication.java      # Main Spring Boot application
│   ├── config/
│   │   └── AppConfig.java                   # Spring configuration
│   ├── controller/
│   │   ├── FintocController.java            # Account validation endpoint
│   │   └── ValidationLogsController.java    # Logs and analytics endpoints
│   ├── entity/
│   │   └── AccountValidationLog.java        # Validation log entity
│   ├── repository/
│   │   └── AccountValidationLogRepository.java # Validation logs repository
│   └── service/
│       ├── FintocApiService.java            # Fintoc API client service
│       └── AccountValidationLogService.java # Validation logging service
└── main/resources/
    ├── application.yml                      # Main configuration
    ├── application-sqlserver.yml            # SQL Server configuration
    └── application-test.yml                 # Test configuration
```

## What Gets Logged

✅ **Account ID** - Which account was validated  
✅ **Validation Type** - Type of validation performed (ownership, balance, etc.)  
✅ **Request/Response Data** - Complete HTTP request and response  
✅ **Validation Result** - SUCCESS, FAILED, or PENDING  
✅ **Performance Metrics** - Execution time, response codes  
✅ **Error Details** - Complete error information for failed validations  
✅ **Authentication Data** - Masked API keys used  

## Testing

Run tests with:
```bash
mvn test
```

Tests use H2 in-memory database for isolation.

## Monitoring

The application includes Spring Boot Actuator endpoints:
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics

## Database Maintenance

Clean up old validation logs:
```bash
curl -X DELETE "http://localhost:8080/api/validation-logs/cleanup?daysToKeep=30"
```

## License

This project is licensed under the MIT License.