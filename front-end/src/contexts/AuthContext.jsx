import { createContext, useContext, useEffect, useState } from "react";
import client from "../api/client";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const[isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  const login = async (username, password) => {
  try {

    const response = await client.post("/auth/login", {
      username: username,
      password: password
    });

    const { token, user } = response.data;

    localStorage.setItem("authToken", token);

    setUser(user);
    setIsAuthenticated(true);

    return true;

  } catch (error) {
    console.error("Error en login:", error);
    return false;
  }
};

const logout = async () => {

  try {
    await client.post("/auth/logout");
  } catch (error) {
    console.warn("Error cerrando sesión:", error);
  }

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

  } catch (error) {

    localStorage.removeItem("authToken");

  } finally {

    setLoading(false);

  }

};

useEffect(() => {
  checkAuth();
}, []);

const value = {
  user,
  isAuthenticated,
  loading,
  login,
  logout
};
return (
  <AuthContext.Provider value={value}>
    {children}
  </AuthContext.Provider>
);

};



export const useAuth = () => {
  return useContext(AuthContext);
};