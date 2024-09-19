package br.com.pedroamarques.estoquista.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputType
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.pedroamarques.estoquista.R
import br.com.pedroamarques.estoquista.adapters.AdapterItensLeitura
import br.com.pedroamarques.estoquista.dao.ItemLeituraDao
import br.com.pedroamarques.estoquista.dao.LeituraDao
import br.com.pedroamarques.estoquista.dao.LoteDao
import br.com.pedroamarques.estoquista.databinding.ActivityLeituraBinding
import br.com.pedroamarques.estoquista.databinding.FragmentEdittextBinding
import br.com.pedroamarques.estoquista.databinding.FragmentListaExportacoesBinding
import br.com.pedroamarques.estoquista.databinding.FragmentModalEdicaoItemLeituraBinding
import br.com.pedroamarques.estoquista.entities.*
import br.com.pedroamarques.estoquista.factory.AppDatabase
import br.com.pedroamarques.estoquista.factory.ColetaExport
import br.com.pedroamarques.estoquista.factory.GlideApp
import br.com.pedroamarques.estoquista.fragments.BottomSheetLotes
import br.com.pedroamarques.estoquista.helper.*
import br.com.pedroamarques.estoquista.helper.Const.Leitor.BarcodeType.*
import br.com.pedroamarques.estoquista.helper.Const.Leitor.ModoLeitura.*
import br.com.pedroamarques.estoquista.helper.Const.Leitor.TiposLeitura.*
import br.com.pedroamarques.estoquista.helper.Const.Leitor.FORMATOS
import br.com.pedroamarques.estoquista.helper.Const.Preference.APP_PREFERENCE
import br.com.pedroamarques.estoquista.helper.Const.Preference.BARCODE_PADRAO
import br.com.pedroamarques.estoquista.helper.Const.Preference.LEITURA_PADRAO
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class LeituraActivity : AppCompatActivity(), AdapterItensLeitura.ItemClickListener, BottomSheetLotes.BottomSheetLotesDelegate {

    // códigos de permissão devem ser menores que < 256
    private val RC_HANDLE_CAMERA_PERM = 2
    private val RESULT_BUSCA = 4

    private val MIN_TIME_BARCODE_CONTINUS: Long = 1000 // milisegundos
    private val locale = Locale("pt", "BR")
    private var itemParent: ItemLeitura? = null
    private var barcode: String? = null
    private var leitura: Leitura? = null
    private var itensLeitura: ArrayList<ItemLeitura> = ArrayList()
    private var adapter: AdapterItensLeitura? = null
    private var modoLeitura: Const.Leitor.ModoLeitura? = null
    private var modoItem: ModoItem = ModoItem.Normal

    private var btnFinalizar: MenuItem? = null
    private var btnExportar: MenuItem? = null
    private var btnExcluir: MenuItem? = null
    private var btnAbrir: MenuItem? = null
    private var btnLeituraLote: MenuItem? = null
    private var btnLeituraSerial: MenuItem? = null

    private var usuarioRealizouLogin = false
    private var salvandoCodigoDeBarras = false
    private var barcodeScannerInicializado = false

    public enum class ModoItem {
        Normal, Serial, Lote
    }

    // dados
    private var tipoLeitura: Const.Leitor.TiposLeitura? = null
    private var itemLeituraDao: ItemLeituraDao? = null
    private var leituraDao: LeituraDao? = null
    private var loteDao: LoteDao? = null

    /*
    Callback do leitor de códito de barras, é executado todas
    as vezes que um código é encontrado
     */
    private val barcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            barcode = if (result.text == null || result.text == barcode) {
                // previne scans duplicados antes da contagem ser salva
                return

            } else if(Manual == tipoLeitura || Pistola == tipoLeitura){
                result.text

            } else if(validaCodigoDeBarras(result.text!!) && Escaner == tipoLeitura) {
                result.text

            }  else {
                return
            }

            adicionaContagem(barcode ?: "")
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }
    
    private lateinit var binding: ActivityLeituraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }
        
        binding = ActivityLeituraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.containerActivityLeitura.visibility = View.GONE

        val appDatabase = AppDatabase.getDatabase(applicationContext)
        loteDao = appDatabase.loteDao()
        leituraDao = appDatabase.leituraDao()
        itemLeituraDao = appDatabase.itemLeituraDao()

        adapter = AdapterItensLeitura(this)
        adapter?.itemClickListener = this

        val config = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)

        val toolbar = supportActionBar
        toolbar?.setHomeButtonEnabled(true)
        toolbar?.setDisplayHomeAsUpEnabled(true)

        binding.barcodeScanner.setStatusText(getString(R.string.instrucao_barcode))
        binding.barcodeScanner.statusView.gravity = Gravity.CENTER
        binding.barcodeScanner.statusView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        binding.recyclerViewItensLeitura.setHasFixedSize(true)
        binding.recyclerViewItensLeitura.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewItensLeitura.adapter = adapter

        binding.btnInputText.setOnClickListener {
            val prefEditor = config.edit()
            prefEditor.putInt(LEITURA_PADRAO, Manual.ordinal)
            prefEditor.apply()

            iniciaLeituraManual()
        }
        binding.btnBarcodeScanner.setOnClickListener {
            val prefEditor = config.edit()
            prefEditor.putInt(LEITURA_PADRAO, Escaner.ordinal)
            prefEditor.apply()

            iniciaLeituraBarcodeScanner()
        }
        binding.btnBarcodePistola.setOnClickListener {
            val prefEditor = config.edit()
            prefEditor.putInt(LEITURA_PADRAO, Pistola.ordinal)
            prefEditor.apply()

            iniciaLeituraBarcodePistola()
        }

        binding.textViewFormatScanner.setOnClickListener{
            val config = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
            var idxSelect = Const.Leitor.FORMATOS.indexOfFirst { it.tipo.ordinal == config.getInt(BARCODE_PADRAO, 0) }
            if (idxSelect == -1) {
                idxSelect = 0
            }

            val nomes = Const.Leitor.FORMATOS.map { it.nome }.toTypedArray()

            val mBuilder = AlertDialog.Builder(this@LeituraActivity)
            mBuilder.setTitle(getString(R.string.selecione_um_formato_de_codigo_de_barras))
            mBuilder.setSingleChoiceItems(nomes, config.getInt(BARCODE_PADRAO, 0)) { dialog, which -> idxSelect = which }

            mBuilder.setPositiveButton(getString(R.string.selecionar)) { dialog, _ ->
                val prefEditor = config.edit()
                prefEditor.putInt(BARCODE_PADRAO, idxSelect)
                prefEditor.commit()

                binding.textViewFormatScanner.text = FORMATOS[idxSelect].nome
                barcodeScannerInicializado = false

                iniciaLeituraBarcodeScanner()
                dialog.dismiss()
            }

            mBuilder.setNegativeButton(getString(R.string.cancelar)) { dialog, which ->
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()
        }

        binding.editTextCodigoDeBarras.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(textView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if(event == null || textView == null) {
                    return false
                }

                if(event.action == KeyEvent.ACTION_DOWN && (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) ) {
                    val newBarcode: String = textView.text?.toString() ?: ""

                    if(newBarcode.isEmpty()) {
                        textView.text = ""
                        ajustaFoco()
                        return true
                    }
                    this@LeituraActivity.barcode = newBarcode

                    textView.text = ""
                    ajustaFoco()
                    adicionaContagem(newBarcode)

                    return true
                } else {
                    return false
                }
            }
        })
        binding.editTextCodigoDeBarras.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && tipoLeitura != Manual) {
                hideKeyboard()
            }
        }
        binding.editTextCodigoDeBarras.nextFocusDownId = binding.editTextCodigoDeBarras.id

        binding.editTextPistola.inputType = InputType.TYPE_NULL
        binding.editTextPistola.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(textView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if(event == null || textView == null) {
                    return false
                }

                if(event.action == KeyEvent.ACTION_DOWN && (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) ) {
                    val newBarcode: String = textView.text?.toString() ?: ""
                    textView.text = ""

                    if(newBarcode.isEmpty()) {
                        return true
                    }
                    this@LeituraActivity.barcode = newBarcode
                    adicionaContagem(newBarcode)

                    return true

                } else {
                    return false
                }
            }
        })
        binding.editTextPistola.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                hideKeyboard()
            }
        }
        binding.editTextPistola.nextFocusDownId = binding.editTextPistola.id

        binding.btnAdicionarCodigoDeBarras.setOnClickListener {
            val newBarcode: String = binding.editTextCodigoDeBarras.text?.toString() ?: ""

            if(newBarcode.isEmpty()) {
                return@setOnClickListener
            }
            this.barcode = newBarcode

            binding.editTextCodigoDeBarras.text = null
            ajustaFoco()
            adicionaContagem(newBarcode)
        }

        binding.textViewUltimoItem.setOnClickListener {
            if (binding.layoutItemUltimoItem.visibility == View.VISIBLE) {
                binding.layoutItemUltimoItem.visibility = View.GONE
                binding.textViewUltimoItem.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_left, 0)
            } else {
                binding.layoutItemUltimoItem.visibility = View.VISIBLE
                binding.textViewUltimoItem.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
            }
        }

        val subtitle: String
        modoLeitura = if (config.getString("MODO_LEITURA", "SEQUENCIAL") == "SEQUENCIAL") {
            subtitle = getString(R.string.modo_sequencial)
            Sequencial
        } else {
            subtitle = getString(R.string.modo_contagem)
            Contagem
        }

        toolbar?.subtitle = subtitle

        binding.sairModoSerial.setOnClickListener {
            modoItem(ModoItem.Normal)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.leitura, menu)

        btnFinalizar = menu.findItem(R.id.btn_finalizar)
        btnExportar = menu.findItem(R.id.btn_exportar)
        btnExcluir = menu.findItem(R.id.btn_excluir)
        btnAbrir = menu.findItem(R.id.btn_abrir)
        btnLeituraLote = menu.findItem(R.id.btn_leitura_lote)
        btnLeituraSerial = menu.findItem(R.id.btn_leitura_serial)

        // necessário fazer isso aqui
        // pois caso contrário o menu não carregou ainda
        val idLeitura = intent?.extras?.getLong("id_leitura", 0L) ?: 0L

        if(idLeitura > 0) {
            carregaLeitura(idLeitura)

        } else {
            iniciaLeituraPadrao()
        }

        binding.containerActivityLeitura.visibility = View.VISIBLE
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val intentClose = Intent()
                intentClose.putExtra("carregarUsuario", usuarioRealizouLogin)

                setResult(Activity.RESULT_OK, intentClose)
                finish()
            }
            R.id.btn_finalizar -> finalizaLeitura()
            R.id.btn_exportar -> exportaLeitura()
            R.id.btn_excluir -> excluirLeitura()
            R.id.btn_modo_leitura -> abreEdicaoLeitura()
            R.id.btn_abrir -> abreLeitura()
            R.id.btn_leitura_lote -> iniciaLeituraLoteFixo()
            R.id.btn_leitura_serial -> iniciaLeituraSerialFixo()
        }
        return super.onOptionsItemSelected(item)
    }

    // Reinicia a câmera se necessário
    override fun onResume() {
        super.onResume()

        if(tipoLeitura == Escaner) {
            binding.barcodeScanner.resume()
        }
    }

    // Para a câmera
    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentClose = Intent()
        intentClose.putExtra("carregarUsuario", usuarioRealizouLogin)

        setResult(Activity.RESULT_OK, intentClose)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return binding.barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Timber.e("Resultado da solicitação de permissão inesperado: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.e("Uso da câmera permitido - inicializando fonte da câmera")
            // temos permissão
            iniciaLeituraBarcodeScanner()
            return
        }

        val listener = DialogInterface.OnClickListener { _, _ -> iniciaLeituraManual() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.atencao)
            .setMessage(R.string.no_camera_permission)
            .setPositiveButton(R.string.entendido, listener)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

       if(requestCode == RESULT_BUSCA && resultCode == Activity.RESULT_OK) {
            val ean = data?.getStringExtra("ean")

            if (!TextUtils.isEmpty(ean)) {
                adicionaContagem(ean!!)
            }
        }
    }

    private fun solicitaPermissaoCamera() {
        Timber.w("Permissão da câmera não foi concedida. Solicitando permissão")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)

        } else {
            Toast.makeText(applicationContext, R.string.no_camera_permission, Toast.LENGTH_SHORT).show()
        }
    }

    /*
    Inicia a leitura de código de barras usando a câmera do dispositivo
     */
    private fun iniciaLeituraBarcodeScanner() {
        hideKeyboard()

        // alterações visuais no botão
        binding.btnInputText.setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.transparente)))
        binding.btnBarcodePistola.setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.transparente)))
        binding.btnBarcodeScanner.setBackground(AppCompatResources.getDrawable(this, R.drawable.primary_to_accent_gradial))

        // verifica permissão de câmera
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {

            if(binding.barcodeScanner.visibility == View.GONE) {
                binding.barcodeScanner.visibility = View.VISIBLE
                binding.containerPistola.visibility = View.GONE
                binding.containerInputText.visibility = View.GONE

                val set = ConstraintSet()
                set.clone(binding.containerActivityLeitura)

                set.connect(R.id.bgBotoes, ConstraintSet.TOP, R.id.barcodeScanner, ConstraintSet.BOTTOM)
                set.connect(R.id.btnBarcodePistola, ConstraintSet.TOP, R.id.barcodeScanner, ConstraintSet.BOTTOM)
                set.connect(R.id.btnBarcodeScanner, ConstraintSet.TOP, R.id.barcodeScanner, ConstraintSet.BOTTOM)
                set.connect(R.id.btnInputText, ConstraintSet.TOP, R.id.barcodeScanner, ConstraintSet.BOTTOM)
                set.connect(R.id.textViewQtdItensLeitura, ConstraintSet.TOP, R.id.barcodeScanner, ConstraintSet.BOTTOM)

                set.applyTo(binding.containerActivityLeitura)
            }

            // deixa marcado opção atual
            tipoLeitura = Escaner

            if(barcodeScannerInicializado) {
                binding.barcodeScanner.resume()

            } else {
                val config = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
                val prefEditor = config.edit()
                val integrator  = IntentIntegrator(this)

                val formato = FORMATOS.firstOrNull { it.tipo.ordinal == config.getInt(BARCODE_PADRAO, 0) } ?: FORMATOS.first()
                prefEditor.putInt(BARCODE_PADRAO, formato.tipo.ordinal)
                prefEditor.apply()

                binding.textViewFormatScanner.text = formato.nome

                when (formato.tipo) {
                    Const.Leitor.BarcodeType.Ean13 -> integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13)
                    Const.Leitor.BarcodeType.Ean8 -> integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_8)
                    Const.Leitor.BarcodeType.UpcA -> integrator.setDesiredBarcodeFormats(IntentIntegrator.UPC_A)
                    Const.Leitor.BarcodeType.UpcE -> integrator.setDesiredBarcodeFormats(IntentIntegrator.UPC_E)
                    Const.Leitor.BarcodeType.Code128 -> integrator.setDesiredBarcodeFormats(IntentIntegrator.CODE_128)
                    Const.Leitor.BarcodeType.Code39 -> integrator.setDesiredBarcodeFormats(IntentIntegrator.CODE_39)
                    Const.Leitor.BarcodeType.Code93 -> integrator.setDesiredBarcodeFormats(IntentIntegrator.CODE_93)
                    Const.Leitor.BarcodeType.Itf -> integrator.setDesiredBarcodeFormats(IntentIntegrator.ITF)
                    Const.Leitor.BarcodeType.Rss14 -> integrator.setDesiredBarcodeFormats(IntentIntegrator.RSS_14)
                    Const.Leitor.BarcodeType.RssExpended -> integrator.setDesiredBarcodeFormats(IntentIntegrator.RSS_EXPANDED)
                }

                val intentCode = integrator.createScanIntent()

                binding.barcodeScanner.initializeFromIntent(intentCode)
                binding.barcodeScanner.decodeContinuous(barcodeCallback)
                binding.barcodeScanner.resume()

                barcodeScannerInicializado = true
            }

        } else {
            solicitaPermissaoCamera()
        }

        // remove foco
        ajustaFoco()
    }

    /*
    Inicia a leitura de modo manual onde o usuário informa o EAN
    em um campo texto
     */
    private fun iniciaLeituraManual() {
        // alterações visuais no botão
        binding.btnInputText.setBackground(AppCompatResources.getDrawable(this, R.drawable.primary_to_accent_gradial))
        binding.btnBarcodePistola.setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.transparente)))
        binding.btnBarcodeScanner.setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.transparente)))
        binding.barcodeScanner.pause()

        if(binding.containerInputText.visibility == View.GONE) {
            binding.containerInputText.visibility = View.VISIBLE
            binding.barcodeScanner.visibility = View.GONE
            binding.containerPistola.visibility = View.GONE

            val set = ConstraintSet()
            set.clone(binding.containerActivityLeitura)

            set.connect(R.id.bgBotoes, ConstraintSet.TOP, R.id.containerInputText, ConstraintSet.BOTTOM)
            set.connect(R.id.btnBarcodePistola, ConstraintSet.TOP, R.id.containerInputText, ConstraintSet.BOTTOM)
            set.connect(R.id.btnBarcodeScanner, ConstraintSet.TOP, R.id.containerInputText, ConstraintSet.BOTTOM)
            set.connect(R.id.btnInputText, ConstraintSet.TOP, R.id.containerInputText, ConstraintSet.BOTTOM)
            set.connect(R.id.textViewQtdItensLeitura, ConstraintSet.TOP, R.id.containerInputText, ConstraintSet.BOTTOM)

            set.applyTo(binding.containerActivityLeitura)
        }

        // deixa marcado opção atual
        tipoLeitura = Manual
        ajustaFoco()
    }

    /*
    Inicia a leitura do código de barras através de um leitor externo
    conectado via USB
     */
    private fun iniciaLeituraBarcodePistola() {
        hideKeyboard()

        // alterações visuais no botão
        binding.btnInputText.setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.transparente)))
        binding.btnBarcodePistola.setBackground(AppCompatResources.getDrawable(this, R.drawable.primary_to_accent_gradial))
        binding.btnBarcodeScanner.setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.transparente)))

        binding.editTextPistola.setText("")

        binding.barcodeScanner.pause()

        if(binding.containerPistola.visibility == View.GONE) {
            binding.containerPistola.visibility = View.VISIBLE
            binding.containerInputText.visibility = View.GONE
            binding.barcodeScanner.visibility = View.GONE

            val set = ConstraintSet()
            set.clone(binding.containerActivityLeitura)

            set.connect(R.id.bgBotoes, ConstraintSet.TOP, R.id.containerPistola, ConstraintSet.BOTTOM)
            set.connect(R.id.btnBarcodePistola, ConstraintSet.TOP, R.id.containerPistola, ConstraintSet.BOTTOM)
            set.connect(R.id.btnBarcodeScanner, ConstraintSet.TOP, R.id.containerPistola, ConstraintSet.BOTTOM)
            set.connect(R.id.btnInputText, ConstraintSet.TOP, R.id.containerPistola, ConstraintSet.BOTTOM)
            set.connect(R.id.textViewQtdItensLeitura, ConstraintSet.TOP, R.id.containerPistola, ConstraintSet.BOTTOM)

            set.applyTo(binding.containerActivityLeitura)
        }
        // limpa a variavel para ser reutilizada no keydown
        barcode = null
        // deixa marcado opção atual
        tipoLeitura = Pistola
        ajustaFoco()
    }

    /*
    Inicia a leitura padrão utilizada no último acesso a tela
     */
    private fun iniciaLeituraPadrao() {
        val config = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)

        if(binding.btnBarcodePistola.visibility == View.GONE) {
            binding.btnBarcodePistola.visibility = View.VISIBLE
        }
        if(binding.btnBarcodeScanner.visibility == View.GONE) {
            binding.btnBarcodeScanner.visibility = View.VISIBLE
        }
        if(binding.btnInputText.visibility == View.GONE) {
            binding.btnInputText.visibility = View.VISIBLE
        }

        when(config.getInt(LEITURA_PADRAO, Escaner.ordinal)) {
            Manual.ordinal -> {
                iniciaLeituraManual()
            }
            Escaner.ordinal -> {
                iniciaLeituraBarcodeScanner()
            }
            else -> {
                iniciaLeituraBarcodePistola()
            }
        }
    }

    /*
    Esconde todos os mecânismos de leitura de códigos de barras
     */
    private fun encerraLeituras() {
        if(binding.containerPistola.visibility == View.VISIBLE) {
            binding.containerPistola.visibility = View.GONE
        }
        if(binding.containerInputText.visibility == View.VISIBLE) {
            binding.containerInputText.visibility = View.GONE
        }
        if(binding.barcodeScanner.visibility == View.VISIBLE) {
            binding.barcodeScanner.visibility = View.GONE
        }

        if(binding.btnBarcodePistola.visibility == View.VISIBLE) {
            binding.btnBarcodePistola.visibility = View.GONE
        }
        if(binding.btnBarcodeScanner.visibility == View.VISIBLE) {
            binding.btnBarcodeScanner.visibility = View.GONE
        }
        if(binding.btnInputText.visibility == View.VISIBLE) {
            binding.btnInputText.visibility = View.GONE
        }

        val set = ConstraintSet()
        set.clone(binding.containerActivityLeitura)
        set.connect(R.id.recyclerViewItensLeitura, ConstraintSet.TOP, R.id.layoutUltimoItem, ConstraintSet.BOTTOM)
        set.applyTo(binding.containerActivityLeitura)

        adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)
    }

    // altera o modo de leitura atual
    private fun abreEdicaoLeitura() {
        var modal: AlertDialog? = null
        val modalContent = layoutInflater.inflate(R.layout.fragment_modal_edicao_leitura, null)
        val editTextTituloLeitura = modalContent.findViewById<EditText>(R.id.editTextTituloLeitura)
        val radioGroupModosLeitura = modalContent.findViewById<RadioGroup>(R.id.radioGroupModosLeitura)
        val btnCancelarDetalhesColeta = modalContent.findViewById<Button>(R.id.btnCancelarDetalhesColeta)
        val btnSalvarDetalhesColeta = modalContent.findViewById<Button>(R.id.btnSalvarDetalhesColeta)
        // carrega dados
        editTextTituloLeitura.setText(leitura?.titulo)

        if(modoLeitura == Contagem) {
            radioGroupModosLeitura.check(R.id.radioButtonLeituraContagem)
        } else {
            radioGroupModosLeitura.check(R.id.radioButtonLeituraSequencial)
        }

        // desabilita se necessário
        if ((leitura?.status ?: "P") != "P") {
            editTextTituloLeitura.isEnabled = false
            //radioGroupModosLeitura.radioButtonLeituraContagem.isEnabled = false
            //radioGroupModosLeitura.radioButtonLeituraSequencial.isEnabled = false
            //btnSalvarDetalhesColeta.visibility = View.GONE
            btnCancelarDetalhesColeta.setText(R.string.fechar)
        }

        btnCancelarDetalhesColeta.setOnClickListener {
            modal?.dismiss()

            ajustaFoco()
        }
        btnSalvarDetalhesColeta.setOnClickListener {
            it.isEnabled = false
            btnCancelarDetalhesColeta.isEnabled = false
            editTextTituloLeitura.isEnabled = false
            radioGroupModosLeitura.isEnabled = false

            val toolbar = supportActionBar
            val editConfig = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE).edit()

            // salva

            var subtitle = ""

            if(radioGroupModosLeitura.checkedRadioButtonId == R.id.radioButtonLeituraSequencial) {
                subtitle = getString(R.string.sequencial)
                modoLeitura = Sequencial
                editConfig.putString("MODO_LEITURA", "SEQUENCIAL")
            } else {
                subtitle = getString(R.string.contagem)
                modoLeitura = Contagem
                editConfig.putString("MODO_LEITURA", "AGRUPADO")
            }

            toolbar?.subtitle = subtitle

            editConfig.apply()

            val titulo = editTextTituloLeitura.text.toString().trim()

            Thread{
                var criouLeitura = false

                if (leitura == null) {
                    leitura = Leitura()
                    leitura?.status = "P"
                    leitura?.titulo = titulo
                    leitura?.dataAlteracao = Calendar.getInstance(locale)
                    leitura?.dataInsercao = Calendar.getInstance(locale)
                    leitura?.quantidade = 0
                    leitura?.id = leituraDao?.insert(leitura!!) ?: 0

                    itensLeitura = ArrayList<ItemLeitura>()
                    criouLeitura = true

                } else {
                    leitura?.titulo = titulo
                    leitura?.dataAlteracao = Calendar.getInstance(locale)
                    leituraDao?.update(leitura!!)

                    if(modoLeitura == Sequencial) {
                        itensLeitura = itemLeituraDao?.getAllByLeituraID(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
                    } else {
                        itensLeitura = itemLeituraDao?.getAllAgrupadoByQuantidade(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
                    }
                }

                runOnUiThread {
                    supportActionBar?.title = titulo.ifEmpty { String.format(getString(R.string.leitura_x), leitura?.id?.toString()) }
                    adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                    if(criouLeitura) {
                        binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())

                        binding.layoutUltimoItem.visibility = if (leitura?.status == "P") View.VISIBLE else View.GONE

                        btnExportar?.isVisible = false
                        btnFinalizar?.isVisible = true
                        btnExcluir?.isVisible = true
                        btnLeituraLote?.isVisible = (leitura?.status == "P")
                        btnLeituraSerial?.isVisible = (leitura?.status == "P")
                    }

                    modal?.dismiss()

                    ajustaFoco()
                }
            }.start()
        }

        modal = AlertDialog.Builder(this)
            .setTitle(R.string.detalhes_da_coleta)
            .setView(modalContent)
            .create()
        modal?.show()
    }

    private fun abreLeitura() {
        if(leitura == null) {
            return
        }
        if(leitura?.status == "P") {
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.atencao)
            .setMessage(R.string.deseja_abrir_leitura)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.confirmar) { _, _ ->
                Thread{
                    leitura!!.status = "P"
                    leitura!!.dataAlteracao = Calendar.getInstance(locale)
                    leituraDao?.update(leitura!!)

                    runOnUiThread {
                        carregaLeitura(leitura?.id ?: 0L)
                    }
                }.start()
                return@setPositiveButton
            }
            .create()
            .show()
    }

    /*
    Vibra o dispositivo uma vez de forma curta
     */
    private fun vibrar() {
        try {
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            // Vibra por 300 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated API 26
                v.vibrate(300)
            }

        } catch (ex: Exception) {
            Timber.e(ex.localizedMessage)
        }
    }

    /*
    Reproduz som de beep
     */
    private fun beep() {
        Thread{
            try {
                val count = 100 * .01f
                var mediaPlayer = MediaPlayer.create(applicationContext, R.raw.audiook)
                mediaPlayer.setVolume(count, count)
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    if(mediaPlayer?.isLooping == true) {
                        mediaPlayer.stop()
                    }
                    mediaPlayer?.release()
                    mediaPlayer = null

                    return@setOnErrorListener(true)
                }
                mediaPlayer.setOnCompletionListener {
                    it.stop()
                    it.release()
                }

                mediaPlayer.start()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    /*
    Reproduz som de buzina
     */
    private fun buzinar() {
        Thread{
            try {
                val count = 100 * .01f
                var mediaPlayer = MediaPlayer.create(applicationContext, R.raw.audionok)
                mediaPlayer.setVolume(count, count)
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    if(mediaPlayer?.isLooping == true) {
                        mediaPlayer.stop()
                    }
                    mediaPlayer?.release()
                    mediaPlayer = null

                    return@setOnErrorListener(true)
                }
                mediaPlayer.setOnCompletionListener {
                    it.stop()
                    it.release()
                }

                mediaPlayer.start()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun carregaLeitura(idLeitura: Long) {
        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = false
        progressDialog.setMessage(getText(R.string.carregando_leitura))
        progressDialog.show()

        Thread{
            leitura = leituraDao?.getByID(idLeitura)?.first()

            itensLeitura = if (modoLeitura == Sequencial) {
                itemLeituraDao?.getAllByLeituraID(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
            } else {
                itemLeituraDao?.getAllAgrupadoByQuantidade(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
            }

            runOnUiThread {
                binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())
                adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                supportActionBar?.title = leitura?.titulo?.ifEmpty { String.format(getString(R.string.leitura_x), idLeitura.toString()) }

                binding.layoutUltimoItem.visibility = if (leitura?.status == "P") View.VISIBLE else View.GONE

                btnExcluir?.isVisible = leitura?.status != "S"
                btnAbrir?.isVisible = leitura?.status != "S" && leitura?.status != "P"
                btnLeituraLote?.isVisible = (leitura?.status == "P")
                btnLeituraSerial?.isVisible = (leitura?.status == "P")

                if( (leitura?.status == "F" || leitura?.status == "S") && itensLeitura.size > 0 ) {
                    btnFinalizar?.isVisible = false
                    btnExportar?.isVisible = true

                } else if( leitura?.status == "P" && itensLeitura.size > 0 ) {
                    btnFinalizar?.isVisible = true
                    btnExportar?.isVisible = false

                } else {
                    btnFinalizar?.isVisible = false
                    btnExportar?.isVisible = false
                }

                if( leitura?.status == "F" || leitura?.status == "S" || leitura?.status == "E") {
                    encerraLeituras()

                } else {
                    iniciaLeituraPadrao()
                }

                progressDialog.dismiss()
            }
        }.start()
    }

    private fun adicionaContagem(barcode: String) {
        Thread {
            val codigoDeBarras = barcode.trim()

            if((tipoLeitura == Escaner && salvandoCodigoDeBarras) || codigoDeBarras.isEmpty()) {
                return@Thread
            }

            if( (leitura?.status ?: "P") != "P") {
                runOnUiThread {
                    Toast.makeText(applicationContext, R.string.validacao_coleta_nao_permite_alteracao, Toast.LENGTH_SHORT).show()
                }
                return@Thread
            }
            salvandoCodigoDeBarras = true

            // salva
            if(leitura == null) {
                val novaLeitura = Leitura()
                novaLeitura.quantidade = 1
                novaLeitura.dataInsercao = Calendar.getInstance(locale)
                novaLeitura.dataAlteracao = Calendar.getInstance(locale)
                novaLeitura.status = "P"

                if (novaLeitura.titulo.isNullOrEmpty()) {
                    novaLeitura.titulo = "${getString(R.string.nova_leitura)} #${Random().nextInt(99999)}"
                }

                val idLeitura = leituraDao?.insert(novaLeitura) ?: 0
                novaLeitura.id = idLeitura

                this.leitura = novaLeitura

                val item = ItemLeitura()
                item.idLeitura = idLeitura
                item.quantidade = 1
                item.dataInsercao = Calendar.getInstance(locale)
                item.dataAlteracao = Calendar.getInstance(locale)
                item.tipoLeitura = getTipoLeituraItem(tipoLeitura ?: Manual)
                item.sequencia = itemLeituraDao?.nextSequencia(idLeitura) ?: 0

                when (modoItem) {
                    ModoItem.Serial -> {
                        item.lote = barcode
                        item.codigoBarras = itemParent?.codigoBarras
                        item.controlaLotes = itemParent?.controlaLotes ?: "S"
                    }
                    ModoItem.Lote -> {
                        item.lote = itemParent?.lote
                        item.codigoBarras = barcode
                        item.controlaLotes = itemParent?.controlaLotes ?: "L"
                    }
                    else -> {
                        item.codigoBarras = barcode
                        item.controlaLotes = itemParent?.controlaLotes ?: "N"
                    }
                }
                atualizaItemLeitura(item, item.quantidade, true)

                runOnUiThread {
                    binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())

                    binding.layoutUltimoItem.visibility = if (leitura?.status == "P") View.VISIBLE else View.GONE

                    adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                    btnAbrir?.isVisible = false
                    btnExportar?.isVisible = false
                    btnExcluir?.isVisible = true
                    btnLeituraSerial?.isVisible = (leitura?.status == "P")
                    btnLeituraLote?.isVisible = (leitura?.status == "P")

                    supportActionBar?.title = leitura?.titulo?.ifEmpty { String.format(getString(R.string.leitura_x), idLeitura.toString()) }

                    vibrar()
                    beep()

                    if(tipoLeitura == Manual && !binding.editTextCodigoDeBarras.hasFocus()) {
                        ajustaFoco()
                    }
                }

                // tranca para evitar leitura duplicada na câmera
                if (tipoLeitura == Escaner) {
                    Thread.sleep(MIN_TIME_BARCODE_CONTINUS)
                }

                // libera para que o mesmo código de barras possa ser salvo novamente, no caso da câmera
                this.barcode = null
                this.salvandoCodigoDeBarras = false

            } else if(leitura?.status == "P") {
                val indexOfItem = itensLeitura.indexOfFirst {
                    when(modoItem) {
                        ModoItem.Normal -> it.codigoBarras == barcode && it.lote == null
                        ModoItem.Lote -> it.codigoBarras == barcode && it.lote == itemParent?.lote
                        ModoItem.Serial -> it.lote == barcode && it.codigoBarras == itemParent?.codigoBarras
                    }
                }

                val item = if (indexOfItem != -1) itensLeitura[indexOfItem] else ItemLeitura()
                item.sequencia = itemLeituraDao?.nextSequencia(leitura?.id ?: 0) ?: 0

                if(indexOfItem == -1) {
                    // item novo
                    item.idLeitura = leitura?.id ?: 0
                    item.quantidade = 1
                    item.dataInsercao = Calendar.getInstance(locale)
                    item.dataAlteracao = Calendar.getInstance(locale)
                    item.tipoLeitura = getTipoLeituraItem(tipoLeitura ?: Manual)
                    item.sequencia = itemLeituraDao?.nextSequencia(item.idLeitura) ?: 0

                    when (modoItem) {
                        ModoItem.Serial -> {
                            item.lote = barcode
                            item.codigoBarras = itemParent?.codigoBarras
                            item.controlaLotes = itemParent?.controlaLotes ?: "S"
                        }
                        ModoItem.Lote -> {
                            item.lote = itemParent?.lote
                            item.codigoBarras = barcode
                            item.controlaLotes = itemParent?.controlaLotes ?: "L"
                        }
                        else -> {
                            item.codigoBarras = barcode
                            item.controlaLotes = itemParent?.controlaLotes ?: "N"
                        }
                    }

                    atualizaItemLeitura(item, item.quantidade, true)

                } else {
                    // item existe, contamos quantos existem
                    when (modoItem) {
                        ModoItem.Serial -> {
                            atualizaItemLeitura(item, 1, true)
                        }
                        ModoItem.Lote -> {
                            if (item.tipoLeitura == null) {
                                item.tipoLeitura = getTipoLeituraItem(tipoLeitura ?: Manual)
                            }

                            atualizaItemLeitura(item, item.quantidade + 1, true)
                        }
                        else -> {
                            val itens = itemLeituraDao?.getAllByCodigoBarras(codigoDeBarras, leitura?.id?.toInt() ?: 0, item.lote) ?: ArrayList<ItemLeitura>()

                            val quantidadeAtualizacao = itens.sumOf { it.quantidade  } + 1

                            // if for unknow reasons nothing was found, prevent delete everything
                            if(quantidadeAtualizacao > 0) {
                                item.quantidade = quantidadeAtualizacao

                                if (item.tipoLeitura == null) {
                                    item.tipoLeitura = getTipoLeituraItem(tipoLeitura ?: Manual)
                                }

                                atualizaItemLeitura(item, quantidadeAtualizacao, true)
                            }
                        }
                    }
                }

                runOnUiThread {
                    binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())
                    adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                    // we should always scroll to top after inserting
                    try {
                        val linearLayout = binding.recyclerViewItensLeitura.layoutManager as LinearLayoutManager
                        linearLayout.scrollToPosition(0)

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                    vibrar()
                    beep()

                    if(tipoLeitura == Manual && !binding.editTextCodigoDeBarras.hasFocus()) {
                        ajustaFoco()
                    }
                }

                // tranca para evitar leitura duplicada no caso da câmera
                if (tipoLeitura == Escaner) {
                    Thread.sleep(MIN_TIME_BARCODE_CONTINUS)
                }

                // libera para que o mesmo código de barras possa ser salvo novamente
                this.barcode = null
                this.salvandoCodigoDeBarras = false
            }
        }.start()
    }

    private fun getTipoLeituraItem(tipo: Const.Leitor.TiposLeitura): String {
        return when(tipo) {
            Escaner -> "C" // Câmera
            Pistola -> "L" // Leitor Laser
            else -> "M" // Manual
        }
    }

    private fun excluirLeitura() {
        if(leitura == null) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.atencao)
            .setMessage(if (leitura?.status == "E") R.string.confirma_exclusao_leitura else R.string.confirma_mover_lixeira)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.confirmar) { _, _ ->
                Thread{
                    if (leitura?.status == "E") {
                        // excluí fisicamente
                        leituraDao?.delete(leitura!!)

                    } else {
                        leitura!!.status = "E"
                        leitura!!.dataAlteracao = Calendar.getInstance(locale)
                        leituraDao?.update(leitura!!)
                    }

                    runOnUiThread {
                        val intentClose = Intent()
                        intentClose.putExtra("carregarUsuario", usuarioRealizouLogin)

                        setResult(Activity.RESULT_OK, intentClose)
                        finish()
                    }
                }.start()
                return@setPositiveButton
            }.create().show()
    }

    override fun clickExcluirItemLeitura(item: ItemLeitura) {
        if(leitura?.status != "P") {
            Toast.makeText(applicationContext, R.string.validacao_coleta_nao_permite_alteracao, Toast.LENGTH_SHORT).show()
            return
        }

        Thread{
            if (!salvandoCodigoDeBarras) {
                barcode = null
            }
            // In count mode, deleting and item means deliting the barcode
            // but in sequence mode, is just one item
            if(modoLeitura == Contagem) {
                atualizaItemLeitura(item, 0)

            } else {
                itemLeituraDao?.delete(item)
                val indexOf = itensLeitura.indexOfFirst { it.id == item.id }

                if (indexOf != -1) {
                    itensLeitura.removeAt(indexOf)

                } else if(modoLeitura == Sequencial) {
                    itensLeitura = itemLeituraDao?.getAllByLeituraID(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>

                } else {
                    itensLeitura = itemLeituraDao?.getAllAgrupadoByQuantidade(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
                }

                leitura?.quantidade = LeituraUtils.contaItens(itensLeitura)
                leitura?.dataAlteracao = Calendar.getInstance(locale)
                leituraDao?.update(leitura!!)
            }

            runOnUiThread {
                binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())
                adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                if(itensLeitura.size <= 0 && btnFinalizar?.isVisible != true) {
                    btnFinalizar?.isVisible = false
                } else if(itensLeitura.size > 0 && btnFinalizar?.isVisible != true) {
                    btnFinalizar?.isVisible = true
                }

                ajustaFoco()
            }
        }.start()
    }

    override fun clickEditarItemLeitura(item: ItemLeitura) {
        val modelContent = FragmentModalEdicaoItemLeituraBinding.inflate(layoutInflater)
        modelContent.textViewCodigoBarras.text = item.codigoBarras

        val editTextQtd = modelContent.editTextQtdItemLeitura
        editTextQtd.setText(item.quantidade.toString())

        val modal = AlertDialog.Builder(this)
            .setTitle(R.string.informe_a_quantidade)
            .setView(modelContent.root)
            .create()

        modelContent.btnCancelarQtdItemLeitura.setOnClickListener {
            modal.dismiss()
        }
        modelContent.btnSalvarQtdItemLeitura.setOnClickListener {
            val texto = editTextQtd.text.toString().trim()
            modal.dismiss()

            if(TextUtils.isEmpty(texto)) {
                Toast.makeText(applicationContext, R.string.nenhum_numero_informado, Toast.LENGTH_SHORT).show()

                ajustaFoco()

            } else if(!isNumericOnly(texto)) {
                Toast.makeText(applicationContext, R.string.apenas_numeros_permitidos, Toast.LENGTH_SHORT).show()

                ajustaFoco()

            } else {
                try {
                    val quantidade = texto.toInt()

                    Thread{
                        if (!salvandoCodigoDeBarras) {
                            barcode = null
                        }
                        atualizaItemLeitura(item, quantidade)

                        runOnUiThread {
                            binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())
                            adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                            if(itensLeitura.size <= 0 && (btnFinalizar?.isVisible ?: true)) {
                                btnFinalizar?.isVisible = false
                            } else if(itensLeitura.size > 0 && !(btnFinalizar?.isVisible ?: false)) {
                                btnFinalizar?.isVisible = true
                            }

                            ajustaFoco()
                        }
                    }.start()

                } catch (ex: Exception) {
                    Toast.makeText(applicationContext, ex.localizedMessage, Toast.LENGTH_SHORT).show()

                    ajustaFoco()
                }
            }
        }

        modal.show()
        editTextQtd.requestFocus()
    }

    override fun atualizaQuantidadeItem(item: ItemLeitura) {
        Thread{
            atualizaItemLeitura(item, item.quantidade)

            runOnUiThread {
                binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())
                adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                if(itensLeitura.size <= 0 && btnFinalizar?.isVisible != false) {
                    btnFinalizar?.isVisible = false
                } else if(itensLeitura.size > 0 && btnFinalizar?.isVisible != true) {
                    btnFinalizar?.isVisible = true
                }

                ajustaFoco()
            }
        }.start()
    }

    private fun atualizaItemLeitura(item: ItemLeitura, quantidade: Int, selecionarLote: Boolean = false) {
        val itens = itemLeituraDao?.getAllByCodigoBarras(item.codigoBarras, leitura?.id?.toInt() ?: 0, item.lote) ?: ArrayList()

        if (quantidade == 0 && itens.isNotEmpty()) {
            // delete everything from that barcode
            itemLeituraDao?.deleteCodigoBarras(item.codigoBarras ?: "", leitura?.id?.toInt() ?: 0, item.lote)
            atualizarTotal()

        } else if (quantidade > 0 && quantidade > itens.size) {
            // insere o que falta
            val qtdFaltante = quantidade - itens.size

            for(index in 1..qtdFaltante) {
                val novoItem = ItemLeitura()

                novoItem.id = 0
                novoItem.idLeitura = leitura?.id ?: 0
                novoItem.codigoBarras = item.codigoBarras
                novoItem.quantidade = 1
                novoItem.tipoLeitura = item.tipoLeitura
                novoItem.mensagemErro = item.mensagemErro
                novoItem.dataAlteracao = Calendar.getInstance(locale)
                novoItem.dataInsercao = Calendar.getInstance(locale)
                novoItem.lote = item.lote
                novoItem.controlaLotes = item.controlaLotes
                novoItem.sequencia = item.sequencia

                if (selecionarLote) {
                    when(modoItem) {
                        ModoItem.Serial -> {
                            itemLeituraDao?.consultaSerial(leitura?.id, item.codigoBarras, item.lote)?.firstOrNull()?.let {
                                runOnUiThread {
                                    AlertDialog.Builder(this@LeituraActivity)
                                        .setTitle(R.string.atencao)
                                        .setMessage(R.string.este_serial_ja_foi_inserido)
                                        .setPositiveButton(R.string.entendido, null)
                                        .show()
                                }
                            } ?: run {
                                insertItemLeitura(novoItem)
                            }
                        }
                        ModoItem.Lote -> {
                            insertItemLeitura(novoItem)
                        }
                        else -> selecaoLote(item)
                    }
                } else {
                    insertItemLeitura(novoItem)
                }
            }

            atualizarTotal()

        } else if (quantidade > 0 && quantidade < itens.size) {
            // excluí o que excede
            val qtdExcedente = itens.size - quantidade
            var qtdExcluida = 0

            for(itemParaExcluir in itens) {
                itemLeituraDao?.delete(itemParaExcluir)
                qtdExcluida += 1

                if(qtdExcluida == qtdExcedente) {
                    break
                }
            }

            atualizarTotal()
        }
    }

    private fun atualizarTotal() {
        // recarrega itens
        itensLeitura = if(modoLeitura == Sequencial) {
            itemLeituraDao?.getAllByLeituraID(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
        } else {
            itemLeituraDao?.getAllAgrupadoByQuantidade(leitura?.id?.toInt() ?: 0) as ArrayList<ItemLeitura>
        }

        leitura?.quantidade = LeituraUtils.contaItens(itensLeitura)
        leitura?.dataAlteracao = Calendar.getInstance(locale)
        leituraDao?.update(leitura!!)
    }

    private fun modoItem(modoItem: ModoItem, itemPai: ItemLeitura? = null) {
        this.itemParent = itemPai
        this.modoItem = modoItem

        when(this.modoItem) {
            ModoItem.Normal -> {
                binding.layoutModoSerial.visibility = View.GONE
            }
            ModoItem.Lote -> {
                binding.layoutModoSerial.visibility = View.VISIBLE
                binding.textViewModoSerial.text = getString(R.string.leitura_de_itens_do_lote_x).replace("%s", itemPai?.lote ?: "")
            }
            ModoItem.Serial -> {
                binding.layoutModoSerial.visibility = View.VISIBLE
                binding.textViewModoSerial.text = getString(R.string.leitura_de_serial_do_item_x).replace("%s", itemPai?.codigoBarras ?: "")
            }
        }
    }

    private fun finalizaLeitura() {
        if(leitura == null) {
            Toast.makeText(this, R.string.validacao_finaliza_leitura_null, Toast.LENGTH_SHORT).show()
            return
        }

        if( (leitura?.quantidade ?: 0) <= 0 || itensLeitura.size <= 0) {
            Toast.makeText(this, R.string.validacao_finaliza_leitura_vazio, Toast.LENGTH_SHORT).show()
            return
        }

        if(leitura?.status != "P") {
            Toast.makeText(this, R.string.validacao_finaliza_leitura_status, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.atencao)
            .setMessage(R.string.confirma_finalizacao_leitura)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.confirmar) { _, _ ->
                Thread{
                    leitura?.status = "F"
                    leitura?.dataAlteracao = Calendar.getInstance(locale)
                    leituraDao?.update(leitura!!)

                    runOnUiThread {
                        binding.layoutUltimoItem.visibility = View.GONE

                        btnFinalizar?.isVisible = false
                        btnLeituraLote?.isVisible = false
                        btnLeituraSerial?.isVisible = false
                        btnExportar?.isVisible = true
                        btnAbrir?.isVisible = true

                        encerraLeituras()
                    }
                }.start()
                return@setPositiveButton
            }.create().show()
    }

    private fun exportaLeitura() {
        if(leitura == null) {
            Toast.makeText(this, R.string.validacao_exporta_leitura_null, Toast.LENGTH_SHORT).show()
            return
        }

        if( (leitura?.quantidade ?: 0) <= 0 || itensLeitura.size <= 0) {
            Toast.makeText(this, R.string.validacao_exporta_leitura_vazio, Toast.LENGTH_SHORT).show()
            return
        }

        if(leitura?.status != "F" && leitura?.status != "S") {
            Toast.makeText(this, R.string.validacao_exporta_leitura_status, Toast.LENGTH_SHORT)
                .show()
            return
        }

        val bottomDialog = BottomSheetDialog(this)
        val sheetView = FragmentListaExportacoesBinding.inflate(layoutInflater)


        sheetView.btnExportarCSV.setOnClickListener {
            bottomDialog.dismiss()

            Thread{
                val itens = itemLeituraDao?.getAllAgrupadoByQuantidade(leitura!!.id.toInt()) ?: ArrayList<ItemLeitura>()
                val coletaExport = ColetaExport(applicationContext, leitura!!, itens)
                val adapter = ColetaExportCSV()
                val uriCompartilhar = coletaExport.geraArquivoUri(adapter)

                runOnUiThread {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = adapter.mimetype()
                    intent.putExtra(Intent.EXTRA_STREAM, uriCompartilhar)

                    startActivity(intent)
                }
            }.start()
        }
        sheetView.btnExportarJSON.setOnClickListener {
            bottomDialog.dismiss()

            Thread{
                val itens = itemLeituraDao?.getAllAgrupadoByQuantidade(leitura!!.id.toInt()) ?: ArrayList<ItemLeitura>()
                val coletaExport = ColetaExport(applicationContext, leitura!!, itens)
                val adapter = ColetaExportJSON()
                val uriCompartilhar = coletaExport.geraArquivoUri(adapter)

                runOnUiThread {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = adapter.mimetype()
                    intent.putExtra(Intent.EXTRA_STREAM, uriCompartilhar)

                    startActivity(intent)
                }
            }.start()
        }
        sheetView.btnExportarXML.setOnClickListener {
            bottomDialog.dismiss()

            Thread{
                val itens = itemLeituraDao?.getAllAgrupadoByQuantidade(leitura!!.id.toInt()) ?: ArrayList<ItemLeitura>()
                val coletaExport = ColetaExport(applicationContext, leitura!!, itens)
                val adapter = ColetaExportXML()
                val uriCompartilhar = coletaExport.geraArquivoUri(adapter)

                runOnUiThread {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = adapter.mimetype()
                    intent.putExtra(Intent.EXTRA_STREAM, uriCompartilhar)

                    startActivity(intent)
                }
            }.start()
        }
        sheetView.btnExportarTXTGenerico.setOnClickListener {
            bottomDialog.dismiss()

            Thread{
                val itens = itemLeituraDao?.getAllAgrupadoByQuantidade(leitura!!.id.toInt()) ?: ArrayList<ItemLeitura>()
                val coletaExport = ColetaExport(applicationContext, leitura!!, itens)
                val adapter = ColetaExportTXTGenerico()
                val uriCompartilhar = coletaExport.geraArquivoUri(adapter)

                runOnUiThread {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = adapter.mimetype()
                    intent.putExtra(Intent.EXTRA_STREAM, uriCompartilhar)

                    startActivity(intent)
                }
            }.start()
        }

        bottomDialog.setContentView(sheetView.root)
        bottomDialog.setTitle(R.string.selecione_uma_opcao_exportacao)
        bottomDialog.show()
    }

    private fun ajustaFoco() {
        if (tipoLeitura == Manual) {
            binding.editTextCodigoDeBarras.requestFocus()

        } else if(tipoLeitura == Pistola) {
            binding.editTextPistola.requestFocus()

        } else {
            // remove foco
            val currentFocus = getCurrentFocus()
            if(currentFocus != null) {
                currentFocus.clearFocus()
            }
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus

        if (view != null) {
            try {
                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun validaCodigoDeBarras (barCode: String): Boolean {
        try {
            val config = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)

            when (config.getInt(BARCODE_PADRAO, Escaner.ordinal)) {
                Ean13.ordinal -> {
                    var digit = 0
                    var calculated = 0
                    var ean: String? = null
                    val checkSum = "131313131313"
                    var sum = 0

                    if (barCode.length == 13) {
                        digit = Integer.parseInt("" + barCode.get(barCode.length - 1))
                        ean = barCode.substring(0, barCode.length - 1)
                        for (i in 0..ean.length - 1) {
                            sum += (Integer.parseInt("" + ean.get(i))) * (Integer.parseInt("" + checkSum.get(i)))
                        }

                        if((sum % 10) == 0){
                            calculated = 0
                        }
                        else{
                            calculated = 10 - (sum % 10)
                        }

                        return (digit == calculated)

                    } else {
                        return false
                    }
                }
                Ean8.ordinal -> {
                    var digit = 0
                    var calculated = 0
                    var ean: String? = null
                    val checkSum = "313131313131"
                    var sum = 0

                    if (barCode.length == 8) {
                        digit = Integer.parseInt("" + barCode.get(barCode.length - 1))
                        ean = barCode.substring(0, barCode.length - 1)
                        for (i in 0..ean.length - 1) {
                            sum += (Integer.parseInt("" + ean.get(i))) * (Integer.parseInt("" + checkSum.get(i)))
                        }

                        if((sum % 10) == 0){
                            calculated = 0
                        } else{
                            calculated = 10 - (sum % 10)
                        }

                        return (digit == calculated)

                    } else {
                        return false
                    }
                }
                UpcA.ordinal -> {
                    var digit = 0
                    var calculated = 0
                    var ean: String? = null
                    val checkSum = "313131313131"
                    var sum = 0

                    if (barCode.length == 12) {
                        digit = Integer.parseInt("" + barCode.get(barCode.length - 1))
                        ean = barCode.substring(0, barCode.length - 1)
                        for (i in 0..ean.length - 1) {
                            sum += (Integer.parseInt("" + ean.get(i))) * (Integer.parseInt("" + checkSum.get(i)))
                        }

                        if((sum % 10) == 0){
                            calculated = 0
                        }
                        else{
                            calculated = 10 - (sum % 10)
                        }

                        return (digit == calculated)

                    } else {
                        return false
                    }
                }
                UpcE.ordinal -> {
                    var digit = 0
                    var calculated = 0
                    var ean: String? = null
                    var sum = 0
                    val checkSum = "313131313131"

                    if (barCode.length == 8) {
                        digit = Integer.parseInt("" + barCode.get(barCode.length - 1))
                        ean = barCode.substring(0, barCode.length - 1)
                        for (i in 0..ean.length - 1) {
                            sum += (Integer.parseInt("" + ean.get(i))) * (Integer.parseInt("" + checkSum.get(i)))
                        }

                        if((sum % 10) == 0){
                            calculated = 0
                        }
                        else{
                            calculated = 10 - (sum % 10)
                        }

                        return (digit == calculated)

                    } else {
                        return false
                    }
                }

                else -> return true
            }
        } catch (e: Exception){
            return false
        }
        return false
    }

    private fun isNumericOnly(string: String): Boolean {
        return try {
            Integer.parseInt(string)
            true

        } catch (ex: Exception) {
            false
        }
    }

    private fun selecaoLote(item: ItemLeitura) {
        atualizaItemLeitura(item, item.quantidade)
        return
    }

    private fun insertItemLeitura(item: ItemLeitura) {
        Thread {
            item.id = itemLeituraDao?.insert(item) ?: 0
            itemLeituraDao?.updateSequencia(item.sequencia, item.idLeitura, item.codigoBarras ?: "", item.lote)

            atualizarTotal()

            runOnUiThread {
                binding.textViewCodigoBarras.text = item.codigoBarras
                if (!item.lote.isNullOrEmpty()) {
                    binding.textViewLote.visibility = View.VISIBLE
                    if (item.controlaLotes.equals("L", true)) {
                        binding.textViewLote.text = "Lot.: ${item.lote}"
                    } else if (item.controlaLotes.equals("S", true)) {
                        binding.textViewLote.text = "S.: ${item.lote}"
                    }
                } else {
                    binding.textViewLote.visibility = View.GONE
                }

                GlideApp.with(applicationContext)
                    .load(R.drawable.icon_barcode)
                    .placeholder(R.drawable.icon_barcode)
                    .error(R.drawable.icon_barcode)
                    .into(binding.iconeBarcode)

                binding.textViewQtdItensLeitura.text = String.format(getString(R.string.total_x), leitura?.quantidade?.toString())
                adapter?.atualizaLista(itensLeitura, leitura?.status == "P", modoLeitura ?: Sequencial)

                if(itensLeitura.size <= 0 && (btnFinalizar?.isVisible != false)) {
                    btnFinalizar?.isVisible = false
                } else if(itensLeitura.size > 0 && btnFinalizar?.isVisible != true) {
                    btnFinalizar?.isVisible = true
                }

                ajustaFoco()
            }
        }.start()
    }

    override fun outroLote(item: ItemLeitura) {
        // Novo lote
        val titulo: String
        val dica: String

        if(item.controlaLotes == "S") {
            titulo = getString(R.string.insira_um_serial)
            dica = getString(R.string.novo_serial)
        } else {
            titulo = getString(R.string.insira_um_lote)
            dica = getString(R.string.novo_lote)
        }

        val frame = FragmentEdittextBinding.inflate(layoutInflater)
        frame.editText.inputType = InputType.TYPE_CLASS_TEXT
        frame.editText.hint = "$dica..."

        AlertDialog.Builder(this@LeituraActivity)
            .setTitle(titulo)
            .setView(frame.root)
            .setPositiveButton(R.string.concluido) { dialog, _ ->
                dialog.dismiss()
                val lote = frame.editText.text.toString().trim().uppercase()

                if (lote.isNotEmpty()) {
                    item.lote = lote

                    Thread {
                        val itens = itemLeituraDao?.getAllByCodigoBarras(item.codigoBarras, item.idLeitura.toInt(), item.lote) ?: ArrayList()
                        val quantidadeAtualizacao = itens.sumOf { it.quantidade  } + 1

                        if(quantidadeAtualizacao > 0) {
                            item.quantidade = quantidadeAtualizacao

                            if (item.tipoLeitura == null) {
                                item.tipoLeitura =
                                    getTipoLeituraItem(tipoLeitura ?: Manual)
                            }

                            atualizaItemLeitura(item, quantidadeAtualizacao)
                        }
                    }.start()
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    override fun fixarLeitura(item: ItemLeitura) {
        if (item.controlaLotes == "S") {
            val item = ItemLeitura(codigoBarras = item.codigoBarras,
                controlaLotes = "S",
                tipoLeitura = item.tipoLeitura ?: getTipoLeituraItem(tipoLeitura ?: Manual)
            )
            modoItem(ModoItem.Serial, item)
        } else {
            val item = ItemLeitura(lote = item.lote,
                controlaLotes = "L",
                tipoLeitura = item.tipoLeitura ?: getTipoLeituraItem(tipoLeitura ?: Manual)
            )
            modoItem(ModoItem.Lote, item)
        }
    }

    override fun selecionaLote(item: ItemLeitura) {
        Thread {
            val itens = itemLeituraDao?.getAllByCodigoBarras(item.codigoBarras, item.idLeitura.toInt(), item.lote) ?: ArrayList<ItemLeitura>()
            val quantidadeAtualizacao = itens.sumOf { it.quantidade  } + 1

            if(quantidadeAtualizacao > 0) {
                item.quantidade = quantidadeAtualizacao

                if (item.tipoLeitura == null) {
                    item.tipoLeitura =
                        getTipoLeituraItem(tipoLeitura ?: Manual)
                }

                atualizaItemLeitura(item, quantidadeAtualizacao)
            }
        }.start()
    }

    override fun dismissLote() {
        if(tipoLeitura == Escaner) {
            binding.barcodeScanner.resume()
        }
    }

    private fun iniciaLeituraLoteFixo() {
        val frame = FragmentEdittextBinding.inflate(layoutInflater)
        frame.editText.inputType = InputType.TYPE_CLASS_TEXT
        frame.editText.hint = "${getString(R.string.lote)}..."

        AlertDialog.Builder(this@LeituraActivity)
            .setTitle(R.string.insira_o_lote_para_iniciar_leitura_de_itens)
            .setView(frame.root)
            .setPositiveButton(R.string.concluido) { dialog, _ ->
                dialog.dismiss()
                val lote = frame.editText.text.toString().trim().uppercase()

                if (lote.isNotEmpty()) {
                    val item = ItemLeitura(lote = lote,
                        controlaLotes = "L",
                        tipoLeitura = getTipoLeituraItem(tipoLeitura ?: Manual)
                    )
                    modoItem(ModoItem.Lote, item)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun iniciaLeituraSerialFixo() {
        val frame = FragmentEdittextBinding.inflate(layoutInflater)
        frame.editText.inputType = InputType.TYPE_CLASS_TEXT
        frame.editText.hint = "${getString(R.string.codigo_de_barras)}..."

        AlertDialog.Builder(this@LeituraActivity)
            .setTitle(R.string.insira_o_codigo_barras)
            .setView(frame.root)
            .setPositiveButton(R.string.concluido) { dialog, _ ->
                dialog.dismiss()
                val codigo = frame.editText.text.toString().trim().uppercase()

                if (codigo.isNotEmpty()) {
                    val item = ItemLeitura(codigoBarras = codigo,
                        controlaLotes = "S",
                        tipoLeitura = getTipoLeituraItem(tipoLeitura ?: Manual)
                    )
                    modoItem(ModoItem.Serial, item)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }
}