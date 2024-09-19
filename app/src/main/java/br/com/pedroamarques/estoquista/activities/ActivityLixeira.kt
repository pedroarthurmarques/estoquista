package br.com.pedroamarques.estoquista.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
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
import timber.log.Timber
import kotlin.collections.ArrayList


class ActivityLixeira : AppCompatActivity(), AdapterLeituras.ItemClickListener {

    private var adapter: AdapterLeituras? = null
    private var leituraDao: LeituraDao? = null

    private val startLeitura = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            carregaLixeira()
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

        val toolbar = supportActionBar
        toolbar?.setHomeButtonEnabled(true)
        toolbar?.setDisplayHomeAsUpEnabled(true)

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

        carregaLixeira()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_lixeira, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val intentClose = Intent()
                setResult(Activity.RESULT_OK, intentClose)
                finish()
            }
            R.id.btn_esvaziar -> esvaziarLixeira()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentClose = Intent()
        setResult(Activity.RESULT_OK, intentClose)
        finish()
    }

    private fun esvaziarLixeira() {
        AlertDialog.Builder(this)
            .setTitle(R.string.atencao)
            .setMessage(R.string.deseja_esvaziar_lixeira)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.confirmar) { _, _ ->
                Thread{
                    leituraDao?.deleteLixeira()
                    runOnUiThread {
                        carregaLixeira()
                    }
                }.start()
                return@setPositiveButton
            }.create().show()
    }

    private fun carregaLixeira() {
        Thread {
            val leituras = leituraDao?.getLixeira() ?: ArrayList()
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
        AlertDialog.Builder(this)
            .setTitle(R.string.atencao)
            .setMessage(R.string.confirma_exclusao_leitura)
            .setNegativeButton(R.string.cancelar) { _, _ ->
                val indexOf = adapter?.mLeituras?.indexOfFirst { it.id == leitura.id } ?: -1

                runOnUiThread {
                    if(indexOf != -1) {
                        adapter?.notifyItemChanged(indexOf)
                    }
                }
            }
            .setPositiveButton(R.string.confirmar) { _, _ ->
                Thread{
                    val indexOf = adapter?.mLeituras?.indexOfFirst { it.id == leitura.id } ?: -1
                    leituraDao?.delete(leitura)

                    runOnUiThread {
                        if (indexOf != -1) {
                            adapter?.mLeituras?.removeAt(indexOf)
                            adapter?.notifyItemRemoved(indexOf)
                        }
                    }
                }.start()
                return@setPositiveButton
            }.create().show()
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