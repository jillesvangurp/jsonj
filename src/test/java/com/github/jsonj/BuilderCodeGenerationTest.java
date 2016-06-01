package com.github.jsonj;

import com.github.jsonj.tools.JsonParser;
import org.testng.annotations.Test;

@Test
public class BuilderCodeGenerationTest {
    JsonParser parser = new JsonParser();
    public void shouldGenerateBuilderCode() {
        String json="{\n" +
                "    \"filter\": {\n" +
                "        \"range\": {\n" +
                "            \"updatedAt\":{\n" +
                "            \"gt\":\"2014-01-01\"\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"aggregations\": {\n" +
                "        \"events\": {\n" +
                "            \"terms\": { \"field\": \"userId\" },\n" +
                "            \"aggregations\": {\n" +
                "                \"weekly activity for user\": {\n" +
                "                    \"date_histogram\": {\n" +
                "                        \"field\":    \"updatedAt\",\n" +
                "                        \"interval\": \"month\",\n" +
                "                        \"format\" : \"yyyy-MM-dd\"\n" +
                "                    },\n" +
                "                    \"aggregations\": {\n" +
                "                        \"event types\": {\n" +
                "                            \"terms\": { \"field\": \"type\" }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        parser.parse(json);
        // builder code below was actually pasted from the output
        // System.out.println(parsed.builderCode());
        //        JsonObject builderResult = $(_("filter", $(_("range", $(_("updatedAt", $(_("gt", "2014-01-01"))))))),
        //                _("aggregations",
        //                        $(_("events",
        //                                $(_("terms", $(_("field", "userId"))),
        //                                        _("aggregations",
        //                                                $(_("weekly activity for user",
        //                                                        $(_("date_histogram", $(_("field", "updatedAt"), _("interval", "month"), _("format", "yyyy-MM-dd"))),
        //                                                                _("aggregations", $(_("event types", $(_("terms", $(_("field", "type"))))))))))))))));
        //        assertThat(builderResult, is(parsed));

    }
}
