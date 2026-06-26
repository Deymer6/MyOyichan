package com.upn.myoyichan.data.remote.model

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results")
    val results: List<DrugLabel>?
)

data class DrugLabel(
    @SerializedName("purpose")
    val purpose: List<String>?,

    @SerializedName("warnings")
    val warnings: List<String>?,

    @SerializedName("adverse_reactions")
    val adverseReactions: List<String>?,

    @SerializedName("dosage_and_administration")
    val dosageAndAdministration: List<String>?,

    @SerializedName("openfda")
    val openfda: OpenFdaInfo?
)

data class OpenFdaInfo(
    @SerializedName("brand_name")
    val brandName: List<String>?,

    @SerializedName("generic_name")
    val genericName: List<String>?
)
