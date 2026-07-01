package org.lsandoval.fileparser.service;

import org.lsandoval.fileparser.service.model.job.FileType;

import java.io.File;

public interface FileProcessingService {
    void processFile(File fileData, Long jobId, FileType fileType);
}
