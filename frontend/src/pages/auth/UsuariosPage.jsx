import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  CheckCircle2,
  Eye,
  EyeOff,
  Pencil,
  Power,
  RefreshCw,
  Search,
  ShieldCheck,
  Trash2,
  UserPlus,
  Users,
  X,
} from 'lucide-react';
import {
  activarUsuario,
  actualizarUsuario,
  eliminarUsuario,
  listarUsuarios,
  registrarUsuario,
} from '../../api/usuariosApi';
import { useAuth } from '../../context/useAuth';
import './UsuariosPage.css';

const INITIAL_FORM = {
  codigoUsuario: '',
  nombres: '',
  apellidos: '',
  password: '',
};

const CODIGO_REGEX = /^[A-Za-z0-9._-]+$/;
const SOLO_LETRAS_REGEX = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;

function validarCampoNombre(valor, etiqueta) {
  const limpio = valor.trim();
  if (!limpio) {
    return `${etiqueta} son obligatorios.`;
  }
  if (limpio.length > 100) {
    return 'Máximo 100 caracteres.';
  }
  if (!SOLO_LETRAS_REGEX.test(limpio)) {
    return `${etiqueta} solo pueden contener letras, tildes, ñ y espacios (sin números ni símbolos).`;
  }
  return '';
}

function validarFormularioRegistro(form) {
  const errors = {};
  const codigo = form.codigoUsuario.trim();

  if (!codigo) {
    errors.codigoUsuario = 'El código de usuario es obligatorio.';
  } else if (codigo.length < 3 || codigo.length > 30) {
    errors.codigoUsuario = 'Debe tener entre 3 y 30 caracteres.';
  } else if (!CODIGO_REGEX.test(codigo)) {
    errors.codigoUsuario = 'Solo letras, números, punto, guion y guion bajo.';
  }

  const errorNombres = validarCampoNombre(form.nombres, 'Los nombres');
  if (errorNombres) errors.nombres = errorNombres;

  const errorApellidos = validarCampoNombre(form.apellidos, 'Los apellidos');
  if (errorApellidos) errors.apellidos = errorApellidos;

  if (!form.password) {
    errors.password = 'La contraseña es obligatoria.';
  } else if (form.password.length < 6 || form.password.length > 100) {
    errors.password = 'Debe tener entre 6 y 100 caracteres.';
  }

  return errors;
}

function validarFormularioEdicion(form) {
  const errors = {};
  const errorNombres = validarCampoNombre(form.nombres, 'Los nombres');
  if (errorNombres) errors.nombres = errorNombres;

  const errorApellidos = validarCampoNombre(form.apellidos, 'Los apellidos');
  if (errorApellidos) errors.apellidos = errorApellidos;

  return errors;
}

function formatearFecha(fechaIso) {
  if (!fechaIso) return '—';
  const fecha = new Date(fechaIso);
  if (Number.isNaN(fecha.getTime())) return '—';
  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(fecha);
}

