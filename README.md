# markdown-to-html

## 概述

通过正则表达式将markdown文档转换成html文档。

## 使用说明

只需要通过`MarkdownToHtml.convert(mdFilePath, htmlFilePath);`即可将markdown文档转换成html文档。具体代码请参考：

```java
public class MarkdownToHtmlTest {
    @Test
    public void testConvert() {
        String mdFilePath = "src/test/resources/test-md.md";
        String htmlFilePath = "src/test/resources/test-md-to-html.html";
        MarkdownToHtml.convert(mdFilePath, htmlFilePath);
    }
}
```