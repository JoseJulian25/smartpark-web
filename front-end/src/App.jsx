
import './App.css'

import { useEffect } from 'react'
import client from "./api/client";

function App() {
  useEffect(() => {
  const testApi = async () => {
      try {
        const response = await client.get("/test/hello"); 
        console.log(" Respuesta del backend:", response.data);
      } catch (error) {
        console.error(" Error al conectar con la API:", error);
      }
    };
    testApi();
 }, []);
  
  return (
    <div className="app-root">
      <h1>Parking System</h1>
        <p>Test fetch result: {'Cargando...'}</p>        
    </div>
    
  )
}

export default App
