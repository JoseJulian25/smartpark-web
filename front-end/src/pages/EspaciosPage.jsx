import { useState, useEffect } from "react";
import { getEspacios } from "../api/espacios";

import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";

import { Car, Bike } from "lucide-react";

export function EspaciosPage() {
  const [espacios, setEspacios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchEspacios();
  }, []);

  const fetchEspacios = async () => {
    try {
      setLoading(true);
      const data = await getEspacios();
      setEspacios(data);
    } catch (err) {
      setError("Error cargando espacios");
    } finally {
      setLoading(false);
    }
  };

  // 📊 Derivados
  const espaciosCarros = espacios.filter(
    (e) => e.tipoVehiculo === "CARRO"
  );

  const espaciosMotos = espacios.filter(
    (e) => e.tipoVehiculo === "MOTO"
  );

  const stats = {
    libre: espacios.filter((e) => e.estado === "LIBRE").length,
    ocupado: espacios.filter((e) => e.estado === "OCUPADO").length,
    reservado: espacios.filter((e) => e.estado === "RESERVADO").length,
  };

  // 🎨 Colores
  const getStatusColor = (estado) => {
    switch (estado) {
      case "LIBRE":
        return "bg-green-100 border-green-300";
      case "OCUPADO":
        return "bg-red-100 border-red-300";
      case "RESERVADO":
        return "bg-yellow-100 border-yellow-300";
      default:
        return "bg-gray-100 border-gray-300";
    }
  };

  const getStatusBadgeVariant = (estado) => {
    switch (estado) {
      case "LIBRE":
        return "secondary";
      case "OCUPADO":
        return "destructive";
      case "RESERVADO":
        return "outline";
      default:
        return "secondary";
    }
  };

  // 🧱 Render grid
  const renderEspacios = (lista) => {
    if (!lista.length) return <p>No hay espacios disponibles</p>;

    return (
      <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-3">
        {lista.map((space) => {
          const ticket = space.ticketActivo;

          return (
            <div
              key={space.id}
              className={`relative p-4 rounded-lg border-2 transition-all ${getStatusColor(
                space.estado
              )}`}
            >
              <div className="text-center">
                <p className="text-2xl font-bold mb-1">
                  {space.numero}
                </p>

                <Badge variant={getStatusBadgeVariant(space.estado)}>
                  {space.estado}
                </Badge>

                {ticket && (
                  <div className="mt-2 pt-2 border-t border-current/20">
                    <p className="text-xs font-medium truncate">
                      {ticket.placa}
                    </p>
                    <p className="text-xs opacity-75">
                      {ticket.horaEntrada}
                    </p>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  // ⏳ Estados UI
  if (loading) return <p>Cargando espacios...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold mb-2">
          Gestión de Espacios
        </h1>
        <p className="text-muted-foreground">
          Vista en tiempo real del estado del parqueo
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
          <p className="text-sm">Libres</p>
          <p className="text-2xl font-bold text-green-700">
            {stats.libre}
          </p>
        </div>

        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm">Ocupados</p>
          <p className="text-2xl font-bold text-red-700">
            {stats.ocupado}
          </p>
        </div>

        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p className="text-sm">Reservados</p>
          <p className="text-2xl font-bold text-yellow-700">
            {stats.reservado}
          </p>
        </div>
      </div>

      {/* Tabs */}
      <Card>
        <CardHeader>
          <CardTitle>Espacios del Parqueo</CardTitle>
        </CardHeader>

        <CardContent>
          <Tabs defaultValue="carros" className="space-y-4">
            <TabsList className="grid w-full max-w-md grid-cols-2">
              <TabsTrigger value="carros">
                <Car className="w-4 h-4 mr-2" />
                Carros ({espaciosCarros.length})
              </TabsTrigger>

              <TabsTrigger value="motos">
                <Bike className="w-4 h-4 mr-2" />
                Motos ({espaciosMotos.length})
              </TabsTrigger>
            </TabsList>

            <TabsContent value="carros">
              {renderEspacios(espaciosCarros)}
            </TabsContent>

            <TabsContent value="motos">
              {renderEspacios(espaciosMotos)}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Leyenda */}
      <Card>
        <CardHeader>
          <CardTitle>Leyenda</CardTitle>
        </CardHeader>

        <CardContent>
          <div className="flex gap-4 flex-wrap">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-green-100 border border-green-300 rounded"></div>
              <span>Libre</span>
            </div>

            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-red-100 border border-red-300 rounded"></div>
              <span>Ocupado</span>
            </div>

            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-yellow-100 border border-yellow-300 rounded"></div>
              <span>Reservado</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}