package com.parking.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.parking.service.ReporteAuditoriaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ReportesAuditoriaInterceptor implements HandlerInterceptor {

    private final ReporteAuditoriaService reporteAuditoriaService;

    public ReportesAuditoriaInterceptor(ReporteAuditoriaService reporteAuditoriaService) {
        this.reporteAuditoriaService = reporteAuditoriaService;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String endpoint = request.getRequestURI();
        if (endpoint == null || !endpoint.startsWith("/reportes")) {
            return;
        }

        String usuario = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
        String parametros = request.getQueryString();
        int statusCode = response.getStatus();

        reporteAuditoriaService.registrar(usuario, endpoint, request.getMethod(), parametros, statusCode);
    }
}
