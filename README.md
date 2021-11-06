# markdown-to-html

## 概述

通过正则表达式将markdown文档转换成html文档。

## 原理

关于markdown与html文本的转换关系及能匹配markdown的正则表达式匹配如下表：

| 名称     | markdown                        | 正则表达式                     | html                                      | 备注     |
| -------- | ------------------------------- | ------------------------------ | ----------------------------------------- | -------- |
| 粗体     | `**粗体**`或`__粗体__`          | `([\*_]{2})(.*?)\1`            | `<b>粗体</b>`                             | 行内匹配 |
| 斜体     | `*斜体*`或`_斜体_`              | `(?<![\*_])(\*|_)([^\*_]+?)\1` | `<i>斜体</i>`                             | 行内匹配 |
| 删除线   | `~~删除线~~`                    | `(~~)(.*?)\1`                  | `<del>删除线</del>`                       | 行内匹配 |
| 超链接   | `[百度](http://www.baidu.com/)` | `(?<!!)\[(.*?)\]\((.*?)\)`     | `<a href="http://www.baidu.com">百度</a>` | 行内匹配 |
| 行内代码 | \`int num=10;\` | (?!<\`)(\`)(\[^\`]+?)\`(?!\`) | `<code>int num=10;</code>` | 行内匹配 |
| 标题 | `# 一级标题` | `^(#{1,6})(.*)` | `<h1>一级标题</h1>` | 单行匹配 |
| 图片 | `![图片名字](http://abc.com/star.jpg)` | `^\!\[(.*?)\]\((.*?)\)$` | `<img src="http://abc.com/star.jpg" alt="图片名字" />` | 单行匹配 |
| 分隔线 | `***`或`---`或`___` | ^(\*\|-\|_){3}$ | `<hr />` | 单行匹配 |
| 无序列表 | `- A`<br/>`- B`<br/>`- C` | `^([-\+\*]) (.*)` | `<ul>`<br/>&nbsp;&nbsp;&nbsp;&nbsp;`<li>A</li>`<br />&nbsp;&nbsp;&nbsp;&nbsp;`<li>B</li>`<br />&nbsp;&nbsp;&nbsp;&nbsp;`<li>C</li>`<br />`</ul>` | 单行匹配，多行作用 |
| 有序列表 | `1. A`<br/>`2. B`<br/>`3. C` | `^([\d+])\.\s(.*)` | `<ol>`<br/>&nbsp;&nbsp;&nbsp;&nbsp;`<li>A</li>`<br />&nbsp;&nbsp;&nbsp;&nbsp;`<li>B</li>`<br />&nbsp;&nbsp;&nbsp;&nbsp;`<li>C</li>`<br />`</ol>` | 单行匹配，多行作用 |
| 简单引用 | `> 引用内容`<br/>`> 引用内容` | `^([>] )(.*)` | `<blockquote>引用内容</blockquote>` | 单行匹配，多行作用 |
| 表格行 | `|表头1|表头2|`<br/>`|--|--|`<br/>`|列1|列2|` |        `^(\|)(.*?)\|$`                        | `<table><tr><th>标题1</th><th>标题1</th></tr><tr><td>列1</td><td>列2</td></tr></table>` | 单行匹配，多行作用 |
| 代码块 | \`\`\`java<br/>int num=10;<br/>\`\`\` |      (`{3})(\w+)([\s\S]\*?\|[\w\W]\*?\|[\d\D]*?)\1                       | `<xmp>int num=10;</xmp>` | 跨行匹配 ||
| 代码块开头 | \`\`\`java | ^[\`]{3}\\w+ |                                           |          |
| 代码块结尾 | \`\`\` | ^[`]{3}$ |                                           |          |

可以将markdown中的标记分为如下四种情况：

- 第一种：行内匹配。即在每一行中的某段文本可能匹配，如粗体、斜体、删除线、超链接、行内代码。处理方法是在读取每一行的时候直接使用正则表达式进行替换，注意，一行内可能会有多次行内匹配。
- 第二种：单行匹配。即一整行完全匹配，如标题、图片、分隔线。处理方法是在读取每一行的时候直接使用正则表达式进行替换，注意，一行只会匹配一次。
- 第三种：单行匹配，多行作用。即一整行完全匹配并且连续行都是这种格式，如无序列表、有序列表、简单引用、表格。处理方法是对连续行进行处理。
- 第四种：跨行匹配。即多行匹配，如代码块。处理方法是直接对整个字符串使用正则表达式进行替换。

所以都是使用正则表达式进行行替换的。

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