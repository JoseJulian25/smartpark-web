package com.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.entity.ReporteAuditoria;

@Repository
public interface ReporteAuditoriaRepository extends JpaRepository<ReporteAuditoria, Long> {
}
