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
      console.log("Respuesta login:", response.data);

      localStorage.setItem("authToken", data.token);

      const userData = {
        username: data.username,
        nombre: data.nombre,
        rol: data.rol,
      };

      setUser(userData);
      setIsAuthenticated(true);

      return true;
    } catch (error) {
      console.error("Error en login:", error);
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    setUser(null);
    setIsAuthenticated(false);
  };

  const checkAuth = () => {
    const token = localStorage.getItem("authToken");
    if (token) setIsAuthenticated(true);
    setLoading(false);
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