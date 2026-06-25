package org.creati.sicloReservationsApi.service.model.parser;

/**
 * @param fileType  logical type key used to load column mappings (e.g. "RESERVATION", "PAYMENT").
 * @param extension lowercase file extension (e.g. "xlsx", "csv", "json", "xml"). Used both to load
 *                  the per-format column mappings and to resolve the format-specific record locator.
 */
public record ParseRequest(String fileType, String extension) {
}
