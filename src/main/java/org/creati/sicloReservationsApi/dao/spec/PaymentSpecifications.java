package org.creati.sicloReservationsApi.dao.spec;

import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

public class PaymentSpecifications {

    public static final Set<String> allowedFilters = Set.of("client", "payment");

    public static Specification<PaymentTransaction> clientLike(String value) {
        return (root, query, cb) -> {
            var client = root.join("client");
            String val = "%" + value.toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(client.get("name")), val),
                    cb.like(cb.lower(client.get("email")), val)
            );
        };
    }

    public static Specification<PaymentTransaction> paymentMethodLike(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("paymentMethod")), "%" + value.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<PaymentTransaction> purchaseDateBetween(
            LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> cb.between(root.get("purchaseDate"), from, to);
    }
}
