import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  Eye,
  Lock,
  Receipt,
  RefreshCw,
  Search,
  ShieldCheck,
  X,
} from 'lucide-react';
import { listarHistorialVentas, obtenerDetalleVenta } from '../../api/ventasApi';
import { useAuth } from '../../context/useAuth';
import './HistorialVentasPage.css';

function formatearFecha(fechaIso) {
  if (!fechaIso) return '—';
  const fecha = new Date(fechaIso);
  if (Number.isNaN(fecha.getTime())) return '—';
  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(fecha);
}

function formatearMoneda(monto) {
  const valor = Number(monto ?? 0);
  return `S/ ${valor.toFixed(2)}`;
}

export default function HistorialVentasPage() {
  const { rol, usuario } = useAuth();
  const esAdministradora = rol === 'ADMINISTRADORA';

  const [ventas, setVentas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busqueda, setBusqueda] = useState('');

  const [detalleSeleccionado, setDetalleSeleccionado] = useState(null);
  const [loadingDetalle, setLoadingDetalle] = useState(false);
  const [errorDetalle, setErrorDetalle] = useState('');

  const cargarHistorial = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await listarHistorialVentas();
      setVentas(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'No se pudo cargar el historial de ventas.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    listarHistorialVentas()
      .then((data) => {
        if (active) {
          setVentas(Array.isArray(data) ? data : []);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(err.message || 'No se pudo cargar el historial de ventas.');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const handleVerDetalle = async (idVenta) => {
    setLoadingDetalle(true);
    setErrorDetalle('');
    setDetalleSeleccionado(null);
    try {
      const detalle = await obtenerDetalleVenta(idVenta);
      setDetalleSeleccionado(detalle);
    } catch (err) {
      setErrorDetalle(err.message || 'No se pudo obtener el detalle de la venta.');
    } finally {
      setLoadingDetalle(false);
    }
  };

  const cerrarModal = () => {
    setDetalleSeleccionado(null);
    setErrorDetalle('');
  };

  const ventasFiltradas = useMemo(() => {
    const termino = busqueda.trim().toLowerCase();
    if (!termino) return ventas;
    return ventas.filter((venta) => {
      const coincideTicket = venta.numeroTicket?.toLowerCase().includes(termino);
      const coincideEstado = venta.estado?.toLowerCase().includes(termino);
      const coincideTrabajadora =
        esAdministradora &&
        (venta.nombreUsuario?.toLowerCase().includes(termino) ||
          venta.codigoUsuario?.toLowerCase().includes(termino));
      return coincideTicket || coincideEstado || coincideTrabajadora;
    });
  }, [ventas, busqueda, esAdministradora]);

  const totalAcumulado = useMemo(
    () => ventasFiltradas.reduce((acc, item) => acc + Number(item.total ?? 0), 0),
    [ventasFiltradas]
  );

  return (
    <main className="ventas-shell">
      <header className="ventas-header">
        <div className="ventas-brand-block">
          <div className="ventas-brand-badge" aria-hidden="true">
            <Receipt size={24} />
          </div>
          <div>
            <span className="ventas-eyebrow">
              {esAdministradora ? 'Supervisión Administrativa' : 'Privacidad entre Turnos'}
            </span>
            <h1>
              {esAdministradora ? 'Historial Global de Ventas' : 'Mi Historial de Ventas'}
            </h1>
            <p className="ventas-subtitle">
              {esAdministradora
                ? 'Visualizando las operaciones registradas por todo el personal de la botica.'
                : `Mostrando únicamente las ventas registradas con tu cuenta (${usuario?.codigoUsuario || 'actual'}).`}
            </p>
          </div>
        </div>

        <div className="ventas-stats">
          <div className="ventas-stat-chip">
            <span className="ventas-stat-label">Operaciones</span>
            <strong className="ventas-stat-value">{ventasFiltradas.length}</strong>
          </div>
          <div className="ventas-stat-chip">
            <span className="ventas-stat-label">Total mostrado</span>
            <strong className="ventas-stat-value">{formatearMoneda(totalAcumulado)}</strong>
          </div>
        </div>
      </header>

      <section
        className={`ventas-scope-banner ${esAdministradora ? 'scope-admin' : 'scope-worker'}`}
        role="status"
      >
        {esAdministradora ? (
          <>
            <ShieldCheck size={18} />
            <span>
              <strong>Acceso completo (ADMINISTRADORA):</strong> Puedes auditar ventas y detalles de
              todas las trabajadoras.
            </span>
          </>
        ) : (
          <>
            <Lock size={18} />
            <span>
              <strong>Vista restringida (TRABAJADORA):</strong> Por seguridad operativa, solo tienes
              acceso a tus propias ventas.
            </span>
          </>
        )}
      </section>

      {error && (
        <div className="ventas-alert ventas-alert-error" role="alert">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {errorDetalle && (
        <div className="ventas-alert ventas-alert-error" role="alert">
          <AlertCircle size={18} />
          <span>{errorDetalle}</span>
        </div>
      )}

      <section className="ventas-card">
        <div className="ventas-toolbar">
          <div className="ventas-search-box">
            <Search size={16} />
            <input
              type="search"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              placeholder={
                esAdministradora
                  ? 'Buscar por ticket, estado o trabajadora...'
                  : 'Buscar por número de ticket o estado...'
              }
              aria-label="Buscar en historial de ventas"
            />
          </div>
          <button
            type="button"
            className="ventas-btn-refresh"
            onClick={cargarHistorial}
            disabled={loading}
          >
            <RefreshCw size={15} className={loading ? 'spin' : ''} />
            <span>Actualizar</span>
          </button>
        </div>

        {loading ? (
          <div className="ventas-empty-state">Cargando historial de ventas...</div>
        ) : ventasFiltradas.length === 0 ? (
          <div className="ventas-empty-state">
            No se encontraron ventas registradas para mostrar.
          </div>
        ) : (
          <div className="ventas-table-wrap">
            <table className="ventas-table">
              <thead>
                <tr>
                  <th>Ticket</th>
                  <th>Fecha y hora</th>
                  {esAdministradora && <th>Trabajadora</th>}
                  <th>Ítems</th>
                  <th>Total</th>
                  <th>Estado</th>
                  <th className="col-actions">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {ventasFiltradas.map((venta) => (
                  <tr key={venta.idVenta}>
                    <td>
                      <code className="ticket-code">{venta.numeroTicket}</code>
                    </td>
                    <td>{formatearFecha(venta.fechaVenta)}</td>
                    {esAdministradora && (
                      <td>
                        <div className="worker-cell">
                          <strong>{venta.nombreUsuario}</strong>
                          <span>{venta.codigoUsuario}</span>
                        </div>
                      </td>
                    )}
                    <td>{venta.cantidadItems}</td>
                    <td className="amount-cell">{formatearMoneda(venta.total)}</td>
                    <td>
                      <span className="estado-badge">{venta.estado}</span>
                    </td>
                    <td className="col-actions">
                      <button
                        type="button"
                        className="ventas-btn-detail"
                        onClick={() => handleVerDetalle(venta.idVenta)}
                      >
                        <Eye size={15} />
                        <span>Ver detalle</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {(loadingDetalle || detalleSeleccionado) && (
        <div className="ventas-modal-backdrop" role="dialog" aria-modal="true">
          <div className="ventas-modal">
            <div className="ventas-modal-header">
              <div>
                <span className="ventas-eyebrow">Comprobante Interno</span>
                <h2>
                  {loadingDetalle
                    ? 'Cargando detalle...'
                    : `Ticket ${detalleSeleccionado.numeroTicket}`}
                </h2>
              </div>
              <button
                type="button"
                className="ventas-modal-close"
                onClick={cerrarModal}
                aria-label="Cerrar detalle"
              >
                <X size={18} />
              </button>
            </div>

            {loadingDetalle ? (
              <div className="ventas-empty-state">Consultando información de la venta...</div>
            ) : (
              detalleSeleccionado && (
                <div className="ventas-modal-body">
                  <div className="ventas-meta-grid">
                    <div>
                      <span className="meta-label">Fecha de venta</span>
                      <strong>{formatearFecha(detalleSeleccionado.fechaVenta)}</strong>
                    </div>
                    <div>
                      <span className="meta-label">Responsable</span>
                      <strong>
                        {detalleSeleccionado.nombreUsuario} ({detalleSeleccionado.codigoUsuario})
                      </strong>
                    </div>
                    <div>
                      <span className="meta-label">Estado</span>
                      <strong>{detalleSeleccionado.estado}</strong>
                    </div>
                    <div>
                      <span className="meta-label">Total</span>
                      <strong className="amount-cell">
                        {formatearMoneda(detalleSeleccionado.total)}
                      </strong>
                    </div>
                  </div>

                  <div className="ventas-table-wrap">
                    <table className="ventas-table">
                      <thead>
                        <tr>
                          <th>Producto</th>
                          <th>Lote</th>
                          <th>Cant.</th>
                          <th>P. Unit.</th>
                          <th>Subtotal</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(detalleSeleccionado.detalles || []).map((item) => (
                          <tr key={item.idDetalle}>
                            <td>{item.nombreProducto}</td>
                            <td>
                              <code className="ticket-code">{item.numeroLote}</code>
                            </td>
                            <td>{item.cantidad}</td>
                            <td>{formatearMoneda(item.precioUnitario)}</td>
                            <td className="amount-cell">{formatearMoneda(item.subtotal)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )
            )}
          </div>
        </div>
      )}
    </main>
  );
}
