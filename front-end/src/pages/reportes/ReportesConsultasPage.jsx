import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import { Loader2, RefreshCw, Search } from "lucide-react";

import {
  consultarPagoPorTicket,
  consultarPorPlaca,
  consultarPorReserva,
  consultarPorTicket,
  getConsultasPagosPorFecha,
  getConsultasReservasPorFecha,
  getConsultasTicketsPorFecha,
  getConsultasVehiculosPorFecha,
} from "../../api/reportesConsultas";
import { getReportesErrorMessage } from "../../api/reportesUtils";
import { ReportesContextBar } from "../../components/reportes/ReportesContextBar";
import { ReportesPageShell } from "../../components/reportes/ReportesPageShell";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../../components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../../components/ui/table";

const toLocalDateTimeInput = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

const startOfTodayInput = () => {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  return toLocalDateTimeInput(now);
};

const nowInput = () => toLocalDateTimeInput(new Date());

const toApiLocalDateTime = (value) => {
  if (!value) return undefined;
  return value.length === 16 ? `${value}:00` : value;
};

const PAGE_SIZE = 20;

const getTotalRegistros = (response) => {
  const total = Number(response?.totalRegistros ?? 0);
  return Number.isFinite(total) ? total : 0;
};

const renderTablaDinamica = (titulo, columnas = [], filas = []) => {
  return (
    <div className="rounded-md border">
      <div className="flex items-center justify-between border-b px-3 py-2">
        <h3 className="text-sm font-semibold">{titulo}</h3>
        <span className="text-xs text-muted-foreground">{filas.length} registros</span>
      </div>

      <Table className="text-xs">
        <TableHeader>
          <TableRow>
            {columnas.map((columna) => (
              <TableHead key={columna} className="h-9 px-2">
                {columna}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {!filas.length ? (
            <TableRow>
              <TableCell colSpan={Math.max(columnas.length, 1)} className="py-6 text-center text-xs text-muted-foreground">
                Sin resultados.
              </TableCell>
            </TableRow>
          ) : (
            filas.map((fila, index) => {
              const columnasFila = fila?.columnas || {};
              return (
                <TableRow key={`row-${index}`}>
                  {columnas.map((columna) => (
                    <TableCell key={`${index}-${columna}`} className="px-2 py-2">
                      {columnasFila[columna] || "-"}
                    </TableCell>
                  ))}
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
};

export const ReportesConsultasPage = () => {
  const [fechaDesde, setFechaDesde] = useState(startOfTodayInput());
  const [fechaHasta, setFechaHasta] = useState(nowInput());
  const [seccionActiva, setSeccionActiva] = useState("tickets");

  const [ticketsPage, setTicketsPage] = useState(0);
  const [pagosPage, setPagosPage] = useState(0);
  const [reservasPage, setReservasPage] = useState(0);
  const [vehiculosPage, setVehiculosPage] = useState(0);

  const [ticketsListado, setTicketsListado] = useState(null);
  const [pagosListado, setPagosListado] = useState(null);
  const [reservasListado, setReservasListado] = useState(null);
  const [vehiculosListado, setVehiculosListado] = useState(null);

  const [codigoTicket, setCodigoTicket] = useState("");
  const [ticketDetalle, setTicketDetalle] = useState(null);

  const [codigoPagoTicket, setCodigoPagoTicket] = useState("");
  const [pagoDetalle, setPagoDetalle] = useState(null);

  const [codigoReserva, setCodigoReserva] = useState("");
  const [reservaDetalle, setReservaDetalle] = useState(null);

  const [placaVehiculo, setPlacaVehiculo] = useState("");
  const [vehiculoDetalle, setVehiculoDetalle] = useState(null);

  const [loadingTickets, setLoadingTickets] = useState(false);
  const [loadingPagos, setLoadingPagos] = useState(false);
  const [loadingReservas, setLoadingReservas] = useState(false);
  const [loadingVehiculos, setLoadingVehiculos] = useState(false);
  const [loadingDetalle, setLoadingDetalle] = useState(false);

  const baseParams = useMemo(
    () => ({
      fechaDesde: toApiLocalDateTime(fechaDesde),
      fechaHasta: toApiLocalDateTime(fechaHasta),
    }),
    [fechaDesde, fechaHasta]
  );

  const cargarTicketsListado = async (page = 0) => {
    try {
      setLoadingTickets(true);
      const data = await getConsultasTicketsPorFecha({ ...baseParams, page, size: PAGE_SIZE });
      setTicketsListado(data);
      setTicketsPage(page);
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo cargar listado de tickets"));
    } finally {
      setLoadingTickets(false);
    }
  };

  const cargarPagosListado = async (page = 0) => {
    try {
      setLoadingPagos(true);
      const data = await getConsultasPagosPorFecha({ ...baseParams, page, size: PAGE_SIZE });
      setPagosListado(data);
      setPagosPage(page);
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo cargar listado de pagos"));
    } finally {
      setLoadingPagos(false);
    }
  };

  const cargarReservasListado = async (page = 0) => {
    try {
      setLoadingReservas(true);
      const data = await getConsultasReservasPorFecha({ ...baseParams, page, size: PAGE_SIZE });
      setReservasListado(data);
      setReservasPage(page);
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo cargar listado de reservas"));
    } finally {
      setLoadingReservas(false);
    }
  };

  const cargarVehiculosListado = async (page = 0) => {
    try {
      setLoadingVehiculos(true);
      const data = await getConsultasVehiculosPorFecha({ ...baseParams, page, size: PAGE_SIZE });
      setVehiculosListado(data);
      setVehiculosPage(page);
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo cargar listado de vehiculos"));
    } finally {
      setLoadingVehiculos(false);
    }
  };

  const buscarTicketPorCodigo = async () => {
    const value = codigoTicket.trim();
    if (!value) {
      toast.error("Ingresa un codigo de ticket");
      return;
    }
    try {
      setLoadingDetalle(true);
      setTicketDetalle(await consultarPorTicket(value));
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo consultar ticket por codigo"));
    } finally {
      setLoadingDetalle(false);
    }
  };

  const buscarPagoPorCodigo = async () => {
    const value = codigoPagoTicket.trim();
    if (!value) {
      toast.error("Ingresa el codigo del ticket para consultar pago");
      return;
    }
    try {
      setLoadingDetalle(true);
      setPagoDetalle(await consultarPagoPorTicket(value));
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo consultar pago por codigo"));
    } finally {
      setLoadingDetalle(false);
    }
  };

  const buscarReservaPorCodigo = async () => {
    const value = codigoReserva.trim();
    if (!value) {
      toast.error("Ingresa un codigo de reserva");
      return;
    }
    try {
      setLoadingDetalle(true);
      setReservaDetalle(await consultarPorReserva(value));
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo consultar reserva por codigo"));
    } finally {
      setLoadingDetalle(false);
    }
  };

  const buscarVehiculoPorCodigo = async () => {
    const value = placaVehiculo.trim().toUpperCase();
    if (!value) {
      toast.error("Ingresa la placa del vehiculo");
      return;
    }
    try {
      setLoadingDetalle(true);
      setVehiculoDetalle(await consultarPorPlaca(value));
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo consultar vehiculo por placa"));
    } finally {
      setLoadingDetalle(false);
    }
  };

  const cargarSeccionActiva = async () => {
    if (seccionActiva === "tickets") return cargarTicketsListado(ticketsPage);
    if (seccionActiva === "pagos") return cargarPagosListado(pagosPage);
    if (seccionActiva === "reservas") return cargarReservasListado(reservasPage);
    return cargarVehiculosListado(vehiculosPage);
  };

  const limpiarFiltrosContexto = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
  };

  useEffect(() => {
    cargarSeccionActiva();
  }, [seccionActiva]);

  const loading = loadingTickets || loadingPagos || loadingReservas || loadingVehiculos || loadingDetalle;

  const renderPagination = (page, total, onPrev, onNext, loadingSection) => {
    const canPrev = page > 0;
    const canNext = (page + 1) * PAGE_SIZE < Number(total || 0);

    return (
      <div className="flex items-center justify-end gap-2">
        <Button size="sm" variant="outline" onClick={onPrev} disabled={!canPrev || loadingSection}>
          Anterior
        </Button>
        <Button size="sm" variant="outline" onClick={onNext} disabled={!canNext || loadingSection}>
          Siguiente
        </Button>
      </div>
    );
  };

  return (
    <ReportesPageShell
      title="Consultas"
      subtitle="Modulo profesional por secciones: Tickets, Pagos, Reservas y Vehiculo."
    >
      <ReportesContextBar
        fechaDesde={fechaDesde}
        fechaHasta={fechaHasta}
        onFechaDesdeChange={setFechaDesde}
        onFechaHastaChange={setFechaHasta}
        showGranularidadFilter={false}
        showUsuarioFilter={false}
        onLimpiar={limpiarFiltrosContexto}
        onActualizar={cargarSeccionActiva}
        loading={loading}
      />

      <Tabs value={seccionActiva} onValueChange={setSeccionActiva} className="space-y-4">
        <TabsList className="grid w-full grid-cols-2 border border-primary/20 bg-primary/5 md:grid-cols-4">
          <TabsTrigger value="tickets">Tickets</TabsTrigger>
          <TabsTrigger value="pagos">Pagos</TabsTrigger>
          <TabsTrigger value="reservas">Reservas</TabsTrigger>
          <TabsTrigger value="vehiculo">Vehiculo</TabsTrigger>
        </TabsList>

        <TabsContent value="tickets" className="space-y-4">
          <div className="rounded-lg border bg-card p-3 space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Listado por fecha</h2>
              <Button size="sm" variant="outline" onClick={() => cargarTicketsListado(ticketsPage)} disabled={loadingTickets}>
                <RefreshCw className={`h-4 w-4 ${loadingTickets ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>
            {renderTablaDinamica("Tickets", ticketsListado?.columnas || [], ticketsListado?.filas || [])}
            {renderPagination(
              ticketsPage,
              getTotalRegistros(ticketsListado),
              () => cargarTicketsListado(ticketsPage - 1),
              () => cargarTicketsListado(ticketsPage + 1),
              loadingTickets
            )}
          </div>

          <div className="rounded-lg border bg-card p-3 space-y-3">
            <h2 className="text-sm font-semibold">Consultar por codigo de ticket</h2>
            <div className="flex gap-2">
              <Input value={codigoTicket} onChange={(e) => setCodigoTicket(e.target.value)} placeholder="Ej: T-1712433330000" />
              <Button size="sm" onClick={buscarTicketPorCodigo} disabled={loadingDetalle}>
                {loadingDetalle ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
              </Button>
            </div>
            {ticketDetalle && renderTablaDinamica(ticketDetalle.titulo || "Detalle ticket", ticketDetalle.columnas || [], ticketDetalle.filas || [])}
          </div>
        </TabsContent>

        <TabsContent value="pagos" className="space-y-4">
          <div className="rounded-lg border bg-card p-3 space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Listado por fecha</h2>
              <Button size="sm" variant="outline" onClick={() => cargarPagosListado(pagosPage)} disabled={loadingPagos}>
                <RefreshCw className={`h-4 w-4 ${loadingPagos ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>
            {renderTablaDinamica("Pagos", pagosListado?.columnas || [], pagosListado?.filas || [])}
            {renderPagination(
              pagosPage,
              getTotalRegistros(pagosListado),
              () => cargarPagosListado(pagosPage - 1),
              () => cargarPagosListado(pagosPage + 1),
              loadingPagos
            )}
          </div>

          <div className="rounded-lg border bg-card p-3 space-y-3">
            <h2 className="text-sm font-semibold">Consultar pago por codigo de ticket</h2>
            <div className="flex gap-2">
              <Input value={codigoPagoTicket} onChange={(e) => setCodigoPagoTicket(e.target.value)} placeholder="Ej: T-1712433330000" />
              <Button size="sm" onClick={buscarPagoPorCodigo} disabled={loadingDetalle}>
                {loadingDetalle ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
              </Button>
            </div>
            {pagoDetalle && renderTablaDinamica(pagoDetalle.titulo || "Detalle pago", pagoDetalle.columnas || [], pagoDetalle.filas || [])}
          </div>
        </TabsContent>

        <TabsContent value="reservas" className="space-y-4">
          <div className="rounded-lg border bg-card p-3 space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Listado por fecha</h2>
              <Button size="sm" variant="outline" onClick={() => cargarReservasListado(reservasPage)} disabled={loadingReservas}>
                <RefreshCw className={`h-4 w-4 ${loadingReservas ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>
            {renderTablaDinamica("Reservas", reservasListado?.columnas || [], reservasListado?.filas || [])}
            {renderPagination(
              reservasPage,
              getTotalRegistros(reservasListado),
              () => cargarReservasListado(reservasPage - 1),
              () => cargarReservasListado(reservasPage + 1),
              loadingReservas
            )}
          </div>

          <div className="rounded-lg border bg-card p-3 space-y-3">
            <h2 className="text-sm font-semibold">Consultar por codigo de reserva</h2>
            <div className="flex gap-2">
              <Input value={codigoReserva} onChange={(e) => setCodigoReserva(e.target.value)} placeholder="Ej: R-1712433330000" />
              <Button size="sm" onClick={buscarReservaPorCodigo} disabled={loadingDetalle}>
                {loadingDetalle ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
              </Button>
            </div>
            {reservaDetalle && renderTablaDinamica(reservaDetalle.titulo || "Detalle reserva", reservaDetalle.columnas || [], reservaDetalle.filas || [])}
          </div>
        </TabsContent>

        <TabsContent value="vehiculo" className="space-y-4">
          <div className="rounded-lg border bg-card p-3 space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Listado por fecha</h2>
              <Button size="sm" variant="outline" onClick={() => cargarVehiculosListado(vehiculosPage)} disabled={loadingVehiculos}>
                <RefreshCw className={`h-4 w-4 ${loadingVehiculos ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>
            {renderTablaDinamica("Vehiculos", vehiculosListado?.columnas || [], vehiculosListado?.filas || [])}
            {renderPagination(
              vehiculosPage,
              getTotalRegistros(vehiculosListado),
              () => cargarVehiculosListado(vehiculosPage - 1),
              () => cargarVehiculosListado(vehiculosPage + 1),
              loadingVehiculos
            )}
          </div>

          <div className="rounded-lg border bg-card p-3 space-y-3">
            <h2 className="text-sm font-semibold">Consultar por codigo de vehiculo (placa)</h2>
            <div className="flex gap-2">
              <Input value={placaVehiculo} onChange={(e) => setPlacaVehiculo(e.target.value)} placeholder="Ej: A123456" />
              <Button size="sm" onClick={buscarVehiculoPorCodigo} disabled={loadingDetalle}>
                {loadingDetalle ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
              </Button>
            </div>

            {vehiculoDetalle?.tickets &&
              renderTablaDinamica(
                vehiculoDetalle.tickets?.titulo || "Tickets del vehiculo",
                vehiculoDetalle.tickets?.columnas || [],
                vehiculoDetalle.tickets?.filas || []
              )}

            {vehiculoDetalle?.reservas &&
              renderTablaDinamica(
                vehiculoDetalle.reservas?.titulo || "Reservas del vehiculo",
                vehiculoDetalle.reservas?.columnas || [],
                vehiculoDetalle.reservas?.filas || []
              )}
          </div>
        </TabsContent>
      </Tabs>
    </ReportesPageShell>
  );
};
