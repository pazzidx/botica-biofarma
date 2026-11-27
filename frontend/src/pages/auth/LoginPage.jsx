import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../../api/authApi';
import { useAuth } from '../../context/useAuth';
import './LoginPage.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const { setSession } = useAuth();

  const [codigoUsuario, setCodigoUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setCargando(true);
    setError('');
    try {
      const data = await login({ codigoUsuario, password });
      setSession({ token: data.token, usuario: data.usuario });
      navigate('/ventas/historial', { replace: true });
    } catch (err) {
      setError(err.message || 'No se pudo iniciar sesión.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="login-shell">
      <div className="login-card">
        <span className="login-eyebrow">Botica Biofarma</span>
        <h1>Iniciar Sesión</h1>
        <p className="login-subtitle">
          Ingresa tu código de usuario y contraseña para acceder al sistema.
        </p>

        {error && (
          <div className="login-error" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form">
          <label>
            Código de usuario
            <input
              type="text"
              value={codigoUsuario}
              onChange={(e) => setCodigoUsuario(e.target.value)}
              placeholder="Ej. trab01"
              required
              autoFocus
            />
          </label>

          <label>
            Contraseña
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </label>

          <button type="submit" className="btn-login-primary" disabled={cargando}>
            {cargando ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>
      </div>
    </div>
  );
}