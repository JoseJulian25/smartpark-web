import { useState } from "react";
import toast from "react-hot-toast";
import { Search, Loader2 } from "lucide-react";

import {
  consultarPorPlaca,
  consultarPorReserva,
  consultarPorTicket,
  getReportesErrorMessage,
} from "../../api/reportes";
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
  const [placa, setPlaca] = useState("");
  const [codigoTicket, setCodigoTicket] = useState("");
  const [codigoReserva, setCodigoReserva] = useState("");

  const [loadingPlaca, setLoadingPlaca] = useState(false);
  const [loadingTicket, setLoadingTicket] = useState(false);
  const [loadingReserva, setLoadingReserva] = useState(false);

  const [resultadoPlaca, setResultadoPlaca] = useState(null);
  const [resultadoTicket, setResultadoTicket] = useState(null);
  const [resultadoReserva, setResultadoReserva] = useState(null);

  const buscarPorPlaca = async () => {
    const value = placa.trim().toUpperCase();
    if (!value) {
      toast.error("Ingresa una placa para consultar");
      return;
    }

    try {
      setLoadingPlaca(true);
      const data = await consultarPorPlaca(value);
      setResultadoPlaca(data);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo consultar por placa");
      toast.error(message);
    } finally {
      setLoadingPlaca(false);
    }
  };

  const buscarPorTicket = async () => {
    const value = codigoTicket.trim();
    if (!value) {
      toast.error("Ingresa un codigo de ticket");
      return;
    }

    try {
      setLoadingTicket(true);
      const data = await consultarPorTicket(value);
      setResultadoTicket(data);
    } catch (error) {
      const message = await getReportesErrorMessage(error, "No se pudo consultar por ticket");
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

  return (
    <section className="space-y-4">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold tracking-tight">Consultas</h1>
        <p className="text-sm text-muted-foreground">
          Busquedas rapidas por placa, ticket y reserva con resultados en tablas compactas.
        </p>
      </header>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <div className="space-y-3 rounded-lg border bg-card p-3">
          <h2 className="text-sm font-semibold">Consulta por placa</h2>
          <div className="flex gap-2">
            <Input
              value={placa}
              onChange={(event) => setPlaca(event.target.value)}
              placeholder="Ej: A123456"
            />
            <Button size="sm" onClick={buscarPorPlaca} disabled={loadingPlaca}>
              {loadingPlaca ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
            </Button>
          </div>

          {resultadoPlaca && (
            <div className="space-y-3">
              <p className="text-xs text-muted-foreground">
                Placa consultada: <span className="font-semibold text-foreground">{resultadoPlaca.placa || "-"}</span>
              </p>
              {renderTablaDinamica(
                resultadoPlaca?.tickets?.titulo || "Tickets",
                resultadoPlaca?.tickets?.columnas || [],
                resultadoPlaca?.tickets?.filas || []
              )}
              {renderTablaDinamica(
                resultadoPlaca?.reservas?.titulo || "Reservas",
                resultadoPlaca?.reservas?.columnas || [],
                resultadoPlaca?.reservas?.filas || []
              )}
            </div>
          )}
        </div>

        <div className="space-y-3 rounded-lg border bg-card p-3">
          <h2 className="text-sm font-semibold">Consulta por ticket</h2>
          <div className="flex gap-2">
            <Input
              value={codigoTicket}
              onChange={(event) => setCodigoTicket(event.target.value)}
              placeholder="Ej: T-1712433330000"
            />
            <Button size="sm" onClick={buscarPorTicket} disabled={loadingTicket}>
              {loadingTicket ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
            </Button>
          </div>

          {resultadoTicket &&
            renderTablaDinamica(
              resultadoTicket?.titulo || "Detalle de ticket",
              resultadoTicket?.columnas || [],
              resultadoTicket?.filas || []
            )}
        </div>

        <div className="space-y-3 rounded-lg border bg-card p-3">
          <h2 className="text-sm font-semibold">Consulta por reserva</h2>
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

          {resultadoReserva &&
            renderTablaDinamica(
              resultadoReserva?.titulo || "Detalle de reserva",
              resultadoReserva?.columnas || [],
              resultadoReserva?.filas || []
            )}
        </div>
      </div>
    </section>
  );
};
