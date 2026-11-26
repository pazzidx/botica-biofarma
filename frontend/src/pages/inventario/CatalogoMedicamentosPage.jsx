import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  Ban,
  CheckCircle2,
  Edit3,
  FileWarning,
  PackagePlus,
  Pill,
  RefreshCw,
  Search,
} from 'lucide-react';
import {
  actualizarMedicamento,
  desactivarMedicamento,
  listarMedicamentos,
  registrarMedicamento,
} from '../../api/medicamentosApi';
import './CatalogoMedicamentosPage.css';

const INITIAL_FORM = {
  codigoProducto: '',
  nombreComercial: '',
  principioActivo: '',
  presentacion: '',
  laboratorio: '',
  precioVenta: '',
  stockMinimo: '5',
  requiereReceta: false,
};

const CODIGO_REGEX = /^[A-Za-z0-9._-]+$/;

function validarFormulario(form, isEditing) {
  const errors = {};

  if (!isEditing) {
    const codigo = form.codigoProducto.trim();
    if (!codigo) {
      errors.codigoProducto = 'El código del medicamento es obligatorio.';
    } else if (codigo.length < 2 || codigo.length > 30) {
      errors.codigoProducto = 'Debe tener entre 2 y 30 caracteres.';
    } else if (!CODIGO_REGEX.test(codigo)) {
      errors.codigoProducto = 'Solo letras, números, punto, guion y guion bajo.';
    }
  }

  if (!form.nombreComercial.trim()) {
    errors.nombreComercial = 'El nombre comercial es obligatorio.';
  } else if (form.nombreComercial.trim().length > 150) {
    errors.nombreComercial = 'Máximo 150 caracteres.';
  }

  if (!form.principioActivo.trim()) {
    errors.principioActivo = 'El principio activo es obligatorio.';
  } else if (form.principioActivo.trim().length > 150) {
    errors.principioActivo = 'Máximo 150 caracteres.';
  }

  if (!form.presentacion.trim()) {
    errors.presentacion = 'La presentación es obligatoria.';
  } else if (form.presentacion.trim().length > 80) {
    errors.presentacion = 'Máximo 80 caracteres.';
  }

  if (form.laboratorio.trim().length > 100) {
    errors.laboratorio = 'Máximo 100 caracteres.';
  }

  const precio = Number(form.precioVenta);
  if (form.precioVenta === '' || Number.isNaN(precio) || precio <= 0) {
    errors.precioVenta = 'El precio unitario debe ser mayor a 0.';
  }

  const stockMin = Number(form.stockMinimo);
  if (form.stockMinimo === '' || !Number.isInteger(stockMin) || stockMin < 0) {
    errors.stockMinimo = 'El stock mínimo debe ser un entero mayor o igual a 0.';
  }

  return errors;
}

