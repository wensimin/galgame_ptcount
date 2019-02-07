package shali.tech.ptcount.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shali.tech.ptcount.entity.Post;
import shali.tech.ptcount.entity.Thread;

import java.util.Date;
import java.util.List;

public interface PostDao extends JpaRepository<Post,Long> {
    List<Post> findByThreadAndTimeBefore(Thread thread, Date date);
}
