package dev.wuason.unearthMechanic.config

import dev.wuason.libs.boostedyaml.YamlDocument
import dev.wuason.libs.boostedyaml.block.implementation.Section
import dev.wuason.libs.boostedyaml.settings.dumper.DumperSettings
import dev.wuason.libs.boostedyaml.settings.general.GeneralSettings
import dev.wuason.libs.boostedyaml.settings.loader.LoaderSettings
import dev.wuason.libs.boostedyaml.settings.updater.UpdaterSettings
import dev.wuason.mechanics.utils.AdventureUtils
import dev.wuason.mechanics.utils.Utils
import dev.wuason.unearthMechanic.UnearthMechanic
import java.io.File
import java.util.Locale

class ConfigManager(private val core: UnearthMechanic) : IConfigManager {

    private val generics: HashMap<String, IGeneric> = HashMap()
    private val genericsBaseItemId: HashMap<String, HashMap<String, IGeneric>> = HashMap()

    override fun loadConfig() {
        generics.clear()
        genericsBaseItemId.clear()

        loadBlockConfig()
        loadFurnitureConfig()
    }

    fun loadBlockConfig() {
        val base = File(core.dataFolder.path)
        base.mkdirs()
        val files: Array<File> = base.listFiles { file: File -> file.name.endsWith(".yml") } ?: return

        for (file in files) {

            val config = YamlDocument.create(file, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)

            config.getSection("unearth.block")?.let { sectionBlocks ->

                for (key in sectionBlocks.getRoutesAsStrings(false)) {

                    val sectionBlock: Section = sectionBlocks.getSection(key) ?: continue
                    val id: String = key
                    val baseItemId: String = sectionBlock.getString("base") ?: continue
                    val tools: Set<String> = sectionBlock.getStringList("tool", listOf("mc:air")).toSet()

                    val stages: MutableList<IStage> = mutableListOf()

                    val sectionStages: Section = sectionBlock.getSection("transformation.stages") ?: continue

                    for (keyStage in sectionStages.getRoutesAsStrings(false)) {


                        val sectionStage: Section = sectionStages.getSection(keyStage) ?: continue

                        val blockId: String? = sectionStage.getString("block_id")
                        val remove: Boolean = sectionStage.getBoolean("remove", false)
                        val drops: List<Drop> = sectionStage.getStringList("drops", emptyList()).map {
                            val split: List<String> = it.split(";")
                            Drop(split[0], split[1], split[2].toInt())
                        }
                        val removeItemMainHand: Boolean = sectionStage.getBoolean("remove_item_main_hand", false)
                        val durabilityToRemove = sectionStage.getInt("reduce_durability", 0)
                        val usagesIaToRemove = sectionStage.getInt("reduce_usages_ia", 0)
                        val onlyOneDrop = sectionStage.getBoolean("only_one_drop", false)
                        val stage: Stage = Stage(
                            stages.size,
                            blockId,
                            drops,
                            remove,
                            removeItemMainHand,
                            durabilityToRemove,
                            usagesIaToRemove,
                            onlyOneDrop
                        )

                        stages.add(stage)

                    }

                    val block: Block = Block(id, tools, baseItemId, stages)


                    generics[id] = block
                    block.getTools().forEach { tool: String -> putTool(baseItemId, tool, block) }

                }

            }

        }

        AdventureUtils.sendMessagePluginConsole(core,
            "<aqua> Blocks loaded: <yellow>" + generics.values.stream().filter { generic: IGeneric -> generic is IBlock }
                .count())

    }

    fun loadFurnitureConfig() {

        val base: File = File(core.dataFolder.path)
        base.mkdirs()
        val files: Array<File> = base.listFiles { file: File -> file.name.endsWith(".yml") } ?: return

        for (file in files) {

            val config = YamlDocument.create(file, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)

            config.getSection("unearth.furniture")?.let { sectionFurnitures ->

                for (key in sectionFurnitures.getRoutesAsStrings(false)) {

                    val sectionFurniture: Section = sectionFurnitures.getSection(key) ?: continue
                    val id = key
                    val baseItemId = sectionFurniture.getString("base") ?: continue
                    val tools = sectionFurniture.getStringList("tool", listOf("mc:air")).toSet()
                    val stages: MutableList<IStage> = mutableListOf()
                    val sectionStages = sectionFurniture.getSection("transformation.stages") ?: continue
                    for (keyStage in sectionStages.getRoutesAsStrings(false)) {
                        val sectionStage = sectionStages.getSection(keyStage) ?: continue
                        val furnitureId: String? = sectionStage.getString("furniture_id")
                        val remove = sectionStage.getBoolean("remove", false)
                        val drops = sectionStage.getStringList("drops", emptyList()).map {
                            val split = it.split(";")
                            Drop(split[0], split[1], split[2].toInt())
                        }
                        val removeItemMainHand = sectionStage.getBoolean("remove_item_main_hand", false)
                        val durabilityToRemove = sectionStage.getInt("reduce_durability", 0)
                        val usagesIaToRemove = sectionStage.getInt("reduce_usages_ia", 0)
                        val onlyOneDrop = sectionStage.getBoolean("only_one_drop", false)
                        val stage = Stage(
                            stages.size,
                            furnitureId,
                            drops,
                            remove,
                            removeItemMainHand,
                            durabilityToRemove,
                            usagesIaToRemove,
                            onlyOneDrop
                        )
                        stages.add(stage)
                    }
                    val furniture = Furniture(id, tools, baseItemId, stages)
                    generics[id] = furniture

                    furniture.getTools().forEach { tool: String -> putTool(baseItemId, tool, furniture) }

                }

            }
        }


        AdventureUtils.sendMessagePluginConsole(core,
            "<aqua> Furniture loaded: <yellow>" + generics.values.stream().filter { generic: IGeneric -> generic is IFurniture }
                .count())
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
        return genericsBaseItemId.containsKey(baseItemId) && genericsBaseItemId[baseItemId]?.containsKey(if ( tool.equals("HAND", true) ) tool.uppercase(Locale.ENGLISH) else tool) ?: false
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
}