package com.fintoc.logger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintoc.logger.entity.AccountValidationResponse;
import com.fintoc.logger.repository.AccountValidationResponseRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FintocApiService {

    private static final Logger logger = LoggerFactory.getLogger(FintocApiService.class);
    private static final String FINTOC_BASE_URL = "https://api.fintoc.com/v2";

    @Value("${fintoc.api.key:}")
    private String apiKey;

    @Value("${fintoc.api.secret:}")
    private String apiSecret;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final AccountValidationLogService validationLogService;
    private final AccountValidationResponseRepository responseRepository;
    private final JwsSignatureService jwsSignatureService;

    @Autowired
    public FintocApiService(ObjectMapper objectMapper,
                           AccountValidationLogService validationLogService,
                           AccountValidationResponseRepository responseRepository,
                           JwsSignatureService jwsSignatureService) {
        this.client = new OkHttpClient();
        this.objectMapper = objectMapper;
        this.validationLogService = validationLogService;
        this.responseRepository = responseRepository;
        this.jwsSignatureService = jwsSignatureService;
    }

    /**
     * Validate an account - THIS IS THE MAIN METHOD WITH LOGGING
     */
    public ResponseEntity<AccountValidationResponse> validateAccount(String accountId) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/account_verifications";
        String fullUrl = FINTOC_BASE_URL + endpoint;
        
        String outgoingHeadersJson = null;
        
        try {
            // Create request body exactly like the OkHttp example
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create("{\"account_number\":\"" + accountId + "\"}", mediaType);
            
            // Generate JWS signature using the same method as /api/jws/create-jws-header
            String signature = jwsSignatureService.createJwsHeader("{\"account_number\":\"" + accountId + "\"}");

            Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", apiKey)
                .addHeader("Fintoc-JWS-Signature", signature)
                .addHeader("content-type", "application/json")
                .build();
            
            // Serialize the headers that we're sending to Fintoc for logging
            try {
                outgoingHeadersJson = objectMapper.writeValueAsString(request.headers().toMultimap());
                logger.info("Outgoing Fintoc headers: {}", outgoingHeadersJson);
            } catch (Exception e) {
                logger.warn("Failed to serialize outgoing headers: {}", e.getMessage());
                outgoingHeadersJson = "Failed to serialize outgoing headers";
            }
            
            // Make the API call
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            
            // Parse to typed response
            AccountValidationResponse validationResponse = objectMapper.readValue(responseBody, AccountValidationResponse.class);

            if (responseBody == null) {
                throw new RuntimeException("Empty response body from Fintoc");
            }

            // Save the response entity to database
            try {
                responseRepository.save(validationResponse);
                logger.info("Saved AccountValidationResponse to database: {}", validationResponse.getId());
            } catch (Exception e) {
                logger.error("Failed to save AccountValidationResponse to database: {}", e.getMessage());
                // Don't fail the main operation if database save fails
            }
            
            // Log successful validation with OUTGOING headers (headers sent to Fintoc)
            long executionTime = System.currentTimeMillis() - startTime;
            
            validationLogService.createValidationLog(
                accountId,
                null,
                outgoingHeadersJson, // The headers WE SENT to Fintoc
                 "{\"account_number\":\"" + accountId + "\"}",
                response.code(),
                null, // response headers - we don't need them
                responseBody,
                executionTime,
                maskApiKey(apiKey),
                true,
                null,
                validationResponse.getId()
            );
            
            logger.info("Account validation successful: {} - Type: {} - Status: {} - ValidationId: {} - Time: {}ms", 
                       accountId, null, response.code(), validationResponse.getId(), executionTime);
            
            // Return typed response
            return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(response.code()));
            
        } catch (Exception e) {
            // Log errors
            long executionTime = System.currentTimeMillis() - startTime;
            
            int statusCode = 0;
            String errorBody = null;
            
            // Try to extract status code and error body if it's an HTTP error
            if (e.getMessage() != null && e.getMessage().contains("HTTP")) {
                try {
                    // Basic parsing for HTTP errors
                    String message = e.getMessage();
                    if (message.contains("HTTP ")) {
                        String[] parts = message.split("HTTP ");
                        if (parts.length > 1) {
                            statusCode = Integer.parseInt(parts[1].split(" ")[0]);
                        }
                    }
                } catch (Exception parseEx) {
                    logger.debug("Failed to parse HTTP status from error: {}", parseEx.getMessage());
                }
            }
            
            validationLogService.createValidationLog(
                accountId,
                null,
                outgoingHeadersJson, // The headers WE SENT to Fintoc
                "{\"account_number\":\"" + accountId + "\"}",
                statusCode > 0 ? statusCode : null,
                null,
                errorBody,
                executionTime,
                maskApiKey(apiKey),
                false,
                e.getMessage(),
                null
            );
            
            logger.error("Account validation error: {} - Error: {} - Outgoing headers: {}", 
                        accountId, e.getMessage(),
                        outgoingHeadersJson != null ? outgoingHeadersJson.substring(0, Math.min(100, outgoingHeadersJson.length())) : "null");
            
            throw new RuntimeException("Account validation failed", e);
        }
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}