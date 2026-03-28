import { Badge } from '../ui/badge';
import { Car, Bike, Settings, X } from 'lucide-react';

const getStatusStyles = (estado) => {
  const normEstado = typeof estado === 'string' ? estado.toUpperCase() : '';

  switch (normEstado) {
    case 'LIBRE':
      return {
        container: 'bg-emerald-100/80 border-emerald-300/80 text-emerald-900',
        badge: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      };
    case 'OCUPADO':
      return {
        container: 'bg-rose-100/80 border-rose-300/80 text-rose-900',
        badge: 'bg-rose-50 text-rose-700 border-rose-200',
      };
    case 'RESERVADO':
      return {
        container: 'bg-amber-100/80 border-amber-300/80 text-amber-900',
        badge: 'bg-amber-50 text-amber-700 border-amber-200',
      };
    default:
      return {
        container: 'bg-slate-50/90 border-slate-200/90 text-slate-800',
        badge: 'bg-slate-50 text-slate-700 border-slate-200',
      };
  }
};

export const EspacioCard = ({
  numero,
  estado,
  tipoVehiculo,
  ticketActivo,
  onEdit,
  onDelete,
  canDelete = false,
}) => {
  const vehicleType = typeof tipoVehiculo === 'string' ? tipoVehiculo.toLowerCase() : '';
  const Icon = vehicleType === 'moto' ? Bike : Car;
  const statusStyles = getStatusStyles(estado);

  return (
    <div
      className={`relative group overflow-hidden rounded-lg border p-4 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md ${statusStyles.container}`}
    >
      <div className="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition">
        <button
          onClick={onEdit}
          className="rounded bg-slate-700/80 p-1 text-white"
          type="button"
        >
          <Settings className="w-4 h-4" />
        </button>
        {canDelete && (
          <button
            onClick={onDelete}
            className="rounded bg-rose-600/90 p-1 text-white"
            type="button"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      <div className="absolute bottom-2 right-2 opacity-40">
        <Icon className="w-4 h-4" aria-hidden="true" />
      </div>

      <div className="text-center pl-1">
        <p className="text-2xl font-bold mb-1">{numero}</p>

        <Badge variant="outline" className={statusStyles.badge}>{estado}</Badge>

        {ticketActivo && (
          <div className="mt-2 border-t border-border pt-2">
            <p className="text-xs font-medium truncate">{ticketActivo.placa}</p>
            <p className="text-xs text-muted-foreground">{ticketActivo.horaEntrada}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default EspacioCard;
