package org.creati.sicloReservationsApi.dao.spec;

import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

public class ReservationSpecifications {

    public static final Set<String> allowedFilters = Set.of("client", "instructor", "discipline");

    public static Specification<Reservation> dateBetween(LocalDate from, LocalDate to) {
        return ((root, query, cb) ->
                cb.between(root.get("reservationDate"), from, to));
    }

    public static Specification<Reservation> clientLike(String value) {
        return (((root, query, cb) -> {
            var client = root.join("client");
            String val = "%" + value.toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(client.get("name")), val),
                    cb.like(cb.lower(client.get("email")), val)
            );
        }));
    }

    public static Specification<Reservation> instructorLike(String value) {
        return (root, query, cb) -> {
            var instructor = root.join("instructor");
            String val = "%" + value.toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(instructor.get("name")), val);
        };
    }

    public static Specification<Reservation> disciplineLike(String value) {
        return (root, query, cb) -> {
            var discipline = root.join("discipline");
            String val = "%" + value.toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(discipline.get("name")), val);
        };
    }

}
