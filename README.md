# CRM Andino Vendedor 💼 — Evaluación Parcial Semana 8
App del **vendedor** del caso CRM Andino Perú (Jean Piero Huamani Rodriguez).
Kotlin · Jetpack Compose · **MVVM** · **Room** · **Retrofit** · **Repository** · **WorkManager (SyncWorker)** · EncryptedSharedPreferences.

Funciona **online y offline**: lee siempre de Room, guarda cada cambio con su operación pendiente en una transacción local y la sincroniza con la API REST (`CRMAndinoAPI`) cuando hay conexión, sin duplicados y mostrando los conflictos que rechaza el servidor.

- Guía de sustentación (RF, tablas, pruebas): [`SUSTENTACION.md`](SUSTENTACION.md)
- Contexto técnico: [`CONTEXTO.md`](CONTEXTO.md)

## Ejecutar
1. Encender la API (`CRMAndinoAPI` → `npm start`).
2. Android Studio → **File → Open** → carpeta `CRMAndinoVendedor` → Gradle Sync → ▶ Run en el **emulador**.
3. Ingresar con `carlos@crmandino.pe` / `vendedor123` (servidor por defecto `http://10.0.2.2:3000/`, editable en el Login).
4. APK: **Build → Generate App Bundles or APKs → Generate APKs**.
