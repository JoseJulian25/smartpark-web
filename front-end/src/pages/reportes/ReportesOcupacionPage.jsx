import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import {
  getTendenciaUsoPorEspacio,
  getUtilizacionPorEspacio,
} from "../../api/reportesOcupacion";
import { getReportesErrorMessage } from "../../api/reportesUtils";
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

const LINE_COLORS = ["#0f172a", "#0ea5e9", "#16a34a", "#f59e0b", "#ef4444", "#7c3aed", "#14b8a6", "#e11d48"];

export const ReportesOcupacionPage = () => {
  const [fechaDesde, setFechaDesde] = useState(startOfTodayInput());
  const [fechaHasta, setFechaHasta] = useState(nowInput());
  const [granularidad, setGranularidad] = useState("dia");
  const [loading, setLoading] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [canRetry, setCanRetry] = useState(false);

  const [utilizacionRows, setUtilizacionRows] = useState([]);
  const [tendenciaRows, setTendenciaRows] = useState([]);

  const cargarDatos = async () => {
    try {
      if (fechaDesde && fechaHasta && fechaHasta < fechaDesde) {
        toast.error("La fecha hasta no puede ser menor que la fecha desde");
        return;
      }

      setErrorMessage("");
      setCanRetry(false);
      setLoading(true);
      const paramsRango = {
        fechaDesde: toApiLocalDateTime(fechaDesde),
        fechaHasta: toApiLocalDateTime(fechaHasta),
      };

      const [utilizacionResp, tendenciaResp] = await Promise.all([
        getUtilizacionPorEspacio(paramsRango),
        getTendenciaUsoPorEspacio({
          ...paramsRango,
          granularidad,
          limiteEspacios: 8,
        }),
      ]);

      setUtilizacionRows(Array.isArray(utilizacionResp?.filas) ? utilizacionResp.filas : []);
      setTendenciaRows(Array.isArray(tendenciaResp?.filas) ? tendenciaResp.filas : []);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudieron cargar los reportes de ocupacion");
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
    if (!hasLoadedOnce) return;
    cargarDatos();
  }, [fechaDesde, fechaHasta, granularidad]);

  const resumen = useMemo(() => {
    const activos = utilizacionRows.filter((fila) => {
      const c = fila?.columnas || {};
      return String(c.activo || "").toUpperCase() === "SI";
    });

    const inactivos = utilizacionRows.length - activos.length;
    const ocupados = activos.filter((fila) => toNumber(fila?.columnas?.usosEnRango) > 0).length;
    const libres = Math.max(0, activos.length - ocupados);
    const porcentaje = activos.length === 0 ? 0 : (ocupados * 100) / activos.length;

    return {
      ocupacionPorcentaje: porcentaje,
      ocupados,
      libres,
      activos: activos.length,
      capacidadTotal: utilizacionRows.length,
      capacidadActiva: activos.length,
      capacidadInactiva: inactivos,
    };
  }, [utilizacionRows]);

  const tendenciaSeries = useMemo(() => {
    const totals = new Map();
    tendenciaRows.forEach((fila) => {
      const c = fila?.columnas || {};
      const codigoEspacio = c.codigoEspacio || "-";
      totals.set(codigoEspacio, (totals.get(codigoEspacio) || 0) + toNumber(c.usos));
    });

    return Array.from(totals.entries())
      .sort((a, b) => b[1] - a[1])
      .map(([codigo]) => codigo);
  }, [tendenciaRows]);

  const tendenciaChartData = useMemo(() => {
    const periodos = [...new Set(tendenciaRows.map((fila) => fila?.columnas?.periodo).filter(Boolean))].sort();

    const usoLookup = new Map();
    tendenciaRows.forEach((fila) => {
      const c = fila?.columnas || {};
      const periodo = c.periodo;
      const codigoEspacio = c.codigoEspacio;
      if (!periodo || !codigoEspacio) return;
      usoLookup.set(`${periodo}|${codigoEspacio}`, toNumber(c.usos));
    });

    return periodos.map((periodo) => {
      const row = { periodo };
      tendenciaSeries.forEach((codigoEspacio) => {
        row[codigoEspacio] = usoLookup.get(`${periodo}|${codigoEspacio}`) || 0;
      });
      return row;
    });
  }, [tendenciaRows, tendenciaSeries]);

  const limpiarFiltros = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
    setGranularidad("dia");
  };

  return (
    <ReportesPageShell
      title="Reportes de Ocupacion"
      subtitle="Vista compacta de ocupacion global, capacidad y utilizacion por espacio."
    >
      <ReportesContextBar
        fechaDesde={fechaDesde}
        fechaHasta={fechaHasta}
        onFechaDesdeChange={setFechaDesde}
        onFechaHastaChange={setFechaHasta}
        showUsuarioFilter={false}
        granularidad={granularidad}
        onGranularidadChange={setGranularidad}
        onLimpiar={limpiarFiltros}
        onActualizar={cargarDatos}
        loading={loading}
      />

      <ReportesFetchState
        loading={loading && !hasLoadedOnce}
        loadingText="Cargando reportes de ocupacion..."
        errorMessage={errorMessage}
        canRetry={canRetry}
        onRetry={cargarDatos}
      />

      <div className="grid grid-cols-2 gap-2 md:grid-cols-4 xl:grid-cols-7">
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Ocupacion global</p>
          <p className="text-lg font-semibold text-primary">{resumen.ocupacionPorcentaje.toFixed(2)}%</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Espacios con uso</p>
          <p className="text-lg font-semibold">{resumen.ocupados}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Espacios sin uso</p>
          <p className="text-lg font-semibold">{resumen.libres}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Activos</p>
          <p className="text-lg font-semibold">{resumen.activos}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Capacidad total</p>
          <p className="text-lg font-semibold">{resumen.capacidadTotal}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Capacidad activa</p>
          <p className="text-lg font-semibold">{resumen.capacidadActiva}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Capacidad inactiva</p>
          <p className="text-lg font-semibold">{resumen.capacidadInactiva}</p>
        </div>
      </div>

      <div className="rounded-lg border bg-card p-3">
        <div className="mb-2 flex items-center justify-between gap-2">
          <h2 className="text-sm font-semibold">Tendencia de uso por espacio</h2>
          <span className="text-xs text-muted-foreground">Top {Math.min(tendenciaSeries.length, 8)} espacios por uso</span>
        </div>

        {!tendenciaChartData.length ? (
          <div className="flex h-72 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground">
            No hay datos de tendencia para el rango seleccionado.
          </div>
        ) : (
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={tendenciaChartData} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                <XAxis dataKey="periodo" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={30} />
                <Tooltip />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                {tendenciaSeries.map((codigoEspacio, index) => (
                  <Line
                    key={codigoEspacio}
                    type="monotone"
                    dataKey={codigoEspacio}
                    name={codigoEspacio}
                    stroke={LINE_COLORS[index % LINE_COLORS.length]}
                    strokeWidth={2}
                    dot={false}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="rounded-lg border bg-card p-3">
        <div className="mb-2 flex items-center justify-between">
          <h2 className="text-sm font-semibold">Utilizacion por espacio</h2>
          <span className="text-xs text-muted-foreground">{utilizacionRows.length} espacios</span>
        </div>

        <Table className="text-xs">
          <TableHeader>
            <TableRow>
              <TableHead className="h-9 px-2">Espacio</TableHead>
              <TableHead className="h-9 px-2">Tipo</TableHead>
              <TableHead className="h-9 px-2">Estado</TableHead>
              <TableHead className="h-9 px-2">Activo</TableHead>
              <TableHead className="h-9 px-2">Usos en rango</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {!utilizacionRows.length ? (
              <TableRow>
                <TableCell colSpan={5} className="py-6 text-center text-xs text-muted-foreground">
                  No hay datos de utilizacion para el rango seleccionado.
                </TableCell>
              </TableRow>
            ) : (
              utilizacionRows.slice(0, 30).map((fila, idx) => {
                const c = fila?.columnas || {};
                return (
                  <TableRow key={`${c.codigoEspacio || "espacio"}-${idx}`}>
                    <TableCell className="px-2 py-2 font-medium">{c.codigoEspacio || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.tipoVehiculo || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.estadoActual || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.activo || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.usosEnRango || "0"}</TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>
    </ReportesPageShell>
  );
};
