package ru.itq.timeinstatus.service;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.dto.IssueReportsDto;
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

    private List<IssueReportsDto> collectReportData(List<Long> issueIdList) {
        List<Issue> issueObjects = issueManager.getIssueObjects(issueIdList);
        Statistic[] statisticForIssue = statisticService.getStatisticForIssues(
                issueObjects.stream().map(Issue::getKey).toArray(String[]::new)
        );
        Map<String, List<Statistic>> groupedByIssueKey = Arrays.stream(statisticForIssue).collect(Collectors.groupingBy(Statistic::getIssueKey));
        return issueObjects.stream().map(
                issue -> {
                    String issueKey = issue.getKey();
                    List<Statistic> statistics = groupedByIssueKey.get(issueKey);
                    if (Objects.isNull(statistics)) {
                        return null;
                    }
                    List<HashMap<String, Long>> transitionsSpent = statistics.stream()
                            .map(statistic -> {
                                HashMap<String, Long> stringLongHashMap = new HashMap<>();
                                if (Objects.isNull(statistic)) {
                                    return stringLongHashMap;
                                }
                                stringLongHashMap.put(statistic.getLastTime() + "->" + statistic.getNextState(), statistic.getTimeSpent());
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
    }

    @SneakyThrows
    public ByteArrayOutputStream generate(List<Long> issueIdList) {
        XSSFWorkbook wb = new XSSFWorkbook(new ClassPathResource("template.xlsx").getInputStream());
        List<IssueReportsDto> collectReportData = collectReportData(issueIdList);
        XSSFSheet sheet = wb.getSheetAt(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < collectReportData.size(); i++) {
            IssueReportsDto issueReportsDto = collectReportData.get(i);
            XSSFRow row = sheet.createRow(i);
            Issue issue = issueReportsDto.getIssue();
            List<HashMap<String, Long>> transitionsSpent = issueReportsDto.getTransitionsSpent();
            if (Objects.nonNull(issue)) {
                long sum = transitionsSpent.stream().map(
                        m -> m.values().stream().mapToLong(value -> value).sum()
                ).mapToLong(v -> v).sum();
                setCellValue(row, 0, issue.getKey());
                setCellValue(row, 1, issue.getSummary());
                setCellValue(row, 2, issue.getStatus().getName());
                setCellValue(row, 3, TimeFormatter.formatTime(sum));
            }
        }
        wb.write(outputStream);
        return outputStream;
    }

    private void setCellValue(XSSFRow row, Integer index, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellValue(value);
    }
}
