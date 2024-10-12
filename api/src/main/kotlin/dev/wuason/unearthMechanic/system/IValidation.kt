package dev.wuason.unearthMechanic.system

interface IValidation {
    /**
     * Initiates the validation process.
     *
     * This function is responsible for starting or triggering the
     * validation sequence defined in the implementation of the IValidation interface.
     */
    fun start()
    /**
     * Checks if the current state is valid according to the implemented validation rules.
     *
     * @return Boolean indicating whether the current state is valid
     */
    fun isValid(): Boolean
    /**
     * Validates the current state according to the implemented validation rules.
     *
     * @return true if the state is valid, false otherwise.
     */
    fun validate(): Boolean
}