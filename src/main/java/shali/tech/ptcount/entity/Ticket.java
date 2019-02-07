package shali.tech.ptcount.entity;

import java.util.Date;

/**
 * 票
 */
public class Ticket {
    private String username;
    private String context;
    private Date time;
    private int floor;
    public Ticket() {
    }

    public Ticket(String username, String context, Date time,int floor) {
        this.floor = floor;
        this.username = username;
        this.context = context;
        this.time = time;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
