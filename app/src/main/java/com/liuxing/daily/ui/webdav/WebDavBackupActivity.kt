package com.liuxing.daily.ui.webdav

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityWebDavBackupBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyWithMedia
import com.liuxing.daily.util.MaterialAlertDialogUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.util.WindowUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


private const val WEB_DAV_URL_KEY = "web_dav_url_key"
private const val WEB_DAV_USER_NAME = "web_dav_user_name"
private const val WEB_DAV_PASS_WORD = "web_dav_pass_word"
private const val WEB_DAV_ENCRYPT_PASS_WORD = "web_dav_encrypt_pass_word"

class WebDavBackupActivity : AppCompatActivity() {

    private lateinit var webDavBackupBinding: ActivityWebDavBackupBinding
    private lateinit var sardine: OkHttpSardine
    private var url = ""
    private var accountNumber = ""
    private var password = ""
    private var encryptPassword = ""
    private lateinit var dailyViewModel: DailyViewModel
    private var dailyList: List<DailyEntity> = ArrayList()
    private var dialog: AlertDialog? = null
    private var isSardineInit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        webDavBackupBinding = ActivityWebDavBackupBinding.inflate(layoutInflater)
        setContentView(webDavBackupBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        SoftHideKeyBoardUtil(this)
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        getWebDavAccountNumber()
        initSardine()
        initViewModel()
        setDailyData()
        testWebDavConnect()
        backupDataToWebDav()
        restoreDataFromWebDav()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(webDavBackupBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    /**
     * 初始化 Sardine
     */
    private fun initSardine() {
        sardine = OkHttpSardine().apply {
            if (accountNumber.isEmpty() && password.isEmpty()) return
            setCredentials(
                webDavBackupBinding.inputAccountNumber.text.toString(),
                webDavBackupBinding.inputPassword.text.toString()
            )
            isSardineInit = true
        }
    }

    /**
     * 获取 WebDab 账号
     */
    private fun getWebDavAccountNumber() {
        url = SharedPreferencesUtil.getString(this, WEB_DAV_URL_KEY, "")
        accountNumber = SharedPreferencesUtil.getString(
            this, WEB_DAV_USER_NAME,
            ""
        )
        password = SharedPreferencesUtil.getString(this, WEB_DAV_PASS_WORD, "")
        encryptPassword = SharedPreferencesUtil.getString(this, WEB_DAV_ENCRYPT_PASS_WORD, "")
        webDavBackupBinding.inputUrl.setText(url)
        webDavBackupBinding.inputAccountNumber.setText(accountNumber)
        webDavBackupBinding.inputPassword.setText(password)
        webDavBackupBinding.inputEncrypt.setText(encryptPassword)
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 设置日记数据
     */
    private fun setDailyData() {
        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailyList = value
            }
        })
    }

