package net.codeocean.cheese.core

import net.codeocean.cheese.backend.impl.ADBImpl
import net.codeocean.cheese.backend.impl.APPImpl

import net.codeocean.cheese.backend.impl.AssetsImpl
import net.codeocean.cheese.backend.impl.BaseImpl
import net.codeocean.cheese.backend.impl.CanvasImpl
import net.codeocean.cheese.backend.impl.ColorImpl
import net.codeocean.cheese.backend.impl.ConvertersImpl
import net.codeocean.cheese.backend.impl.DeviceImpl
import net.codeocean.cheese.backend.impl.EnvImpl
import net.codeocean.cheese.backend.impl.EventsImpl
import net.codeocean.cheese.backend.impl.FilesImpl
import net.codeocean.cheese.backend.impl.FloatingWindowImpl
import net.codeocean.cheese.backend.impl.HttpImpl
import net.codeocean.cheese.backend.impl.ImageImpl
import net.codeocean.cheese.backend.impl.KeyboardImpl
import net.codeocean.cheese.backend.impl.KeysImpl
import net.codeocean.cheese.backend.impl.OCRImpl
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.backend.impl.PermissionsImpl
import net.codeocean.cheese.backend.impl.PersistentStoreImpl
import net.codeocean.cheese.backend.impl.PluginsImpl
import net.codeocean.cheese.backend.impl.PointImpl
import net.codeocean.cheese.backend.impl.RecordScreenImpl
import net.codeocean.cheese.backend.impl.RootImpl
import net.codeocean.cheese.backend.impl.ThreadImpl
import net.codeocean.cheese.backend.impl.ToolWindowImpl
import net.codeocean.cheese.backend.impl.UiNodeImpl
import net.codeocean.cheese.backend.impl.WebSocketImpl
import net.codeocean.cheese.backend.impl.WebViewImpl
import net.codeocean.cheese.backend.impl.YoloImpl
import net.codeocean.cheese.backend.impl.ZipImpl
import net.codeocean.cheese.core.api.ADB
import net.codeocean.cheese.core.api.APP

import net.codeocean.cheese.core.api.Assets
import net.codeocean.cheese.core.api.Base
import net.codeocean.cheese.core.api.Canvas
import net.codeocean.cheese.core.api.Color
import net.codeocean.cheese.core.api.Converters
import net.codeocean.cheese.core.api.Device
import net.codeocean.cheese.core.api.Events
import net.codeocean.cheese.core.api.Files
import net.codeocean.cheese.core.api.FloatingWindow
import net.codeocean.cheese.core.api.Http
import net.codeocean.cheese.core.api.Image
import net.codeocean.cheese.core.api.Keyboard
import net.codeocean.cheese.core.api.Keys
import net.codeocean.cheese.core.api.OCR
import net.codeocean.cheese.core.api.Path
import net.codeocean.cheese.core.api.Permissions
import net.codeocean.cheese.core.api.PersistentStore
import net.codeocean.cheese.core.api.Plugins
import net.codeocean.cheese.core.api.Point
import net.codeocean.cheese.core.api.RecordScreen
import net.codeocean.cheese.core.api.Root
import net.codeocean.cheese.core.api.Thread
import net.codeocean.cheese.core.api.UiNode
import net.codeocean.cheese.core.api.WebSocket
import net.codeocean.cheese.core.api.WebView
import net.codeocean.cheese.core.api.Yolo
import net.codeocean.cheese.core.api.Zip

object CoreFactory {

    fun getDevice(): Device {
        return DeviceImpl
    }

    fun getAssets(): Assets {
        return AssetsImpl
    }

    fun getAPP(): APP {
        return APPImpl
    }

    fun getPersistentStore(): PersistentStore {
        return PersistentStoreImpl
    }

    fun getImage(): Image {
        return ImageImpl
    }

    fun getBase(): Base {
        return BaseImpl
    }
    fun getConverters(): Converters {
        return ConvertersImpl
    }
    fun getEvents(): Events {
        return EventsImpl
    }
    fun getFiles(): Files {
        return FilesImpl
    }

    fun createFloatingWindow():FloatingWindow{
        return FloatingWindowImpl()
    }

    fun getFloatingWindow(): FloatingWindow.Companion {
        return FloatingWindow.Companion
    }

    fun  createHttp():Http{
        return HttpImpl()
    }
    fun getKeyboard():Keyboard{
        return KeyboardImpl
    }
    fun getKeys(): Keys {
        return KeysImpl
    }
    fun getOCR(): OCR {
        return OCRImpl
    }
    fun getPermissions(): Permissions {
        return PermissionsImpl
    }

    fun getPlugins(): Plugins.Companion {
        return Plugins.Companion
    }

    fun createPlugins(): Plugins {
        return PluginsImpl()
    }

    fun getPoint(): Point {
        return PointImpl
    }
    fun getRecordScreen(): RecordScreen {
        return RecordScreenImpl
    }
    fun getRoot(): Root {
        return RootImpl
    }
    fun getADB(): ADB {
        return ADBImpl
    }
    fun createThread(): Thread {
        return ThreadImpl()
    }
    fun createUiNode(): UiNode {
        return UiNodeImpl()
    }

    fun getUiNode(): UiNode.Companion {
        return UiNode.Companion
    }

    fun getWebSocket(): WebSocket {
        return WebSocketImpl
    }

    fun getWebView(): WebView {
        return WebViewImpl
    }
    fun getYolo(): Yolo {
        return YoloImpl
    }

    fun getZip(): Zip {
        return ZipImpl
    }
    fun getColor(): Color {
        return ColorImpl
    }
    fun getPath(): Path {
        return PathImpl
    }

    fun getEnv(): net.codeocean.cheese.core.api.Env {
        return EnvImpl
    }


    fun getToolWindow(): net.codeocean.cheese.core.api.ToolWindow {
        return ToolWindowImpl
    }

    fun getCanvas():Canvas{
        return CanvasImpl
    }



}