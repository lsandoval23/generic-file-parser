package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.service.model.job.FileJobCreateRequest;
import org.creati.sicloReservationsApi.service.model.job.FileJobDto;
import org.creati.sicloReservationsApi.service.model.job.FileJobUpdateRequest;
import org.creati.sicloReservationsApi.service.model.reports.PagedResponse;

import java.time.LocalDate;
import java.util.Optional;

public interface FileJobService {

    FileJob createFileJob(FileJobCreateRequest createRequest);

    void updateStatus(Long jobId, FileJobUpdateRequest updateRequest, FileJob existingJob);

    Optional<FileJob> getFileJobById(Long jobId);

    PagedResponse<FileJobDto> getFileJobs(LocalDate from, LocalDate to, int page, int size);

}
