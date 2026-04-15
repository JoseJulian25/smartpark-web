import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import {
  crearReserva
} from "../../api/reservas";

import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Badge } from "../ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { cn } from "@/lib/utils";
import { getEspacios } from "@/api/espacios";

export default function CrearReserva({ onSuccess }) {

  const [placa, setPlaca] = useState("");
  const [tipoVehiculo, setTipoVehiculo] = useState("CARRO");
  const [fechaReserva, setFechaReserva] = useState("");

  const [espacioId, setEspacioId] = useState("");
  const [espacios, setEspacios] = useState([]);
  const [reservaCreada, setReservaCreada] = useState(null);

  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [horaInicio, setHoraInicio] = useState("");
  const [email, setEmail] = useState("");
  const [telefono, setTelefono] = useState("");

  const [loading, setLoading] = useState(false);

  const formatDateTime = (value) => {
    if (!value) {
      return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat("es-DO", {
      dateStyle: "short",
      timeStyle: "short"
    }).format(date);
  };

  const fetchEspacios = async () => {
    try {
      const data = await getEspacios();
      setEspacios(data);
    } catch (err) {
      console.error(err);
      toast.error("Error cargando espacios disponibles");
    }
  };

  useEffect(() => {
    fetchEspacios();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      setLoading(true);

      const fechaHoraInicio = `${fechaReserva}T${horaInicio}:00`;

      const espacioSeleccionado = espacios.find(
        (espacio) => espacio.id === Number(espacioId)
      );

      if (!espacioSeleccionado) {
        throw new Error("Debe seleccionar un espacio valido");
      }

      const data = {
        espacioId: espacioSeleccionado.id,
        placa,
        tipoVehiculo,
        horaInicio: fechaHoraInicio,
        clienteNombreCompleto: `${nombre} ${apellido}`.trim(),
        clienteTelefono: telefono,
        clienteEmail: email
      };

      const reservaCreadaResponse = await crearReserva(data);

      setReservaCreada({
        codigoReserva: reservaCreadaResponse.codigoReserva,
        nombre: reservaCreadaResponse.clienteNombreCompleto,
        email: reservaCreadaResponse.clienteEmail,
        telefono: reservaCreadaResponse.clienteTelefono,
        placa: reservaCreadaResponse.placa,
        tipoVehiculo: reservaCreadaResponse.tipoVehiculo,
        horaInicio: reservaCreadaResponse.horaInicio,
        espacio: reservaCreadaResponse.codigoEspacio
      });

      toast.success("Reserva creada correctamente");

      setPlaca("");
      setFechaReserva("");
      setHoraInicio("");
      setNombre("");
      setApellido("");
      setEmail("");
      setTelefono("");
      setEspacioId("");

      await fetchEspacios();

      if (onSuccess) {
        onSuccess();
      }
    } catch (err) {
      const mensajeError =
        err?.response?.data?.message ||
        err.message ||
        "Error creando reserva";

      console.error("Error creando reserva:", err?.response?.data || err);
      toast.error(mensajeError);
    } finally {
      setLoading(false);
    }
  };

  const espaciosLibres = espacios.filter(
    (espacio) => espacio.estado === "LIBRE"
  );

  const carrosDisponibles = espaciosLibres.filter(
    (espacio) => espacio.tipoVehiculo === "CARRO"
  ).length;

  const motosDisponibles = espaciosLibres.filter(
    (espacio) => espacio.tipoVehiculo === "MOTO"
  ).length;

  const espaciosFiltrados = espaciosLibres.filter(
    (espacio) => espacio.tipoVehiculo === tipoVehiculo
  );

  return (
    <div className="space-y-6">
      {reservaCreada && (
        <Card className="border-emerald-300 bg-emerald-50">
          <CardHeader className="pb-2">
            <CardTitle className="text-emerald-700 text-base">
              Reserva creada
            </CardTitle>
          </CardHeader>

          <CardContent className="grid gap-2 text-sm md:grid-cols-2">
            <div><strong>Codigo:</strong> {reservaCreada.codigoReserva}</div>
            <div><strong>Espacio:</strong> {reservaCreada.espacio}</div>
            <div><strong>Cliente:</strong> {reservaCreada.nombre}</div>
            <div><strong>Placa:</strong> {reservaCreada.placa}</div>
            <div><strong>Inicio:</strong> {formatDateTime(reservaCreada.horaInicio)}</div>
            <div className="md:col-span-2 flex justify-end">
              <Button
                size="sm"
                variant="outline"
                onClick={() => {
                  navigator.clipboard.writeText(reservaCreada.codigoReserva);
                  toast.success("Codigo de reserva copiado");
                }}
              >
                Copiar codigo
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex flex-wrap items-center gap-2 rounded-md border bg-card px-3 py-2">
        <span className="text-xs font-medium text-muted-foreground">
          Disponibilidad:
        </span>
        <Badge variant="secondary" className="gap-1">
          Carros <span className="font-bold">{carrosDisponibles}</span>
        </Badge>
        <Badge variant="secondary" className="gap-1">
          Motos <span className="font-bold">{motosDisponibles}</span>
        </Badge>
        <Badge variant="outline" className="gap-1">
          Total libres <span className="font-bold">{espaciosLibres.length}</span>
        </Badge>
      </div>

      <form
        onSubmit={handleSubmit}
        className="space-y-4"
      >
            <div className="rounded-md border bg-white p-3 shadow-sm space-y-3">
              <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Datos del cliente
              </p>
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <Label>Nombre</Label>
                  <Input
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    placeholder="Nombre"
                    required
                  />
                </div>

                <div>
                  <Label>Apellido</Label>
                  <Input
                    value={apellido}
                    onChange={(e) => setApellido(e.target.value)}
                    placeholder="Apellido"
                    required
                  />
                </div>

                <div>
                  <Label>Email</Label>
                  <Input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="cliente@email.com"
                    required
                  />
                </div>

                <div>
                  <Label>Telefono</Label>
                  <Input
                    type="tel"
                    value={telefono}
                    onChange={(e) => setTelefono(e.target.value)}
                    placeholder="809-555-1234"
                    required
                  />
                </div>
              </div>
            </div>

            <div className="rounded-md border bg-white p-3 shadow-sm space-y-3">
              <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Vehiculo y parqueo
              </p>
              <div className="space-y-2">
                <Label>Tipo de Vehiculo</Label>
                <div className="flex gap-2">
                  <label
                    className={cn(
                      "flex items-center gap-2 rounded-md border px-3 py-2 text-sm cursor-pointer transition-colors",
                      tipoVehiculo === "CARRO"
                        ? "border-primary bg-primary text-primary-foreground"
                        : "border-border bg-background hover:bg-muted"
                    )}
                  >
                    <input
                      type="radio"
                      className="sr-only"
                      value="CARRO"
                      checked={tipoVehiculo === "CARRO"}
                      onChange={(e) => setTipoVehiculo(e.target.value)}
                    />
                    Carro
                  </label>

                  <label
                    className={cn(
                      "flex items-center gap-2 rounded-md border px-3 py-2 text-sm cursor-pointer transition-colors",
                      tipoVehiculo === "MOTO"
                        ? "border-primary bg-primary text-primary-foreground"
                        : "border-border bg-background hover:bg-muted"
                    )}
                  >
                    <input
                      type="radio"
                      className="sr-only"
                      value="MOTO"
                      checked={tipoVehiculo === "MOTO"}
                      onChange={(e) => setTipoVehiculo(e.target.value)}
                    />
                    Moto
                  </label>
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <Label>Placa del Vehículo</Label>
                  <Input
                    value={placa}
                    onChange={(e) => setPlaca(e.target.value.toUpperCase())}
                    placeholder="A123456"
                    required
                  />
                </div>

                <div>
                  <Label>Seleccionar Parqueo</Label>
                  <select
                    className="w-full h-10 rounded-md border bg-background px-3 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    value={espacioId}
                    onChange={(e) => setEspacioId(e.target.value)}
                    required
                  >
                    <option value="">Seleccione un parqueo</option>

                    {espaciosFiltrados.map((espacio) => (
                      <option key={espacio.id} value={espacio.id}>
                        {espacio.codigoEspacio} - {espacio.tipoVehiculo}
                      </option>
                    ))}
                  </select>
                  <p className="text-xs text-muted-foreground mt-1">
                    {espaciosFiltrados.length} espacios disponibles para {tipoVehiculo.toLowerCase()}
                  </p>
                </div>
              </div>
            </div>

            <div className="rounded-md border bg-white p-3 shadow-sm space-y-3">
              <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Programacion
              </p>
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <Label>Fecha Reserva</Label>
                  <Input
                    type="date"
                    value={fechaReserva}
                    onChange={(e) => setFechaReserva(e.target.value)}
                    min={new Date().toISOString().split("T")[0]}
                    required
                  />
                </div>

                <div>
                  <Label>Hora Inicio</Label>
                  <Input
                    type="time"
                    value={horaInicio}
                    onChange={(e) => setHoraInicio(e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end">
              <Button
                className="min-w-40 bg-primary text-primary-foreground hover:bg-primary/90"
                disabled={loading || espaciosFiltrados.length === 0}
              >
                {loading ? "Creando..." : "Crear Reserva"}
              </Button>
            </div>

      </form>

    </div>

  );

}