package dev.wuason.unearthMechanic.config

/**
 * Represents a generic interface that provides various methods to access and manage generic items.
 */
interface IGeneric {

    /**
     * Retrieves the unique identifier associated with this instance.
     *
     * @return the ID as a string
     */
    fun getId(): String

    /**
     * Retrieves the set of tools associated with the generic item.
     *
     * @return A `Set` of `ITool` instances representing the tools.
     */
    fun getTools(): Set<ITool>

    /**
     * Retrieves the base stage of the current instance.
     *
     * @return An instance of `IStage` representing the base stage.
     */
    fun getBaseStage(): IStage

    /**
     * Retrieves the list of stages associated with the current object.
     *
     * @return A list of instances implementing the IStage interface.
     */
    fun getStages(): List<IStage>

    /**
     * Retrieves a hashmap mapping stage item IDs to their corresponding IStage instances.
     *
     * @return A hashmap where the keys are stage item IDs (String) and the values are IStage instances.
     */
    fun getStagesItemsId(): HashMap<String, IStage>

    /**
     * Retrieves an ITool instance based on the provided tool ID.
     *
     * @param toolId The unique identifier of the tool to retrieve.
     * @return The ITool instance if found, or null otherwise.
     */
    fun getTool(toolId: String): ITool?

    /**
     * Checks if a tool with the specified ID exists within the collection.
     *
     * @param toolId the unique identifier of the tool to be checked
     * @return true if the tool exists, false otherwise
     */
    fun existsTool(toolId: String): Boolean

    /**
     * Determines if the specified stage is the last stage.
     *
     * @param stage The stage to check.
     * @return True if the stage is the last stage; otherwise, false.
     */
    fun isLastStage(stage: IStage): Boolean

    /**
     * Checks if the current instance should not be protected.
     *
     * @return true if the instance is not protected, false otherwise.
     */
    fun isNotProtect(): Boolean
}