import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>将markdown中的标记分为如下四种情况：</p>
 * <ul>
 *     <li>第一种：行内匹配。即在每一行中的某段文本可能匹配，如粗体、斜体、删除线、超链接、行内代码。处理方法是在读取每一行的时候直接使用正则表达式进行替换，注意，一行内可能会有多次行内匹配。</li>
 *     <li>第二种：单行匹配。即一整行完全匹配，如标题、图片、分隔线。处理方法是在读取每一行的时候直接使用正则表达式进行替换，注意，一行只会匹配一次。</li>
 *     <li>第三种：单行匹配，多行作用。即一整行完全匹配并且连续行都是这种格式，如无序列表、有序列表、简单引用、表格。处理方法是对连续行进行处理。</li>
 *     <li>第四种：跨行匹配。即多行匹配，如代码块。处理方法是直接对整个字符串使用正则表达式进行替换。</li>
 * </ul>
 *
 * @author lcl100
 * @create 2021-11-06 20:05
 * @desc 将markdown文件转换html文件
 */
public class MarkdownToHtml {

    // 行内匹配，粗体正则
    private final static String MARKDOWN_BOLD_REGEXP = "([\\*_]{2})(.*?)\\1";
    // 行内匹配，斜体正则
    private final static String MARKDOWN_ITALIC_REGEXP = "(?<![\\*_])(\\*|_)([^\\*_]+?)\\1";// 图片超链接中可能有"_"下划线，会导致被当作斜体处理，无法显示，所以添加"\b"
    // 行内匹配，删除线正则，匹配 ~~red~~
    private final static String MARKDOWN_DELETE_LINE_REGEXP = "(~~)(.*?)\\1";
    // 行内匹配，普通链接正则，匹配 []()
    private final static String MARKDOWN_SIMPLE_LINK_REGEXP = "(?<!!)\\[(.*?)\\]\\((.*?)\\)";
    // 行内匹配，行内代码正则，匹配 ``
    private final static String MARKDOWN_LINE_CODE_REGEXP = "(?!<`)(`)([^`]+?)`(?!`)";

    // 单行匹配，标题正则
    private final static String MARKDOWN_TITLE_REGEXP = "^(#{1,6})(.*)";
    // 单行匹配，图片链接正则，匹配 ![]()
    private final static String MARKDOWN_IMAGE_LINK_REGEXP = "^\\!\\[(.*?)\\]\\((.*?)\\)$";
    // 单行匹配，分割线正则，匹配 ***或---或___
    private final static String MARKDOWN_SEPARATE_LINE_REGEXP = "^(\\*|-|_){3}$";

    // 单行匹配，多行作用，无序列表正则
    private final static String MARKDOWN_UNORDERED_LIST_REGEXP = "^([-\\+\\*]) (.*)";
    // 单行匹配，多行作用，有序列表正则
    private final static String MARKDOWN_ORDERED_LIST_REGEXP = "^([\\d+])\\.\\s(.*)";
    // 单行匹配，多行作用，简单引用正则
    private final static String MARKDOWN_SIMPLE_QUOTE_REGEXP = "^([>] ?)(.*)";
    // 单行匹配，多行作用，表格行正则，匹配 |表格列内容|表格列内容|
    private final static String MARKDOWN_TABLE_ROW_REGEXP = "^(\\|)(.*?)\\|$";

    // 跨行匹配，代码块正则，需要跨行匹配 ```java ```
    private final static String MARKDOWN_CODE_BLOCK_REGEXP = "(`{3})(\\w+)([\\s\\S]*?|[\\w\\W]*?|[\\d\\D]*?)\\1";// 匹配一整个代码块
    private final static String MARKDOWN_CODE_BLOCK_START_REGEXP = "^[`]{3}\\w+";// 匹配代码块的开头
    private final static String MARKDOWN_CODE_BLOCK_END_REGEXP = "^[`]{3}$";// 匹配代码块的结尾

