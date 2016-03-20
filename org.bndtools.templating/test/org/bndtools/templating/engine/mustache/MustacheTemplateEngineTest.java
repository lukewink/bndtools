package org.bndtools.templating.engine.mustache;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bndtools.templating.FolderResource;
import org.bndtools.templating.ResourceMap;
import org.bndtools.templating.StringResource;
import org.junit.Test;

import aQute.lib.io.IO;

public class MustacheTemplateEngineTest {

    @Test
    public void testBasic() throws Exception {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        ResourceMap input = new ResourceMap();
        input.put("{{srcDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/package-info.java", new StringResource("package {{packageName}};"));

        Map<String,List<Object>> params = new HashMap<>();
        params.put("srcDir", Collections.<Object> singletonList("src"));
        params.put("packageDir", Collections.<Object> singletonList("org/example/foo"));
        params.put("packageName", Collections.<Object> singletonList("org.example.foo"));
        ResourceMap output = engine.generateOutputs(input, params);

        assertEquals(3, output.size());
        assertEquals("package org.example.foo;", IO.collect(output.get("src/org/example/foo/package-info.java").getContent()));
    }

    @Test
    public void testIgnore() throws Exception {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        ResourceMap input = new ResourceMap();
        input.put("_template.properties", new StringResource("ignore=*/donotcopy.*"));
        input.put("{{srcDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/package-info.java", new StringResource("package {{packageName}};"));
        input.put("{{srcDir}}/{{packageDir}}/donotcopy.txt", new StringResource(""));

        Map<String,List<Object>> params = new HashMap<>();
        params.put("srcDir", Collections.<Object> singletonList("src"));
        params.put("packageDir", Collections.<Object> singletonList("org/example/foo"));
        params.put("packageName", Collections.<Object> singletonList("org.example.foo"));
        ResourceMap output = engine.generateOutputs(input, params);

        assertEquals(3, output.size());
        assertEquals("package org.example.foo;", IO.collect(output.get("src/org/example/foo/package-info.java").getContent()));
        assertNull(output.get("src/org/example/foo/donotcopy.txt"));
    }

    @Test
    public void testNoProcessDefaultPattern() throws Exception {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        ResourceMap input = new ResourceMap();
        input.put("{{srcDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/package-info.java", new StringResource("package {{packageName}};"));
        input.put("{{srcDir}}/{{packageDir}}/package-info.jpg", new StringResource("package {{packageName}};"));

        Map<String,List<Object>> params = new HashMap<>();
        params.put("srcDir", Collections.<Object> singletonList("src"));
        params.put("packageDir", Collections.<Object> singletonList("org/example/foo"));
        params.put("packageName", Collections.<Object> singletonList("org.example.foo"));
        ResourceMap output = engine.generateOutputs(input, params);

        assertEquals(4, output.size());
        assertEquals("package org.example.foo;", IO.collect(output.get("src/org/example/foo/package-info.java").getContent()));
        assertEquals("package {{packageName}};", IO.collect(output.get("src/org/example/foo/package-info.jpg").getContent()));
    }

    @Test
    public void testNoProcessExtendedPattern() throws Exception {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        ResourceMap input = new ResourceMap();
        input.put("_template.properties", new StringResource("process.before=!*.java"));
        input.put("{{srcDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/", new FolderResource());
        input.put("{{srcDir}}/{{packageDir}}/package-info.java", new StringResource("package {{packageName}};"));

        Map<String,List<Object>> params = new HashMap<>();
        params.put("srcDir", Collections.<Object> singletonList("src"));
        params.put("packageDir", Collections.<Object> singletonList("org/example/foo"));
        params.put("packageName", Collections.<Object> singletonList("org.example.foo"));
        ResourceMap output = engine.generateOutputs(input, params);

        assertEquals(3, output.size());
        assertEquals("package {{packageName}};", IO.collect(output.get("src/org/example/foo/package-info.java").getContent()));
    }

    @Test
    public void testAlternativeDelimiters() throws Exception {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        ResourceMap input = new ResourceMap();
        input.put("readme.txt", new StringResource("{{=\u00ab \u00bb=}}Unprocessed: {{packageName}}. Processed: \u00abpackageName\u00bb"));

        Map<String,List<Object>> params = new HashMap<>();
        params.put("packageName", Collections.<Object> singletonList("org.example.foo"));
        ResourceMap output = engine.generateOutputs(input, params);

        assertEquals(1, output.size());
        assertEquals("Unprocessed: {{packageName}}. Processed: org.example.foo", IO.collect(output.get("readme.txt").getContent()));
    }

    @Test
    public void testGetParamNames() throws Exception {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        ResourceMap input = new ResourceMap();
        input.put("readme.txt", new StringResource("Blah {{fish}} blah {{battleship}} blah {{antidisestablishmentarianism}}"));

        Collection<String> names = engine.getTemplateParameterNames(input);
        assertTrue(names.contains("fish"));
        assertTrue(names.contains("battleship"));
        assertTrue(names.contains("antidisestablishmentarianism"));
    }

}
