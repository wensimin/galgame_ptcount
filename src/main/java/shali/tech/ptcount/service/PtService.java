package shali.tech.ptcount.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shali.tech.ptcount.config.SynonymConfig;
import shali.tech.ptcount.dao.CommentDao;
import shali.tech.ptcount.dao.PostDao;
import shali.tech.ptcount.dao.PtDao;
import shali.tech.ptcount.dao.ThreadDao;
import shali.tech.ptcount.entity.*;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PtService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private PtDao ptDao;
    private PostDao postDao;
    private ThreadDao threadDao;
    private CommentDao commentDao;
    private SynonymConfig synonymConfig;
    @Value("${prop.chinese.threadId}")
    private Long chineseThreadId;
    @Value("${prop.japanese.threadId}")
    private Long japaneseThreadId;
    @Value("${prop.lvlLimit}")
    private int lvlLimit;
    @Value("${prop.starLimit}")
    private int starLimit;
    @Value("${prop.heartLimit}")
    private int heartLimit;
    @Value("${prop.startAllLimit}")
    private int startAllLimit;
    @Value("${prop.heartAllLimit}")
    private int heartAllLimit;
    /**
     * 截至时间
     */
    private Date dateLimit;
    /**
     * 捕获pt正则A
     */
    private static String ptRegexA = "%s *(星+|心+)?(星+|心+)?";
    /**
     * 捕获pt正则B
     */
    private static String ptRegexB = "%s *([0-9]星+|[0-9]心+)?([0-9]星+|[0-9]心+)?";

    @Autowired
    public PtService(PtDao ptDao, PostDao postDao, ThreadDao threadDao, CommentDao commentDao, SynonymConfig synonymConfig) {
        this.ptDao = ptDao;
        this.postDao = postDao;
        this.threadDao = threadDao;
        this.commentDao = commentDao;
        this.synonymConfig = synonymConfig;
        try {
            //TODO HARD CODE
            dateLimit = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-02-08 24:00:00");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 执行统计pt任务
     */
    public void ptCount() {
        ptCount(chineseThreadId, synonymConfig.getChineseGames());
        ptCount(japaneseThreadId, synonymConfig.getJapaneseGames());
    }

    /**
     * 执行统计pt
     *
     * @param threadId
     * @param games
     */
    @Transactional
    public void ptCount(Long threadId, Map<String, List<String>> games) {
        List<Post> posts = postDao.findByThreadAndTimeBefore(threadDao.getOne(threadId), dateLimit);
        List<Ticket> tickets = new ArrayList<>();
        for (Post post : posts) {
            // 主楼剔除
            if (post.getFloor() == 1) {
                continue;
            }
            Ticket ticket;
            // 投票等级限制
            if (post.getAuthorLvl() < lvlLimit) {
                continue;
            }
            // 主楼如果有投票内容
            if (hasTicket(games, post.getContent())) {
                ticket = new Ticket(post.getAuthor(), post.getContent(), post.getTime(), post.getFloor());
                addTicket(tickets, ticket);
            } else {
                logger.debug("楼层" + post.getFloor() + "无效票:" + post.getContent());
            }
            // 如果有楼中楼
            if (post.getCommentNum() > 0) {
                List<Comment> comments = commentDao.findByPostAndTimeBefore(post, dateLimit);
                for (Comment comment : comments) {
                    // 楼层主回复 且拥有投票内容
                    if (comment.getAuthor().equals(post.getAuthor()) &&
                            hasTicket(games, comment.getContent())) {
                        ticket = new Ticket(comment.getAuthor(), comment.getContent(), comment.getTime(), post.getFloor());
                        addTicket(tickets, ticket);
                    }
                }
            }
        }
        // 转化计算pt
        List<Pt> pts = ticketToPt(tickets, games, threadId);
        ptDao.deleteByThreadId(threadId);
        ptDao.saveAll(pts);
    }

    /**
     * 转化计算pt
     *
     * @param tickets
     */
    private List<Pt> ticketToPt(List<Ticket> tickets, Map<String, List<String>> games, Long threadId) {
        List<Pt> allPts = new ArrayList<>();
        // 有效票统计
        int goodTicket = 0;
        for (Ticket ticket : tickets) {
            // 票同义词中的星心等词会影响到游戏名,先进行票统一在进行游戏名统一
            replaceTicket(ticket);
            replaceGameName(ticket, games);
            List<Pt> pts = ticketToPt(ticket, games, threadId);
            // 无效票
            if (pts == null || pts.isEmpty()) {
                logger.debug("无效票,楼层:" + ticket.getFloor());
                continue;
            }
            goodTicket++;
            for (Pt pt : pts) {
                int index = allPts.indexOf(pt);
                if (index >= 0) {
                    Pt oldPt = allPts.get(index);
                    oldPt.setPt(oldPt.getPt() + pt.getPt());
                } else {
                    allPts.add(pt);
                }
            }
        }
        logger.debug("帖子" + threadId + "共计有效票:" + goodTicket + ",原票数:" + tickets.size());
        return allPts;
    }

    /**
     * ticket 转化为pt
     *
     * @param ticket
     * @param games
     * @param threadId
     * @return
     */
    private List<Pt> ticketToPt(Ticket ticket, Map<String, List<String>> games, Long threadId) {
        List<Pt> pts = new ArrayList<>();
        int starCount = 0;
        int heartCount = 0;
        //FIXME
        if (ticket.getFloor() == 870)
            System.out.println("a");
        for (String game : games.keySet()) {
            int star = 0;
            int heart = 0;
            String context = ticket.getContext();
            Pattern patternA = Pattern.compile(String.format(ptRegexA, game));
            Matcher matcherA = patternA.matcher(context);
            if (matcherA.find()) {
                String group1 = matcherA.group(1);
                String group2 = matcherA.group(2);
                // 如果两个均未捕获则使用方案B
                if (group1 != null || group2 != null) {
                    //regexA: 通过长度直接获取
                    if (group1 != null) {
                        if (group1.contains("心")) {
                            heart = group1.length();
                        } else {
                            star = group1.length();
                        }
                    }
                    if (group2 != null) {
                        if (group2.contains("心")) {
                            heart = group2.length();
                        } else {
                            star = group2.length();
                        }
                    }
                } else {
                    Pattern patternB = Pattern.compile(String.format(ptRegexB, game));
                    Matcher matcherB = patternB.matcher(context);
                    matcherB.find();
                    group1 = matcherB.group(1);
                    group2 = matcherB.group(2);
                    // 能匹配却无法捕获的票数
                    if (group1 == null && group2 == null) {
                        return null;
                    }
                    // regexB 通过捕获的数字+ticket获取
                    if (group1 != null) {
                        if (group1.contains("心")) {
                            heart = Integer.parseInt(group1.substring(0, 1));
                        } else {
                            star = Integer.parseInt(group1.substring(0, 1));
                        }
                    }
                    if (group2 != null) {
                        if (group2.contains("心")) {
                            heart = Integer.parseInt(group2.substring(0, 1));
                        } else {
                            star = Integer.parseInt(group2.substring(0, 1));
                        }
                    }
                }
                if (star == 0 && heart == 0) {
                    logger.warn("zero!!!");
                    return null;
                }
                // 超出单个游戏限制【每个游戏最多分配3个☆】【一个游戏只能有一个💗】
                if (star > starLimit || heart > heartLimit) {
                    return null;
                }
                starCount += star;
                heartCount += heart;
                //TODO HARD CODE 星为234pt,心为4pt
                int ptNum = (star == 0 ? 0 : (star + 1)) + heart * 4;
                Pt pt = new Pt();
                pt.setThreadId(threadId);
                pt.setPt(ptNum);
                pt.setGameName(game);
                pts.add(pt);
            }
        }
        //超出全部限制 每个人有【5个☆】和【2颗💗】
        if (starCount > startAllLimit || heartCount > heartAllLimit) {
            return null;
        }
        //如果你共计给2及2以上的游戏投票，则必须用完所有的☆和💗，否则投票无效
        if (pts.size() >= 2 && (starCount != startAllLimit || heartCount != heartAllLimit)) {
            return null;
        }
        // ！如果你只给1个游戏投票，则pts减半
        if (pts.size() == 1) {
            Pt pt = pts.stream().findFirst().get();
            pt.setPt(pt.getPt() / 2);
        }
        return pts;
    }

    /**
     * 替换游戏名不规范内容
     *
     * @param ticket
     */
    private void replaceGameName(Ticket ticket, Map<String, List<String>> games) {
        for (Map.Entry<String, List<String>> entry : games.entrySet()) {
            for (String s : entry.getValue()) {
                String newContext = ticket.getContext().replace(s, entry.getKey());
                ticket.setContext(newContext);
            }
        }
    }

    /**
     * 替换ticket不规范内容
     *
     * @param ticket
     */
    private void replaceTicket(Ticket ticket) {
        for (Map.Entry<String, List<String>> entry : synonymConfig.getTickets().entrySet()) {
            for (String s : entry.getValue()) {
                String newContext = ticket.getContext().replace(s, entry.getKey());
                ticket.setContext(newContext);
            }
        }
    }

    /**
     * 新增票到集合中
     * 同时过滤
     * 同一用户多张票取时间最新
     *
     * @param tickets 集合
     * @param ticket  票
     */
    private void addTicket(List<Ticket> tickets, Ticket ticket) {
        Ticket badTicket = null;
        for (Ticket t : tickets) {
            // 是否是同一用户重复票
            if (t.getUsername().equals(ticket.getUsername())) {
                // 如果新增票时间大于原来票则删除原来票
                if (ticket.getTime().after(t.getTime())) {
                    badTicket = t;
                } else {
                    // 重复票且不是新票则直接废弃
                    return;
                }
            }
        }
        //删除原票
        if (badTicket != null) {
            tickets.remove(badTicket);
        }
        tickets.add(ticket);
    }

    /**
     * 是否有投票内容
     *
     * @param games   游戏名
     * @param context 回复内容
     * @return 是否有投票内容
     */
    private boolean hasTicket(Map<String, List<String>> games, String context) {
        boolean rs = false;
        for (Map.Entry<String, List<String>> entry : games.entrySet()) {
            String key = entry.getKey();
            // 本名
            if (hasGame(context, key)) {
                return true;
            } else {
                // 别名
                for (String value : entry.getValue()) {
                    if (hasGame(context, value)) {
                        return true;
                    }
                }
            }
        }
        return rs;
    }

    /**
     * 是否含有游戏名
     *
     * @return
     */
    private boolean hasGame(String context, String key) {
        // 忽略大小写
        return context.toLowerCase().contains(key.toLowerCase());
    }

    /**
     * 查找对应类型的pt统计
     *
     * @param threadType
     * @return
     */
    public List<Pt> findByType(ThreadType threadType) {
        Long id = null;
        switch (threadType) {
            case chinese:
                id = chineseThreadId;
                break;
            case japanese:
                id = japaneseThreadId;
                break;
        }
        return ptDao.findByThreadIdOrderByPtDesc(id);
    }
}
