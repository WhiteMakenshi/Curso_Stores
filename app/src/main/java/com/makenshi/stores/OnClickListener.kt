package com.makenshi.stores

interface OnClickListener {
    fun OnClick(storeId: Long)
    fun OnFavoriteStore(storeEntity: StoreEntity)
    fun onDeleteStore(storeEntity: StoreEntity)
}