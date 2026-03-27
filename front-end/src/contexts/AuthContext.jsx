import { createContext, useContext, useEffect, useState } from "react";
import client from "../api/client";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  const login = async (username, password) => {
    try {
      const response = await client.post("/auth/login", { username, password });
      const data = response.data;

      localStorage.setItem("authToken", data.token);

      setUser({
        username: data.username,
        nombre: data.nombre,
        rol: data.rol,
      });
      setIsAuthenticated(true);

      return { success: true };
    } catch (error) {
      return {
        success: false,
        message: typeof error === "string"
          ? error
          : "Usuario o contraseña inválidos",
      };
    }
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    setUser(null);
    setIsAuthenticated(false);
  };

  const checkAuth = async () => {
    const token = localStorage.getItem("authToken");

    if (!token) {
      setLoading(false);
      return;
    }

    try {
      const response = await client.get("/auth/me");
      setUser(response.data);
      setIsAuthenticated(true);
    } catch {
      // Token inválido o expirado — limpiar sesión
      localStorage.removeItem("authToken");
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkAuth();
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
export const useAuth = () => useContext(AuthContext);

