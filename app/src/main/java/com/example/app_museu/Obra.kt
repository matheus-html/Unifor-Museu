package com.example.app_museu

var listaObras = mutableListOf<Obra>()

data class Obra(
    var isAdminAdded: Boolean = false,
    val autor: String = "",
    val cover: String = "",
    val data: String = "",
    val descricao: String = "",
    val tema: String = "",
    val titulo: String = ""
)
 {

}



