package com.makenshi.stores

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.makenshi.stores.databinding.FragmentEditStoreBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class EditStoreFragment : Fragment() {

    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null

    private fun String.editable() : Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)


        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val storeId = arguments?.getLong(getString(R.string.arg_id), 0)
        if (storeId != null && storeId != 0L) {
            mIsEditMode = true
            getStore(storeId)
        } else {
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }

        setupActionbar()

        setupTextFields()
    }

    private fun setupActionbar() {
        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title =
            if(mIsEditMode) getString(R.string.edit_store_title_edit)
            else getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)
    }

    private fun getStore(storeId: Long) {
        doAsync {
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(storeId)
            uiThread {
                if (mStoreEntity != null) setUiStore(mStoreEntity!!)
            }
        }
    }

    private fun setUiStore(storeEntity: StoreEntity) {
        with(mBinding) {
            etName.text = storeEntity.name.editable()
            etPhone.text = storeEntity.phone.editable()
            etWebsite.text = storeEntity.webSite.editable()
            etPhotoUrl.text = storeEntity.photoUrl.editable()

//            Glide.with(mActivity!!)
//                .load(storeEntity.photoUrl)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .centerCrop()
//                .into(imgPhoto)
        }
    }

    private fun setupTextFields() {
        with(mBinding) {
            etName.addTextChangedListener { validateFields(tilName) }
            etPhone.addTextChangedListener { validateFields(tilPhone) }
            etPhotoUrl.addTextChangedListener {
                validateFields(tilPhotoUrl)
                loadImage(it.toString().trim())
            }
        }

    }

    private fun loadImage(url: String) {
        mBinding.etPhotoUrl.addTextChangedListener {
            Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mBinding.imgPhoto)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save -> {
                if (mStoreEntity != null &&
                    validateFields(mBinding.tilPhotoUrl, mBinding.tilPhone, mBinding.tilName)) {
                    //val store = StoreEntity(
                    with(mStoreEntity!!) {
                        name = mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        webSite = mBinding.etWebsite.text.toString().trim()
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                    }

                    doAsync {
                        if (mIsEditMode) StoreApplication.database.storeDao().updateStore(mStoreEntity!!)
                        else mStoreEntity!!.id = StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        uiThread {
                            hideKeyboard()
                            if (mIsEditMode) {
                                mActivity?.updateStore(mStoreEntity!!)
                                Snackbar.make(mBinding.root,
                                    R.string.edit_store_messages_update_success,
                                    Snackbar.LENGTH_SHORT)
                                    .show()
                            } else {
                                mActivity?.addStore(mStoreEntity!!)
                                Toast.makeText(
                                    mActivity,
                                    R.string.edit_store_messages_save_success,
                                    Toast.LENGTH_SHORT
                                ).show()

                                mActivity?.onBackPressed()
                            }
                        }
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validateFields(vararg textFields: TextInputLayout): Boolean {
        var isValid = true

        for(textField in textFields) {
            if (textField.editText?.text.toString().trim().isEmpty()) {
                textField.error = getString(R.string.helper_required)
                isValid = false
            } else {
                textField.error = null
            }
        }

        if (!isValid) Snackbar.make(mBinding.root,
            R.string.edit_store_message_valid,
            Snackbar.LENGTH_SHORT).show()

        return isValid
    }

//    private fun validateFields(): Boolean {
//        var isValid = true
//
//        if (mBinding.etPhotoUrl.text.toString().trim().isEmpty()) {
//            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
//            mBinding.etPhotoUrl.requestFocus()
//            isValid = false
//        }
//
//        if (mBinding.etPhone.text.toString().trim().isEmpty()) {
//            mBinding.tilPhone.error = getString(R.string.helper_required)
//            mBinding.etPhone.requestFocus()
//            isValid = false
//        }
//
//        if (mBinding.etName.text.toString().trim().isEmpty()) {
//            mBinding.tilName.error = getString(R.string.helper_required)
//            mBinding.etName.requestFocus()
//            isValid= false
//        }
//
//        return isValid
//    }

    private fun hideKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) {
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)

        setHasOptionsMenu(false)
        super.onDestroy()
    }
}