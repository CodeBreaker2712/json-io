package com.cedarsoftware.io;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class URLTest
{

    private static final String LOCALHOST = "http://localhost";
    private static final String OUTSIDE_DOMAIN = "https://foo.bar.com";
    
    private static Stream<Arguments> argumentsForUrlTesting() {
        return Stream.of(
                Arguments.of("https://domain.com"),
                Arguments.of("http://localhost"),
                Arguments.of("http://localhost:8080"),
                Arguments.of("http://localhost:8080/file/path"),
                Arguments.of("http://localhost:8080/path/file.html"),
                Arguments.of("http://localhost:8080/path/file.html?foo=1&bar=2"),
                Arguments.of("http://localhost:8080/path/file.html?foo=bar&qux=quy#AnchorLocation"),
                Arguments.of("https://foo.bar.com/"),
                Arguments.of("https://foo.bar.com/path/foo%20bar.html"),
                Arguments.of("https://foo.bar.com/path/file.html?text=Hello+G%C3%BCnter"),
                Arguments.of("ftp://user@bar.com/foo/bar.txt"),
                Arguments.of("ftp://user:password@host/foo/bar.txt"),
                Arguments.of("ftp://user:password@host:8192/foo/bar.txt"),
                Arguments.of("file:/path/to/file"),
                Arguments.of("file://localhost/path/to/file.json"),
                Arguments.of("file://servername/path/to/file.json"),
                Arguments.of("jar:file:/c://my.jar!/"),
                Arguments.of("jar:file:/c://my.jar!/com/mycompany/MyClass.class")
        );
    }
    @ParameterizedTest
    @MethodSource("argumentsForUrlTesting")
    void testToString_withDifferentUrls_works(String input) throws Exception {
        URL url = new URL(input);
        assertThat(url.toString()).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("argumentsForUrlTesting")
    void testSerialization_withDifferentUrls_works(String input) throws Exception {
        URL url = new URL(input);
        String json = TestUtil.toJson(url);

        TestUtil.printLine("json=" + json);
        Object read = TestUtil.toObjects(json, null);
        assertThat(read).isEqualTo(url);
    }

    @Test
    void testURL_hasTypeAndUrl_and_noOtherUrlParams() throws Exception {
        URL url = new URL(OUTSIDE_DOMAIN);
        String json = TestUtil.toJson(url);
        assertThatJsonIsNewStyle(json);
    }

    private static void assertThatJsonIsNewStyle(String json) {
        assertThat(json)
                .contains("@type")
                .doesNotContain("protocol")
                .doesNotContain("file")
                .doesNotContain("ref")
                .doesNotContain("authority")
                .doesNotContain("host")
                .doesNotContain("port");
    }

    @Test
    void testUrl_inGenericSubobject_serializeBackCorrectly() throws Exception {
        URL url = new URL(LOCALHOST);
        GenericSubObject initial = new GenericSubObject<>(url);
        String json = TestUtil.toJson(initial);

        TestUtil.printLine("json=" + json);
        GenericSubObject actual = TestUtil.toObjects(json, null);
        assertThat(actual.getObject()).isEqualTo(initial.getObject());
    }

    @Test
    void testUrl_inNestedObject_serializeBackCorrectly() throws Exception {
        URL url = new URL(OUTSIDE_DOMAIN);
        NestedUrl initial = new NestedUrl(url);
        String json = TestUtil.toJson(initial);
        assertThatJsonIsNewStyle(json);

        TestUtil.printLine("json=" + json);
        NestedUrl actual = TestUtil.toObjects(json, null);

        assertThat(actual.getUrl()).isEqualTo(initial.getUrl());
    }

    @Test
    void testURL_referencedInArray() throws Exception {
        URL url = new URL(OUTSIDE_DOMAIN);
        List<URL> list = MetaUtils.listOf(url, url, url, url, url);
        String json = TestUtil.toJson(list);

        List<URL> actual = TestUtil.toObjects(json, null);

        assertThat(actual).containsAll(list);
    }

    @Test
    void testURL_referencedInObject() throws Exception {
        NestedTwice expected = new NestedTwice(new URL(OUTSIDE_DOMAIN));

        String json = TestUtil.toJson(expected);

        NestedTwice actual = TestUtil.toObjects(json, null);

        assertThat(expected.getUrl1()).isEqualTo(actual.getUrl1());
        assertThat(expected.getUrl2()).isEqualTo(actual.getUrl2());
    }


    private static class NestedUrl {
        private final URL url;

        public NestedUrl(URL url) {
            this.url = url;
        }

        public URL getUrl() {
            return this.url;
        }
    }

    private static class NestedTwice {
        private final URL url1;

        private final URL url2;

        public NestedTwice(URL url) {
            this.url1 = url;
            this.url2 = url;
        }

        public URL getUrl1() {
            return this.url1;
        }

        public URL getUrl2() {
            return this.url2;
        }
    }
}
