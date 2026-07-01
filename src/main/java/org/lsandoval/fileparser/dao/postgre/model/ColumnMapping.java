package org.lsandoval.fileparser.dao.postgre.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.lsandoval.fileparser.service.model.mapping.ColumnMappingDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "column_mapping")
public class ColumnMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long mappingId;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String fieldName;

    @Column(name = "source_field", nullable = false)
    private String sourceField;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    private boolean required;

    private String dataType;

    private Instant createdAt;


    public ColumnMappingDto toDto() {
        return ColumnMappingDto.builder()
                .mappingId(this.getMappingId())
                .fileType(this.getFileType())
                .fieldName(this.getFieldName())
                .excelHeader(this.getSourceField())
                .required(this.isRequired())
                .dataType(this.getDataType())
                .createdAt(LocalDateTime.ofInstant(this.getCreatedAt(), ZoneId.of("America/Lima")))
                .build();
    }
}
