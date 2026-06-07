package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.service.model.mapping.ColumnMappingDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "excel_column_mapping")
public class ExcelColumnMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long mappingId;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String excelHeader;

    private boolean required;

    private String dataType;

    private Instant createdAt;


    public ColumnMappingDto toDto() {
        return ColumnMappingDto.builder()
                .mappingId(this.getMappingId())
                .fileType(this.getFileType())
                .fieldName(this.getFieldName())
                .excelHeader(this.getExcelHeader())
                .required(this.isRequired())
                .dataType(this.getDataType())
                .createdAt(LocalDateTime.ofInstant(this.getCreatedAt(), ZoneId.of("America/Lima")))
                .build();
    }


}
