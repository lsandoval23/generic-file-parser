package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "discipline")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discipline_id")
    private Long disciplineId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

}
