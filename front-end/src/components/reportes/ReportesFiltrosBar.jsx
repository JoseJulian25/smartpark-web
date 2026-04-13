import { RefreshCw } from "lucide-react";

import { Button } from "../ui/button";
import { Input } from "../ui/input";

export const ReportesFiltrosBar = ({
  fechaDesde,
  fechaHasta,
  onFechaDesdeChange,
  onFechaHastaChange,
  onLimpiar,
  onActualizar,
  loading = false,
  children = null,
  columnsClassName = "md:grid-cols-4",
  actionsSpanClassName = "md:col-span-2",
}) => {
  return (
    <div className="rounded-lg border bg-card p-3">
      <div className={`grid grid-cols-1 gap-2 ${columnsClassName}`}>
        <Input type="datetime-local" value={fechaDesde} onChange={(e) => onFechaDesdeChange(e.target.value)} />
        <Input type="datetime-local" value={fechaHasta} onChange={(e) => onFechaHastaChange(e.target.value)} />

        {children}

        <div className={`${actionsSpanClassName} flex items-center justify-end gap-2`}>
          <Button size="sm" variant="outline" onClick={onLimpiar} disabled={loading}>
            Limpiar
          </Button>
          <Button size="sm" onClick={onActualizar} disabled={loading}>
            <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Actualizar
          </Button>
        </div>
      </div>
    </div>
  );
};
