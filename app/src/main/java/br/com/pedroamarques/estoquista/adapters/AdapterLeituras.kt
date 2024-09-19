package br.com.pedroamarques.estoquista.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.pedroamarques.estoquista.R
import br.com.pedroamarques.estoquista.activities.ActivityLeituras
import br.com.pedroamarques.estoquista.activities.ActivityLixeira
import br.com.pedroamarques.estoquista.databinding.FragmentLeituraBinding
import br.com.pedroamarques.estoquista.entities.Leitura
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapterLeituras: RecyclerView.Adapter<RecyclerView.ViewHolder> {

    constructor(context: ActivityLeituras): super() {
        this.mContext = context
        this.itemClickListener = context
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    constructor(context: ActivityLixeira): super() {
        this.mContext = context
        this.itemClickListener = context
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    interface ItemClickListener {
        fun clickItem(item: Leitura, position: Int)
    }

    private val mContext: Context
    private val itemClickListener: ItemClickListener
    private val layoutInflater: LayoutInflater
    private var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    var mLeituras: ArrayList<Leitura> = ArrayList()

    class LeituraViewHolder(itemBinding: FragmentLeituraBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        val containerLeitura = itemBinding.containerLeitura
        val textViewTituloLeitura = itemBinding.textViewTituloLeitura
        val textViewQuantidade = itemBinding.textViewQuantidade
        val textViewNumero = itemBinding.textViewNumero
        val textViewData = itemBinding.textViewData
        val textViewStatusLeitura = itemBinding.textViewStatusLeitura
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = FragmentLeituraBinding.inflate(layoutInflater, parent, false)
        return LeituraViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return mLeituras.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as LeituraViewHolder
        val item = mLeituras[position]

        val id: Long = item.id
        var titulo: String? = item.titulo
        val status: String? = item.status
        val quantidade: Int = item.quantidade

        if (titulo.isNullOrEmpty()) {
            titulo = mContext.getString(R.string.sem_titulo)
        }

        holder.textViewTituloLeitura.text = titulo
        holder.textViewNumero.backgroundTintList = ContextCompat.getColorStateList(mContext, R.color.colorAccent)
        holder.textViewNumero.text = mContext.getString(R.string.leit_x, id.toString())

        if(quantidade <= 0) {
            holder.textViewQuantidade.text = mContext.getText(R.string.nenhum_produto_lido)
        } else if(quantidade == 1) {
            holder.textViewQuantidade.text = mContext.getString(R.string.x_produto, quantidade.toString())
        } else {
            holder.textViewQuantidade.text = mContext.getString(R.string.x_produtos, quantidade.toString())
        }

        when(status) {
            "P" -> {
                holder.textViewStatusLeitura.text = mContext.getString(R.string.pendente)
                holder.textViewStatusLeitura.setTextColor(ContextCompat.getColor(mContext, R.color.colorLaranja))
            }
            "F" -> {
                holder.textViewStatusLeitura.text = mContext.getString(R.string.finalizado)
                holder.textViewStatusLeitura.setTextColor(ContextCompat.getColor(mContext, R.color.colorPreco))
            }
            "E" -> {
                holder.textViewStatusLeitura.text = mContext.getString(R.string.excluido)
                holder.textViewStatusLeitura.setTextColor(ContextCompat.getColor(mContext, R.color.colorVermelho))
            }
        }

        holder.textViewData.text = item.dataInsercao?.time?.let { dateFormat.format(it) }

        holder.containerLeitura.setOnClickListener {
            itemClickListener.clickItem(item, position)
        }
    }

    public fun atualizaLista(leituras: ArrayList<Leitura>) {
        val diffUtil: DiffUtil.DiffResult = DiffUtil.calculateDiff(DiffCallback(mLeituras, leituras))
        mLeituras.clear()
        mLeituras.addAll(leituras)
        diffUtil.dispatchUpdatesTo(this)
    }

    private class DiffCallback(oldList: ArrayList<Leitura>, newList: ArrayList<Leitura>): DiffUtil.Callback() {

        val mOldList: ArrayList<Leitura> = oldList
        val mNewList: ArrayList<Leitura> = newList

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOldList[oldItemPosition].id == mNewList[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return mOldList.size
        }

        override fun getNewListSize(): Int {
            return mNewList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOldList[oldItemPosition] == mNewList[newItemPosition] &&
                    mOldList[oldItemPosition].quantidade == mNewList[newItemPosition].quantidade &&
                    mOldList[oldItemPosition].dataInsercao == mNewList[newItemPosition].dataInsercao &&
                    mOldList[oldItemPosition].id == mNewList[newItemPosition].id &&
                    mOldList[oldItemPosition].status == mNewList[newItemPosition].status
        }
    }
}