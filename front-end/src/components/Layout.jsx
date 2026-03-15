import {
  Calendar,
  Car,
  DoorOpen,
  Grid3x3,
  History,
  LayoutDashboard,
  LogIn,
  LogOut,
  Settings,
  Users,
} from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "./ui/button";

const menuItems = [
  { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { path: "/entrada", label: "Entradas", icon: LogIn },
  { path: "/salida", label: "Salidas y Cobro", icon: DoorOpen },
  { path: "/espacios", label: "Espacios", icon: Grid3x3 },
  { path: "/reservas", label: "Reservas", icon: Calendar },
  { path: "/historial", label: "Historial", icon: History },
  { path: "/tarifas", label: "Tarifas", icon: Settings },
  { path: "/usuarios", label: "Usuarios", icon: Users },
];

export const Layout = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50 md:flex">
      <aside className="w-full border-b bg-white md:min-h-screen md:w-64 md:border-b-0 md:border-r md:flex md:flex-col">
        <div className="border-b p-6">
          <div className="flex items-center gap-2">
            <div className="rounded-lg bg-blue-600 p-2">
              <Car className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="font-semibold">Parqueo</h1>
              <p className="text-xs text-muted-foreground">Sistema de Gestion</p>
            </div>
          </div>
        </div>

        <nav className="grid grid-cols-2 gap-1 p-4 sm:grid-cols-3 md:flex-1 md:grid-cols-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-lg px-4 py-3 text-sm transition-colors ${
                    isActive
                      ? "bg-blue-600 text-white"
                      : "text-gray-700 hover:bg-gray-100"
                  }`
                }
              >
                <Icon className="h-5 w-5" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        <div className="border-t p-4">
          <div className="mb-3 px-1">
            <p className="text-sm font-medium">{user?.nombre || "Usuario"}</p>
            <p className="text-xs capitalize text-muted-foreground">
              {user?.rol || user?.role || "sin rol"}
            </p>
          </div>

          <Button variant="outline" className="w-full" onClick={logout}>
            <LogOut className="mr-2 h-4 w-4" />
            Cerrar Sesion
          </Button>
        </div>
      </aside>

      <main className="flex-1 overflow-auto p-4 md:p-8">
        <Outlet />
      </main>
    </div>
  );
};
