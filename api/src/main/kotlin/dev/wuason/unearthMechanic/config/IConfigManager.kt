package dev.wuason.unearthMechanic.config

/**
 * Represents a manager responsible for handling configuration settings.
 * Provides methods to load configurations, validate base item IDs and tools, and retrieve generics data.
 */
interface IConfigManager {
    /**
     * Loads the configuration settings from a predefined source.
     *
     * This method is typically called to initialize or refresh the configuration data used by the application.
     * Implementations may vary in terms of where and how they load the configuration, such as from
     * a file, a database, or an external service.
     */
    fun loadConfig()

    /**
     * Retrieves generics information as a map where the key is a string identifier and the value is an IGeneric object.
     *
     * @return A HashMap where the keys are String identifiers for generics and the values are IGeneric objects.
     */
    fun getGenerics(): HashMap<String, IGeneric>

    /**
     * Retrieves a mapping of base item IDs to their respective generic tool information.
     *
     * @return A HashMap where the key is a String representing the base item ID, and the value is another HashMap.
     *         This inner HashMap pairs String tool IDs with their corresponding IGeneric implementations.
     */
    fun getGenericsBaseItemId(): HashMap<String, HashMap<String, IGeneric>>

    /**
     * Checks if the given baseItemId is valid.
     *
     * @param baseItemId the ID of the base item to be validated
     * @return true if the baseItemId is valid, otherwise false
     */
    fun validBaseItemId(baseItemId: String): Boolean

    /**
     * Checks if the given tool is valid for the specified base item.
     *
     * @param baseItemId the ID of the base item to check against
     * @param tool the tool to validate
     * @return true if the tool is valid for the given base item, false otherwise
     */
    fun validTool(baseItemId: String, tool: String): Boolean

    /**
     * Retrieves a generic item associated with the provided base item ID and tool.
     *
     * @param baseItemId The ID of the base item to retrieve.
     * @param tool The tool associated with the generic item to retrieve.
     * @return The IGeneric item corresponding to the base item ID and tool, or null if not found.
     */
    fun getGeneric(baseItemId: String, tool: String): IGeneric?
}