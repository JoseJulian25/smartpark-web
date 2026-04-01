import { useState } from "react";

import {
  buscarReservaPorCodigo,
  registrarSalida
} from "../../api/reservas";

import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Alert, AlertDescription } from "../ui/alert";
import { Badge } from "../ui/badge";

export default function RegistrarSalida({ onSuccess }) {

  const [codigo, setCodigo] = useState("");
  const [reserva, setReserva] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");


  const handleBuscar = async () => {

    setError("");
    setSuccess("");
    setReserva(null);

    if (!codigo) {
      setError("Ingrese código de reserva");
      return;
    }

    try {

      setLoading(true);

      const data = await buscarReservaPorCodigo(codigo);

      setReserva(data);

    } catch (err) {
      console.error(err);
      setError("Reserva no encontrada");
    } finally {
      setLoading(false);
    }

  };


  const handleSalida = async () => {

    try {

      setLoading(true);

      await registrarSalida(codigo);

      setSuccess("Salida registrada correctamente");
      setReserva(null);
      setCodigo("");

      if (onSuccess) {
        onSuccess();
      }

    } catch (err) {
      console.error(err);
      setError("Error registrando salida");
    } finally {
      setLoading(false);
    }

  };


  return (

    <div className="bg-white p-6 rounded-lg shadow">

      <h2 className="text-lg font-semibold mb-4">
        Registrar Salida
      </h2>

      <div className="flex gap-2 mb-4">

        <Input
          placeholder="Código Reserva"
          value={codigo}
          onChange={(e) => setCodigo(e.target.value)}
        />

        <Button
          onClick={handleBuscar}
          disabled={loading}
        >
          Buscar
        </Button>

      </div>


      {error && (
        <Alert variant="destructive">
          <AlertDescription>
            {error}
          </AlertDescription>
        </Alert>
      )}


      {success && (
        <Alert>
          <AlertDescription>
            {success}
          </AlertDescription>
        </Alert>
      )}


      {reserva && (

        <div className="border rounded-lg p-4 space-y-3">

          <div>
            <strong>Placa:</strong> {reserva.placa}
          </div>

          <div>
            <strong>Tipo:</strong> 
            <Badge className="ml-2">
              {reserva.tipoVehiculo}
            </Badge>
          </div>

          <div>
            <strong>Fecha:</strong> {reserva.fechaReserva}
          </div>

          <div>
            <strong>Hora:</strong> {reserva.horaReserva}
          </div>

          <Button
            className="w-full"
            onClick={handleSalida}
            disabled={loading}
          >
            Registrar Salida
          </Button>

        </div>

      )}

    </div>

  );

}