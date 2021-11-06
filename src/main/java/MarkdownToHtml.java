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
    private final static String MARKDOWN_CODE_BLOCK_REGEXP = "(`{3})(\\w+)([\\s\\S]*?|[\\w\\W]*?|[\\d\\D]*?)\\1";

    /**
     * 将markdown文件转换成html文件
     *
     * @param md   markdown文件路径
     * @param html html文件路径
     */
    public static void convert(String md, String html) {
        // 加载markdown文件中的所有非空白行
        List<String> lines = loadMd(md);

        // 处理行内匹配和单行匹配
        for (int i = 0; i < lines.size(); i++) {
            // 获取当前正在遍历的行
            String line = lines.get(i);
            String newLine = line;
            // 处理行内匹配，由于一行内可能会有多个行内匹配，所以必须都匹配一遍
            Matcher boldMatcher = Pattern.compile(MARKDOWN_BOLD_REGEXP).matcher(line);
            if (boldMatcher.find()) {// 粗体
                newLine = boldMatcher.replaceAll("<b>$2</b>");
                lines.set(i, newLine);// 替换后，用set()方法修改lines中对应索引的值
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
        }

        // 处理单行匹配，多行作用
        ArrayList<String> newLines = new ArrayList<String>();
        int count = 0;// 计数器，记录匹配到的行数
        boolean unorderedListFlag = false;// 无序列表标志，当为true时表示当前正在遍历的行是无序列表行，为false时表示前正在遍历的行不是无序列表行
        boolean orderedListFlag = false;// 有序列表标志，当为true时表示当前正在遍历的行是有序列表行，为false时表示前正在遍历的行不是有序列表行
        boolean simpleQuoteFlag = false;// 简单引用行标志，当为true时表示当前正在遍历的行是简单引用行，为false时表示前正在遍历的行不是简单引用行
        boolean tableRowFlag = false;// 表格行标志，当为true时表示当前正在遍历的行是表格行标志，为false时表示前正在遍历的行不是表格行标志
        String newLine = "";
        List<String> tempList = new ArrayList<String>();// 临时集合，用来存放连续匹配到的行
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // 无序列表
            Matcher unorderedListMatcher = Pattern.compile(MARKDOWN_UNORDERED_LIST_REGEXP).matcher(line);
            if (unorderedListMatcher.find()) {
                count++;
                tempList.add(unorderedListMatcher.group(2));
                unorderedListFlag = true;
                continue;// 注意，匹配后，需要跳出本次循环，不要执行后面的代码
            }
            if (count > 0 && unorderedListFlag) {// 单凭count一个变量无法确定当前处理的是无序列表，所以还增加了一个unorderedListFlag标记用来判断当前处理的是无序列表
                // 无序列表需要添加"<ul></ul>"
                newLine += "<ul>";
                for (int j = 0; j < count; j++) {
                    // 将匹配到的所有列表行作"<li></li>"处理
                    newLine += "<li>" + tempList.get(j) + "</li>";
                }
                newLine += "</ul>";
                newLines.add(newLine);
                // 注意，由于下面几个都是局部变量，会被用到多次，所以将其重置
                newLine = "";
                count = 0;
                tempList.clear();
                unorderedListFlag = false;
            }
            // 有序列表
            Matcher orderedListMatcher = Pattern.compile(MARKDOWN_ORDERED_LIST_REGEXP).matcher(line);
            if (orderedListMatcher.find()) {
                count++;
                tempList.add(orderedListMatcher.group(2));
                orderedListFlag = true;
                continue;
            }
            if (count > 0 && orderedListFlag) {
                newLine += "<ol>";
                for (int j = 0; j < count; j++) {
                    newLine += "<li>" + tempList.get(j) + "</li>";
                }
                newLine += "</ol>";
                newLines.add(newLine);
                newLine = "";
                count = 0;
                tempList.clear();
                orderedListFlag = false;
            }
            // 简单引用
            Matcher simpleQuoteMatcher = Pattern.compile(MARKDOWN_SIMPLE_QUOTE_REGEXP).matcher(line);
            if (simpleQuoteMatcher.find()) {
                count++;
                tempList.add(simpleQuoteMatcher.group(2));
                simpleQuoteFlag = true;
                continue;
            }
            if (count > 0 && simpleQuoteFlag) {
                newLine += "<blockquote>";
                for (int j = 0; j < count; j++) {
                    newLine += tempList.get(j) + "<br/>";
                }
                newLine += "</blockquote>";
                newLines.add(newLine);
                newLine = "";
                count = 0;
                tempList.clear();
                simpleQuoteFlag = false;
            }
            // 表格行
            Matcher tableRowMatcher = Pattern.compile(MARKDOWN_TABLE_ROW_REGEXP).matcher(line);
            if (tableRowMatcher.find()) {
                count++;
                tempList.add(tableRowMatcher.group(2));
                tableRowFlag = true;
                continue;
            }
            if (count > 0 && tableRowFlag) {
                newLine += "<table border='1' cellspacing='0'>";
                // 处理表头
                if (count >= 2) {
                    String tableTitle = tempList.get(0);
                    String[] titles = tableTitle.split("\\|");
                    newLine += "<tr>";
                    for (String title : titles) {
                        newLine += "<th>" + title + "</th>";
                    }
                    newLine += "</tr>";
                }
                // 处理表格内容
                for (int j = 2; j < count; j++) {
                    String tableRow = tempList.get(j);
                    String[] columns = tableRow.split("\\|");
                    newLine += "<tr>";
                    for (String column : columns) {
                        newLine += "<td>" + column + "</td>";
                    }
                    newLine += "</tr>";
                }
                newLine += "</table>";
                newLines.add(newLine);
                newLine = "";
                count = 0;
                tempList.clear();
                simpleQuoteFlag = false;
            }

            newLines.add("<p>" + line + "</p>");
        }

        // 处理跨行匹配，即处理代码块的情况
        String str = "";
        for (String line : newLines) {
            str += line + "\n";
        }
        Matcher codeBlockMatcher = Pattern.compile(MARKDOWN_CODE_BLOCK_REGEXP).matcher(str);
        if (codeBlockMatcher.find()) {
            str = codeBlockMatcher.replaceAll("<pre><code>$3</code></pre>");
        }
        lines.clear();
        lines.add(str);

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
        for (String line : lines) {
            htmlStr.append(line).append("\n");
        }
        htmlStr.append("</body>\n" +
                "</html>");

        // 将转换后的html结果写入文件
        writeHtml(html, htmlStr.toString());
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
