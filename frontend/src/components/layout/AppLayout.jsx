import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Home, LogOut, Pill, Shield, User, Users } from 'lucide-react';
import { useAuth } from '../../context/useAuth';
import './AppLayout.css';

export default function AppLayout() {
  const { usuario, rol, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const nombreCompleto = usuario
    ? `${usuario.nombres} ${usuario.apellidos}`
    : 'Usuario';

  return (
    <div className="layout-shell">
      <header className="layout-navbar">
        <div className="nav-container">
          <div className="nav-brand-section">
            <div className="nav-brand-badge" aria-hidden="true">
              <Pill size={22} />
            </div>
            <div className="nav-brand-text">
              <span className="brand-name">Botica Biofarma</span>
              <span className="brand-app">Sistema POS & Inventario</span>
            </div>
          </div>

          <nav className="nav-links" aria-label="Navegación principal">
            <NavLink
              to="/inicio"
              className={({ isActive }) =>
                `nav-item ${isActive ? 'is-active' : ''}`
              }
            >
              <Home size={18} />
              <span>Inicio</span>
            </NavLink>

            {isAdmin && (
              <>
                <NavLink
                  to="/usuarios"
                  className={({ isActive }) =>
                    `nav-item ${isActive ? 'is-active' : ''}`
                  }
                >
                  <Users size={18} />
                  <span>Gestión de Usuarios</span>
                </NavLink>
                <NavLink
                  to="/medicamentos"
                  className={({ isActive }) =>
                    `nav-item ${isActive ? 'is-active' : ''}`
                  }
                >
                  <Pill size={18} />
                  <span>Catálogo de Medicamentos</span>
                </NavLink>
              </>
            )}
          </nav>

          <div className="nav-user-section">
            <div className="user-profile-chip" title={`Usuario: ${usuario?.codigoUsuario || ''}`}>
              <div className="user-avatar" aria-hidden="true">
                <User size={16} />
              </div>
              <div className="user-info">
                <span className="user-name">{nombreCompleto}</span>
                <span className={`role-badge ${isAdmin ? 'role-admin' : 'role-trabajadora'}`}>
                  {isAdmin ? <Shield size={11} /> : null}
                  {rol || 'USUARIA'}
                </span>
              </div>
            </div>

            <button
              type="button"
              className="logout-btn"
              onClick={handleLogout}
              title="Cerrar sesión en el sistema"
              aria-label="Cerrar sesión"
            >
              <LogOut size={16} />
              <span className="logout-text">Cerrar sesión</span>
            </button>
          </div>
        </div>
      </header>

      <main className="layout-main">
        <Outlet />
      </main>
    </div>
  );
}
