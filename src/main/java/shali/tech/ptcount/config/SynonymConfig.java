package shali.tech.ptcount.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同义词配置
 */
@ConfigurationProperties(prefix = "synonym")
public class SynonymConfig {
    /**
     * 汉化游戏
     */
    private Map<String,List<String>> chineseGames = new HashMap<>();
    /**
     * 日语游戏
     */
    private Map<String,List<String>> japaneseGames = new HashMap<>();
    /**
     * 票同义词
     */
    private Map<String,List<String>> tickets = new HashMap<>();

    public Map<String, List<String>> getTickets() {
        return tickets;
    }

    public void setTickets(Map<String, List<String>> tickets) {
        this.tickets = tickets;
    }

    public Map<String, List<String>> getChineseGames() {
        return chineseGames;
    }

    public void setChineseGames(Map<String, List<String>> chineseGames) {
        this.chineseGames = chineseGames;
    }

    public Map<String, List<String>> getJapaneseGames() {
        return japaneseGames;
    }

    public void setJapaneseGames(Map<String, List<String>> japaneseGames) {
        this.japaneseGames = japaneseGames;
    }
}
