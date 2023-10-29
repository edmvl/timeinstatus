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
        '#403294',

    ];

    if (index > colorArray.length) {
        return '#C1C7D0';
    }

    return colorArray[index];
}

var timeSpentMap = {};
var allSpentTime = 0;

function fillTimeSpentMap(selected = []) {
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

function getSelectedTableItems() {
    return new Array(...document.querySelectorAll(".issue-selected-key"))
        .filter(el => el.checked)
        .map(el => el.id);
}

function rerenderChart() {
    timeSpentMap = {};
    fillTimeSpentMap(getSelectedTableItems());
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

function longToTime(long) {
    var seconds = long / 1000;
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

    new Chart($("#results-chart"), {
        type: 'doughnut',
        data: data,
        options: {
            legend: {
                display: true,
                position: "right",
                align: "center"
            },
            tooltips: {
                displayColors: false,
                callbacks: {
                    title: function (tooltipItems, data) {
                        return '';
                    },
                    label: function (tooltipItem, data) {
                        let datum = data.datasets[0].data[tooltipItem.index];
                        let label = data.labels[tooltipItem.index];
                        return label + "(" + longToTime(datum) + ")";
                    }
                }
            },
            plugins: {
                datalabels: {
                    formatter: (value, ctx) => {
                        let datasets = ctx.chart.data.datasets;
                        if (datasets.indexOf(ctx.dataset) === datasets.length - 1) {
                            let sum = datasets[0].data.reduce((a, b) => a + b, 0);
                            return Math.round((value / sum) * 100) + '%';
                        } else {
                            return percentage;
                        }
                    },
                    color: '#100101',
                }
            }
        },
    });
}