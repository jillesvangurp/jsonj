package com.github.jsonj.bson;

import com.github.jsonj.JsonObject;
import com.github.jsonj.tools.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.testng.annotations.Test;

@Test
public class BsonTest {

    String sample="{\n" +
            "  \"web-app\": {\n" +
            "    \"servlet\": [\n" +
            "      {\n" +
            "        \"servlet-name\": \"cofaxCDS\",\n" +
            "        \"servlet-class\": \"org.cofax.cds.CDSServlet\",\n" +
            "        \"init-param\": {\n" +
            "          \"configGlossary:installationAt\": \"Philadelphia, PA\",\n" +
            "          \"configGlossary:adminEmail\": \"ksm@pobox.com\",\n" +
            "          \"configGlossary:poweredBy\": \"Cofax\",\n" +
            "          \"configGlossary:poweredByIcon\": \"/images/cofax.gif\",\n" +
            "          \"configGlossary:staticPath\": \"/content/static\",\n" +
            "          \"templateProcessorClass\": \"org.cofax.WysiwygTemplate\",\n" +
            "          \"templateLoaderClass\": \"org.cofax.FilesTemplateLoader\",\n" +
            "          \"templatePath\": \"templates\",\n" +
            "          \"templateOverridePath\": \"\",\n" +
            "          \"defaultListTemplate\": \"listTemplate.htm\",\n" +
            "          \"defaultFileTemplate\": \"articleTemplate.htm\",\n" +
            "          \"useJSP\": false,\n" +
            "          \"jspListTemplate\": \"listTemplate.jsp\",\n" +
            "          \"jspFileTemplate\": \"articleTemplate.jsp\",\n" +
            "          \"cachePackageTagsTrack\": 200,\n" +
            "          \"cachePackageTagsStore\": 200,\n" +
            "          \"cachePackageTagsRefresh\": 60,\n" +
            "          \"cacheTemplatesTrack\": 100,\n" +
            "          \"cacheTemplatesStore\": 50,\n" +
            "          \"cacheTemplatesRefresh\": 15,\n" +
            "          \"cachePagesTrack\": 200,\n" +
            "          \"cachePagesStore\": 100,\n" +
            "          \"cachePagesRefresh\": 10,\n" +
            "          \"cachePagesDirtyRead\": 10,\n" +
            "          \"searchEngineListTemplate\": \"forSearchEnginesList.htm\",\n" +
            "          \"searchEngineFileTemplate\": \"forSearchEngines.htm\",\n" +
            "          \"searchEngineRobotsDb\": \"WEB-INF/robots.db\",\n" +
            "          \"useDataStore\": true,\n" +
            "          \"dataStoreClass\": \"org.cofax.SqlDataStore\",\n" +
            "          \"redirectionClass\": \"org.cofax.SqlRedirection\",\n" +
            "          \"dataStoreName\": \"cofax\",\n" +
            "          \"dataStoreDriver\": \"com.microsoft.jdbc.sqlserver.SQLServerDriver\",\n" +
            "          \"dataStoreUrl\": \"jdbc:microsoft:sqlserver://LOCALHOST:1433;DatabaseName=goon\",\n" +
            "          \"dataStoreUser\": \"sa\",\n" +
            "          \"dataStorePassword\": \"dataStoreTestQuery\",\n" +
            "          \"dataStoreTestQuery\": \"SET NOCOUNT ON;select test='test';\",\n" +
            "          \"dataStoreLogFile\": \"/usr/local/tomcat/logs/datastore.log\",\n" +
            "          \"dataStoreInitConns\": 10,\n" +
            "          \"dataStoreMaxConns\": 100,\n" +
            "          \"dataStoreConnUsageLimit\": 100,\n" +
            "          \"dataStoreLogLevel\": \"debug\",\n" +
            "          \"maxUrlLength\": 500\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"servlet-name\": \"cofaxEmail\",\n" +
            "        \"servlet-class\": \"org.cofax.cds.EmailServlet\",\n" +
            "        \"init-param\": {\n" +
            "          \"mailHost\": \"mail1\",\n" +
            "          \"mailHostOverride\": \"mail2\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"servlet-name\": \"cofaxAdmin\",\n" +
            "        \"servlet-class\": \"org.cofax.cds.AdminServlet\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"servlet-name\": \"fileServlet\",\n" +
            "        \"servlet-class\": \"org.cofax.cds.FileServlet\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"servlet-name\": \"cofaxTools\",\n" +
            "        \"servlet-class\": \"org.cofax.cms.CofaxToolsServlet\",\n" +
            "        \"init-param\": {\n" +
            "          \"templatePath\": \"toolstemplates/\",\n" +
            "          \"log\": 1,\n" +
            "          \"logLocation\": \"/usr/local/tomcat/logs/CofaxTools.log\",\n" +
            "          \"logMaxSize\": \"\",\n" +
            "          \"dataLog\": 1,\n" +
            "          \"dataLogLocation\": \"/usr/local/tomcat/logs/dataLog.log\",\n" +
            "          \"dataLogMaxSize\": \"\",\n" +
            "          \"removePageCache\": \"/content/admin/remove?cache=pages&id=\",\n" +
            "          \"removeTemplateCache\": \"/content/admin/remove?cache=templates&id=\",\n" +
            "          \"fileTransferFolder\": \"/usr/local/tomcat/webapps/content/fileTransferFolder\",\n" +
            "          \"lookInContext\": 1,\n" +
            "          \"adminGroupID\": 4,\n" +
            "          \"betaServer\": true\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"servlet-mapping\": {\n" +
            "      \"cofaxCDS\": \"/\",\n" +
            "      \"cofaxEmail\": \"/cofaxutil/aemail/*\",\n" +
            "      \"cofaxAdmin\": \"/admin/*\",\n" +
            "      \"fileServlet\": \"/static/*\",\n" +
            "      \"cofaxTools\": \"/tools/*\"\n" +
            "    },\n" +
            "    \"taglib\": {\n" +
            "      \"taglib-uri\": \"cofax.tld\",\n" +
            "      \"taglib-location\": \"/WEB-INF/tlds/cofax.tld\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public void shouldSerializeAndParseBson() throws IOException {

        JsonParser p = new JsonParser();
        JsonObject o = p.parseObject(sample);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        BsonSerializer.serialize(o, bos);
        bos.flush();
        byte[] bytes = bos.toByteArray();

        BsonParser parser = new BsonParser();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        parser.parse(is);
    }
}
