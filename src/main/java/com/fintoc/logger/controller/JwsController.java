package com.fintoc.logger.controller;

import com.fintoc.logger.service.JwsSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller for testing JWS signature functionality
 */
@RestController
@RequestMapping("/api/jws")
public class JwsController {
    
    private static final Logger logger = LoggerFactory.getLogger(JwsController.class);
    private final JwsSignatureService jwsSignatureService;
    
    @Autowired
    public JwsController(JwsSignatureService jwsSignatureService) {
        this.jwsSignatureService = jwsSignatureService;
    }
    
    /**
     * Test endpoint to sign data
     */
    @PostMapping("/sign")
    public ResponseEntity<Map<String, String>> signData(@RequestBody Map<String, String> request) {
        String data = request.get("data");
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data is required"));
        }
        
        try {
            String signature = jwsSignatureService.signData(data);
            
            Map<String, String> response = new HashMap<>();
            response.put("data", data);
            response.put("signature", signature);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to sign data: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to verify signature
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifySignature(@RequestBody Map<String, String> request) {
        String data = request.get("data");
        String signature = request.get("signature");
        
        if (data == null || signature == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Both data and signature are required"));
        }
        
        try {
            boolean isValid = jwsSignatureService.verifySignature(data, signature);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("signature", signature);
            response.put("valid", isValid);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to verify signature: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to create JWS token
     */
    @PostMapping("/create-token")
    public ResponseEntity<Map<String, String>> createJwsToken(@RequestBody Map<String, String> request) {
        String payload = request.get("payload");
        if (payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Payload is required"));
        }
        
        try {
            String jwsToken = jwsSignatureService.createJwsHeader(payload);
            
            Map<String, String> response = new HashMap<>();
            response.put("payload", payload);
            response.put("jws_token", jwsToken);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to create JWS token: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint specifically for createJwsHeader method
     */
    @PostMapping("/create-jws-header")
    public ResponseEntity<Map<String, String>> createJwsHeader(@RequestBody Map<String, String> request) {
        String rawBody = request.get("body");
        if (rawBody == null || rawBody.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Body is required"));
        }
        
        try {
            String jwsHeader = jwsSignatureService.createJwsHeader(rawBody);
            
            Map<String, String> response = new HashMap<>();
            response.put("raw_body", rawBody);
            response.put("jws_header", jwsHeader);
            response.put("status", "success");
            response.put("method", "createJwsHeader");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating JWS header: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to create JWS header: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to create COMPLETE JWS token (header.payload.signature)
     * This includes the payload in the token so it can be decoded later
     */
    @PostMapping("/create-jws-complete")
    public ResponseEntity<Map<String, String>> createCompleteJws(@RequestBody Map<String, String> request) {
        String rawBody = request.get("body");
        if (rawBody == null || rawBody.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Body is required"));
        }
        
        try {
            String completeJws = jwsSignatureService.createCompleteJws(rawBody);
            
            Map<String, String> response = new HashMap<>();
            response.put("raw_body", rawBody);
            response.put("complete_jws", completeJws);
            response.put("format", "complete (header.payload.signature)");
            response.put("status", "success");
            response.put("method", "createCompleteJws");
            response.put("note", "This token contains the payload and can be fully decoded");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to create complete JWS: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to decode JWS header
     */
    @PostMapping("/decode-jws-header")
    public ResponseEntity<Map<String, Object>> decodeJwsHeader(@RequestBody Map<String, String> request) {
        String jwsHeader = request.get("jws_header");
        String originalBody = request.get("original_body"); // Optional: the original body used to create JWS
        
        if (jwsHeader == null || jwsHeader.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "jws_header is required"));
        }
        
        try {
            // Split JWS header into parts
            String[] parts = jwsHeader.split("\\.");
            if (parts.length != 2 && parts.length != 3) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid JWS format. Expected: header.signature OR header.payload.signature"));
            }
            
            // Decode the header part (base64url)
            byte[] headerBytes = java.util.Base64.getUrlDecoder().decode(parts[0]);
            String headerJson = new String(headerBytes, "UTF-8");
            
            // Parse header JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>> typeRef = new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {};
            Map<String, Object> headerMap = mapper.readValue(headerJson, typeRef);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jws_header", jwsHeader);
            response.put("header_base64", parts[0]);
            response.put("decoded_header", headerMap);
            response.put("status", "success");
            response.put("method", "decodeJwsHeader");
            
            // Handle different JWS formats
            if (parts.length == 2) {
                // Detached format: header.signature (no payload)
                response.put("signature_base64", parts[1]);
                response.put("format", "detached (header.signature)");
                
                // If original body was provided, show it
                if (originalBody != null && !originalBody.isEmpty()) {
                    response.put("original_body", originalBody);
                    response.put("body_note", "Original body provided separately (detached JWS format)");
                } else {
                    response.put("payload_note", "No payload in detached format - provide 'original_body' to see it");
                }
            } else if (parts.length == 3) {
                // Complete format: header.payload.signature
                response.put("payload_base64", parts[1]);
                response.put("signature_base64", parts[2]);
                response.put("format", "complete (header.payload.signature)");
                
                // Decode the payload/body
                try {
                    byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
                    String payloadString = new String(payloadBytes, "UTF-8");
                    response.put("decoded_body", payloadString);
                    
                    // Try to parse as JSON if possible
                    try {
                        Object payloadJson = mapper.readValue(payloadString, Object.class);
                        response.put("decoded_body_json", payloadJson);
                    } catch (Exception e) {
                        // Not JSON, keep as string
                        response.put("body_note", "Body is not valid JSON, showing as string");
                    }
                } catch (Exception e) {
                    response.put("decoded_body", "Failed to decode payload: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to decode JWS header: " + e.getMessage()));
        }
    }
    
    /**
     * Verify JWS signature endpoint - validates signature and timestamp
     */
    @PostMapping("/verify-jws")
    public ResponseEntity<Map<String, Object>> verifyJws(@RequestBody Map<String, String> request) {
        String jwsToken = request.get("jws_token");
        String originalBody = request.get("original_body"); // Required for detached format
        
        if (jwsToken == null || jwsToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "JWS token is required"));
        }
        
        try {
            boolean isValid = jwsSignatureService.verifyJwsSignature(jwsToken, originalBody);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jws_token", jwsToken);
            response.put("signature_valid", isValid);
            response.put("method", "verifyJws");
            response.put("status", isValid ? "valid" : "invalid");
            
            if (originalBody != null) {
                response.put("original_body", originalBody);
                response.put("format", "detached (header.signature)");
            } else {
                // Determine format from token parts
                String[] parts = jwsToken.split("\\.");
                response.put("format", parts.length == 3 ? "complete (header.payload.signature)" : "detached (header.signature)");
            }
            
            // Add timestamp info if token is valid
            if (isValid) {
                try {
                    String[] parts = jwsToken.split("\\.");
                    String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), "UTF-8");
                    JsonNode headerNode = new ObjectMapper().readTree(headerJson);
                    if (headerNode.has("ts")) {
                        response.put("timestamp", headerNode.get("ts").asLong());
                        response.put("timestamp_valid", jwsSignatureService.validateTimestamp(headerNode.get("ts").asLong()));
                    }
                    if (headerNode.has("nonce")) {
                        response.put("nonce", headerNode.get("nonce").asText());
                    }
                } catch (Exception e) {
                    // Don't fail verification if timestamp parsing fails
                    logger.warn("Could not parse timestamp from JWS header: {}", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error verifying JWS: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to verify JWS: " + e.getMessage()));
        }
    }
    
    /**
     * Comprehensive JWS validation endpoint - includes nonce tracking for replay attack prevention
     */
    @PostMapping("/validate-jws-comprehensive")
    public ResponseEntity<Map<String, Object>> validateJwsComprehensive(@RequestBody Map<String, String> request) {
        String jwsToken = request.get("jws_token");
        String originalBody = request.get("original_body"); // Required for detached format
        
        if (jwsToken == null || jwsToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "JWS token is required"));
        }
        
        try {
            boolean isValid = jwsSignatureService.validateJwsComprehensive(jwsToken, originalBody);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jws_token", jwsToken);
            response.put("comprehensive_validation", isValid);
            response.put("method", "validateJwsComprehensive");
            response.put("status", isValid ? "valid" : "invalid");
            response.put("security_note", "Includes nonce tracking for replay attack prevention");
            
            if (originalBody != null) {
                response.put("original_body", originalBody);
                response.put("format", "detached (header.signature)");
            } else {
                // Determine format from token parts
                String[] parts = jwsToken.split("\\.");
                response.put("format", parts.length == 3 ? "complete (header.payload.signature)" : "detached (header.signature)");
            }
            
            // Add security details if valid
            if (isValid) {
                try {
                    String[] parts = jwsToken.split("\\.");
                    String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), "UTF-8");
                    JsonNode headerNode = new ObjectMapper().readTree(headerJson);
                    
                    Map<String, Object> securityInfo = new HashMap<>();
                    if (headerNode.has("ts")) {
                        securityInfo.put("timestamp_validated", true);
                        securityInfo.put("timestamp", headerNode.get("ts").asLong());
                    }
                    if (headerNode.has("nonce")) {
                        securityInfo.put("nonce_validated", true);
                        securityInfo.put("nonce", headerNode.get("nonce").asText());
                        securityInfo.put("replay_protection", "active");
                    }
                    securityInfo.put("signature_validated", true);
                    
                    response.put("security_validations", securityInfo);
                } catch (Exception e) {
                    logger.warn("Could not extract security details: {}", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in comprehensive JWS validation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to validate JWS: " + e.getMessage()));
        }
    }
}