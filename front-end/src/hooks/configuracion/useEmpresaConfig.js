import { useCallback, useState } from "react";
import toast from "react-hot-toast";

import {
  getEmpresa,
  updateEmpresa,
  getErrorMessage as getEmpresaErrorMessage,
} from "../../api/empresa";

const emptyEmpresa = {
  nombre: "",
  rnc: "",
  direccion: "",
  telefono: "",
  email: "",
};

export const useEmpresaConfig = ({ onError, onSuccess }) => {
  const [savingEmpresa, setSavingEmpresa] = useState(false);
  const [empresaForm, setEmpresaForm] = useState(emptyEmpresa);
  const [empresaBase, setEmpresaBase] = useState(emptyEmpresa);
  const [isEditingEmpresa, setIsEditingEmpresa] = useState(false);

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

  const loadEmpresa = useCallback(async () => {
    try {
      const data = await getEmpresa();
      const empresa = {
        nombre: data.nombre || "",
        rnc: data.rnc || "",
        direccion: data.direccion || "",
        telefono: data.telefono || "",
        email: data.email || "",
      };

      setEmpresaForm(empresa);
      setEmpresaBase(empresa);
    } catch (error) {
      if (error?.response?.status !== 404) {
        pushError(getEmpresaErrorMessage(error, "No se pudo cargar la empresa"));
      }
    }
  }, [pushError]);

  const handleEmpresaChange = useCallback((field, value) => {
    setEmpresaForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const handleGuardarEmpresa = useCallback(
    async (e) => {
      e.preventDefault();
      onError?.("");
      onSuccess?.("");

      const payload = {
        nombre: empresaForm.nombre.trim(),
        rnc: empresaForm.rnc.trim(),
        direccion: empresaForm.direccion.trim(),
        telefono: empresaForm.telefono.trim(),
        email: empresaForm.email.trim(),
      };

      if (!payload.nombre || !payload.rnc || !payload.direccion || !payload.telefono || !payload.email) {
        pushError("Todos los campos de empresa son requeridos");
        return;
      }

      try {
        setSavingEmpresa(true);
        const data = await updateEmpresa(payload);
        const empresa = {
          nombre: data.nombre || "",
          rnc: data.rnc || "",
          direccion: data.direccion || "",
          telefono: data.telefono || "",
          email: data.email || "",
        };

        setEmpresaForm(empresa);
        setEmpresaBase(empresa);
        setIsEditingEmpresa(false);
        pushSuccess("Configuración general guardada exitosamente");
      } catch (error) {
        pushError(getEmpresaErrorMessage(error, "No se pudo guardar la empresa"));
      } finally {
        setSavingEmpresa(false);
      }
    },
    [empresaForm, onError, onSuccess, pushError, pushSuccess]
  );

  const toggleEmpresaEditMode = useCallback(() => {
    if (isEditingEmpresa) {
      setEmpresaForm(empresaBase);
      onError?.("");
      onSuccess?.("");
    }

    setIsEditingEmpresa((prev) => !prev);
  }, [empresaBase, isEditingEmpresa, onError, onSuccess]);

  const empresaInputLockClass = !isEditingEmpresa
    ? "bg-primary/5 border-primary/20 text-foreground/80"
    : "";

  return {
    savingEmpresa,
    empresaForm,
    isEditingEmpresa,
    empresaInputLockClass,
    loadEmpresa,
    handleEmpresaChange,
    handleGuardarEmpresa,
    toggleEmpresaEditMode,
  };
};
