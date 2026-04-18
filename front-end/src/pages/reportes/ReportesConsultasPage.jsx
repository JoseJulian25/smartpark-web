import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import { Loader2, RefreshCw, Search } from "lucide-react";

import {
  anularTicket,
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
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../../components/ui/dialog";
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

const isEstadoColumn = (columnName) => String(columnName || "").toLowerCase().includes("estado");

const getEstadoBadgeClassName = (estado) => {
  const normalized = String(estado || "").trim().toUpperCase();
  if (normalized === "ACTIVO") return "border-emerald-300 bg-emerald-50 text-emerald-700";
  if (normalized === "CERRADO" || normalized === "FINALIZADA") return "border-slate-300 bg-slate-100 text-slate-700";
  if (normalized === "ANULADO" || normalized === "CANCELADA") return "border-rose-300 bg-rose-50 text-rose-700";
  if (normalized === "PENDIENTE") return "border-amber-300 bg-amber-50 text-amber-700";
  return "border-slate-300 bg-slate-50 text-slate-700";
};

const renderCellValue = (columnName, value) => {
  const rawValue = value || "-";
  if (isEstadoColumn(columnName)) {
    return (
      <Badge variant="outline" className={`text-[11px] font-semibold ${getEstadoBadgeClassName(rawValue)}`}>
        {rawValue}
      </Badge>
    );
  }
  return rawValue;
};

const toTablaPrincipalResponse = (response) => {
  const filas = Array.isArray(response?.filas) ? response.filas : [];
  const columnas = Array.isArray(response?.columnas) ? response.columnas : [];
  const totalRegistros = Number(response?.totalRegistros);

  return {
    ...response,
    columnas,
    filas,
    totalRegistros: Number.isFinite(totalRegistros) ? totalRegistros : filas.length,
  };
};

const toVehiculoBusquedaTabla = (response) => {
  const tickets = Array.isArray(response?.tickets?.filas) ? response.tickets.filas : [];
  const reservas = Array.isArray(response?.reservas?.filas) ? response.reservas.filas : [];
  const placa = String(response?.placa || "-");

  const filasTickets = tickets.map((fila) => {
    const c = fila?.columnas || {};
    return {
      columnas: {
        registro: "Ticket",
        placa,
        codigo: c.codigoTicket || "-",
        estado: c.estado || "-",
        tipoVehiculo: c.tipoVehiculo || "-",
        codigoEspacio: c.codigoEspacio || "-",
        inicio: c.horaEntrada || "-",
        fin: c.horaSalida || "-",
      },
    };
  });

  const filasReservas = reservas.map((fila) => {
    const c = fila?.columnas || {};
    return {
      columnas: {
        registro: "Reserva",
        placa,
        codigo: c.codigoReserva || "-",
        estado: c.estado || "-",
        tipoVehiculo: c.tipoVehiculo || "-",
        codigoEspacio: c.codigoEspacio || "-",
        inicio: c.horaInicio || "-",
        fin: c.horaFin || "-",
      },
    };
  });

  const filas = [...filasTickets, ...filasReservas];
  return {
    titulo: `Historial por placa: ${placa}`,
    columnas: ["registro", "placa", "codigo", "estado", "tipoVehiculo", "codigoEspacio", "inicio", "fin"],
    filas,
    totalRegistros: filas.length,
  };
};

const renderTablaDinamica = (titulo, columnas = [], filas = [], options = {}) => {
  const showTotal = options.showTotal ?? true;
  return (
    <div className="rounded-md border border-slate-300/90 bg-white">
      {titulo ? (
        <div className="flex items-center justify-between border-b px-3 py-2">
          <h3 className="text-sm font-semibold">{titulo}</h3>
          {showTotal ? <span className="text-xs text-muted-foreground">{filas.length} registros</span> : null}
        </div>
      ) : null}

      <Table className="reportes-table">
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
                      {renderCellValue(columna, columnasFila[columna])}
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

const renderBusquedaCodigo = ({ label, value, onChange, onSearch, placeholder, loading }) => {
  return (
    <div className="grid grid-cols-1 gap-2 md:grid-cols-[minmax(0,1fr)_auto] md:items-end">
      <div className="space-y-1">
        <label className="reportes-field-label">{label}</label>
        <Input className="h-9 text-xs" value={value} onChange={onChange} placeholder={placeholder} />
      </div>
      <Button size="sm" className="h-9 px-3 text-xs" onClick={onSearch} disabled={loading}>
        {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
        Buscar
      </Button>
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

  const [ticketBusquedaActiva, setTicketBusquedaActiva] = useState(false);
  const [pagoBusquedaActiva, setPagoBusquedaActiva] = useState(false);
  const [reservaBusquedaActiva, setReservaBusquedaActiva] = useState(false);
  const [vehiculoBusquedaActiva, setVehiculoBusquedaActiva] = useState(false);

  const [codigoTicket, setCodigoTicket] = useState("");

  const [codigoPagoTicket, setCodigoPagoTicket] = useState("");

  const [codigoReserva, setCodigoReserva] = useState("");

  const [placaVehiculo, setPlacaVehiculo] = useState("");

  const [loadingTickets, setLoadingTickets] = useState(false);
  const [loadingPagos, setLoadingPagos] = useState(false);
  const [loadingReservas, setLoadingReservas] = useState(false);
  const [loadingVehiculos, setLoadingVehiculos] = useState(false);
  const [loadingDetalle, setLoadingDetalle] = useState(false);
  const [loadingAnulacion, setLoadingAnulacion] = useState(false);
  const [openAnularDialog, setOpenAnularDialog] = useState(false);
  const [ticketPendienteAnulacion, setTicketPendienteAnulacion] = useState("");

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
      setTicketsListado(toTablaPrincipalResponse(data));
      setTicketsPage(page);
      setTicketBusquedaActiva(false);
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
      setPagosListado(toTablaPrincipalResponse(data));
      setPagosPage(page);
      setPagoBusquedaActiva(false);
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
      setReservasListado(toTablaPrincipalResponse(data));
      setReservasPage(page);
      setReservaBusquedaActiva(false);
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
      setVehiculosListado(toTablaPrincipalResponse(data));
      setVehiculosPage(page);
      setVehiculoBusquedaActiva(false);
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
      const response = await consultarPorTicket(value);
      setTicketsListado(toTablaPrincipalResponse(response));
      setTicketsPage(0);
      setTicketBusquedaActiva(true);
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo consultar ticket por codigo"));
    } finally {
      setLoadingDetalle(false);
    }
  };

  const handleAnularTicketDesdeFila = async (codigo) => {
    const codigoNormalizado = String(codigo || "").trim();
    if (!codigoNormalizado) {
      toast.error("No se encontro el codigo de ticket");
      return;
    }

    setTicketPendienteAnulacion(codigoNormalizado);
    setOpenAnularDialog(true);
  };

  const confirmarAnulacionTicket = async () => {
    const codigoNormalizado = String(ticketPendienteAnulacion || "").trim();
    if (!codigoNormalizado) {
      setOpenAnularDialog(false);
      return;
    }

    try {
      setLoadingAnulacion(true);
      await anularTicket(codigoNormalizado);
      toast.success("Ticket anulado correctamente");

      if (ticketBusquedaActiva && codigoTicket.trim()) {
        await buscarTicketPorCodigo();
      } else {
        await cargarTicketsListado(ticketsPage);
      }
      setOpenAnularDialog(false);
      setTicketPendienteAnulacion("");
    } catch (error) {
      toast.error(await getReportesErrorMessage(error, "No se pudo anular el ticket"));
    } finally {
      setLoadingAnulacion(false);
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
      const response = await consultarPagoPorTicket(value);
      setPagosListado(toTablaPrincipalResponse(response));
      setPagosPage(0);
      setPagoBusquedaActiva(true);
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
      const response = await consultarPorReserva(value);
      setReservasListado(toTablaPrincipalResponse(response));
      setReservasPage(0);
      setReservaBusquedaActiva(true);
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
      const response = await consultarPorPlaca(value);
      setVehiculosListado(toVehiculoBusquedaTabla(response));
      setVehiculosPage(0);
      setVehiculoBusquedaActiva(true);
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

  const loading = loadingTickets || loadingPagos || loadingReservas || loadingVehiculos || loadingDetalle || loadingAnulacion;

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
      <Dialog open={openAnularDialog} onOpenChange={setOpenAnularDialog}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Anular ticket</DialogTitle>
            <DialogDescription>
              Esta accion anulara el ticket {ticketPendienteAnulacion || "-"} y liberara su espacio.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                if (loadingAnulacion) return;
                setOpenAnularDialog(false);
              }}
            >
              Cancelar
            </Button>
            <Button
              className="bg-rose-600 text-white hover:bg-rose-700"
              onClick={confirmarAnulacionTicket}
              disabled={loadingAnulacion}
            >
              {loadingAnulacion ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
              Confirmar anulacion
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

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

      <Tabs value={seccionActiva} onValueChange={setSeccionActiva} className="space-y-3">
        <TabsList className="reportes-tabs grid w-full grid-cols-2 md:grid-cols-4">
          <TabsTrigger value="tickets">Tickets</TabsTrigger>
          <TabsTrigger value="pagos">Pagos</TabsTrigger>
          <TabsTrigger value="reservas">Reservas</TabsTrigger>
          <TabsTrigger value="vehiculo">Vehiculo</TabsTrigger>
        </TabsList>

        <TabsContent value="tickets" className="space-y-3">
          <div className="reportes-panel space-y-2.5">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Tickets</h2>
              <Button size="sm" variant="outline" onClick={() => cargarTicketsListado(ticketsPage)} disabled={loadingTickets}>
                <RefreshCw className={`h-4 w-4 ${loadingTickets ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>

            {renderBusquedaCodigo({
              label: "Codigo de ticket",
              value: codigoTicket,
              onChange: (e) => setCodigoTicket(e.target.value),
              onSearch: buscarTicketPorCodigo,
              placeholder: "Ej: T-1712433330000",
              loading: loadingDetalle,
            })}

            <div className="rounded-md border border-slate-300/90 bg-white">
              <Table className="reportes-table">
                <TableHeader>
                  <TableRow>
                    {(ticketsListado?.columnas || []).map((columna) => (
                      <TableHead key={columna} className="h-9 px-2">
                        {columna}
                      </TableHead>
                    ))}
                    <TableHead className="h-9 px-2 text-right">Acciones</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {!ticketsListado?.filas?.length ? (
                    <TableRow>
                      <TableCell colSpan={Math.max((ticketsListado?.columnas || []).length + 1, 1)} className="py-6 text-center text-xs text-muted-foreground">
                        Sin resultados.
                      </TableCell>
                    </TableRow>
                  ) : (
                    ticketsListado.filas.map((fila, index) => {
                      const columnasFila = fila?.columnas || {};
                      const estadoFila = String(columnasFila.estado || "").toUpperCase();
                      const codigoFila = columnasFila.codigoTicket;
                      const canAnularFila = estadoFila === "ACTIVO" && Boolean(codigoFila);

                      return (
                        <TableRow key={`row-ticket-${index}`}>
                          {(ticketsListado?.columnas || []).map((columna) => (
                            <TableCell key={`${index}-${columna}`} className="px-2 py-2">
                              {renderCellValue(columna, columnasFila[columna])}
                            </TableCell>
                          ))}
                          <TableCell className="px-2 py-2 text-right">
                            {canAnularFila ? (
                              <Button
                                size="sm"
                                variant="outline"
                                className="h-7 border-rose-300 px-2 text-[11px] text-rose-700 hover:bg-rose-50"
                                onClick={() => handleAnularTicketDesdeFila(codigoFila)}
                                disabled={loadingAnulacion}
                              >
                                {loadingAnulacion ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : null}
                                Anular
                              </Button>
                            ) : (
                              <span className="text-[11px] text-muted-foreground">-</span>
                            )}
                          </TableCell>
                        </TableRow>
                      );
                    })
                  )}
                </TableBody>
              </Table>
            </div>
            {!ticketBusquedaActiva ? renderPagination(
              ticketsPage,
              getTotalRegistros(ticketsListado),
              () => cargarTicketsListado(ticketsPage - 1),
              () => cargarTicketsListado(ticketsPage + 1),
              loadingTickets
            ) : null}
          </div>
        </TabsContent>

        <TabsContent value="pagos" className="space-y-3">
          <div className="reportes-panel space-y-2.5">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Pagos</h2>
              <Button size="sm" variant="outline" onClick={() => cargarPagosListado(pagosPage)} disabled={loadingPagos}>
                <RefreshCw className={`h-4 w-4 ${loadingPagos ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>

            {renderBusquedaCodigo({
              label: "Codigo de ticket",
              value: codigoPagoTicket,
              onChange: (e) => setCodigoPagoTicket(e.target.value),
              onSearch: buscarPagoPorCodigo,
              placeholder: "Ej: T-1712433330000",
              loading: loadingDetalle,
            })}

            {renderTablaDinamica(null, pagosListado?.columnas || [], pagosListado?.filas || [])}
            {!pagoBusquedaActiva ? renderPagination(
              pagosPage,
              getTotalRegistros(pagosListado),
              () => cargarPagosListado(pagosPage - 1),
              () => cargarPagosListado(pagosPage + 1),
              loadingPagos
            ) : null}
          </div>
        </TabsContent>

        <TabsContent value="reservas" className="space-y-3">
          <div className="reportes-panel space-y-2.5">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Reservas</h2>
              <Button size="sm" variant="outline" onClick={() => cargarReservasListado(reservasPage)} disabled={loadingReservas}>
                <RefreshCw className={`h-4 w-4 ${loadingReservas ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>

            {renderBusquedaCodigo({
              label: "Codigo de reserva",
              value: codigoReserva,
              onChange: (e) => setCodigoReserva(e.target.value),
              onSearch: buscarReservaPorCodigo,
              placeholder: "Ej: R-1712433330000",
              loading: loadingDetalle,
            })}

            {renderTablaDinamica(null, reservasListado?.columnas || [], reservasListado?.filas || [])}
            {!reservaBusquedaActiva ? renderPagination(
              reservasPage,
              getTotalRegistros(reservasListado),
              () => cargarReservasListado(reservasPage - 1),
              () => cargarReservasListado(reservasPage + 1),
              loadingReservas
            ) : null}
          </div>
        </TabsContent>

        <TabsContent value="vehiculo" className="space-y-3">
          <div className="reportes-panel space-y-2.5">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Vehiculo</h2>
              <Button size="sm" variant="outline" onClick={() => cargarVehiculosListado(vehiculosPage)} disabled={loadingVehiculos}>
                <RefreshCw className={`h-4 w-4 ${loadingVehiculos ? "animate-spin" : ""}`} />
                Actualizar
              </Button>
            </div>

            {renderBusquedaCodigo({
              label: "Placa",
              value: placaVehiculo,
              onChange: (e) => setPlacaVehiculo(e.target.value),
              onSearch: buscarVehiculoPorCodigo,
              placeholder: "Ej: A123456",
              loading: loadingDetalle,
            })}

            {renderTablaDinamica(null, vehiculosListado?.columnas || [], vehiculosListado?.filas || [])}
            {!vehiculoBusquedaActiva ? renderPagination(
              vehiculosPage,
              getTotalRegistros(vehiculosListado),
              () => cargarVehiculosListado(vehiculosPage - 1),
              () => cargarVehiculosListado(vehiculosPage + 1),
              loadingVehiculos
            ) : null}
          </div>
        </TabsContent>
      </Tabs>
    </ReportesPageShell>
  );
};
