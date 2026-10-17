package org.isoron.platform.gui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.fetch.RequestInit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.json

class JsImage(private val canvas: HTMLCanvasElement) : Image {
    private val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    private val imageData = ctx.getImageData(
        0.0,
        0.0,
        canvas.width.toDouble(),
        canvas.height.toDouble()
    )
    private val data = imageData.data.asDynamic()

    override val width: Int get() = canvas.width
    override val height: Int get() = canvas.height

    override fun getPixel(x: Int, y: Int): Color {
        val i = (y * width + x) * 4
        val r = (data[i] as Number).toDouble() / 255.0
        val g = (data[i + 1] as Number).toDouble() / 255.0
        val b = (data[i + 2] as Number).toDouble() / 255.0
        val a = (data[i + 3] as Number).toDouble() / 255.0
        return Color(r, g, b, a)
    }

    override fun setPixel(x: Int, y: Int, color: Color) {
        val i = (y * width + x) * 4
        data[i] = (color.red * 255).toInt()
        data[i + 1] = (color.green * 255).toInt()
        data[i + 2] = (color.blue * 255).toInt()
        data[i + 3] = (color.alpha * 255).toInt()
    }

    override suspend fun export(path: String) {
        ctx.putImageData(imageData, 0.0, 0.0)
        val blob = suspendCoroutine<dynamic> { cont ->
            canvas.asDynamic().toBlob { b: dynamic -> cont.resume(b) }
        }
        val init = RequestInit(
            method = "POST",
            body = blob,
            headers = json("X-File-Path" to path)
        )
        suspendCoroutine<Unit> { cont ->
            window.fetch("/save-file", init).then(
                { cont.resume(Unit) },
                { err -> cont.resumeWithException(RuntimeException("$err")) }
            )
        }
    }

    companion object {
        fun create(width: Int, height: Int): JsImage {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = width
            canvas.height = height
            return JsImage(canvas)
        }
    }
}
