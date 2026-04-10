import { useEffect, useState } from "react";

import {
  getReservasPendientes,
  getReservasActivas,
  getHistorialReservas,
  confirmarLlegada,
  registrarSalida,
  cancelarReserva
} from "../../api/reservas";

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle
} from "../ui/card";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from "../ui/table";

import { Button } from "../ui/button";

export default function ListaReservas() {

  const [reservas, setReservas] = useState([]);
  const [filtro, setFiltro] = useState("pendientes");
  const [loading, setLoading] = useState(false);

  const fetchReservas = async () => {
    try {

      setLoading(true);

      let data = [];

      if (filtro === "pendientes") {
        data = await getReservasPendientes();
      }

      if (filtro === "activas") {
        data = await getReservasActivas();
      }

      if (filtro === "historial") {
        data = await getHistorialReservas();
      }

      console.log("Reservas:", data);

      setReservas(Array.isArray(data) ? data : []);

    } catch (error) {
      console.error("Error cargando reservas:", error);
      setReservas([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservas();
  }, [filtro]);

  const handleConfirmar = async (id) => {
    try {
      await confirmarLlegada(id);
      fetchReservas();
    } catch (error) {
      console.error(error);
    }
  };

  const handleSalida = async (codigo) => {
    try {
      await registrarSalida(codigo);
      fetchReservas();
    } catch (error) {
      console.error(error);
    }
  };

  const handleCancelar = async (id) => {
    try {
      await cancelarReserva(id);
      fetchReservas();
    } catch (error) {
      console.error(error);
    }
  };

  return (

    <Card>

      <CardHeader className="flex flex-row justify-between">

        <CardTitle>
          Lista de Reservas
        </CardTitle>

        <div className="space-x-2">

          <Button
            size="sm"
            onClick={() => setFiltro("pendientes")}
          >
            Pendientes
          </Button>

          <Button
            size="sm"
            onClick={() => setFiltro("activas")}
          >
            Activas
          </Button>

          <Button
            size="sm"
            onClick={() => setFiltro("historial")}
          >
            Historial
          </Button>

        </div>

      </CardHeader>

      <CardContent>

        <Table>

          <TableHeader>

            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Placa</TableHead>
              <TableHead>Espacio</TableHead>
              <TableHead>Hora</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead>Acciones</TableHead>
            </TableRow>

          </TableHeader>

          <TableBody>

            {loading && (
              <TableRow>
                <TableCell colSpan={7}>
                  Cargando...
                </TableCell>
              </TableRow>
            )}

            {!loading && reservas.length === 0 && (
              <TableRow>
                <TableCell colSpan={7}>
                  No hay reservas
                </TableCell>
              </TableRow>
            )}

            {reservas.map((reserva) => (

              <TableRow key={reserva.id}>

                <TableCell>
                  {reserva.id}
                </TableCell>

                <TableCell>
                  {reserva.clienteNombreCompleto}
                </TableCell>

                <TableCell>
                  {reserva.placa}
                </TableCell>

                <TableCell>
                  {reserva.codigoEspacio || reserva.espacioId}
                </TableCell>

                <TableCell>
                  {reserva.horaInicio} - {reserva.horaFin}
                </TableCell>

                <TableCell>
                  {reserva.estado}
                </TableCell>

                <TableCell className="space-x-2">

                  {filtro === "pendientes" && (
                    <Button
                      size="sm"
                      onClick={() => handleConfirmar(reserva.id)}
                    >
                      Confirmar
                    </Button>
                  )}

                  {filtro === "activas" && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleSalida(reserva.codigoReserva)}
                    >
                      Salida
                    </Button>
                  )}

                  {filtro === "pendientes" && (
                    <Button
                      size="sm"
                      variant="destructive"
                      onClick={() => handleCancelar(reserva.id)}
                    >
                      Cancelar
                    </Button>
                  )}

                </TableCell>

              </TableRow>

            ))}

          </TableBody>

        </Table>

      </CardContent>

    </Card>

  );
}