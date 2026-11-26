import { Link } from 'react-router-dom';
import {
  ArrowRight,
  Clock,
  Package,
  Receipt,
  Shield,
  ShoppingBag,
  TrendingUp,
  UserCheck,
  Users,
} from 'lucide-react';
import { useAuth } from '../../context/useAuth';
import './InicioPage.css';

const FECHA_HOY = new Intl.DateTimeFormat('es-PE', {
  dateStyle: 'full',
}).format(new Date());

export default function InicioPage() {
  const { usuario, rol, isAdmin } = useAuth();

  return (
    <div className="inicio-shell">
      <header className="inicio-welcome-banner">
        <div className="welcome-text-block">
          <span className="welcome-date">{FECHA_HOY}</span>
          <h1 className="welcome-title">
            ¡Bienvenida al sistema, {usuario?.nombres || 'Usuaria'}!
          </h1>
          <p className="welcome-subtitle">
            Sistema Web Integral de Control de Inventario y Ventas — Botica Biofarma
          </p>
        </div>

        <div className="welcome-chip">
          <div className="chip-icon">
            {isAdmin ? <Shield size={20} /> : <UserCheck size={20} />}
          </div>
          <div className="chip-meta">
            <span className="chip-label">Rol Asignado</span>
            <span className="chip-value">{rol}</span>
          </div>
        </div>
      </header>

      <section className="inicio-cards-grid">
        {isAdmin ? (
          <>
            <div className="action-card is-active-module">
              <div className="card-header">
                <div className="card-icon is-primary">
                  <Users size={24} />
                </div>
                <span className="badge-ready">Módulo Habilitado</span>
              </div>
              <h2 className="card-title">Gestión de Usuarios</h2>
              <p className="card-desc">
                Registro de trabajadoras y administradoras, supervisión de cuentas,
                estados de acceso y asignación de roles del sistema.
              </p>
              <Link to="/usuarios" className="card-action-btn">
                <span>Administrar usuarios</span>
                <ArrowRight size={16} />
              </Link>
            </div>

            <div className="action-card is-upcoming">
              <div className="card-header">
                <div className="card-icon">
                  <Package size={24} />
                </div>
                <span className="badge-upcoming">Sprint 2</span>
              </div>
              <h2 className="card-title">Catálogo e Inventario</h2>
              <p className="card-desc">
                Administración de medicamentos, control de lotes con fechas de vencimiento
                y alertas preventivas de stock mínimo.
              </p>
              <span className="card-note">Próxima implementación (HU-04 a HU-07)</span>
            </div>

            <div className="action-card is-upcoming">
              <div className="card-header">
                <div className="card-icon">
                  <ShoppingBag size={24} />
                </div>
                <span className="badge-upcoming">Sprint 3</span>
              </div>
              <h2 className="card-title">Punto de Venta (POS)</h2>
              <p className="card-desc">
                Ventas multiproducto con descuento automático por lógica FEFO,
                impresión térmica de tickets y retención de recetas.
              </p>
              <span className="card-note">Próxima implementación (HU-08 a HU-10)</span>
            </div>

            <div className="action-card is-upcoming">
              <div className="card-header">
                <div className="card-icon">
                  <TrendingUp size={24} />
                </div>
                <span className="badge-upcoming">Sprint 4</span>
              </div>
              <h2 className="card-title">Reportes y Supervisión</h2>
              <p className="card-desc">
                Consolidado diario de ventas, trazabilidad por trabajadora y turno,
                y valorización global de inventario.
              </p>
              <span className="card-note">Próxima implementación (HU-11 y HU-12)</span>
            </div>
          </>
        ) : (
          <>
            <div className="action-card is-info-card">
              <div className="card-header">
                <div className="card-icon is-primary">
                  <Clock size={24} />
                </div>
                <span className="badge-ready">Atención en Mostrador</span>
              </div>
              <h2 className="card-title">Turno de Atención Activo</h2>
              <p className="card-desc">
                Su cuenta está habilitada para registrar ventas y atender al público en
                mostrador. La trazabilidad de sus operaciones se asociará automáticamente
                a su código de usuario.
              </p>
              <span className="card-note">
                Código de trabajadora: <strong>{usuario?.codigoUsuario}</strong>
              </span>
            </div>

            <div className="action-card is-upcoming">
              <div className="card-header">
                <div className="card-icon">
                  <ShoppingBag size={24} />
                </div>
                <span className="badge-upcoming">Sprint 3</span>
              </div>
              <h2 className="card-title">Registro de Ventas</h2>
              <p className="card-desc">
                Carrito de venta en mostrador con cálculo automático, descuento FEFO y
                emisión de comprobante interno de venta.
              </p>
              <span className="card-note">En desarrollo para próximo Sprint</span>
            </div>

            <div className="action-card is-upcoming">
              <div className="card-header">
                <div className="card-icon">
                  <Receipt size={24} />
                </div>
                <span className="badge-upcoming">Sprint 3</span>
              </div>
              <h2 className="card-title">Recetas Médicas Retenidas</h2>
              <p className="card-desc">
                Constancia de retención de recetas para medicamentos bajo prescripción
                médica vinculadas a la venta.
              </p>
              <span className="card-note">En desarrollo para próximo Sprint</span>
            </div>
          </>
        )}
      </section>
    </div>
  );
}
