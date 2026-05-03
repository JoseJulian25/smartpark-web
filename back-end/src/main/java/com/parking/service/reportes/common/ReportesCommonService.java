package com.parking.service.reportes.common;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class ReportesCommonService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Clock appClock;

    public ReportesCommonService(Clock appClock) {
        this.appClock = appClock;
    }

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

    public RangoFechas resolverRango(OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, int maxRangeDias) {
        LocalDateTime desdeLocal = toBusinessLocalDateTime(fechaDesde);
        LocalDateTime hastaLocal = toBusinessLocalDateTime(fechaHasta);

        RangoFechas rango;
        if (desdeLocal != null && hastaLocal != null) {
            if (hastaLocal.isBefore(desdeLocal)) {
                throw new IllegalArgumentException("fechaHasta no puede ser menor que fechaDesde");
            }
            rango = new RangoFechas(desdeLocal, hastaLocal);
            return rango;
        }

        if (desdeLocal != null) {
            rango = new RangoFechas(desdeLocal, desdeLocal.plusDays(1));
            return rango;
        }

        if (hastaLocal != null) {
            rango = new RangoFechas(hastaLocal.minusDays(1), hastaLocal);
            return rango;
        }

        LocalDate today = LocalDate.now(appClock);
        rango = new RangoFechas(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return rango;
    }

    private LocalDateTime toBusinessLocalDateTime(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZoneSameInstant(appClock.getZone()).toLocalDateTime();
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

    public record RangoFechas(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
    }
}
