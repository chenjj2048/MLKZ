/**
 * .
 * Created by 彩笔怪盗基德 on 2015/12/8
 * github：https://github.com/chenjj2048
 * .
 */

package ecust.library.books_query.modles;

public class Book {
    //标题
    private String title;
    //作者
    private String author;
    //出版社
    private String publisher;
    //出版时间
    private String publishTime;
    //中图分类号（Chinese Library Classification）
    private String CLCIndex;

    /**
     * &#183;转为·
     * trim()
     */
    private String convertString(String s) {
        return s.replace("&#183;", "·").trim();
    }

    public Book title(String title) {
        this.title = convertString(title);
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public Book author(String author) {
        this.author = convertString(author);
        return this;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public Book publisher(String publisher) {
        this.publisher = convertString(publisher);
        return this;
    }

    public String getPublishTime() {
        return this.publishTime;
    }

    public Book publishTime(String publishTime) {
        this.publishTime = convertString(publishTime);
        return this;
    }

    public String getCLCIndex() {
        return this.CLCIndex;
    }

    public Book CLCIndex(String CLCIndex) {
        this.CLCIndex = convertString(CLCIndex);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (title != null ? !title.equals(book.title) : book.title != null) return false;
        if (author != null ? !author.equals(book.author) : book.author != null) return false;
        if (publisher != null ? !publisher.equals(book.publisher) : book.publisher != null)
            return false;
        if (publishTime != null ? !publishTime.equals(book.publishTime) : book.publishTime != null)
            return false;
        return !(CLCIndex != null ? !CLCIndex.equals(book.CLCIndex) : book.CLCIndex != null);
    }

    @Override
    public int hashCode() {
        return CLCIndex != null ? CLCIndex.hashCode() : 0;
    }
}

