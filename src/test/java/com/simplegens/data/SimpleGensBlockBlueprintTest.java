package com.simplegens.data;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleGensBlockBlueprintTest {

    @Test
    void serializeAndDeserializeRoundTrip() {
        SimpleGensBlockBlueprint blueprint = new SimpleGensBlockBlueprint(1, 2, 3, Material.STONE);
        String serialized = blueprint.serialize();

        SimpleGensBlockBlueprint result = SimpleGensBlockBlueprint.deserialize(serialized);

        assertNotNull(result);
        assertEquals(1, result.getX());
        assertEquals(2, result.getY());
        assertEquals(3, result.getZ());
        assertEquals(Material.STONE, result.getMaterial());
    }

    @Test
    void deserializeReturnsNullForWrongFormat() {
        assertNull(SimpleGensBlockBlueprint.deserialize("invalid-format"));
        assertNull(SimpleGensBlockBlueprint.deserialize("1,2:STONE"));
    }
}
