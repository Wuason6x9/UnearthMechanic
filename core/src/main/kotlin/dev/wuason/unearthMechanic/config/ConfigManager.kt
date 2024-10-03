package dev.wuason.unearthMechanic.config

import dev.wuason.libs.boostedyaml.YamlDocument
import dev.wuason.libs.boostedyaml.block.implementation.Section
import dev.wuason.libs.boostedyaml.settings.dumper.DumperSettings
import dev.wuason.libs.boostedyaml.settings.general.GeneralSettings
import dev.wuason.libs.boostedyaml.settings.loader.LoaderSettings
import dev.wuason.libs.boostedyaml.settings.updater.UpdaterSettings
import dev.wuason.mechanics.utils.AdventureUtils
import dev.wuason.unearthMechanic.UnearthMechanic
import java.io.File
import java.lang.reflect.Constructor
import java.util.Locale

class ConfigManager(private val core: UnearthMechanic) : IConfigManager {

    private val generics: HashMap<String, IGeneric> = HashMap()
    private val genericsBaseItemId: HashMap<String, HashMap<String, IGeneric>> = HashMap()

    override fun loadConfig() {
        generics.clear()
        genericsBaseItemId.clear()
        loadConfig(GenericType.BLOCK)
        loadConfig(GenericType.FURNITURE)
    }

    private fun getAllFilesRecursive(file: File): List<File> {
        val files = mutableListOf<File>()
        file.listFiles()?.forEach {
            if (it.isDirectory) {
                files.addAll(getAllFilesRecursive(it))
            } else {
                files.add(it)
            }
        }
        return files
    }

