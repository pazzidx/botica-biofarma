import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/auth/LoginPage';
import UsuariosPage from './pages/auth/UsuariosPage';
import HistorialVentasPage from './pages/ventas/HistorialVentasPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute allowedRoles={['ADMINISTRADORA']}>
                <UsuariosPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/ventas/historial"
            element={
              <ProtectedRoute allowedRoles={['ADMINISTRADORA', 'TRABAJADORA']}>
                <HistorialVentasPage />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/ventas/historial" replace />} />
          <Route path="*" element={<Navigate to="/ventas/historial" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}