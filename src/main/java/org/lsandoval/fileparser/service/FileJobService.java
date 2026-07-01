package org.lsandoval.fileparser.service;

import org.lsandoval.fileparser.dao.postgre.model.FileJob;
import org.lsandoval.fileparser.service.model.job.FileJobCreateRequest;
import org.lsandoval.fileparser.service.model.job.FileJobDto;
import org.lsandoval.fileparser.service.model.job.FileJobUpdateRequest;
import org.lsandoval.fileparser.service.model.common.PagedResponse;

import java.time.LocalDate;
import java.util.Optional;

public interface FileJobService {

    FileJob createFileJob(FileJobCreateRequest createRequest);

    void updateStatus(Long jobId, FileJobUpdateRequest updateRequest, FileJob existingJob);

    Optional<FileJob> getFileJobById(Long jobId);

    PagedResponse<FileJobDto> getFileJobs(LocalDate from, LocalDate to, int page, int size);

}
