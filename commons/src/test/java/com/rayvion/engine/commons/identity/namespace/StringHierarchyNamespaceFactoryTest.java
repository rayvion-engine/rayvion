package com.rayvion.engine.commons.identity.namespace;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StringHierarchyNamespaceFactoryTest {

    @Test
    void testParseStandardHierarchy() {
        String input = "com.rayvion.engine";
        HierarchyNamespace<String> result = StringHierarchyNamespaceFactory.parse(input);
        
        assertNotNull(result);
        assertEquals(List.of("com", "rayvion", "engine"), result.hierarchy());
    }

    @Test
    void testParseSingleElement() {
        String input = "single";
        HierarchyNamespace<String> result = StringHierarchyNamespaceFactory.parse(input);
        
        assertNotNull(result);
        assertEquals(List.of("single"), result.hierarchy());
    }

    @Test
    void testParseEmptyString() {
        String input = "";
        HierarchyNamespace<String> result = StringHierarchyNamespaceFactory.parse(input);
        
        assertNotNull(result);
        // split("") returns [""] in Java, which might be surprising but that's how it works with ""
        assertEquals(List.of(""), result.hierarchy());
    }
}
