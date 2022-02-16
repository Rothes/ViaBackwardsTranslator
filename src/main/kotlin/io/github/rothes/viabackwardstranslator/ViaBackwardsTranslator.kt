package io.github.rothes.viabackwardstranslator

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.rothes.viabackwardstranslator.utils.FileUtils
import io.github.rothes.viabackwardstranslator.utils.FileUtils.readFile
import java.io.File
import java.util.regex.Pattern


object ViaBackwardsTranslator {

    @JvmStatic
    fun main(args: Array<String>) {
        println("\nViaBackwardsTranslator v1.0\n - Code By Rothes")
        println("\n")

        val loadLocale = loadLocale()
        if (loadLocale.isEmpty())
            return

        translate(loadLocale)

        println("\n\nViaBackwardsTranslator v1.0\n - Code By Rothes")
        println("\nThanks for using!")
    }

    private fun loadLocale(): Map<String, String> {
        println("请提供一份从 Minecraft 中提取的语言 Json 文件的路径:")
        println("Please provide a path to a locale Json file extracted from Minecraft:")

        val localeFile = File(readln())
        if (!FileUtils.isExist(localeFile)) {
            return mapOf()
        }
        val jsonElement = JsonParser.parseString(readFile(localeFile))
        val jsonObject = jsonElement.asJsonObject
        val result = mutableMapOf<String, String>()
        for (key in jsonObject.keySet()) {
            if (
                key.startsWith("entity.minecraft.")
                || key.startsWith("block.minecraft.")
                || key.startsWith("item.minecraft.")
            )
                result[key] = jsonObject.getAsJsonPrimitive(key).asString
        }
        return result.toMap()
    }

    private fun translate(localeMap: Map<String, String>) {
        println("\n请提供 ViaBackwards 插件目录路径:")
        println("Please provide ViaBackwards plugin directory path:")

        val pluginFolder = File(readln())
        if (!FileUtils.isExist(pluginFolder)) {
            return
        }
        if (!pluginFolder.isDirectory) {
            println("ERROR: File is not directory: ${pluginFolder.absolutePath}")
            return
        }

        val toTranslate = mutableListOf<File>()
        for (file in pluginFolder.listFiles()!!) {
            if (file.name.startsWith("mapping-") && file.extension.contentEquals("json", true)) {
                toTranslate.add(file)
            }
        }

        val versionPattern = Pattern.compile("mapping-(?:[\\d.]+)to([\\d.]+).json")
        for (file in toTranslate) {
            println("\nNow processing ${file.name}")
            val matcher = versionPattern.matcher(file.name)
            if (!matcher.matches()) {
                println("WARN: File name Pattern does not match, ignoring.")
                continue
            }
            val version = matcher.group(1)
            val jsonElement = JsonParser.parseString(readFile(file))
            val jsonObject = jsonElement.asJsonObject
            val items = jsonObject.getAsJsonObject("items")
            if (items != null) {
                for (key in items.keySet()) {
                    val material = key.substring(10)
                    val locale = localeMap["block.minecraft.$material"] ?: localeMap["item.minecraft.$material"]
                    if (locale == null) {
                        println("!!! Cannot find material locale for $key, ignoring.")
                        continue
                    }

                    items.getAsJsonObject(key).addProperty("name", "$version $locale")
                }
            }

            val entitynames = jsonObject.getAsJsonObject("entitynames")
            if (entitynames != null) {
                for (key in entitynames.keySet()) {
                    val locale = localeMap["entity.minecraft.$key"]
                    if (locale == null) {
                        println("!!! Cannot find entity locale for $key, ignoring.")
                        continue
                    }
                    entitynames.addProperty(key, locale)
                }
            }

            val result = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement)
            file.writer(Charsets.UTF_8).use {
                it.write(result)
            }

        }
    }

}