package dev.wuason.unearthMechanic.config

class Furniture(id: String, tools: Set<String>, baseItemId: String, stages: List<IStage>) :
    Generic(id, tools, baseItemId, stages), IFurniture