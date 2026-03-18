import { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Car} from 'lucide-react';


export const LoginPage = () => {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);



  useEffect(() => {
    if (isAuthenticated) {
  navigate("/dashboard");
}
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!username || !password) {
      toast.error("Todos los campos son obligatorios");
      return;
    }

    setLoading(true);

   const result = await login(username, password);

    setLoading(false);

    if (result.success) {
      toast.success("¡Bienvenido!");
      navigate("/dashboard");
    } else {
      toast.error(result.message);
}
  };

  return (
     <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
          <Card className="w-full max-w-md">
            <CardHeader className="space-y-1 text-center">
              <div className="flex justify-center mb-4">
                <div className="p-3 bg-blue-600 rounded-full">
                  <Car className="w-8 h-8 text-white" />
                </div>
              </div>
              <CardTitle className="text-2xl">Sistema de Gestión de Parqueo</CardTitle>
              <CardDescription>
                Ingrese sus credenciales para acceder al sistema
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="username">Usuario</Label>
                  <Input
                    id="username"
                    type="text"
                    placeholder="Ingrese su usuario"
                    value={username}
                    
                    onChange={(e) => setUsername(e.target.value)}
                    autoComplete="username"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">Contraseña</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="Ingrese su contraseña"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="current-password"
                  />
                </div>
    
    
                <Button type="submit" className="w-full"
                  disabled={loading}>
        {loading ? "Cargando..." : "Ingresar"}
                </Button>
              </form>
    
              <div className="mt-6 pt-6 border-t">
                <p className="text-sm text-muted-foreground text-center mb-2">
                  Credenciales de prueba:
                </p>
                <div className="space-y-1 text-xs text-muted-foreground">
                  <p>
                    <strong>Admin:</strong> admin / admin123
                  </p>
                  <p>
                    <strong>Operador:</strong> operador / operador123
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
  );
}
