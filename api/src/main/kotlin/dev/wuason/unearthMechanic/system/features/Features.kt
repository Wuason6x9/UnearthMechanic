package dev.wuason.unearthMechanic.system.features

class Features {
    companion object {
        private val features: MutableList<Feature> = mutableListOf()

        fun registerFeature(feature: Feature) {
            features.add(feature)
        }

        fun unregisterFeature(feature: Feature) {
            features.remove(feature)
        }

        fun getFeatures(): List<Feature> {
            return features
        }

    }
}