    /**
     * 测试连接
     */
    private fun testWebDavConnect() {
        webDavBackupBinding.btConnect.setOnClickListener {
            if (webDavBackupBinding.inputUrl.text.isNullOrEmpty() ||
                webDavBackupBinding.inputAccountNumber.text.isNullOrEmpty() ||
                webDavBackupBinding.inputPassword.text.isNullOrEmpty()
            ) {
                dialog = MaterialAlertDialogUtil.showDialog(
                    this@WebDavBackupActivity,
                    getString(R.string.failed_to_connect),
                    getString(R.string.sure),
                    null
                )
                return@setOnClickListener
            }

            SharedPreferencesUtil.putString(
                this,
                WEB_DAV_URL_KEY,
                webDavBackupBinding.inputUrl.text.toString()
            )
            SharedPreferencesUtil.putString(
                this,
                WEB_DAV_USER_NAME,
                webDavBackupBinding.inputAccountNumber.text.toString()
            )
            SharedPreferencesUtil.putString(
                this,
                WEB_DAV_PASS_WORD,
                webDavBackupBinding.inputPassword.text.toString()
            )
            SharedPreferencesUtil.putString(
                this,
                WEB_DAV_ENCRYPT_PASS_WORD,
                webDavBackupBinding.inputEncrypt.text.toString()
            )

            // 初始化 Sardine
            getWebDavAccountNumber()
            initSardine()

            // 测试连接
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val testUrl = "${url}${if (url.endsWith("/")) "" else "/"}"
                    sardine.list(testUrl)
                    withContext(Dispatchers.Main) {
                        dialog = MaterialAlertDialogUtil.showDialog(
                            this@WebDavBackupActivity,
                            getString(R.string.connection_successful),
                            getString(R.string.sure),
                            null
                        )
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        dialog = MaterialAlertDialogUtil.showDialog(
                            this@WebDavBackupActivity,
                            getString(R.string.failed_to_connect),
                            getString(R.string.sure),
                            null
                        )
                    }
                }
            }
        }
    }


    /**
     * 备份数据到 WebDav
     */
    private fun backupDataToWebDav() {
        webDavBackupBinding.btSave.setOnClickListener {
            if (!webDavBackupBinding.inputUrl.text.isNullOrEmpty() ||
                !webDavBackupBinding.inputAccountNumber.text.isNullOrEmpty() ||
                !webDavBackupBinding.inputPassword.text.isNullOrEmpty()
            ) {
                SharedPreferencesUtil.putString(
                    this,
                    WEB_DAV_URL_KEY,
                    webDavBackupBinding.inputUrl.text.toString()
                )
                SharedPreferencesUtil.putString(
                    this, WEB_DAV_USER_NAME,
                    webDavBackupBinding.inputAccountNumber.text.toString()
                )
                SharedPreferencesUtil.putString(
                    this,
                    WEB_DAV_PASS_WORD,
                    webDavBackupBinding.inputPassword.text.toString()
                )
                SharedPreferencesUtil.putString(
                    this,
                    WEB_DAV_ENCRYPT_PASS_WORD,
                    webDavBackupBinding.inputEncrypt.text.toString()
                )

                if (!isSardineInit) {
                    initSardine()
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FileUtil().checkDirExists(sardine, url).let {
                            val fileName = if (url.endsWith("/")) "醒悟" else "/醒悟"
                            if (!it) sardine.createDirectory("${url}${fileName}")
                        }

                        FileUtil().checkFileExists(sardine, url).let {
                            if (it) {
                                FileUtil().deleteFile(sardine, url)
                            }
                            val processedFileList = mutableSetOf<String>()
                            val processedImageFileList = mutableSetOf<String>()
                            val processedVideoFileList = mutableSetOf<String>()
                            val processedAudioFileList = mutableSetOf<String>()

                            withContext(Dispatchers.Main) {
                                val materialAlertDialogBuilder =
                                    MaterialAlertDialogBuilder(this@WebDavBackupActivity)
                                materialAlertDialogBuilder.setView(
                                    layoutInflater.inflate(
                                        R.layout.loading_lndicators_dialog_layout,
                                        null
                                    )
                                )
                                materialAlertDialogBuilder.setCancelable(false)
                                dialog = materialAlertDialogBuilder.create()
                                dialog!!.show()
                            }

                            val dailyWithMediaList = mutableListOf<DailyWithMedia>()
                            dailyList.forEach { dailyEntity ->
                                if (!processedFileList.contains(dailyEntity.dailyUUID)) {
                                    processedFileList.add(dailyEntity.dailyUUID.toString())
                                    val queryImageList =
                                        dailyViewModel.queryDailyImageByUuidToList(dailyEntity.dailyUUID.toString())
                                    val queryVideoList =
                                        dailyViewModel.queryDailyVideoByUuidToList(dailyEntity.dailyUUID.toString())
                                    val queryAudioList =
                                        dailyViewModel.queryDailyAudioByUuidToList(dailyEntity.dailyUUID.toString())
                                    dailyWithMediaList.add(
                                        DailyWithMedia(
                                            dailyEntity,
                                            queryImageList,
                                            queryVideoList,
                                            queryAudioList
                                        )
                                    )
                                }
                            }

                            val zipFilePath = File(externalCacheDir, "daily.zip")
                            val password = webDavBackupBinding.inputEncrypt.text.toString()

                            val zipFile = if (password.isNotEmpty()) {
                                net.lingala.zip4j.ZipFile(zipFilePath, password.toCharArray())
                            } else {
                                net.lingala.zip4j.ZipFile(zipFilePath)
                            }

                            val baseZipParameters = net.lingala.zip4j.model.ZipParameters().apply {
                                if (password.isNotEmpty()) {
                                    isEncryptFiles = true
                                    encryptionMethod =
                                        net.lingala.zip4j.model.enums.EncryptionMethod.AES
                                    aesKeyStrength =
                                        net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256
                                }
                            }

                            val tempDir = File(externalCacheDir, "temp_daily").apply {
                                mkdirs()
                            }

                            dailyWithMediaList.forEach { dailyWithMedia ->
                                val jsonFile =
                                    File(tempDir, "${dailyWithMedia.dailyEntity.dailyUUID}.json")
                                val gson =
                                    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                jsonFile.writeText(gson.toJson(dailyWithMedia))
                                val jsonParams = net.lingala.zip4j.model.ZipParameters().apply {
                                    isEncryptFiles = baseZipParameters.isEncryptFiles
                                    encryptionMethod = baseZipParameters.encryptionMethod
                                    aesKeyStrength = baseZipParameters.aesKeyStrength
                                    fileNameInZip = "${dailyWithMedia.dailyEntity.dailyUUID}.json"
                                }
                                zipFile.addFile(jsonFile, jsonParams)

                                dailyWithMedia.imageList?.forEach { dailyImageEntity ->
                                    dailyImageEntity?.imagePath?.let { imagePath ->
                                        val imageFile = File(imagePath)
                                        if (imageFile.exists() && !processedImageFileList.contains(
                                                imageFile.name
                                            )
                                        ) {
                                            processedImageFileList.add(imageFile.name)
                                            val imageParams =
                                                net.lingala.zip4j.model.ZipParameters().apply {
                                                    isEncryptFiles =
                                                        baseZipParameters.isEncryptFiles
                                                    encryptionMethod =
                                                        baseZipParameters.encryptionMethod
                                                    aesKeyStrength =
                                                        baseZipParameters.aesKeyStrength
                                                    fileNameInZip = "Pictures/${imageFile.name}"
                                                }
                                            zipFile.addFile(imageFile, imageParams)
                                        }
                                    }
                                }

                                dailyWithMedia.videoList?.forEach { dailyVideoEntity ->
                                    dailyVideoEntity?.videoPath?.let { videoPath ->
                                        val videoFile = File(videoPath)
                                        if (videoFile.exists() && !processedVideoFileList.contains(
                                                videoFile.name
                                            )
                                        ) {
                                            processedVideoFileList.add(videoFile.name)
                                            val videoParams =
                                                net.lingala.zip4j.model.ZipParameters().apply {
                                                    isEncryptFiles =
                                                        baseZipParameters.isEncryptFiles
                                                    encryptionMethod =
                                                        baseZipParameters.encryptionMethod
                                                    aesKeyStrength =
                                                        baseZipParameters.aesKeyStrength
                                                    fileNameInZip = "Movies/${videoFile.name}"
                                                }
                                            zipFile.addFile(videoFile, videoParams)
                                        }
                                    }
                                }

                                dailyWithMedia.audioList?.forEach { dailyAudioEntity ->
                                    dailyAudioEntity?.audioPath?.let { audioPath ->
                                        val audioFile = File(audioPath)
                                        if (audioFile.exists() && !processedAudioFileList.contains(
                                                audioFile.name
                                            )
                                        ) {
                                            processedAudioFileList.add(audioFile.name)
                                            val audioParams =
                                                net.lingala.zip4j.model.ZipParameters().apply {
                                                    isEncryptFiles =
                                                        baseZipParameters.isEncryptFiles
                                                    encryptionMethod =
                                                        baseZipParameters.encryptionMethod
                                                    aesKeyStrength =
                                                        baseZipParameters.aesKeyStrength
                                                    fileNameInZip = "Music/${audioFile.name}"
                                                }
                                            zipFile.addFile(audioFile, audioParams)
                                        }
                                    }
                                }
                            }

                            // 处理标签
                            val queryLabelList = dailyViewModel.queryDailyLabelToList()
                            val labelFile = File(tempDir, "labels.json")
                            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                            labelFile.writeText(gson.toJson(queryLabelList))
                            val labelParams = net.lingala.zip4j.model.ZipParameters().apply {
                                isEncryptFiles = baseZipParameters.isEncryptFiles
                                encryptionMethod = baseZipParameters.encryptionMethod
                                aesKeyStrength = baseZipParameters.aesKeyStrength
                                fileNameInZip = "Label/labels.json"
                            }
                            zipFile.addFile(labelFile, labelParams)

                            tempDir.deleteRecursively()

                            val fileName =
                                if (url.endsWith("/")) "醒悟/daily.zip" else "/醒悟/daily.zip"
                            val upload = "${url}${fileName}"
                            sardine.put(upload, zipFilePath.readBytes())

                            withContext(Dispatchers.Main) {
                                if (FileUtil().checkFileExists(zipFilePath.toString())) FileUtil().deleteFile(
                                    zipFilePath.toString()
                                )
                                dialog?.dismiss()
                                SnackbarUtil.showSnackbarShort(
                                    webDavBackupBinding.btSave,
                                    getString(R.string.backup_success)
                                )
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            dialog?.dismiss()
                            val tempZipFile = File(externalCacheDir, "temp_daily.zip")
                            if (tempZipFile.exists()) {
                                tempZipFile.deleteRecursively()
                            }
                            MaterialAlertDialogBuilder(this@WebDavBackupActivity).apply {
                                setMessage(getString(R.string.failed_to_backup_data))
                                setPositiveButton(getString(R.string.sure), null)
                                create()
                                show()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 从 WebDav 恢复数据
     */
    private fun restoreDataFromWebDav() {
        webDavBackupBinding.btRestore.setOnClickListener {
            if (!webDavBackupBinding.inputUrl.text.isNullOrEmpty() ||
                !webDavBackupBinding.inputAccountNumber.text.isNullOrEmpty() ||
                !webDavBackupBinding.inputPassword.text.isNullOrEmpty()
            ) {
                SharedPreferencesUtil.putString(
                    this,
                    WEB_DAV_URL_KEY,
                    webDavBackupBinding.inputUrl.text.toString()
                )
                SharedPreferencesUtil.putString(
                    this, WEB_DAV_USER_NAME,
                    webDavBackupBinding.inputAccountNumber.text.toString()
                )
                SharedPreferencesUtil.putString(
                    this, WEB_DAV_PASS_WORD,
                    webDavBackupBinding.inputPassword.text.toString()
                )
                SharedPreferencesUtil.putString(
                    this,
                    WEB_DAV_ENCRYPT_PASS_WORD,
                    webDavBackupBinding.inputEncrypt.text.toString()
                )

                if (!isSardineInit) {
                    getWebDavAccountNumber()
                    initSardine()
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val fileName = if (url.endsWith("/")) "醒悟/daily.zip" else "/醒悟/daily.zip"
                    importDailyZipFromWebDav(
                        "${url}${fileName}",
                        webDavBackupBinding.inputEncrypt.text.toString()
                    )
                }
            }
        }
    }

    /**
     * 从 WebDav 导入数据
     */
    private suspend fun importDailyZipFromWebDav(url: String, password: String? = null) {
        try {
            val inputStream = sardine.get(url)
            if (inputStream == null) {
                withContext(Dispatchers.Main) {
                    MaterialAlertDialogBuilder(this@WebDavBackupActivity).apply {
                        setMessage(getString(R.string.import_failed))
                        setPositiveButton(getString(R.string.sure), null)
                        create()
                        show()
                    }
                }
                return
            }

            withContext(Dispatchers.Main) {
                val materialAlertDialogBuilder =
                    MaterialAlertDialogBuilder(this@WebDavBackupActivity)
                materialAlertDialogBuilder.setView(
                    layoutInflater.inflate(R.layout.loading_lndicators_dialog_layout, null)
                )
                materialAlertDialogBuilder.setCancelable(false)
                dialog = materialAlertDialogBuilder.create()
                dialog!!.show()
            }

            val tempZipFile = File(externalCacheDir, "temp_daily.zip")
            inputStream.use { input ->
                FileOutputStream(tempZipFile).use { output ->
                    input.copyTo(output)
                }
            }

            val zipFile = if (!password.isNullOrEmpty()) {
                net.lingala.zip4j.ZipFile(tempZipFile, password.toCharArray())
            } else {
                net.lingala.zip4j.ZipFile(tempZipFile)
            }

            val diaryWithMediaList = mutableListOf<DailyWithMedia>()
            val processedFileList = mutableSetOf<String>()

            val imageDir = File(getExternalFilesDir(null), "Pictures").apply { mkdirs() }
            val audioDir = File(getExternalFilesDir(null), "Music").apply { mkdirs() }
            val videoDir = File(getExternalFilesDir(null), "Movies").apply { mkdirs() }

            val fileHeaders = zipFile.fileHeaders
            fileHeaders.forEach { fileHeader ->
                val entryName = fileHeader.fileName
                when {
                    entryName.endsWith(".json") -> {
                        if (processedFileList.contains(entryName)) return@forEach
                        val input = zipFile.getInputStream(fileHeader)
                        val jsonString = input.bufferedReader().readText()
                        input.close()
                        val gson = Gson()
                        if (fileHeader.fileName == "Label/labels.json") {
                            // 获取标签数据库，提取 label 字段
                            val queryDailyLabel =
                                dailyViewModel.queryDailyLabelToList().map { it.label }.toSet()
                            // 反序列化
                            val labelList: List<DailyLabelEntity> = gson.fromJson(
                                jsonString,
                                object : TypeToken<List<DailyLabelEntity>>() {}.type
                            )
                            // 过滤掉已经存在的标签，避免重复插入
                            val newLabels = labelList.filter { it.label !in queryDailyLabel }
                            newLabels.forEach {
                                dailyViewModel.insertDailyLabel(it)
                            }
                        } else {
                            val dailyWithMedia = gson.fromJson(jsonString, DailyWithMedia::class.java)
                            diaryWithMediaList.add(dailyWithMedia)
                        }
                        processedFileList.add(entryName)
                    }

                    entryName.startsWith("Pictures/") -> {
                        val imageFile = File(imageDir, entryName.removePrefix("Pictures/"))
                        zipFile.extractFile(fileHeader, imageDir.absolutePath, imageFile.name)
                        val imagePath = imageFile.absolutePath
                        diaryWithMediaList.forEach { dailyWithImage ->
                            dailyWithImage.imageList?.forEach { dailyImageEntity ->
                                if (dailyImageEntity.imagePath == null) {
                                    dailyImageEntity.imagePath = imagePath
                                }
                            }
                        }
                    }

                    entryName.startsWith("Music/") -> {
                        val audioFile = File(audioDir, entryName.removePrefix("Music/"))
                        zipFile.extractFile(fileHeader, audioDir.absolutePath, audioFile.name)
                        val audioPath = audioFile.absolutePath
                        diaryWithMediaList.forEach { dailyWithMedia ->
                            dailyWithMedia.audioList?.forEach { audioEntity ->
                                if (audioEntity.audioPath == null) {
                                    audioEntity.audioPath = audioPath
                                }
                            }
                        }
                    }

                    entryName.startsWith("Movies/") -> {
                        val videoFile = File(videoDir, entryName.removePrefix("Movies/"))
                        zipFile.extractFile(fileHeader, videoDir.absolutePath, videoFile.name)
                        val videoPath = videoFile.absolutePath
                        diaryWithMediaList.forEach { dailyWithMedia ->
                            dailyWithMedia.videoList?.forEach { videoEntity ->
                                if (videoEntity.videoPath == null) {
                                    videoEntity.videoPath = videoPath
                                }
                            }
                        }
                    }
                }
            }

            // 清理临时文件
            tempZipFile.delete()

            diaryWithMediaList.forEach { dailyWithMedia ->
                val daily = dailyWithMedia.dailyEntity
                if (daily != null) {
                    val exists = withContext(Dispatchers.IO) {
                        dailyViewModel.checkDailyExists(daily.dailyUUID.toString())
                    }
                    if (exists) return@forEach // 存在，跳过
                    dailyViewModel.insertDaily(daily)
                    dailyWithMedia.imageList?.forEach { dailyImageEntity ->
                        dailyImageEntity?.imagePath?.let {
                            dailyImageEntity.dailyUuid = daily.dailyUUID
                            dailyViewModel.insertDailyImagePath(
                                dailyImageEntity.dailyUuid.toString(),
                                listOf(it)
                            )
                        }
                    }
                    dailyWithMedia.audioList?.forEach { audioEntity ->
                        audioEntity?.audioPath?.let {
                            audioEntity.dailyUuid = daily.dailyUUID
                            dailyViewModel.insertDailyAudioPath(
                                audioEntity.dailyUuid.toString(),
                                listOf(it)
                            )
                        }
                    }
                    dailyWithMedia.videoList?.forEach { videoEntity ->
                        videoEntity?.videoPath?.let {
                            videoEntity.dailyUuid = daily.dailyUUID
                            dailyViewModel.insertDailyVideoPath(
                                videoEntity.dailyUuid.toString(),
                                listOf(it)
                            )
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                SnackbarUtil.showSnackbarShort(
                    webDavBackupBinding.btSave,
                    getString(R.string.recovery_successful)
                )
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                File(externalCacheDir, "temp_daily.zip")?.delete()
                dialog?.dismiss()
                MaterialAlertDialogBuilder(this@WebDavBackupActivity).apply {
                    setMessage(getString(R.string.recovery_failed))
                    setPositiveButton(getString(R.string.sure), null)
                    create()
                    show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
           /* R.id.item_help -> {
                val view = LayoutInflater.from(this).inflate(R.layout.help_web_dav_layout, null)
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("使用 WebDav 备份")
                    setView(view)
                    setPositiveButton(getString(R.string.sure), null)
                    create()
                    show()
                }
            }*/

            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // menuInflater.inflate(R.menu.menu_web_dav_backup, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }
}