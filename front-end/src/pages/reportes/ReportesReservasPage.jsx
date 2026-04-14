import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import {
  getCancelacionesDetalleReporte,
  getReservasPorEstadoReporte,
  getReservasProximasReporte,
} from "../../api/reportesReservas";
import { getReportesErrorMessage } from "../../api/reportesUtils";
import { getUsuarios } from "../../api/usuarios";
import { ReportesContextBar } from "../../components/reportes/ReportesContextBar";
import { ReportesFetchState } from "../../components/reportes/ReportesFetchState";
import { ReportesPageShell } from "../../components/reportes/ReportesPageShell";
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

const toNumber = (value) => Number(value || 0);

const KPI_ORDER = [
  { codigo: "RES_PENDIENTES", key: "pendientes", label: "Pendientes" },
  { codigo: "RES_ACTIVAS", key: "activas", label: "Activas" },
  { codigo: "RES_FINALIZADAS", key: "finalizadas", label: "Finalizadas" },
  { codigo: "RES_CANCELADAS", key: "canceladas", label: "Canceladas" },
];

export const ReportesReservasPage = () => {
  const [fechaDesde, setFechaDesde] = useState(startOfTodayInput());
  const [fechaHasta, setFechaHasta] = useState(nowInput());
  const [granularidad, setGranularidad] = useState("dia");
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState("TODOS");
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [canRetry, setCanRetry] = useState(false);

  const [kpisRaw, setKpisRaw] = useState([]);
  const [cancelaciones, setCancelaciones] = useState([]);
  const [reservasProximas, setReservasProximas] = useState([]);

  const cargarDatos = async () => {
    try {
      if (fechaDesde && fechaHasta && fechaHasta < fechaDesde) {
        toast.error("La fecha hasta no puede ser menor que la fecha desde");
        return;
      }

      const minutos = 30;

      setErrorMessage("");
      setCanRetry(false);
      setLoading(true);
      const paramsRango = {
        fechaDesde: toApiLocalDateTime(fechaDesde),
        fechaHasta: toApiLocalDateTime(fechaHasta),
      };

      const [estadoResp, cancelacionesResp, proximasResp] = await Promise.all([
        getReservasPorEstadoReporte(paramsRango),
        getCancelacionesDetalleReporte(paramsRango),
        getReservasProximasReporte(minutos),
      ]);

      setKpisRaw(Array.isArray(estadoResp?.kpis) ? estadoResp.kpis : []);
      setCancelaciones(Array.isArray(cancelacionesResp?.filas) ? cancelacionesResp.filas : []);
      setReservasProximas(Array.isArray(proximasResp?.filas) ? proximasResp.filas : []);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudieron cargar los reportes de reservas");
      setErrorMessage(message);
      setCanRetry(!error?.response);
      toast.error(message);
    } finally {
      setLoading(false);
      setHasLoadedOnce(true);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

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

  const kpis = useMemo(() => {
    const byCode = new Map(kpisRaw.map((item) => [item.codigo, toNumber(item.valor)]));
    return KPI_ORDER.map((item) => ({
      ...item,
      value: byCode.get(item.codigo) || 0,
    }));
  }, [kpisRaw]);

  const chartData = useMemo(
    () => kpis.map((item) => ({ estado: item.label, cantidad: item.value })),
    [kpis]
  );

  const limpiarFiltros = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
    setGranularidad("dia");
    setUsuarioSeleccionado("TODOS");
  };

  return (
    <ReportesPageShell
      title="Reportes de Reservas"
      subtitle="Vista compacta de estados, cancelaciones y reservas proximas."
      actions={null}
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
        onLimpiar={limpiarFiltros}
        onActualizar={cargarDatos}
        loading={loading}
      />

      <ReportesFetchState
        loading={loading && !hasLoadedOnce}
        loadingText="Cargando reportes de reservas..."
        errorMessage={errorMessage}
        canRetry={canRetry}
        onRetry={cargarDatos}
      />

      <div className="grid grid-cols-2 gap-2 md:grid-cols-4">
        {kpis.map((kpi) => (
          <div key={kpi.key} className="rounded-md border px-3 py-2">
            <p className="text-[11px] uppercase tracking-wide text-muted-foreground">{kpi.label}</p>
            <p className="text-lg font-semibold">{kpi.value}</p>
          </div>
        ))}
      </div>

      <div className="rounded-lg border bg-card p-3">
        <h2 className="mb-2 text-sm font-semibold">Reservas por estado</h2>
        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
              <XAxis dataKey="estado" tick={{ fontSize: 11 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={30} />
              <Tooltip />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Bar dataKey="cantidad" name="Cantidad" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <div className="rounded-lg border bg-card p-3">
          <div className="mb-2 flex items-center justify-between">
            <h2 className="text-sm font-semibold">Cancelaciones con motivo</h2>
            <span className="text-xs text-muted-foreground">{cancelaciones.length} registros</span>
          </div>
          <Table className="text-xs">
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-2">Reserva</TableHead>
                <TableHead className="h-9 px-2">Placa</TableHead>
                <TableHead className="h-9 px-2">Espacio</TableHead>
                <TableHead className="h-9 px-2">Cancelado por</TableHead>
                <TableHead className="h-9 px-2">Motivo</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!cancelaciones.length ? (
                <TableRow>
                  <TableCell colSpan={5} className="py-6 text-center text-xs text-muted-foreground">
                    No hay cancelaciones en el rango seleccionado.
                  </TableCell>
                </TableRow>
              ) : (
                cancelaciones.slice(0, 10).map((fila, idx) => {
                  const c = fila?.columnas || {};
                  return (
                    <TableRow key={`${c.codigoReserva || "cancel"}-${idx}`}>
                      <TableCell className="px-2 py-2 font-medium">{c.codigoReserva || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{c.placa || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{c.codigoEspacio || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{c.canceladoPor || "SIN_USUARIO"}</TableCell>
                      <TableCell className="px-2 py-2 max-w-[220px] truncate" title={c.motivoCancelacion || "-"}>
                        {c.motivoCancelacion || "-"}
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>

        <div className="rounded-lg border bg-card p-3">
          <div className="mb-2 flex items-center justify-between">
            <h2 className="text-sm font-semibold">Reservas proximas</h2>
            <span className="text-xs text-muted-foreground">Ventana fija: 30 min</span>
          </div>
          <Table className="text-xs">
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-2">Reserva</TableHead>
                <TableHead className="h-9 px-2">Cliente</TableHead>
                <TableHead className="h-9 px-2">Placa</TableHead>
                <TableHead className="h-9 px-2">Inicio</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!reservasProximas.length ? (
                <TableRow>
                  <TableCell colSpan={4} className="py-6 text-center text-xs text-muted-foreground">
                    No hay reservas proximas para la ventana configurada.
                  </TableCell>
                </TableRow>
              ) : (
                reservasProximas.slice(0, 10).map((fila, idx) => {
                  const c = fila?.columnas || {};
                  return (
                    <TableRow key={`${c.codigoReserva || "prox"}-${idx}`}>
                      <TableCell className="px-2 py-2 font-medium">{c.codigoReserva || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{c.cliente || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{c.placa || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{c.horaInicio || "-"}</TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </div>
    </ReportesPageShell>
  );
};
