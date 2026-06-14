package greencity.constant;

public final class SwaggerExampleModel {
    private static final String IMAGE_DESCRIPTION = "pass image as base64 or upload image\n";

    private static final String BEFORE_EXAMPLE = """
        <div>
        \t<ul class="tab">
        \t\t<li class="tabitem active">
        \t\t\t<a class="tablinks" data-name="example">Example Value</a>
        \t\t</li>
        \t\t<li class="tabitem">
        \t\t\t<a class="tablinks" data-name="model">Model</a>
        \t\t</li>
        \t</ul>
        \t<pre>
        """;

    private static final String AFTER_EXAMPLE = "\t</pre>\n"
        + "</div>";

    public static final String ADD_ECO_NEWS_REQUEST =
        "Add Eco News Request\n"
            + IMAGE_DESCRIPTION
            + BEFORE_EXAMPLE
            + "{\n"
            + "  \"title\": \"string\",\n"
            + "  \"text\": \"string\",\n"
            + "  \"shortInfo\": \"string\",\n"
            + "  \"tags\": [\n"
            + "    \"string\"\n"
            + "  ],\n"
            + "  \"image\": \"string\",\n"
            + "  \"source\": \"string\"\n"
            + "}\n"
            + AFTER_EXAMPLE;

    public static final String UPDATE_ECO_NEWS =
        "Update Eco News\n"
            + IMAGE_DESCRIPTION
            + BEFORE_EXAMPLE
            + "{\n"
            + "  \"id\": 0,\n"
            + "  \"title\": \"string\",\n"
            + "  \"content\": \"string\",\n"
            + "  \"shortInfo\": \"string\",\n"
            + "  \"tags\": [\n"
            + "    \"string\"\n"
            + "  ],\n"
            + "  \"image\": \"string\",\n"
            + "  \"source\": \"string\"\n"
            + "}\n"
            + AFTER_EXAMPLE;

    private SwaggerExampleModel() {
    }
}
