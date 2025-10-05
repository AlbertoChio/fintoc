-- ===============================================
-- Fintoc API Logger - Unified SQL Server Database Setup
-- ===============================================

-- Create database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'FintocApiLogger')
BEGIN
    CREATE DATABASE FintocApiLogger
    COLLATE SQL_Latin1_General_CP1_CI_AS;
END
GO

USE FintocApiLogger;
GO

-- ===============================================
-- Create Tables
-- ===============================================

-- Table: logsbook (Simplified 9-column schema)
-- Stores raw HTTP request/response data for account validation API calls
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[logsbook]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[logsbook] (
        [id] BIGINT IDENTITY(1,1) NOT NULL,
        [type] NVARCHAR(50) NOT NULL,
        [url] NVARCHAR(500) NOT NULL,
        [request_headers] NTEXT NULL,
        [request_body] NTEXT NULL,
        [response_status] INT NOT NULL,
        [response_headers] NTEXT NULL,
        [response_body] NTEXT NULL,
        [created_at] DATETIME2(7) NOT NULL DEFAULT GETUTCDATE(),
        
        CONSTRAINT [PK_logsbook] PRIMARY KEY CLUSTERED ([id] ASC)
    );
    
    PRINT 'Table logsbook created successfully.';
END
ELSE
BEGIN
    PRINT 'Table logsbook already exists.';
END
GO

-- Table: account_validation (Structured response data)
-- Stores parsed Fintoc API response data for analysis and querying
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[account_validation] (
        [id] NVARCHAR(255) NOT NULL,
        [object_type] NVARCHAR(100) NULL,
        [status] NVARCHAR(50) NULL,
        [reason] NVARCHAR(500) NULL,
        [transfer_id] NVARCHAR(255) NULL,
        [mode] NVARCHAR(20) NULL,
        [receipt_url] NVARCHAR(500) NULL,
        [transaction_date] NVARCHAR(100) NULL,
        
        -- Counterparty fields (embedded)
        [counterparty_account_number] NVARCHAR(255) NULL,
        [counterparty_holder_id] NVARCHAR(255) NULL,
        [counterparty_holder_name] NVARCHAR(255) NULL,
        [counterparty_account_type] NVARCHAR(50) NULL,
        
        -- Institution fields (embedded within counterparty)
        [institution_id] NVARCHAR(255) NULL,
        [institution_name] NVARCHAR(255) NULL,
        [institution_country] NVARCHAR(10) NULL,
        
        -- Audit fields
        [created_at] DATETIME2(7) NOT NULL DEFAULT GETUTCDATE(),
        [updated_at] DATETIME2(7) NOT NULL DEFAULT GETUTCDATE(),
        
        CONSTRAINT [PK_account_validation] PRIMARY KEY CLUSTERED ([id] ASC)
    );
    
    PRINT 'Table account_validation created successfully.';
END
ELSE
BEGIN
    PRINT 'Table account_validation already exists.';
END
GO

-- Table: validation_usage_stats (Aggregated statistics)
-- Stores aggregated statistics for account validation analysis
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[validation_usage_stats]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[validation_usage_stats] (
        [id] BIGINT IDENTITY(1,1) NOT NULL,
        [validation_type] NVARCHAR(50) NOT NULL,
        [validation_count] BIGINT NOT NULL DEFAULT 0,
        [success_count] BIGINT NOT NULL DEFAULT 0,
        [failed_count] BIGINT NOT NULL DEFAULT 0,
        [pending_count] BIGINT NOT NULL DEFAULT 0,
        [avg_execution_time_ms] FLOAT NULL,
        [last_validated_at] DATETIME2(7) NULL,
        [created_at] DATETIME2(7) NOT NULL DEFAULT GETUTCDATE(),
        [updated_at] DATETIME2(7) NOT NULL DEFAULT GETUTCDATE(),
        
        CONSTRAINT [PK_validation_usage_stats] PRIMARY KEY CLUSTERED ([id] ASC),
        CONSTRAINT [UQ_validation_usage_stats_type] UNIQUE ([validation_type])
    );
    
    PRINT 'Table validation_usage_stats created successfully.';
END
ELSE
BEGIN
    PRINT 'Table validation_usage_stats already exists.';
END
GO

-- ===============================================
-- Create Indexes for Performance
-- ===============================================

-- Indexes on logsbook table (simplified schema)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[logsbook]') AND name = N'IX_logsbook_type')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_logsbook_type] 
    ON [dbo].[logsbook] ([type] ASC);
    PRINT 'Index IX_logsbook_type created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[logsbook]') AND name = N'IX_logsbook_created_at')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_logsbook_created_at] 
    ON [dbo].[logsbook] ([created_at] DESC);
    PRINT 'Index IX_logsbook_created_at created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[logsbook]') AND name = N'IX_logsbook_response_status')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_logsbook_response_status] 
    ON [dbo].[logsbook] ([response_status] ASC);
    PRINT 'Index IX_logsbook_response_status created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[logsbook]') AND name = N'IX_logsbook_url')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_logsbook_url] 
    ON [dbo].[logsbook] ([url] ASC);
    PRINT 'Index IX_logsbook_url created.';
