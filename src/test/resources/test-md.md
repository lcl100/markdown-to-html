# 使用正则表达式将markdown转换成html

## 行内匹配
我是**加粗文本**，我在一行中，我还可以通过 __加粗__。

我也是*斜体*文本，还可以通过这也的方式 _hello world_ 倾斜。

我还可以是行内~~删除线~~呢

当然我也可以使用超链接，比如说打开[百度](http://wwww.baidu.com)，或者打开交友网站[github](http://www.github.com/)

我还可以在每一行文本中间写一些代码如`int num=12;`或者一个`System.out.println("hello world);`呢。

我是不是很厉害啊！

## 单行匹配
# 一级标题
## 二级标题
### 三级标题
#### 四级标题
##### 五级标题
###### 六级标题
####### 六级标题

一张美丽的图片：
![三体智子](https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fgss0.baidu.com%2F9vo3dSag_xI4khGko9WTAnF6hhy%2Fzhidao%2Fpic%2Fitem%2F0dd7912397dda144a4a40cd8bcb7d0a20cf4862e.jpg&refer=http%3A%2F%2Fgss0.baidu.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1638676098&t=dc2653a3a586c52e3336906846756daf)

我们是美丽的分割线：
***

分割线

---
分割线
___


## 跨行匹配
```java
public class Test{
    @Test
    public void test() {
        String str = "我是**加粗文本**，我在一行中，我还可以通过 __加粗__。";
        Matcher matcher = Pattern.compile(MARKDOWN_BOLD_REGEXP).matcher(str);
        String result = matcher.replaceAll("<b>$2</b>");
        System.out.println(result);
    }
}
```

单行匹配，多行作用
无序列表：
- red
- green
- blue

我们还有有序列表
1. 唐僧
2. 孙悟空
3. 猪八戒
4. 沙僧

有一个Bug就是最后必须留一个空行，否则列表无法被遍历到

我们可以写简单的引用：
> 注：这是引用的一段话。
> 
> 这也是引用的一段话。

还需要内容。。。。。在单行匹配后面必须还有一行内容才能正确运行。

这里还有一个表格待处理：

|ID|姓名|年龄|性别|
|--|--|--|--|
|1|张三|18|男|
|2|李四|19|女|
|3|王五|20|女|
|4|赵六|21|男|
|5|郑七|22|男|

表格完成