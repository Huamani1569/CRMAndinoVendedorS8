# Sustentación Semana 8 — CRM Andino Vendedor
Jean Piero Huamani Rodriguez · Desarrollo de Aplicaciones Móviles · Docente: Guillermo Peña

**Historia de usuario:** Como vendedor, quiero iniciar sesión, consultar mis leads guardados en Room, registrar interacciones y cambiar su etapa de venta, para organizar el seguimiento comercial entre sesiones. Debo poder consultar los datos locales y conservar las operaciones autorizadas sin conexión, para sincronizarlas con la API REST cuando el servidor esté disponible.

## Requerimientos funcionales
| RF | Descripción | Dónde se ve / código |
|---|---|---|
| RF06 | Mínimo 30 leads leídos desde Room | Lista de leads · `LeadDao.obtenerTodos()` (Carlos tiene 30) |
| RF07 | Filtrar por etapa del embudo | Chips de la lista y tocar una etapa del embudo · `LeadDao.obtenerPorEtapa()` |
| RF08 | Registrar interacción (llamada, correo, reunión) | Detalle → "Registrar interacción" · `InteraccionRepository.registrar()` |
| RF09 | Cambiar etapa + recordatorio con fecha límite | Detalle → "Cambiar etapa" · `LeadRepository.cambiarEtapaConRecordatorio()` (1 transacción, 2 operaciones) |
| RF10 | No guardar lead sin empresa | `LeadViewModel.guardarLead()` y también el servidor (422 `EMPRESA_OBLIGATORIA`) |
| RF11 | Actualizar Room con datos de la API | `SyncRepository.descargar()` → `fusionarLeads/Interacciones/Tareas` |
| RF12 | Escritura + operación pendiente en transacción | `db.withTransaction { dao…; cola.registrar(…) }` en los repositorios |
| RF13 | WorkManager + botón manual | `worker/SyncWorker.kt` (red requerida, periódico 15 min) · botón "Sincronizar" |
| RF14 | Pendientes, errores y última sincronización | Panel del inicio y pantalla **Sincronización** |
| RF15 | Sin duplicados (UUID) y conflictos visibles | Cabecera `X-Operacion-UUID` · "Reenviar (prueba de duplicado)" · sección "Errores y conflictos" |

## Room (base `crm_vendedor_s8.db`)
| Tabla | Campos principales |
|---|---|
| `usuarios` | id, nombre, correo, rol, esSesion *(sin contraseña)* |
| `leads` | **id (local)**, **remoteId**, **uuid**, empresa, contacto, valorPotencial, etapa, vendedorId, vendedorNombre, **version**, **estadoSync**, mensajeError, eliminado |
| `interacciones` | id, remoteId, uuid, leadUuid, tipo, fecha, nota, version, estadoSync, mensajeError, eliminado |
| `tareas` | id, remoteId, uuid, leadUuid, leadEmpresa, descripcion, fechaLimite, completada, version, estadoSync… |
| `operaciones_pendientes` | id, uuidOperacion, entidad, idEntidadLocal, tipoOperacion, payload, fechaRegistro, estado, intentos, mensajeError (+ descripcion, codigoHttp, respuestaServidor) |
| `sync_metadata` | recurso, ultimaSincronizacionExitosa, cursorRemoto |

Estados de sincronización (separados de la etapa del negocio): **PENDIENTE → ENVIANDO → SINCRONIZADO**, o **ERROR** si el servidor lo rechaza.

## Sesión
Login contra `POST /api/auth/login`. El token JWT y el perfil se guardan en **EncryptedSharedPreferences** (`SesionLocal`). Sin conexión solo puede entrar el último usuario autenticado en ese celular (se compara un hash SHA-256 con sal; nunca se guarda la clave).
