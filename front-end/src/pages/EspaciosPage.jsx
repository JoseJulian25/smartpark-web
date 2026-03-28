import { useState, useEffect } from "react";

import { getEspacios, updateEstadoEspacio, addEspaciosLote, deleteEspacio } from "../api/espacios";

import { Card, CardContent } from "../components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../components/ui/tabs";
import { Button } from "../components/ui/button";

import EspacioEstadoDialog from "../components/espacios/EspacioEstadoDialog";
import AddEspaciosDialog from "../components/espacios/AddEspaciosDialog";
import EspacioCard from "../components/espacios/EspacioCard";


import { Car, Bike, Plus } from "lucide-react";

export function EspaciosPage() {
  const [espacios, setEspacios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [selectedEspacio, setSelectedEspacio] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [openAddDialog, setOpenAddDialog] = useState(false);

  useEffect(() => {
    fetchEspacios();
  }, []);

  const fetchEspacios = async () => {
    try {
      setLoading(true);
      const data = await getEspacios();
      setEspacios(data);
      setError("");
    } catch (err) {
      setError("Error cargando espacios");
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (espacio) => {
    setSelectedEspacio(espacio);
    setOpenDialog(true);
  };

  const handleUpdateEstado = async (nuevoEstado) => {
    try {
      if (nuevoEstado === selectedEspacio.estado) {
      setOpenDialog(false);
      return;
    }
      await updateEstadoEspacio(selectedEspacio.id, nuevoEstado);

      setEspacios((prev) =>
        prev.map((e) =>
          e.id === selectedEspacio.id ? { ...e, estado: nuevoEstado } : e
        )
      );

      setOpenDialog(false);
      setSelectedEspacio(null);
      setError("");
    } catch (err) {
      setError("Error actualizando estado");
      throw err;
    }
  };

const handleAddEspacios = async (data) => {
  try {
    await addEspaciosLote({
      cantidadCarros: data.carros,
      cantidadMotos: data.motos
    });

    await fetchEspacios();
    setOpenAddDialog(false);

  } catch (error) {
    console.log(error);
    setError("Error agregando espacios");
  }
};

const handleDeleteEspacio = async (id) => {
  const confirmacion = window.confirm(
    "¿Seguro que deseas eliminar este espacio? Esta acción no se puede deshacer."
  );

  if (!confirmacion) return;

  try {
    await deleteEspacio(id);

    setEspacios((prev) => prev.filter((e) => e.id !== id));

    setError("");
  } catch (err) {
    setError("No se puede eliminar el espacio. Puede estar en uso.");
  }
};

  const espaciosCarros = espacios.filter(
    (e) => e.tipoVehiculo === "CARRO"
  );

  const espaciosMotos = espacios.filter(
    (e) => e.tipoVehiculo === "MOTO"
  );

  const stats = {
    libre: espacios.filter((e) => e.estado === "LIBRE").length,
    ocupado: espacios.filter((e) => e.estado === "OCUPADO").length,
    reservado: espacios.filter((e) => e.estado === "RESERVADO").length,
  };

  const renderEspacios = (lista) => {
    if (!lista.length) return <p>No hay espacios disponibles</p>;

    return (
      <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-3">
        {lista.map((space) => (
          <EspacioCard
            key={space.id}
            numero={space.numero || space.codigoEspacio}
            estado={space.estado}
            tipoVehiculo={space.tipoVehiculo}
            ticketActivo={space.ticketActivo}
            onEdit={() => handleOpenDialog(space)}
            onDelete={() => handleDeleteEspacio(space.id)}
            canDelete={space.estado === "LIBRE"}
          />
        ))}
      </div>
    );
  };

  if (loading) return <p>Cargando espacios...</p>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">
            Gestión de Espacios
          </h1>
        </div>

        <Button onClick={() => setOpenAddDialog(true)}>
          <Plus className="w-4 h-4 mr-2" />
          Agregar Espacios
        </Button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-300 text-red-600 p-3 rounded">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_260px]">
        <Card>
          <CardContent className="pt-6">
            <Tabs defaultValue="carros" className="space-y-4">
              <TabsList className="mx-auto grid w-full max-w-md grid-cols-2 border bg-white p-1">
                <TabsTrigger
                  value="carros"
                  className="border border-transparent bg-white text-slate-600 data-[state=active]:border-primary data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-none"
                >
                  <Car className="w-4 h-4 mr-2" />
                  Carros ({espaciosCarros.length})
                </TabsTrigger>

                <TabsTrigger
                  value="motos"
                  className="border border-transparent bg-white text-slate-600 data-[state=active]:border-primary data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-none"
                >
                  <Bike className="w-4 h-4 mr-2" />
                  Motos ({espaciosMotos.length})
                </TabsTrigger>
              </TabsList>

              <TabsContent value="carros">
                {renderEspacios(espaciosCarros)}
              </TabsContent>

              <TabsContent value="motos">
                {renderEspacios(espaciosMotos)}
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        <Card className="h-fit">
          <CardContent className="p-4">
            <div className="space-y-3">
              <div className="rounded-lg border bg-card p-3">
                <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Libres</p>
                <p className="text-2xl font-bold text-emerald-700">{stats.libre}</p>
              </div>

              <div className="rounded-lg border bg-card p-3">
                <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Ocupados</p>
                <p className="text-2xl font-bold text-rose-700">{stats.ocupado}</p>
              </div>

              <div className="rounded-lg border bg-card p-3">
                <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Reservados</p>
                <p className="text-2xl font-bold text-amber-700">{stats.reservado}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {openDialog && selectedEspacio && (
        <EspacioEstadoDialog
          espacio={selectedEspacio}
          onClose={() => setOpenDialog(false)}
          onSave={handleUpdateEstado}
        />
      )}

      <AddEspaciosDialog
        open={openAddDialog}
        onClose={() => setOpenAddDialog(false)}
        onSave={handleAddEspacios}
      />
    </div>
  );
}