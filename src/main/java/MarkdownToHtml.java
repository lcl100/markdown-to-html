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
    private final static String MARKDOWN_ITALIC_REGEXP = "(?<![\\*_])(\\*|_)([^\\*_]+?)\\1";
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
    private final static String MARKDOWN_SIMPLE_QUOTE_REGEXP = "^([>] )(.*)";
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
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // 处理行内匹配
            Matcher boldMatcher = Pattern.compile(MARKDOWN_BOLD_REGEXP).matcher(line);
            if (boldMatcher.find()) {// 粗体
                newLine = boldMatcher.replaceAll("<b>$2</b>");
                lines.set(i, newLine);
            }
            Matcher italicMatcher = Pattern.compile(MARKDOWN_ITALIC_REGEXP).matcher(line);
            if (italicMatcher.find()) {// 斜体
                newLine = italicMatcher.replaceAll("<i>$2</i>");
                lines.set(i, newLine);
            }
            Matcher deleteLineMatcher = Pattern.compile(MARKDOWN_DELETE_LINE_REGEXP).matcher(line);
            if (deleteLineMatcher.find()) {// 删除线
                newLine = deleteLineMatcher.replaceAll("<del>$2</del>");
                lines.set(i, newLine);
            }
            Matcher simpleLinkMatcher = Pattern.compile(MARKDOWN_SIMPLE_LINK_REGEXP).matcher(line);
            if (simpleLinkMatcher.find()) {// 普通链接
                newLine = simpleLinkMatcher.replaceAll("<a href='$2'>$1</a>");
                lines.set(i, newLine);
            }
            Matcher lineCodeMatcher = Pattern.compile(MARKDOWN_LINE_CODE_REGEXP).matcher(line);
            if (lineCodeMatcher.find()) {// 行内代码
                newLine = lineCodeMatcher.replaceAll("<code>$2</code>");
                lines.set(i, newLine);
            }
            // 处理单行匹配
            Matcher titleMatcher = Pattern.compile(MARKDOWN_TITLE_REGEXP).matcher(line);
            if (titleMatcher.find()) {// 标题行
                String sign = titleMatcher.group(1);
                newLine = titleMatcher.replaceAll("<h" + sign.length() + ">$2</h" + sign.length() + ">");
                lines.set(i, newLine);
            }
            Matcher imageLinkMatcher = Pattern.compile(MARKDOWN_IMAGE_LINK_REGEXP).matcher(line);
            if (imageLinkMatcher.find()) {// 图片链接行
                newLine = imageLinkMatcher.replaceAll("<img src='$2' title='$1' alt='$1' />");
                lines.set(i, newLine);
            }
            Matcher separateLineMatcher = Pattern.compile(MARKDOWN_SEPARATE_LINE_REGEXP).matcher(line);
            if (separateLineMatcher.find()) {// 分隔线行
                newLine = separateLineMatcher.replaceAll("<hr/>");
                lines.set(i, newLine);
            }

            // 无序列表
            newLine = "";
            Matcher unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);
            while (unorderedListMatcher.find()) {
                unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);// 因为find()方法是一次性的，所以要重新生成Matcher
                if (unorderedListMatcher.find()) {
                    newLine += "<li>" + unorderedListMatcher.group(2) + "</li>";
                }
                i++;
                line = lines.get(i);
                unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);
            }
            if (newLine.trim().length() > 0) {
                resultList.add("<ul>" + newLine + "</ul>");
            }
            // 有序列表
            newLine = "";
            Matcher orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
            while (orderedListMatcher.find()) {
                orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
                if (orderedListMatcher.find()) {
                    newLine += "<li>" + orderedListMatcher.group(2) + "</li>";
                }
                i++;
                line = lines.get(i);
                orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
            }
            if (newLine.trim().length() > 0) {
                resultList.add("<ol>" + newLine + "</ol>");
            }
            // 简单引用，如果是空">"行则后面应该有空格，否则也无法匹配成功
            newLine = "";
            Matcher simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
            while (simpleQuoteMatcher.find()) {
                simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
                if (simpleQuoteMatcher.find()) {
                    newLine += simpleQuoteMatcher.group(2) + "<br/>";
                }
                i++;
                line = lines.get(i);
                simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
            }
            if (newLine.trim().length() > 0) {
                resultList.add("<blockquote>" + newLine + "</blockquote>");
            }
            // 表格行，注意每个表格行最后一个"|"的后面不能有空格，否则无法匹配成功
            newLine = "";
            Matcher tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
            int rowCount = 0;
            while (tableRowMatcher.find()) {
                tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
                if (tableRowMatcher.find()) {
                    rowCount++;
                    String row = tableRowMatcher.group(2);
                    String[] cols = row.split("\\|");
                    newLine += "<tr>";
                    for (String col : cols) {
                        if (rowCount == 1) {
                            newLine += "<th>" + col + "</th>";
                        } else if (rowCount > 2) {
                            newLine += "<td>" + col + "</td>";
                        }
                    }
                    newLine += "</tr>";
                }
                i++;
                line = lines.get(i);
                tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
            }
            if (newLine.trim().length() > 0) {
                resultList.add("<table border='1' cellspacing='0'>" + newLine + "</table>");
            }

            // 试图处理跨行代码
            newLine = "";
            Matcher codeBlockStartMatcher = Pattern.compile(MARKDOWN_CODE_BLOCK_START_REGEXP).matcher(line);
            if (codeBlockStartMatcher.find()) {
                String code = "";
                while (!Pattern.compile(MARKDOWN_CODE_BLOCK_END_REGEXP).matcher(line).find()) {
                    i++;
                    line = lines.get(i);
                    if (!Pattern.compile(MARKDOWN_CODE_BLOCK_END_REGEXP).matcher(line).find()) {
                        code += line + "\n";
                    }
                }
                newLine += "<xmp>" + code + "</xmp>";
                resultList.add(newLine);
                continue;
            }

            if (lines.get(i).trim().length() > 0) {
                resultList.add("<p>" + lines.get(i) + "</p>");
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
     * 匹配替换
     *
     * @param regex       匹配的正则表达式
     * @param text        待匹配的字符串
     * @param replacement 匹配成功后要替换成的字符串
     * @return 返回替换后的字符串
     */
    private static String matchReplace(String regex, String text, String replacement) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        String result = "";
        if (matcher.find()) {
            result = matcher.replaceAll(replacement);
            if (text.contains("三体")) {
                System.out.println("=============" + result);
            }
        }
        if (Pattern.compile(MARKDOWN_SIMPLE_LINK_REGEXP).matcher(text).find() && !Pattern.compile(MARKDOWN_IMAGE_LINK_REGEXP).matcher(text).find()) {
            return "";
        }
        return result;
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
