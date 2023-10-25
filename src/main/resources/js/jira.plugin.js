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

function fillTimeSpentMap() {
    $.each($(".time-in-status-stats"), function (i, e) {
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
                        return data.labels[tooltipItem.index];
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