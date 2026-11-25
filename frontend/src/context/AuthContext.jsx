import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { TOKEN_STORAGE_KEY } from '../api/httpClient';

const USER_STORAGE_KEY = 'biofarma_user';
const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [usuario, setUsuario] = useState(() => readStoredUser());
  const [turnoActivo, setTurnoActivo] = useState(null);

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setToken(null);
    setUsuario(null);
    setTurnoActivo(null);
  }, []);

  const setSession = useCallback(({ token: newToken, usuario: newUsuario, turno = null }) => {
    if (newToken) {
      localStorage.setItem(TOKEN_STORAGE_KEY, newToken);
      setToken(newToken);
    }
    if (newUsuario) {
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(newUsuario));
      setUsuario(newUsuario);
    }
    setTurnoActivo(turno);
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => clearSession();
    window.addEventListener('biofarma:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('biofarma:unauthorized', handleUnauthorized);
  }, [clearSession]);

  const rol = usuario?.rol || null;
  const isAuthenticated = Boolean(token && usuario);

  const hasRole = useCallback(
    (allowedRoles) => {
      if (!allowedRoles || allowedRoles.length === 0) return true;
      return Boolean(rol && allowedRoles.includes(rol));
    },
    [rol]
  );

  const value = useMemo(
    () => ({
      token,
      usuario,
      rol,
      turnoActivo,
      isAuthenticated,
      hasRole,
      setSession,
      clearSession,
    }),
    [token, usuario, rol, turnoActivo, isAuthenticated, hasRole, setSession, clearSession]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthContext;
