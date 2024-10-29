package dev.wuason.unearthMechanic.config

class Furniture(id: String, tools: Set<ITool>, baseStage: FurnitureStage, stages: List<IStage>, notProtected: Boolean):
    Generic(id, tools, baseStage, stages, notProtected), IFurniture