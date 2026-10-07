package com.crmandino.vendedor.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.crmandino.vendedor.CrmVendedorApplication
import java.util.concurrent.TimeUnit

/**
 * Proceso en SEGUNDO PLANO (WorkManager) que sincroniza la cola con la API REST mediante
 * SyncRepository, aunque la app esté cerrada. Solo se ejecuta CON RED (restricción CONNECTED):
 * si no hay internet, WorkManager espera y lo lanza solo cuando vuelve la conexión.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val contenedor = (applicationContext as CrmVendedorApplication).contenedor
        val resultado = contenedor.syncRepository.sincronizar()
        return when {
            resultado.exito -> Result.success()
            resultado.reintentar && runAttemptCount < 5 -> Result.retry() // servidor caído: reintento con espera creciente
            else -> Result.success()
        }
    }

    companion object {
        private const val TRABAJO_INMEDIATO = "crm_sync_inmediata"
        private const val TRABAJO_PERIODICO = "crm_sync_periodica"

        private val conRed = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Se llama después de cada escritura: sincroniza en cuanto haya red.
        fun sincronizarPronto(context: Context) {
            val solicitud = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(conRed)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(TRABAJO_INMEDIATO, ExistingWorkPolicy.KEEP, solicitud)
        }

        // Sincronización periódica cada 15 minutos (mínimo permitido por Android).
        fun programarPeriodica(context: Context) {
            val solicitud = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(conRed)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TRABAJO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP, solicitud)
        }
    }
}
