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
  getCapacidadActivaInactiva,
  getOcupacionGlobal,
  getOcupacionPorTipo,
  getReportesErrorMessage,
  getUtilizacionPorEspacio,
} from "../../api/reportes";
import { ReportesFiltrosBar } from "../../components/reportes/ReportesFiltrosBar";
import { ReportesFetchState } from "../../components/reportes/ReportesFetchState";
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

const parseKpisByCode = (kpis = []) => {
  return new Map((Array.isArray(kpis) ? kpis : []).map((item) => [item.codigo, item.valor]));
};

export const ReportesOcupacionPage = () => {
  const [fechaDesde, setFechaDesde] = useState(startOfTodayInput());
  const [fechaHasta, setFechaHasta] = useState(nowInput());
  const [loading, setLoading] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [canRetry, setCanRetry] = useState(false);

  const [ocupacionGlobalKpis, setOcupacionGlobalKpis] = useState([]);
  const [capacidadKpis, setCapacidadKpis] = useState([]);
  const [ocupacionTipoRows, setOcupacionTipoRows] = useState([]);
  const [utilizacionRows, setUtilizacionRows] = useState([]);

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

      const [globalResp, tipoResp, capacidadResp, utilizacionResp] = await Promise.all([
        getOcupacionGlobal(),
        getOcupacionPorTipo(),
        getCapacidadActivaInactiva(),
        getUtilizacionPorEspacio(paramsRango),
      ]);

      setOcupacionGlobalKpis(Array.isArray(globalResp?.kpis) ? globalResp.kpis : []);
      setCapacidadKpis(Array.isArray(capacidadResp?.kpis) ? capacidadResp.kpis : []);
      setOcupacionTipoRows(Array.isArray(tipoResp?.filas) ? tipoResp.filas : []);
      setUtilizacionRows(Array.isArray(utilizacionResp?.filas) ? utilizacionResp.filas : []);
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

  const resumen = useMemo(() => {
    const globalMap = parseKpisByCode(ocupacionGlobalKpis);
    const capMap = parseKpisByCode(capacidadKpis);

    return {
      ocupacionPorcentaje: toNumber(globalMap.get("OCUP_PORCENTAJE")),
      ocupados: toNumber(globalMap.get("OCUP_OCUPADOS")),
      libres: toNumber(globalMap.get("OCUP_LIBRES")),
      activos: toNumber(globalMap.get("OCUP_TOTAL_ACTIVOS")),
      capacidadTotal: toNumber(capMap.get("CAP_TOTAL")),
      capacidadActiva: toNumber(capMap.get("CAP_ACTIVA")),
      capacidadInactiva: toNumber(capMap.get("CAP_INACTIVA")),
    };
  }, [ocupacionGlobalKpis, capacidadKpis]);

  const ocupacionTipoData = useMemo(() => {
    return ocupacionTipoRows.map((fila) => {
      const c = fila?.columnas || {};
      return {
        tipo: c.tipoVehiculo || "-",
        ocupados: toNumber(c.ocupados),
        libres: toNumber(c.libres),
      };
    });
  }, [ocupacionTipoRows]);

  const capacidadData = useMemo(() => {
    return [
      { estado: "Activa", cantidad: resumen.capacidadActiva },
      { estado: "Inactiva", cantidad: resumen.capacidadInactiva },
    ];
  }, [resumen.capacidadActiva, resumen.capacidadInactiva]);

  const limpiarFiltros = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
  };

  return (
    <section className="space-y-4">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold tracking-tight">Reportes de Ocupacion</h1>
        <p className="text-sm text-muted-foreground">
          Vista compacta de ocupacion global, capacidad y utilizacion por espacio.
        </p>
      </header>

      <ReportesFiltrosBar
        fechaDesde={fechaDesde}
        fechaHasta={fechaHasta}
        onFechaDesdeChange={setFechaDesde}
        onFechaHastaChange={setFechaHasta}
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
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Ocupados</p>
          <p className="text-lg font-semibold">{resumen.ocupados}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Libres</p>
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

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <div className="rounded-lg border bg-card p-3">
          <h2 className="mb-2 text-sm font-semibold">Ocupacion por tipo de vehiculo</h2>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={ocupacionTipoData} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                <XAxis dataKey="tipo" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={30} />
                <Tooltip />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="ocupados" name="Ocupados" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                <Bar dataKey="libres" name="Libres" fill="#94a3b8" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-lg border bg-card p-3">
          <h2 className="mb-2 text-sm font-semibold">Capacidad activa vs inactiva</h2>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={capacidadData} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                <XAxis dataKey="estado" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={30} />
                <Tooltip />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="cantidad" name="Espacios" fill="#0f172a" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
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
    </section>
  );
};
