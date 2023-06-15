package br.com.minhaempresa.redonto

import android.os.Parcel
import android.os.Parcelable

data class ListChamados(
    val id: String,
    val nome: String?,
    val imagens: List<String> = listOf()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.createStringArrayList()?.filterNotNull() ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
        parcel.writeStringList(imagens)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ListChamados> {
        override fun createFromParcel(parcel: Parcel): ListChamados {
            return ListChamados(parcel)
        }

        override fun newArray(size: Int): Array<ListChamados?> {
            return arrayOfNulls(size)
        }
    }
}
