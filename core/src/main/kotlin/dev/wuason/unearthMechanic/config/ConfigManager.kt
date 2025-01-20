package dev.wuason.unearthMechanic.config

import dev.wuason.libs.adapter.Adapter
import dev.wuason.libs.adapter.AdapterData
import dev.wuason.libs.boostedyaml.YamlDocument
import dev.wuason.libs.boostedyaml.block.implementation.Section
import dev.wuason.libs.boostedyaml.settings.dumper.DumperSettings
import dev.wuason.libs.boostedyaml.settings.general.GeneralSettings
import dev.wuason.libs.boostedyaml.settings.loader.LoaderSettings
import dev.wuason.libs.boostedyaml.settings.updater.UpdaterSettings
import dev.wuason.mechanics.utils.AdventureUtils
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.utils.Utils.Companion.toAdapter
import java.io.File
import java.lang.reflect.Constructor
import java.util.Locale
import kotlin.jvm.optionals.getOrNull

class ConfigManager(private val core: UnearthMechanic) : IConfigManager {

    private val generics: HashMap<String, IGeneric> = HashMap()
    private val genericsBaseItemId: HashMap<AdapterData, HashMap<AdapterData, IGeneric>> = HashMap()

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

                    val notProtected: Boolean = sectionGeneric.getBoolean("no_protect", false)

                    val tools: Set<ITool> = sectionGeneric.getStringList("tool", listOf("mc:air")).map { Tool.parseTool(it) }.toSet()

                    val stages: MutableList<IStage> = mutableListOf()

                    val sectionStages: Section = sectionGeneric.getSection("transformation.stages") ?: continue

                    for (keyStage in sectionStages.getRoutesAsStrings(false)) {


                        val sectionStage: Section = sectionStages.getSection(keyStage) ?: continue
                        var stageType: StageType = StageType.valueOf(type.name.uppercase(Locale.ENGLISH))
                        var itemStageId: String? = null
                        for (t in StageType.values()) {
                            sectionStage.getString("${t.getRoute()}_id")?.let {
                                stageType = t
                                itemStageId = it
                            }
                        }

                        val stageAdapterData: AdapterData? = itemStageId?.let { Adapter.getAdapterData(it).getOrNull() }

                        val remove: Boolean = sectionStage.getBoolean("remove", false)
                        val drops: List<Drop> = sectionStage.getStringList("drops", emptyList()).map {
                            val split: List<String> = it.split(";")
                            Drop(Adapter.getAdapterData(split[0]).getOrNull()?: throw NullPointerException("The adapter id: ${split[0]} is not valid!"), split[1], split[2].toInt())
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
                            Item(Adapter.getAdapterData(split[0]).getOrNull()?: throw NullPointerException("The adapter id: ${split[0]} is not valid!"), split[1], split[2].toInt())
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
                        val stage: Stage = stageType?.let {
                            stageType.getClazz().declaredConstructors[0].newInstance(
                                stages.size,
                                stageAdapterData,
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
                            ) as Stage
                        }?: Stage(
                            stages.size,
                            stageAdapterData,
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

                    for ((i, baseItemId1) in basesItemId.withIndex()) {
                        var cid = getCorrectId(id, baseItemId1)
                        if (basesItemId.size > 1) {
                            if (cid.equals(id)) {
                                cid = "${id}_${i}"
                            }
                        }

                        val constructor: Constructor<*> = type.getClazz().declaredConstructors[0]

                        val baseStage: Stage = StageType.valueOf(type.name.uppercase(Locale.ENGLISH)).getClazz().declaredConstructors[0].newInstance(
                            -1,
                            if (baseItemId1.contains(";")) baseItemId1.substring(0, baseItemId1.indexOf(';')).toAdapter() else baseItemId1.toAdapter(),
                            listOf<Drop>(),
                            false,
                            false,
                            0,
                            0,
                            false,
                            0,
                            listOf<Item>(),
                            false,
                            listOf<Sound>(),
                            0,
                            false
                        ) as Stage

                        val generic: IGeneric = constructor.newInstance(cid, tools, baseStage, stages, notProtected) as IGeneric

                        generics[generic.getId()] = generic

                        generic.getTools().forEach { tool: ITool -> putTool(generic.getBaseStage().getAdapterData()!!, tool.getAdapterData(), generic) }
                    }

                }

            }

        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> ${type.getName()} loaded: <yellow>${generics.count { type.getClazz().isInstance(it.value) }}")
    }

    private fun putTool(baseAdapterData: AdapterData, tool: AdapterData, generic: IGeneric) {
        if (!genericsBaseItemId.containsKey(baseAdapterData)) {
            genericsBaseItemId[baseAdapterData] = HashMap()
        }
        genericsBaseItemId[baseAdapterData]?.set(tool, generic)
    }

    override fun validTool(baseAdapterData: AdapterData, tool: AdapterData): Boolean {
        return genericsBaseItemId.containsKey(baseAdapterData) && genericsBaseItemId[baseAdapterData]?.containsKey(tool) ?: false
    }

    override fun validBaseItemId(baseAdapterData: AdapterData): Boolean {
        return genericsBaseItemId.containsKey(baseAdapterData)
    }

    override fun getGeneric(baseAdapterData: AdapterData, tool: AdapterData): IGeneric? {
        if (!validTool(baseAdapterData, tool)) return null
        return genericsBaseItemId[baseAdapterData]?.get(tool)
    }

    override fun getGenerics(): HashMap<String, IGeneric> {
        return generics
    }

    override fun getGenericsBaseItemId(): HashMap<AdapterData, HashMap<AdapterData, IGeneric>> {
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

    enum class StageType(private val route: String, private val clazz: Class<out Stage>) {
        BLOCK("block", BlockStage::class.java),
        FURNITURE("furniture", FurnitureStage::class.java);

        fun getName(): String {
            return route.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
        }

        fun getRoute(): String {
            return route
        }

        fun getClazz(): Class<out Stage> {
            return clazz
        }
    }
}