    /**
     * 将markdown文件转换成html文件
     *
     * @param md   markdown文件路径
     * @param html html文件路径
     */
    public static void convert(String md, String html) {
        // 加载markdown文件中的所有非空白行
        List<String> lines = loadMd(md);

        // 处理单行匹配，多行作用
        String newLine = "";
        List<String> resultList = new ArrayList<String>();
        List<String> tempList = new ArrayList<String>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // 处理行内匹配
            Matcher boldMatcher = Pattern.compile(MARKDOWN_BOLD_REGEXP).matcher(line);
            if (boldMatcher.find()) {// 粗体
                line = replaceBold(line);
            }
            Matcher italicMatcher = Pattern.compile(MARKDOWN_ITALIC_REGEXP).matcher(line);
            if (italicMatcher.find()) {// 斜体
                line = replaceItalic(line);
            }
            Matcher deleteLineMatcher = Pattern.compile(MARKDOWN_DELETE_LINE_REGEXP).matcher(line);
            if (deleteLineMatcher.find()) {// 删除线
                line = replaceDeleteLine(line);
            }
            Matcher simpleLinkMatcher = Pattern.compile(MARKDOWN_SIMPLE_LINK_REGEXP).matcher(line);
            if (simpleLinkMatcher.find()) {// 普通链接
                line = replaceSimpleLink(line);
            }
            Matcher lineCodeMatcher = Pattern.compile(MARKDOWN_LINE_CODE_REGEXP).matcher(line);
            if (lineCodeMatcher.find()) {// 行内代码
                line = replaceLineCode(line);
            }
            // 处理单行匹配
            Matcher titleMatcher = Pattern.compile(MARKDOWN_TITLE_REGEXP).matcher(line);
            if (titleMatcher.find()) {// 标题行
                line = replaceTitle(line);
            }
            Matcher imageLinkMatcher = Pattern.compile(MARKDOWN_IMAGE_LINK_REGEXP).matcher(line);
            if (imageLinkMatcher.find()) {// 图片链接行，注意图片链接中如果匹配到斜体的"_"可能无法解析
                line = replaceImageLink(line);
            }
            Matcher separateLineMatcher = Pattern.compile(MARKDOWN_SEPARATE_LINE_REGEXP).matcher(line);
            if (separateLineMatcher.find()) {// 分隔线行
                line = replaceSeparateLine(line);
            }

            // 处理单行匹配，多行作用
            // 无序列表
            // 下面的代码就是将连续的无序列表行添加到List集合中，再调用相关方法进行处理
            tempList.clear();
            Matcher unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);
            while (unorderedListMatcher.find()) {
                tempList.add(line);
                i++;
                line = lines.get(i);
                unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);
            }
            resultList.add(replaceUnorderedList(tempList));
            // 有序列表
            tempList.clear();
            Matcher orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
            while (orderedListMatcher.find()) {
                tempList.add(line);
                i++;
                line = lines.get(i);
                orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
            }
            resultList.add(replaceOrderedList(tempList));
            // 简单引用，如果是空">"行则后面应该有空格，否则也无法匹配成功
            tempList.clear();
            Matcher simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
            while (simpleQuoteMatcher.find()) {
                tempList.add(line);
                i++;
                line = lines.get(i);
                simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
            }
            resultList.add(replaceSimpleQuote(tempList));
            // 表格行，注意每个表格行最后一个"|"的后面不能有空格，否则无法匹配成功
            tempList.clear();
            Matcher tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
            while (tableRowMatcher.find()) {
                tempList.add(line);
                i++;
                line = lines.get(i);
                tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
            }
            resultList.add(replaceTable(tempList));

            // 试图处理跨行代码
            tempList.clear();
            Matcher codeBlockStartMatcher = Pattern.compile(MARKDOWN_CODE_BLOCK_START_REGEXP).matcher(line);
            if (codeBlockStartMatcher.find()) {
                Matcher codeBlockEndMatcher = Pattern.compile(MARKDOWN_CODE_BLOCK_END_REGEXP).matcher(line);
                while (!codeBlockEndMatcher.find()) {
                    tempList.add(line);
                    i++;
                    line = lines.get(i);
                    codeBlockEndMatcher = Pattern.compile(MARKDOWN_CODE_BLOCK_END_REGEXP).matcher(line);
                }
                tempList.add(line);
                resultList.add(replaceCodeBlock(tempList));
                continue;
            }

            if (line.trim().length() > 0) {
                resultList.add("<p>" + line + "</p>");
            }
        }

        // 将lines集合中的所有行写入到字符串中
        StringBuilder htmlStr = new StringBuilder();
        // 添加html文档的头部和尾部
        htmlStr.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>");
        for (String line : resultList) {
            htmlStr.append(line);
        }
        htmlStr.append("</body>\n" +
                "</html>");

        // 将转换后的html结果写入文件
        writeHtml(html, htmlStr.toString());
    }

    /**
     * 将markdown格式的粗体文本转换成html格式的粗体字符串
     *
     * @param text markdown格式的粗体文本
     * @return html格式的粗体字符串
     */
    private static String replaceBold(String text) {
        Matcher boldMatcher = Pattern.compile(MARKDOWN_BOLD_REGEXP).matcher(text);
        if (boldMatcher.find()) {// 粗体
            text = boldMatcher.replaceAll("<b>$2</b>");
        }
        return text;
    }

    /**
     * 将markdown格式的斜体文本转换成html格式的斜体字符串
     *
     * @param text markdown格式的斜体文本
     * @return html格式的斜体字符串
     */
    private static String replaceItalic(String text) {
        Matcher italicMatcher = Pattern.compile(MARKDOWN_ITALIC_REGEXP).matcher(text);
        if (italicMatcher.find()) {// 斜体
            text = italicMatcher.replaceAll("<i>$2</i>");
        }
        return text;
    }

    /**
     * 将markdown格式的删除线文本转换成html格式的删除线字符串
     *
     * @param text markdown格式的删除线文本
     * @return html格式的删除线字符串
     */
    private static String replaceDeleteLine(String text) {
        Matcher deleteLineMatcher = Pattern.compile(MARKDOWN_DELETE_LINE_REGEXP).matcher(text);
        if (deleteLineMatcher.find()) {// 删除线
            text = deleteLineMatcher.replaceAll("<del>$2</del>");
        }
        return text;
    }

    /**
     * 将markdown格式的超链接文本转换成html格式的超链接字符串
     *
     * @param text markdown格式的超链接文本
     * @return html格式的超链接字符串
     */
    private static String replaceSimpleLink(String text) {
        Matcher simpleLinkMatcher = Pattern.compile(MARKDOWN_SIMPLE_LINK_REGEXP).matcher(text);
        if (simpleLinkMatcher.find()) {// 普通链接
            text = simpleLinkMatcher.replaceAll("<a href='$2'>$1</a>");
        }
        return text;
    }

    /**
     * 将markdown格式的行内代码文本转换成html格式的行内代码字符串
     *
     * @param text markdown格式的行内代码文本
     * @return html格式的行内代码字符串
     */
    private static String replaceLineCode(String text) {
        Matcher lineCodeMatcher = Pattern.compile(MARKDOWN_LINE_CODE_REGEXP).matcher(text);
        if (lineCodeMatcher.find()) {// 行内代码
            text = lineCodeMatcher.replaceAll("<code>$2</code>");
        }
        return text;
    }

    /**
     * 将markdown格式的标题行文本转换成html格式的标题行字符串
     *
     * @param text markdown格式的标题行文本
     * @return html格式的标题行字符串
     */
    private static String replaceTitle(String text) {
        Matcher titleMatcher = Pattern.compile(MARKDOWN_TITLE_REGEXP).matcher(text);
        if (titleMatcher.find()) {// 标题行
            String sign = titleMatcher.group(1);
            text = titleMatcher.replaceAll("<h" + sign.length() + ">$2</h" + sign.length() + ">");
        }
        return text;
    }

    /**
     * 将markdown格式的图片链接文本转换成html格式的图片链接字符串
     *
     * @param text markdown格式的图片链接文本
     * @return html格式的图片链接字符串
     */
    private static String replaceImageLink(String text) {
        Matcher imageLinkMatcher = Pattern.compile(MARKDOWN_IMAGE_LINK_REGEXP).matcher(text);
        if (imageLinkMatcher.find()) {// 图片链接行
            text = imageLinkMatcher.replaceAll("<img src='$2' title='$1' alt='$1' />");
        }
        return text;
    }

    /**
     * 将markdown格式的分隔线文本转换成html格式的分隔线字符串
     *
     * @param text markdown格式的分隔线文本
     * @return html格式的分隔线字符串
     */
    private static String replaceSeparateLine(String text) {
        Matcher separateLineMatcher = Pattern.compile(MARKDOWN_SEPARATE_LINE_REGEXP).matcher(text);
        if (separateLineMatcher.find()) {// 分隔线行
            text = separateLineMatcher.replaceAll("<hr/>");
        }
        return text;
    }

    /**
     * 将集合中的markdown格式的无序列表行转换成html格式的字符串
     *
     * @param unorderedList markdown格式的无序列表行集合
     * @return html格式的字符串
     */
    private static String replaceUnorderedList(List<String> unorderedList) {
        // 无序列表
        StringBuilder result = new StringBuilder();
        if (unorderedList.size() > 0) {
            result.append("<ul>");
            for (String line : unorderedList) {
                Matcher unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);
                if (unorderedListMatcher.find()) {
                    result.append("<li>").append(unorderedListMatcher.group(2)).append("</li>");
                }
            }
            result.append("</ul>");
        }
        return result.toString();
    }

    /**
     * 将集合中的markdown格式的有序列表行转换成html格式的字符串
     *
     * @param orderedList markdown格式的有序列表行集合
     * @return html格式的字符串
     */
    private static String replaceOrderedList(List<String> orderedList) {
        StringBuilder result = new StringBuilder();
        if (orderedList.size() > 0) {
            result.append("<ol>");
            for (String line : orderedList) {
                Matcher orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
                if (orderedListMatcher.find()) {
                    result.append("<li>").append(orderedListMatcher.group(2)).append("</li>");
                }
            }
            result.append("</ol>");
        }
        return result.toString();
    }

    /**
     * 将集合中的markdown格式的引用行转换成html格式的字符串
     *
     * @param quoteList markdown格式的引用行集合
     * @return html格式的字符串
     */
    private static String replaceSimpleQuote(List<String> quoteList) {
        StringBuilder result = new StringBuilder();
        if (quoteList.size() > 0) {
            result.append("<blockquote>");
            for (String line : quoteList) {
                Matcher simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
                if (simpleQuoteMatcher.find()) {
                    result.append(simpleQuoteMatcher.group(2)).append("<br/>");
                }
            }
            result.append("</blockquote>");
        }
        return result.toString();
    }

    /**
     * 将集合中的markdown格式的表格行转换成html格式的字符串
     *
     * @param tableRowList markdown格式的表格行集合
     * @return html格式的字符串
     */
    private static String replaceTable(List<String> tableRowList) {
        StringBuilder result = new StringBuilder();
        if (tableRowList.size() > 0) {
            result.append("<table border='1' cellspacing='0'>");
            for (int i = 0; i < tableRowList.size(); i++) {
                String line = tableRowList.get(i);
                Matcher tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
                if (tableRowMatcher.find()) {
                    String row = tableRowMatcher.group(2);
                    String[] cols = row.split("\\|");
                    result.append("<tr>");
                    for (String col : cols) {
                        if (i == 0) {
                            result.append("<th>").append(col).append("</th>");
                        } else if (i >= 2) {
                            result.append("<td>").append(col).append("</td>");
                        }
                    }
                    result.append("</tr>");
                }
            }
            result.append("</table>");
        }
        return result.toString();
    }

    /**
     * 将集合中的markdown格式的代码块中的代码行转换成html格式的字符串
     *
     * @param codeBlockList markdown格式的代码块中的代码行集合
     * @return html格式的字符串
     */
    private static String replaceCodeBlock(List<String> codeBlockList) {
        StringBuilder result = new StringBuilder();
        if (codeBlockList.size() > 0) {
            result.append("<xmp>\n");
            for (int i = 1; i < codeBlockList.size() - 1; i++) {
                result.append(codeBlockList.get(i)).append("\n");
            }
            result.append("</xmp>");
        }
        return result.toString();
    }

    /**
     * 读取markdown文件中的每一个非空白行，保存到集合中返回
     *
     * @param md markdown文件路径
     * @return 包含markdown文件中所有非空白行的集合
     */
    private static List<String> loadMd(String md) {
        FileReader fr = null;
        List<String> lines = new ArrayList<String>();
        String line = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(md);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                // 只添加非空白行
                if (line.trim().length() > 0) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

    /**
     * 将指定html内容写入到指定路径的文件中
     *
     * @param htmlPath    指定保存路径
     * @param htmlContent 待写入的html格式的字符串内容
     */
    private static void writeHtml(String htmlPath, String htmlContent) {
        FileWriter fw = null;
        try {
            File file = new File(htmlPath);
            fw = new FileWriter(file);
            fw.write(htmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
