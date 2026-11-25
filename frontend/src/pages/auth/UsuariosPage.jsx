import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  CheckCircle2,
  Eye,
  EyeOff,
  RefreshCw,
  Search,
  ShieldCheck,
  UserPlus,
  Users,
} from 'lucide-react';
import { listarUsuarios, registrarUsuario } from '../../api/usuariosApi';
import './UsuariosPage.css';

const INITIAL_FORM = {
  codigoUsuario: '',
  nombres: '',
  apellidos: '',
  password: '',
  rol: 'TRABAJADORA',
};

const CODIGO_REGEX = /^[A-Za-z0-9._-]+$/;

function validarFormulario(form) {
  const errors = {};
  const codigo = form.codigoUsuario.trim();

  if (!codigo) {
    errors.codigoUsuario = 'El código de usuario es obligatorio.';
  } else if (codigo.length < 3 || codigo.length > 20) {
    errors.codigoUsuario = 'Debe tener entre 3 y 20 caracteres.';
  } else if (!CODIGO_REGEX.test(codigo)) {
    errors.codigoUsuario = 'Solo letras, números, punto, guion y guion bajo.';
  }

  if (!form.nombres.trim()) {
    errors.nombres = 'Los nombres son obligatorios.';
  } else if (form.nombres.trim().length > 100) {
    errors.nombres = 'Máximo 100 caracteres.';
  }

  if (!form.apellidos.trim()) {
    errors.apellidos = 'Los apellidos son obligatorios.';
  } else if (form.apellidos.trim().length > 100) {
    errors.apellidos = 'Máximo 100 caracteres.';
  }

  if (!form.password) {
    errors.password = 'La contraseña es obligatoria.';
  } else if (form.password.length < 6 || form.password.length > 100) {
    errors.password = 'Debe tener entre 6 y 100 caracteres.';
  }

  if (!form.rol) {
    errors.rol = 'Seleccione un rol.';
  }

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
  const [form, setForm] = useState(INITIAL_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [usuarios, setUsuarios] = useState([]);
  const [loadingList, setLoadingList] = useState(true);
  const [busqueda, setBusqueda] = useState('');

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

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
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

    const validationErrors = validarFormulario(form);
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
        rol: form.rol,
      });

      setForm(INITIAL_FORM);
      setFieldErrors({});
      setShowPassword(false);
      setUsuarios((prev) => [creado, ...prev]);
      setFeedback({
        type: 'success',
        message: `Usuario "${creado.codigoUsuario}" registrado con rol ${creado.rol}.`,
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
              <h2 id="titulo-registro">Nueva Cuenta de Personal</h2>
              <p>Complete los datos para habilitar un acceso autorizado con contraseña cifrada.</p>
            </div>
          </div>

          <form className="usuario-form" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="codigoUsuario">Código de usuario *</label>
              <input
                id="codigoUsuario"
                name="codigoUsuario"
                type="text"
                maxLength={20}
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
              <label htmlFor="rol">Rol asignado *</label>
              <select
                id="rol"
                name="rol"
                value={form.rol}
                onChange={handleChange}
                aria-invalid={Boolean(fieldErrors.rol)}
              >
                <option value="TRABAJADORA">TRABAJADORA — Atención en mostrador y ventas</option>
                <option value="ADMINISTRADORA">ADMINISTRADORA — Control total del establecimiento</option>
              </select>
              {fieldErrors.rol && <span className="field-error">{fieldErrors.rol}</span>}
            </div>

            <button type="submit" className="primary-btn" disabled={submitting}>
              <UserPlus size={18} aria-hidden="true" />
              {submitting ? 'Registrando cuenta...' : 'Registrar Usuario'}
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
                  </tr>
                </thead>
                <tbody>
                  {usuariosFiltrados.map((user) => (
                    <tr key={user.idUsuario}>
                      <td>
                        <code className="code-pill">{user.codigoUsuario}</code>
                      </td>
                      <td>
                        <span className="person-name">
                          {user.nombres} {user.apellidos}
                        </span>
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
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
