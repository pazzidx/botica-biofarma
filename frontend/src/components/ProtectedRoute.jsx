import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

/**
 * Guardia reutilizable de rutas por autenticación y rol.
 * En HU-01 puede operar con requireAuth={false} hasta integrar el login JWT en HU-02.
 */
export default function ProtectedRoute({
  children,
  allowedRoles = [],
  requireAuth = true,
  redirectTo = '/login',
}) {
  const { isAuthenticated, hasRole } = useAuth();

  if (requireAuth && !isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  if (requireAuth && allowedRoles.length > 0 && !hasRole(allowedRoles)) {
    return (
      <div className="access-denied-card" role="alert">
        <h2>Acceso restringido</h2>
        <p>No cuenta con permisos suficientes para acceder a este módulo.</p>
      </div>
    );
  }

  return children;
}
