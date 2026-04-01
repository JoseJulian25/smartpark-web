import { Bike, Car, Clock, DollarSign, Pencil, Settings } from "lucide-react";

import { Button } from "../ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Input } from "../ui/input";
import { Label } from "../ui/label";

export const TarifasSection = ({
  tarifasForm,
  isEditingTarifas,
  tarifasInputLockClass,
  savingTarifas,
  ultimaActualizacionTarifas,
  tarifaCarroActual,
  tarifaMotoActual,
  minutosFraccionActual,
  toleranciaActual,
  minimoActual,
  onToggleEdit,
  onChange,
  onSubmit,
  formatDateTime,
  calcularEjemplo,
}) => {
  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Tarifa Actual - Carros</CardTitle>
            <Car className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">${tarifaCarroActual.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Por cada {minutosFraccionActual} minutos
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Tarifa Actual - Motos</CardTitle>
            <Bike className="w-4 h-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">${tarifaMotoActual.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Por cada {minutosFraccionActual} minutos
            </p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-xl">
            <Settings className="w-5 h-5" />
            Configuración de Tarifas
          </CardTitle>
          <Button type="button" variant="outline" size="sm" onClick={onToggleEdit}>
            <Pencil className="w-4 h-4 mr-2" />
            {isEditingTarifas ? "Cancelar" : "Editar"}
          </Button>
        </CardHeader>

        <CardContent>
          <form onSubmit={onSubmit} className="space-y-6">
            <div>
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <DollarSign className="w-4 h-4" />
                Tarifas por Tipo de Vehículo
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="tarifa-carro" className="flex items-center gap-2">
                    <Car className="w-4 h-4" />
                    Tarifa para Carros ($)
                  </Label>
                  <Input
                    id="tarifa-carro"
                    type="number"
                    disabled={!isEditingTarifas}
                    className={tarifasInputLockClass}
                    min="0"
                    step="0.01"
                    value={tarifasForm.tarifaCarro}
                    onChange={(e) => onChange("tarifaCarro", e.target.value)}
                    placeholder="Ej: 200"
                  />
                  <p className="text-xs text-muted-foreground">Monto por cada fracción de tiempo</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="tarifa-moto" className="flex items-center gap-2">
                    <Bike className="w-4 h-4" />
                    Tarifa para Motos ($)
                  </Label>
                  <Input
                    id="tarifa-moto"
                    type="number"
                    disabled={!isEditingTarifas}
                    className={tarifasInputLockClass}
                    min="0"
                    step="0.01"
                    value={tarifasForm.tarifaMoto}
                    onChange={(e) => onChange("tarifaMoto", e.target.value)}
                    placeholder="Ej: 100"
                  />
                  <p className="text-xs text-muted-foreground">Monto por cada fracción de tiempo</p>
                </div>
              </div>
            </div>

            <div>
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <Clock className="w-4 h-4" />
                Parámetros de Cobro
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="minutos-fraccion">Fracción de Tiempo (min)</Label>
                  <Input
                    id="minutos-fraccion"
                    type="number"
                    disabled={!isEditingTarifas}
                    className={tarifasInputLockClass}
                    min="1"
                    step="1"
                    value={tarifasForm.minutosFraccion}
                    onChange={(e) => onChange("minutosFraccion", e.target.value)}
                  />
                  <p className="text-xs text-muted-foreground">Cada cuántos minutos se cobra</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="minutos-tolerancia">Tolerancia (min)</Label>
                  <Input
                    id="minutos-tolerancia"
                    type="number"
                    disabled={!isEditingTarifas}
                    className={tarifasInputLockClass}
                    min="0"
                    step="1"
                    value={tarifasForm.minutosTolerancia}
                    onChange={(e) => onChange("minutosTolerancia", e.target.value)}
                  />
                  <p className="text-xs text-muted-foreground">Minutos de gracia al salir</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="minutos-minimo">Cobro Mínimo (min)</Label>
                  <Input
                    id="minutos-minimo"
                    type="number"
                    disabled={!isEditingTarifas}
                    className={tarifasInputLockClass}
                    min="0"
                    step="1"
                    value={tarifasForm.minutosMinimo}
                    onChange={(e) => onChange("minutosMinimo", e.target.value)}
                  />
                  <p className="text-xs text-muted-foreground">Tiempo mínimo a cobrar</p>
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <p className="text-sm text-muted-foreground">Última actualización: {formatDateTime(ultimaActualizacionTarifas)}</p>
              {isEditingTarifas && (
                <div className="flex gap-3">
                  <Button type="submit" className="flex-1" disabled={savingTarifas}>
                    {savingTarifas ? "Guardando..." : "Guardar Tarifas"}
                  </Button>
                  <Button type="button" variant="outline" onClick={onToggleEdit}>
                    Cancelar
                  </Button>
                </div>
              )}
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Ejemplos de Cálculo</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <p className="text-sm text-muted-foreground">
              Con la configuración actual, estos serían algunos ejemplos de cobro:
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-3">
                <h4 className="font-medium flex items-center gap-2">
                  <Car className="w-4 h-4" />
                  Carros
                </h4>
                <div className="space-y-2">
                  {[25, 40, 65, 120].map((minutos) => (
                    <div key={`carro-${minutos}`} className="flex justify-between p-2 bg-gray-50 rounded">
                      <span className="text-sm">{minutos} minutos</span>
                      <span className="font-medium text-primary">${calcularEjemplo(minutos, "carro").toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="space-y-3">
                <h4 className="font-medium flex items-center gap-2">
                  <Bike className="w-4 h-4" />
                  Motos
                </h4>
                <div className="space-y-2">
                  {[25, 40, 65, 120].map((minutos) => (
                    <div key={`moto-${minutos}`} className="flex justify-between p-2 bg-gray-50 rounded">
                      <span className="text-sm">{minutos} minutos</span>
                      <span className="font-medium text-primary">${calcularEjemplo(minutos, "moto").toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="p-4 bg-primary/10 border border-primary/30 rounded-lg">
              <p className="text-sm text-primary">
                <strong>Nota:</strong> Los primeros {toleranciaActual} minutos son de tolerancia. El cobro mínimo es de {minimoActual} minutos, incluso si el vehículo permanece menos tiempo.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </>
  );
};
