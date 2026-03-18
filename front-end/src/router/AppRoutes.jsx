import { Navigate, Route, Routes } from "react-router-dom";
import { Layout } from "../components/Layout";
import { DashboardPage } from "../pages/DashboardPage";
import { EntradaPage } from "../pages/EntradaPage";
import { EspaciosPage } from "../pages/EspaciosPage";
import { HistorialPage } from "../pages/HistorialPage";
import  {LoginPage}  from "../pages/LoginPage.jsx";
import { ReservasPage } from "../pages/ReservasPage";
import { SalidaPage } from "../pages/SalidaPage";
import { TarifasPage } from "../pages/TarifasPage";
import { UsuariosPage } from "../pages/UsuariosPage";
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
        <Route path="/salida" element={<SalidaPage />} />
        <Route path="/espacios" element={<EspaciosPage />} />
        <Route path="/reservas" element={<ReservasPage />} />
        <Route path="/historial" element={<HistorialPage />} />
        <Route path="/tarifas" element={<TarifasPage />} />
        <Route path="/usuarios" element={<UsuariosPage />} />
      </Route>
    </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};
