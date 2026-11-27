import httpClient from './httpClient';

/**
 * Autentica un usuario contra el backend y retorna el token JWT y perfil.
 *
 * @param {{ codigoUsuario: string, password: string }} credentials
 */
export async function login({ codigoUsuario, password }) {
  const { data } = await httpClient.post('/api/v1/auth/login', {
    codigoUsuario,
    password,
  });
  return data;
}
