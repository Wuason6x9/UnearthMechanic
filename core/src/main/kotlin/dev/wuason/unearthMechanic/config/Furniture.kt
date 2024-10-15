package dev.wuason.unearthMechanic.config

class Furniture(id: String, tools: Set<ITool>, baseItemId: String, stages: List<IStage>, notProtected: Boolean):
    Generic(id, tools, baseItemId, stages, notProtected), IFurniture