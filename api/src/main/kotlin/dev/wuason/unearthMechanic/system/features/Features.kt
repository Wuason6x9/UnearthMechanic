package dev.wuason.unearthMechanic.system.features

/**
 * A class that manages a collection of features.
 */
class Features {
    /**
     * Companion object for managing features within the Features class.
     * It provides functionalities to register, unregister, and retrieve features.
     */
    companion object {
        /**
         * A mutable list that holds registered features.
         *
         * This list maintains the currently active features within the application.
         * The list can be modified by adding or removing features through
         * `registerFeature` and `unregisterFeature` methods.
         *
         * The contents of this list can be retrieved via the `getFeatures` method.
         */
        private val features: MutableList<AbstractFeature> = mutableListOf()

        /**
         * Registers a feature to be available within the system.
         *
         * @param feature The feature to be registered.
         */
        fun registerFeature(feature: AbstractFeature) {
            features.add(feature)
        }

        /**
         * Unregisters a feature from the list of registered features.
         *
         * @param feature The feature to be unregistered.
         */
        fun unregisterFeature(feature: AbstractFeature) {
            features.remove(feature)
        }

        /**
         * Retrieves the list of currently registered features.
         *
         * @return A list of registered features.
         */
        fun getFeatures(): List<AbstractFeature> {
            return features
        }

    }
}