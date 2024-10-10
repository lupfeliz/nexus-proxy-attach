package com.ntiple.nexus;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.IOUtils.passthrough;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NexusProxyAttach {
  private String request(String request, boolean downloadOnly) throws Exception {
    URL url = null;
    URLConnection con = null;
    OutputStream ostream = null;
    InputStream istream = null;
    ByteArrayOutputStream bos = null;
    Reader ir = null;
    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();
    try {
      sb.setLength(0);
      url = new URL(request);
      con = url.openConnection();
      con.setUseCaches(false);
      istream = con.getInputStream();
      if (!downloadOnly) {
        ir = new InputStreamReader(istream, UTF8);
        reader = new BufferedReader(ir);
        for (String rl; (rl = reader.readLine()) != null;) {
          sb.append(rl).append("\r\n");
        }
        // log.debug("CONTENT:{}", sb);
      } else {
        bos = new ByteArrayOutputStream();
        passthrough(istream, bos);
        bos.close();
      }
      return String.valueOf(sb);
    } finally {
      safeclose(reader);
      safeclose(ir);
      safeclose(istream);
      safeclose(ostream);
      safeclose(bos);
    }
  }

  private void testHttp(String addr, String repository, String addr2, String repository2) throws Exception {
    String token = "";
    String uriPrefix = "/service/rest/v1/components";
    LOOP1: for (;;) {
      try {
        String request = cat(
          addr, uriPrefix, "?", "repository=", repository,
          token.length() > 0 ? cat("&continuationToken=", token) : "");
        String content = request(request, false);
        JSONObject obj = new JSONObject(content);
        JSONArray arr = obj.optJSONArray("items");
        token = obj.optString("continuationToken");
        // log.debug("TOKEN:{}", token);
        if (arr != null) {
          LOOP2: for (int iinx = 0; iinx < arr.length(); iinx++) {
            JSONObject row = arr.optJSONObject(iinx);
            JSONArray assets = row.optJSONArray("assets");
            if (assets == null) { continue LOOP2; }
            LOOP3: for (int ainx = 0; ainx < assets.length(); ainx++) {
              JSONObject asset = assets.optJSONObject(ainx);
              String contentUrl = asset.optString("downloadUrl");
              if (contentUrl == null || "".equals(contentUrl)) { continue LOOP3; }
              contentUrl = contentUrl.substring(cat(addr, "/repository/", repository).length());
              String downUrl = cat(addr2, "/repository/", repository2, contentUrl);
              log.debug("URL:{}", downUrl);
              request(downUrl, true);
              continue LOOP3;
            }
            continue LOOP2;
          }
        }
      } catch (Exception e) {
        log.debug("E:{}", e);
        break LOOP1;
      }
      if (token.length() > 0) { continue LOOP1; }
      break LOOP1;
    }
  }

  public static void main(String[] arg) throws Exception {
    NexusProxyAttach inst = new NexusProxyAttach();
    if (arg != null && arg.length > 3) {
      inst.testHttp(arg[0], arg[1], arg[2], arg[3]);
    } else {
      System.out.println("USAGE: \r\n\r\njava -jar nexus-proxy-attach {원본 Nexus주소} {원본 NexusRepository명} {사본 Nexus주소} {사본 NexusRepository명}\r\n");
      System.out.println("예제: \r\n\r\njava -jar nexus-proxy-attach http://192.168.0.2:8081 test-maven-repo http://192.168.0.2:8082 test2-maven-proxy\r\n");
    }
  }
}