import { useState } from "react";

export default function EspacioEstadoDialog({ espacio, onClose, onSave }) {
  const [estado, setEstado] = useState(espacio.estado);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    try {
      setLoading(true);
      await onSave(estado);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center">
      <div className="bg-white p-6 rounded-lg w-80">
        <h2 className="text-lg font-bold mb-4">
          Cambiar estado
        </h2>

        {error && (
          <div className="text-red-500 mb-2">{error}</div>
        )}

        <select
          value={estado}
          onChange={(e) => setEstado(e.target.value)}
          className="w-full border p-2 rounded mb-4"
        >
          <option value="LIBRE">Libre</option>
          <option value="OCUPADO">Ocupado</option>
          <option value="RESERVADO">Reservado</option>
        </select>

        <div className="flex justify-end gap-2">
          <button onClick={onClose}>
            Cancelar
          </button>

          <button
            onClick={handleSubmit}
            className="bg-blue-600 text-white px-3 py-1 rounded"
          >
            {loading ? "Guardando..." : "Guardar"}
          </button>
        </div>
      </div>
    </div>
  );
}