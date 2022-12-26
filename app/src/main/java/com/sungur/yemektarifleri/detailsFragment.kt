package com.sungur.yemektarifleri

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_details.*
import java.io.ByteArrayOutputStream
import java.net.URL


class detailsFragment : Fragment() {

    var selectedView : Uri? = null
    var selectedBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView.setOnClickListener(){
            addView(it)
        }
        button.setOnClickListener(){
            Save(it)
        }

        arguments?.let {
            var gelenBilgi = detailsFragmentArgs.fromBundle(it).bilgi
            if(gelenBilgi.equals("menudenGeldim")){
                // Yeni bir yemek ekleme
                nameFood.setText("")
                detailsFood.setText("")
                button.visibility = View.VISIBLE

                val gorselSecme = BitmapFactory.decodeResource(context?.resources,R.drawable.example)
                imageView.setImageBitmap(gorselSecme)
            }
            else{
                //daha önce oluşturulan yemeği görmeye geldim
                button.visibility = View.INVISIBLE
                val secilenId = detailsFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val IcindekilerIndex = cursor.getColumnIndex("icindekiler")
                        val gorsel = cursor.getColumnIndex("gorsel")
                        while (cursor.moveToNext()){
                            nameFood.setText(cursor.getString(yemekIsmiIndex))
                            detailsFood.setText(cursor.getString(IcindekilerIndex))

                            val byteDizi = cursor.getBlob(gorsel)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizi,0,byteDizi.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }

    }
    fun Save(view: View){
        // SQL KODLARI YAZILACAK
        val yemekIsmi = nameFood.text.toString()
        val icindekiler = detailsFood.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = createSmallImage(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()
            try {
                context?.let{
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY,yemekismi VARCHAR,icindekiler VARCHAR,gorsel BLOB)")

                    val sqlString = "INSERT INTO yemekler (yemekismi,icindekiler,gorsel) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,icindekiler)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()

                }
            }catch (e:Exception){
                e.printStackTrace()
            }
            val  action = detailsFragmentDirections.actionDetailsFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)
        }



    }
    fun addView(view: View){
        activity.let {
            // RESİM SEÇİLECEK
            if(ContextCompat.checkSelfPermission(it!!.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                // İZİN VERİLMİŞ
                val galeryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent,2)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                //izni aldık
                val galeryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            selectedView = data.data
            try {
                context?.let {
                    if(selectedView != null){

                        if (Build.VERSION.SDK_INT >=28){
                            val source = ImageDecoder.createSource(it.contentResolver,selectedView!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,selectedView)
                            imageView.setImageBitmap(selectedBitmap)
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)

    }
    fun createSmallImage(selectedBitmap :Bitmap,maximumSize:Int) :Bitmap{
        var height = selectedBitmap.height
        var width = selectedBitmap.width

        val bitmapRate: Double = width.toDouble() / height.toDouble()
        if(bitmapRate>1){
            //görsel yatay
            val smallHeight = width / bitmapRate
            height = smallHeight.toInt()
        }else{
            //dikey
            val smallWidth = height * bitmapRate
            width = smallWidth.toInt()
        }

        return Bitmap.createScaledBitmap(selectedBitmap,width,height,true)
    }
}