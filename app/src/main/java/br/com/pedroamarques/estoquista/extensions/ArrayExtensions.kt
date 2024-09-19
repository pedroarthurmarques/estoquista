package br.com.pedroamarques.estoquista.extensions

object ArrayExtensions {
    fun <T>Iterable<T>?.hasIndex(index: Int): Boolean {
        this?.let {
            return index < it.count() && index >= 0
        } ?: run { return  false }
    }
}