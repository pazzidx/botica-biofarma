# Desarrollo Detallado de las Historias de Usuario (HU-01 a HU-12)
**Sistema Web de Control de Inventario y Ventas — Botica Biofarma**

---

## Índice de Historias de Usuario

- **ÉPICA 01: Acceso y Seguridad**
  - [HU-01. Registro de usuarios](#hu-01-registro-de-usuarios)
  - [HU-02. Inicio y cierre de sesión con control de acceso por rol](#hu-02-inicio-y-cierre-de-sesión-con-control-de-acceso-por-rol)
  - [HU-03. Restricción de historial de ventas por trabajadora entre turnos](#hu-03-restricción-de-historial-de-ventas-por-trabajadora-entre-turnos)
- **ÉPICA 02: Gestión de Catálogo e Inventario**
  - [HU-04. Administración del catálogo de medicamentos](#hu-04-administración-del-catálogo-de-medicamentos)
  - [HU-05. Búsqueda y consulta de disponibilidad y stock de medicamentos](#hu-05-búsqueda-y-consulta-de-disponibilidad-y-stock-de-medicamentos)
  - [HU-06. Registro de ingresos de mercadería con lotes, vencimientos y faltantes](#hu-06-registro-de-ingresos-de-mercadería-con-lotes-vencimientos-y-faltantes)
  - [HU-07. Alertas preventivas de stock mínimo y lotes próximos a vencer](#hu-07-alertas-preventivas-de-stock-mínimo-y-lotes-próximos-a-vencer)
- **ÉPICA 03: Ventas y Comprobantes**
  - [HU-08. Registro de ventas multiproducto con cálculo y descuento automático de stock](#hu-08-registro-de-ventas-multiproducto-con-cálculo-y-descuento-automático-de-stock)
  - [HU-09. Generación e impresión térmica de tickets internos de venta](#hu-09-generación-e-impresión-térmica-de-tickets-internos-de-venta)
  - [HU-10. Registro de constancia de recetas médicas retenidas en ventas](#hu-10-registro-de-constancia-de-recetas-médicas-retenidas-en-ventas)
- **ÉPICA 04: Reportes y Supervisión Administrativa**
  - [HU-11. Trazabilidad automática de la trabajadora responsable por venta](#hu-11-trazabilidad-automática-de-la-trabajadora-responsable-por-venta)
  - [HU-12. Generación de reportes diarios consolidados de ventas e inventario](#hu-12-generación-de-reportes-diarios-consolidados-de-ventas-e-inventario)

---

## ÉPICA 01: Acceso y Seguridad

### HU-01. Registro de usuarios
- **Épica:** EP-01 – Acceso y seguridad
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-01, RNF-03
- **Dependencia:** Estructura base del proyecto y persistencia de usuarios.

#### Historia de usuario
> Como **administradora de la Botica Biofarma**, quiero registrar nuevos usuarios con sus datos personales, código de acceso, contraseña y rol correspondiente, para gestionar las cuentas del personal autorizado que utilizará el sistema.

#### Descripción funcional
Esta historia permite que la administradora cree cuentas individuales para las trabajadoras y usuarios autorizados del establecimiento. El sistema debe solicitar un código único de usuario, nombres, apellidos, contraseña y rol (`ADMINISTRADORA` o `TRABAJADORA`). Antes de guardar la información, debe validar que los campos obligatorios estén completos, comprobar que el código no esté duplicado y cifrar la contraseña mediante un algoritmo seguro (BCrypt). El acceso a esta función está restringido exclusivamente al perfil de administradora y la respuesta del sistema nunca debe exponer contraseñas ni hashes.

#### Criterios de aceptación
- **CA-01. Registro exitoso:** Dado que una administradora autorizada ingresa datos válidos y un código disponible, cuando solicita registrar un usuario, entonces el sistema crea la cuenta y devuelve una confirmación con sus datos permitidos.
- **CA-02. Validación de datos:** Dado que existen campos obligatorios vacíos o con formato inválido, cuando se solicita el registro, entonces el sistema rechaza la operación y muestra un mensaje claro sin guardar información.
- **CA-03. Prevención de duplicados:** Dado que un código ya pertenece a otro usuario registrado, cuando se intenta crear una nueva cuenta con ese mismo código, entonces el sistema rechaza el registro y conserva intacta la cuenta original.
- **CA-04. Protección de contraseña:** Dado que se proporciona una contraseña válida, cuando se guarda la cuenta en la base de datos, entonces la contraseña se almacena cifrada mediante BCrypt y nunca en texto plano.
- **CA-05. Restricción de permisos:** Dado que una trabajadora o un usuario no autenticado intenta registrar una cuenta, cuando realiza la solicitud, entonces el sistema deniega el acceso.
- **CA-06. Protección de información sensible:** Dado que el registro finaliza correctamente o presenta un error, cuando se genera la respuesta, entonces no se exponen contraseñas, hashes ni detalles internos del servidor.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-01.1** | Implementar entidad de usuario, roles y persistencia en SQL Server. | Modelo y repositorio de usuarios. |
| **ST-01.2** | Implementar lógica de negocio, validaciones y cifrado BCrypt. | Caso de uso de registro de usuario. |
| **ST-01.3** | Desarrollar endpoint REST `POST /api/v1/usuarios` y control de permisos. | API segura de registro. |
| **ST-01.4** | Desarrollar formulario de registro de usuarios en la interfaz web. | Pantalla administrativa de usuarios. |
| **ST-01.5** | Ejecutar pruebas unitarias y de integración del registro. | Evidencias de pruebas funcionales y de permisos. |

**Entregable de la historia:** Módulo funcional (interfaz y servicio REST) para el registro seguro de cuentas de usuario respaldado por pruebas verificables.

---

### HU-02. Inicio y cierre de sesión con control de acceso por rol
- **Épica:** EP-01 – Acceso y seguridad
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-02, RF-03, RF-19, RNF-04, RNF-05
- **Dependencia:** HU-01.

#### Historia de usuario
> Como **personal autorizado de la Botica Biofarma (ADMINISTRADORA o TRABAJADORA)**, quiero iniciar sesión con mi código de acceso y contraseña, y cerrarla al finalizar mi turno, para acceder únicamente a las funciones permitidas según mi rol y proteger la información del establecimiento.

#### Descripción funcional
Esta historia permite que los usuarios registrados ingresen al sistema mediante su código personal y contraseña. El servidor debe validar las credenciales, verificar que la cuenta se encuentre activa y emitir un token de sesión (JWT) junto con el nombre y rol del usuario. Todas las rutas protegidas deben validar la vigencia del token y restringir las funciones administrativas exclusivamente al rol `ADMINISTRADORA`. Al finalizar la jornada o cambio de turno, el usuario podrá cerrar su sesión para bloquear el acceso inmediato desde el navegador.

#### Criterios de aceptación
- **CA-01. Inicio de sesión exitoso:** Dado que un usuario activo ingresa un código y contraseña válidos, cuando solicita iniciar sesión, entonces el sistema valida su identidad y le permite el acceso según su rol.
- **CA-02. Credenciales incorrectas o cuenta inactiva:** Dado que el código o la contraseña son incorrectos, o la cuenta está desactivada, cuando intenta ingresar, entonces el sistema rechaza el acceso mediante un mensaje claro en español.
- **CA-03. Protección de rutas privadas:** Dado que una solicitud no incluye un token de sesión válido o este ha expirado, cuando intenta acceder a un recurso protegido, entonces el sistema deniega la operación.
- **CA-04. Restricción de funciones administrativas:** Dado que una trabajadora intenta acceder a vistas o servicios exclusivos de administración (gestión de usuarios, catálogo o reportes globales), cuando realiza la acción, entonces el sistema bloquea el acceso.
- **CA-05. Cierre de sesión:** Dado que un usuario mantiene una sesión activa, cuando selecciona cerrar sesión al término de su turno, entonces el sistema finaliza la sesión local y redirige a la pantalla de inicio de sesión.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-02.1** | Configurar Spring Security y generación/validación de tokens JWT. | Configuración de seguridad y filtros. |
| **ST-02.2** | Implementar servicio y endpoint de inicio y cierre de sesión. | API de autenticación. |
| **ST-02.3** | Implementar autorización basada en roles (ADMINISTRADORA y TRABAJADORA). | Reglas de acceso en servidor. |
| **ST-02.4** | Desarrollar pantalla de inicio de sesión y protección de rutas en React. | Interfaz de login y control de sesión. |
| **ST-02.5** | Ejecutar pruebas de autenticación, cierre de sesión y bloqueo por rol. | Suite de pruebas de seguridad. |

**Entregable de la historia:** Sistema de autenticación y control de acceso por roles operativo en frontend y backend.

---

### HU-03. Restricción de historial de ventas por trabajadora entre turnos
- **Épica:** EP-01 – Acceso y seguridad
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-20, RNF-05
- **Dependencia:** HU-02 y HU-08.

#### Contexto y delimitación
> En la arquitectura del sistema, separamos la funcionalidad en dos historias puntuales: **HU-03** (privacidad de consulta entre trabajadoras dentro de EP-01: Acceso y seguridad, cubriendo RF-20) y **HU-11** (registro automático de la responsable en cada venta dentro de EP-04: Reportes y supervisión, cubriendo RF-18). Así cada historia tiene un propósito puntual sin mezclar seguridad con reportes.

#### Historia de usuario
> Como **trabajadora de la Botica Biofarma**, quiero consultar únicamente el historial de las ventas registradas con mi propia cuenta, para mantener la privacidad de mis operaciones frente a otros turnos de trabajo.

#### Descripción funcional
Esta historia garantiza la confidencialidad operativa entre las trabajadoras del establecimiento. Cuando una usuaria con rol `TRABAJADORA` accede al listado o historial de ventas, el servidor debe filtrar automáticamente los resultados utilizando el identificador obtenido de su sesión autenticada, impidiendo visualizar operaciones efectuadas por otras compañeras. Por su parte, el rol `ADMINISTRADORA` conserva acceso completo al historial global de todas las trabajadoras. El filtrado debe aplicarse obligatoriamente en el servidor para evitar accesos indebidos mediante manipulación de parámetros en la petición.

#### Criterios de aceptación
- **CA-01. Consulta filtrada para trabajadora:** Dado que una trabajadora autenticada consulta el historial de ventas, cuando el sistema carga los registros, entonces muestra únicamente las ventas asociadas a su propio usuario.
- **CA-02. Bloqueo de consulta de ventas ajenas:** Dado que una trabajadora intenta consultar el detalle de una venta registrada por otra trabajadora, cuando envía la solicitud al servidor, entonces el sistema rechaza el acceso.
- **CA-03. Acceso completo para la administradora:** Dado que la administradora consulta el historial de ventas, cuando solicita los registros, entonces visualiza las operaciones realizadas por todo el personal.
- **CA-04. Validación desde el token de sesión:** Dado que se realiza una consulta de historial, cuando el servidor procesa la petición, entonces determina la identidad del usuario a partir de su token activo y no mediante parámetros enviados manualmente por el cliente.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-03.1** | Implementar filtro por usuario autenticado en las consultas de ventas del servidor. | Lógica de filtrado por identidad y rol. |
| **ST-03.2** | Validar propiedad del registro al consultar el detalle de una venta individual. | Control de acceso a nivel de registro. |
| **ST-03.3** | Adaptar la vista de historial de ventas en la interfaz según el rol activo. | Pantalla de historial filtrada. |
| **ST-03.4** | Ejecutar pruebas de privacidad entre cuentas de distintas trabajadoras. | Evidencias de pruebas de autorización. |

**Entregable de la historia:** Control de privacidad operativo que aísla el historial de ventas de cada trabajadora entre turnos.

---

## ÉPICA 02: Gestión de Catálogo e Inventario

### HU-04. Administración del catálogo de medicamentos
- **Épica:** EP-02 – Gestión de catálogo e inventario
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-04, RF-05, RF-06, RNF-07
- **Dependencia:** HU-02.

#### Historia de usuario
> Como **administradora de la Botica Biofarma**, quiero registrar, modificar y desactivar medicamentos en el catálogo, para mantener organizada y actualizada la información comercial de los productos del establecimiento.

#### Descripción funcional
Esta historia permite que la administradora administre los medicamentos comercializados en la botica. El sistema debe permitir registrar nuevos productos ingresando su código identificador único, nombre comercial o genérico, presentación, precio unitario en soles, nivel de stock mínimo y el indicador de si requiere receta médica retenida. Asimismo, debe permitir editar los datos comerciales de medicamentos existentes y desactivar aquellos que dejen de comercializarse. La desactivación aplica un borrado lógico que oculta el producto para nuevas ventas pero conserva intactos sus registros históricos. El catálogo no modifica directamente el stock físico; las cantidades ingresan únicamente mediante recepciones de mercadería.

#### Criterios de aceptación
- **CA-01. Registro válido:** Dado que la administradora ingresa todos los datos obligatorios válidos de un medicamento, cuando confirma el registro, entonces el producto se incorpora al catálogo activo.
- **CA-02. Código duplicado:** Dado que existe otro medicamento registrado con el mismo código identificador, cuando se intenta guardar uno nuevo, entonces el sistema rechaza la operación e informa el error.
- **CA-03. Validación de datos:** Dado que el nombre está vacío o el precio unitario o stock mínimo tienen valores negativos o inválidos, cuando se intenta guardar, entonces el sistema muestra un mensaje claro en español y no guarda cambios.
- **CA-04. Actualización de datos:** Dado que existe un medicamento registrado, cuando la administradora edita sus datos comerciales permitidos, entonces los cambios se guardan y se reflejan en las consultas posteriores.
- **CA-05. Desactivación lógica:** Dado que un medicamento ya no se comercializa, cuando la administradora lo desactiva, entonces deja de aparecer disponible para nuevas ventas sin eliminar su historial de movimientos.
- **CA-06. Restricción de acceso:** Dado que una trabajadora intenta registrar, editar o desactivar medicamentos, cuando realiza la solicitud, entonces el sistema deniega el acceso.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-04.1** | Diseñar entidad Medicamento y tabla con restricciones de unicidad en SQL Server. | Modelo y persistencia del catálogo. |
| **ST-04.2** | Implementar casos de uso de registro, actualización y desactivación lógica. | Lógica de negocio del catálogo. |
| **ST-04.3** | Desarrollar endpoints REST del catálogo protegidos para rol `ADMINISTRADORA`. | API de gestión de medicamentos. |
| **ST-04.4** | Desarrollar interfaz de mantenimiento (listado, registro, edición y desactivación). | Pantalla administrativa de catálogo. |
| **ST-04.5** | Ejecutar pruebas unitarias y de integración de validaciones y permisos. | Suite de pruebas del catálogo. |

**Entregable de la historia:** Módulo administrativo completo para el registro, edición y desactivación lógica de medicamentos.

---

### HU-05. Búsqueda y consulta de disponibilidad y stock de medicamentos
- **Épica:** EP-02 – Gestión de catálogo e inventario
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-07, RF-08, RNF-02
- **Dependencia:** HU-04 y HU-06.

#### Historia de usuario
> Como **trabajadora de la Botica Biofarma**, quiero buscar medicamentos por nombre o código y visualizar en tiempo real su estado de disponibilidad, precio unitario y cantidad en stock, para atender rápidamente al cliente sin revisar físicamente los estantes.

#### Descripción funcional
Esta historia permite al personal autorizado consultar los medicamentos activos del catálogo mediante un buscador rápido por nombre o código identificador. Como resultado, el sistema debe mostrar cada producto coincidente junto con su presentación, precio unitario en soles, cantidad total en stock vigente y estado de disponibilidad (`Disponible` o `Agotado`). Cuando no existan coincidencias, el sistema debe mostrar un mensaje informativo claro. La consulta debe responder en un tiempo máximo de 2 segundos y obtener las existencias directamente del inventario actualizado.

#### Criterios de aceptación
- **CA-01. Búsqueda por código:** Dado que existe un medicamento activo con el código ingresado, cuando se ejecuta la búsqueda, entonces el sistema muestra sus datos comerciales y existencias.
- **CA-02. Búsqueda por nombre:** Dado que existen medicamentos activos cuyos nombres coinciden parcial o totalmente con el texto buscado, cuando se consulta, entonces se listan los resultados correspondientes.
- **CA-03. Visualización de stock vigente:** Dado que el medicamento posee unidades en inventario, cuando se muestra en los resultados, entonces presenta su precio unitario y la cantidad exacta disponible.
- **CA-04. Producto agotado:** Dado que un medicamento activo tiene stock igual a cero, cuando aparece en la consulta, entonces se identifica claramente como Agotado.
- **CA-05. Sin coincidencias:** Dado que el término buscado no coincide con ningún medicamento activo, cuando se realiza la consulta, entonces el sistema informa que no se encontraron resultados.
- **CA-06. Tiempo de respuesta:** Dado que se realiza una búsqueda bajo condiciones normales de operación, cuando el servidor procesa la consulta, entonces muestra los resultados en un máximo de 2 segundos.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-05.1** | Implementar consultas indexadas por nombre y código en el repositorio. | Consultas optimizadas en base de datos. |
| **ST-05.2** | Implementar servicio y endpoint REST de búsqueda con cálculo de stock vigente. | API de consulta de disponibilidad. |
| **ST-05.3** | Desarrollar buscador en la interfaz con indicadores visuales de disponibilidad. | Componente de búsqueda en mostrador. |
| **ST-05.4** | Ejecutar pruebas funcionales y medición de tiempo de respuesta. | Evidencias de pruebas y rendimiento. |

**Entregable de la historia:** Buscador ágil de medicamentos integrado con las existencias reales del inventario.

---

### HU-06. Registro de ingresos de mercadería con lotes, vencimientos y faltantes
- **Épica:** EP-02 – Gestión de catálogo e inventario
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-09, RNF-06
- **Dependencia:** HU-04.

#### Historia de usuario
> Como **administradora de la Botica Biofarma**, quiero registrar los ingresos de mercadería recibida detallando el número de lote, la cantidad en buen estado, la fecha de vencimiento y observaciones de faltantes, para alimentar el inventario con trazabilidad y controlar las diferencias en las entregas.

#### Descripción funcional
Esta historia permite incorporar nuevas existencias al inventario cuando llega mercadería al establecimiento. Al registrar un ingreso, el usuario selecciona el medicamento del catálogo, ingresa el número de lote, la cantidad de unidades recibidas en buen estado, la fecha de vencimiento del lote y, de ser necesario, una observación detallando unidades faltantes o incidencias en la entrega. Al confirmar la operación, el sistema incrementa automáticamente el stock del medicamento únicamente con las unidades en buen estado y guarda el historial del movimiento de entrada de forma atómica.

#### Criterios de aceptación
- **CA-01. Registro de ingreso exitoso:** Dado que se selecciona un medicamento activo y se ingresan lote, cantidad en buen estado mayor a cero y fecha de vencimiento válida, cuando se confirma el ingreso, entonces se registra el lote y aumenta el stock disponible.
- **CA-02. Registro de observaciones de faltantes:** Dado que una entrega presenta unidades faltantes respecto a lo esperado, cuando se registra la observación en el ingreso, entonces el sistema conserva dicha anotación asociada al movimiento sin sumar las unidades no recibidas.
- **CA-03. Validación de datos:** Dado que se ingresan cantidades nulas o negativas, lote vacío o fecha de vencimiento ya expirada, cuando se intenta confirmar, entonces el sistema rechaza la operación con un mensaje claro.
- **CA-04. Trazabilidad de movimientos:** Dado que se confirma un ingreso, cuando se consulta el historial de inventario, entonces se visualiza el medicamento, lote, cantidad ingresada, fecha, observación y usuario que lo registró.
- **CA-05. Integridad transaccional:** Dado que ocurre un error técnico durante el guardado del ingreso, cuando falla la operación, entonces se revierten todos los cambios sin alterar el stock previo.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-06.1** | Diseñar entidades Lote e IngresoInventario con trazabilidad de movimientos. | Modelo de persistencia de ingresos y lotes. |
| **ST-06.2** | Implementar caso de uso transaccional de ingreso y actualización de stock. | Lógica de negocio de recepción de mercadería. |
| **ST-06.3** | Desarrollar endpoint REST para registrar y consultar ingresos de mercadería. | API de ingresos e inventario. |
| **ST-06.4** | Desarrollar formulario de ingreso de mercadería y listado de movimientos. | Interfaz de recepción en almacén. |
| **ST-06.5** | Ejecutar pruebas de ingreso, cálculo de stock y validaciones de lote/vencimiento. | Evidencias de pruebas de inventario. |

**Entregable de la historia:** Módulo de recepción e ingreso de mercadería integrado con control de lotes, vencimientos, observaciones de faltantes e inventario.

---

### HU-07. Alertas preventivas de stock mínimo y lotes próximos a vencer
- **Épica:** EP-02 – Gestión de catálogo e inventario
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-11, RF-12
- **Dependencia:** HU-04 y HU-06.

#### Historia de usuario
> Como **administradora de la Botica Biofarma**, quiero visualizar alertas preventivas cuando un medicamento alcance su stock mínimo o cuando un lote esté próximo a vencer, para gestionar oportunamente su reposición o retiro del mostrador.

#### Descripción funcional
Esta historia permite monitorear preventivamente el estado del inventario desde un panel de alertas interno en el sistema. Por un lado, el sistema evalúa las existencias totales de cada medicamento activo y genera una alerta de reposición cuando la cantidad disponible es igual o inferior al nivel mínimo configurado. Por otro lado, evalúa las fechas de vencimiento de los lotes con stock positivo y clasifica alertas preventivas con anticipación de 30, 15 y 7 días, además de marcar los lotes ya vencidos.

#### Criterios de aceptación
- **CA-01. Alerta de stock mínimo:** Dado que las existencias vigentes de un medicamento alcanzan o descienden de su nivel mínimo configurado, cuando se consulta el panel de alertas, entonces aparece listado con su stock actual y mínimo.
- **CA-02. Stock dentro de rango normal:** Dado que las existencias superan el stock mínimo configurado, cuando se evalúa el producto, entonces no genera alerta de reposición.
- **CA-03. Alertas de vencimiento a 30, 15 y 7 días:** Dado que un lote con existencias tiene fecha de vencimiento dentro de los próximos 30, 15 o 7 días calendario, cuando se consulta el panel, entonces se muestra clasificado según su nivel de proximidad.
- **CA-04. Identificación de lotes vencidos:** Dado que la fecha de vencimiento de un lote ya se cumplió, cuando se evalúa el inventario, entonces el sistema lo identifica como Vencido para evitar su comercialización.
- **CA-05. Detalle claro de la alerta:** Dado que existe una condición de alerta activa, cuando el usuario la revisa, entonces visualiza el medicamento, código de lote (si aplica), cantidad afectada y motivo de la alerta.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-07.1** | Implementar consultas de evaluación de stock mínimo y clasificación de vencimientos (30, 15 y 7 días). | Reglas de negocio de alertas preventivas. |
| **ST-07.2** | Desarrollar endpoint REST consolidado de alertas activas. | API de consulta de alertas. |
| **ST-07.3** | Desarrollar panel visual de alertas diferenciando prioridad por color/estado. | Interfaz de monitoreo de alertas. |
| **ST-07.4** | Ejecutar pruebas con escenarios límite de stock y fechas de vencimiento. | Suite de pruebas de alertas. |

**Entregable de la historia:** Panel preventivo operativo de alertas de stock bajo y lotes próximos a vencer dentro del sistema.

---

## ÉPICA 03: Ventas y Comprobantes

### HU-08. Registro de ventas multiproducto con cálculo y descuento automático de stock
- **Épica:** EP-03 – Ventas y comprobantes
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-10, RF-13, RF-14, RNF-01, RNF-06, RNF-07
- **Dependencia:** HU-02, HU-05 y HU-06.

#### Historia de usuario
> Como **trabajadora de la Botica Biofarma**, quiero registrar ventas que agrupen múltiples medicamentos con sus cantidades, calculando automáticamente los importes y descontando el stock al confirmar, para agilizar la atención en mostrador y evitar errores manuales.

#### Descripción funcional
Esta historia permite gestionar el proceso central de venta en el punto de atención. La trabajadora puede buscar y agregar varios medicamentos activos al carrito de venta, indicar las cantidades solicitadas y visualizar en tiempo real el subtotal por producto y el importe total en soles. Antes de confirmar, el sistema valida que cada medicamento esté activo y cuente con stock suficiente. Al confirmar la venta, el sistema guarda la cabecera y el detalle de la operación y descuenta automáticamente las unidades vendidas del inventario dentro de una misma transacción atómica.

#### Criterios de aceptación
- **CA-01. Venta multiproducto exitosa:** Dado que se agregan uno o más medicamentos activos con stock suficiente, cuando la trabajadora confirma la venta, entonces se registra la operación con todo su detalle y se descuenta el stock correspondiente.
- **CA-02. Cálculo automático de importes:** Dado que se agregan o modifican cantidades en el carrito, cuando se actualiza el detalle, entonces cada subtotal equivale a cantidad × precio unitario y el total refleja la suma exacta en soles.
- **CA-03. Validación de stock insuficiente:** Dado que la cantidad solicitada de un producto supera su stock disponible, cuando se intenta agregar o confirmar, entonces el sistema rechaza la operación e informa la cantidad disponible.
- **CA-04. Validación de cantidades inválidas:** Dado que se ingresa una cantidad igual o menor a cero, cuando se intenta procesar, entonces el sistema solicita corregir el valor.
- **CA-05. Atomicidad ante fallos:** Dado que ocurre un error durante el registro de la venta o el descuento de inventario, cuando falla la transacción, entonces se revierten todos los cambios sin dejar ventas incompletas ni stock alterado.
- **CA-06. Agilidad de uso:** Dado que se registra una venta estándar de hasta tres productos desde la pantalla principal, cuando la trabajadora opera el sistema, entonces puede completarla en un máximo de cuatro pasos de interacción.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-08.1** | Diseñar entidades Venta y DetalleVenta con relaciones en SQL Server. | Modelo de persistencia de ventas. |
| **ST-08.2** | Implementar caso de uso transaccional de venta con cálculo y descuento de stock. | Servicio atómico de ventas. |
| **ST-08.3** | Desarrollar endpoint REST `POST /api/v1/ventas` con validaciones de negocio. | API de registro de ventas. |
| **ST-08.4** | Desarrollar interfaz de punto de venta (buscador, carrito, subtotales y total). | Pantalla principal de ventas. |
| **ST-08.5** | Ejecutar pruebas transaccionales, de cálculo y control de stock insuficiente. | Evidencias de pruebas de ventas. |

**Entregable de la historia:** Módulo de punto de venta multiproducto integrado con cálculo automático y descuento transaccional de inventario.

---

### HU-09. Generación e impresión térmica de tickets internos de venta
- **Épica:** EP-03 – Ventas y comprobantes
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-15, RF-16
- **Dependencia:** HU-08.

#### Historia de usuario
> Como **trabajadora de la Botica Biofarma**, quiero generar y visualizar un ticket interno digital de cada venta confirmada e imprimirlo en la miniimpresora térmica del establecimiento, para entregar al cliente el detalle de su compra sin duplicar operaciones.

#### Descripción funcional
Esta historia permite emitir el comprobante interno de control comercial asociado a una venta confirmada. El ticket digital muestra el encabezado del establecimiento, número correlativo de operación, fecha y hora, trabajadora responsable, listado de medicamentos vendidos con cantidades, precios unitarios, subtotales e importe total en soles. El diseño del ticket está adaptado al formato de ancho estándar (58 mm / 80 mm) compatible con la miniimpresora térmica de la botica y permite su reimpresión sin alterar inventarios ni generar nuevas ventas.

#### Criterios de aceptación
- **CA-01. Generación automática tras la venta:** Dado que existe una venta confirmada, cuando finaliza el registro, entonces el sistema genera y muestra su ticket interno digital con el detalle completo.
- **CA-02. Exactitud de datos:** Dado que se visualiza o imprime un ticket, cuando se revisa su contenido, entonces los productos, cantidades, precios unitarios y total coinciden exactamente con la venta registrada.
- **CA-03. Formato compatible con impresora térmica:** Dado que la trabajadora selecciona imprimir el ticket, cuando se envía a impresión desde el navegador, entonces el diseño se ajusta correctamente al formato de la miniimpresora térmica del establecimiento.
- **CA-04. Reimpresión sin duplicidad:** Dado que una venta ya fue registrada, cuando se solicita visualizar o reimprimir su ticket desde el historial, entonces se emite una copia sin duplicar la venta ni descontar stock nuevamente.
- **CA-05. Control de acceso:** Dado que una trabajadora intenta consultar o imprimir el ticket de una venta registrada por otra compañera, cuando realiza la solicitud, entonces el sistema deniega el acceso.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-09.1** | Implementar endpoint REST para consultar el detalle formateado del ticket de venta. | API de consulta de ticket. |
| **ST-09.2** | Diseñar vista de ticket digital con estilos CSS de impresión térmica (58/80 mm). | Plantilla imprimible de ticket. |
| **ST-09.3** | Integrar botón de impresión y reimpresión en la confirmación e historial de ventas. | Flujo de impresión en interfaz. |
| **ST-09.4** | Ejecutar pruebas de visualización, impresión física y reimpresión sin duplicidad. | Evidencias de impresión térmica. |

**Entregable de la historia:** Emisión e impresión térmica de tickets internos de venta integrada al flujo comercial.

---

### HU-10. Registro de constancia de recetas médicas retenidas en ventas
- **Épica:** EP-03 – Ventas y comprobantes
- **Prioridad:** Media
- **Requerimientos asociados:** RF-21
- **Dependencia:** HU-04 y HU-08.

#### Historia de usuario
> Como **trabajadora autorizada de la Botica Biofarma**, quiero registrar la constancia de retención de receta médica dentro de la venta cuando se dispensen medicamentos sujetos a prescripción, para mantener un control ordenado de las recetas físicas retenidas en el mostrador.

#### Descripción funcional
Esta historia incorpora el control documental de medicamentos sujetos a prescripción médica directamente en el flujo de venta. Cuando la trabajadora agrega al carrito un medicamento configurado en el catálogo con el indicador de receta retenida, el sistema muestra una advertencia visual y habilita los campos de constancia (código o número de receta y observación del documento físico retenido). Esta constancia queda vinculada al registro de la venta para su posterior revisión administrativa.

#### Criterios de aceptación
- **CA-01. Advertencia en medicamentos con receta:** Dado que se agrega a la venta un medicamento configurado como sujeto a receta retenida, cuando se incorpora al detalle, entonces el sistema muestra una advertencia visible.
- **CA-02. Registro de constancia en la venta:** Dado que se verificó y retuvo físicamente la receta médica, cuando la trabajadora ingresa los datos de la constancia y confirma la venta, entonces la información queda guardada junto con la operación.
- **CA-03. Validación de constancia obligatoria:** Dado que la venta incluye un medicamento que exige receta retenida y los datos de constancia están vacíos, cuando se intenta confirmar la venta, entonces el sistema solicita completar la constancia.
- **CA-04. Consulta administrativa:** Dado que la administradora revisa las ventas efectuadas, cuando consulta una operación con medicamento sujeto a receta, entonces visualiza la constancia registrada.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-10.1** | Agregar atributos de constancia de receta retenida en el modelo de venta. | Esquema de persistencia de constancias. |
| **ST-10.2** | Implementar validación en servidor cuando la venta incluya productos sujetos a receta. | Regla de validación de prescripción. |
| **ST-10.3** | Desarrollar advertencia y campos de registro de receta en la pantalla de venta. | Formulario de constancia en POS. |
| **ST-10.4** | Ejecutar pruebas de venta con y sin medicamentos sujetos a receta retenida. | Evidencias de pruebas funcionales. |

**Entregable de la historia:** Registro de constancia de recetas médicas retenidas integrado en el proceso de venta.

---

## ÉPICA 04: Reportes y Supervisión Administrativa

### HU-11. Trazabilidad automática de la trabajadora responsable por venta
- **Épica:** EP-04 – Reportes y supervisión administrativa
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-18
- **Dependencia:** HU-02 y HU-08.

#### Historia de usuario
> Como **administradora de la Botica Biofarma**, quiero que cada venta registre automáticamente la identidad de la trabajadora autenticada que la efectuó, para contar con trazabilidad clara de las operaciones por turno y supervisar al personal.

#### Descripción funcional
Esta historia vincula de manera obligatoria y automática cada operación de venta con el usuario que la ejecuta. Al confirmarse una venta, el servidor extrae el identificador del usuario directamente desde el token de sesión activo (JWT) y lo almacena junto con la transacción, impidiendo que el cliente envíe o suplante manualmente el identificador de otro usuario. Asimismo, la relación histórica con el responsable se conserva incluso si la cuenta de la trabajadora es desactivada posteriormente.

#### Criterios de aceptación
- **CA-01. Asignación automática del responsable:** Dado que un usuario autenticado confirma una venta, cuando se guarda la operación, entonces se registra su identificador de usuario como responsable de la venta.
- **CA-02. Prevención de suplantación:** Dado que una petición intenta enviar manualmente el identificador de otro usuario como vendedor, cuando el servidor procesa la venta, entonces ignora dicho parámetro y asigna la identidad del token autenticado.
- **CA-03. Visualización administrativa:** Dado que la administradora consulta el listado de ventas o reportes, cuando revisa las operaciones, entonces visualiza el nombre de la trabajadora responsable de cada venta.
- **CA-04. Conservación histórica:** Dado que la cuenta de una trabajadora es desactivada en el futuro, cuando la administradora consulta ventas anteriores, entonces se mantiene visible la identificación histórica de quién realizó cada operación.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-11.1** | Vincular entidad Venta con Usuario responsable en el modelo de datos. | Relación de trazabilidad en base de datos. |
| **ST-11.2** | Obtener el usuario responsable desde el contexto de seguridad (JWT) al registrar ventas. | Asignación automática en servidor. |
| **ST-11.3** | Mostrar columna y filtro de trabajadora responsable en vistas administrativas. | Interfaz de supervisión por personal. |
| **ST-11.4** | Ejecutar pruebas de asignación automática y prevención de suplantación. | Evidencias de pruebas de trazabilidad. |

**Entregable de la historia:** Trazabilidad automática e inalterable de la trabajadora responsable en cada venta registrada.

---

### HU-12. Generación de reportes diarios consolidados de ventas e inventario
- **Épica:** EP-04 – Reportes y supervisión administrativa
- **Prioridad:** Alta
- **Requerimientos asociados:** RF-17, RNF-05
- **Dependencia:** HU-06, HU-08 y HU-11.

#### Historia de usuario
> Como **administradora de la Botica Biofarma**, quiero generar un reporte diario consolidado que muestre el monto total vendido en soles, las unidades comercializadas y las existencias finales al cierre del día, para evaluar el desempeño del establecimiento al finalizar cada jornada.

#### Descripción funcional
Esta historia proporciona a la administradora una vista consolidada de los resultados operativos de una fecha específica. Al seleccionar un día calendario, el sistema calcula automáticamente a partir de las ventas confirmadas el importe total recaudado en soles, el total de unidades vendidas, el desglose de operaciones por trabajadora y el estado actual de existencias en inventario al cierre. El acceso a este módulo está restringido exclusivamente al rol `ADMINISTRADORA`.

#### Criterios de aceptación
- **CA-01. Consolidado económico y de unidades:** Dado que existen ventas confirmadas en la fecha consultada, cuando la administradora genera el reporte diario, entonces visualiza el monto total vendido en soles y el total de unidades comercializadas.
- **CA-02. Estado de existencias al cierre:** Dado que se consulta el reporte diario, cuando se muestra el resumen de inventario, entonces refleja las existencias vigentes de los medicamentos tras los movimientos del día.
- **CA-03. Filtro por fecha específica:** Dado que la administradora selecciona una fecha determinada, cuando consulta el reporte, entonces se contabilizan exclusivamente las operaciones registradas en dicha fecha.
- **CA-04. Fecha sin movimientos:** Dado que no se registraron ventas en la fecha seleccionada, cuando se consulta el reporte, entonces el sistema muestra el total en S/ 0.00 y cero unidades vendidas sin generar errores.
- **CA-05. Restricción de acceso:** Dado que una trabajadora intenta consultar el reporte diario consolidado, cuando realiza la solicitud, entonces el sistema deniega el acceso.

#### Subtareas
| Código | Subtarea | Entregable |
| :--- | :--- | :--- |
| **ST-12.1** | Implementar consultas de consolidación de ventas e inventario por fecha. | Servicio de cálculo de reporte diario. |
| **ST-12.2** | Desarrollar endpoint REST de reporte diario protegido para rol `ADMINISTRADORA`. | API de reportes diarios. |
| **ST-12.3** | Desarrollar interfaz de reporte diario con selector de fecha y tarjetas de resumen. | Pantalla administrativa de reportes. |
| **ST-12.4** | Ejecutar pruebas de cálculo de totales, días sin ventas y control de permisos. | Evidencias de pruebas de reportes. |

**Entregable de la historia:** Módulo de reportes diarios consolidados de ventas e inventario accesible exclusivamente para la administradora.
