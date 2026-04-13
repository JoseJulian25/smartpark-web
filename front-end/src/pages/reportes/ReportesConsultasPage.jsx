import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { Search, Loader2 } from "lucide-react";

import {
  getConsultasPorRangoMontos,
  getHistorialConsolidadoCliente,
  getTrazabilidadTicket,
  consultarPorReserva,
  getReportesErrorMessage,
} from "../../api/reportes";
import { getUsuarios } from "../../api/usuarios";
import { ReportesContextBar } from "../../components/reportes/ReportesContextBar";
import { ReportesPageShell } from "../../components/reportes/ReportesPageShell";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
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

const HISTORIAL_PAGE_SIZE = 20;
const MONTOS_PAGE_SIZE = 20;

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
  const [granularidad, setGranularidad] = useState("dia");
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState("TODOS");
  const [usuarios, setUsuarios] = useState([]);

  const [placa, setPlaca] = useState("");
  const [codigoTicket, setCodigoTicket] = useState("");
  const [codigoReserva, setCodigoReserva] = useState("");
  const [montoDesde, setMontoDesde] = useState("");
  const [montoHasta, setMontoHasta] = useState("");

  const [loadingHistorial, setLoadingHistorial] = useState(false);
  const [loadingTicket, setLoadingTicket] = useState(false);
  const [loadingReserva, setLoadingReserva] = useState(false);
  const [loadingMontos, setLoadingMontos] = useState(false);

  const [resultadoHistorial, setResultadoHistorial] = useState(null);
  const [resultadoTicket, setResultadoTicket] = useState(null);
  const [resultadoReserva, setResultadoReserva] = useState(null);
  const [resultadoMontos, setResultadoMontos] = useState(null);

  const [historialPage, setHistorialPage] = useState(0);
  const [montosPage, setMontosPage] = useState(0);

  useEffect(() => {
    const cargarUsuarios = async () => {
      try {
        const data = await getUsuarios();
        setUsuarios(Array.isArray(data) ? data : []);
      } catch (error) {
        const message = await getReportesErrorMessage(error, "No se pudo cargar la lista de usuarios");
        toast.error(message);
      }
    };

    cargarUsuarios();
  }, []);

  const buscarHistorialPorPlaca = async (targetPage = 0) => {
    const value = placa.trim().toUpperCase();
    if (!value) {
      toast.error("Ingresa una placa para consultar");
      return;
    }

    try {
      setLoadingHistorial(true);
      const data = await getHistorialConsolidadoCliente({
        placa: value,
        page: targetPage,
        size: HISTORIAL_PAGE_SIZE,
      });
      setResultadoHistorial(data);
      setHistorialPage(targetPage);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo consultar historial por placa");
      toast.error(message);
    } finally {
      setLoadingHistorial(false);
    }
  };

  const buscarTrazabilidadTicket = async () => {
    const value = codigoTicket.trim();
    if (!value) {
      toast.error("Ingresa un codigo de ticket");
      return;
    }

    try {
      setLoadingTicket(true);
      const data = await getTrazabilidadTicket(value);
      setResultadoTicket(data);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo consultar trazabilidad de ticket");
      toast.error(message);
    } finally {
      setLoadingTicket(false);
    }
  };

  const buscarPorReserva = async () => {
    const value = codigoReserva.trim();
    if (!value) {
      toast.error("Ingresa un codigo de reserva");
      return;
    }

    try {
      setLoadingReserva(true);
      const data = await consultarPorReserva(value);
      setResultadoReserva(data);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo consultar por reserva");
      toast.error(message);
    } finally {
      setLoadingReserva(false);
    }
  };

  const buscarPorRangoMontos = async (targetPage = 0) => {
    try {
      setLoadingMontos(true);
      const data = await getConsultasPorRangoMontos({
        montoDesde: montoDesde.trim() ? Number(montoDesde) : undefined,
        montoHasta: montoHasta.trim() ? Number(montoHasta) : undefined,
        fechaDesde: toApiLocalDateTime(fechaDesde),
        fechaHasta: toApiLocalDateTime(fechaHasta),
        page: targetPage,
        size: MONTOS_PAGE_SIZE,
      });
      setResultadoMontos(data);
      setMontosPage(targetPage);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo consultar por rango de montos");
      toast.error(message);
    } finally {
      setLoadingMontos(false);
    }
  };

  const construirTimelineReserva = () => {
    const row = resultadoReserva?.filas?.[0]?.columnas || null;
    if (!row) return [];

    const eventos = [];
    eventos.push({
      fechaEvento: row.horaInicio || "-",
      tipoEvento: "RESERVA_INICIO",
      codigo: row.codigoReserva || "-",
      estado: row.estado || "-",
      detalle: `Espacio: ${row.codigoEspacio || "-"} | Tipo: ${row.tipoVehiculo || "-"}`,
      monto: "-",
    });

    if (row.horaFin && row.horaFin !== "-") {
      eventos.push({
        fechaEvento: row.horaFin,
        tipoEvento: "RESERVA_FIN",
        codigo: row.codigoReserva || "-",
        estado: row.estado || "-",
        detalle: `Motivo cancelacion: ${row.motivoCancelacion || "-"}`,
        monto: "-",
      });
    }

    return eventos;
  };

  const limpiarFiltrosContexto = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
    setGranularidad("dia");
    setUsuarioSeleccionado("TODOS");
  };

  const actualizarContexto = async () => {
    const tareas = [];
    if (placa.trim()) tareas.push(buscarHistorialPorPlaca(historialPage));
    if (codigoTicket.trim()) tareas.push(buscarTrazabilidadTicket());
    if (codigoReserva.trim()) tareas.push(buscarPorReserva());
    if (montoDesde.trim() || montoHasta.trim() || resultadoMontos) {
      tareas.push(buscarPorRangoMontos(montosPage));
    }

    if (!tareas.length) {
      toast("No hay consultas activas para actualizar");
      return;
    }

    await Promise.all(tareas);
  };

  const loading = loadingHistorial || loadingTicket || loadingReserva || loadingMontos;

  const historialTotal = getTotalRegistros(resultadoHistorial);
  const historialCanPrev = historialPage > 0;
  const historialCanNext = (historialPage + 1) * HISTORIAL_PAGE_SIZE < historialTotal;

  const montosTotal = getTotalRegistros(resultadoMontos);
  const montosCanPrev = montosPage > 0;
  const montosCanNext = (montosPage + 1) * MONTOS_PAGE_SIZE < montosTotal;

  const timelineEventos = [
    ...(resultadoTicket?.filas || []).map((row) => row?.columnas || {}),
    ...construirTimelineReserva(),
    ...(resultadoHistorial?.filas || []).slice(0, 20).map((row) => row?.columnas || {}),
  ];

  return (
    <ReportesPageShell
      title="Consultas"
      subtitle="Consultas avanzadas por placa, ticket, reserva y montos con timeline compacto de eventos."
    >
      <ReportesContextBar
        fechaDesde={fechaDesde}
        fechaHasta={fechaHasta}
        onFechaDesdeChange={setFechaDesde}
        onFechaHastaChange={setFechaHasta}
        granularidad={granularidad}
        onGranularidadChange={setGranularidad}
        usuarioSeleccionado={usuarioSeleccionado}
        onUsuarioSeleccionadoChange={setUsuarioSeleccionado}
        usuarios={usuarios}
        onLimpiar={limpiarFiltrosContexto}
        onActualizar={actualizarContexto}
        loading={loading}
      />

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <div className="space-y-3 rounded-lg border bg-card p-3">
          <h2 className="text-sm font-semibold">Historial consolidado por placa</h2>
          <div className="flex gap-2">
            <Input
              value={placa}
              onChange={(event) => setPlaca(event.target.value)}
              placeholder="Ej: A123456"
            />
            <Button size="sm" onClick={() => buscarHistorialPorPlaca(0)} disabled={loadingHistorial}>
              {loadingHistorial ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
            </Button>
          </div>

          {resultadoHistorial && (
            <div className="space-y-3">
              <p className="text-xs text-muted-foreground">
                Total eventos: <span className="font-semibold text-foreground">{historialTotal}</span>
              </p>
              {renderTablaDinamica(
                resultadoHistorial?.titulo || "Historial consolidado",
                resultadoHistorial?.columnas || [],
                resultadoHistorial?.filas || []
              )}
              <div className="flex items-center justify-end gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => buscarHistorialPorPlaca(historialPage - 1)}
                  disabled={!historialCanPrev || loadingHistorial}
                >
                  Anterior
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => buscarHistorialPorPlaca(historialPage + 1)}
                  disabled={!historialCanNext || loadingHistorial}
                >
                  Siguiente
                </Button>
              </div>
            </div>
          )}
        </div>

        <div className="space-y-3 rounded-lg border bg-card p-3">
          <h2 className="text-sm font-semibold">Trazabilidad ticket y detalle de reserva</h2>
          <div className="flex gap-2">
            <Input
              value={codigoTicket}
              onChange={(event) => setCodigoTicket(event.target.value)}
              placeholder="Ej: T-1712433330000"
            />
            <Button size="sm" onClick={buscarTrazabilidadTicket} disabled={loadingTicket}>
              {loadingTicket ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
            </Button>
          </div>

          <div className="flex gap-2">
            <Input
              value={codigoReserva}
              onChange={(event) => setCodigoReserva(event.target.value)}
              placeholder="Ej: R-1712433330000"
            />
            <Button size="sm" onClick={buscarPorReserva} disabled={loadingReserva}>
              {loadingReserva ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
            </Button>
          </div>

          {resultadoTicket &&
            renderTablaDinamica(
              resultadoTicket?.titulo || "Trazabilidad de ticket",
              resultadoTicket?.columnas || [],
              resultadoTicket?.filas || []
            )}

          {resultadoReserva &&
            renderTablaDinamica(
              resultadoReserva?.titulo || "Detalle de reserva",
              resultadoReserva?.columnas || [],
              resultadoReserva?.filas || []
            )}
        </div>

        <div className="space-y-3 rounded-lg border bg-card p-3">
          <h2 className="text-sm font-semibold">Consulta por rango de montos y fechas</h2>
          <div className="grid grid-cols-2 gap-2">
            <Input
              value={montoDesde}
              onChange={(event) => setMontoDesde(event.target.value)}
              placeholder="Monto desde"
              type="number"
              min="0"
              step="0.01"
            />
            <Input
              value={montoHasta}
              onChange={(event) => setMontoHasta(event.target.value)}
              placeholder="Monto hasta"
              type="number"
              min="0"
              step="0.01"
            />
          </div>

          <div className="flex justify-end">
            <Button size="sm" onClick={() => buscarPorRangoMontos(0)} disabled={loadingMontos}>
              {loadingMontos ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />} Consultar
            </Button>
          </div>

          {resultadoMontos && (
            <div className="space-y-3">
              {renderTablaDinamica(
                resultadoMontos?.titulo || "Pagos por rango",
                resultadoMontos?.columnas || [],
                resultadoMontos?.filas || []
              )}
              <div className="flex items-center justify-end gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => buscarPorRangoMontos(montosPage - 1)}
                  disabled={!montosCanPrev || loadingMontos}
                >
                  Anterior
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => buscarPorRangoMontos(montosPage + 1)}
                  disabled={!montosCanNext || loadingMontos}
                >
                  Siguiente
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="rounded-lg border bg-card p-3 space-y-2">
        <h2 className="text-sm font-semibold">Timeline compacto de eventos ticket/reserva</h2>
        {!timelineEventos.length ? (
          <p className="text-xs text-muted-foreground">Realiza una consulta para visualizar eventos en formato timeline.</p>
        ) : (
          <div className="space-y-2">
            {timelineEventos.slice(0, 24).map((evento, index) => (
              <div key={`timeline-${index}`} className="rounded-md border px-2 py-2">
                <div className="flex flex-wrap items-center justify-between gap-1">
                  <p className="text-xs font-semibold">{evento.tipoEvento || "EVENTO"}</p>
                  <span className="text-[11px] text-muted-foreground">{evento.fechaEvento || "-"}</span>
                </div>
                <p className="text-xs">Codigo: {evento.codigo || "-"} | Estado: {evento.estado || "-"}</p>
                <p className="text-xs text-muted-foreground">{evento.detalle || "Sin detalle"}</p>
                <p className="text-xs">Monto: {evento.monto || "-"}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </ReportesPageShell>
  );
};
