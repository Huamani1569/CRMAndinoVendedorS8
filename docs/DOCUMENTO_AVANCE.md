# Avance Semana 5 — CRM Andino Perú
**Estudiante:** Jean Piero Huamani Rodriguez · **Docente:** Guillermo Peña · Desarrollo de Aplicaciones Móviles

## 1. Nombre, sector y problema
- **Aplicación:** CRM Andino Perú
- **Sector:** Software empresarial · **Ciudad:** Lima
- **Problema:** muchas empresas peruanas pequeñas y medianas registran a sus clientes potenciales en hojas de Excel, cuadernos o chats de WhatsApp. Por eso pierden oportunidades de venta, olvidan hacer seguimiento y el supervisor no sabe en qué etapa está cada negociación ni cómo rinde cada vendedor.
- **Solución:** una app que organiza los leads por etapa del embudo de ventas, registra las interacciones y recuerda las tareas de seguimiento, con un panel para el supervisor.

## 2. Usuarios (roles)
| Rol | Quién es | Qué hace al entrar |
|---|---|---|
| **Vendedor** (usuario principal) | Ejecutivo comercial de campo | Ve sus leads, su valor en embudo y sus tareas de seguimiento |
| **Supervisor comercial** (administrador) | Jefe del equipo de ventas | Ve los indicadores del equipo, el embudo y el rendimiento de cada vendedor |

**Público objetivo:** equipos comerciales de pymes (de 3 a 20 vendedores) que venden a otras empresas.

## 3. Identidad visual
- **Logo:** dos montañas andinas (la región) y una flecha dorada que sube (crecimiento de las ventas). Archivo: `docs/logo_crm_andino.png`.

| Color | Hex | Uso |
|---|---|---|
| Azul andino | `#1F3A5F` | Principal: barras superiores, botones, Splash |
| Turquesa lago | `#1B998B` | Secundario: montos y progreso |
| Dorado inca | `#F2A93B` | Acento: tareas, metas, flecha del logo |
| Fondo claro | `#F5F7FA` | Fondo de pantallas |

Cada etapa del embudo tiene su color (gris, azul, morado, naranja y verde) y siempre va con su texto, para no depender solo del color.

## 4. Flujo de navegación
```
Ícono de la app → Splash Screen (logo animado + carga) → Login (credenciales simuladas)
   → Validación → Vendedor:   Inicio del vendedor
                → Supervisor: Panel comercial
```
**Cuentas de prueba:** `carlos@crmandino.pe` / `vendedor123` · `supervisor@crmandino.pe` / `super123`

## 5. Pantallas implementadas en Jetpack Compose
1. **SplashScreen:** logo animado, nombre, eslogan e indicador de carga.
2. **LoginScreen:** correo y contraseña con validaciones, mensaje de error, carga y botones de cuenta de prueba.
3. **HomeVendedorScreen:** indicadores, tareas de seguimiento, leads abiertos de mayor valor y accesos rápidos.
4. **DashboardSupervisorScreen:** indicadores del equipo, leads por etapa y rendimiento de cada vendedor frente a su meta.

## 6. Arquitectura MVVM
```
com.crmandino.peru/
├── data/
│   ├── fake/FakeData.kt           → 30 leads, vendedores, tareas, interacciones
│   ├── repository/                → AuthRepository, LeadRepository, TareaRepository, InteraccionRepository
│   ├── local/ (AppDatabase, dao)  → Semana 6: Room / SQLite
│   └── remote/ApiService.kt       → Etapa final: Web Service
├── model/                         → Lead, Vendedor, Usuario, RolUsuario, EtapaVenta, Tarea, Interaccion
├── state/                         → AuthUiState, LeadUiState, DashboardUiState
├── viewmodel/                     → AuthViewModel, LeadViewModel, DashboardCrmViewModel
├── ui/
│   ├── navigation/AppNavigation.kt
│   ├── screens/                   → Splash, Login, HomeVendedor, DashboardSupervisor
│   ├── components/                → AppLogo, InputField, LeadCard, EtapaChip, ResumenCard...
│   └── theme/                     → colores, tipografía y tema
└── MainActivity.kt                → solo punto de entrada
```
**Flujo de datos:** el usuario pulsa "Ingresar" → `LoginScreen` avisa a `AuthViewModel` → el ViewModel valida los campos → pide a `AuthRepository` verificar las credenciales → actualiza `AuthUiState` → Compose redibuja y navega al inicio según el rol.

## 7. Historia de usuario principal y requerimientos funcionales
**HU principal:** *Como vendedor, quiero consultar mis clientes potenciales, registrar interacciones y cambiar su etapa de venta, para organizar el seguimiento comercial.*

**Criterios de aceptación (avance actual):** al iniciar sesión como vendedor veo cuántos leads tengo, el valor total en negociación, mis tareas pendientes con fecha límite y mis leads abiertos de mayor valor.

**Requerimientos funcionales iniciales:**
- **RF01.** Mostrar Splash Screen con el logo y luego el Login. ✅
- **RF02.** Iniciar sesión validando correo y contraseña, y dirigir a cada usuario a su inicio según el rol (vendedor o supervisor). ✅
- **RF03.** Mostrar al vendedor el resumen de sus leads y sus tareas de seguimiento pendientes. ✅
- **RF04.** Mostrar al supervisor los indicadores del equipo y la cantidad de leads por etapa del embudo. ✅
- **RF05.** Listar como mínimo 30 leads y filtrarlos por etapa del embudo y por vendedor. *(próximo avance)*
- **RF06.** Ver el detalle de un cliente, registrar llamadas, correos y reuniones, y cambiar su etapa. *(próximo avance)*

