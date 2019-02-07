package shali.tech.ptcount.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * 楼层
 */
@Entity
public class Post {
    @Id
    private Long id;
    /**
     * 楼层
     */
    private Integer floor;
    /**
     * 作者
     */
    private String author;
    /**
     * 作者贴吧等级
     */
    private int authorLvl;
    /**
     * 内容
     */
    private String content;
    /**
     * 时间
     */
    private Date time;
    /**
     * 楼中楼回复数量
     */
    private int commentNum;
    /**
     * 所属帖子
     */
    @JoinColumn
    @ManyToOne
    private Thread thread;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAuthorLvl() {
        return authorLvl;
    }

    public void setAuthorLvl(int authorLvl) {
        this.authorLvl = authorLvl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
