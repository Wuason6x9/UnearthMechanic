package dev.wuason.unearthMechanic.config

data class Sound(
    override val soundId: String,
    override val volume: Float,
    override val pitch: Float,
    override val delay: Long
) : ISound {
    override fun toString(): String {
        return "Sound(soundId='$soundId', volume=$volume, pitch=$pitch, delay=$delay)"
    }
}