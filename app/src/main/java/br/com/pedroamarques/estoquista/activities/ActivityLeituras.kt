package br.com.pedroamarques.estoquista.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.pedroamarques.estoquista.R
import br.com.pedroamarques.estoquista.adapters.AdapterLeituras
import br.com.pedroamarques.estoquista.dao.LeituraDao
import br.com.pedroamarques.estoquista.databinding.ActivityLeiturasBinding
import br.com.pedroamarques.estoquista.entities.*
import br.com.pedroamarques.estoquista.factory.AppDatabase
import br.com.pedroamarques.estoquista.helper.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import java.util.Random
import kotlin.collections.ArrayList


class ActivityLeituras : AppCompatActivity(), AdapterLeituras.ItemClickListener {

    // códigos de permissão devem ser menores que < 256
    private val RC_HANDLE_CAMERA_PERM = 2

    private var adapter: AdapterLeituras? = null
    private var leituraDao: LeituraDao? = null

    private val startLeitura = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            carregaLeituras()
        }
    }

    private lateinit var binding: ActivityLeiturasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeiturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e.localizedMessage)
        }

        val appDatabase = AppDatabase.getDatabase(applicationContext)
        leituraDao = appDatabase.leituraDao()

        adapter = AdapterLeituras(this)
        binding.recyclerViewItensLeitura.setHasFixedSize(true)
        binding.recyclerViewItensLeitura.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewItensLeitura.adapter = adapter

        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                validaLista()
            }
            override fun onChanged() {
                super.onChanged()
                validaLista()
            }
        })

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position: Int = viewHolder.adapterPosition
                excluiLeitura(adapter?.mLeituras?.get(position) ?: return)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewItensLeitura)

        carregaLeituras()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_leituras, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val intentClose = Intent()
                setResult(Activity.RESULT_OK, intentClose)
                finish()
            }
            R.id.btn_add -> novaLeitura()
            R.id.btn_delete -> startLeitura.launch(Intent(this, ActivityLixeira::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun novaLeitura() {
        startLeitura.launch(Intent(this, LeituraActivity::class.java))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentClose = Intent()
        setResult(Activity.RESULT_OK, intentClose)
        finish()
    }


    private fun carregaLeituras() {
        Thread {
            val leituras = leituraDao?.getAll() ?: ArrayList()
            runOnUiThread {
                atualizaLista(leituras as ArrayList<Leitura>)
            }
        }.start()
    }

    private fun atualizaLista(itens: ArrayList<Leitura>) {
        adapter?.atualizaLista(itens)
        validaLista()
    }

    private fun excluiLeitura(leitura: Leitura) {
        Thread{
            val indexOf = adapter?.mLeituras?.indexOfFirst { it.id == leitura.id } ?: -1
            leitura.status = "E"
            leitura.dataAlteracao = Calendar.getInstance()

            leituraDao?.update(leitura)

            runOnUiThread {
                if (indexOf != -1) {
                    adapter?.mLeituras?.removeAt(indexOf)
                    adapter?.notifyItemRemoved(indexOf)
                }
            }
        }.start()
    }

    private fun validaLista() {
        if (adapter?.itemCount == 0) {
            binding.recyclerViewItensLeitura.visibility = View.GONE
            binding.imageViewNoData.visibility = View.VISIBLE
        } else {
            binding.recyclerViewItensLeitura.visibility = View.VISIBLE
            binding.imageViewNoData.visibility = View.GONE
        }
    }

    override fun clickItem(item: Leitura, position: Int) {
        val intent = Intent(this, LeituraActivity::class.java)
        intent.putExtra("id_leitura", item.id)
        startLeitura.launch(intent)
    }
}