package dev.wuason.unearthMechanic.system

interface IValidation {
    fun start()
    fun isValid(): Boolean
    fun validate(): Boolean
}