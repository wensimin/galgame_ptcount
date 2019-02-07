package shali.tech.ptcount.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Pt {
    @Id
    @GeneratedValue
    private Long id;
    /**
     * 游戏名
     */
    private String gameName;
    /**
     * pt值
     */
    private Integer pt;
    /**
     * 所属帖子
     */
    private Long threadId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Integer getPt() {
        return pt;
    }

    public void setPt(Integer pt) {
        this.pt = pt;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pt)) {
            return false;
        }
        Pt pt = (Pt) obj;
        return pt.getGameName().equals(this.gameName) && pt.getThreadId().equals(threadId);
    }

    @Override
    public int hashCode() {
        return this.gameName.hashCode() * threadId.hashCode();
    }
}
