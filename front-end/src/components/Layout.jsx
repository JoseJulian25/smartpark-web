import {
  Building2,
  Calendar,
  ChevronDown,
  DollarSign,
  History,
  LayoutDashboard,
  LogIn,
  LogOut,
  Menu,
  Settings,
  Users,
} from "lucide-react";
import { useMemo, useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "./ui/button";
import { Sheet, SheetContent, SheetTrigger } from "./ui/sheet";


const menuItems = [
  { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard, roles: ["admin", "operador"] },
  { path: "/entrada", label: "Entradas", icon: LogIn, roles: ["admin", "operador"] },
  { path: "/reservas", label: "Reservas", icon: Calendar, roles: ["admin", "operador"] },
  { path: "/historial", label: "Historial", icon: History, roles: ["admin", "operador"] },
  {
    path: "/configuracion",
    label: "Configuracion",
    icon: Settings,
    roles: ["admin"],
    children: [
      { path: "/configuracion/empresa", label: "Informacion de la empresa", icon: Building2 },
      { path: "/configuracion/tarifas", label: "Tarifas", icon: DollarSign }
    ]
  },
  { path: "/usuarios", label: "Usuarios", icon: Users, roles: ["admin"] },
];

export const Layout = () => {
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [configOpen, setConfigOpen] = useState(true);
  const location = useLocation();

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
          const hasChildren = Array.isArray(item.children) && item.children.length > 0;
          const isConfigSectionActive = hasChildren
            ? item.children.some((child) => location.pathname === child.path)
            : false;

          if (hasChildren) {
            return (
              <div key={item.path} className="space-y-1">
                <button
                  type="button"
                  onClick={() => setConfigOpen((prev) => !prev)}
                  className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 text-sm font-semibold transition-colors ${
                    isConfigSectionActive ? "bg-primary text-primary-foreground" : "text-gray-700 hover:bg-slate-200/70"
                  }`}
                >
                  <Icon className="h-5 w-5" />
                  <span className="flex-1 text-left">{item.label}</span>
                  <ChevronDown className={`h-4 w-4 transition-transform ${configOpen ? "rotate-180" : ""}`} />
                </button>

                {configOpen && (
                  <div className="ml-4 space-y-1 border-l border-slate-200 pl-3">
                    {item.children.map((child) => {
                      const ChildIcon = child.icon;
                      return (
                        <NavLink
                          key={child.path}
                          to={child.path}
                          onClick={() => {
                            if (closeOnNavigate) {
                              setMobileMenuOpen(false);
                            }
                          }}
                          className={({ isActive }) =>
                            `flex items-center gap-2 rounded-lg px-3 py-2 text-xs font-semibold transition-colors ${
                              isActive
                                ? "bg-primary/10 text-primary"
                                : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                            }`
                          }
                        >
                          <ChildIcon className="h-4 w-4" />
                          <span>{child.label}</span>
                        </NavLink>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          }

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
