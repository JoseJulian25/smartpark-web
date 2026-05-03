import { Layers3, PlusCircle } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";

import { addEspaciosLote, deleteEspacio, getEspacios, getEspaciosInactivos, reactivarEspacio } from "../../api/espacios";
import AddEspaciosDialog from "../espacios/AddEspaciosDialog";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";

export const EspaciosSection = () => {
  const PAGE_SIZE = 20;
  const [espacios, setEspacios] = useState([]);
  const [espaciosInactivos, setEspaciosInactivos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingActionId, setLoadingActionId] = useState(null);
  const [openAddDialog, setOpenAddDialog] = useState(false);
  const [categoria, setCategoria] = useState("total");
  const [page, setPage] = useState(1);

  const fetchEspacios = async () => {
    try {
      setLoading(true);
      const [activos, inactivos] = await Promise.all([getEspacios(), getEspaciosInactivos()]);
      setEspacios(Array.isArray(activos) ? activos : []);
      setEspaciosInactivos(Array.isArray(inactivos) ? inactivos : []);
    } catch (error) {
      toast.error("No se pudo cargar el inventario de espacios");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEspacios();
  }, []);

  useEffect(() => {
    setPage(1);
  }, [categoria, espaciosInactivos.length, espacios.length]);

  const handleAgregarLote = async (payload) => {
    try {
      await addEspaciosLote(payload);
      toast.success("Espacios agregados correctamente");
      await fetchEspacios();
    } catch (error) {
      const message = error?.response?.data?.message || "No se pudieron agregar espacios";
      toast.error(message);
      throw error;
    }
  };

  const handleDesactivar = async (espacio) => {
    if (!espacio?.id) return;
    if (String(espacio.estado || "").toUpperCase() !== "LIBRE") {
      toast.error("Solo se pueden desactivar espacios en estado LIBRE");
      return;
    }

    const confirmed = window.confirm(`Desactivar ${espacio.codigoEspacio}? Se marcara como activo=0.`);
    if (!confirmed) return;

    try {
      setLoadingActionId(espacio.id);
      await deleteEspacio(espacio.id);
      toast.success("Espacio desactivado (activo=0)");
      await fetchEspacios();
    } catch (error) {
      const message = error?.response?.data?.message || "No se pudo desactivar el espacio";
      toast.error(message);
    } finally {
      setLoadingActionId(null);
    }
  };

  const handleReactivar = async (espacio) => {
    if (!espacio?.id) return;
    try {
      setLoadingActionId(espacio.id);
      await reactivarEspacio(espacio.id);
      toast.success("Espacio reactivado (activo=1)");
      await fetchEspacios();
    } catch (error) {
      const message = error?.response?.data?.message || "No se pudo reactivar el espacio";
      toast.error(message);
    } finally {
      setLoadingActionId(null);
    }
  };

  const espaciosCombinados = useMemo(() => {
    const map = new Map();
    espacios.forEach((espacio) => {
      if (espacio?.id !== undefined) {
        map.set(espacio.id, espacio);
      }
    });
    espaciosInactivos.forEach((espacio) => {
      if (espacio?.id !== undefined) {
        map.set(espacio.id, espacio);
      }
    });
    return Array.from(map.values());
  }, [espacios, espaciosInactivos]);

  const resumen = useMemo(() => {
    const libres = espacios.filter((espacio) => String(espacio.estado || "").toUpperCase() === "LIBRE").length;
    const carros = espaciosCombinados.filter((espacio) => String(espacio.tipoVehiculo || "").toUpperCase() === "CARRO").length;
    const motos = espaciosCombinados.filter((espacio) => String(espacio.tipoVehiculo || "").toUpperCase() === "MOTO").length;

    return {
      total: espaciosCombinados.length,
      libres,
      carros,
      motos,
      inactivos: espaciosInactivos.length,
    };
  }, [espacios, espaciosInactivos, espaciosCombinados]);

  const espaciosFiltrados = useMemo(() => {
    if (categoria === "carros") {
      return espaciosCombinados.filter((e) => String(e.tipoVehiculo || "").toUpperCase() === "CARRO");
    }

    if (categoria === "motos") {
      return espaciosCombinados.filter((e) => String(e.tipoVehiculo || "").toUpperCase() === "MOTO");
    }

    if (categoria === "libres") {
      return espacios.filter((e) => String(e.estado || "").toUpperCase() === "LIBRE");
    }

    if (categoria === "inactivos") {
      return espaciosInactivos;
    }

    return espaciosCombinados;
  }, [categoria, espaciosCombinados, espacios, espaciosInactivos]);

  const espaciosInactivosIds = useMemo(() => new Set(espaciosInactivos.map((e) => e.id)), [espaciosInactivos]);

  const isEspacioActivo = (espacio) => {
    if (espacio?.activo !== undefined && espacio?.activo !== null) {
      const value = String(espacio.activo).trim().toLowerCase();
      return value === "1" || value === "true";
    }

    return !espaciosInactivosIds.has(espacio?.id);
  };

  const totalPages = Math.max(1, Math.ceil(espaciosFiltrados.length / PAGE_SIZE));
  const safePage = Math.min(page, totalPages);
  const startIndex = (safePage - 1) * PAGE_SIZE;
  const espaciosPaginados = espaciosFiltrados.slice(startIndex, startIndex + PAGE_SIZE);

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-xl">
            <Layers3 className="h-5 w-5" />
            Gestión de Espacios
          </CardTitle>
          <Button onClick={() => setOpenAddDialog(true)} className="bg-primary text-primary-foreground hover:bg-primary/90">
            <PlusCircle className="mr-2 h-4 w-4" />
            Agregar lote
          </Button>
        </CardHeader>

        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Aquí se gestiona capacidad y activación lógica (activo=0/1). El estado operativo del parqueo se controla por el flujo normal del sistema.
          </p>

          <div className="flex flex-wrap gap-2">
            <Badge variant="outline" className="cursor-pointer" onClick={() => setCategoria("total")}>
              Total: {resumen.total}
            </Badge>

            <Badge variant="outline" className="cursor-pointer" onClick={() => setCategoria("carros")}>
              Carros: {resumen.carros}
            </Badge>

            <Badge variant="outline" className="cursor-pointer" onClick={() => setCategoria("motos")}>
              Motos: {resumen.motos}
            </Badge>

            <Badge variant="outline" className="cursor-pointer" onClick={() => setCategoria("libres")}>
              Libres: {resumen.libres}
            </Badge>

            <Badge variant="outline" className="cursor-pointer" onClick={() => setCategoria("inactivos")}>
              Inactivos: {resumen.inactivos}
            </Badge>
          </div>

          <Table className="text-xs">
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-2">Código</TableHead>
                <TableHead className="h-9 px-2">Tipo</TableHead>
                <TableHead className="h-9 px-2">Estado</TableHead>
                <TableHead className="h-9 px-2">Activo</TableHead>
                <TableHead className="h-9 px-2 text-right">Acción</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={5} className="py-6 text-center text-xs text-muted-foreground">
                    Cargando espacios...
                  </TableCell>
                </TableRow>
                ) : !espaciosFiltrados.length ? (
                <TableRow>
                  <TableCell colSpan={5} className="py-6 text-center text-xs text-muted-foreground">
                    No hay espacios registrados.
                  </TableCell>
                </TableRow>
              ) : (
                espaciosPaginados.map((espacio) => {
                  const activo = isEspacioActivo(espacio);
                  return (
                    <TableRow key={espacio.id}>
                      <TableCell className="px-2 py-2 font-medium">{espacio.codigoEspacio || espacio.numero || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{espacio.tipoVehiculo || "-"}</TableCell>
                      <TableCell className="px-2 py-2">{espacio.estado || "-"}</TableCell>
                      <TableCell className="px-2 py-2">
                        <Badge
                          variant="outline"
                          className={activo ? "border-emerald-300 text-emerald-700" : "border-rose-300 text-rose-700"}
                        >
                          {activo ? "Sí" : "No"}
                        </Badge>
                      </TableCell>
                      <TableCell className="px-2 py-2 text-right">
                        <Button
                          size="sm"
                          variant="outline"
                          disabled={loadingActionId === espacio.id || (activo && String(espacio.estado || "").toUpperCase() !== "LIBRE")}
                          onClick={() => (activo ? handleDesactivar(espacio) : handleReactivar(espacio))}
                        >
                          {loadingActionId === espacio.id ? "Procesando..." : activo ? "Desactivar" : "Reactivar"}
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>

          <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-muted-foreground">
            <span>
              Mostrando {espaciosFiltrados.length ? startIndex + 1 : 0} a {Math.min(startIndex + PAGE_SIZE, espaciosFiltrados.length)} de {espaciosFiltrados.length}.
            </span>
            <div className="flex items-center gap-2">
              <Button
                size="sm"
                variant="outline"
                disabled={safePage === 1}
                onClick={() => setPage((prev) => Math.max(1, prev - 1))}
              >
                Anterior
              </Button>
              <span>
                Pagina {safePage} de {totalPages}
              </span>
              <Button
                size="sm"
                variant="outline"
                disabled={safePage === totalPages}
                onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))}
              >
                Siguiente
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <AddEspaciosDialog
        open={openAddDialog}
        onClose={() => setOpenAddDialog(false)}
        onSave={handleAgregarLote}
      />
    </div>
  );
};
