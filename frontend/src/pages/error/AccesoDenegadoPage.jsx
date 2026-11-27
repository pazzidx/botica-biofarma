import { Link } from 'react-router-dom';
import { Home, ShieldAlert } from 'lucide-react';
import { useAuth } from '../../context/useAuth';
import './AccesoDenegadoPage.css';

export default function AccesoDenegadoPage() {
  const { rol, usuario } = useAuth();

  return (
    <div className="denied-viewport">
      <div className="denied-card" role="alert" aria-live="assertive">
        <div className="denied-icon" aria-hidden="true">
          <ShieldAlert size={40} />
        </div>
        <span className="denied-code">HTTP 403</span>
        <h1 className="denied-title">Acceso Restringido</h1>
        <p className="denied-text">
          No cuenta con los permisos suficientes para acceder a este módulo.
          Esta sección está reservada exclusivamente para usuarias con rol <strong>ADMINISTRADORA</strong>.
        </p>

        <div className="denied-user-box">
          <span className="denied-user-label">Su cuenta actual:</span>
          <span className="denied-user-val">
            {usuario?.nombres} {usuario?.apellidos} ({rol || 'SIN ROL'})
          </span>
        </div>

        <div className="denied-actions">
          <Link to="/inicio" className="denied-home-btn">
            <Home size={18} />
            <span>Volver a la página principal</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
