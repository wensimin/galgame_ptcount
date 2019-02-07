package shali.tech.ptcount.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shali.tech.ptcount.entity.Comment;
import shali.tech.ptcount.entity.Post;

import java.util.Date;
import java.util.List;

public interface CommentDao  extends JpaRepository<Comment,Long> {
    List<Comment> findByPostAndTimeBefore(Post post, Date time);
}
