package ru.itq.timeinstatus.service;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.dto.IssueReportsDto;
import ru.itq.timeinstatus.dto.ReportDto;
import ru.itq.timeinstatus.utils.TimeFormatter;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExcelGeneratorService {
    private StatisticService statisticService;
    private final IssueManager issueManager = ComponentAccessor.getIssueManager();

    @Autowired
    public ExcelGeneratorService(StatisticService statisticService) {
        this.statisticService = statisticService;

    }

    private ReportDto collectReportData(List<Long> issueIdList) {
        List<Issue> issueObjects = issueManager.getIssueObjects(issueIdList);
        Statistic[] statisticForIssue = statisticService.getStatisticForIssues(
                issueObjects.stream().map(Issue::getKey).toArray(String[]::new)
        );
        Map<String, List<Statistic>> groupedByIssueKey = Arrays.stream(statisticForIssue).collect(Collectors.groupingBy(Statistic::getIssueKey));
        Map<String, Long> byStatuses = Arrays.stream(statisticForIssue).collect(Collectors.toMap(
                st -> st.getLastState() + "->" + st.getNextState(),
                Statistic::getTimeSpent,
                Long::sum
        ));
        List<IssueReportsDto> byIssues = issueObjects.stream().map(
                issue -> {
                    String issueKey = issue.getKey();
                    List<Statistic> statistics = groupedByIssueKey.get(issueKey);
                    if (Objects.isNull(statistics)) {
                        return IssueReportsDto.builder()
                                .key(issueKey)
                                .issue(issue)
                                .transitionsSpent(null)
                                .build();

                    }
                    List<HashMap<String, Long>> transitionsSpent = statistics.stream()
                            .map(statistic -> {
                                HashMap<String, Long> stringLongHashMap = new HashMap<>();
                                if (Objects.isNull(statistic)) {
                                    return stringLongHashMap;
                                }
                                stringLongHashMap.put(statistic.getLastState() + "->" + statistic.getNextState(), statistic.getTimeSpent());
                                return stringLongHashMap;
                            })
                            .collect(Collectors.toList());
                    return IssueReportsDto.builder()
                            .key(issueKey)
                            .issue(issue)
                            .transitionsSpent(transitionsSpent)
                            .build();
                }
        ).collect(Collectors.toList());
        return ReportDto.builder().
                byIssues(byIssues).
                byStatuses(byStatuses).
                build();
    }

    @SneakyThrows
    public ByteArrayOutputStream generate(List<Long> issueIdList) {
        XSSFWorkbook wb = new XSSFWorkbook(new ClassPathResource("template.xlsx").getInputStream());
        ReportDto reportDto = collectReportData(issueIdList);
        List<IssueReportsDto> byIssues = reportDto.getByIssues();
        Map<String, Long> byStatuses = reportDto.getByStatuses();
        XSSFSheet mainSheet = wb.getSheetAt(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < byIssues.size(); i++) {
            IssueReportsDto issueReportsDto = byIssues.get(i);
            Issue issue = issueReportsDto.getIssue();
            XSSFRow row = mainSheet.createRow(i + 1);
            List<HashMap<String, Long>> transitionsSpent = issueReportsDto.getTransitionsSpent();
            if (Objects.nonNull(issue)) {
                createIssueSheet(wb, issue, transitionsSpent);
                fillMainData(issue, row, transitionsSpent);
            }
        }
        XSSFSheet dataSheet = wb.createSheet("Data");
        fillDiagramData(dataSheet, mainSheet, byStatuses);
        wb.write(outputStream);
        return outputStream;
    }

    private void fillDiagramData(XSSFSheet dataSheet, XSSFSheet mainSheet, Map<String, Long> byStatuses) {
        int rowNum = 0;
        for (Map.Entry<String, Long> entry : byStatuses.entrySet()) {
            String status = entry.getKey();
            Long timeSpent = entry.getValue();
            fillDiagramRow(dataSheet, rowNum, status, timeSpent);
            rowNum++;
        }
        XSSFDrawing drawing = mainSheet.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 0, 20, 20);
        XSSFChart chart = drawing.createChart(anchor);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);


        XDDFChartData chartData = chart.createData(ChartTypes.PIE, null, null);

        XDDFCategoryDataSource xddfCategoryDataSource = XDDFDataSourcesFactory.fromStringCellRange(dataSheet,
                new CellRangeAddress(0, rowNum - 1, 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet,
                new CellRangeAddress(0, rowNum - 1, 1, 1));
        chartData.addSeries(xddfCategoryDataSource, values);
        CTDLbls ctdLbls = chart.getCTChart().getPlotArea().getPieChartArray(0).addNewDLbls();
        ctdLbls.addNewShowPercent().setVal(true);
        ctdLbls.addNewShowCatName().setVal(false);
        ctdLbls.addNewShowVal().setVal(false);
        ctdLbls.addNewShowSerName().setVal(false);
        chart.plot(chartData);
    }


    private void fillMainData(Issue issue, XSSFRow row, List<HashMap<String, Long>> transitionsSpent) {
        long sum = Objects.isNull(transitionsSpent) ? 0 : transitionsSpent.stream().map(
                m -> m.values().stream().mapToLong(value -> value).sum()
        ).mapToLong(v -> v).sum();
        setCellValue(row, 0, issue.getKey());
        setCellValue(row, 1, issue.getSummary());
        setCellValue(row, 2, issue.getStatus().getName());
        setCellValue(row, 3, TimeFormatter.formatTime(sum));
    }

    private void createIssueSheet(XSSFWorkbook wb, Issue issue, List<HashMap<String, Long>> transitionsSpent) {
        XSSFSheet sheet = wb.getSheet(issue.getKey());
        XSSFSheet issueSheet = Objects.nonNull(sheet) ? sheet : wb.createSheet(issue.getKey());
        if (Objects.isNull(transitionsSpent)) {
            return;
        }
        int lastRowNum = issueSheet.getLastRowNum();
        for (int j = 0; j < transitionsSpent.size(); j++) {
            HashMap<String, Long> data = transitionsSpent.get(j);
            XSSFRow row = issueSheet.createRow(lastRowNum + j);
            data.keySet().forEach(s -> {
                setCellValue(row, 0, s);
                setCellValue(row, 1, TimeFormatter.formatTime(data.get(s)));
            });
        }
    }

    private void fillDiagramRow(XSSFSheet sheet, int rowNum, String s, Long l) {
        XSSFRow row = sheet.createRow(rowNum);
        setCellValue(row, 0, s);
        setCellValue(row, 1, l);
    }

    private void setCellValue(XSSFRow row, Integer index, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
    }

    private void setCellValue(XSSFRow row, Integer index, Long value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
    }
}
