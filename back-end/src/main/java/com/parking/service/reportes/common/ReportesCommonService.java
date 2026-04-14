package com.parking.service.reportes.common;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class ReportesCommonService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    public String normalizarTexto(String value) {
        return value == null ? "" : value.trim();
    }

    public String normalizarMetodoPago(String metodoPago) {
        String value = normalizarTexto(metodoPago).toUpperCase(Locale.ROOT);
        return value.isBlank() ? "SIN_METODO" : value;
    }

    public String normalizarMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            return "SIN_MOTIVO_REGISTRADO";
        }
        return motivo.trim();
    }

    public RangoFechas resolverRango(LocalDateTime fechaDesde, LocalDateTime fechaHasta, int maxRangeDias) {
        RangoFechas rango;
        if (fechaDesde != null && fechaHasta != null) {
            if (fechaHasta.isBefore(fechaDesde)) {
                throw new IllegalArgumentException("fechaHasta no puede ser menor que fechaDesde");
            }
            rango = new RangoFechas(fechaDesde, fechaHasta);
            validarRangoMaximo(rango, maxRangeDias);
            return rango;
        }

        if (fechaDesde != null) {
            rango = new RangoFechas(fechaDesde, fechaDesde.plusDays(1));
            validarRangoMaximo(rango, maxRangeDias);
            return rango;
        }

        if (fechaHasta != null) {
            rango = new RangoFechas(fechaHasta.minusDays(1), fechaHasta);
            validarRangoMaximo(rango, maxRangeDias);
            return rango;
        }

        LocalDate today = LocalDate.now();
        rango = new RangoFechas(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        validarRangoMaximo(rango, maxRangeDias);
        return rango;
    }

    public <T> List<T> paginarFilas(List<T> filas, Integer page, Integer size, int defaultPage, int defaultSize, int maxSize) {
        if (filas == null || filas.isEmpty()) {
            return List.of();
        }

        int pageValue = page == null ? defaultPage : page;
        int sizeValue = size == null ? defaultSize : size;

        if (pageValue < 0) {
            throw new IllegalArgumentException("page no puede ser negativo");
        }
        if (sizeValue < 1 || sizeValue > maxSize) {
            throw new IllegalArgumentException("size debe estar entre 1 y " + maxSize);
        }

        int fromIndex = pageValue * sizeValue;
        if (fromIndex >= filas.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + sizeValue, filas.size());
        return filas.subList(fromIndex, toIndex);
    }

    private void validarRangoMaximo(RangoFechas rango, int maxRangeDias) {
        long dias = Duration.between(rango.fechaDesde(), rango.fechaHasta()).toDays();
        if (dias > maxRangeDias) {
            throw new IllegalArgumentException("El rango maximo permitido es de " + maxRangeDias + " dias");
        }
    }

    public record RangoFechas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
    }
}
