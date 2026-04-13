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
  exportarTicketsCsv,
  getEntradasPorHora,
  getSalidasPorHora,
  getTicketsActivosReporte,
  getEstadiasLargas,
  getReportesErrorMessage,
} from "../../api/reportes";
import { getUsuarios } from "../../api/usuarios";
import { Button } from "../../components/ui/button";
import { ReportesFiltrosBar } from "../../components/reportes/ReportesFiltrosBar";
import { ReportesFetchState } from "../../components/reportes/ReportesFetchState";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../../components/ui/select";
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

const normalizeSeries = (items = []) => {
  return items.map((item) => ({
    etiqueta: item?.periodo || "-",
    valor: Number(item?.valor || 0),
  }));
};

const toApiLocalDateTime = (value) => {
  if (!value) return undefined;
  return value.length === 16 ? `${value}:00` : value;
};

const triggerFileDownload = (blob, fileName) => {
  const url = window.URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName || "reporte.csv";
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.URL.revokeObjectURL(url);
};

export const ReportesOperativosPage = () => {
  const [fechaDesde, setFechaDesde] = useState(startOfTodayInput());
  const [fechaHasta, setFechaHasta] = useState(nowInput());
  const [tipoVehiculo, setTipoVehiculo] = useState("TODOS");
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState("TODOS");
  const [loading, setLoading] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [canRetry, setCanRetry] = useState(false);
  const [usuarios, setUsuarios] = useState([]);

  const [entradasSerie, setEntradasSerie] = useState([]);
  const [salidasSerie, setSalidasSerie] = useState([]);
  const [ticketsActivos, setTicketsActivos] = useState([]);
  const [estadiasLargas, setEstadiasLargas] = useState([]);

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
        usuarioId: usuarioSeleccionado !== "TODOS" ? Number(usuarioSeleccionado) : undefined,
      };

      const [entradasResp, salidasResp, ticketsResp, estadiasResp] = await Promise.all([
        getEntradasPorHora(params),
        getSalidasPorHora(params),
        getTicketsActivosReporte({ usuarioId: params.usuarioId }),
        getEstadiasLargas(360, params.usuarioId),
      ]);

      setEntradasSerie(normalizeSeries(entradasResp?.items));
      setSalidasSerie(normalizeSeries(salidasResp?.items));
      setTicketsActivos(Array.isArray(ticketsResp?.filas) ? ticketsResp.filas : []);
      setEstadiasLargas(Array.isArray(estadiasResp?.filas) ? estadiasResp.filas : []);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudieron cargar los reportes operativos");
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

  const chartData = useMemo(() => {
    const entradasMap = new Map(entradasSerie.map((item) => [item.etiqueta, item.valor]));
    const salidasMap = new Map(salidasSerie.map((item) => [item.etiqueta, item.valor]));
    const etiquetas = [...new Set([...entradasMap.keys(), ...salidasMap.keys()])];

    return etiquetas.map((etiqueta) => ({
      hora: etiqueta,
      entradas: entradasMap.get(etiqueta) || 0,
      salidas: salidasMap.get(etiqueta) || 0,
    }));
  }, [entradasSerie, salidasSerie]);

  const ticketsFiltrados = useMemo(() => {
    if (tipoVehiculo === "TODOS") return ticketsActivos;
    return ticketsActivos.filter((fila) => {
      const tipo = String(fila?.columnas?.tipoVehiculo || "").toUpperCase();
      return tipo === tipoVehiculo;
    });
  }, [ticketsActivos, tipoVehiculo]);

  const entradasTotal = useMemo(
    () => chartData.reduce((acc, row) => acc + Number(row.entradas || 0), 0),
    [chartData]
  );

  const salidasTotal = useMemo(
    () => chartData.reduce((acc, row) => acc + Number(row.salidas || 0), 0),
    [chartData]
  );

  const limpiarFiltros = () => {
    setFechaDesde(startOfTodayInput());
    setFechaHasta(nowInput());
    setTipoVehiculo("TODOS");
    setUsuarioSeleccionado("TODOS");
  };

  const exportarCsvTickets = async () => {
    try {
      const params = {
        fechaDesde: toApiLocalDateTime(fechaDesde),
        fechaHasta: toApiLocalDateTime(fechaHasta),
      };
      const { blob, fileName } = await exportarTicketsCsv(params);
      triggerFileDownload(blob, fileName);
      toast.success("CSV de tickets descargado");
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo exportar tickets CSV");
      toast.error(message);
    }
  };

  return (
    <section className="space-y-4">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold tracking-tight">Reportes Operativos</h1>
        <p className="text-sm text-muted-foreground">
          Vista compacta de operación diaria: KPIs, entradas/salidas por hora y tickets activos.
        </p>
        <div className="pt-1">
          <Button
            size="sm"
            className="bg-emerald-600 text-white hover:bg-emerald-700"
            onClick={exportarCsvTickets}
            disabled={loading}
          >
            Exportar CSV tickets
          </Button>
        </div>
      </header>

      <ReportesFiltrosBar
        fechaDesde={fechaDesde}
        fechaHasta={fechaHasta}
        onFechaDesdeChange={setFechaDesde}
        onFechaHastaChange={setFechaHasta}
        onLimpiar={limpiarFiltros}
        onActualizar={cargarDatos}
        loading={loading}
        columnsClassName="md:grid-cols-5"
        actionsSpanClassName=""
      >
          <Select value={tipoVehiculo} onValueChange={setTipoVehiculo}>
            <SelectTrigger>
              <SelectValue placeholder="Tipo de vehiculo" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="TODOS">Todos los tipos</SelectItem>
              <SelectItem value="CARRO">Carro</SelectItem>
              <SelectItem value="MOTO">Moto</SelectItem>
            </SelectContent>
          </Select>

          <Select value={usuarioSeleccionado} onValueChange={setUsuarioSeleccionado}>
            <SelectTrigger>
              <SelectValue placeholder="Usuario" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="TODOS">Todos los usuarios</SelectItem>
              {usuarios.map((usuario) => (
                <SelectItem key={usuario.id} value={String(usuario.id)}>
                  {usuario.nombre}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
      </ReportesFiltrosBar>

      <ReportesFetchState
        loading={loading && !hasLoadedOnce}
        loadingText="Cargando reportes operativos..."
        errorMessage={errorMessage}
        canRetry={canRetry}
        onRetry={cargarDatos}
      />

      <div className="grid grid-cols-2 gap-2 md:grid-cols-4">
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Entradas</p>
          <p className="text-lg font-semibold text-primary">{entradasTotal}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Salidas</p>
          <p className="text-lg font-semibold">{salidasTotal}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Tickets activos</p>
          <p className="text-lg font-semibold">{ticketsFiltrados.length}</p>
        </div>
        <div className="rounded-md border px-3 py-2">
          <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Estadias largas</p>
          <p className="text-lg font-semibold">{estadiasLargas.length}</p>
        </div>
      </div>

      <div className="rounded-lg border bg-card p-3">
        <h2 className="mb-2 text-sm font-semibold">Entradas vs Salidas por hora</h2>
        {!chartData.length ? (
          <div className="flex h-72 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground">
            No hay movimientos en el rango seleccionado.
          </div>
        ) : (
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData} margin={{ top: 12, right: 10, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                <XAxis dataKey="hora" tick={{ fontSize: 11 }} interval={2} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={30} />
                <Tooltip />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Line type="monotone" dataKey="entradas" stroke="hsl(var(--primary))" strokeWidth={2.5} dot={false} />
                <Line type="monotone" dataKey="salidas" stroke="#ef4444" strokeWidth={2.5} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="rounded-lg border bg-card p-3">
        <div className="mb-2 flex items-center justify-between gap-2">
          <h2 className="text-sm font-semibold">Tickets activos</h2>
          <span className="text-xs text-muted-foreground">
            Filtro tipo aplicado: {tipoVehiculo === "TODOS" ? "Todos" : tipoVehiculo}
          </span>
        </div>

        <Table className="text-xs">
          <TableHeader>
            <TableRow>
              <TableHead className="h-9 px-2">Ticket</TableHead>
              <TableHead className="h-9 px-2">Placa</TableHead>
              <TableHead className="h-9 px-2">Tipo</TableHead>
              <TableHead className="h-9 px-2">Espacio</TableHead>
              <TableHead className="h-9 px-2">Usuario</TableHead>
              <TableHead className="h-9 px-2">Hora entrada</TableHead>
              <TableHead className="h-9 px-2">Estado</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {!ticketsFiltrados.length ? (
              <TableRow>
                <TableCell colSpan={7} className="py-6 text-center text-xs text-muted-foreground">
                  No hay tickets activos para los filtros seleccionados.
                </TableCell>
              </TableRow>
            ) : (
              ticketsFiltrados.map((fila, idx) => {
                const c = fila?.columnas || {};
                return (
                  <TableRow key={`${c.codigoTicket || "ticket"}-${idx}`}>
                    <TableCell className="px-2 py-2 font-medium">{c.codigoTicket || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.placa || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.tipoVehiculo || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.codigoEspacio || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.usuario || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.horaEntrada || "-"}</TableCell>
                    <TableCell className="px-2 py-2">{c.estado || "-"}</TableCell>
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
