import assert from 'node:assert/strict';
import test from 'node:test';
import {
  formatearPrecioSoles,
  normalizarTerminoBusqueda,
  obtenerEstadoDisponibilidad,
  validarTerminoBusqueda,
} from './consultaStockUtils.js';

test('normaliza espacios y valida terminos con numeros y caracteres especiales', () => {
  assert.equal(normalizarTerminoBusqueda('  7501001  '), '7501001');
  assert.equal(validarTerminoBusqueda('7501001'), null);
  assert.equal(validarTerminoBusqueda('Amoxicilina 500mg'), null);
  assert.equal(validarTerminoBusqueda('MED-001_A'), null);
});

test('rechaza campo vacio, solo espacios y terminos demasiado extensos', () => {
  assert.ok(validarTerminoBusqueda(''));
  assert.ok(validarTerminoBusqueda('     '));
  assert.ok(validarTerminoBusqueda(null));
  assert.ok(validarTerminoBusqueda('a'.repeat(151)));
});

test('formatea precio unitario en soles sin inventar precios invalidos', () => {
  assert.equal(formatearPrecioSoles(12.5), 'S/ 12.50');
  assert.equal(formatearPrecioSoles('16'), 'S/ 16.00');
  assert.equal(formatearPrecioSoles(null), 'Precio inválido');
  assert.equal(formatearPrecioSoles(0), 'Precio inválido');
  assert.equal(formatearPrecioSoles(-5), 'Precio inválido');
});

test('determina correctamente estados Disponible, Agotado e inconsistente', () => {
  assert.deepEqual(obtenerEstadoDisponibilidad({ stockActual: 15 }), {
    etiqueta: 'Disponible',
    disponible: true,
    inconsistente: false,
  });
  assert.deepEqual(obtenerEstadoDisponibilidad({ stockActual: 0 }), {
    etiqueta: 'Agotado',
    disponible: false,
    inconsistente: false,
  });
  assert.deepEqual(obtenerEstadoDisponibilidad({ stockActual: -2 }), {
    etiqueta: 'Inconsistente',
    disponible: false,
    inconsistente: true,
  });
});
