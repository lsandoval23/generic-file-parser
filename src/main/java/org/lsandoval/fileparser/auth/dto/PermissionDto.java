package org.lsandoval.fileparser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lsandoval.fileparser.auth.model.Permission;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PermissionDto {

    private Long id;
    private String name;
    private String description;
    private String resource;
    private String action;

    public PermissionDto(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.description = permission.getDescription();
        this.resource = permission.getResource();
        this.action = permission.getAction();
    }
}
