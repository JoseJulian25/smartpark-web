
import EspacioCard from '../components/EspacioCard';

export const EspaciosPage = () => {
  return (
    <div className="p-6">
      <div className="max-w-xs">
        <EspacioCard
          numero="A-01"
          estado="LIBRE"
          tipoVehiculo="AUTO"
          ticketActivo={{ placa: 'ABC-123', horaEntrada: '08:45' }}
        />
      </div>
    </div>
  );
};


