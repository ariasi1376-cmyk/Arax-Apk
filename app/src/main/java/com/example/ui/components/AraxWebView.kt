package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ServiceWorkerClient
import android.webkit.ServiceWorkerController
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.AraxBlack

private const val ARAX_THEATER_URL = "https://arax-theater.ai.studio"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AraxWebViewContainer(
    onProgressUpdate: (Int) -> Unit,
    onPageFinishedLoading: () -> Unit,
    onErrorOccurred: () -> Unit,
    onErrorCleared: () -> Unit,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var customFullScreenView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var pendingFileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var pendingPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }

    // File Chooser Launcher for PWA file & media uploads
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val uris: Array<Uri>? = if (activityResult.resultCode == Activity.RESULT_OK) {
            val dataIntent = activityResult.data
            when {
                dataIntent?.clipData != null -> {
                    val clipData = dataIntent.clipData!!
                    Array(clipData.itemCount) { idx -> clipData.getItemAt(idx).uri }
                }
                dataIntent?.data != null -> arrayOf(dataIntent.data!!)
                else -> null
            }
        } else {
            null
        }
        pendingFileChooserCallback?.onReceiveValue(uris)
        pendingFileChooserCallback = null
    }

    // Dynamic Permission Request Launcher for Camera and Microphone requested by PWA
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val request = pendingPermissionRequest
        if (request != null) {
            val grantedList = mutableListOf<String>()
            val isCameraGranted = permissionsMap[Manifest.permission.CAMERA] == true ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val isAudioGranted = permissionsMap[Manifest.permission.RECORD_AUDIO] == true ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

            for (res in request.resources) {
                if (res == PermissionRequest.RESOURCE_VIDEO_CAPTURE && isCameraGranted) {
                    grantedList.add(res)
                } else if (res == PermissionRequest.RESOURCE_AUDIO_CAPTURE && isAudioGranted) {
                    grantedList.add(res)
                } else if (res == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID) {
                    grantedList.add(res)
                }
            }

            if (grantedList.isNotEmpty()) {
                request.grant(grantedList.toTypedArray())
            } else {
                request.deny()
            }
            pendingPermissionRequest = null
        }
    }

    // Intercept hardware Back Button navigation inside the WebView
    BackHandler(enabled = true) {
        when {
            customFullScreenView != null -> {
                customViewCallback?.onCustomViewHidden()
                customFullScreenView = null
                customViewCallback = null
            }
            webViewInstance?.canGoBack() == true -> {
                webViewInstance?.goBack()
            }
            else -> {
                (context as? Activity)?.finish()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AraxBlack)
            .testTag("arax_webview_container")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // Enable ServiceWorker for PWA offline capabilities and asset caching
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        ServiceWorkerController.getInstance().setServiceWorkerClient(object : ServiceWorkerClient() {
                            override fun shouldInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
                                return super.shouldInterceptRequest(request)
                            }
                        })
                    } catch (_: Exception) {}
                }

                // Security: Disable WebView remote debugging tools in production
                WebView.setWebContentsDebuggingEnabled(false)

                WebView(ctx).apply {
                    setBackgroundColor(AndroidColor.BLACK)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Enable third-party cookies for OAuth & session persistence in PWA
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // Native PWA-optimized WebSettings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT

                        // Enable modern viewport and media rendering
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false

                        // Disable browser chrome & zoom controls to feel like a native app
                        displayZoomControls = false
                        builtInZoomControls = false
                        setSupportZoom(false)

                        // Security configurations
                        allowFileAccess = false
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                        // Standalone PWA User Agent identifier
                        val defaultUa = userAgentString
                        userAgentString = "$defaultUa AraxNativeTheater/1.0"
                    }

                    // WebViewClient: Lock all navigation inside the app and hide any URL
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            val scheme = request.url?.scheme?.lowercase() ?: ""

                            // Handle native device schemes
                            if (scheme in listOf("tel", "mailto", "sms")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                                    view?.context?.startActivity(intent)
                                } catch (_: Exception) {}
                                return true
                            }

                            // Keep all HTTP/HTTPS navigation strictly inside WebView (Never open external browser)
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            onErrorCleared()
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            onPageFinishedLoading()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            // Only show error view if the main frame failed to load
                            if (request?.isForMainFrame == true) {
                                onErrorOccurred()
                            }
                        }

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: RenderProcessGoneDetail?
                        ): Boolean {
                            // Gracefully handle render process termination (e.g. Mesa GPU crash in emulator)
                            try {
                                view?.let {
                                    val parent = it.parent as? ViewGroup
                                    parent?.removeView(it)
                                    it.destroy()
                                }
                            } catch (_: Exception) {}
                            onErrorOccurred()
                            return true
                        }
                    }

                    // WebChromeClient: Fullscreen video, file picker, permissions, progress
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            onProgressUpdate(newProgress)
                        }

                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            pendingFileChooserCallback?.onReceiveValue(null)
                            pendingFileChooserCallback = filePathCallback

                            val intent = try {
                                fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                            } catch (_: Exception) {
                                Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                            }

                            try {
                                fileChooserLauncher.launch(intent)
                                return true
                            } catch (_: Exception) {
                                pendingFileChooserCallback?.onReceiveValue(null)
                                pendingFileChooserCallback = null
                                return false
                            }
                        }

                        override fun onPermissionRequest(request: PermissionRequest?) {
                            if (request == null) return

                            val neededPermissions = mutableListOf<String>()
                            for (resource in request.resources) {
                                if (resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        neededPermissions.add(Manifest.permission.CAMERA)
                                    }
                                }
                                if (resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                        neededPermissions.add(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }

                            if (neededPermissions.isEmpty()) {
                                request.grant(request.resources)
                            } else {
                                pendingPermissionRequest = request
                                permissionLauncher.launch(neededPermissions.toTypedArray())
                            }
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            customFullScreenView = view
                            customViewCallback = callback
                        }

                        override fun onHideCustomView() {
                            customViewCallback?.onCustomViewHidden()
                            customFullScreenView = null
                            customViewCallback = null
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            // Suppress browser console logs from leaking into production
                            return true
                        }
                    }

                    // Load the PWA URL
                    loadUrl(ARAX_THEATER_URL)
                    webViewInstance = this
                }
            },
            update = { view ->
                webViewInstance = view
            }
        )

        // Custom Full-Screen Video / Media View overlay
        if (customFullScreenView != null) {
            AndroidView(
                factory = {
                    FrameLayout(it).apply {
                        setBackgroundColor(AndroidColor.BLACK)
                        addView(
                            customFullScreenView,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(AraxBlack)
            )
        }

        // Native Error Screen with Retry Action if network fails
        if (hasError) {
            AraxErrorView(
                onRetry = {
                    onErrorCleared()
                    webViewInstance?.reload()
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}
