package shali.tech.ptcount.web;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shali.tech.ptcount.entity.Pt;
import shali.tech.ptcount.entity.ThreadType;
import shali.tech.ptcount.service.PtService;
import shali.tech.ptcount.utils.ChartUtils;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

@RestController
@RequestMapping("api")
public class Api {
    @Autowired
    private PtService ptService;

    @PostMapping("pt/count")
    public String count() {
        ptService.ptCount();
        return "ok";
    }

    @GetMapping("pt/{threadType}")
    public List<Pt> getPtByThread(@PathVariable ThreadType threadType) {
        return ptService.findByType(threadType);
    }

    @GetMapping("pt/pie/{threadType}")
    public void getPiePt(@PathVariable ThreadType threadType, HttpServletResponse response) throws IOException {
        List<Pt> pts = ptService.findByType(threadType);
        DefaultPieDataset dataSet = new DefaultPieDataset();
        pts.forEach(pt -> {
            dataSet.setValue(pt.getGameName(), pt.getPt());
        });
        ChartUtils.setChartTheme();
        JFreeChart chart = ChartFactory.createPieChart(
                "投票结果饼图",
                dataSet,
                true, true, true);
        PiePlot plot = (PiePlot) chart.getPlot();
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        plot.setLabelGenerator(gen);
        ChartUtils.setAntiAlias(chart);
        ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 1000, 800);
    }

}
