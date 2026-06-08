package com.simplegens.input;

public class InputContext {
    private final InputType type;
    private final String generatorId;
    private final Object contextObject; // Can be a BlockBlueprint or null
    private final Runnable onComplete;

    public InputContext(InputType type, String generatorId, Object contextObject, Runnable onComplete) {
        this.type = type;
        this.generatorId = generatorId;
        this.contextObject = contextObject;
        this.onComplete = onComplete;
    }

    public InputType getType() {
        return type;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public Object getContextObject() {
        return contextObject;
    }

    public Runnable getOnComplete() {
        return onComplete;
    }
}
