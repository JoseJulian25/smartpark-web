const pad = (value) => String(value).padStart(2, "0");

const sanitize = (value) => {
  return String(value || "reporte")
    .trim()
    .toLowerCase()
    .replace(/\s+/g, "_")
    .replace(/[^a-z0-9_\-]/g, "");
};

const timestampForFile = (date = new Date()) => {
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  return `${year}${month}${day}_${hours}${minutes}`;
};

export const buildProfessionalReportFileName = ({ modulo, tipoReporte, extension }) => {
  const moduloSafe = sanitize(modulo || "modulo");
  const tipoSafe = sanitize(tipoReporte || "reporte");
  const extSafe = String(extension || "pdf").toLowerCase().replace(/[^a-z0-9]/g, "");
  return `${moduloSafe}_${tipoSafe}_${timestampForFile()}.${extSafe}`;
};

export const triggerFileDownload = (blob, fileName) => {
  const url = window.URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName || "reporte";
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.URL.revokeObjectURL(url);
};
