import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  Bar,
  BarChart,
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
  exportarResumenEjecutivoPdf,
  getIngresosPorMetodoPago,
  getIngresosPorPeriodo,
  getIngresosPorTipoVehiculoFinanciero,
  getPromediosFinancieros,
  getRankingHorasPicoPorIngreso,
  getReportesErrorMessage,
  transformIndicadoresFinancieros,
  transformSerieTemporalToChart,
  transformTopNToChart,
} from "../../api/reportes";
import { getUsuarios } from "../../api/usuarios";
import { ReportesExportDialog } from "../../components/reportes/ReportesExportDialog";
import { ReportesContextBar } from "../../components/reportes/ReportesContextBar";
import { ReportesFetchState } from "../../components/reportes/ReportesFetchState";
import { ReportesPageShell } from "../../components/reportes/ReportesPageShell";
import { Button } from "../../components/ui/button";
import { useExportProgress } from "../../hooks/reportes/useExportProgress";
import { buildProfessionalReportFileName, triggerFileDownload } from "../../lib/download";
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

const formatApiLocalDateTimeFromDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
};

const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const GRANULARIDADES_FINANCIERAS = [
  { value: "dia", label: "Dia" },
  { value: "semana", label: "Semana" },
  { value: "mes", label: "Mes" },
];

const normalizarGranularidadFinanciera = (value) => {
  if (value === "semana" || value === "mes") return value;
  return "dia";
};

const sumIngresos = (serie = []) => {
  return serie.reduce((acc, item) => acc + toNumber(item.valor), 0);
};