END

-- Indexes on account_validation table
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND name = N'IX_account_validation_status')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_account_validation_status] 
    ON [dbo].[account_validation] ([status] ASC);
    PRINT 'Index IX_account_validation_status created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND name = N'IX_account_validation_mode')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_account_validation_mode] 
    ON [dbo].[account_validation] ([mode] ASC);
    PRINT 'Index IX_account_validation_mode created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND name = N'IX_account_validation_transfer_id')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_account_validation_transfer_id] 
    ON [dbo].[account_validation] ([transfer_id] ASC);
    PRINT 'Index IX_account_validation_transfer_id created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND name = N'IX_account_validation_created_at')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_account_validation_created_at] 
    ON [dbo].[account_validation] ([created_at] DESC);
    PRINT 'Index IX_account_validation_created_at created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND name = N'IX_account_validation_institution_id')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_account_validation_institution_id] 
    ON [dbo].[account_validation] ([institution_id] ASC);
    PRINT 'Index IX_account_validation_institution_id created.';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[account_validation]') AND name = N'IX_account_validation_counterparty_account_type')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_account_validation_counterparty_account_type] 
    ON [dbo].[account_validation] ([counterparty_account_type] ASC);
    PRINT 'Index IX_account_validation_counterparty_account_type created.';
END

-- Indexes on validation_usage_stats table
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'[dbo].[validation_usage_stats]') AND name = N'IX_validation_stats_count')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_validation_stats_count] 
    ON [dbo].[validation_usage_stats] ([validation_count] DESC);
    PRINT 'Index IX_validation_stats_count created.';
END

-- ===============================================
-- Create Views for Common Queries
-- ===============================================

-- View: Recent Validation Logs (Last 24 hours) - Simplified Schema
IF EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vw_recent_validations]'))
    DROP VIEW [dbo].[vw_recent_validations];
GO

CREATE VIEW [dbo].[vw_recent_validations] AS
SELECT 
    id,
    type,
    url,
    response_status,
    created_at,
    CASE 
        WHEN response_status >= 200 AND response_status < 300 THEN 'Success'
        WHEN response_status >= 400 AND response_status < 500 THEN 'Client Error'
        WHEN response_status >= 500 THEN 'Server Error'
        ELSE 'Unknown'
    END AS status_description
FROM logsbook
WHERE created_at >= DATEADD(HOUR, -24, GETUTCDATE());
GO

-- View: Validation Performance Summary - Simplified Schema
IF EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vw_validation_summary]'))
    DROP VIEW [dbo].[vw_validation_summary];
GO

