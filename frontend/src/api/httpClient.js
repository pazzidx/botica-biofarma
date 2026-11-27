import axios from 'axios';

const TOKEN_STORAGE_KEY = 'biofarma_token';

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

httpClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const data = error.response?.data;
    const url = error.config?.url || '';

    // Diferenciar 401 de login (credenciales inválidas) de 401 en peticiones autenticadas (sesión expirada)
    const isLoginEndpoint = url.includes('/api/v1/auth/login');
    if (status === 401 && !isLoginEndpoint) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.dispatchEvent(new CustomEvent('biofarma:unauthorized'));
    }

    const normalizedError = {
      status: status || 0,
      message:
        data?.message ||
        (status === 403
          ? 'No tiene permisos suficientes para realizar esta acción.'
          : status === 401 && isLoginEndpoint
            ? 'Código de usuario o contraseña incorrectos.'
            : status === 401
              ? 'Su sesión ha expirado o no es válida. Inicie sesión nuevamente.'
              : status === 0
                ? 'No se pudo conectar con el servidor de Botica Biofarma.'
                : 'Ocurrió un error al procesar la solicitud.'),
      fieldErrors: data?.fieldErrors || {},
    };

    return Promise.reject(normalizedError);
  }
);

export { TOKEN_STORAGE_KEY };
export default httpClient;
