package org.lsandoval.fileparser.service.model.job;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileJobCreateRequest {

    private final String fileName;
    private final String fileExtension;
    private final FileType fileType;


}
