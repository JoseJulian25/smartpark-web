import {
  Calendar,
  DoorOpen,
  Grid3x3,
  History,
  LayoutDashboard,
  LogIn,
  LogOut,
  Menu,
  Settings,
  Users,
} from "lucide-react";
import { useMemo, useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "./ui/button";
import { Sheet, SheetContent, SheetTrigger } from "./ui/sheet";


const menuItems = [
  { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard, roles: ["admin", "operador"] },
  { path: "/entrada", label: "Entradas", icon: LogIn, roles: ["admin", "operador"] },
  { path: "/salida", label: "Salidas y Cobro", icon: DoorOpen, roles: ["admin", "operador"] },
  { path: "/espacios", label: "Espacios", icon: Grid3x3, roles: ["admin", "operador"] },
  { path: "/reservas", label: "Reservas", icon: Calendar, roles: ["admin", "operador"] },
  { path: "/historial", label: "Historial", icon: History, roles: ["admin", "operador"] },
  { path: "/tarifas", label: "Configuracion", icon: Settings, roles: ["admin"] },
  { path: "/usuarios", label: "Usuarios", icon: Users, roles: ["admin"] },
];

export const Layout = () => {
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const userRole = (user?.rol || user?.role || "").toLowerCase();
  const filteredMenuItems = useMemo(
    () => menuItems.filter((item) => item.roles.includes(userRole)),
    [userRole]
  );

  const MenuContent = ({ closeOnNavigate = false }) => (
    <div className="flex h-full flex-col">
      <div className="border-b p-6">
        <div className="flex items-center gap-2">
          <img src="/icon.png" alt="SmartPark" className="h-10 w-10 text-white" />
          <div>
            <h1 className="font-semibold">SmartPark</h1>
          </div>
        </div>
      </div>

      <nav className="flex-1 space-y-1 p-4">
        {filteredMenuItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              onClick={() => {
                if (closeOnNavigate) {
                  setMobileMenuOpen(false);
                }
              }}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-semibold transition-colors ${isActive ? "bg-primary text-primary-foreground" : "text-gray-700 hover:bg-slate-200/70"
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
          <p className="text-xs capitalize text-muted-foreground">{userRole || "sin rol"}</p>
        </div>

        <Button variant="outline" className="w-full" onClick={logout}>
          <LogOut className="mr-2 h-4 w-4" />
          Cerrar Sesion
        </Button>
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen bg-background">
      <aside className="hidden w-64 border-r bg-card md:flex md:flex-col">
        <MenuContent />
      </aside>

      <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
        <SheetTrigger asChild className="md:hidden">
          <Button variant="ghost" size="icon" className="fixed left-4 top-4 z-40">
            <Menu className="h-6 w-6" />
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="w-64 bg-card p-0">
          <MenuContent closeOnNavigate />
        </SheetContent>
      </Sheet>

      <main className="flex-1 overflow-auto">
        <div className="p-4 pt-20 md:p-8 md:pt-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
