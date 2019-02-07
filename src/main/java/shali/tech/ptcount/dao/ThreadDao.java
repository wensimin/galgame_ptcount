package shali.tech.ptcount.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shali.tech.ptcount.entity.Thread;

public interface ThreadDao extends JpaRepository<Thread,Long> {
}
