import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import './Navbar.css';

export default function Navbar() {
  const { isAuthenticated, usuario, rol, clearSession } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    clearSession();
    navigate('/login', { replace: true });
  };

  return (
    <nav className="bf-navbar" aria-label="Navegación principal">
      <div className="bf-navbar-inner">
        <div className="bf-navbar-left">
          <Link to="/ventas/historial" className="bf-brand">
            Botica Biofarma
          </Link>
          {isAuthenticated && (
            <div className="bf-nav-links">
              <Link
                to="/ventas/historial"
                className={location.pathname === '/ventas/historial' ? 'active' : ''}
              >
                Historial de Ventas
              </Link>
              {rol === 'ADMINISTRADORA' && (
                <Link
                  to="/usuarios"
                  className={location.pathname === '/usuarios' ? 'active' : ''}
                >
                  Usuarios
                </Link>
              )}
            </div>
          )}
        </div>

        <div className="bf-navbar-right">
          {isAuthenticated ? (
            <>
              <span className="bf-user-pill">
                <strong>{usuario?.nombreCompleto || usuario?.codigoUsuario}</strong>
                <span> · {rol}</span>
              </span>
              <button type="button" className="bf-btn-logout" onClick={handleLogout}>
                Cerrar sesión
              </button>
            </>
          ) : (
            <Link to="/login" className="bf-btn-login-link">
              Iniciar sesión
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}