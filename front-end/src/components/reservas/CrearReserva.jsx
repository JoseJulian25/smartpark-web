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
  const [horaReserva, setHoraReserva] = useState("");

  const [espacios, setEspacios] = useState({
    carros: 0,
    motos: 0
  });

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

      await crearReserva({
        placa,
        tipoVehiculo,
        fechaReserva,
        horaReserva
      });

      setSuccess("Reserva creada correctamente");

      setPlaca("");
      setFechaReserva("");
      setHoraReserva("");

      fetchEspacios();

      if (onSuccess) {
        onSuccess();
      }

    } catch (err) {
      console.error(err);
      setError("Error creando reserva");
    } finally {
      setLoading(false);
    }
  };

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
              {espacios.carros}
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
              {espacios.motos}
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

              <Label>
                Placa del Vehículo
              </Label>
              <Input
                value={placa}
                onChange={(e) =>
                  setPlaca(e.target.value.toUpperCase())
                }
                required
              />
            </div>

            <div>
              <Label>
                Tipo de Vehículo
              </Label>
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
              <Label>
                Fecha Reserva
              </Label>
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

              <Label>
                Hora Reserva
              </Label>

              <Input
                type="time"
                value={horaReserva}
                onChange={(e) =>
                  setHoraReserva(e.target.value)
                }
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