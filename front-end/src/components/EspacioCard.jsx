import React from 'react';
import { Badge } from './ui/badge';
import { Car, Bike } from 'lucide-react';

const getStatusColor = (estado) => {
  const normEstado = typeof estado === 'string' ? estado.toUpperCase() : '';
  switch (normEstado) {
    case 'LIBRE':
      return 'bg-green-100 text-green-700';
    case 'OCUPADO':
      return 'bg-red-100 text-red-700';
    case 'RESERVADO':
      return 'bg-yellow-100 text-yellow-700';
    case 'MANTENIMIENTO':
      return 'bg-gray-100 text-gray-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
};

const getStatusBadgeVariant = (estado) => {
  const normEstado = typeof estado === 'string' ? estado.toUpperCase() : '';
  switch (normEstado) {
    case 'LIBRE':
      return 'outline';
    case 'OCUPADO':
      return 'destructive';
    case 'RESERVADO':
      return 'secondary';
    default:
      return 'default';
  }
};

export const EspacioCard = ({
  numero,
  estado,
  tipoVehiculo,
  ticketActivo
}) => {
  const vehicleType = typeof tipoVehiculo === 'string' ? tipoVehiculo.toLowerCase() : '';
  const isOcupado = typeof estado === 'string' && estado.toUpperCase() === 'OCUPADO';

  const Icon = vehicleType === 'moto' ? Bike : Car;

  return (
    <div
      className={`relative w-full rounded-lg border p-4 flex flex-col items-center justify-center text-center hover:shadow-md transition-all duration-200 ${getStatusColor(estado)}`}
    >
      <div className="absolute top-3 right-3 opacity-60">
        <Icon className="w-5 h-5 cursor-default" aria-hidden="true" />
      </div>

      <p className="text-2xl font-bold mb-2">{numero}</p>

      <Badge
        variant={getStatusBadgeVariant(estado)}
        className="text-xs uppercase mb-1"
      >
        {estado}
      </Badge>

      {isOcupado && ticketActivo && (
        <div className="mt-3 w-full pt-2 border-t border-current/20 flex flex-col gap-1 overflow-hidden">
          <p className="text-sm font-semibold truncate leading-none" title={ticketActivo.placa}>
            {ticketActivo.placa}
          </p>
          <p className="text-xs opacity-80 truncate" title={ticketActivo.horaEntrada}>
            {ticketActivo.horaEntrada}
          </p>
        </div>
      )}
    </div>
  );
};

export default EspacioCard;
