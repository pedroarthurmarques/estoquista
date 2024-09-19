package br.com.pedroamarques.estoquista.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.pedroamarques.estoquista.databinding.FragmentSimpleBinding
import kotlin.collections.ArrayList

class RecyclerViewAdapterLotes(context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface itemClickListener {
        fun clickLote(lote: String, position: Int)
        fun clickIcon(lote: String, position: Int)
    }

    private val mContext = context
    private val mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    public var mControleLotes: String = "L"
    public var mLotes = ArrayList<String>()
    public var mLotesSelecionados = ArrayList<String>()
    public var onClickItemListener: itemClickListener? = null

    class LoteViewHolder(itemBinding: FragmentSimpleBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        val mLayout = itemBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder {
        val itemBinding = FragmentSimpleBinding.inflate(mLayoutInflater, parent, false)
        return LoteViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return mLotesSelecionados.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, posicao: Int) {
        if (holder is LoteViewHolder) {
            val s = mLotesSelecionados[posicao]

            holder.mLayout.textViewTitleSimple.text = s

            holder.mLayout.root.setOnClickListener {
                onClickItemListener?.clickLote(s, posicao)
            }

            if (mControleLotes.equals("L", true)) {
                holder.mLayout.imageViewSimple.visibility = View.VISIBLE

                holder.mLayout.imageViewSimple.setOnClickListener {
                    onClickItemListener?.clickIcon(s, posicao)
                }
            } else {
                holder.mLayout.imageViewSimple.visibility = View.GONE
                holder.mLayout.imageViewSimple.setOnClickListener(null)
            }
        }
    }
}