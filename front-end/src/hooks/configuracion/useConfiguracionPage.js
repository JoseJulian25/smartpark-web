import { useEffect, useState } from "react";

import { useEmpresaConfig } from "./useEmpresaConfig";
import { useTarifasConfig } from "./useTarifasConfig";

export const useConfiguracionPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const empresa = useEmpresaConfig({ onError: setError, onSuccess: setSuccess });
  const tarifas = useTarifasConfig({ onError: setError, onSuccess: setSuccess });
  const { loadEmpresa } = empresa;
  const { loadTarifas } = tarifas;

  useEffect(() => {
    const fetchConfiguraciones = async () => {
      try {
        setLoading(true);
        setError("");
        await Promise.all([loadEmpresa(), loadTarifas()]);
      } finally {
        setLoading(false);
      }
    };

    fetchConfiguraciones();
  }, [loadEmpresa, loadTarifas]);

  return {
    loading,
    error,
    success,
    empresa,
    tarifas,
  };
};
