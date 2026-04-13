
import { useEffect, useMemo, useState } from "react";
import { Banknote, Bike, Car, CheckCircle2, CreditCard, Info, RefreshCw, Ticket } from "lucide-react";
import toast from "react-hot-toast";
import { useLocation, useNavigate } from "react-router-dom";

import { registrarEntradaVehiculo } from "../api/entradas";
import { getEspacios } from "../api/espacios";
import { getResumenSalidaPorEspacio, procesarCobroSalida } from "../api/salidas";
import { Badge } from "../components/ui/badge";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import EspacioCard from "../components/espacios/EspacioCard";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle
} from "../components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../components/ui/tabs";

const getErrorMessage = (error, fallback) => {
  return error?.response?.data?.message || error?.message || fallback;
};

const formatCurrency = (value) => {
  const numericValue = Number(value || 0);
  return new Intl.NumberFormat("es-DO", {
    style: "currency",
    currency: "DOP",
    minimumFractionDigits: 2
  }).format(numericValue);
};

const formatDateTime = (value) => {
  if (!value) return "-";
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) return value;
  return parsedDate.toLocaleString("es-DO");
};

const formatDuration = (minutesValue) => {
  const minutes = Number(minutesValue || 0);
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  if (hours > 0) {
    return `${hours}h ${remainingMinutes}m`;
  }
  return `${remainingMinutes}m`;
};

