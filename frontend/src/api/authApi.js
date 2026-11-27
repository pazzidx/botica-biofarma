import httpClient from './httpClient';

/**
 * Autentica un usuario en el sistema de Botica Biofarma (HU-02 / BIO-30 / BIO-32).
 * @param {Object} credentials Credenciales de acceso
 * @param {string} credentials.codigoUsuario Código de usuario (ej. admin)
 * @param {string} credentials.password Contraseña en texto plano
 * @returns {Promise<Object>} Datos del usuario autenticado y token JWT
 */
export async function login({ codigoUsuario, password }) {
  const { data } = await httpClient.post('/api/v1/auth/login', {
    codigoUsuario: codigoUsuario.trim(),
    password,
  });
  return data;
}

/**
 * Notifica al backend el cierre de sesión (HU-02 / BIO-30 / BIO-32).
 * Envía el token en cabecera Authorization: Bearer <token>.
 * @returns {Promise<Object>} Confirmación de cierre de sesión
 */
export async function logout() {
  const { data } = await httpClient.post('/api/v1/auth/logout');
  return data;
}
