import { Building2, DollarSign } from "lucide-react";

import { Card, CardContent } from "../components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../components/ui/tabs";
import { GeneralSection } from "../components/configuracion/GeneralSection";
import { TarifasSection } from "../components/configuracion/TarifasSection";
import { useConfiguracionPage } from "../hooks/configuracion/useConfiguracionPage";

export const ConfiguracionPage = () => {
  const { loading, empresa, tarifas } = useConfiguracionPage();

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
        <Tabs defaultValue="general" className="space-y-6">
          <TabsList className="grid w-full max-w-md grid-cols-2 border bg-white p-1">
            <TabsTrigger
              value="general"
              className="border border-transparent bg-white text-slate-600 data-[state=active]:border-primary data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
            >
              <Building2 className="w-4 h-4 mr-2" />
              General
            </TabsTrigger>

            <TabsTrigger
              value="tarifas"
              className="border border-transparent bg-white text-slate-600 data-[state=active]:border-primary data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
            >
              <DollarSign className="w-4 h-4 mr-2" />
              Tarifas
            </TabsTrigger>
          </TabsList>

          <TabsContent value="general" className="space-y-6">
            <GeneralSection
              empresaForm={empresa.empresaForm}
              isEditingEmpresa={empresa.isEditingEmpresa}
              empresaInputLockClass={empresa.empresaInputLockClass}
              savingEmpresa={empresa.savingEmpresa}
              onToggleEdit={empresa.toggleEmpresaEditMode}
              onChange={empresa.handleEmpresaChange}
              onSubmit={empresa.handleGuardarEmpresa}
            />
          </TabsContent>

          <TabsContent value="tarifas" className="space-y-6">
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
          </TabsContent>
        </Tabs>
      )}
    </div>
  );
};