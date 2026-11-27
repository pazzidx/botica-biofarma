import httpClient from './httpClient';

/**
 * Registra una nueva trabajadora en el sistema (HU-01).
 * @param {Object} payload Datos del usuario a registrar
 */
export async function registrarUsuario(payload) {
  const { data } = await httpClient.post('/api/v1/usuarios', payload);
  return data;
}

/**
 * Obtiene la lista de usuarios registrados (HU-01).
 */
export async function listarUsuarios() {
  const { data } = await httpClient.get('/api/v1/usuarios');
  return data;
}

/**
 * Actualiza únicamente nombres y apellidos de una trabajadora (HU-01).
 * @param {number} idUsuario Identificador del usuario
 * @param {{ nombres: string, apellidos: string }} payload
 */
export async function actualizarUsuario(idUsuario, payload) {
  const { data } = await httpClient.put(`/api/v1/usuarios/${idUsuario}`, payload);
  return data;
}

/**
 * Elimina físicamente si no tiene ventas o desactiva lógicamente si registra ventas (HU-01).
 * @param {number} idUsuario Identificador del usuario
 */
export async function eliminarUsuario(idUsuario) {
  const { data } = await httpClient.delete(`/api/v1/usuarios/${idUsuario}`);
  return data;
}

/**
 * Reactiva una cuenta inactiva (HU-01).
 * @param {number} idUsuario Identificador del usuario
 */
export async function activarUsuario(idUsuario) {
  const { data } = await httpClient.patch(`/api/v1/usuarios/${idUsuario}/activar`);
  return data;
}
