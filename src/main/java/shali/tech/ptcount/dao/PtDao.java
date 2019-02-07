package shali.tech.ptcount.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shali.tech.ptcount.entity.Pt;

import javax.transaction.Transactional;
import java.util.List;

public interface PtDao extends JpaRepository<Pt, Long> {
    List<Pt> findByThreadIdOrderByPtDesc(Long threadId);

    @Transactional
    void deleteByThreadId(Long threadId);
}
