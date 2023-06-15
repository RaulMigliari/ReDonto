package br.com.minhaempresa.redonto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import br.com.minhaempresa.redonto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragments(Home())

        binding.bottomNavigationView2.setOnItemSelectedListener {

            when(it.itemId){
                R.id.Home -> replaceFragments(Home())
                R.id.MeusChamados -> replaceFragments(Consulta())
                R.id.Perfil -> replaceFragments(PerfilF())
                R.id.ChamadosEspera -> replaceFragments(ChamadosEspera())

                else -> {

                }
            }
            true
        }

    }

    private fun replaceFragments(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()

    }

}