    fun loadConfig(type: GenericType) {
        val base = File(core.dataFolder.path)
        base.mkdirs()
        val files: List<File> = getAllFilesRecursive(base).filter { it.name.endsWith(".yml") }
        for (file in files) {

            val config = YamlDocument.create(file, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)

            config.getSection("unearth.${type.getRoute()}")?.let { sectionGenerics ->

                for (key in sectionGenerics.getRoutesAsStrings(false)) {

                    val sectionGeneric: Section = sectionGenerics.getSection(key) ?: continue
                    val id: String = key
                    val basesItemId: ArrayList<String> = ArrayList()
                    val baseItemId = sectionGeneric.get("base")?: continue
                    if (baseItemId is String) {
                        basesItemId.add(baseItemId)
                    } else if (baseItemId is List<*>) {
                        basesItemId.addAll(baseItemId as List<String>)
                    }

                    val tools: Set<ITool> = sectionGeneric.getStringList("tool", listOf("mc:air")).map { Tool.parseTool(it) }.toSet()

                    val stages: MutableList<IStage> = mutableListOf()

                    val sectionStages: Section = sectionGeneric.getSection("transformation.stages") ?: continue

                    for (keyStage in sectionStages.getRoutesAsStrings(false)) {


                        val sectionStage: Section = sectionStages.getSection(keyStage) ?: continue

                        val itemStageId: String? = sectionStage.getString("${type.getRoute()}_id")
                        val remove: Boolean = sectionStage.getBoolean("remove", false)
                        val drops: List<Drop> = sectionStage.getStringList("drops", emptyList()).map {
                            val split: List<String> = it.split(";")
                            Drop(split[0], split[1], split[2].toInt())
                        }
                        val removeItemMainHand: Boolean = sectionStage.getBoolean("remove_item_main_hand", false)
                        val durabilityToRemove = sectionStage.getInt("reduce_durability", 0)
                        val usagesIaToRemove = sectionStage.getInt("reduce_usages_ia", 0)
                        val onlyOneDrop = sectionStage.getBoolean("only_one_drop", false)
                        val onlyOneItem = sectionStage.getBoolean("only_one_add", false)
                        val reduceItemMainHand: Int = sectionStage.getInt("reduce_item_main_hand", 0)
                        val delay: Long = sectionStage.getLong("delay", 0)
                        val toolAnimDelay = sectionStage.getBoolean("tool_anim_on_delay", false)
                        val items: List<Item> = sectionStage.getStringList("items_add", emptyList()).map {
                            val split: List<String> = it.split(";")
                            Item(split[0], split[1], split[2].toInt())
                        }

                        val sounds: List<Sound> = sectionStage.getMapList("sounds", emptyList()).filter {
                            it.containsKey("sound") && it["sound"] is String && (it["sound"] as String).isNotBlank()
                        }.map {
                            val sound = it["sound"] as String
                            val volume = it.getOrDefault("volume", 1.0) as Number
                            val pitch = it.getOrDefault("pitch", 1.0) as Number
                            val delay = it.getOrDefault("delay", 0) as Number
                            Sound(sound, volume.toFloat(), pitch.toFloat(), delay.toLong())
                        }
                        val stage: Stage = Stage(
                            stages.size,
                            itemStageId,
                            drops,
                            remove,
                            removeItemMainHand,
                            durabilityToRemove,
                            usagesIaToRemove,
                            onlyOneDrop,
                            reduceItemMainHand,
                            items,
                            onlyOneItem,
                            sounds,
                            delay,
                            toolAnimDelay
                        )

                        stages.add(stage)

                    }

                    for ((i, baseItemId) in basesItemId.withIndex()) {
                        var cid = getCorrectId(id, baseItemId)
                        if (basesItemId.size > 1) {
                            if (cid.equals(id)) {
                                cid = "${id}_${i}"
                            }
                        }

                        val constructor: Constructor<*> = type.getClazz().declaredConstructors[0]

                        val generic: IGeneric = constructor.newInstance(cid, tools, if (baseItemId.contains(";")) baseItemId.substring(0, baseItemId.indexOf(';')) else baseItemId, stages) as IGeneric

                        generics[generic.getId()] = generic

                        generic.getTools().forEach { tool: ITool -> putTool(generic.getBaseItemId(), tool.getItemId(), generic) }
                    }

                }

            }

        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> ${type.getName()} loaded: <yellow>${generics.count { type.getClazz().isInstance(it.value) }}")
    }

    private fun putTool(baseItemId: String, tool: String, generic: IGeneric) {
        if (!genericsBaseItemId.containsKey(baseItemId)) {
            genericsBaseItemId[baseItemId] = HashMap()
        }
        val map: HashMap<String, IGeneric> = genericsBaseItemId[baseItemId] ?: return
        if (!map.containsKey(tool)) {
            map[tool] = generic
        }
    }

    override fun validTool(baseItemId: String, tool: String): Boolean {
        return genericsBaseItemId.containsKey(baseItemId) && genericsBaseItemId[baseItemId]?.containsKey(tool) ?: false
    }

    override fun validBaseItemId(baseItemId: String): Boolean {
        return genericsBaseItemId.containsKey(baseItemId)
    }

    override fun getGeneric(baseItemId: String, tool: String): IGeneric? {
        if (!validTool(baseItemId, tool)) return null
        return genericsBaseItemId[baseItemId]?.get(tool)
    }

    override fun getGenerics(): HashMap<String, IGeneric> {
        return generics
    }

    override fun getGenericsBaseItemId(): HashMap<String, HashMap<String, IGeneric>> {
        return genericsBaseItemId
    }

    private fun getCorrectId(id: String, baseItemId: String): String {
        val split = baseItemId.split(";")
        return if (split.size >= 2 && split[1].isNotBlank()) "${id}_${split[1]}" else id
    }

    enum class GenericType(private val route: String, private val clazz: Class<out Generic>) {
        BLOCK("block", Block::class.java),
        FURNITURE("furniture", Furniture::class.java);

        fun getName(): String {
            return route.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
        }

        fun getRoute(): String {
            return route
        }

        fun getClazz(): Class<out Generic> {
            return clazz
        }
    }
}