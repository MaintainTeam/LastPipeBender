package org.schabi.newpipe.brave;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class NewVersionHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @SuppressWarnings({"checkstyle:LineLength", "checkstyle:MethodName"})
    @Test
    public void checkJsonConfigWith_alternative_apks() throws JsonParserException {
        final String stdUrl = "stdUrl";
        final String expectedAltUrl = "altUrl";

        final String altConfig = "{\"flavors\":{\"github\":{\"stable\":{\"apk\":\"" + stdUrl + "\","
                + "\"alternative_apks\":[{\"alternative\":\"conscrypt\",\"url\":\"" + expectedAltUrl + "\"}],"
                + "\"version\":\"1\",\"version_code\":1}}}}";

        final JsonObject obj = JsonParser.object().from(altConfig).getObject("flavors")
                .getObject("github").getObject("stable");
        final String result = NewVersionHelper.checkForAlternativeApkUrl(obj, stdUrl);

        assertEquals(expectedAltUrl, result);
    }

    @SuppressWarnings({"checkstyle:LineLength", "checkstyle:MethodName"})
    @Test
    public void checkJsonConfigWithout_alternative_apks() throws JsonParserException {
        final String stdUrl = "stdUrl";
        final String expectedStdUrl = stdUrl;

        final String altConfig = "{\"flavors\":{\"github\":{\"stable\":{\"apk\":\"" + stdUrl + "\","
                + "\"version\":\"1\",\"version_code\":1}}}}";

        final JsonObject obj = JsonParser.object().from(altConfig).getObject("flavors")
                .getObject("github").getObject("stable");
        final String result = NewVersionHelper.checkForAlternativeApkUrl(obj, stdUrl);

        assertEquals(expectedStdUrl, result);
    }
}
