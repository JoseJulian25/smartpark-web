export const buildParams = (params = {}) => {
  const clean = {};
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      clean[key] = value;
    }
  });
  return clean;
};

export const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

export const NO_TIMEOUT_CONFIG = { timeout: 0 };
