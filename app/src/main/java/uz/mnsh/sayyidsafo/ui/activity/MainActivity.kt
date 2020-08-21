package uz.mnsh.sayyidsafo.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.repository.AudiosRepository
import uz.mnsh.sayyidsafo.playback.PlayerAdapter
import uz.mnsh.sayyidsafo.utils.AboutUsDialog


class MainActivity : AppCompatActivity(), KodeinAware, NavigationView.OnNavigationItemSelectedListener {

    override val kodein by kodein()
    private val audiosRepository: AudiosRepository by instance<AudiosRepository>()
    private lateinit var navController: NavController
    private lateinit var navView: BottomNavigationView
    var drawerLayout: DrawerLayout? = null

    companion object {
        var playerAdapter: PlayerAdapter? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        drawerLayout = findViewById(R.id.drawer_layout)
        findViewById<NavigationView>(R.id.navigation_menu).setNavigationItemSelectedListener(this)

        audiosRepository.fetchAudios()
        requestPermissions()

        val bottomDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_player, findViewById(R.id.bottomSheetContainer))
        bottomDialog.setContentView(bottomSheetView)

        layoutPlayer.setOnClickListener {
            bottomDialog.show()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        App.DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        App.DIR_PATH += "/SayyidSafo/"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.telegram_chanel -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.telegram_url))
                startActivity(intent)
            }
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var message = getString(R.string.about_us_text)
                message =
                    message + "\n" + getString(R.string.app_url) + "\n\n"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, "Улашиш"))
            }
            R.id.about -> {
                AboutUsDialog().show(supportFragmentManager, "ABOUT_US")
            }
            R.id.other_apps -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.our_app))
                startActivity(intent)
            }
        }

        return true
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.telegram_chanel -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.telegram_url))
                startActivity(intent)
            }
            R.id.share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var message = getString(R.string.about_us_text)
                message =
                    message + "\n" + getString(R.string.app_url) + "\n\n"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, "Улашиш"))
            }
            R.id.about -> {
                AboutUsDialog().show(supportFragmentManager, "ABOUT_US")
            }
            R.id.other_apps -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.our_app))
                startActivity(intent)
            }
        }
        return true
    }
}