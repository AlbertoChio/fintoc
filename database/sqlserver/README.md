# SQL Server Setup for Fintoc API Logger

This document provides instructions for setting up the Fintoc API Logger with Microsoft SQL Server.

## Prerequisites

- Microsoft SQL Server 2012 or later
- SQL Server Management Studio (SSMS) or Azure Data Studio
- Java 8+ with Maven
- SQL Server JDBC Driver (included in Maven dependencies)

## Database Setup

### 1. Run the Database Creation Script

Execute the SQL Server database creation script:

```sql
-- Run this script in SQL Server Management Studio
-- File: database/sqlserver/create_database.sql
```

The script will:
- ✅ Create the `FintocApiLogger` database
- ✅ Create tables: `fintoc_api_calls` and `api_usage_stats`
- ✅ Create performance indexes
- ✅ Create useful views for monitoring
- ✅ Create stored procedures for maintenance

### 2. Create Database User (Optional)

```sql
-- Create login and user for the application
CREATE LOGIN fintoc_app_user WITH PASSWORD = 'YourSecurePassword123!';
USE FintocApiLogger;
CREATE USER fintoc_app_user FOR LOGIN fintoc_app_user;

-- Grant necessary permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON fintoc_api_calls TO fintoc_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON api_usage_stats TO fintoc_app_user;
GRANT SELECT ON vw_recent_api_calls TO fintoc_app_user;
GRANT SELECT ON vw_api_performance_summary TO fintoc_app_user;
GRANT EXECUTE ON sp_cleanup_old_api_calls TO fintoc_app_user;
GRANT EXECUTE ON sp_get_usage_stats_by_date_range TO fintoc_app_user;
```

## Application Configuration

### 1. Use SQL Server Profile

Run the application with the SQL Server configuration:

```bash
mvn spring-boot:run -Dspring.profiles.active=sqlserver
```

### 2. Environment Variables

Set these environment variables:

```bash
# Database Configuration
DB_USERNAME=fintoc_app_user
DB_PASSWORD=YourSecurePassword123!

# Fintoc API Configuration  
FINTOC_API_KEY=your_fintoc_api_key
FINTOC_API_SECRET=your_fintoc_api_secret

# Optional: Custom database URL
# DB_URL=jdbc:sqlserver://your-server:1433;databaseName=FintocApiLogger;trustServerCertificate=true
```

### 3. Connection String Options

The default connection string includes:
- `trustServerCertificate=true` - For local development
- `databaseName=FintocApiLogger` - Target database
- Standard port `1433`

For production, consider additional security options:
```
jdbc:sqlserver://prod-server:1433;databaseName=FintocApiLogger;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

## Database Schema

### Tables Created

#### 1. `fintoc_api_calls`
Stores detailed information about each Fintoc API call:

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT IDENTITY | Primary key |
| `endpoint` | NVARCHAR(500) | API endpoint called |
| `http_method` | NVARCHAR(10) | HTTP method (GET, POST, etc.) |
| `request_headers` | NTEXT | Request headers JSON |
| `request_body` | NTEXT | Request body |
| `response_status` | INT | HTTP response status |
| `response_headers` | NTEXT | Response headers JSON |
| `response_body` | NTEXT | Response body |
| `execution_time_ms` | BIGINT | Execution time in milliseconds |
| `created_at` | DATETIME2(7) | Timestamp (UTC) |
| `success` | BIT | Success flag |
| `link_id` | NVARCHAR(100) | Fintoc link ID |
| `account_id` | NVARCHAR(100) | Fintoc account ID |

#### 2. `api_usage_stats`
Stores aggregated usage statistics:

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT IDENTITY | Primary key |
| `endpoint` | NVARCHAR(500) | API endpoint pattern |
| `call_count` | BIGINT | Total number of calls |
| `success_count` | BIGINT | Number of successful calls |
| `error_count` | BIGINT | Number of failed calls |
| `avg_execution_time_ms` | FLOAT | Average execution time |
| `last_called_at` | DATETIME2(7) | Last call timestamp |

### Views Available

#### 1. `vw_recent_api_calls`
Shows API calls from the last 24 hours with status descriptions.

#### 2. `vw_api_performance_summary`
Provides performance metrics including success rates and execution times.

### Stored Procedures

#### 1. `sp_cleanup_old_api_calls`
```sql
EXEC sp_cleanup_old_api_calls @days_to_keep = 30;
```
Removes API call logs older than specified days.

#### 2. `sp_get_usage_stats_by_date_range`
```sql
EXEC sp_get_usage_stats_by_date_range 
    @start_date = '2023-01-01', 
    @end_date = '2023-12-31';
```
Returns usage statistics for a specific date range.

## Performance Considerations

### Indexes Created
- `IX_fintoc_api_calls_endpoint` - For endpoint-based queries
- `IX_fintoc_api_calls_created_at` - For date-based queries
- `IX_fintoc_api_calls_success` - For success/failure filtering
- `IX_fintoc_api_calls_link_id` - For link-based queries
- `IX_fintoc_api_calls_account_id` - For account-based queries
- `IX_fintoc_api_calls_execution_time` - For performance analysis

### Maintenance

#### Regular Cleanup
Schedule regular cleanup of old logs:

```sql
-- Weekly cleanup job (keep last 30 days)
EXEC sp_cleanup_old_api_calls @days_to_keep = 30;
```

#### Monitoring Queries

```sql
-- Check database size
SELECT 
    DB_NAME() AS DatabaseName,
    (SUM(size) * 8 / 1024) AS SizeMB
FROM sys.database_files;

-- Check table sizes
SELECT 
    t.name AS TableName,
    p.rows AS RowCount,
    (SUM(a.total_pages) * 8 / 1024) AS TotalSpaceMB
FROM sys.tables t
INNER JOIN sys.partitions p ON t.object_id = p.object_id
INNER JOIN sys.allocation_units a ON p.partition_id = a.container_id
WHERE t.name IN ('fintoc_api_calls', 'api_usage_stats')
GROUP BY t.name, p.rows;
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**
   - Increase `loginTimeout` in connection string
   - Check SQL Server is running and accessible

2. **Authentication Failed**
   - Verify username/password
   - Check user permissions on database

3. **Certificate Issues**
   - Use `trustServerCertificate=true` for local development
   - Configure proper SSL certificates for production

### Logging

Enable SQL logging in `application-sqlserver.yml`:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Azure SQL Database

For Azure SQL Database, use this connection string format:
```
jdbc:sqlserver://your-server.database.windows.net:1433;database=FintocApiLogger;user=your-user@your-server;password=your-password;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

## Security Best Practices

1. **Use strong passwords** for database users
2. **Enable SSL encryption** in production
3. **Limit user permissions** to only required operations
4. **Regular backups** of the database
5. **Monitor access logs** for suspicious activity
6. **Keep SQL Server updated** with latest patches