CREATE VIEW [dbo].[vw_validation_summary] AS
SELECT 
    type,
    COUNT(*) AS total_calls,
    SUM(CASE WHEN response_status >= 200 AND response_status < 300 THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN response_status >= 400 THEN 1 ELSE 0 END) AS error_count,
    CASE 
        WHEN COUNT(*) > 0 THEN CAST((SUM(CASE WHEN response_status >= 200 AND response_status < 300 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS DECIMAL(5,2))
        ELSE 0
    END AS success_rate_percent,
    MAX(created_at) AS last_call_at
FROM logsbook
GROUP BY type;
GO

-- View: Successful Validations (Structured Data)
IF EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vw_successful_validations]'))
    DROP VIEW [dbo].[vw_successful_validations];
GO

CREATE VIEW [dbo].[vw_successful_validations] AS
SELECT 
    id,
    status,
    transfer_id,
    counterparty_holder_name,
    counterparty_account_number,
    institution_name,
    institution_country,
    mode,
    created_at
FROM account_validation 
WHERE status = 'succeeded';
GO

-- View: Validation Statistics (Structured Data)
IF EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[vw_validation_stats]'))
    DROP VIEW [dbo].[vw_validation_stats];
GO

CREATE VIEW [dbo].[vw_validation_stats] AS
SELECT 
    status,
    mode,
    institution_country,
    counterparty_account_type,
    COUNT(*) as count,
    MIN(created_at) as first_validation,
    MAX(created_at) as last_validation
FROM account_validation 
GROUP BY status, mode, institution_country, counterparty_account_type;
GO

-- ===============================================
-- Create Stored Procedures
-- ===============================================

-- Procedure: Clean up old validation logs
IF EXISTS (SELECT * FROM sys.procedures WHERE object_id = OBJECT_ID(N'[dbo].[sp_cleanup_old_validation_logs]'))
    DROP PROCEDURE [dbo].[sp_cleanup_old_validation_logs];
GO

CREATE PROCEDURE [dbo].[sp_cleanup_old_validation_logs]
    @days_to_keep INT = 30
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @cutoff_date DATETIME2(7);
    DECLARE @deleted_logs_count INT;
    DECLARE @deleted_responses_count INT;
    
    SET @cutoff_date = DATEADD(DAY, -@days_to_keep, GETUTCDATE());
    
    -- Clean up old logs
    DELETE FROM logsbook 
    WHERE created_at < @cutoff_date;
    SET @deleted_logs_count = @@ROWCOUNT;
    
    -- Clean up old responses
    DELETE FROM account_validation 
    WHERE created_at < @cutoff_date;
    SET @deleted_responses_count = @@ROWCOUNT;
    
    PRINT CONCAT('Deleted ', @deleted_logs_count, ' validation logs and ', @deleted_responses_count, ' validation responses older than ', @days_to_keep, ' days.');
END
GO

-- Procedure: Get validation statistics for a date range (Simplified Schema)
IF EXISTS (SELECT * FROM sys.procedures WHERE object_id = OBJECT_ID(N'[dbo].[sp_get_validation_stats_by_date_range]'))
    DROP PROCEDURE [dbo].[sp_get_validation_stats_by_date_range];
GO

CREATE PROCEDURE [dbo].[sp_get_validation_stats_by_date_range]
    @start_date DATETIME2(7),
    @end_date DATETIME2(7)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        type,
        COUNT(*) AS total_calls,
        SUM(CASE WHEN response_status >= 200 AND response_status < 300 THEN 1 ELSE 0 END) AS successful_calls,
        SUM(CASE WHEN response_status >= 400 AND response_status < 500 THEN 1 ELSE 0 END) AS client_errors,
        SUM(CASE WHEN response_status >= 500 THEN 1 ELSE 0 END) AS server_errors,
        AVG(CAST(response_status AS FLOAT)) AS avg_response_status,
        MIN(created_at) AS first_call,
        MAX(created_at) AS last_call
    FROM logsbook
    WHERE created_at BETWEEN @start_date AND @end_date
    GROUP BY type
    ORDER BY total_calls DESC;
END
GO

-- ===============================================
-- Insert Sample Data (Optional)
-- ===============================================

-- Sample validation statistics entries
IF NOT EXISTS (SELECT 1 FROM validation_usage_stats WHERE validation_type = 'account_validation')
BEGIN
    INSERT INTO validation_usage_stats (validation_type, validation_count, success_count, failed_count, pending_count)
    VALUES ('account_validation', 0, 0, 0, 0);
    PRINT 'Sample validation stats for account_validation created.';
END

-- ===============================================
-- Grant Permissions (Adjust as needed)
-- ===============================================

-- Create application user (uncomment and modify as needed)
/*
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'fintoc_app_user')
BEGIN
    CREATE USER [fintoc_app_user] FOR LOGIN [fintoc_app_user];
    PRINT 'User fintoc_app_user created.';
END

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON logsbook TO fintoc_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON account_validation TO fintoc_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON validation_usage_stats TO fintoc_app_user;
GRANT SELECT ON vw_recent_validations TO fintoc_app_user;
GRANT SELECT ON vw_validation_summary TO fintoc_app_user;
GRANT SELECT ON vw_successful_validations TO fintoc_app_user;
GRANT SELECT ON vw_validation_stats TO fintoc_app_user;
GRANT EXECUTE ON sp_cleanup_old_validation_logs TO fintoc_app_user;
GRANT EXECUTE ON sp_get_validation_stats_by_date_range TO fintoc_app_user;

PRINT 'Permissions granted to fintoc_app_user.';
*/

-- ===============================================
-- Final Status
-- ===============================================

PRINT '================================================';
PRINT 'Fintoc Account Validation Logger Database Setup Complete!';
PRINT '================================================';
PRINT 'Tables created:';
PRINT '  - logsbook (simplified 9-column schema)';
PRINT '  - account_validation (structured Fintoc response data)';
PRINT '  - validation_usage_stats (aggregated statistics)';
PRINT 'Views created:';
PRINT '  - vw_recent_validations (last 24 hours)';
PRINT '  - vw_validation_summary (performance metrics)';
PRINT '  - vw_successful_validations (successful responses only)';
PRINT '  - vw_validation_stats (response statistics)';
PRINT 'Stored Procedures created:';
PRINT '  - sp_cleanup_old_validation_logs';
PRINT '  - sp_get_validation_stats_by_date_range';
PRINT 'Indexes created for optimal query performance.';
PRINT '================================================';

-- Show table information
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    ty.name AS DataType,
    c.max_length,
    c.is_nullable,
    c.is_identity
FROM sys.tables t
INNER JOIN sys.columns c ON t.object_id = c.object_id
INNER JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE t.name IN ('logsbook', 'account_validation', 'validation_usage_stats')
ORDER BY t.name, c.column_id;

GO