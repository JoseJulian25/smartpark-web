
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import {
  AlertTriangle,
  Bike,
  CalendarClock,
  Car,
  Clock3,
  RefreshCw,
  TrendingUp
} from "lucide-react";
import toast from "react-hot-toast";

import { getMovimientosHoy } from "../api/dashboard";
import { getEspacios } from "../api/espacios";
import { getReservas } from "../api/reservas";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../components/ui/table";

const getErrorMessage = (error, fallback) => {
  return error?.response?.data?.message || error?.message || fallback;
};

const formatDateTime = (value) => {
  if (!value) return "-";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;

  return new Intl.DateTimeFormat("es-DO", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(parsed);
};

const getTicketEntradaDate = (ticketActivo) => {
  if (!ticketActivo) return null;

  const fullDateValue = ticketActivo.fechaHoraEntrada;
  if (fullDateValue) {
    const parsed = new Date(fullDateValue);
    if (!Number.isNaN(parsed.getTime())) return parsed;
  }

  const horaEntrada = ticketActivo.horaEntrada;
  if (!horaEntrada) return null;

  const [hour, minute] = String(horaEntrada).split(":").map(Number);
  if (Number.isNaN(hour) || Number.isNaN(minute)) return null;

  const fallback = new Date();
  fallback.setHours(hour, minute, 0, 0);
  return fallback;
};

export const DashboardPage = () => {
  const navigate = useNavigate();

  const [espacios, setEspacios] = useState([]);
  const [reservas, setReservas] = useState([]);
  const [movimientosHora, setMovimientosHora] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchDashboard = async (showError = false) => {
    try {
      setLoading(true);
      const [espaciosData, reservasData, movimientosData] = await Promise.all([
        getEspacios(),
        getReservas(),
        getMovimientosHoy()
      ]);

      setEspacios(Array.isArray(espaciosData) ? espaciosData : []);
      setReservas(Array.isArray(reservasData) ? reservasData : []);
      setMovimientosHora(Array.isArray(movimientosData) ? movimientosData : []);
    } catch (error) {
      console.error("Error cargando dashboard:", error);
      if (showError) {
        toast.error(getErrorMessage(error, "No se pudo cargar el dashboard"));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard(true);
  }, []);

  const metrics = useMemo(() => {
    const total = espacios.length;
    const libres = espacios.filter((e) => (e.estado || "").toUpperCase() === "LIBRE").length;
    const ocupados = espacios.filter((e) => (e.estado || "").toUpperCase() === "OCUPADO").length;
    const reservados = espacios.filter((e) => (e.estado || "").toUpperCase() === "RESERVADO").length;
    const ocupacionPct = total > 0 ? Math.round(((ocupados + reservados) / total) * 100) : 0;

    const pendientes = reservas.filter((r) => (r.estado || "").toUpperCase() === "PENDIENTE").length;
    const activas = reservas.filter((r) => (r.estado || "").toUpperCase() === "ACTIVA").length;

    return {
      total,
      libres,
      ocupados,
      reservados,
      ocupacionPct,
      pendientes,
      activas
    };
  }, [espacios, reservas]);

  const breakdownTipo = useMemo(() => {
    const build = (tipo) => {
      const filtered = espacios.filter((space) => (space.tipoVehiculo || "").toUpperCase() === tipo);
      const total = filtered.length;
      const ocupados = filtered.filter((space) => {
        const estado = (space.estado || "").toUpperCase();
        return estado === "OCUPADO" || estado === "RESERVADO";
      }).length;

      return {
        total,
        ocupados,
        pct: total > 0 ? Math.round((ocupados / total) * 100) : 0
      };
    };

    return {
      carros: build("CARRO"),
      motos: build("MOTO")
    };
  }, [espacios]);

  const reservasPendientesProximas = useMemo(() => {
    const now = Date.now();
    const thirtyMinutesAhead = now + 30 * 60 * 1000;

    return reservas
      .filter((reserva) => (reserva.estado || "").toUpperCase() === "PENDIENTE")
      .filter((reserva) => {
        const timestamp = new Date(reserva.horaInicio).getTime();
        return !Number.isNaN(timestamp) && timestamp >= now && timestamp <= thirtyMinutesAhead;
      })
      .sort((a, b) => new Date(a.horaInicio).getTime() - new Date(b.horaInicio).getTime());
  }, [reservas]);

  const estadiasLargas = useMemo(() => {
    const now = Date.now();
    return espacios
      .filter((space) => space.ticketActivo?.horaEntrada)
      .map((space) => {
        const fechaEntrada = getTicketEntradaDate(space.ticketActivo);
        const minutes = fechaEntrada ? Math.max(0, Math.floor((now - fechaEntrada.getTime()) / 60000)) : 0;
        return {
          codigoEspacio: space.codigoEspacio,
          placa: space.ticketActivo.placa,
          minutos: minutes
        };
      })
      .filter((item) => item.minutos >= 360)
      .sort((a, b) => b.minutos - a.minutos);
  }, [espacios]);

  const alertasOperativas = useMemo(() => {
    const alertasReservas = reservasPendientesProximas.map((item) => ({
      tipo: "Reserva proxima",
      referencia: `${item.codigoReserva || "-"} · ${item.placa || "-"}`,
      detalle: "Reserva pendiente por iniciar",
      tiempo: formatDateTime(item.horaInicio),
      ordenGrupo: 0,
      ordenValor: new Date(item.horaInicio).getTime(),
    }));

    const alertasEstadias = estadiasLargas.map((item) => ({
      tipo: "Estadia larga",
      referencia: `${item.codigoEspacio || "-"} · ${item.placa || "-"}`,
      detalle: "Vehiculo con permanencia extensa",
      tiempo: `${Math.floor(item.minutos / 60)}h ${item.minutos % 60}m`,
      ordenGrupo: 1,
      ordenValor: item.minutos,
    }));

    return [...alertasReservas, ...alertasEstadias].sort((a, b) => {
      if (a.ordenGrupo !== b.ordenGrupo) return a.ordenGrupo - b.ordenGrupo;
      return a.ordenGrupo === 0 ? a.ordenValor - b.ordenValor : b.ordenValor - a.ordenValor;
    });
  }, [reservasPendientesProximas, estadiasLargas]);

  const lineChartData = useMemo(() => {
    return (Array.isArray(movimientosHora) ? movimientosHora : []).map((item) => ({
      hora: item.etiquetaHora || `${String(item.hora || 0).padStart(2, "0")}:00`,
      entradas: Number(item.entradas || 0),
      salidas: Number(item.salidas || 0)
    }));
  }, [movimientosHora]);

  const estadoRapido = useMemo(() => {
    const totalEntradas = lineChartData.reduce((sum, item) => sum + Number(item.entradas || 0), 0);
    const totalSalidas = lineChartData.reduce((sum, item) => sum + Number(item.salidas || 0), 0);
    const flujoNeto = totalEntradas - totalSalidas;

    const proximaReserva = reservasPendientesProximas[0] || null;
    const estadiaMasLarga = estadiasLargas[0] || null;

    return {
      flujoNeto,
      proximaReserva,
      estadiaMasLarga
    };
  }, [lineChartData, reservasPendientesProximas, estadiasLargas]);

  return (
    <div className="space-y-3 max-w-6xl">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-lg font-semibold tracking-tight">Dashboard Operativo</h1>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => fetchDashboard(true)} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Actualizar
          </Button>
          <Button size="sm" onClick={() => navigate("/entrada")}>Ir a Operacion</Button>
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-7 gap-2">
        <Card className="border-primary/20">
          <CardContent className="p-3">
            <p className="text-xs text-muted-foreground">Ocupacion</p>
            <p className="text-lg font-bold text-primary">{metrics.ocupacionPct}%</p>
          </CardContent>
        </Card>
        <Card><CardContent className="p-3"><p className="text-xs text-muted-foreground">Total espacios</p><p className="text-lg font-bold">{metrics.total}</p></CardContent></Card>
        <Card><CardContent className="p-3"><p className="text-xs text-muted-foreground">Libres</p><p className="text-lg font-bold text-emerald-700">{metrics.libres}</p></CardContent></Card>
        <Card><CardContent className="p-3"><p className="text-xs text-muted-foreground">Ocupados</p><p className="text-lg font-bold text-rose-700">{metrics.ocupados}</p></CardContent></Card>
        <Card><CardContent className="p-3"><p className="text-xs text-muted-foreground">Reservados</p><p className="text-lg font-bold text-amber-700">{metrics.reservados}</p></CardContent></Card>
        <Card><CardContent className="p-3"><p className="text-xs text-muted-foreground">Reservas pendientes</p><p className="text-lg font-bold">{metrics.pendientes}</p></CardContent></Card>
        <Card><CardContent className="p-3"><p className="text-xs text-muted-foreground">Reservas activas</p><p className="text-lg font-bold">{metrics.activas}</p></CardContent></Card>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_360px] gap-3">
        <Card>
          <CardHeader className="pb-1">
            <CardTitle className="text-base">Registros de Entradas y Salidas (Hoy)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5">
            <div className="flex flex-wrap items-center gap-4 text-xs">
              <span className="inline-flex items-center gap-2">
                <span className="h-2.5 w-2.5 rounded-full bg-primary" />
                Entradas
              </span>
              <span className="inline-flex items-center gap-2">
                <span className="h-2.5 w-2.5 rounded-full bg-rose-500" />
                Salidas
              </span>
            </div>

            {!lineChartData.length ? (
              <p className="text-sm text-muted-foreground">No hay datos de movimientos para hoy.</p>
            ) : (
              <div className="rounded-md border bg-white p-2">
                <div className="h-56 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={lineChartData} margin={{ top: 16, right: 12, left: 0, bottom: 8 }}>
                      <CartesianGrid strokeDasharray="4 4" strokeOpacity={0.2} />
                      <XAxis
                        dataKey="hora"
                        interval={2}
                        tick={{ fontSize: 11 }}
                        tickMargin={8}
                      />
                      <YAxis allowDecimals={false} tick={{ fontSize: 11 }} width={32} />
                      <Tooltip
                        contentStyle={{
                          borderRadius: "10px",
                          border: "1px solid rgba(148, 163, 184, 0.35)",
                          boxShadow: "0 10px 24px rgba(15, 23, 42, 0.08)",
                          fontSize: "12px"
                        }}
                      />
                      <Legend wrapperStyle={{ fontSize: "12px" }} />
                      <Line
                        type="monotone"
                        dataKey="entradas"
                        name="Entradas"
                        stroke="hsl(var(--primary))"
                        strokeWidth={2.5}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                      />
                      <Line
                        type="monotone"
                        dataKey="salidas"
                        name="Salidas"
                        stroke="#f43f5e"
                        strokeWidth={2.5}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            )}

            <div className="grid grid-cols-2 gap-2 text-xs">
              <div className="rounded-md border p-2">
                <p className="text-muted-foreground">Total Entradas Hoy</p>
                <p className="font-semibold text-primary">
                  {lineChartData.reduce((sum, item) => sum + Number(item.entradas || 0), 0)}
                </p>
              </div>
              <div className="rounded-md border p-2">
                <p className="text-muted-foreground">Total Salidas Hoy</p>
                <p className="font-semibold text-rose-600">
                  {lineChartData.reduce((sum, item) => sum + Number(item.salidas || 0), 0)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-3">
          <Card>
            <CardHeader className="pb-1">
              <CardTitle className="text-base flex items-center gap-2">
                <TrendingUp className="h-4 w-4" />
                Ocupacion por tipo
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="space-y-1">
                <div className="flex items-center justify-between">
                  <span className="flex items-center gap-2"><Car className="h-4 w-4" />Carros</span>
                  <span>{breakdownTipo.carros.ocupados}/{breakdownTipo.carros.total} ({breakdownTipo.carros.pct}%)</span>
                </div>
                <div className="h-2 rounded-full bg-slate-100 overflow-hidden">
                  <div className="h-full bg-primary" style={{ width: `${breakdownTipo.carros.pct}%` }} />
                </div>
              </div>

              <div className="space-y-1">
                <div className="flex items-center justify-between">
                  <span className="flex items-center gap-2"><Bike className="h-4 w-4" />Motos</span>
                  <span>{breakdownTipo.motos.ocupados}/{breakdownTipo.motos.total} ({breakdownTipo.motos.pct}%)</span>
                </div>
                <div className="h-2 rounded-full bg-slate-100 overflow-hidden">
                  <div className="h-full bg-primary" style={{ width: `${breakdownTipo.motos.pct}%` }} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-1">
              <CardTitle className="text-base">Estado rapido</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-xs">
              <div className="rounded-md border px-2 py-2">
                <p className="text-muted-foreground">Flujo neto hoy</p>
                <p className={`font-semibold ${estadoRapido.flujoNeto >= 0 ? "text-emerald-700" : "text-rose-700"}`}>
                  {estadoRapido.flujoNeto >= 0 ? "+" : ""}
                  {estadoRapido.flujoNeto} vehiculos
                </p>
              </div>

              <div className="rounded-md border px-2 py-2">
                <p className="text-muted-foreground">Proxima reserva</p>
                {estadoRapido.proximaReserva ? (
                  <p className="font-medium">
                    {formatDateTime(estadoRapido.proximaReserva.horaInicio)} · {estadoRapido.proximaReserva.placa || "-"}
                  </p>
                ) : (
                  <p className="font-medium">Sin reservas proximas</p>
                )}
              </div>

              <div className="rounded-md border px-2 py-2">
                <p className="text-muted-foreground">Estadia mas larga activa</p>
                {estadoRapido.estadiaMasLarga ? (
                  <p className="font-medium">
                    {estadoRapido.estadiaMasLarga.codigoEspacio || "-"} · {estadoRapido.estadiaMasLarga.placa || "-"} · {Math.floor(estadoRapido.estadiaMasLarga.minutos / 60)}h {estadoRapido.estadiaMasLarga.minutos % 60}m
                  </p>
                ) : (
                  <p className="font-medium">Sin estadias largas</p>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <Card>
        <CardHeader className="pb-1">
          <CardTitle className="text-base flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-600" />
            Alertas operativas
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Table className="text-xs">
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-2">Tipo</TableHead>
                <TableHead className="h-9 px-2">Referencia</TableHead>
                <TableHead className="h-9 px-2">Detalle</TableHead>
                <TableHead className="h-9 px-2">Tiempo</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!alertasOperativas.length ? (
                <TableRow>
                  <TableCell colSpan={4} className="py-6 text-center text-xs text-muted-foreground">
                    Sin alertas operativas activas.
                  </TableCell>
                </TableRow>
              ) : (
                alertasOperativas.map((alerta, idx) => (
                  <TableRow key={`${alerta.tipo}-${alerta.referencia}-${idx}`}>
                    <TableCell className="px-2 py-2 font-medium">{alerta.tipo}</TableCell>
                    <TableCell className="px-2 py-2">{alerta.referencia}</TableCell>
                    <TableCell className="px-2 py-2">{alerta.detalle}</TableCell>
                    <TableCell className="px-2 py-2">{alerta.tiempo}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};


