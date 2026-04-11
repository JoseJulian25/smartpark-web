import { Building2, DollarSign } from "lucide-react";

import { Card, CardContent } from "../components/ui/card";
import { GeneralSection } from "../components/configuracion/GeneralSection";
import { TarifasSection } from "../components/configuracion/TarifasSection";
import { useConfiguracionPage } from "../hooks/configuracion/useConfiguracionPage";

export const ConfiguracionPage = ({ initialTab = "general" }) => {
  const { loading, empresa, tarifas } = useConfiguracionPage();
  const activeTab = initialTab === "tarifas" ? "tarifas" : "general";

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-3xl font-bold mb-2">Configuración del Sistema</h1>
        <p className="text-muted-foreground">
          Configure los parámetros generales y tarifas del parqueo
        </p>
      </div>

      {loading ? (
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground">Cargando configuración...</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          {activeTab === "general" && (
            <GeneralSection
              empresaForm={empresa.empresaForm}
              isEditingEmpresa={empresa.isEditingEmpresa}
              empresaInputLockClass={empresa.empresaInputLockClass}
              savingEmpresa={empresa.savingEmpresa}
              onToggleEdit={empresa.toggleEmpresaEditMode}
              onChange={empresa.handleEmpresaChange}
              onSubmit={empresa.handleGuardarEmpresa}
            />
          )}

          {activeTab === "tarifas" && (
            <TarifasSection
              tarifasForm={tarifas.tarifasForm}
              isEditingTarifas={tarifas.isEditingTarifas}
              tarifasInputLockClass={tarifas.tarifasInputLockClass}
              savingTarifas={tarifas.savingTarifas}
              ultimaActualizacionTarifas={tarifas.ultimaActualizacionTarifas}
              tarifaCarroActual={tarifas.tarifaCarroActual}
              tarifaMotoActual={tarifas.tarifaMotoActual}
              minutosFraccionActual={tarifas.minutosFraccionActual}
              toleranciaActual={tarifas.toleranciaActual}
              minimoActual={tarifas.minimoActual}
              onToggleEdit={tarifas.toggleTarifasEditMode}
              onChange={tarifas.handleTarifasChange}
              onSubmit={tarifas.handleGuardarTarifas}
              formatDateTime={tarifas.formatDateTime}
              calcularEjemplo={tarifas.calcularEjemplo}
            />
          )}
        </div>
      )}
    </div>
  );
};