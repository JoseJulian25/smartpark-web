import { useState } from "react";
import toast from "react-hot-toast";

import {
  buscarReservaPorCodigo,
  confirmarLlegada
} from "../../api/reservas";

import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Alert, AlertDescription } from "../ui/alert";
import { Badge } from "../ui/badge";

const getEstadoStyle = (estado) => {
  const estadoNormalizado = (estado || "").toUpperCase();

  if (estadoNormalizado === "PENDIENTE") {
    return "bg-amber-100 text-amber-800 border-amber-300";
  }

  if (estadoNormalizado === "ACTIVA") {
    return "bg-blue-100 text-blue-800 border-blue-300";
  }

  if (estadoNormalizado === "FINALIZADA") {
    return "bg-emerald-100 text-emerald-800 border-emerald-300";
  }

  if (estadoNormalizado === "CANCELADA") {
    return "bg-rose-100 text-rose-800 border-rose-300";
  }

  return "bg-slate-100 text-slate-700 border-slate-300";
};

const formatDateTime = (value) => {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("es-DO", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(date);
};

export default function ConfirmarLlegada({ onSuccess }) {

  const [codigoReserva, setCodigoReserva] = useState("");
  const [reserva, setReserva] = useState(null);
  const [buscando, setBuscando] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  const cargarReserva = async () => {
    try {
      const codigo = codigoReserva.trim();
      if (!codigo) {
        toast.error("Ingrese el codigo de reserva");
        return;
      }

      setBuscando(true);
      setError("");
      setSuccess("");

      const data = await buscarReservaPorCodigo(codigo);
      setReserva(data);
      toast.success("Reserva encontrada");
    } catch (err) {
      const mensajeError =
        err?.response?.data?.message ||
        "No se pudo cargar la reserva";

      console.error(err);
      setReserva(null);
      setError(mensajeError);
      toast.error(mensajeError);
    } finally {
      setBuscando(false);
    }
  };

  const handleConfirmar = async () => {
    try {
      if (!reserva) {
        toast.error("Primero busque una reserva");
        return;
      }

      if ((reserva.estado || "").toUpperCase() !== "PENDIENTE") {
        toast.error("Solo se pueden confirmar reservas en estado PENDIENTE");
        return;
      }

      setLoading(true);
      setError("");
      setSuccess("");

      await confirmarLlegada(reserva.codigoReserva);

      setSuccess("Llegada confirmada correctamente");
      toast.success("Llegada confirmada correctamente");
      setCodigoReserva("");
      setReserva(null);

      if (onSuccess) {
        onSuccess();
      }

    } catch (err) {
      const mensajeError =
        err?.response?.data?.message ||
        "Error confirmando llegada";

      console.error(err);

      setError(mensajeError);
      toast.error(mensajeError);

    } finally {
      setLoading(false);
    }
  };

  return (

    <Card>

      <CardHeader>
        <CardTitle className="text-base">
          Confirmar Llegada
        </CardTitle>
      </CardHeader>

      <CardContent className="space-y-4">

        <p className="text-sm text-muted-foreground">
          Busque la reserva por codigo y confirme la llegada solo si esta en estado PENDIENTE.
        </p>

        <div className="grid gap-2 md:grid-cols-[1fr_auto]">
          <Input
            placeholder="Codigo de la reserva"
            value={codigoReserva}
            onChange={(e) => setCodigoReserva(e.target.value.toUpperCase())}
          />

          <Button
            type="button"
            variant="outline"
            onClick={cargarReserva}
            disabled={buscando || loading}
          >
            {buscando ? "Buscando..." : "Buscar"}
          </Button>
        </div>

        {reserva && (
          <div className="rounded-md border bg-white p-3 shadow-sm space-y-2">
            <div className="flex items-center justify-between gap-2">
              <div className="text-sm font-medium">{reserva.codigoReserva}</div>
              <Badge variant="outline" className={getEstadoStyle(reserva.estado)}>
                {reserva.estado}
              </Badge>
            </div>
            <div className="text-sm">Cliente: {reserva.clienteNombreCompleto}</div>
            <div className="text-sm">Placa: {reserva.placa}</div>
            <div className="text-sm">Espacio: {reserva.codigoEspacio}</div>
            <div className="text-xs text-muted-foreground">
              {formatDateTime(reserva.horaInicio)} - {formatDateTime(reserva.horaFin)}
            </div>
          </div>
        )}

        <Button
          onClick={handleConfirmar}
          disabled={loading || !reserva}
          className="w-full"
        >
          {loading ? "Confirmando..." : "Confirmar Llegada"}
        </Button>

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

      </CardContent>

    </Card>

  );
}