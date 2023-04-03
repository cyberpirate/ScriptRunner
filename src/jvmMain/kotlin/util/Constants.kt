package util

import java.io.File

object Constants {
    val configFolder get() = File(File(System.getProperty("user.home")), ".script_runner")
}