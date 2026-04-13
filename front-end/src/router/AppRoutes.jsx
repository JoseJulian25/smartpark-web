import { Navigate, Route, Routes } from "react-router-dom";
import { Layout } from "../components/Layout";
import { DashboardPage } from "../pages/DashboardPage";
import { EntradaPage } from "../pages/EntradaPage";
import  {LoginPage}  from "../pages/LoginPage.jsx";
import { ReservasPage } from "../pages/ReservasPage";
import { ConfiguracionPage } from "../pages/ConfiguracionPage";
import { UsuariosPage } from "../pages/UsuariosPage";
import { ReportesOperativosPage } from "../pages/reportes/ReportesOperativosPage";
import { ReportesReservasPage } from "../pages/reportes/ReportesReservasPage";
import { ReportesOcupacionPage } from "../pages/reportes/ReportesOcupacionPage";
import { ReportesConsultasPage } from "../pages/reportes/ReportesConsultasPage";
import { ReportesFinancierosPage } from "../pages/reportes/ReportesFinancierosPage";
import { ProtectedRoute } from "./ProtectedRoute";

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute/>}>
        <Route element={<Layout />}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/entrada" element={<EntradaPage />} />
        <Route path="/salida" element={<Navigate to="/entrada" replace />} />
        <Route path="/espacios" element={<Navigate to="/entrada" replace />} />
        <Route path="/reservas" element={<ReservasPage />} />
        <Route path="/historial" element={<Navigate to="/entrada" replace />} />
        <Route path="/configuracion" element={<Navigate to="/configuracion/empresa" replace />} />
        <Route path="/configuracion/empresa" element={<ConfiguracionPage initialTab="general" />} />
        <Route path="/configuracion/tarifas" element={<ConfiguracionPage initialTab="tarifas" />} />
        <Route path="/configuracion/espacios" element={<ConfiguracionPage initialTab="espacios" />} />
        <Route path="/tarifas" element={<Navigate to="/configuracion/tarifas" replace />} />
        <Route path="/usuarios" element={<UsuariosPage />} />
        <Route path="/reportes" element={<Navigate to="/reportes/operativos" replace />} />
        <Route path="/reportes/operativos" element={<ReportesOperativosPage />} />
        <Route path="/reportes/reservas" element={<ReportesReservasPage />} />
        <Route path="/reportes/ocupacion" element={<ReportesOcupacionPage />} />
        <Route path="/reportes/financieros" element={<ReportesFinancierosPage />} />
        <Route path="/reportes/consultas" element={<ReportesConsultasPage />} />
      </Route>
    </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};
