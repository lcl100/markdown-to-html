import org.junit.Test;

/**
 * @author lcl100
 * @create 2021-11-06 20:48
 * @desc MarkdownToHtml类的测试类
 */
public class MarkdownToHtmlTest {
    @Test
    public void testConvert() {
        String mdFilePath = "src/test/resources/test-md.md";
        String htmlFilePath = "src/test/resources/test-md-to-html.html";
        MarkdownToHtml.convert(mdFilePath, htmlFilePath);
    }
}
