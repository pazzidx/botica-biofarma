import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AlertCircle, Eye, EyeOff, Loader2, LogIn, Pill, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../context/useAuth';
import './LoginPage.css';

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [codigoUsuario, setCodigoUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Si ya se encuentra autenticada, redirigir a /inicio
  useEffect(() => {
    if (isAuthenticated) {
      const destino = location.state?.from?.pathname || '/inicio';
      navigate(destino, { replace: true });
    }
  }, [isAuthenticated, navigate, location.state]);

  const validarFormulario = () => {
    const errors = {};
    const codigo = codigoUsuario.trim();

    if (!codigo) {
      errors.codigoUsuario = 'El código de usuario es obligatorio.';
    }

    if (!password) {
      errors.password = 'La contraseña es obligatoria.';
    }

    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (submitting) return;

    const errors = validarFormulario();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setSubmitting(true);

    try {
      await login({
        codigoUsuario: codigoUsuario.trim(),
        password,
      });

      const destino = location.state?.from?.pathname || '/inicio';
      navigate(destino, { replace: true });
    } catch (error) {
      setErrorMessage(
        error.message || 'Error al iniciar sesión. Verifique sus credenciales.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-viewport">
      <div className="login-card" role="region" aria-label="Inicio de sesión">
        <header className="login-header">
          <div className="login-brand-icon" aria-hidden="true">
            <Pill size={32} />
          </div>
          <span className="login-brand-name">Botica Biofarma</span>
          <h1 className="login-title">Control de Inventario y Ventas</h1>
          <p className="login-subtitle">
            Ingrese sus credenciales autorizadas para acceder al sistema.
          </p>
        </header>

        {errorMessage && (
          <div className="login-alert is-error" role="alert">
            <AlertCircle size={20} className="alert-icon" />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form" noValidate>
          <div className="login-field">
            <label htmlFor="codigoUsuario" className="field-label">
              Código de Usuario
            </label>
            <div className="input-wrapper">
              <input
                id="codigoUsuario"
                name="codigoUsuario"
                type="text"
                autoComplete="username"
                autoCapitalize="none"
                spellCheck="false"
                placeholder="Ej. admin o c_mendoza"
                value={codigoUsuario}
                disabled={submitting}
                onChange={(e) => {
                  setCodigoUsuario(e.target.value);
                  if (fieldErrors.codigoUsuario) {
                    setFieldErrors((prev) => ({ ...prev, codigoUsuario: null }));
                  }
                }}
                className={fieldErrors.codigoUsuario ? 'input-error' : ''}
                aria-invalid={Boolean(fieldErrors.codigoUsuario)}
                aria-describedby={
                  fieldErrors.codigoUsuario ? 'codigoUsuario-error' : undefined
                }
              />
            </div>
            {fieldErrors.codigoUsuario && (
              <span id="codigoUsuario-error" className="error-text">
                {fieldErrors.codigoUsuario}
              </span>
            )}
          </div>

          <div className="login-field">
            <label htmlFor="password" className="field-label">
              Contraseña
            </label>
            <div className="input-wrapper">
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="••••••••"
                value={password}
                disabled={submitting}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (fieldErrors.password) {
                    setFieldErrors((prev) => ({ ...prev, password: null }));
                  }
                }}
                className={fieldErrors.password ? 'input-error' : ''}
                aria-invalid={Boolean(fieldErrors.password)}
                aria-describedby={
                  fieldErrors.password ? 'password-error' : undefined
                }
              />
              <button
                type="button"
                className="toggle-password-btn"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Ver contraseña'}
                disabled={submitting}
                tabIndex={0}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {fieldErrors.password && (
              <span id="password-error" className="error-text">
                {fieldErrors.password}
              </span>
            )}
          </div>

          <button
            type="submit"
            className="login-submit-btn"
            disabled={submitting}
          >
            {submitting ? (
              <>
                <Loader2 size={18} className="spinner" />
                <span>Iniciando sesión...</span>
              </>
            ) : (
              <>
                <LogIn size={18} />
                <span>Iniciar sesión</span>
              </>
            )}
          </button>
        </form>

        <footer className="login-footer">
          <div className="security-note">
            <ShieldCheck size={16} />
            <span>Acceso seguro protegido con tokens JWT y control por rol</span>
          </div>
        </footer>
      </div>
    </div>
  );
}
