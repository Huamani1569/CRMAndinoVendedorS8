# CONTEXTO — CRM Andino (Semana 8, evaluación parcial)
**Estudiante:** Jean Piero Huamani Rodriguez · **Curso:** Desarrollo de Aplicaciones Móviles · **Docente:** Guillermo Peña · **Caso 7:** CRM Andino Perú.

## Arquitectura
`Compose (ui/) → ViewModel (viewmodel/) → Repository (data/repository/) → Room (data/local/) + Retrofit (data/remote/) → API REST (CRMAndinoAPI, Node/Express) → PostgreSQL en Neon`.
La cola local (`operaciones_pendientes`) la procesa `SyncRepository`, que llama el `worker/SyncWorker` (WorkManager) y el botón "Sincronizar".

## Decisiones técnicas
- Gradle igual que la Semana 6: AGP 9.1.1, Kotlin 2.2.10 integrado, KSP 2.2.10-2.0.2, Room 2.8.4, compileSdk 37, minSdk 24. Nuevas dependencias: Retrofit 2.11.0 + Gson, OkHttp 4.12.0, WorkManager 2.10.0, security-crypto 1.1.0-alpha06.
- Base Room nueva (`crm_*_s8.db`) para no chocar con la de la Semana 6.
- Inyección manual en `di/AppContainer.kt`, creado en la clase `Application`.
- La dirección de la API se configura en el Login (por defecto `http://10.0.2.2:3000/`, el localhost de la laptop visto desde el emulador). `usesCleartextTraffic=true` porque la API local usa HTTP.
- Cada escritura: `db.withTransaction { dato + operación pendiente }` → `SyncWorker.sincronizarPronto()` (KEEP, red requerida).
- Envío FIFO; el id remoto y la versión se leen al momento de enviar. Error de red/5xx → la operación sigue PENDIENTE (reintento). 4xx → ERROR con el motivo (el usuario descarta o reintenta).
- Descarga: no sobrescribe registros con cambios PENDIENTES/ERROR; borra los que el servidor ya no devuelve (reasignados o eliminados).
- Conflictos: `version` en cada registro; el servidor responde 409 `CONFLICTO_VERSION`, `REASIGNADO` o `ELIMINADO`.