export default function UsuariosPage() {
  const { usuario: usuarioSesion } = useAuth();

  const [form, setForm] = useState(INITIAL_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [usuarios, setUsuarios] = useState([]);
  const [loadingList, setLoadingList] = useState(true);
  const [busqueda, setBusqueda] = useState('');

  const [editingUser, setEditingUser] = useState(null);
  const [editForm, setEditForm] = useState({ nombres: '', apellidos: '' });
  const [editErrors, setEditErrors] = useState({});
  const [savingEdit, setSavingEdit] = useState(false);

  const [confirmDeleteUser, setConfirmDeleteUser] = useState(null);
  const [processingActionId, setProcessingActionId] = useState(null);

  const [feedback, setFeedback] = useState(null);

  const fetchUsuarios = useCallback(async () => {
    const data = await listarUsuarios();
    return Array.isArray(data) ? data : [];
  }, []);

  const cargarUsuarios = useCallback(async () => {
    setLoadingList(true);
    try {
      const lista = await fetchUsuarios();
      setUsuarios(lista);
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'No se pudo cargar la lista de usuarios.',
      });
    } finally {
      setLoadingList(false);
    }
  }, [fetchUsuarios]);

  useEffect(() => {
    let active = true;
    fetchUsuarios()
      .then((lista) => {
        if (active) setUsuarios(lista);
      })
      .catch((error) => {
        if (active) {
          setFeedback({
            type: 'error',
            message: error.message || 'No se pudo cargar la lista de usuarios.',
          });
        }
      })
      .finally(() => {
        if (active) setLoadingList(false);
      });
    return () => {
      active = false;
    };
  }, [fetchUsuarios]);

  const esCuentaProtegida = useCallback(
    (user) => {
      const codigoLower = user.codigoUsuario?.toLowerCase();
      const sesionLower = usuarioSesion?.codigoUsuario?.toLowerCase();
      return (
        codigoLower === 'admin' ||
        user.rol === 'ADMINISTRADORA' ||
        Boolean(sesionLower && codigoLower === sesionLower)
      );
    },
    [usuarioSesion]
  );

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));

    if (name === 'nombres' || name === 'apellidos') {
      const etiqueta = name === 'nombres' ? 'Los nombres' : 'Los apellidos';
      if (value.trim() && !SOLO_LETRAS_REGEX.test(value.trim())) {
        setFieldErrors((prev) => ({
          ...prev,
          [name]: `${etiqueta} solo pueden contener letras, tildes, ñ y espacios (sin números ni símbolos).`,
        }));
        return;
      }
    }

    setFieldErrors((prev) => {
      if (!prev[name]) return prev;
      const next = { ...prev };
      delete next[name];
      return next;
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFeedback(null);

    const validationErrors = validarFormularioRegistro(form);
    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors);
      return;
    }

    setSubmitting(true);
    try {
      const creado = await registrarUsuario({
        codigoUsuario: form.codigoUsuario.trim(),
        nombres: form.nombres.trim(),
        apellidos: form.apellidos.trim(),
        password: form.password,
        rol: 'TRABAJADORA',
      });

      setForm(INITIAL_FORM);
      setFieldErrors({});
      setShowPassword(false);
      setUsuarios((prev) => [creado, ...prev]);
      setFeedback({
        type: 'success',
        message: `Trabajadora "${creado.codigoUsuario}" registrada correctamente.`,
      });
    } catch (error) {
      if (error.fieldErrors && Object.keys(error.fieldErrors).length > 0) {
        setFieldErrors(error.fieldErrors);
      }
      setFeedback({
        type: 'error',
        message: error.message || 'No se pudo registrar el usuario.',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const abrirModalEdicion = (user) => {
    setEditingUser(user);
    setEditForm({ nombres: user.nombres, apellidos: user.apellidos });
    setEditErrors({});
  };

  const cerrarModalEdicion = () => {
    if (savingEdit) return;
    setEditingUser(null);
    setEditErrors({});
  };

  const handleEditChange = (event) => {
    const { name, value } = event.target;
    setEditForm((prev) => ({ ...prev, [name]: value }));
    const etiqueta = name === 'nombres' ? 'Los nombres' : 'Los apellidos';
    const errorMsg = validarCampoNombre(value, etiqueta);
    setEditErrors((prev) => {
      const next = { ...prev };
      if (errorMsg) {
        next[name] = errorMsg;
      } else {
        delete next[name];
      }
      return next;
    });
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();
    if (!editingUser) return;

    const validationErrors = validarFormularioEdicion(editForm);
    if (Object.keys(validationErrors).length > 0) {
      setEditErrors(validationErrors);
      return;
    }

    setSavingEdit(true);
    setFeedback(null);
    try {
      const actualizado = await actualizarUsuario(editingUser.idUsuario, {
        nombres: editForm.nombres.trim(),
        apellidos: editForm.apellidos.trim(),
      });
      setUsuarios((prev) =>
        prev.map((u) => (u.idUsuario === actualizado.idUsuario ? actualizado : u))
      );
      setEditingUser(null);
      setFeedback({
        type: 'success',
        message: `Datos personales de "${actualizado.codigoUsuario}" actualizados.`,
      });
    } catch (error) {
      if (error.fieldErrors && Object.keys(error.fieldErrors).length > 0) {
        setEditErrors(error.fieldErrors);
      }
      setFeedback({
        type: 'error',
        message: error.message || 'No se pudo actualizar el usuario.',
      });
    } finally {
      setSavingEdit(false);
    }
  };

  const confirmarEliminacion = async () => {
    if (!confirmDeleteUser) return;
    const target = confirmDeleteUser;

    setProcessingActionId(target.idUsuario);
    setFeedback(null);
    try {
      const resultado = await eliminarUsuario(target.idUsuario);
      if (resultado.eliminadoFisicamente) {
        setUsuarios((prev) => prev.filter((u) => u.idUsuario !== target.idUsuario));
      } else if (resultado.usuario) {
        setUsuarios((prev) =>
          prev.map((u) => (u.idUsuario === resultado.usuario.idUsuario ? resultado.usuario : u))
        );
      }
      setConfirmDeleteUser(null);
      setFeedback({
        type: 'success',
        message: resultado.mensaje || `Operación realizada sobre "${target.codigoUsuario}".`,
      });
    } catch (error) {
      setConfirmDeleteUser(null);
      setFeedback({
        type: 'error',
        message: error.message || 'No se pudo eliminar o desactivar el usuario.',
      });
    } finally {
      setProcessingActionId(null);
    }
  };

  const handleActivar = async (user) => {
    setProcessingActionId(user.idUsuario);
    setFeedback(null);
    try {
      const reactivado = await activarUsuario(user.idUsuario);
      setUsuarios((prev) =>
        prev.map((u) => (u.idUsuario === reactivado.idUsuario ? reactivado : u))
      );
      setFeedback({
        type: 'success',
        message: `Cuenta "${reactivado.codigoUsuario}" reactivada correctamente.`,
      });
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'No se pudo reactivar el usuario.',
      });
    } finally {
      setProcessingActionId(null);
    }
  };

  const usuariosFiltrados = useMemo(() => {
    const q = busqueda.trim().toLowerCase();
    if (!q) return usuarios;
    return usuarios.filter((u) => {
      const fullName = `${u.nombres} ${u.apellidos}`.toLowerCase();
      return (
        u.codigoUsuario.toLowerCase().includes(q) ||
        fullName.includes(q) ||
        u.rol.toLowerCase().includes(q)
      );
    });
  }, [usuarios, busqueda]);

  const resumenRoles = useMemo(() => {
    return usuarios.reduce(
      (acc, u) => {
        if (u.rol === 'ADMINISTRADORA') acc.administradoras += 1;
        if (u.rol === 'TRABAJADORA') acc.trabajadoras += 1;
        return acc;
      },
      { administradoras: 0, trabajadoras: 0 }
    );
  }, [usuarios]);

  return (
    <div className="usuarios-shell">
      <header className="usuarios-header">
        <div className="brand-block">
          <div className="brand-badge">
            <ShieldCheck size={22} aria-hidden="true" />
          </div>
          <div>
            <span className="brand-eyebrow">Botica Biofarma · Épica 01 (HU-01)</span>
            <h1>Registro y Gestión de Usuarios</h1>
          </div>
        </div>

        <div className="header-stats" aria-label="Resumen de cuentas registradas">
          <div className="stat-chip">
            <span className="stat-label">Total cuentas</span>
            <strong className="stat-value">{usuarios.length}</strong>
          </div>
          <div className="stat-chip">
            <span className="stat-label">Administradoras</span>
            <strong className="stat-value">{resumenRoles.administradoras}</strong>
          </div>
          <div className="stat-chip">
            <span className="stat-label">Trabajadoras</span>
            <strong className="stat-value">{resumenRoles.trabajadoras}</strong>
          </div>
        </div>
      </header>

      {feedback && (
        <div
          className={`feedback-banner ${feedback.type === 'success' ? 'is-success' : 'is-error'}`}
          role={feedback.type === 'error' ? 'alert' : 'status'}
        >
          {feedback.type === 'success' ? (
            <CheckCircle2 size={20} aria-hidden="true" />
          ) : (
            <AlertCircle size={20} aria-hidden="true" />
          )}
          <span>{feedback.message}</span>
        </div>
      )}

      <main className="usuarios-grid">
        <section className="panel-card" aria-labelledby="titulo-registro">
          <div className="panel-heading">
            <UserPlus size={20} aria-hidden="true" />
            <div>
              <h2 id="titulo-registro">Nueva Cuenta de Trabajadora</h2>
              <p>Complete los datos para habilitar un acceso de mostrador con contraseña cifrada.</p>
            </div>
          </div>

          <form className="usuario-form" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="codigoUsuario">Código de usuario *</label>
              <input
                id="codigoUsuario"
                name="codigoUsuario"
                type="text"
                maxLength={30}
                autoComplete="off"
                placeholder="Ej. trab.lucia"
                value={form.codigoUsuario}
                onChange={handleChange}
                aria-invalid={Boolean(fieldErrors.codigoUsuario)}
              />
              {fieldErrors.codigoUsuario && (
                <span className="field-error">{fieldErrors.codigoUsuario}</span>
              )}
            </div>

            <div className="form-row-two">
              <div className="form-field">
                <label htmlFor="nombres">Nombres *</label>
                <input
                  id="nombres"
                  name="nombres"
                  type="text"
                  maxLength={100}
                  placeholder="Ej. Lucía"
                  value={form.nombres}
                  onChange={handleChange}
                  aria-invalid={Boolean(fieldErrors.nombres)}
                />
                {fieldErrors.nombres && <span className="field-error">{fieldErrors.nombres}</span>}
              </div>

              <div className="form-field">
                <label htmlFor="apellidos">Apellidos *</label>
                <input
                  id="apellidos"
                  name="apellidos"
                  type="text"
                  maxLength={100}
                  placeholder="Ej. Quispe Ramos"
                  value={form.apellidos}
                  onChange={handleChange}
                  aria-invalid={Boolean(fieldErrors.apellidos)}
                />
                {fieldErrors.apellidos && (
                  <span className="field-error">{fieldErrors.apellidos}</span>
                )}
              </div>
            </div>

            <div className="form-field">
              <label htmlFor="password">Contraseña temporal *</label>
              <div className="password-input-wrap">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  maxLength={100}
                  autoComplete="new-password"
                  placeholder="Mínimo 6 caracteres"
                  value={form.password}
                  onChange={handleChange}
                  aria-invalid={Boolean(fieldErrors.password)}
                />
                <button
                  type="button"
                  className="toggle-password-btn"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
            </div>

            <div className="form-field">
              <span className="fixed-role-label">Rol asignado</span>
              <div className="fixed-role-badge" aria-label="Rol fijo asignado">
                <ShieldCheck size={16} aria-hidden="true" />
                <span>TRABAJADORA — Atención en mostrador y venta</span>
              </div>
            </div>

            <button type="submit" className="primary-btn" disabled={submitting}>
              <UserPlus size={18} aria-hidden="true" />
              {submitting ? 'Registrando cuenta...' : 'Registrar Trabajadora'}
            </button>
          </form>
        </section>

        <section className="panel-card" aria-labelledby="titulo-listado">
          <div className="panel-heading-row">
            <div className="panel-heading">
              <Users size={20} aria-hidden="true" />
              <div>
                <h2 id="titulo-listado">Cuentas Registradas</h2>
                <p>Personal autorizado en Botica Biofarma.</p>
              </div>
            </div>

            <button
              type="button"
              className="secondary-btn"
              onClick={cargarUsuarios}
              disabled={loadingList}
            >
              <RefreshCw size={16} className={loadingList ? 'spin' : ''} aria-hidden="true" />
              Actualizar
            </button>
          </div>

          <div className="search-bar">
            <Search size={18} aria-hidden="true" />
            <input
              type="search"
              placeholder="Buscar por código, nombre o rol..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              aria-label="Buscar usuarios"
            />
          </div>

          <div className="table-scroll">
            {loadingList ? (
              <div className="empty-state">Cargando usuarios...</div>
            ) : usuariosFiltrados.length === 0 ? (
              <div className="empty-state">
                {busqueda.trim()
                  ? 'No se encontraron usuarios que coincidan con la búsqueda.'
                  : 'Aún no hay usuarios registrados.'}
              </div>
            ) : (
              <table className="usuarios-table">
                <thead>
                  <tr>
                    <th>Código</th>
                    <th>Personal</th>
                    <th>Rol</th>
                    <th>Estado</th>
                    <th>Registro</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {usuariosFiltrados.map((user) => {
                    const protegido = esCuentaProtegida(user);
                    const ocupado = processingActionId === user.idUsuario;

                    return (
                      <tr key={user.idUsuario}>
                        <td>
                          <code className="code-pill">{user.codigoUsuario}</code>
                        </td>
                        <td>
                          <div className="person-cell">
                            <span className="person-name">
                              {user.nombres} {user.apellidos}
                            </span>
                            {!protegido && (
                              <button
                                type="button"
                                className="icon-edit-btn"
                                onClick={() => abrirModalEdicion(user)}
                                title={`Editar nombres y apellidos de ${user.codigoUsuario}`}
                                aria-label={`Editar datos personales de ${user.nombres} ${user.apellidos}`}
                              >
                                <Pencil size={14} aria-hidden="true" />
                              </button>
                            )}
                          </div>
                        </td>
                        <td>
                          <span
                            className={`role-badge ${
                              user.rol === 'ADMINISTRADORA' ? 'role-admin' : 'role-worker'
                            }`}
                          >
                            {user.rol}
                          </span>
                        </td>
                        <td>
                          <span className={`status-dot ${user.activo ? 'is-active' : 'is-inactive'}`}>
                            {user.activo ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td className="date-cell">{formatearFecha(user.fechaCreacion)}</td>
                        <td className="actions-cell">
                          {protegido ? (
                            <span className="session-badge">Sesión actual</span>
                          ) : user.activo ? (
                            <button
                              type="button"
                              className="action-btn action-delete"
                              onClick={() => setConfirmDeleteUser(user)}
                              disabled={ocupado}
                            >
                              <Trash2 size={14} aria-hidden="true" />
                              Eliminar
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="action-btn action-activate"
                              onClick={() => handleActivar(user)}
                              disabled={ocupado}
                            >
                              <Power size={14} aria-hidden="true" />
                              Activar
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </section>
      </main>

      {editingUser && (
        <div className="modal-backdrop" role="presentation" onClick={cerrarModalEdicion}>
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-edit-title"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <div>
                <h3 id="modal-edit-title">Editar Datos Personales</h3>
                <p>
                  Cuenta: <code className="code-pill">{editingUser.codigoUsuario}</code>
                </p>
              </div>
              <button
                type="button"
                className="modal-close-btn"
                onClick={cerrarModalEdicion}
                aria-label="Cerrar modal de edición"
              >
                <X size={18} />
              </button>
            </div>

            <form className="usuario-form" onSubmit={handleEditSubmit} noValidate>
              <div className="form-field">
                <label htmlFor="edit-nombres">Nombres *</label>
                <input
                  id="edit-nombres"
                  name="nombres"
                  type="text"
                  maxLength={100}
                  value={editForm.nombres}
                  onChange={handleEditChange}
                  aria-invalid={Boolean(editErrors.nombres)}
                />
                {editErrors.nombres && <span className="field-error">{editErrors.nombres}</span>}
              </div>

              <div className="form-field">
                <label htmlFor="edit-apellidos">Apellidos *</label>
                <input
                  id="edit-apellidos"
                  name="apellidos"
                  type="text"
                  maxLength={100}
                  value={editForm.apellidos}
                  onChange={handleEditChange}
                  aria-invalid={Boolean(editErrors.apellidos)}
                />
                {editErrors.apellidos && (
                  <span className="field-error">{editErrors.apellidos}</span>
                )}
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="secondary-btn"
                  onClick={cerrarModalEdicion}
                  disabled={savingEdit}
                >
                  Cancelar
                </button>
                <button type="submit" className="primary-btn" disabled={savingEdit}>
                  {savingEdit ? 'Guardando...' : 'Guardar cambios'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {confirmDeleteUser && (
        <div
          className="modal-backdrop"
          role="presentation"
          onClick={() => !processingActionId && setConfirmDeleteUser(null)}
        >
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-delete-title"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <div>
                <h3 id="modal-delete-title">Confirmar eliminación</h3>
                <p>
                  Trabajadora: <code className="code-pill">{confirmDeleteUser.codigoUsuario}</code>
                </p>
              </div>
              <button
                type="button"
                className="modal-close-btn"
                onClick={() => !processingActionId && setConfirmDeleteUser(null)}
                aria-label="Cerrar confirmación"
              >
                <X size={18} />
              </button>
            </div>

            <p className="confirm-copy">
              ¿Desea eliminar a{' '}
              <strong>
                {confirmDeleteUser.nombres} {confirmDeleteUser.apellidos}
              </strong>
              ? Si no tiene ventas registradas será eliminada definitivamente; si posee ventas,
              pasará a estado <strong>Inactivo</strong> para preservar los reportes históricos.
            </p>

            <div className="modal-actions">
              <button
                type="button"
                className="secondary-btn"
                onClick={() => setConfirmDeleteUser(null)}
                disabled={Boolean(processingActionId)}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="action-btn action-delete action-btn-lg"
                onClick={confirmarEliminacion}
                disabled={Boolean(processingActionId)}
              >
                <Trash2 size={16} aria-hidden="true" />
                {processingActionId ? 'Procesando...' : 'Sí, eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
