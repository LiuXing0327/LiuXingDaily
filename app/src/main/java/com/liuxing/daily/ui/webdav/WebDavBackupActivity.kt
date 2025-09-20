package com.liuxing.daily.ui.webdav

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.util.ZipUtil.addFileToZipSafely
import com.liuxing.daily.viewmodel.DailyViewModel
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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
     * 保存 WebDav 配置
     */
    private fun saveWebDavConfig() {
        SharedPreferencesUtil.putString(
            this, WEB_DAV_URL_KEY, webDavBackupBinding.inputUrl.text.toString()
        )
        SharedPreferencesUtil.putString(
            this, WEB_DAV_USER_NAME, webDavBackupBinding.inputAccountNumber.text.toString()
        )
        SharedPreferencesUtil.putString(
            this, WEB_DAV_PASS_WORD, webDavBackupBinding.inputPassword.text.toString()
        )
        SharedPreferencesUtil.putString(
            this, WEB_DAV_ENCRYPT_PASS_WORD, webDavBackupBinding.inputEncrypt.text.toString()
        )
    }

    /**
     * 保持光标所在位置
     */
    private fun keepCursorPosition() {
        val editTexts = arrayOf(
            webDavBackupBinding.inputUrl,
            webDavBackupBinding.inputAccountNumber,
            webDavBackupBinding.inputPassword,
            webDavBackupBinding.inputEncrypt
        )

        editTexts.forEach { editText ->
            editText.setSelection(editText.text?.length ?: 0)
        }
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

            saveWebDavConfig()
            // 初始化 Sardine
            getWebDavAccountNumber()
            initSardine()
            keepCursorPosition()

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

    private fun <T> chunkList(list: List<T>, chunkSize: Int): List<List<T>> {
        if (chunkSize <= 0) return listOf(list)
        val result = mutableListOf<List<T>>()
        var i = 0
        while (i < list.size) {
            val end = (i + chunkSize).coerceAtMost(list.size)
            result.add(list.subList(i, end))
            i += chunkSize
        }
        return result
    }

    @Throws(Exception::class)
    private fun uploadFileWithOkHttp(
        uploadUrl: String,
        zipFile: File,
        username: String,
        password: String,
        progressCallback: ((sentBytes: Long, totalBytes: Long) -> Unit)? = null
    ) {
        val client = sharedOkHttpClient

        val requestBody = object : RequestBody() {
            private val contentTypeString = "application/zip"

            override fun contentType() = contentTypeString.toMediaTypeOrNull()

            override fun contentLength(): Long {
                return try {
                    zipFile.length()
                } catch (e: Exception) {
                    -1L
                }
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val total = contentLength().coerceAtLeast(0L)
                var uploaded = 0L

                zipFile.inputStream().use { fis ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (true) {
                        read = fis.read(buffer)
                        if (read == -1) break
                        sink.write(buffer, 0, read)
                        uploaded += read
                        progressCallback?.invoke(uploaded, total)
                    }
                }
            }

        }

        val credential = Credentials.basic(username, password)
        val request =
            Request.Builder().url(uploadUrl).addHeader("Authorization", credential).put(requestBody)
                .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("Upload failed: ${resp.code} ${resp.message}")
            }
        }
    }

    /**
     * 分批备份数据到 WebDav
     */
    private fun backupDataToWebDav() {
        webDavBackupBinding.btSave.setOnClickListener {
            if (webDavBackupBinding.inputUrl.text.isNullOrEmpty() || webDavBackupBinding.inputAccountNumber.text.isNullOrEmpty() || webDavBackupBinding.inputPassword.text.isNullOrEmpty()
            ) {
                return@setOnClickListener
            }

            // 保存配置
            saveWebDavConfig()
            if (!isSardineInit) {
                getWebDavAccountNumber()
                initSardine()
            }
            keepCursorPosition()

            CoroutineScope(Dispatchers.IO).launch {
                var dialogShown = false
                try {
                    FileUtil().checkDirExists(sardine, url).let {
                        val dirName = if (url.endsWith("/")) "醒悟" else "/醒悟"
                        if (!it) sardine.createDirectory("${url}${dirName}")
                    }
                    val dailyWithMediaList = mutableListOf<DailyWithMedia>()
                    val processedDailyUuids = mutableSetOf<String>()
                    dailyList.forEach { dailyEntity ->
                        if (!processedDailyUuids.contains(dailyEntity.dailyUUID)) {
                            processedDailyUuids.add(dailyEntity.dailyUUID.toString())
                            val queryImageList =
                                dailyViewModel.queryDailyImageByUuidToList(dailyEntity.dailyUUID.toString())
                            val queryVideoList =
                                dailyViewModel.queryDailyVideoByUuidToList(dailyEntity.dailyUUID.toString())
                            val queryAudioList =
                                dailyViewModel.queryDailyAudioByUuidToList(dailyEntity.dailyUUID.toString())
                            dailyWithMediaList.add(
                                DailyWithMedia(
                                    dailyEntity, queryImageList, queryVideoList, queryAudioList
                                )
                            )
                        }
                    }

                    withContext(Dispatchers.Main) {
                        val materialAlertDialogBuilder =
                            MaterialAlertDialogBuilder(this@WebDavBackupActivity)
                        materialAlertDialogBuilder.setView(
                            layoutInflater.inflate(
                                R.layout.loading_lndicators_dialog_layout, null
                            )
                        )
                        materialAlertDialogBuilder.setCancelable(false)
                        dialog = materialAlertDialogBuilder.create()
                        dialog!!.show()
                        dialogShown = true
                    }
                    val batchSize = 30
                    val batches = chunkList(dailyWithMediaList, batchSize)
                    val globalProcessedImageNames = mutableSetOf<String>()
                    val globalProcessedVideoNames = mutableSetOf<String>()
                    val globalProcessedAudioNames = mutableSetOf<String>()
                    val password = webDavBackupBinding.inputEncrypt.text.toString()
                    val baseZipParams = ZipParameters().apply {
                        if (password.isNotEmpty()) {
                            isEncryptFiles = true
                            encryptionMethod = EncryptionMethod.AES
                            aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                        }
                    }

                    batches.forEachIndexed { batchIndex, batch ->
                        val tempDir = File(
                            externalCacheDir, "temp_daily_batch_${batchIndex + 1}"
                        ).apply { mkdirs() }
                        val zipFilePath = File(externalCacheDir, "daily_part_${batchIndex + 1}.zip")
                        val zip = if (password.isNotEmpty()) ZipFile(
                            zipFilePath, password.toCharArray()
                        ) else ZipFile(zipFilePath)
                        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                        batch.forEach { dailyWithMedia ->
                            val jsonFile =
                                File(tempDir, "${dailyWithMedia.dailyEntity.dailyUUID}.json")
                            jsonFile.writeText(gson.toJson(dailyWithMedia))
                            val jsonParams = ZipParameters().apply {
                                isEncryptFiles = baseZipParams.isEncryptFiles
                                encryptionMethod = baseZipParams.encryptionMethod
                                aesKeyStrength = baseZipParams.aesKeyStrength
                                fileNameInZip = "${dailyWithMedia.dailyEntity.dailyUUID}.json"
                            }
                            zip.addFile(jsonFile, jsonParams)

                            dailyWithMedia.imageList?.forEach { img ->
                                img?.imagePath?.let { imagePath ->
                                    val imageFile = File(imagePath)
                                    if (imageFile.exists() && !globalProcessedImageNames.contains(
                                            imageFile.name
                                        )
                                    ) {
                                        globalProcessedImageNames.add(imageFile.name)
                                        addFileToZipSafely(
                                            zip, imageFile, "Pictures", baseZipParams
                                        )
                                    }
                                }
                            }

                            dailyWithMedia.videoList?.forEach { vid ->
                                vid?.videoPath?.let { videoPath ->
                                    val videoFile = File(videoPath)
                                    if (videoFile.exists() && !globalProcessedVideoNames.contains(
                                            videoFile.name
                                        )
                                    ) {
                                        globalProcessedVideoNames.add(videoFile.name)
                                        addFileToZipSafely(zip, videoFile, "Movies", baseZipParams)
                                    }
                                }
                            }

                            dailyWithMedia.audioList?.forEach { au ->
                                au?.audioPath?.let { audioPath ->
                                    val audioFile = File(audioPath)
                                    if (audioFile.exists() && !globalProcessedAudioNames.contains(
                                            audioFile.name
                                        )
                                    ) {
                                        globalProcessedAudioNames.add(audioFile.name)
                                        addFileToZipSafely(zip, audioFile, "Music", baseZipParams)
                                    }
                                }
                            }
                        }

                        // 处理标签（只添加一次，放在第一个 batch）
                        if (batchIndex == 0) {
                            val labelFile = File(tempDir, "labels.json")
                            labelFile.writeText(gson.toJson(dailyViewModel.queryDailyLabelToList()))
                            val labelParams = ZipParameters().apply {
                                isEncryptFiles = baseZipParams.isEncryptFiles
                                encryptionMethod = baseZipParams.encryptionMethod
                                aesKeyStrength = baseZipParams.aesKeyStrength
                                fileNameInZip = "Label/labels.json"
                            }
                            zip.addFile(labelFile, labelParams)
                        }
                        tempDir.listFiles()?.forEach { it.delete() }
                        val remoteFileName = "daily_part_${batchIndex + 1}.zip"
                        val remotePath =
                            if (url.endsWith("/")) "${url}醒悟/$remoteFileName" else "$url/醒悟/$remoteFileName"

                        // 上传
                        try {
                            uploadFileWithOkHttp(
                                uploadUrl = remotePath,
                                zipFile = zipFilePath,
                                username = webDavBackupBinding.inputAccountNumber.text.toString(),
                                password = webDavBackupBinding.inputPassword.text.toString()
                            ) { sent, total ->
                                LogUtil.d("Batch ${batchIndex + 1} uploaded $sent / $total")
                            }
                            // 上传成功后删除本地 zip
                            if (zipFilePath.exists()) zipFilePath.delete()
                            // 删除临时目录
                            tempDir.deleteRecursively()

                        } catch (e: Exception) {
                            throw e
                        }
                    }

                    // 所有批次上传成功
                    withContext(Dispatchers.Main) {
                        if (dialogShown) {
                            dialog?.dismiss()
                        }
                        SnackbarUtil.showSnackbarShort(
                            webDavBackupBinding.btSave, getString(R.string.backup_success)
                        )
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
                        LogUtil.e("Backup Failed -> ${e.message}", e)
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


    /**
     * 从 WebDav 恢复数据
     */
    private fun restoreDataFromWebDav() {
        webDavBackupBinding.btRestore.setOnClickListener {
            if (!webDavBackupBinding.inputUrl.text.isNullOrEmpty() ||
                !webDavBackupBinding.inputAccountNumber.text.isNullOrEmpty() ||
                !webDavBackupBinding.inputPassword.text.isNullOrEmpty()
            ) {
                saveWebDavConfig()
                if (!isSardineInit) {
                    getWebDavAccountNumber()
                    initSardine()
                }
                keepCursorPosition()

                CoroutineScope(Dispatchers.IO).launch {
                    val password = webDavBackupBinding.inputEncrypt.text.toString()
                    importAllDailyParts(password)
                }

            }
        }
    }

    /**
     * 从 WebDav 导入所有分片备份文件。
     *
     * 说明：
     * 1. 优先尝试分片备份的方式：
     *      - 按顺序检查 remotePath 是否存在 daily_part_1.zip、daily_part_2.zip...。
     *      - 如果存在则逐个下载并导入，直到遇到不存在的分片为止。
     *      - 每个分片都会调用 importDailyZipFromWebDav 进行解压和数据写入。
     *
     * 2. 如果 daily_part_1.zip 不存在：
     *      - 则判断 daily.zip 是否存在，存在则调用 importDailyZipFromWebDav 进行解压和数据写入。
     *      - 兼容之前未采用分片方式的备份。
     *
     *
     * @param password 解压密码
     */
    private suspend fun importAllDailyParts(password: String) {
        withContext(Dispatchers.Main) {
            val builder = MaterialAlertDialogBuilder(this@WebDavBackupActivity)
            builder.setView(layoutInflater.inflate(R.layout.loading_lndicators_dialog_layout, null))
            builder.setCancelable(false)
            dialog = builder.create()
            dialog!!.show()
        }

        var batchIndex = 1

        try {
            val firstPart = if (url.endsWith("/")) "醒悟/daily_part_1.zip"
            else "/醒悟/daily_part_1.zip"
            val firstPartPath = "$url$firstPart"

            if (sardine.exists(firstPartPath)) {
                while (true) {
                    val fileName = if (url.endsWith("/")) "醒悟/daily_part_$batchIndex.zip"
                    else "/醒悟/daily_part_$batchIndex.zip"
                    val remotePath = "$url$fileName"

                    val exists = sardine.exists(remotePath)
                    if (!exists) break

                    importDailyZipFromWebDav(remotePath, password)
                    batchIndex++
                }
            } else {
                //兼容旧的单文件
                val fileName = if (url.endsWith("/")) "醒悟/daily.zip" else "/醒悟/daily.zip"
                val oldPath = "$url$fileName"

                if (sardine.exists(oldPath)) {
                    importDailyZipFromWebDav(oldPath, password)
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
                dialog?.dismiss()
                LogUtil.e("Failed to restore batch $batchIndex", e)
                MaterialAlertDialogBuilder(this@WebDavBackupActivity)
                    .setMessage(getString(R.string.recovery_failed))
                    .setPositiveButton(getString(R.string.sure), null)
                    .create()
                    .show()
            }
        }
    }


    /**
     * 从 WebDav 导入数据
     */
    private suspend fun importDailyZipFromWebDav(url: String, password: String? = null) {
        try {
            val inputStream = sardine.get(url) ?: return

            val tempZipFile = File(externalCacheDir, "temp_daily.zip")
            inputStream.use { input ->
                FileOutputStream(tempZipFile).use { output ->
                    input.copyTo(output)
                }
            }

            val zipFile = if (!password.isNullOrEmpty()) {
                ZipFile(tempZipFile, password.toCharArray())
            } else {
                ZipFile(tempZipFile)
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
        } finally {
            File(externalCacheDir, "temp_daily.zip")?.delete()
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