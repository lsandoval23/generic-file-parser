package org.creati.sicloReservationsApi.service.model.parser;

/**
 * @param fileType       logical type key used to load column mappings (e.g. "RESERVATION", "PAYMENT").
 * @param sourceHint     format-specific hint: sheet name for Excel, JSON pointer to the record array,
 *                       or repeating element name for XML. Null means "use default" (first sheet /
 *                       top-level array / infer from structure).
 */
public record ParseRequest(String fileType, String sourceHint) {
}
