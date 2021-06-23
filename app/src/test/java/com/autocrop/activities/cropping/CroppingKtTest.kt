//package com.autocrop.activities.cropping
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import org.junit.Assert.*
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.powermock.api.mockito.PowerMockito.mockStatic
//import org.powermock.core.classloader.annotations.PowerMockIgnore
//import org.powermock.core.classloader.annotations.PrepareForTest
//import org.powermock.modules.junit4.PowerMockRunner
//import java.io.File
//import java.io.InputStream
//
//
//@PowerMockIgnore("jdk.internal.reflect.*")
//@RunWith(PowerMockRunner::class)
//@PrepareForTest(BitmapFactory::class)
//internal class CroppingKtTest {
//
//    companion object{
//        private val TEST_RSC_DIR_PATH = File(System.getProperty("user.dir"),"src/test/res")
//    }
//
//    @Test
//    fun croppedImage() {
//        mockStatic(BitmapFactory::class.java)
//
//        val bitmap: Bitmap = BitmapFactory.decodeStream(instream)
//        val croppingResults: Pair<Bitmap, Int> = croppedImage(bitmap)!!
//
//        assertEquals(54, croppingResults.second)
//    }
//}