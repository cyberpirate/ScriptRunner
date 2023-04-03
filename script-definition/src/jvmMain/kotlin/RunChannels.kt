import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class RunChannels(val process: Process, private val scope: CoroutineScope) {
    val recvChannel: ReceiveChannel<String>
    val errChannel: ReceiveChannel<String>
    val sendChannel: SendChannel<String>

    init {
        recvChannel = getLineChannel(process.inputStream)
        errChannel = getLineChannel(process.errorStream)
        sendChannel = sendLineChannel(process.outputStream)
    }

    fun checkHasExited(): Boolean {
        return process.waitFor(0, TimeUnit.MILLISECONDS)
    }

    suspend fun waitForExited() {
        while(!checkHasExited()) {
            delay(100)
        }
    }

    private fun getLineChannel(stream: InputStream): ReceiveChannel<String> {
        val ret = Channel<String>(Channel.UNLIMITED)

        scope.launch(Dispatchers.IO) {
            stream.use {
                it.bufferedReader(Charset.defaultCharset()).use { br ->
                    val charBuffer = ArrayList<Char>()

                    while(true) {

                        val isReady = try { br.ready() } catch(_: IOException) { true }
                        val dataOnLine = stream.available() > 0
                        val processRunning = !checkHasExited()

                        if(isReady || !processRunning) {
                            val c = try { br.read() } catch(_: IOException) { -1 }

                            if(c != -1)
                                charBuffer.add(c.toChar())

                            if(c == -1 || c.toChar() == '\n') {
                                if(charBuffer.isNotEmpty()) {
                                    val s = String(charBuffer.toCharArray()).trim()
                                    ret.send(s)
                                    charBuffer.clear()
                                }
                            }

                            if(c == -1 || (!isReady && !dataOnLine && !processRunning)) break
                        } else {
                            try {
                                delay(100)
                            } catch(_: CancellationException) {
                                break
                            }
                        }
                    }

                    ret.close()
                }
            }
        }

        return ret
    }

    private fun sendLineChannel(stream: OutputStream, lineEnding: String = System.lineSeparator()): SendChannel<String> {
        val ret = Channel<String>(Channel.UNLIMITED)

        scope.launch(Dispatchers.IO) {
            stream.use {
                while(true) {
                    val processRunning = !checkHasExited()

                    if(!processRunning) {
                        ret.close()
                        break
                    }

                    val line = ret.tryReceive().getOrNull()
                    if(line != null) {
                        try {
                            it.write("${line.trim()}${lineEnding}".toByteArray(Charset.defaultCharset()))
                        } catch(e: IOException) {
                            ret.close(e)
                            break
                        }
                    } else {
                        delay(100)
                    }
                }
            }
        }

        return ret
    }

}