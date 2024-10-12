package dev.wuason.unearthMechanic.config

/**
 * Interface representing a sound entity that can be associated with various game objects.
 */
interface ISound {
    /**
     * Represents the identifier for a specific sound.
     *
     * This identifier is used to fetch and play the appropriate sound
     * within various events and features in the system.
     * Implementations of the ISound interface use soundId along with
     * other properties like `volume`, `pitch`, and `delay` to control
     * audio behavior.
     */
    val soundId: String
    /**
     * Represents the volume level for a given sound.
     * The volume is a floating-point value typically ranging from 0.0 (muted) to 1.0 (full volume),
     * but it can go beyond this range depending on the implementation of the sound system.
     *
     * This property is used to control the playback intensity of sounds.
     */
    val volume: Float
    /**
     * Represents the pitch value for a sound. This value is used to adjust the pitch (frequency)
     * of the sound effect being played. It typically ranges from 0.5 (half the normal pitch)
     * to 2.0 (double the normal pitch).
     */
    val pitch: Float
    /**
     * The delay in milliseconds before a sound is played.
     *
     * This variable is used to specify how long to wait before a sound associated with a particular event is played in the game.
     */
    val delay: Long
}