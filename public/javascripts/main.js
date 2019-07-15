function handleMouseIn(e) {
    e.target.parentElement.parentElement.parentElement.classList.add('active-weekday-' + e.target.dataset.dayOfWeek);
}
function handleMouseOut(e) {
    e.target.parentElement.parentElement.parentElement.classList.remove('active-weekday-' + e.target.dataset.dayOfWeek);
}

function openInfoPopup(e) {
    e.preventDefault();
    let events = JSON.parse(e.target.dataset.events);
    if (events.length === 0) {
        alert('No events on this day!');
        return;
    }

    let popupOverlay = document.createElement('div');
    popupOverlay.classList.add('popup-overlay');
    popupOverlay.addEventListener('click', e => {
        if (e.target === popupOverlay) {
            document.body.removeChild(popupOverlay)
        }
    });

    let popupContainer = document.createElement('div');
    popupContainer.classList.add('popup');
    let header = document.createElement('h2');
    header.appendChild(document.createTextNode('Events'));
    popupContainer.appendChild(header);

    let popupContent = document.createElement('div');
    popupContent.classList.add('popup-content');
    popupContainer.appendChild(popupContent);

    let eventList = document.createElement('ul');

    events.forEach(event => {
        let elem = document.createElement('li');
        elem.appendChild(document.createTextNode(event.name));

        eventList.appendChild(elem);
    });
    popupContent.appendChild(eventList);

    popupOverlay.appendChild(popupContainer);
    document.body.appendChild(popupOverlay);
}

function buildYear(yearData) {
    let yearContainer = document.getElementById('year');

    let weekOfYear = null;
    let month = null;
    let monthContainer;
    let weekDays;

    for (let dayData of yearData.days) {
        let date = new Date(dayData.date);
        let thisDayOfWeek = dayData.dayOfWeek;
        let thisWeekOfYear = dayData.weekOfYear;
        let thisMonth = date.getMonth();

        if (thisMonth !== month) {
            month = thisMonth;
            weekOfYear = null;
            monthContainer = document.createElement('div');
            monthContainer.classList.add('month', 'month-' + thisMonth);
            yearContainer.appendChild(monthContainer);
        }

        if (thisWeekOfYear !== weekOfYear) {
            weekOfYear = thisWeekOfYear;

            let weekContainer = document.createElement('div');
            weekContainer.classList.add('week', 'week-' + weekOfYear);
            weekContainer.title = 'Week ' + weekOfYear;
            monthContainer.appendChild(weekContainer);

            weekDays = new Array(7);
            for (let i = 0; i < weekDays.length; ++i) {
                let weekDay = document.createElement('div');
                weekDay.classList.add('weekday', 'weekday-' + (i + 1));
                weekContainer.appendChild(weekDay);
                weekDays[i] = weekDay;
            }
        }


        let container = weekDays[thisDayOfWeek - 1];
        let day = document.createElement('div');
        day.classList.add('day');

        day.dataset.dayOfWeek = thisDayOfWeek;
        day.dataset.weekOfYear = thisWeekOfYear;

        if (dayData.events.length > 0) {
            day.dataset.events = JSON.stringify(dayData.events);
            day.addEventListener('click', openInfoPopup);
            day.classList.add('has-events')
        } else {
            day.classList.add('no-events')
        }

        day.addEventListener('mouseenter', handleMouseIn);
        day.addEventListener('mouseleave', handleMouseOut);
        day.appendChild(document.createTextNode(date.getDate() + ''));

        container.appendChild(day);
    }
}

window.addEventListener('DOMContentLoaded', () => {
    fetch('/year')
        .then(r => r.json())
        .then(buildYear)
});