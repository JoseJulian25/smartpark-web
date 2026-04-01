import { Badge } from "../ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";

export default function ListaReservas({
  activas = [],
  pendientes = [],
  historial = []
}) {

  const renderEstado = (estado) => {

    const colors = {
      activa: "bg-green-500",
      pendiente: "bg-yellow-500",
      finalizada: "bg-gray-500",
      cancelada: "bg-red-500"
    };

    return (
      <Badge className={colors[estado] || "bg-gray-500"}>
        {estado}
      </Badge>
    );
  };

  const renderTabla = (data) => {

    if (!data.length) {
      return (
        <div className="text-center py-4 text-gray-500">
          Sin registros
        </div>
      );
    }

    return (
      <table className="w-full text-sm">

        <thead>
          <tr className="border-b">
            <th className="text-left p-2">Código</th>
            <th className="text-left p-2">Placa</th>
            <th className="text-left p-2">Tipo</th>
            <th className="text-left p-2">Fecha</th>
            <th className="text-left p-2">Hora</th>
            <th className="text-left p-2">Estado</th>
          </tr>
        </thead>

        <tbody>

          {data.map((reserva) => (

            <tr
              key={reserva.id}
              className="border-b hover:bg-gray-50"
            >

              <td className="p-2">
                {reserva.codigo}
              </td>

              <td className="p-2">
                {reserva.placa}
              </td>

              <td className="p-2">
                {reserva.tipoVehiculo}
              </td>

              <td className="p-2">
                {reserva.fechaReserva}
              </td>

              <td className="p-2">
                {reserva.horaReserva}
              </td>

              <td className="p-2">
                {renderEstado(reserva.estado)}
              </td>

            </tr>

          ))}

        </tbody>

      </table>
    );
  };


  return (

    <div className="space-y-6">
      <Card>

        <CardHeader>
          <CardTitle>
            Reservas Activas
          </CardTitle>
        </CardHeader>

        <CardContent>
          {renderTabla(activas)}
        </CardContent>

      </Card>

      <Card>
        <CardHeader>
          <CardTitle>
            Reservas Pendientes
          </CardTitle>
        </CardHeader>

        <CardContent>
          {renderTabla(pendientes)}
        </CardContent>

      </Card>
      <Card>
        <CardHeader>
          <CardTitle>
            Historial
          </CardTitle>
        </CardHeader>
        <CardContent>
          {renderTabla(historial)}
        </CardContent>
      </Card>
    </div>
  );
}