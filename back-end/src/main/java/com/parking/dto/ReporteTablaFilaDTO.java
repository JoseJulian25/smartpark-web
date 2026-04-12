package com.parking.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTablaFilaDTO {

    private Map<String, String> columnas = new LinkedHashMap<>();
}
