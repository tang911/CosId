package me.ahoo.cosid.spring.boot.starter;

import me.ahoo.cosid.converter.PrefixIdConverter;
import me.ahoo.cosid.converter.Radix62IdConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * IdConverterDefinitionTest .
 *
 * @author ahoo wang
 */
class IdConverterDefinitionTest {
    
    @Test
    void getType() {
        IdConverterDefinition definition = new IdConverterDefinition();
        Assertions.assertEquals(IdConverterDefinition.Type.RADIX, definition.getType());
    }
    
    @Test
    void setType() {
        IdConverterDefinition definition = new IdConverterDefinition();
        definition.setType(IdConverterDefinition.Type.SNOWFLAKE_FRIENDLY);
        Assertions.assertEquals(IdConverterDefinition.Type.SNOWFLAKE_FRIENDLY, definition.getType());
    }
    
    @Test
    void getPrefix() {
        IdConverterDefinition definition = new IdConverterDefinition();
        Assertions.assertEquals(PrefixIdConverter.EMPTY_PREFIX, definition.getPrefix());
    }
    
    @Test
    void setPrefix() {
        String prefix = "test";
        IdConverterDefinition definition = new IdConverterDefinition();
        definition.setPrefix(prefix);
        Assertions.assertEquals(prefix, definition.getPrefix());
    }
    
    @Test
    void getRadix() {
        IdConverterDefinition definition = new IdConverterDefinition();
        Assertions.assertNotNull(definition.getRadix());
        Assertions.assertTrue(definition.getRadix().isPadStart());
        Assertions.assertEquals(Radix62IdConverter.MAX_CHAR_SIZE, definition.getRadix().getCharSize());
    }
    
    @Test
    void setRadix() {
        IdConverterDefinition definition = new IdConverterDefinition();
        IdConverterDefinition.Radix radix = new IdConverterDefinition.Radix();
        radix.setPadStart(false);
        radix.setCharSize(10);
        definition.setRadix(radix);
        Assertions.assertNotNull(definition.getRadix());
        Assertions.assertFalse(definition.getRadix().isPadStart());
        Assertions.assertEquals(10, definition.getRadix().getCharSize());
    }
}
