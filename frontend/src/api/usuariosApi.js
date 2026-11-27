import httpClient from './httpClient';

/**
 * Registra un nuevo usuario en el sistema (HU-01).
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
 * Elimina o desactiva un usuario por su ID (solo ADMINISTRADORA).
 * @param {number} idUsuario
 */
export async function eliminarUsuario(idUsuario) {
  await httpClient.delete(`/api/v1/usuarios/${idUsuario}`);
}

/**
 * Alterna el estado activo/inactivo de un usuario por su ID (solo ADMINISTRADORA).
 * @param {number} idUsuario
 */
export async function cambiarEstadoUsuario(idUsuario) {
  const { data } = await httpClient.patch(`/api/v1/usuarios/${idUsuario}/estado`);
  return data;
}