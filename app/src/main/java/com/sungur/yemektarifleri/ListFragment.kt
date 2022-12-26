package com.sungur.yemektarifleri

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.jar.Manifest


class ListFragment : Fragment() {

    val yemekIsmiListesi = ArrayList<String>()
    val yemekIdListesi = ArrayList<Int>()
    private lateinit var listeAdapter : recycle_adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeAdapter = recycle_adapter(yemekIsmiListesi,yemekIdListesi)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listeAdapter
        sqlVeriAlma()
    }
    fun sqlVeriAlma(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM yemekler",null)
                val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                val yemekIdIndex = cursor.getColumnIndex("id")

                yemekIsmiListesi.clear()
                yemekIdListesi.clear()

                while (cursor.moveToNext()){
                   yemekIsmiListesi.add(cursor.getString(yemekIsmiIndex))
                   yemekIdListesi.add(cursor.getInt(yemekIdIndex))
                }

                listeAdapter.notifyDataSetChanged()

                cursor.close()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }



}