export const EntradaPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [espacios, setEspacios] = useState([]);
  const [tipoVehiculo, setTipoVehiculo] = useState("CARRO");
  const [espacioSeleccionadoId, setEspacioSeleccionadoId] = useState(null);
  const [placa, setPlaca] = useState("");

  const [ticketRegistrado, setTicketRegistrado] = useState(null);
  const [espacioDetalle, setEspacioDetalle] = useState(null);
  const [openDetalleDialog, setOpenDetalleDialog] = useState(false);

  const [openCobroDialog, setOpenCobroDialog] = useState(false);
  const [loadingResumenCobro, setLoadingResumenCobro] = useState(false);
  const [loadingProcesarCobro, setLoadingProcesarCobro] = useState(false);
  const [salidaResumen, setSalidaResumen] = useState(null);
  const [cobroProcesado, setCobroProcesado] = useState(null);
  const [metodoPago, setMetodoPago] = useState("EFECTIVO");
  const [montoRecibido, setMontoRecibido] = useState("");

  const [loading, setLoading] = useState(false);
  const [loadingRegistro, setLoadingRegistro] = useState(false);

  const fetchEspacios = async (showErrorToast = false) => {
    try {
      setLoading(true);
      const data = await getEspacios();
      setEspacios(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error cargando panel de espacios:", error);
      if (showErrorToast) {
        toast.error("No se pudo actualizar el panel de espacios");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegistrarEntrada = async (event) => {
    event.preventDefault();

    const espacioSeleccionado = espacios.find((espacio) => espacio.id === espacioSeleccionadoId);
    if (!espacioSeleccionado) {
      toast.error("Seleccione un espacio libre para registrar la entrada");
      return;
    }

    const estadoEspacio = (espacioSeleccionado.estado || "").toUpperCase();
    if (estadoEspacio !== "LIBRE" && estadoEspacio !== "RESERVADO") {
      toast.error("Solo se puede registrar entrada en espacios LIBRES o RESERVADOS");
      return;
    }

    if (!placa.trim()) {
      toast.error("Ingrese la placa del vehiculo");
      return;
    }

    try {
      setLoadingRegistro(true);

      const ticket = await registrarEntradaVehiculo({
        placa: placa.trim().toUpperCase(),
        tipoVehiculo,
        espacioId: espacioSeleccionado.id
      });

      setTicketRegistrado(ticket);
      setPlaca("");
      setEspacioSeleccionadoId(null);
      toast.success("Entrada registrada correctamente");
      await fetchEspacios(false);
    } catch (error) {
      console.error("Error registrando entrada:", error);
      toast.error(getErrorMessage(error, "No se pudo registrar la entrada"));
    } finally {
      setLoadingRegistro(false);
    }
  };

  const handleOpenDetalle = (space) => {
    setEspacioDetalle(space);
    setOpenDetalleDialog(true);
  };

  const resetCobroState = () => {
    setSalidaResumen(null);
    setCobroProcesado(null);
    setMetodoPago("EFECTIVO");
    setMontoRecibido("");
  };

  const handleIniciarCobro = async (espacio) => {
    if (!espacio || (espacio.estado || "").toUpperCase() !== "OCUPADO") {
      toast.error("Seleccione un espacio ocupado para procesar salida");
      return;
    }

    try {
      setLoadingResumenCobro(true);
      resetCobroState();
      const resumen = await getResumenSalidaPorEspacio(espacio.id);
      setSalidaResumen(resumen);
      setOpenDetalleDialog(false);
      setOpenCobroDialog(true);
    } catch (error) {
      console.error("Error cargando resumen de cobro:", error);
      toast.error(getErrorMessage(error, "No se pudo cargar el resumen de cobro"));
    } finally {
      setLoadingResumenCobro(false);
    }
  };

  const handleProcesarCobro = async () => {
    if (!salidaResumen) {
      toast.error("No hay resumen de salida para procesar");
      return;
    }

    if (metodoPago === "EFECTIVO") {
      if (!montoRecibido.trim()) {
        toast.error("Ingrese el monto recibido en efectivo");
        return;
      }

      const monto = Number(montoRecibido);
      if (Number.isNaN(monto) || monto < 0) {
        toast.error("El monto recibido no es valido");
        return;
      }
    }

    try {
      setLoadingProcesarCobro(true);
      const response = await procesarCobroSalida({
        espacioId: salidaResumen.espacioId,
        metodoPago,
        montoRecibido: metodoPago === "EFECTIVO" ? Number(montoRecibido) : null
      });

      setCobroProcesado(response);
      toast.success("Salida y cobro registrados correctamente");
      setOpenDetalleDialog(false);
      await fetchEspacios(false);
    } catch (error) {
      console.error("Error procesando cobro de salida:", error);
      toast.error(getErrorMessage(error, "No se pudo procesar el cobro"));
    } finally {
      setLoadingProcesarCobro(false);
    }
  };

  const handleCloseCobroDialog = (open) => {
    setOpenCobroDialog(open);
    if (!open) {
      resetCobroState();
    }
  };

  useEffect(() => {
    fetchEspacios(true);
  }, []);

  useEffect(() => {
    const prefill = location.state;
    if (!prefill?.reservaConfirmada) return;

    if (prefill.placa) {
      setPlaca(String(prefill.placa).toUpperCase());
    }

    if (prefill.tipoVehiculo) {
      setTipoVehiculo(String(prefill.tipoVehiculo).toUpperCase());
    }

    if (prefill.espacioId) {
      setEspacioSeleccionadoId(Number(prefill.espacioId));
    }

    toast.success("Datos de reserva cargados. Solo confirme para emitir el ticket.", {
      id: "reserva-prefill"
    });

    navigate(location.pathname, { replace: true, state: null });
  }, [location.pathname, location.state, navigate]);

  const espaciosCarros = useMemo(
    () => espacios.filter((e) => e.tipoVehiculo === "CARRO"),
    [espacios]
  );

  const espaciosMotos = useMemo(
    () => espacios.filter((e) => e.tipoVehiculo === "MOTO"),
    [espacios]
  );

  const stats = useMemo(
    () => ({
      libre: espacios.filter((e) => e.estado === "LIBRE").length,
      ocupado: espacios.filter((e) => e.estado === "OCUPADO").length,
      reservado: espacios.filter((e) => e.estado === "RESERVADO").length
    }),
    [espacios]
  );

  const renderEspacios = (lista) => {
    if (!lista.length) {
      return (
        <div className="rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground">
          No hay espacios para mostrar.
        </div>
      );
    }

    return (
      <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-3">
        {lista.map((space) => (
          <div
            key={space.id}
            role="button"
            tabIndex={0}
            onClick={() => {
              if ((space.estado || "").toUpperCase() === "LIBRE" && space.tipoVehiculo === tipoVehiculo) {
                setEspacioSeleccionadoId(space.id);
              }
            }}
            onDoubleClick={() => handleOpenDetalle(space)}
            onKeyDown={(event) => {
              if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                if ((space.estado || "").toUpperCase() === "LIBRE" && space.tipoVehiculo === tipoVehiculo) {
                  setEspacioSeleccionadoId(space.id);
                }
              }
            }}
            className={`relative text-left rounded-lg transition-all cursor-pointer ${espacioSeleccionadoId === space.id ? "ring-2 ring-primary ring-offset-2" : ""}`}
          >
            <button
              type="button"
              className="absolute right-1 top-1 z-10 rounded-md bg-transparent p-1 text-muted-foreground/80 transition-colors hover:bg-white/80 hover:text-foreground"
              onClick={(event) => {
                event.stopPropagation();
                handleOpenDetalle(space);
              }}
              aria-label="Ver detalle del espacio"
              title="Ver detalle"
            >
              <Info className="h-3.5 w-3.5" />
            </button>

            <EspacioCard
              numero={space.numero || space.codigoEspacio}
              estado={space.estado}
              tipoVehiculo={space.tipoVehiculo}
              ticketActivo={space.ticketActivo}
              showActions={false}
            />
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight">Panel de Entradas y Salidas</h1>
        </div>

        <Button
          variant="outline"
          onClick={() => fetchEspacios(true)}
          disabled={loading}
        >
          <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
          Actualizar
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_340px]">
        <Card>
          <CardHeader className="pb-3" />

          <CardContent>
            <Tabs
              value={tipoVehiculo === "CARRO" ? "carros" : "motos"}
              onValueChange={(value) => {
                setTipoVehiculo(value === "motos" ? "MOTO" : "CARRO");
                setEspacioSeleccionadoId(null);
              }}
              className="space-y-4"
            >
              <div className="flex flex-wrap items-center gap-2">
                <Badge variant="outline" className="border-emerald-300 text-emerald-700 bg-emerald-50">
                  Libres: {stats.libre}
                </Badge>
                <Badge variant="outline" className="border-rose-300 text-rose-700 bg-rose-50">
                  Ocupados: {stats.ocupado}
                </Badge>
                <Badge variant="outline" className="border-amber-300 text-amber-700 bg-amber-50">
                  Reservados: {stats.reservado}
                </Badge>
              </div>

              <TabsList className="grid w-full grid-cols-2 border border-primary/30 bg-primary/10 p-1">
                <TabsTrigger
                  value="carros"
                  className="text-primary data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
                >
                  <Car className="mr-2 h-4 w-4" />
                  Carros ({espaciosCarros.length})
                </TabsTrigger>
                <TabsTrigger
                  value="motos"
                  className="text-primary data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
                >
                  <Bike className="mr-2 h-4 w-4" />
                  Motos ({espaciosMotos.length})
                </TabsTrigger>
              </TabsList>

              <TabsContent value="carros">{renderEspacios(espaciosCarros)}</TabsContent>
              <TabsContent value="motos">{renderEspacios(espaciosMotos)}</TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        <Card className="h-fit">
          <CardHeader>
            <CardTitle className="text-base">Registrar Entrada</CardTitle>
          </CardHeader>

          <CardContent className="space-y-4">
            <form onSubmit={handleRegistrarEntrada} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="placa">Placa del Vehiculo</Label>
                <Input
                  id="placa"
                  value={placa}
                  onChange={(e) => setPlaca(e.target.value.toUpperCase())}
                  placeholder="ABC123"
                  autoComplete="off"
                />
              </div>

              <div className="rounded-lg border bg-muted/20 p-3 text-sm">
                <p className="text-xs text-muted-foreground">Espacio seleccionado</p>
                <p className="font-semibold text-base">
                  {espacioSeleccionadoId
                    ? espacios.find((e) => e.id === espacioSeleccionadoId)?.codigoEspacio
                    : "Ninguno"}
                </p>
                <p className="text-xs text-muted-foreground mt-1">Tipo: {tipoVehiculo}</p>
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={loadingRegistro || !espacioSeleccionadoId || !placa.trim()}
              >
                <Ticket className="mr-2 h-4 w-4" />
                {loadingRegistro ? "Registrando..." : "Registrar Entrada"}
              </Button>
            </form>

            {ticketRegistrado && (
              <div className="rounded-lg border border-emerald-300 bg-emerald-50 p-3 text-sm space-y-1">
                <p className="font-semibold text-emerald-800">Entrada registrada</p>
                <p><strong>Ticket:</strong> {ticketRegistrado.codigoTicket}</p>
                <p><strong>Placa:</strong> {ticketRegistrado.placa}</p>
                <p><strong>Espacio:</strong> {ticketRegistrado.codigoEspacio}</p>
              </div>
            )}

          </CardContent>
        </Card>
      </div>

      <Dialog open={openDetalleDialog} onOpenChange={setOpenDetalleDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Detalle del Espacio</DialogTitle>
            <DialogDescription>
              Informacion operativa del espacio seleccionado.
            </DialogDescription>
          </DialogHeader>

          {espacioDetalle && (
            <div className="space-y-2 text-sm">
              {espacioDetalle.ticketActivo ? (
                <div className="space-y-1 mt-1">
                  <div><strong>Numero de Ticket Asociado:</strong> {espacioDetalle.ticketActivo.codigoTicket}</div>
                  <div><strong>Placa:</strong> {espacioDetalle.ticketActivo.placa}</div>
                  <div><strong>Hora de entrada:</strong> {espacioDetalle.ticketActivo.horaEntrada}</div>
                  <Button
                    type="button"
                    className="w-full mt-3"
                    onClick={() => handleIniciarCobro(espacioDetalle)}
                    disabled={loadingResumenCobro}
                  >
                    {loadingResumenCobro ? "Cargando cobro..." : "Procesar Salida y Cobro"}
                  </Button>
                </div>
              ) : espacioDetalle.reservaActiva ? (
                <div className="space-y-1 mt-1">
                  <div><strong>Codigo de Reserva:</strong> {espacioDetalle.reservaActiva.codigoReserva}</div>
                  <div><strong>Cliente:</strong> {espacioDetalle.reservaActiva.clienteNombreCompleto}</div>
                  <div><strong>Placa:</strong> {espacioDetalle.reservaActiva.placa}</div>
                  <div><strong>Hora de llegada esperada:</strong> {espacioDetalle.reservaActiva.horaInicio}</div>
                </div>
              ) : (
                <p className="text-muted-foreground">No hay un vehiculo parqueado en este espacio.</p>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={openCobroDialog} onOpenChange={handleCloseCobroDialog}>
        <DialogContent className="sm:max-w-md">
          {!cobroProcesado && (
            <DialogHeader>
              <DialogTitle>Salida y Cobro</DialogTitle>
              <DialogDescription>
                Complete el pago para registrar la salida y liberar el espacio.
              </DialogDescription>
            </DialogHeader>
          )}

          {salidaResumen && !cobroProcesado && (
            <div className="space-y-4 text-sm">
              <div className="rounded-lg border bg-muted/20 p-3 space-y-1">
                <div><strong>Espacio:</strong> {salidaResumen.codigoEspacio}</div>
                <div><strong>Ticket:</strong> {salidaResumen.codigoTicket}</div>
                <div><strong>Placa:</strong> {salidaResumen.placa}</div>
                <div><strong>Entrada:</strong> {formatDateTime(salidaResumen.horaEntrada)}</div>
                <div><strong>Tiempo:</strong> {formatDuration(salidaResumen.minutosEstadia)}</div>
                <div className="text-base font-semibold pt-1">
                  Total: {formatCurrency(salidaResumen.montoTotal)}
                </div>
              </div>

              <div className="space-y-2">
                <Label>Metodo de pago</Label>
                <div className="grid grid-cols-2 gap-2">
                  <Button
                    type="button"
                    variant={metodoPago === "EFECTIVO" ? "default" : "outline"}
                    onClick={() => setMetodoPago("EFECTIVO")}
                  >
                    <Banknote className="mr-2 h-4 w-4" />
                    Efectivo
                  </Button>
                  <Button
                    type="button"
                    variant={metodoPago === "TARJETA" ? "default" : "outline"}
                    onClick={() => setMetodoPago("TARJETA")}
                  >
                    <CreditCard className="mr-2 h-4 w-4" />
                    Tarjeta
                  </Button>
                </div>
              </div>

              {metodoPago === "EFECTIVO" && (
                <div className="space-y-2">
                  <Label htmlFor="monto-recibido">Monto recibido</Label>
                  <Input
                    id="monto-recibido"
                    type="number"
                    min="0"
                    step="0.01"
                    value={montoRecibido}
                    onChange={(event) => setMontoRecibido(event.target.value)}
                    placeholder="0.00"
                  />
                </div>
              )}

              <Button
                type="button"
                className="w-full"
                onClick={handleProcesarCobro}
                disabled={loadingProcesarCobro}
              >
                {loadingProcesarCobro ? "Procesando..." : "Confirmar Salida y Cobro"}
              </Button>
            </div>
          )}

          {cobroProcesado && (
            <div className="space-y-4 text-sm">
              <div className="rounded-lg border border-emerald-300 bg-emerald-50 p-5">
                <div className="flex flex-col items-center text-center gap-3">
                  <CheckCircle2 className="h-16 w-16 text-emerald-600" />
                  <p className="text-xs uppercase tracking-wide text-emerald-800/80">Cambio</p>
                  <p className="text-3xl font-bold text-emerald-800">
                    {formatCurrency(cobroProcesado.cambio)}
                  </p>
                </div>
              </div>

              <Button type="button" className="w-full" onClick={() => setOpenCobroDialog(false)}>
                Cerrar
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};


