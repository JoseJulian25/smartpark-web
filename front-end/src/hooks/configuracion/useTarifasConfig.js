import { useCallback, useMemo, useState } from "react";
import toast from "react-hot-toast";

import {
  getTarifas,
  updateTarifas,
  getErrorMessage as getTarifasErrorMessage,
} from "../../api/tarifas";

const emptyTarifas = {
  tarifaCarro: "",
  tarifaMoto: "",
  minutosFraccion: "",
  minutosTolerancia: "",
  minutosMinimo: "",
};

export const useTarifasConfig = ({ onError, onSuccess }) => {
  const [savingTarifas, setSavingTarifas] = useState(false);
  const [tarifasForm, setTarifasForm] = useState(emptyTarifas);
  const [tarifasBase, setTarifasBase] = useState(emptyTarifas);
  const [ultimaActualizacionTarifas, setUltimaActualizacionTarifas] = useState(null);
  const [isEditingTarifas, setIsEditingTarifas] = useState(false);

  const pushError = useCallback(
    (message) => {
      onError?.(message);
      toast.error(message);
    },
    [onError]
  );

  const pushSuccess = useCallback(
    (message) => {
      onSuccess?.(message);
      toast.success(message);
    },
    [onSuccess]
  );

  const parseInteger = (value) => {
    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
  };

  const parseDecimal = (value) => {
    const parsed = Number.parseFloat(value);
    return Number.isNaN(parsed) ? null : parsed;
  };

  const parseNumberForDisplay = (value) => {
    const parsed = Number.parseFloat(value || "0");
    return Number.isNaN(parsed) ? 0 : parsed;
  };

  const parseIntForDisplay = (value) => {
    const parsed = Number.parseInt(value || "0", 10);
    return Number.isNaN(parsed) ? 0 : parsed;
  };

  const loadTarifas = useCallback(async () => {
    try {
      const data = await getTarifas();
      const tarifas = {
        tarifaCarro: data.tarifaCarro?.toString() || "",
        tarifaMoto: data.tarifaMoto?.toString() || "",
        minutosFraccion: data.minutosFraccion?.toString() || "",
        minutosTolerancia: data.minutosTolerancia?.toString() || "",
        minutosMinimo: data.minutosMinimo?.toString() || "",
      };

      setTarifasForm(tarifas);
      setTarifasBase(tarifas);
      setUltimaActualizacionTarifas(data.actualizadoEn || null);
    } catch (error) {
      pushError(getTarifasErrorMessage(error, "No se pudieron cargar las tarifas"));
    }
  }, [pushError]);

  const handleTarifasChange = useCallback((field, value) => {
    setTarifasForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const handleGuardarTarifas = useCallback(
    async (e) => {
      e.preventDefault();
      onError?.("");
      onSuccess?.("");

      const payload = {
        tarifaCarro: parseDecimal(tarifasForm.tarifaCarro),
        tarifaMoto: parseDecimal(tarifasForm.tarifaMoto),
        minutosFraccion: parseInteger(tarifasForm.minutosFraccion),
        minutosTolerancia: parseInteger(tarifasForm.minutosTolerancia),
        minutosMinimo: parseInteger(tarifasForm.minutosMinimo),
      };

      if (
        payload.tarifaCarro === null ||
        payload.tarifaMoto === null ||
        payload.minutosFraccion === null ||
        payload.minutosTolerancia === null ||
        payload.minutosMinimo === null
      ) {
        pushError("Todos los campos de tarifas deben ser numéricos");
        return;
      }

      try {
        setSavingTarifas(true);
        const data = await updateTarifas(payload);
        const tarifas = {
          tarifaCarro: data.tarifaCarro?.toString() || "",
          tarifaMoto: data.tarifaMoto?.toString() || "",
          minutosFraccion: data.minutosFraccion?.toString() || "",
          minutosTolerancia: data.minutosTolerancia?.toString() || "",
          minutosMinimo: data.minutosMinimo?.toString() || "",
        };

        setTarifasForm(tarifas);
        setTarifasBase(tarifas);
        setUltimaActualizacionTarifas(data.actualizadoEn || null);
        setIsEditingTarifas(false);
        pushSuccess("Tarifas actualizadas exitosamente");
      } catch (error) {
        pushError(getTarifasErrorMessage(error, "No se pudieron actualizar las tarifas"));
      } finally {
        setSavingTarifas(false);
      }
    },
    [onError, onSuccess, pushError, pushSuccess, tarifasForm]
  );

  const toggleTarifasEditMode = useCallback(() => {
    if (isEditingTarifas) {
      setTarifasForm(tarifasBase);
      onError?.("");
      onSuccess?.("");
    }

    setIsEditingTarifas((prev) => !prev);
  }, [isEditingTarifas, onError, onSuccess, tarifasBase]);

  const formatDateTime = useCallback((value) => {
    if (!value) {
      return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "-";
    }

    return date.toLocaleString("es-DO");
  }, []);

  const calcularEjemplo = useCallback(
    (minutos, tipo) => {
      const tarifa =
        tipo === "carro"
          ? parseNumberForDisplay(tarifasForm.tarifaCarro)
          : parseNumberForDisplay(tarifasForm.tarifaMoto);
      const fraccion = parseIntForDisplay(tarifasForm.minutosFraccion);
      const tolerancia = parseIntForDisplay(tarifasForm.minutosTolerancia);
      const minimo = parseIntForDisplay(tarifasForm.minutosMinimo);

      if (tarifa <= 0 || fraccion <= 0) {
        return 0;
      }

      const tiempoFacturable = Math.max(0, minutos - tolerancia);
      const tiempoACobrar = Math.max(tiempoFacturable, minimo);
      const fracciones = Math.ceil(tiempoACobrar / fraccion);
      return fracciones * tarifa;
    },
    [tarifasForm]
  );

  const tarifasInputLockClass = !isEditingTarifas
    ? "bg-primary/5 border-primary/20 text-foreground/80"
    : "";

  const tarifaCarroActual = useMemo(
    () => parseNumberForDisplay(tarifasForm.tarifaCarro),
    [tarifasForm.tarifaCarro]
  );
  const tarifaMotoActual = useMemo(
    () => parseNumberForDisplay(tarifasForm.tarifaMoto),
    [tarifasForm.tarifaMoto]
  );
  const minutosFraccionActual = useMemo(
    () => parseIntForDisplay(tarifasForm.minutosFraccion),
    [tarifasForm.minutosFraccion]
  );
  const toleranciaActual = useMemo(
    () => parseIntForDisplay(tarifasForm.minutosTolerancia),
    [tarifasForm.minutosTolerancia]
  );
  const minimoActual = useMemo(
    () => parseIntForDisplay(tarifasForm.minutosMinimo),
    [tarifasForm.minutosMinimo]
  );

  return {
    savingTarifas,
    tarifasForm,
    ultimaActualizacionTarifas,
    isEditingTarifas,
    tarifasInputLockClass,
    tarifaCarroActual,
    tarifaMotoActual,
    minutosFraccionActual,
    toleranciaActual,
    minimoActual,
    loadTarifas,
    handleTarifasChange,
    handleGuardarTarifas,
    toggleTarifasEditMode,
    formatDateTime,
    calcularEjemplo,
  };
};