const formatMoney = (value, currency = "DOP") => {
  return new Intl.NumberFormat("es-DO", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(toNumber(value));
};

const formatMinutes = (value) => {
  return `${toNumber(value).toFixed(2)} min`;
};

const calcularVariacion = (actual, comparado) => {
  if (comparado === 0) {
    return actual === 0 ? 0 : 100;
  }
  return ((actual - comparado) / Math.abs(comparado)) * 100;
};

const getKpiIndicator = (indicadores, code) => {
  return indicadores.find((item) => item.codigo === code)?.valor || 0;
};

const tableRowsFromResponse = (response = {}, labelKey, valueKey) => {
  const filas = Array.isArray(response?.filas) ? response.filas : [];
  return filas.map((row) => ({
    etiqueta: row?.columnas?.[labelKey] || "-",
    valor: toNumber(row?.columnas?.[valueKey]),
    moneda: row?.columnas?.moneda || "DOP",
  }));
};

const FINANCIEROS_EXPORT_OPTIONS = [
  {
    value: "pdf_resumen_ejecutivo",
    label: "PDF resumen ejecutivo",
    extension: "pdf",
    tipoReporte: "financieros_resumen_ejecutivo",
  },
];

export const ReportesFinancierosPage = () => {
  const [fechaDesde, setFechaDesde] = useState(startOfTodayInput());
  const [fechaHasta, setFechaHasta] = useState(nowInput());
  const [granularidad, setGranularidad] = useState("dia");
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState("TODOS");
  const [usuarios, setUsuarios] = useState([]);

  const [loading, setLoading] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [canRetry, setCanRetry] = useState(false);

  const [ingresosSerie, setIngresosSerie] = useState([]);
  const [ingresosPeriodoAnterior, setIngresosPeriodoAnterior] = useState(0);
  const [indicadores, setIndicadores] = useState([]);
  const [ingresosTipoVehiculo, setIngresosTipoVehiculo] = useState([]);
  const [ingresosMetodoPago, setIngresosMetodoPago] = useState([]);
  const [rankingHorasPico, setRankingHorasPico] = useState([]);
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [exportType, setExportType] = useState("pdf_resumen_ejecutivo");

  const { exporting, progress, runWithProgress } = useExportProgress();

  const cargarDatos = async () => {
    try {
      if (fechaDesde && fechaHasta && fechaHasta < fechaDesde) {
        toast.error("La fecha hasta no puede ser menor que la fecha desde");
        return;
      }

      setErrorMessage("");
      setCanRetry(false);
      setLoading(true);

      const params = {
        fechaDesde: toApiLocalDateTime(fechaDesde),
        fechaHasta: toApiLocalDateTime(fechaHasta),
        granularidad: normalizarGranularidadFinanciera(granularidad),
      };

      const [ingresosResp, promediosResp, tipoResp, metodoResp, rankingResp] = await Promise.all([
        getIngresosPorPeriodo(params),
        getPromediosFinancieros(params),
        getIngresosPorTipoVehiculoFinanciero(params),
        getIngresosPorMetodoPago(params),
        getRankingHorasPicoPorIngreso({
          fechaDesde: params.fechaDesde,
          fechaHasta: params.fechaHasta,
          limite: 5,
        }),
      ]);

      setIngresosSerie(transformSerieTemporalToChart(ingresosResp));
      setIndicadores(transformIndicadoresFinancieros(promediosResp));
      setIngresosTipoVehiculo(tableRowsFromResponse(tipoResp, "tipoVehiculo", "ingresos"));
      setIngresosMetodoPago(tableRowsFromResponse(metodoResp, "metodoPago", "ingresos"));
      setRankingHorasPico(transformTopNToChart(rankingResp));

      const inicioActual = new Date(fechaDesde);
      const finActual = new Date(fechaHasta);
      const rangoMs = Math.max(0, finActual.getTime() - inicioActual.getTime());

      const finAnterior = new Date(inicioActual.getTime() - 1000);
      const inicioAnterior = new Date(finAnterior.getTime() - rangoMs);

      const ingresosPrevResp = await getIngresosPorPeriodo({
        fechaDesde: formatApiLocalDateTimeFromDate(inicioAnterior),
        fechaHasta: formatApiLocalDateTimeFromDate(finAnterior),
        granularidad: normalizarGranularidadFinanciera(granularidad),
      });

      const serieAnterior = transformSerieTemporalToChart(ingresosPrevResp);
      setIngresosPeriodoAnterior(sumIngresos(serieAnterior));
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudieron cargar los reportes financieros");
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

  const ingresosTotales = useMemo(() => sumIngresos(ingresosSerie), [ingresosSerie]);

  const variacionPeriodoAnterior = useMemo(() => {
    return calcularVariacion(ingresosTotales, ingresosPeriodoAnterior);
  }, [ingresosTotales, ingresosPeriodoAnterior]);

  const ticketPromedio = useMemo(
    () => getKpiIndicator(indicadores, "TICKET_PROMEDIO"),
    [indicadores]
  );

  const estadiaPromedio = useMemo(
    () => getKpiIndicator(indicadores, "ESTADIA_PROMEDIO_MIN"),
    [indicadores]
  );

  const limpiarFiltros = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
    setGranularidad("dia");
    setUsuarioSeleccionado("TODOS");
  };

  const exportarReporte = async () => {
    const selected = FINANCIEROS_EXPORT_OPTIONS.find((option) => option.value === exportType);
    if (!selected) {
      toast.error("Selecciona un bloque de exportacion");
      return;
    }

    try {
      await runWithProgress(async () => {
        const params = {
          fechaDesde: toApiLocalDateTime(fechaDesde),
          fechaHasta: toApiLocalDateTime(fechaHasta),
          granularidad: normalizarGranularidadFinanciera(granularidad),
          usuarioId: usuarioSeleccionado !== "TODOS" ? Number(usuarioSeleccionado) : undefined,
        };

        const response = await exportarResumenEjecutivoPdf(params);

        const professionalName = buildProfessionalReportFileName({
          modulo: "reportes",
          tipoReporte: selected.tipoReporte,
          extension: selected.extension,
        });

        triggerFileDownload(response.blob, professionalName || response.fileName);
      });

      setIsExportOpen(false);
      toast.success("Exportacion completada");
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo exportar el reporte financiero");
      toast.error(message);
    }
  };

  const colorVariacion = variacionPeriodoAnterior > 0
    ? "text-emerald-700"
    : variacionPeriodoAnterior < 0
      ? "text-rose-700"
      : "text-muted-foreground";

  return (
    <ReportesPageShell
      title="Reportes Financieros"
      subtitle="KPIs financieros, tendencia de ingresos y detalle por tipo de vehiculo y metodo de pago."
      actions={(
        <Button
          size="sm"
          className="bg-slate-900 text-white hover:bg-slate-800"
          onClick={() => setIsExportOpen(true)}
          disabled={loading}
        >
          Exportar reportes
        </Button>
      )}
    >
      <ReportesExportDialog
        open={isExportOpen}
        onOpenChange={setIsExportOpen}
        title="Exportacion avanzada financiera"
        description="Genera PDF ejecutivo profesional con los filtros actuales."
        options={FINANCIEROS_EXPORT_OPTIONS}
        value={exportType}
        onValueChange={setExportType}
        onExport={exportarReporte}
        exporting={exporting}
        progress={progress}
      />

      <ReportesContextBar
        fechaDesde={fechaDesde}
        fechaHasta={fechaHasta}
        onFechaDesdeChange={setFechaDesde}
        onFechaHastaChange={setFechaHasta}
        granularidad={granularidad}
        onGranularidadChange={(value) => setGranularidad(normalizarGranularidadFinanciera(value))}
        granularidades={GRANULARIDADES_FINANCIERAS}
        usuarioSeleccionado={usuarioSeleccionado}
        onUsuarioSeleccionadoChange={setUsuarioSeleccionado}
        usuarios={usuarios}
        onLimpiar={limpiarFiltros}
        onActualizar={cargarDatos}
        loading={loading}
      />

      <ReportesFetchState
        loading={loading && !hasLoadedOnce}
        loadingText="Cargando reportes financieros..."
        errorMessage={errorMessage}
        canRetry={canRetry}
        onRetry={cargarDatos}
      />

      <div className="grid grid-cols-1 gap-2 md:grid-cols-4">
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Ingresos totales</p>
          <p className="text-lg font-semibold text-primary">{formatMoney(ingresosTotales)}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Ticket promedio</p>
          <p className="text-lg font-semibold">{formatMoney(ticketPromedio)}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Variacion vs periodo anterior</p>
          <p className={`text-lg font-semibold ${colorVariacion}`}>
            {variacionPeriodoAnterior.toFixed(2)}%
          </p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Estadia promedio</p>
          <p className="text-lg font-semibold">{formatMinutes(estadiaPromedio)}</p>
        </div>
      </div>

      <div className="rounded-lg border bg-card p-3">
        <h2 className="mb-2 text-sm font-semibold">Ingresos por tiempo</h2>
        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={ingresosSerie} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
              <XAxis dataKey="etiqueta" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} width={70} />
              <Tooltip formatter={(value) => formatMoney(value)} />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Line
                type="monotone"
                dataKey="valor"
                name="Ingresos"
                stroke="hsl(var(--primary))"
                strokeWidth={2.5}
                dot={{ r: 2 }}
                activeDot={{ r: 5 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <div className="rounded-lg border bg-card p-3">
          <h2 className="mb-2 text-sm font-semibold">Ingresos por tipo de vehiculo</h2>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={ingresosTipoVehiculo} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                <XAxis dataKey="etiqueta" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} width={70} />
                <Tooltip formatter={(value) => formatMoney(value)} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="valor" name="Ingresos" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-lg border bg-card p-3">
          <h2 className="mb-2 text-sm font-semibold">Top horas pico por ingreso</h2>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={rankingHorasPico} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                <XAxis dataKey="clave" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} width={70} />
                <Tooltip formatter={(value) => formatMoney(value)} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="valor" name="Ingresos" fill="#0f172a" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="rounded-lg border bg-card p-3">
        <div className="mb-2 flex items-center justify-between">
          <h2 className="text-sm font-semibold">Ingresos por metodo de pago</h2>
          <span className="text-xs text-muted-foreground">{ingresosMetodoPago.length} metodos</span>
        </div>

        <Table className="text-xs">
          <TableHeader>
            <TableRow>
              <TableHead className="h-9 px-2">Metodo</TableHead>
              <TableHead className="h-9 px-2">Moneda</TableHead>
              <TableHead className="h-9 px-2 text-right">Ingresos</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {!ingresosMetodoPago.length ? (
              <TableRow>
                <TableCell colSpan={3} className="py-6 text-center text-xs text-muted-foreground">
                  No hay ingresos por metodo de pago para el rango seleccionado.
                </TableCell>
              </TableRow>
            ) : (
              ingresosMetodoPago.map((row) => (
                <TableRow key={row.etiqueta}>
                  <TableCell className="px-2 py-2 font-medium">{row.etiqueta}</TableCell>
                  <TableCell className="px-2 py-2">{row.moneda}</TableCell>
                  <TableCell className="px-2 py-2 text-right">{formatMoney(row.valor, row.moneda)}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </ReportesPageShell>
  );
};
