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
