package org.creati.sicloReservationsApi.web;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.postgre.PaymentTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@PreAuthorize("hasAuthority('REPORT_VIEW')")
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentController(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @GetMapping("/methods/values")
    public ResponseEntity<List<String>> getPaymentMethodsValues() {
        return ResponseEntity.ok(paymentTransactionRepository.findDistinctPaymentMethods());
    }

}
