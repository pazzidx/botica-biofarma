const MAX_TERMINO_LENGTH = 150;

/**
 * Normaliza el término de búsqueda eliminando espacios al inicio y al final.
 */
export function normalizarTerminoBusqueda(valor) {
  if (typeof valor !== 'string') {
    return '';
  }
  return valor.trim();
}

/**
 * Valida el término de búsqueda sin bloquear números ni caracteres de códigos.
 * Retorna un mensaje de error en español o null si es válido.
 */
export function validarTerminoBusqueda(valor) {
  const normalizado = normalizarTerminoBusqueda(valor);
  if (!normalizado) {
    return 'Ingrese el nombre o código de un medicamento para buscar.';
  }
  if (normalizado.length > MAX_TERMINO_LENGTH) {
    return `El término de búsqueda no puede superar los ${MAX_TERMINO_LENGTH} caracteres.`;
  }
  return null;
}

/**
 * Formatea el precio unitario en soles (S/) sin inventar valores cuando el dato es inválido.
 */
export function formatearPrecioSoles(precio) {
  if (precio === null || precio === undefined || precio === '') {
    return 'Precio inválido';
  }
  const numero = Number(precio);
  if (!Number.isFinite(numero) || numero <= 0) {
    return 'Precio inválido';
  }
  return `S/ ${numero.toFixed(2)}`;
}

/**
 * Evalúa el estado visual de disponibilidad de forma defensiva.
 */
export function obtenerEstadoDisponibilidad(item) {
  const stock = item?.stockActual;
  if (typeof stock !== 'number' || !Number.isInteger(stock) || stock < 0) {
    return {
      etiqueta: 'Inconsistente',
      disponible: false,
      inconsistente: true,
    };
  }
  if (stock > 0) {
    return {
      etiqueta: 'Disponible',
      disponible: true,
      inconsistente: false,
    };
  }
  return {
    etiqueta: 'Agotado',
    disponible: false,
    inconsistente: false,
  };
}
