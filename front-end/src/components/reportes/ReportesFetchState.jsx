import { AlertCircle, Loader2, RotateCcw } from "lucide-react";

import { Button } from "../ui/button";

export const ReportesFetchState = ({
  loading = false,
  loadingText = "Cargando datos...",
  errorMessage = "",
  canRetry = false,
  onRetry,
}) => {
  if (loading) {
    return (
      <div className="rounded-lg border bg-card p-3">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" />
          <span>{loadingText}</span>
        </div>
        <div className="mt-3 grid grid-cols-2 gap-2 md:grid-cols-4">
          <div className="h-16 animate-pulse rounded-md border bg-muted/40" />
          <div className="h-16 animate-pulse rounded-md border bg-muted/40" />
          <div className="h-16 animate-pulse rounded-md border bg-muted/40" />
          <div className="h-16 animate-pulse rounded-md border bg-muted/40" />
        </div>
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="rounded-lg border border-rose-300 bg-rose-50 p-3 text-rose-900">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-2">
            <AlertCircle className="mt-0.5 h-4 w-4" />
            <div>
              <p className="text-sm font-medium">No se pudo cargar la informacion</p>
              <p className="text-xs opacity-90">{errorMessage}</p>
            </div>
          </div>
          {canRetry && (
            <Button size="sm" variant="outline" onClick={onRetry}>
              <RotateCcw className="h-4 w-4" />
              Reintentar
            </Button>
          )}
        </div>
      </div>
    );
  }

  return null;
};
