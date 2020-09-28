package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;
import org.testng.annotations.Test;

public class MimeTypeApiTest extends AbstractRestApiTest {

  private static final String MIMETYPE_API_ENDPOINT =
      TEST_CONFIG.getInstitutionUrl() + "api/mimetype";

  @Test
  public void testRetrieveMimeTypes() throws Exception {
    final List<MimeTypeDetail> initialFilters = getMimeTypes();
    assertEquals(153, initialFilters.size());
  }

  @Test
  public void testRetrieveMimeTypeConfiguration() throws Exception {
    // Let's process all the known mimetypes to ensure we can
    for (MimeTypeDetail detail : getMimeTypes()) {
      final HttpMethod method =
          new GetMethod(MIMETYPE_API_ENDPOINT + "/viewerconfig/" + detail.getMimeType());
      assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
      JsonNode viewConfig = mapper.readTree(method.getResponseBodyAsString());
      assertNotNull(viewConfig.findValue("defaultViewer"));
    }
  }

  private List<MimeTypeDetail> getMimeTypes() throws IOException {
    final HttpMethod method = new GetMethod(MIMETYPE_API_ENDPOINT);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    return mapper.readValue(
        method.getResponseBodyAsString(), new TypeReference<List<MimeTypeDetail>>() {});
  }

  // Mirror of com.tle.web.api.settings.MimeTypeDetail
  private static class MimeTypeDetail {

    private String mimeType = null;
    private String desc = null;

    public String getMimeType() {
      return mimeType;
    }

    public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
    }

    public String getDesc() {
      return desc;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }
  }
}
