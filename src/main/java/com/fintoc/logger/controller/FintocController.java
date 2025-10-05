package com.fintoc.logger.controller;

import com.fintoc.logger.entity.AccountValidationResponse;
import com.fintoc.logger.service.FintocApiService;
import com.fintoc.logger.service.JwsSignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fintoc")
public class FintocController {

    private static final Logger logger = LoggerFactory.getLogger(FintocController.class);
    private final FintocApiService fintocApiService;
    private final JwsSignatureService jwsSignatureService;

    @Autowired
    public FintocController(FintocApiService fintocApiService, JwsSignatureService jwsSignatureService) {
        this.fintocApiService = fintocApiService;
        this.jwsSignatureService = jwsSignatureService;
    }

    @PostMapping("/accounts/{accountId}/validate")
    public ResponseEntity<AccountValidationResponse> validateAccount(
            @PathVariable String accountId,
            HttpServletRequest request) {
        
        return fintocApiService.validateAccount(accountId);
    }
}