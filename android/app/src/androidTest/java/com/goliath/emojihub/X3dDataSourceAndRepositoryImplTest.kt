package com.goliath.emojihub

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goliath.emojihub.data_sources.local.X3dDataSourceImpl
import com.goliath.emojihub.models.CreatedEmoji
import com.goliath.emojihub.models.X3dInferenceResult
import com.goliath.emojihub.repositories.local.X3dRepositoryImpl
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.pytorch.Module
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class X3dDataSourceAndRepositoryImplTest {
    @Deprecated("Deprecated because now we use hagrid model instead of kinetics400 model")
    suspend fun createEmoji_archeryVideo_returnPairOfClassNameAndUnicode() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val x3dRepositoryImpl = X3dRepositoryImpl(x3dDataSourceImpl)
         /*
         shaking hands 영상에 대해서는 prediction이 정확하지 않음.
         이유!!!: inference의 시작 시점을 정확하게 잡아주는 것이 상당히 중요함.
                 sampling의 총 시간이 sampling rate (12 frames) / 30 fps = 0.4 sec 이고
                 xs expansion 기준 4 frame을 sampling 한다고 했을 때 총 1.6 sec 이므로
                 상당히 짧은 시간이다. 따라서, 시작 시점을 정확하게 잡아주는 것이 중요함.
          */
        // val sampleVideoAbsolutePath = x3dRepositoryImpl.assetFilePath("shaking hands.mp4")
        val sampleVideoAbsolutePath = x3dDataSourceImpl.assetFilePath("kinetics/archery.mp4")
        val videoUri = Uri.fromFile(File(sampleVideoAbsolutePath))

        var emojiInfo : List<CreatedEmoji>
        runBlocking {
            emojiInfo = x3dRepositoryImpl.createEmoji(videoUri, topK=1)
        }
        Log.d("X3dRepositoryImplTest", "emojiInfo: $emojiInfo")
        assert(
            CreatedEmoji("archery", "U+1F3AF") == emojiInfo[0]
        ){
            """
            Predicted class index is not 5. 
            This error may be caused by the poor performance of the model.   
            """.trimMargin()
        }
    }

    @Test
    fun createEmoji_palmVideo_returnPairOfClassNameAndUnicode() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val x3dRepositoryImpl = X3dRepositoryImpl(x3dDataSourceImpl)

        val sampleVideoAbsolutePath = x3dDataSourceImpl.assetFilePath("Hagrid/test_palm_video.mp4")
        val videoUri = Uri.fromFile(File(sampleVideoAbsolutePath))

        var emojiInfo : List<CreatedEmoji>
        runBlocking {
            emojiInfo = x3dRepositoryImpl.createEmoji(videoUri, topK=1)
        }
        Log.d("X3dRepositoryImplTest", "emojiInfo: $emojiInfo")
        assert(
            CreatedEmoji("palm", "U+1F64B") == emojiInfo[0]
        ) {
            """
            Predicted class index is not 10. 
            This error may be caused by the poor performance of the model.   
            """.trimMargin()
        }
    }

    // Followings are the step by step guide to run X3dRepository unit test.
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.goliath.emojihub", appContext.packageName)
    }

    @Test
    fun assetManager_efficientX3dXsTutorialFloat_returnFileInputStream() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val assetManager = appContext.assets
        val inputStream = assetManager.open("kinetics/efficient_x3d_xs_tutorial_float.pt")
        Log.e("X3dRepositoryImplTest", "inputStream: $inputStream")
        assertNotNull(assetManager)
    }

    @Test
    fun assetFilePath_efficientX3dXsTutorialFloat_returnFilePath() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSource = X3dDataSourceImpl(appContext)
        val filePath = x3dDataSource.assetFilePath("kinetics/efficient_x3d_xs_tutorial_float.pt")
        // NOTE!: Module.load 에 absolute path 가 사용되므로 assetFilePath 는
        //       assets 폴더의 파일을 context.filesDir 에 복사해 그 파일의 absolute path 를 반환한다.
        assertEquals(
            "/data/user/0/com.goliath.emojihub/files/efficient_x3d_xs_tutorial_float.pt",
            filePath
        )
    }

    @Test
    fun loadModule_efficientX3dXsTutorialFloat_returnModule() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val module = x3dDataSourceImpl.loadModule("kinetics/efficient_x3d_xs_tutorial_float.pt")
        assertTrue(module is Module)
    }

    @Test
    fun loadModule_efficientX3dsHagridFloat_returnModule() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val module = x3dDataSourceImpl.loadModule("Hagrid/efficient_x3d_s_hagrid_float.pt")
        assertTrue(module is Module)
    }

    @Test
    fun checkAnnotationFilesExist_kinetics400_returnPairOfFilePaths() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val filePaths = x3dDataSourceImpl.checkAnnotationFilesExist(
            "kinetics/kinetics_id_to_classname.json",
            "kinetics/kinetics_classname_to_unicode.json"
        )
        assertEquals(
            Pair("/data/user/0/com.goliath.emojihub/files/kinetics_id_to_classname.json",
                "/data/user/0/com.goliath.emojihub/files/kinetics_classname_to_unicode.json"),
            filePaths
        )
    }

    @Test
    fun checkAnnotaionFilesExist_hagrid_returnPairOfFilePaths() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val filePaths = x3dDataSourceImpl.checkAnnotationFilesExist(
            "Hagrid/hagrid_id_to_classname.json",
            "Hagrid/hagrid_classname_to_unicode.json"
        )
        assertEquals(
            Pair("/data/user/0/com.goliath.emojihub/files/hagrid_id_to_classname.json",
                "/data/user/0/com.goliath.emojihub/files/hagrid_classname_to_unicode.json"),
            filePaths
        )
    }

    //  how can I access to the video file in the device?
    //  -> route this issue by using file in assets folder
    @Test
    fun loadVideoMediaMetadataRetriever_videoUri_returnMediaMetadataRetriever() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val sampleVideoAbsolutePath = x3dDataSourceImpl.assetFilePath("kinetics/archery.mp4")
        val videoUri = Uri.fromFile(File(sampleVideoAbsolutePath))

        val mediaMetadataRetriever = x3dDataSourceImpl.loadVideoMediaMetadataRetriever(videoUri)
        assertTrue(
            (mediaMetadataRetriever?.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong() ?: 0) > 0
        )
    }

    @Test
    fun extractFrameTensorsFromVideo_mediaMetadataRetriever_returnTensors() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val sampleVideoAbsolutePath = x3dDataSourceImpl.assetFilePath("kinetics/archery.mp4")
        val videoUri = Uri.fromFile(File(sampleVideoAbsolutePath))

        val mediaMetadataRetriever = x3dDataSourceImpl.loadVideoMediaMetadataRetriever(videoUri)
        if (mediaMetadataRetriever == null){
            Log.e("X3dRepositoryImplTest", "mediaMetadataRetriever is null")
            return
        }
        // Target method: extractFrameTensorsFromVideo
        val startTime = System.currentTimeMillis()
        val inputVideoFrameTensors = x3dDataSourceImpl.extractFrameTensorsFromVideo(mediaMetadataRetriever)
        val elapsedTime = System.currentTimeMillis() - startTime
        Log.i("X3dRepositoryImplTest", "elapsedTime: $elapsedTime ms")
        if (inputVideoFrameTensors == null){
            Log.e("X3dRepositoryImplTest", "tensors is null")
            return
        }
        assertEquals(
            mutableListOf(
                1,
                X3dDataSourceImpl.NUM_CHANNELS.toLong(),
                X3dDataSourceImpl.COUNT_OF_FRAMES_PER_INFERENCE.toLong(),
                X3dDataSourceImpl.CROP_SIZE.toLong(),
                X3dDataSourceImpl.CROP_SIZE.toLong()
            ),
            inputVideoFrameTensors.shape().toList()
        )
    }

    @Test
    fun runInference_efficientX3dXsTutorialFloat_archeryVideo_returnPredictedClassIndex5() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        // load x3d Module
        val x3dModule = x3dDataSourceImpl.loadModule("kinetics/efficient_x3d_xs_tutorial_float.pt")
        if (x3dModule == null){
            Log.e("X3dRepositoryImplTest", "x3dModule is null")
            return
        }
        // load archery video input tensors
        val sampleVideoAbsolutePath = x3dDataSourceImpl.assetFilePath("kinetics/archery.mp4")
        val videoUri = Uri.fromFile(File(sampleVideoAbsolutePath))
        val mediaMetadataRetriever = x3dDataSourceImpl.loadVideoMediaMetadataRetriever(videoUri)
        if (mediaMetadataRetriever == null){
            Log.e("X3dRepositoryImplTest", "mediaMetadataRetriever is null")
            return
        }
        val inputVideoFrameTensors = x3dDataSourceImpl.extractFrameTensorsFromVideo(mediaMetadataRetriever)
        if (inputVideoFrameTensors == null){
            Log.e("X3dRepositoryImplTest", "tensors is null")
            return
        }
        // run inference
        val startTime = System.currentTimeMillis()
        val predictedClassInfo = x3dDataSourceImpl.runInference(x3dModule, inputVideoFrameTensors, topK=1)
        val elapsedTime = System.currentTimeMillis() - startTime
        Log.i("X3dRepositoryImplTest", "elapsedTime: $elapsedTime ms")

        assert (predictedClassInfo[0].score > X3dRepositoryImpl.SCORE_THRESHOLD) {
            """
            X3dRepositoryImplTest, Score of ${predictedClassInfo[0].score} is lower than 
            threshold ${X3dRepositoryImpl.SCORE_THRESHOLD}
            """.trimMargin()
        }
        assert(5 == predictedClassInfo[0].scoreIdx) {
            """
            Predicted class index is not 5. 
            This error may be caused by the poor performance of the model.   
            """.trimMargin()
        }
    }

    @Test
    fun indexToEmojiInfo_0_returnPairOfClassNameAndUnicode() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val x3dDataSourceImpl = X3dDataSourceImpl(appContext)
        val filePaths = x3dDataSourceImpl.checkAnnotationFilesExist(
            "kinetics/kinetics_id_to_classname.json",
            "kinetics/kinetics_classname_to_unicode.json"
        )
        if (filePaths == null){
            Log.e("X3dRepositoryImplTest", "checkAnnotationFilesExist() returns null")
            return
        }
        val classNameFilePath = filePaths.first
        val classUnicodeFilePath = filePaths.second

        val mockInferenceResults = listOf(X3dInferenceResult(0, 0.0f))
        val emojiInfo = x3dDataSourceImpl.indexToEmojiInfo(
            mockInferenceResults, classNameFilePath, classUnicodeFilePath
        )
        assertEquals(
            // dummy emoji unicode is same as the class index
            CreatedEmoji("abseiling", "U+00000"),
            emojiInfo[0]
        )
    }
}