## 8. Datos masivos
- **Entidad principal:** `Lead` (empresa, contacto, cargo, teléfono, correo, sector, distrito, etapa, valor potencial y vendedor asignado).
- **Cantidad inicial:** 30 leads simulados, repartidos entre 4 vendedores y las 5 etapas del embudo (8 Prospecto, 7 Contactado, 5 Propuesta, 5 Negociación, 5 Cerrado), además de 14 tareas y 10 interacciones.
- **Evolución:** listas en Kotlin (`FakeData`) → **Room/SQLite** (Semana 6) → **Web Service/API** con base de datos remota (etapa final). Como las pantallas solo hablan con los Repositories, al cambiar la fuente de datos no hay que tocar la interfaz.
- Todos los datos son **ficticios**.

---

## Guion de exposición (unos 2 minutos)
1. Mi aplicación se llama **CRM Andino Perú** y pertenece al sector **software empresarial**, en Lima.
2. El usuario principal es el **vendedor** y el usuario administrativo es el **supervisor comercial**.
3. El problema que resuelve es que las pymes pierden ventas por llevar el seguimiento en Excel o WhatsApp, y el supervisor no ve el avance del equipo.
4. Al abrir la app se muestra el ícono (montañas y una flecha de crecimiento), luego la Splash Screen y finalmente el Login.
5. Después del Login, el vendedor va a su **inicio con sus leads y tareas**, y el supervisor va al **panel comercial** con los indicadores del equipo. *(Demostrar entrando con las dos cuentas de prueba.)*
6. Mi entidad principal es el **Lead** y trabajo inicialmente con **30 registros simulados**.
7. Mi arquitectura tiene las capas **ui, viewmodel, model y data**. Por ejemplo, el Login no valida nada por sí mismo: se lo pide al `AuthViewModel`, que consulta al `AuthRepository`.
8. Mi funcionalidad principal será el **seguimiento del embudo de ventas**: listar y filtrar leads, registrar interacciones, cambiar de etapa y crear recordatorios.
9. Primero trabajo con datos simulados, luego implementaré **Room/SQLite** y finalmente un **Web Service o API**.

---

## Explicación sencilla de cada archivo (para sustentar)
**Punto de entrada**
- `MainActivity.kt`: aplica el tema y llama a `AppNavigation()`. No tiene lógica.

**model/** (qué datos existen)
- `Lead.kt`: un cliente potencial. Es la entidad principal.
- `EtapaVenta.kt`: las 5 etapas del embudo, en orden.
- `Usuario.kt` / `RolUsuario.kt`: quién inicia sesión y si es vendedor o supervisor.
- `Vendedor.kt`: miembro del equipo, con su zona y meta mensual.
- `Tarea.kt`: recordatorio de seguimiento con fecha límite.
- `Interaccion.kt`: llamada, correo o reunión con un lead.

**data/** (de dónde vienen los datos)
- `fake/FakeData.kt`: todos los datos simulados (30 leads, usuarios, tareas...).
- `repository/AuthRepository.kt`: busca el usuario por correo y contraseña. Espera 0,9 s para simular un servidor.
- `repository/LeadRepository.kt`: devuelve todos los leads o solo los de un vendedor.
- `repository/TareaRepository.kt`: devuelve las tareas pendientes.
- `repository/InteraccionRepository.kt`: historial por lead (se usará en el detalle).
- `local/` y `remote/`: carpetas preparadas para Room y la API.

**state/** (qué debe mostrar cada pantalla)
- `AuthUiState.kt`: campos del Login, errores, si está cargando y el usuario autenticado.
- `LeadUiState.kt`: datos del inicio del vendedor.
- `DashboardUiState.kt`: indicadores del supervisor.

**viewmodel/** (la lógica)
- `AuthViewModel.kt`: valida el correo (formato) y la contraseña (mínimo 6), llama al repositorio y guarda el usuario o el error.
- `LeadViewModel.kt`: calcula los leads del vendedor, el valor en embudo, las tareas y el top 5.
- `DashboardCrmViewModel.kt`: calcula el total de leads, el valor del embudo, las ventas cerradas, la conversión, los leads por etapa y el rendimiento de cada vendedor.

**ui/** (lo que se ve)
- `navigation/AppNavigation.kt`: decide qué pantalla mostrar (Splash, Login o Inicio) y, en Inicio, cuál según el rol.
- `screens/SplashScreen.kt`: animación del logo y espera breve.
- `screens/LoginScreen.kt`: formulario. Observa el `AuthUiState` y navega cuando hay usuario.
- `screens/HomeVendedorScreen.kt` / `DashboardSupervisorScreen.kt`: inicios por rol.
- `components/AppLogo.kt`: el logo dibujado con Canvas (círculo, dos triángulos y una línea).
- `components/InputField.kt`, `LeadCard`, `EtapaChip`, `ResumenCard`, `AccesoRapido`, `EstadosPantalla`: piezas reutilizables.
- `theme/`: paleta de colores, tipografía y tema.

**Conceptos clave que te pueden preguntar**
- **StateFlow:** una "caja" con el estado actual. El ViewModel la cambia y la pantalla se redibuja sola con `collectAsState()`.
- **viewModelScope.launch:** ejecuta tareas que tardan, como el login simulado, sin congelar la app.
- **¿Por qué MVVM?** Separa la interfaz de la lógica y de los datos. Así se puede cambiar FakeData por Room sin tocar las pantallas.
