import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './pages/auth/LoginPage';
import InicioPage from './pages/dashboard/InicioPage';
import UsuariosPage from './pages/auth/UsuariosPage';
import CatalogoMedicamentosPage from './pages/inventario/CatalogoMedicamentosPage';
import HistorialVentasPage from './pages/ventas/HistorialVentasPage';
import AccesoDenegadoPage from './pages/error/AccesoDenegadoPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Ruta pública de inicio de sesión */}
          <Route path="/login" element={<LoginPage />} />

          {/* Rutas autenticadas bajo el layout principal */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            {/* Inicio disponible para cualquier usuario autenticado */}
            <Route path="/inicio" element={<InicioPage />} />

            {/* Historial de ventas con restricción por trabajadora entre turnos (HU-03) */}
            <Route
              path="/ventas/historial"
              element={
                <ProtectedRoute allowedRoles={['ADMINISTRADORA', 'TRABAJADORA']}>
                  <HistorialVentasPage />
                </ProtectedRoute>
              }
            />

            {/* Gestión de usuarios exclusiva para ADMINISTRADORA */}
            <Route
              path="/usuarios"
              element={
                <ProtectedRoute allowedRoles={['ADMINISTRADORA']}>
                  <UsuariosPage />
                </ProtectedRoute>
              }
            />

            {/* Catálogo de medicamentos exclusivo para ADMINISTRADORA (HU-04) */}
            <Route
              path="/medicamentos"
              element={
                <ProtectedRoute allowedRoles={['ADMINISTRADORA']}>
                  <CatalogoMedicamentosPage />
                </ProtectedRoute>
              }
            />

            {/* Vista amigable de acceso denegado (HTTP 403) */}
            <Route path="/acceso-denegado" element={<AccesoDenegadoPage />} />
          </Route>

          {/* Redirecciones automáticas por defecto */}
          <Route path="/" element={<Navigate to="/inicio" replace />} />
          <Route path="*" element={<Navigate to="/inicio" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