export default function CatalogoMedicamentosPage() {
  const [medicamentos, setMedicamentos] = useState([]);
  const [loadingList, setLoadingList] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [soloActivos, setSoloActivos] = useState(false);

  const cargarCatalogo = useCallback(async () => {
    setLoadingList(true);
    try {
      const data = await listarMedicamentos(soloActivos ? true : undefined);
      setMedicamentos(data);
    } catch (error) {
      setFeedback({ type: 'error', text: error.message });
    } finally {
      setLoadingList(false);
    }
  }, [soloActivos]);

  useEffect(() => {
    let active = true;
    listarMedicamentos(soloActivos ? true : undefined)
      .then((data) => {
        if (active) setMedicamentos(data);
      })
      .catch((error) => {
        if (active) setFeedback({ type: 'error', text: error.message });
      })
      .finally(() => {
        if (active) setLoadingList(false);
      });
    return () => {
      active = false;
    };
  }, [soloActivos]);

  const filtrados = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();
    if (!query) return medicamentos;
    return medicamentos.filter(
      (item) =>
        item.codigoProducto.toLowerCase().includes(query) ||
        item.nombreComercial.toLowerCase().includes(query) ||
        item.principioActivo.toLowerCase().includes(query)
    );
  }, [medicamentos, searchTerm]);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const resetForm = () => {
    setEditingId(null);
    setForm(INITIAL_FORM);
    setFieldErrors({});
  };

  const handleEdit = (med) => {
    setEditingId(med.idProducto);
    setForm({
      codigoProducto: med.codigoProducto,
      nombreComercial: med.nombreComercial,
      principioActivo: med.principioActivo,
      presentacion: med.presentacion,
      laboratorio: med.laboratorio || '',
      precioVenta: String(med.precioVenta),
      stockMinimo: String(med.stockMinimo),
      requiereReceta: Boolean(med.requiereReceta),
    });
    setFieldErrors({});
    setFeedback(null);
  };

  const handleDesactivar = async (med) => {
    if (!window.confirm(`¿Desea desactivar el medicamento "${med.nombreComercial}" (${med.codigoProducto})?`)) {
      return;
    }
    setFeedback(null);
    try {
      const actualizado = await desactivarMedicamento(med.idProducto);
      setMedicamentos((prev) =>
        prev.map((item) => (item.idProducto === actualizado.idProducto ? actualizado : item))
      );
      setFeedback({
        type: 'success',
        text: `El medicamento "${actualizado.nombreComercial}" fue desactivado correctamente.`,
      });
    } catch (error) {
      setFeedback({ type: 'error', text: error.message });
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFeedback(null);

    const isEditing = editingId !== null;
    const errors = validarFormulario(form, isEditing);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setSubmitting(true);
    try {
      const basePayload = {
        nombreComercial: form.nombreComercial.trim(),
        principioActivo: form.principioActivo.trim(),
        presentacion: form.presentacion.trim(),
        laboratorio: form.laboratorio.trim() || null,
        precioVenta: Number(form.precioVenta),
        stockMinimo: Number(form.stockMinimo),
        requiereReceta: Boolean(form.requiereReceta),
      };

      if (isEditing) {
        const actualizado = await actualizarMedicamento(editingId, basePayload);
        setMedicamentos((prev) =>
          prev.map((item) => (item.idProducto === actualizado.idProducto ? actualizado : item))
        );
        setFeedback({
          type: 'success',
          text: `Medicamento "${actualizado.nombreComercial}" actualizado correctamente.`,
        });
      } else {
        const creado = await registrarMedicamento({
          ...basePayload,
          codigoProducto: form.codigoProducto.trim(),
        });
        setMedicamentos((prev) => [creado, ...prev]);
        setFeedback({
          type: 'success',
          text: `Medicamento "${creado.nombreComercial}" registrado en el catálogo.`,
        });
      }
      resetForm();
    } catch (error) {
      setFieldErrors(error.fieldErrors || {});
      setFeedback({ type: 'error', text: error.message });
    } finally {
      setSubmitting(false);
    }
  };

  const totalActivos = medicamentos.filter((m) => m.activo).length;

  return (
    <div className="catalogo-shell">
      <header className="catalogo-header">
        <div className="brand-block">
          <div className="brand-badge">
            <Pill size={24} />
          </div>
          <div>
            <span className="brand-eyebrow">Botica Biofarma · HU-04</span>
            <h1>Catálogo de Medicamentos</h1>
          </div>
        </div>
        <div className="header-stats">
          <div className="stat-chip">
            <span>Total registrados</span>
            <strong>{medicamentos.length}</strong>
          </div>
          <div className="stat-chip stat-chip--active">
            <span>Activos</span>
            <strong>{totalActivos}</strong>
          </div>
        </div>
      </header>

      {feedback && (
        <div className={`alert-banner alert-banner--${feedback.type}`} role="alert">
          {feedback.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{feedback.text}</span>
        </div>
      )}

      <div className="catalogo-grid">
        <section className="panel-card">
          <div className="panel-header">
            <PackagePlus size={20} />
            <h2>{editingId ? 'Editar Medicamento' : 'Nuevo Medicamento'}</h2>
          </div>

          <form onSubmit={handleSubmit} noValidate className="catalogo-form">
            <div className="form-group">
              <label htmlFor="codigoProducto">Código único *</label>
              <input
                id="codigoProducto"
                name="codigoProducto"
                value={form.codigoProducto}
                onChange={handleChange}
                disabled={editingId !== null}
                placeholder="Ej. MED-001"
              />
              {fieldErrors.codigoProducto && (
                <span className="field-error">{fieldErrors.codigoProducto}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="nombreComercial">Nombre comercial *</label>
              <input
                id="nombreComercial"
                name="nombreComercial"
                value={form.nombreComercial}
                onChange={handleChange}
                placeholder="Ej. Paracetamol 500mg"
              />
              {fieldErrors.nombreComercial && (
                <span className="field-error">{fieldErrors.nombreComercial}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="principioActivo">Principio activo / genérico *</label>
              <input
                id="principioActivo"
                name="principioActivo"
                value={form.principioActivo}
                onChange={handleChange}
                placeholder="Ej. Paracetamol"
              />
              {fieldErrors.principioActivo && (
                <span className="field-error">{fieldErrors.principioActivo}</span>
              )}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="presentacion">Presentación *</label>
                <input
                  id="presentacion"
                  name="presentacion"
                  value={form.presentacion}
                  onChange={handleChange}
                  placeholder="Ej. Caja x 100 tab."
                />
                {fieldErrors.presentacion && (
                  <span className="field-error">{fieldErrors.presentacion}</span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="laboratorio">Laboratorio</label>
                <input
                  id="laboratorio"
                  name="laboratorio"
                  value={form.laboratorio}
                  onChange={handleChange}
                  placeholder="Ej. Genfar"
                />
                {fieldErrors.laboratorio && (
                  <span className="field-error">{fieldErrors.laboratorio}</span>
                )}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="precioVenta">Precio unitario (S/) *</label>
                <input
                  id="precioVenta"
                  name="precioVenta"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={form.precioVenta}
                  onChange={handleChange}
                  placeholder="0.00"
                />
                {fieldErrors.precioVenta && (
                  <span className="field-error">{fieldErrors.precioVenta}</span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="stockMinimo">Stock mínimo *</label>
                <input
                  id="stockMinimo"
                  name="stockMinimo"
                  type="number"
                  min="0"
                  step="1"
                  value={form.stockMinimo}
                  onChange={handleChange}
                />
                {fieldErrors.stockMinimo && (
                  <span className="field-error">{fieldErrors.stockMinimo}</span>
                )}
              </div>
            </div>

            <label className="checkbox-row" htmlFor="requiereReceta">
              <input
                id="requiereReceta"
                name="requiereReceta"
                type="checkbox"
                checked={form.requiereReceta}
                onChange={handleChange}
              />
              <span>Requiere receta médica retenida</span>
            </label>

            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={submitting}>
                {submitting
                  ? 'Guardando...'
                  : editingId
                    ? 'Guardar Cambios'
                    : 'Registrar Medicamento'}
              </button>
              {editingId && (
                <button type="button" className="btn-secondary" onClick={resetForm}>
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </section>

        <section className="panel-card">
          <div className="list-toolbar">
            <div className="search-box">
              <Search size={16} />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Buscar por código, nombre o principio activo..."
              />
            </div>

            <label className="filter-toggle">
              <input
                type="checkbox"
                checked={soloActivos}
                onChange={(e) => setSoloActivos(e.target.checked)}
              />
              <span>Solo activos</span>
            </label>

            <button
              type="button"
              className="btn-icon"
              onClick={cargarCatalogo}
              title="Actualizar lista"
            >
              <RefreshCw size={16} />
            </button>
          </div>

          {loadingList ? (
            <p className="empty-state">Cargando catálogo de medicamentos...</p>
          ) : filtrados.length === 0 ? (
            <p className="empty-state">No hay medicamentos que coincidan con la búsqueda.</p>
          ) : (
            <div className="table-scroll">
              <table className="catalogo-table">
                <thead>
                  <tr>
                    <th>Código</th>
                    <th>Medicamento</th>
                    <th>Presentación</th>
                    <th>Precio (S/)</th>
                    <th>Stock Mín.</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filtrados.map((med) => (
                    <tr key={med.idProducto} className={!med.activo ? 'row-inactive' : ''}>
                      <td>
                        <code>{med.codigoProducto}</code>
                      </td>
                      <td>
                        <strong>{med.nombreComercial}</strong>
                        <div className="subtext">
                          {med.principioActivo}
                          {med.laboratorio ? ` · ${med.laboratorio}` : ''}
                        </div>
                        {med.requiereReceta && (
                          <span className="badge badge--warn">
                            <FileWarning size={12} /> Receta retenida
                          </span>
                        )}
                      </td>
                      <td>{med.presentacion}</td>
                      <td>S/ {Number(med.precioVenta).toFixed(2)}</td>
                      <td>{med.stockMinimo}</td>
                      <td>
                        <span className={`badge ${med.activo ? 'badge--ok' : 'badge--off'}`}>
                          {med.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="btn-action"
                            onClick={() => handleEdit(med)}
                            title="Editar medicamento"
                          >
                            <Edit3 size={15} />
                          </button>
                          {med.activo && (
                            <button
                              type="button"
                              className="btn-action btn-action--danger"
                              onClick={() => handleDesactivar(med)}
                              title="Desactivar medicamento"
                            >
                              <Ban size={15} />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
