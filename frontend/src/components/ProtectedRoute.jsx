import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

/**
 * Guardia de rutas protegidas para Botica Biofarma (HU-02 / BIO-32).
 * Protege el acceso por estado de autenticación y rol de usuario.
 * Evita la visualización anticipada de contenido antes de verificar la sesión.
 *
 * @param {Object} props
 * @param {React.ReactNode} props.children Componente a proteger
 * @param {string[]} [props.allowedRoles] Roles permitidos (ej. ['ADMINISTRADORA'])
 */
export default function ProtectedRoute({ children, allowedRoles = [] }) {
  const { isAuthenticated, hasRole, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="auth-loading-screen" role="status" aria-live="polite">
        <div className="auth-loading-spinner" aria-hidden="true" />
        <span>Comprobando sesión de usuario...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles.length > 0 && !hasRole(allowedRoles)) {
    return <Navigate to="/acceso-denegado" replace />;
  }

  return children;
}
