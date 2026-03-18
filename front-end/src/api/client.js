import axios from "axios";


const client = axios.create({
  baseURL: "http://localhost:8080", 
  timeout: 10000, 
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

client.interceptors.response.use(
  (response) => response, 
  (error) => {
    if (error.response) {

      if (error.response.status === 401) {

        localStorage.removeItem("authToken");
        
        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
      }

      return Promise.reject(
        error.response.data?.message ||
          "Ocurrió un error al procesar la solicitud."
      );
    } else if (error.request) {

      return Promise.reject("No se pudo conectar con el servidor.");
    } else {

      return Promise.reject("Ocurrió un error inesperado.");
    }
  }
);

export default client;