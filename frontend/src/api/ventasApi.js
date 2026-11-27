import httpClient from './httpClient';

/**
 * Consulta el historial de ventas.
 * El servidor determina identidad y rol directamente desde el token JWT.
 */
export async function listarHistorialVentas() {
  const response = await httpClient.get('/api/v1/ventas/historial');
  return response.data;
}

/**
 * Consulta el detalle completo de una venta por su identificador.
 */
export async function obtenerDetalleVenta(idVenta) {
  const response = await httpClient.get(`/api/v1/ventas/${idVenta}`);
  return response.data;
}
