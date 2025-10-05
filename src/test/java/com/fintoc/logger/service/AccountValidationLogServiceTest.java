package com.fintoc.logger.service;

import com.fintoc.logger.entity.AccountValidationLog;
import com.fintoc.logger.repository.AccountValidationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AccountValidationLogServiceTest {

    @Autowired
    private AccountValidationLogService validationLogService;

    @Autowired
    private AccountValidationLogRepository validationLogRepository;

    @Test
    public void testSaveValidationLog() {
        // Create a test validation log
        AccountValidationLog validationLog = new AccountValidationLog("test_account_123", "ownership");
        validationLog.setResponseStatus(200);
        validationLog.setSuccess(true);
        validationLog.setValidationResult("SUCCESS");
        validationLog.setExecutionTimeMs(150L);

        // Save the validation log
        AccountValidationLog savedLog = validationLogService.saveValidationLog(validationLog);

        // Verify it was saved
        assertNotNull(savedLog.getId());
        assertEquals("test_account_123", savedLog.getAccountId());
        assertEquals("ownership", savedLog.getValidationType());
        assertEquals("SUCCESS", savedLog.getValidationResult());
        assertEquals(200, savedLog.getResponseStatus());
        assertTrue(savedLog.getSuccess());

        // Verify it exists in the database
        assertTrue(validationLogRepository.findById(savedLog.getId()).isPresent());
    }

    @Test
    public void testCreateValidationLog() {
        // Test the createValidationLog method
        AccountValidationLog createdLog = validationLogService.createValidationLog(
            "test_account_456",
            "balance",
            "{\"Authorization\":\"Bearer test\"}",
            "{\"validation_type\":\"balance\"}",
            200,
            "{\"Content-Type\":\"application/json\"}",
            "{\"result\":\"valid\"}",
            250L,
            "test_key_****",
            true,
            null,
            "link_123"
        );

        // Verify the log was created and saved
        assertNotNull(createdLog.getId());
        assertEquals("test_account_456", createdLog.getAccountId());
        assertEquals("balance", createdLog.getValidationType());
        assertEquals("SUCCESS", createdLog.getValidationResult()); // Should be SUCCESS based on status 200
        assertEquals(200, createdLog.getResponseStatus());
        assertTrue(createdLog.getSuccess());
        assertEquals("link_123", createdLog.getLinkId());
    }

    @Test
    public void testGetTotalValidationLogsCount() {
        // Initially should be 0
        long initialCount = validationLogService.getTotalValidationLogsCount();

        // Add a test validation log
        AccountValidationLog validationLog = new AccountValidationLog("test_account_789", "ownership");
        validationLog.setResponseStatus(201);
        validationLog.setSuccess(true);
        validationLog.setValidationResult("SUCCESS");
        validationLogService.saveValidationLog(validationLog);

        // Count should increase by 1
        long newCount = validationLogService.getTotalValidationLogsCount();
        assertEquals(initialCount + 1, newCount);
    }

    @Test
    public void testValidationResultHelperMethods() {
        AccountValidationLog log = new AccountValidationLog("test_account", "test");
        
        log.setValidationResult("SUCCESS");
        assertTrue(log.isValidationSuccessful());
        assertFalse(log.isValidationFailed());
        assertFalse(log.isValidationPending());
        
        log.setValidationResult("FAILED");
        assertFalse(log.isValidationSuccessful());
        assertTrue(log.isValidationFailed());
        assertFalse(log.isValidationPending());
        
        log.setValidationResult("PENDING");
        assertFalse(log.isValidationSuccessful());
        assertFalse(log.isValidationFailed());
        assertTrue(log.isValidationPending());
    }
}