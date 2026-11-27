import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { TOKEN_STORAGE_KEY } from '../api/httpClient';
import { login as loginApi, logout as logoutApi } from '../api/authApi';

const USER_STORAGE_KEY = 'biofarma_user';
const EXPIRES_AT_KEY = 'biofarma_expires_at';

const AuthContext = createContext(null);

/**
 * Valida si un token JWT ha expirado utilizando la marca de expiración guardada
 * o decodificando el payload del token (claim exp).
 */
function isTokenExpired(token, expiresAt) {
  if (!token) return true;
  const now = Date.now();

  if (expiresAt && now >= Number(expiresAt)) {
    return true;
  }

  try {
    const parts = token.split('.');
    if (parts.length === 3) {
      // Reemplazar caracteres URL-safe base64
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const parsed = JSON.parse(jsonPayload);
      if (parsed.exp && now >= parsed.exp * 1000) {
        return true;
      }
    }
  } catch {
    // Si el token tiene formato inválido o no puede decodificarse, se considera expirado/inválido
    return true;
  }

  return false;
}

function readInitialSession() {
  try {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    const rawUser = localStorage.getItem(USER_STORAGE_KEY);
    const expiresAt = localStorage.getItem(EXPIRES_AT_KEY);

    if (!token || !rawUser) {
      return { token: null, usuario: null, expiresAt: null };
    }

    if (isTokenExpired(token, expiresAt)) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      localStorage.removeItem(USER_STORAGE_KEY);
      localStorage.removeItem(EXPIRES_AT_KEY);
      return { token: null, usuario: null, expiresAt: null };
    }

    const usuario = JSON.parse(rawUser);
    return { token, usuario, expiresAt: expiresAt ? Number(expiresAt) : null };
  } catch {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    localStorage.removeItem(EXPIRES_AT_KEY);
    return { token: null, usuario: null, expiresAt: null };
  }
}

export function AuthProvider({ children }) {
  const [session, setSessionState] = useState(() => readInitialSession());
  const [turnoActivo, setTurnoActivo] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    localStorage.removeItem(EXPIRES_AT_KEY);
    setSessionState({ token: null, usuario: null, expiresAt: null });
    setTurnoActivo(null);
  }, []);

  const login = useCallback(async ({ codigoUsuario, password }) => {
    setIsLoading(true);
    try {
      const data = await loginApi({ codigoUsuario, password });
      const { token, expiraEnMs, idUsuario, nombres, apellidos, rol } = data;

      const userObj = {
        idUsuario,
        codigoUsuario: data.codigoUsuario,
        nombres,
        apellidos,
        rol,
      };

      const expiresAt = Date.now() + (expiraEnMs || 28800000);

      localStorage.setItem(TOKEN_STORAGE_KEY, token);
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userObj));
      localStorage.setItem(EXPIRES_AT_KEY, String(expiresAt));

      setSessionState({ token, usuario: userObj, expiresAt });
      return userObj;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setIsLoading(true);
    try {
      // Intentar notificar al backend (siempre limpiamos sesión local incluso si falla red)
      await logoutApi();
    } catch {
      // Ignorar error de red o timeout al cerrar sesión
    } finally {
      clearSession();
      setIsLoading(false);
    }
  }, [clearSession]);

  // Escuchar evento global 401 disparado por httpClient
  useEffect(() => {
    const handleUnauthorized = () => {
      clearSession();
    };
    window.addEventListener('biofarma:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('biofarma:unauthorized', handleUnauthorized);
  }, [clearSession]);

  // Verificar periódicamente o al ganar foco si la sesión expiró
  useEffect(() => {
    const checkExpiration = () => {
      if (session.token && isTokenExpired(session.token, session.expiresAt)) {
        clearSession();
      }
    };

    window.addEventListener('focus', checkExpiration);
    const interval = setInterval(checkExpiration, 60000); // Revisar cada 1 minuto

    return () => {
      window.removeEventListener('focus', checkExpiration);
      clearInterval(interval);
    };
  }, [session.token, session.expiresAt, clearSession]);

  const usuario = session.usuario;
  const token = session.token;
  const rol = usuario?.rol || null;
  const isAuthenticated = Boolean(token && usuario && !isTokenExpired(token, session.expiresAt));
  const isAdmin = rol === 'ADMINISTRADORA';
  const isTrabajadora = rol === 'TRABAJADORA';

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
      isAdmin,
      isTrabajadora,
      turnoActivo,
      setTurnoActivo,
      isAuthenticated,
      isLoading,
      hasRole,
      login,
      logout,
      clearSession,
    }),
    [
      token,
      usuario,
      rol,
      isAdmin,
      isTrabajadora,
      turnoActivo,
      isAuthenticated,
      isLoading,
      hasRole,
      login,
      logout,
      clearSession,
    ]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthContext;
