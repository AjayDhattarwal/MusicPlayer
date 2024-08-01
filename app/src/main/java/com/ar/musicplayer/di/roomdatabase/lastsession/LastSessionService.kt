//import android.content.Intent
//import android.os.IBinder
//import android.util.Log
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.LifecycleService
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.ProcessLifecycleOwner
//import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
//import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
//import com.ar.musicplayer.models.SongResponse
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//class LastSessionService : LifecycleService(), LifecycleObserver {
//
//    @Inject
//    lateinit var lastSessionViewModel: LastSessionViewModel
//
//    private var job: Job? = null
//    private var playStartTime: Long = 0
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("LastSessionService", "Service created")
//        // Registering the service as a LifecycleObserver
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        super.onBind(intent) // Calling the super method
//        Log.d("LastSessionService", "Service bound")
//        return null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d("LastSessionService", "Service destroyed")
//        job?.cancel()
//        // Unregistering the service as a LifecycleObserver
//        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId) // Calling the super method
//        Log.d("LastSessionService", "onStartCommand called")
//        Log.d("LastSessionService", "Received song: ")
//
//        val currentSong = intent?.getSerializableExtra("CURRENT_SONG") as? SongResponse
//        Log.d("LastSessionService", "Received song: ${currentSong?.title}")
//
//        currentSong?.let {
//            playStartTime = System.currentTimeMillis()
//            job = lifecycleScope.launch(Dispatchers.IO) {
//                delay(10000)
//                val elapsedTime = System.currentTimeMillis() - playStartTime
//                val playCount = if (elapsedTime >= 10000) 1 else 0
//                val skipCount = if (playCount == 0) 1 else 0
//
//                Log.d("LastSessionService", "Elapsed time: $elapsedTime, playCount: $playCount, skipCount: $skipCount")
//                lastSessionViewModel.onEvent(
//                    LastSessionEvent.InsertLastPlayedData(
//                        songResponse = currentSong,
//                        playCount = playCount,
//                        skipCount = skipCount
//                    )
//                )
//            }
//        } ?: run {
//            Log.e("LastSessionService", "No song received")
//        }
//
//        return START_NOT_STICKY
//    }
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        Log.d("LastSessionService", "Task removed")
//        stopSelf()
//    }
//}
