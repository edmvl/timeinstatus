function getColor(index) {
    var colorArray = [
        '#4C9AFF',
        '#C1C7D0',
        '#79E2F2',
        '#8777D9',
        '#79F2C0',
        '#FFAB00',
        '#00B8D9',
        '#0052CC',
        '#6554C0',
        '#344563',
        '#C1C7D0',
        '#403294'
    ];

    if (index > colorArray.length) {
        return '#C1C7D0';
    }

    return colorArray[index];
}

var timeSpentMap = {};
var allSpentTime = 0;
var chart = null;
var issueIds = {};

function fillTimeSpentMap(selected) {
    $.each($(".time-in-status-stats"), function (i, e) {
        var key = e.dataset.key;
        if (selected && !selected.includes(key)) {
            return;
        }
        var state = e.dataset.state;
        var spent = parseInt(e.dataset.spent);
        allSpentTime += spent;
        if (timeSpentMap[state]) {
            timeSpentMap[state] += spent;
        } else {
            timeSpentMap[state] = spent;
        }
    })
}

function getIssueSelectedKeys() {
    var result = [];
    var elements = document.querySelectorAll(".issue-selected-key");
    for (var i = 0; i < elements.length; i++) {
        result.push(elements[i])
    }
    return result;
}

function getSelectedTableItems() {
    return getIssueSelectedKeys()
        .filter(function (el) {
            return el.checked;
        })
        .map(function (el) {
            return el.id;
        });
}

function getSelectedIssueIds() {
    return getIssueSelectedKeys()
        .filter(function (el) {
            return el.checked;
        })
        .map(function (el) {
            return el.dataset.issueid;
        });
}

function loadExcel() {
    var url = AJS.contextPath() + "/rest/time-in-status/1.0/excel/download";
    url += "?issueIds=" + getSelectedIssueIds().join(",");
    window.location.replace(url);
}

function fillAllCheckBoxes() {
    var checkedAll = document.querySelector("#header-check").checked;
    getIssueSelectedKeys().forEach(function (value) {
        value.checked = checkedAll;
    })
    rerenderChart();
}

function rerenderChart() {
    timeSpentMap = {};
    fillTimeSpentMap(getSelectedTableItems());
    if (chart) chart.destroy();
    renderChart();
}

function renderChart() {
    var chartData = {
        data: [], labels: [], colors: []
    }
    var index = 0;
    $.each(timeSpentMap, function (k, v) {
        chartData.data.push(v);
        chartData.labels.push(k);
        chartData.colors.push(getColor(index))
        index++;
    });
    initiateChart(chartData);
}

function longToTime(millis) {
    var seconds = millis / 1000;
    var d = Math.floor(seconds / 32400);
    var h = Math.floor(seconds / 3600 - d * 9);
    var m = Math.floor(seconds % 3600 / 60);
    return addLeadingZero(d) + ":" + addLeadingZero(h) + ":" + addLeadingZero(m);
}

function addLeadingZero(d) {
    return d > 9 ? "" + d : "0" + d;
}

function initiateChart(chartData) {
    var data = {
        datasets: [{
            data: chartData.data,
            backgroundColor: chartData.colors
        }],
        labels: chartData.labels
    };

    chart = new Chart($("#results-chart"), {
        type: 'doughnut',
        data: data,
        options: {
            legend: {
                display: true,
                position: "right",
                align: "left"
            },
            tooltips: {
                displayColors: false,
                callbacks: {
                    title: function () {
                        return '';
                    },
                    label: function (tooltipItem, data) {
                        var datum = data.datasets[0].data[tooltipItem.index];
                        var label = data.labels[tooltipItem.index];
                        return label + "(" + longToTime(datum) + ")";
                    }
                }
            },
            plugins: {
                datalabels: {
                    formatter: function (value, ctx) {
                        var datasets = ctx.chart.data.datasets;
                        if (datasets.indexOf(ctx.dataset) === datasets.length - 1) {
                            var sum = datasets[0].data.reduce(function (a, b) {
                                return a + b;
                            }, 0);
                            return Math.round((value / sum) * 100) + '%';
                        } else {
                            return percentage;
                        }
                    },
                    color: '#100101'
                }
            }
        }
    });
}