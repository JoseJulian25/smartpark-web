import { useEffect, useState } from "react";

import {
  crearReserva,
  getEspaciosDisponibles
} from "../../api/reservas";

import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Alert, AlertDescription } from "../ui/alert";

export default function CrearReserva({ onSuccess }) {

  const [placa, setPlaca] = useState("");
  const [tipoVehiculo, setTipoVehiculo] = useState("CARRO");
  const [fechaReserva, setFechaReserva] = useState("");
  const [documento, setDocumento] = useState("");

  const [espacioId, setEspacioId] = useState("");
  const [espacios, setEspacios] = useState([]);

  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [tipoDocumento, setTipoDocumento] = useState("CEDULA");
  
  const [horaInicio, setHoraInicio] = useState("");
  const [horaFin, setHoraFin] = useState("");
  const [email, setEmail] = useState("");
  const [telefono, setTelefono] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const fetchEspacios = async () => {
    try {
      const data = await getEspaciosDisponibles();
      setEspacios(data);
    } catch (err) {
      console.error(err);
      setError("Error cargando espacios disponibles");
    }
  };

  useEffect(() => {
    fetchEspacios();
  }, []);

const handleSubmit = async (e) => {
  e.preventDefault();

  try {
    setLoading(true);
    setError("");
    setSuccess("");

    const fechaHoraInicio = `${fechaReserva}T${horaInicio}:00`;
    const fechaHoraFin = `${fechaReserva}T${horaFin}:00`;

    const data = {
      espacioId: Number(espacioId),
      placa,
      tipoVehiculo,
      horaInicio: fechaHoraInicio,
      horaFin: fechaHoraFin,
      clienteNombreCompleto: `${nombre} ${apellido}`,
      clienteTelefono: telefono,
      clienteEmail: email
    };

    console.log("Creando reserva:", data);

    await crearReserva(data);

    setSuccess("Reserva creada correctamente");

    // Limpiar formulario
    setPlaca("");
    setFechaReserva("");
    setHoraInicio("");
    setHoraFin("");
    setNombre("");
    setApellido("");
    setDocumento("");
    setEmail("");
    setTelefono("");
    setEspacioId("");

    fetchEspacios();

    if (onSuccess) {
      onSuccess();
    }

  } catch (err) {
    console.error("Error creando reserva:", err?.response?.data || err);

    setError(
      err?.response?.data?.message ||
      "Error creando reserva"
    );

  } finally {
    setLoading(false);
  }
};
  const carrosDisponibles = espacios.filter(
  e => e.tipoVehiculo === "CARRO" && e.estado === "LIBRE"
).length;

const motosDisponibles = espacios.filter(
  e => e.tipoVehiculo === "MOTO" && e.estado === "LIBRE"
).length;

  return (

    <div className="space-y-6">

      <div className="grid grid-cols-2 gap-4">

        <Card>
          <CardHeader>
            <CardTitle>
              Espacios para Carros
            </CardTitle>
          </CardHeader>

          <CardContent>
            <div className="text-3xl font-bold">
              {carrosDisponibles}
            </div>
            <p className="text-sm text-gray-500">
              disponibles
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>
              Espacios para Motos
            </CardTitle>
          </CardHeader>

          <CardContent>
            <div className="text-3xl font-bold">
              {motosDisponibles}
            </div>
            <p className="text-sm text-gray-500">
              disponibles
            </p>
          </CardContent>
        </Card>

      </div>

      <Card>

        <CardHeader>
          <CardTitle>
            Nueva Reserva
          </CardTitle>
        </CardHeader>

        <CardContent>

          <form
            onSubmit={handleSubmit}
            className="space-y-4"
          >

            <div>
              <Label>Nombre</Label>
              <Input
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                required
              />
            </div>

            <div>
              <Label>Apellido</Label>
              <Input
                value={apellido}
                onChange={(e) => setApellido(e.target.value)}
                required
              />
            </div>

            <div>
              <Label>Email</Label>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div>
              <Label>Teléfono</Label>
              <Input
                type="tel"
                value={telefono}
                onChange={(e) => setTelefono(e.target.value)}
                required
              />
            </div>

            <div>
              <Label>Tipo Documento</Label>

              <div className="flex gap-6 mt-2">

                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    value="CEDULA"
                    checked={tipoDocumento === "CEDULA"}
                    onChange={(e) => setTipoDocumento(e.target.value)}
                  />
                  Cédula
                </label>

                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    value="PASAPORTE"
                    checked={tipoDocumento === "PASAPORTE"}
                    onChange={(e) => setTipoDocumento(e.target.value)}
                  />
                  Pasaporte
                </label>

              </div>
            </div>

            <div>
              <Label>
                {tipoDocumento === "CEDULA" ? "Cédula" : "Pasaporte"}
              </Label>

              <Input
                value={documento}
                onChange={(e) => setDocumento(e.target.value)}
                required
              />
            </div>
            

            <div>
              <Label>Tipo de Vehículo</Label>

              <div className="flex gap-6 mt-2">

                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    value="CARRO"
                    checked={tipoVehiculo === "CARRO"}
                    onChange={(e) =>
                      setTipoVehiculo(e.target.value)
                    }
                  />
                  Carro
                </label>

                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    value="MOTO"
                    checked={tipoVehiculo === "MOTO"}
                    onChange={(e) =>
                      setTipoVehiculo(e.target.value)
                    }
                  />
                  Moto
                </label>

              </div>

            </div>
            <div>
              <Label>Seleccionar Parqueo</Label>

              <select
                className="w-full border rounded-md p-2"
                value={espacioId}
                onChange={(e) => setEspacioId(e.target.value)}
                required
              >
              <option value="">Seleccione un parqueo</option>

              {espacios
                  .filter(
                  (espacio) =>
                  espacio.estado === "LIBRE" &&
                  espacio.tipoVehiculo === tipoVehiculo
                  )
                .map((espacio) => (
              <option key={espacio.id} value={espacio.id}>
                {espacio.codigoEspacio} - {espacio.tipoVehiculo}
              </option>
                ))}
              </select>
            </div>

            <div>
              <Label>Placa del Vehículo</Label>
              <Input
                value={placa}
                onChange={(e) =>
                  setPlaca(e.target.value.toUpperCase())
                }
                required
              />
            </div>
            <div>
              <Label>Fecha Reserva</Label>
              <Input
                type="date"
                value={fechaReserva}
                onChange={(e) =>
                  setFechaReserva(e.target.value)
                }
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

            <div>
              <Label>Hora Fin</Label>
              <Input
              type="time"
              value={horaFin}
              onChange={(e) => setHoraFin(e.target.value)}
              required
              />
            </div>
            {error && (
              <Alert variant="destructive">
                <AlertDescription>
                  {error}
                </AlertDescription>
              </Alert>
            )}

            {success && (
              <Alert>
                <AlertDescription>
                  {success}
                </AlertDescription>
              </Alert>
            )}

            <Button
              className="w-full"
              disabled={loading}
            >
              Crear Reserva
            </Button>

          </form>

        </CardContent>

      </Card>

    </div>

  );

}