import { Building2, Pencil } from "lucide-react";

import { Button } from "../ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Input } from "../ui/input";
import { Label } from "../ui/label";

export const GeneralSection = ({
  empresaForm,
  isEditingEmpresa,
  empresaInputLockClass,
  savingEmpresa,
  onToggleEdit,
  onChange,
  onSubmit,
}) => {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="flex items-center gap-2 text-xl">
          <Building2 className="w-5 h-5" />
          Información del Parqueo
        </CardTitle>
        <Button type="button" variant="outline" size="sm" onClick={onToggleEdit}>
          <Pencil className="w-4 h-4 mr-2" />
          {isEditingEmpresa ? "Cancelar" : "Editar"}
        </Button>
      </CardHeader>

      <CardContent>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="empresa-nombre">Nombre</Label>
              <Input
                id="empresa-nombre"
                disabled={!isEditingEmpresa}
                className={empresaInputLockClass}
                value={empresaForm.nombre}
                onChange={(e) => onChange("nombre", e.target.value)}
                placeholder="Ej: SmartPark SRL"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="empresa-rnc">RNC</Label>
              <Input
                id="empresa-rnc"
                disabled={!isEditingEmpresa}
                className={empresaInputLockClass}
                value={empresaForm.rnc}
                onChange={(e) => onChange("rnc", e.target.value)}
                placeholder="Ej: 131-99999-1"
              />
            </div>

            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="empresa-direccion">Dirección</Label>
              <Input
                id="empresa-direccion"
                disabled={!isEditingEmpresa}
                className={empresaInputLockClass}
                value={empresaForm.direccion}
                onChange={(e) => onChange("direccion", e.target.value)}
                placeholder="Ej: Av. 27 de Febrero, Santo Domingo"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="empresa-telefono">Teléfono</Label>
              <Input
                id="empresa-telefono"
                disabled={!isEditingEmpresa}
                className={empresaInputLockClass}
                value={empresaForm.telefono}
                onChange={(e) => onChange("telefono", e.target.value)}
                placeholder="Ej: 809-555-0000"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="empresa-email">Email</Label>
              <Input
                id="empresa-email"
                type="email"
                disabled={!isEditingEmpresa}
                className={empresaInputLockClass}
                value={empresaForm.email}
                onChange={(e) => onChange("email", e.target.value)}
                placeholder="Ej: info@smartpark.com"
              />
            </div>
          </div>

          {isEditingEmpresa && (
            <div className="flex gap-3 pt-4">
              <Button type="submit" className="flex-1" disabled={savingEmpresa}>
                {savingEmpresa ? "Guardando..." : "Guardar Configuración General"}
              </Button>
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
};
