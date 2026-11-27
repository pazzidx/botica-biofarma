import httpClient from './httpClient';

/**
 * Obtiene la lista de medicamentos del catálogo (HU-04).
 * @param {boolean|undefined} soloActivos Si es true, filtra solo medicamentos activos
 */
export async function listarMedicamentos(soloActivos) {
  const params = soloActivos !== undefined ? { soloActivos } : undefined;
  const { data } = await httpClient.get('/api/v1/medicamentos', { params });
  return data;
}

/**
 * Registra un nuevo medicamento en el catálogo (HU-04).
 * @param {Object} payload Datos del medicamento
 */
export async function registrarMedicamento(payload) {
  const { data } = await httpClient.post('/api/v1/medicamentos', payload);
  return data;
}

/**
 * Actualiza los datos comerciales de un medicamento existente (HU-04).
 * @param {number} idProducto ID del medicamento
 * @param {Object} payload Datos actualizados
 */
export async function actualizarMedicamento(idProducto, payload) {
  const { data } = await httpClient.put(`/api/v1/medicamentos/${idProducto}`, payload);
  return data;
}

/**
 * Desactiva lógicamente un medicamento del catálogo (HU-04).
 * @param {number} idProducto ID del medicamento
 */
export async function desactivarMedicamento(idProducto) {
  const { data } = await httpClient.patch(`/api/v1/medicamentos/${idProducto}/desactivar`);
  return data;
}
