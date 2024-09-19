package br.com.pedroamarques.estoquista.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.pedroamarques.estoquista.R
import br.com.pedroamarques.estoquista.activities.LeituraActivity
import br.com.pedroamarques.estoquista.adapters.RecyclerViewAdapterLotes
import br.com.pedroamarques.estoquista.databinding.BottomFragmentLotesBinding
import br.com.pedroamarques.estoquista.entities.ItemLeitura
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.collections.ArrayList


class BottomSheetLotes: BottomSheetDialogFragment(), RecyclerViewAdapterLotes.itemClickListener {

    interface BottomSheetLotesDelegate {
        fun outroLote(item: ItemLeitura)
        fun fixarLeitura(itemLeitura: ItemLeitura)
        fun selecionaLote(item: ItemLeitura)
        fun dismissLote()
    }

    private var delegate: BottomSheetLotesDelegate? = null

    private lateinit var itemLeitura: ItemLeitura

    var lotes = ArrayList<String>()
    var adapter: RecyclerViewAdapterLotes? = null

    private var _binding: BottomFragmentLotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BackgroundBottomSheetStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setStyle(STYLE_NORMAL, R.style.BackgroundBottomSheetStyle)
        _binding = BottomFragmentLotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        view.layoutParams.height = (displayMetrics.heightPixels/2) + 100

        arguments?.let {
            if (it.containsKey("lotes")) {
                lotes = it.getStringArrayList("lotes") ?: arrayListOf()
                binding.textViewTitulo.text = it.getString("titulo", requireContext().getString(R.string.lotes))
            }
        }

        if (activity is LeituraActivity) {
            delegate = activity as LeituraActivity
        }

        binding.recyclerViewLotes.setHasFixedSize(true)

        adapter = context?.let { RecyclerViewAdapterLotes(it) }
        adapter?.mControleLotes = itemLeitura.controlaLotes
        adapter?.onClickItemListener = this

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.recyclerViewLotes.adapter = adapter
        binding.recyclerViewLotes.layoutManager = layoutManager

        adapter?.mLotes = lotes
        adapter?.mLotesSelecionados = lotes
        adapter?.notifyDataSetChanged()

        binding.btnNovo.setOnClickListener {
            delegate?.outroLote(itemLeitura)
            dismiss()
        }

        if (itemLeitura.controlaLotes.equals("S", true)) {
            binding.btnBarcode.visibility = View.VISIBLE
            binding.btnClose.visibility = View.GONE

            binding.btnBarcode.setOnClickListener {
                delegate?.fixarLeitura(itemLeitura)
                dismiss()
            }
        } else {
            binding.btnBarcode.visibility = View.GONE
            binding.btnClose.visibility = View.VISIBLE

            binding.btnClose.setOnClickListener {
                dismiss()
            }
        }



        binding.searchViewText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter?.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                adapter?.let { adapter ->
                    if (texto.isNotEmpty()) {
                        adapter.mLotesSelecionados = adapter.mLotes.filter { it.contains(texto, true) } as ArrayList<String>
                    } else {
                        adapter.mLotesSelecionados = adapter.mLotes
                    }
                }
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        delegate?.dismissLote()
        delegate = null
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance(itemLeitura: ItemLeitura, lotes: ArrayList<String>, titulo: String) = BottomSheetLotes().apply {
            arguments = Bundle().apply {
                putStringArrayList("lotes", lotes)
                putString("titulo", titulo)
            }

            this.itemLeitura = itemLeitura
        }
    }

    override fun clickLote(lote: String, position: Int) {
        itemLeitura.lote = lote

        delegate?.selecionaLote(itemLeitura)
        dismiss()
    }

    override fun clickIcon(lote: String, position: Int) {
        itemLeitura.lote = lote

        delegate?.selecionaLote(itemLeitura)
        delegate?.fixarLeitura(itemLeitura)

        dismiss()
    }
}