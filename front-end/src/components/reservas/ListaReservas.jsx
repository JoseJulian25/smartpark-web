import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

import {
  confirmarLlegada,
  cancelarReserva,
  getReservas,
  reenviarCorreoReserva
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
import { Input } from "../ui/input";
import { Badge } from "../ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from "../ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "../ui/select";

const ESTADOS_HISTORIAL = ["FINALIZADA", "CANCELADA"];
const MOTIVOS_CANCELACION = [
  "Cliente no se presentara",
  "Error en los datos de la reserva",
  "Espacio no disponible por incidencia",
  "Solicitud del cliente",
  "Cambio de horario del cliente"
];

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

const getErrorMessage = (error, fallback) => {
  return (
    error?.response?.data?.message ||
    error?.message ||
    fallback
  );
};

export default function ListaReservas({ refresh }) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAdmin = (user?.rol || user?.role || "").toLowerCase() === "admin";

  const [reservas, setReservas] = useState([]);
  const [filtro, setFiltro] = useState("pendientes");
  const [busqueda, setBusqueda] = useState("");
  const [loading, setLoading] = useState(false);
  const [procesandoCodigo, setProcesandoCodigo] = useState("");
  const [reenviandoCodigo, setReenviandoCodigo] = useState("");
  const [openCancelDialog, setOpenCancelDialog] = useState(false);
  const [motivoCancelacion, setMotivoCancelacion] = useState("");
  const [codigoReservaCancelar, setCodigoReservaCancelar] = useState("");

  const fetchReservas = async () => {
    try {

      setLoading(true);

      const data = await getReservas();
      setReservas(Array.isArray(data) ? data : []);

    } catch (error) {
      console.error("Error cargando reservas:", error);
      setReservas([]);
      toast.error(getErrorMessage(error, "Error cargando reservas"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservas();
  }, [refresh]);

  const handleConfirmar = async (codigoReserva) => {
    try {
      setProcesandoCodigo(codigoReserva);
      const reservaActualizada = await confirmarLlegada(codigoReserva);
      await fetchReservas();

      navigate("/entrada", {
        state: {
          reservaConfirmada: true,
          placa: reservaActualizada?.placa,
          tipoVehiculo: reservaActualizada?.tipoVehiculo,
          espacioId: reservaActualizada?.espacioId,
          codigoReserva: reservaActualizada?.codigoReserva,
        },
      });
    } catch (error) {
      console.error(error);
      toast.error(getErrorMessage(error, "Error confirmando llegada"));
    } finally {
      setProcesandoCodigo("");
    }
  };

  const handleOpenCancelar = (codigoReserva) => {
    setCodigoReservaCancelar(codigoReserva);
    setMotivoCancelacion("");
    setOpenCancelDialog(true);
  };

  const handleCancelar = async () => {
    if (!codigoReservaCancelar) {
      return;
    }

    if (!motivoCancelacion.trim()) {
      toast.error("Debe indicar un motivo para cancelar la reserva");
      return;
    }

    try {
      setProcesandoCodigo(codigoReservaCancelar);
      await cancelarReserva(codigoReservaCancelar, motivoCancelacion.trim());
      toast.success("Reserva cancelada correctamente");
      setOpenCancelDialog(false);
      setCodigoReservaCancelar("");
      setMotivoCancelacion("");
      await fetchReservas();
    } catch (error) {
      console.error(error);
      toast.error(getErrorMessage(error, "Error cancelando reserva"));
    } finally {
      setProcesandoCodigo("");
    }
  };

  const handleReenviarCorreo = async (codigoReserva) => {
    try {
      setReenviandoCodigo(codigoReserva);
      await reenviarCorreoReserva(codigoReserva);
      toast.success("Correo reenviado correctamente");
      await fetchReservas();
    } catch (error) {
      console.error(error);
      toast.error(getErrorMessage(error, "Error reenviando correo"));
    } finally {
      setReenviandoCodigo("");
    }
  };

  const resumen = useMemo(() => {
    const base = {
      pendientes: 0,
      activas: 0,
      historial: 0
    };

    reservas.forEach((reserva) => {
      const estado = (reserva.estado || "").toUpperCase();
      if (estado === "PENDIENTE") {
        base.pendientes += 1;
      } else if (estado === "ACTIVA") {
        base.activas += 1;
      } else if (ESTADOS_HISTORIAL.includes(estado)) {
        base.historial += 1;
      }
    });

    return base;
  }, [reservas]);

  const reservasFiltradas = reservas.filter((reserva) => {
    const estado = (reserva.estado || "").toUpperCase();
    const term = busqueda.trim().toLowerCase();

    if (term) {
      const texto = [
        reserva.codigoReserva,
        reserva.clienteNombreCompleto,
        reserva.placa,
        reserva.codigoEspacio
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      if (!texto.includes(term)) {
        return false;
      }
    }

    if (filtro === "pendientes") {
      return estado === "PENDIENTE";
    }

    if (filtro === "activas") {
      return estado === "ACTIVA";
    }

    return ESTADOS_HISTORIAL.includes(estado);
  });

  return (

    <Card>

      <CardHeader className="space-y-3">

        <CardTitle className="text-base">
          Lista de Reservas
        </CardTitle>

        <div className="grid gap-2 md:grid-cols-[1fr_auto] md:items-center">

          <Input
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por codigo, cliente, placa o espacio"
            className="h-9"
          />

          <div className="flex flex-wrap gap-2">

            <Button
              size="sm"
              variant={filtro === "pendientes" ? "default" : "outline"}
              onClick={() => setFiltro("pendientes")}
            >
              Pendientes ({resumen.pendientes})
            </Button>

            <Button
              size="sm"
              variant={filtro === "activas" ? "default" : "outline"}
              onClick={() => setFiltro("activas")}
            >
              Activas ({resumen.activas})
            </Button>

            <Button
              size="sm"
              variant={filtro === "historial" ? "default" : "outline"}
              onClick={() => setFiltro("historial")}
            >
              Historial ({resumen.historial})
            </Button>

          </div>
        </div>

      </CardHeader>

      <CardContent>

        <Table className="text-xs md:text-sm">

          <TableHeader>

            <TableRow>
              <TableHead>Codigo</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Placa</TableHead>
              <TableHead>Espacio</TableHead>
              <TableHead>Horario</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead>Acciones</TableHead>
            </TableRow>

          </TableHeader>

          <TableBody>

            {loading && (
              <TableRow>
                <TableCell colSpan={7}>
                  <div className="flex justify-center items-center py-4">
                    <svg className="animate-spin h-6 w-6 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"></path>
                    </svg>
                  </div>
                </TableCell>
              </TableRow>
            )}

            {!loading && reservasFiltradas.length === 0 && (
              <TableRow>
                <TableCell colSpan={7}>
                  No hay reservas para este filtro
                </TableCell>
              </TableRow>
            )}

            {reservasFiltradas.map((reserva) => (

              <TableRow key={reserva.codigoReserva || reserva.id}>

                <TableCell>
                  {reserva.codigoReserva}
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
                  <div className="leading-tight">
                    <div>{formatDateTime(reserva.horaInicio)}</div>
                    <div className="text-muted-foreground">{formatDateTime(reserva.horaFin)}</div>
                    {(reserva.estado || "").toUpperCase() === "CANCELADA" && reserva.motivoCancelacion && (
                      <div className="mt-1 space-y-1">
                        <div className="text-rose-700">Motivo: {reserva.motivoCancelacion}</div>
                        <div className="text-xs text-muted-foreground">
                          Cancelado por: {reserva.canceladoPor || "SIN_USUARIO"}
                        </div>
                      </div>
                    )}
                  </div>
                </TableCell>

                <TableCell>
                  <Badge variant="outline" className={getEstadoStyle(reserva.estado)}>
                    {reserva.estado}
                  </Badge>
                </TableCell>

                <TableCell className="space-x-2 whitespace-nowrap">

                  {filtro === "pendientes" && (
                    <Button
                      size="sm"
                      disabled={procesandoCodigo === reserva.codigoReserva}
                      onClick={() => handleConfirmar(reserva.codigoReserva)}
                    >
                      {procesandoCodigo === reserva.codigoReserva ? "Procesando..." : "Confirmar"}
                    </Button>
                  )}

                  {(filtro === "pendientes" || filtro === "activas") && (
                    <Button
                      size="sm"
                      variant="destructive"
                      disabled={procesandoCodigo === reserva.codigoReserva}
                      onClick={() => handleOpenCancelar(reserva.codigoReserva)}
                    >
                      {procesandoCodigo === reserva.codigoReserva ? "Procesando..." : "Cancelar"}
                    </Button>
                  )}

                  {isAdmin && !reserva.correoEnviado && (
                    <Button
                      size="sm"
                      variant="outline"
                      disabled={reenviandoCodigo === reserva.codigoReserva}
                      onClick={() => handleReenviarCorreo(reserva.codigoReserva)}
                    >
                      {reenviandoCodigo === reserva.codigoReserva ? "Reenviando..." : "Reenviar correo"}
                    </Button>
                  )}

                </TableCell>

              </TableRow>

            ))}

          </TableBody>

        </Table>

      </CardContent>

      <Dialog open={openCancelDialog} onOpenChange={setOpenCancelDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cancelar Reserva</DialogTitle>
            <DialogDescription>
              Seleccione el motivo de cancelacion para continuar.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <label className="text-sm font-medium">Motivo</label>
            <Select value={motivoCancelacion} onValueChange={setMotivoCancelacion}>
              <SelectTrigger>
                <SelectValue placeholder="Seleccione un motivo" />
              </SelectTrigger>
              <SelectContent>
                {MOTIVOS_CANCELACION.map((motivo) => (
                  <SelectItem key={motivo} value={motivo}>
                    {motivo}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setOpenCancelDialog(false);
                setCodigoReservaCancelar("");
                setMotivoCancelacion("");
              }}
            >
              Cerrar
            </Button>
            <Button
              type="button"
              variant="destructive"
              disabled={!motivoCancelacion || procesandoCodigo === codigoReservaCancelar}
              onClick={handleCancelar}
            >
              {procesandoCodigo === codigoReservaCancelar ? "Procesando..." : "Confirmar Cancelacion"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

    </Card>

  );
}