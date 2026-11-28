import { useCallback, useEffect, useRef, useState } from 'react';
import {
  AlertCircle,
  CheckCircle2,
  FileWarning,
  PackageCheck,
  PackageX,
  Search,
  X,
} from 'lucide-react';
import { buscarDisponibilidadMedicamentos } from '../../api/medicamentosApi';
import {
  formatearPrecioSoles,
  normalizarTerminoBusqueda,
  obtenerEstadoDisponibilidad,
  validarTerminoBusqueda,
} from './consultaStockUtils';
import './ConsultaStockPage.css';

const DEBOUNCE_MS = 320;

export default function ConsultaStockPage() {
  const [termino, setTermino] = useState('');
  const [terminoConsultado, setTerminoConsultado] = useState('');
  const [resultados, setResultados] = useState([]);
  const [haBuscado, setHaBuscado] = useState(false);
  const [loading, setLoading] = useState(false);
  const [validationError, setValidationError] = useState('');
  const [requestError, setRequestError] = useState('');
  const [tiempoRespuestaMs, setTiempoRespuestaMs] = useState(null);

  const inputRef = useRef(null);
  const abortControllerRef = useRef(null);

  const ejecutarBusqueda = useCallback(async (valorBruto) => {
    const errorValidacion = validarTerminoBusqueda(valorBruto);
    if (errorValidacion) {
      setValidationError(errorValidacion);
      return;
    }

    const limpio = normalizarTerminoBusqueda(valorBruto);
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const controller = new AbortController();
    abortControllerRef.current = controller;

    setValidationError('');
    setRequestError('');
    setLoading(true);
    const inicio = performance.now();

    try {
      const data = await buscarDisponibilidadMedicamentos(limpio, controller.signal);
      setResultados(Array.isArray(data) ? data : []);
      setTerminoConsultado(limpio);
      setHaBuscado(true);
      setTiempoRespuestaMs(Math.round(performance.now() - inicio));
    } catch (err) {
      if (err?.name === 'CanceledError' || err?.code === 'ERR_CANCELED') {
        return;
      }
      setResultados([]);
      setTerminoConsultado(limpio);
      setHaBuscado(true);
      setRequestError(
        err?.message || 'No se pudo consultar la disponibilidad de medicamentos.'
      );
    } finally {
      if (abortControllerRef.current === controller) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const limpio = normalizarTerminoBusqueda(termino);
    if (!limpio || limpio.length > 150) {
      return undefined;
    }

    const timerId = window.setTimeout(() => {
      ejecutarBusqueda(limpio);
    }, DEBOUNCE_MS);

    return () => {
      window.clearTimeout(timerId);
    };
  }, [termino, ejecutarBusqueda]);

  useEffect(() => {
    const handleKeyDown = (event) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        inputRef.current?.focus();
      } else if (
        event.key === '/' &&
        document.activeElement?.tagName !== 'INPUT' &&
        document.activeElement?.tagName !== 'TEXTAREA'
      ) {
        event.preventDefault();
        inputRef.current?.focus();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
  }, []);

  const handleInputChange = (event) => {
    const nuevoValor = event.target.value;
    setTermino(nuevoValor);
    const limpio = normalizarTerminoBusqueda(nuevoValor);
    if (!limpio) {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      setLoading(false);
      setValidationError('');
      setRequestError('');
      setResultados([]);
      setHaBuscado(false);
      setTerminoConsultado('');
      setTiempoRespuestaMs(null);
      return;
    }
    if (limpio.length > 150) {
      setValidationError('El término de búsqueda no puede superar los 150 caracteres.');
      return;
    }
    setValidationError('');
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    ejecutarBusqueda(termino);
  };

  const handleLimpiar = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    setTermino('');
    setTerminoConsultado('');
    setResultados([]);
    setHaBuscado(false);
    setValidationError('');
    setRequestError('');
    setTiempoRespuestaMs(null);
    inputRef.current?.focus();
  };

  const totalDisponibles = resultados.filter(
    (item) => obtenerEstadoDisponibilidad(item).disponible
  ).length;
  const totalAgotados = resultados.filter(
    (item) =>
      !obtenerEstadoDisponibilidad(item).disponible &&
      !obtenerEstadoDisponibilidad(item).inconsistente
  ).length;

  return (
    <div className="consulta-stock-shell">
      <header className="consulta-header">
        <div className="consulta-brand">
          <div className="consulta-badge" aria-hidden="true">
            <Search size={24} />
          </div>
          <div>
            <span className="consulta-eyebrow">Botica Biofarma · Atención en Mostrador</span>
            <h1>Consulta de Disponibilidad y Stock</h1>
            <p className="consulta-subtitle">
              Busque medicamentos activos por código identificador, nombre comercial o principio activo.
            </p>
          </div>
        </div>

        {haBuscado && !requestError && (
          <div className="consulta-stats" aria-label="Resumen de resultados">
            <div className="consulta-stat-chip">
              <span>Coincidencias</span>
              <strong>{resultados.length}</strong>
            </div>
            <div className="consulta-stat-chip is-disponible">
              <span>Disponibles</span>
              <strong>{totalDisponibles}</strong>
            </div>
            <div className="consulta-stat-chip is-agotado">
              <span>Agotados</span>
              <strong>{totalAgotados}</strong>
            </div>
          </div>
        )}
      </header>

      <section className="consulta-search-card" aria-label="Buscador de medicamentos">
        <form onSubmit={handleSubmit} noValidate className="consulta-search-form">
          <label htmlFor="busqueda-medicamento" className="sr-only">
            Buscar medicamento por nombre o código
          </label>
          <div className="consulta-input-wrap">
            <Search size={18} className="consulta-input-icon" aria-hidden="true" />
            <input
              ref={inputRef}
              id="busqueda-medicamento"
              type="text"
              value={termino}
              onChange={handleInputChange}
              placeholder="Escriba código o nombre del medicamento (ej. MED-001, Paracetamol)..."
              autoFocus
              maxLength={160}
              aria-invalid={Boolean(validationError)}
              aria-describedby={validationError ? 'busqueda-error' : 'busqueda-ayuda'}
            />
            {termino && (
              <button
                type="button"
                className="consulta-clear-btn"
                onClick={handleLimpiar}
                aria-label="Limpiar búsqueda"
                title="Limpiar búsqueda"
              >
                <X size={16} />
              </button>
            )}
          </div>

          <button type="submit" className="consulta-submit-btn" disabled={loading}>
            {loading ? 'Buscando...' : 'Buscar'}
          </button>
        </form>

        <div className="consulta-meta-bar">
          {validationError ? (
            <span id="busqueda-error" className="consulta-validation-msg" role="alert">
              <AlertCircle size={15} />
              {validationError}
            </span>
          ) : (
            <span id="busqueda-ayuda" className="consulta-help-msg">
              Atajo rápido: presione <kbd>/</kbd> o <kbd>Ctrl</kbd>+<kbd>K</kbd> para enfocar el buscador.
            </span>
          )}

          {tiempoRespuestaMs !== null && haBuscado && !loading && !requestError && (
            <span className="consulta-latency">Consulta en {tiempoRespuestaMs} ms</span>
          )}
        </div>
      </section>

      {requestError && (
        <div className="consulta-alert-error" role="alert">
          <AlertCircle size={18} />
          <span>{requestError}</span>
        </div>
      )}

      {loading && (
        <div className="consulta-status-box" role="status" aria-live="polite">
          <div className="consulta-spinner" aria-hidden="true" />
          <span>Consultando stock actualizado en inventario...</span>
        </div>
      )}

      {!loading && !haBuscado && !requestError && (
        <div className="consulta-empty-card">
          <PackageCheck size={36} className="consulta-empty-icon" aria-hidden="true" />
          <h2>Listo para consultar en mostrador</h2>
          <p>
            Ingrese un código identificador o el nombre completo o parcial del medicamento para
            visualizar su precio unitario en soles, stock exacto y estado de disponibilidad.
          </p>
        </div>
      )}

      {!loading && haBuscado && !requestError && resultados.length === 0 && (
        <div className="consulta-empty-card is-not-found" role="status" aria-live="polite">
          <PackageX size={36} className="consulta-empty-icon" aria-hidden="true" />
          <h2>No se encontraron resultados</h2>
          <p>
            No existen medicamentos activos que coincidan con <strong>&ldquo;{terminoConsultado}&rdquo;</strong>.
            Verifique el código o intente con otro nombre comercial o principio activo.
          </p>
        </div>
      )}

      {!loading && !requestError && resultados.length > 0 && (
        <section className="consulta-results-card" aria-label="Resultados de disponibilidad">
          <div className="consulta-table-scroll">
            <table className="consulta-table">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Medicamento</th>
                  <th>Presentación</th>
                  <th>Precio Unitario</th>
                  <th>Stock Disponible</th>
                  <th>Disponibilidad</th>
                </tr>
              </thead>
              <tbody>
                {resultados.map((med) => {
                  const estado = obtenerEstadoDisponibilidad(med);
                  const precioTexto = formatearPrecioSoles(med.precioVenta);

                  return (
                    <tr
                      key={med.idProducto}
                      className={!estado.disponible ? 'row-agotado' : ''}
                    >
                      <td>
                        <code className="codigo-pill">{med.codigoProducto}</code>
                      </td>
                      <td>
                        <div className="med-nombre">{med.nombreComercial}</div>
                        <div className="med-subtext">
                          {med.principioActivo}
                          {med.laboratorio ? ` · ${med.laboratorio}` : ''}
                        </div>
                        {med.requiereReceta && (
                          <span className="receta-tag">
                            <FileWarning size={12} /> Receta médica retenida
                          </span>
                        )}
                      </td>
                      <td>{med.presentacion}</td>
                      <td className="precio-cell">{precioTexto}</td>
                      <td className="stock-cell">
                        {estado.inconsistente ? (
                          <span className="stock-error">Dato inconsistente</span>
                        ) : (
                          <strong>{med.stockActual} unid.</strong>
                        )}
                      </td>
                      <td>
                        <span
                          className={`estado-pill ${
                            estado.inconsistente
                              ? 'estado-inconsistente'
                              : estado.disponible
                                ? 'estado-disponible'
                                : 'estado-agotado'
                          }`}
                        >
                          {estado.disponible ? (
                            <CheckCircle2 size={14} aria-hidden="true" />
                          ) : (
                            <AlertCircle size={14} aria-hidden="true" />
                          )}
                          <span>{estado.etiqueta}</span>
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}
