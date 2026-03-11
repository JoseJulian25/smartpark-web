import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";


const LoginPage = () => <h1>Login</h1>;
const DashboardPage = () => <h1>Dashboard</h1>;
const EntradaPage = () => <h1>Entrada de Vehículos</h1>;
const SalidaPage = () => <h1>Salida de Vehículos</h1>;
const EspaciosPage = () => <h1>Gestión de Espacios</h1>;
const ReservasPage = () => <h1>Reservas</h1>;
const HistorialPage = () => <h1>Historial</h1>;
const TarifasPage = () => <h1>Tarifas</h1>;
const UsuariosPage = () => <h1>Usuarios</h1>;

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>

        <Routes>


          <Route path="/login" element={<LoginPage />} />


          <Route path="/" element={<Navigate to="/dashboard" />} />

          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/entrada" element={<EntradaPage />} />
          <Route path="/salida" element={<SalidaPage />} />
          <Route path="/espacios" element={<EspaciosPage />} />
          <Route path="/reservas" element={<ReservasPage />} />
          <Route path="/historial" element={<HistorialPage />} />
          <Route path="/tarifas" element={<TarifasPage />} />
          <Route path="/usuarios" element={<UsuariosPage />} />

          <Route path="*" element={<Navigate to="/dashboard" />} />

        </Routes>

      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;