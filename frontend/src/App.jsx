import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import UsuariosPage from './pages/auth/UsuariosPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute requireAuth={false}>
                <UsuariosPage />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/usuarios" replace />} />
          <Route path="*" element={<Navigate to="/usuarios" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
