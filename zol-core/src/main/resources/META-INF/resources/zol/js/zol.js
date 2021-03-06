/*
 * Javascript file for stompee.html
 * Phillip Kruger (stompee@phillip-kruger.com)
 */

var contextRoot = getContextRoot();
var webSocket;

var $logtable = $("#logtable");
var $tableBody = $("#messages");
var maxLogs = 500;

var currentRowFilters = {};

var debounceSortTableData = debounce(sortTableData, 100);

$('document').ready(function () {
    //createCaseInsensitiveContainsSelector();
    registerFilterListeners();
    openSocket();
    registerPopups();
    //addTableSortCompleteListener();

    // Get all logger names
    var url = contextRoot + "/zol/servlet?action=getAllLoggerNames";
    var loggerNames = httpGet(url);
    var result = parseJson(loggerNames);

    var loggerNamesArray = [];

    if (result.valid) {
        loggerNamesArray = result.data;
    }

    var loggerNameMenu = $('#loggerNameMenu');


    // Populate the dropdown
    for (var i = 0; i < loggerNamesArray.length; i++) {
        var $div = $("<div>", {"class": "item"});
        $div.append(loggerNamesArray[i]);
        loggerNameMenu.append($div);
    }

    $('#loggerDropdown').dropdown();

    $("#loggerDropdown").on('keyup', function (e) {
        if (e.keyCode == 13) {
            startLog();
        }
    });

    // Check if we should populate the logger
    var loggerName = getParameterByName('loggerName');

    if (loggerName) {
        $('#selectedLoggerName').text(loggerName);
    } else {
        // Else find default
        var url = contextRoot + "/zol/servlet?action=getDefaultSettings";
        var defaultSettings = httpGet(url);

        var defaultSettingsJson = {};

        var result = parseJson(defaultSettings);
        if (result.valid) {
            defaultSettingsJson = result.data;
        }

        var defaultLoggerName = defaultSettingsJson.loggerName;
        if (defaultLoggerName) {
            $('#selectedLoggerName').text(defaultLoggerName);
        }
    }

    $logtable.tablesort();
    // Make sure we stop the connection when the browser close
    window.onbeforeunload = function () {
        closeSocket();
    };
});

function getContextRoot() {
    var base = document.getElementsByTagName('base')[0];
    if (base && base.href && (base.href.length > 0)) {
        base = base.href;
    } else {
        base = document.URL;
    }

    var u = base.substr(0, base.indexOf("/", base.indexOf("/", base.indexOf("//") + 2) + 1));
    var u = u.substr(u.indexOf("//") + 2);
    var contextRoot = u.substr(u.indexOf("/"));
    return contextRoot;
}

function openSocket() {
    // Ensures only one connection is open at a time
    if (isWebSocketOpen()) {
        writeResponse("Already connected...");
        return;
    }

    // Create a new instance of the websocket
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += contextRoot + "/zol/socket";
    webSocket = new WebSocket(new_uri);

    /**
     * Binds functions to the listeners for the websocket.
     */
    webSocket.onopen = function (event) {
        if (event.data === undefined) {
            return;
        }

        writeResponse(event.data);
    };

    webSocket.onmessage = function (event) {
        try {
            // JSON Message
            var json = {};

            var result = parseJson(event.data);
            if (result.valid) {
                json = result.data;
            }

            switch (json.messageType) {
                case "log":
                    logMessage(json);
                    break;
                case "startupMessage":
                    messageStartup(json);
                    break;
            }
        } catch (e) {
            console.error(e.stack);
        }
    };

    webSocket.onclose = function () {
        writeResponse("Connection closed");
    };


    function logMessage(json) {
        var timestamp = new Date(json.timestamp);
        var timestring = timestamp.toLocaleTimeString();
        var datestring = timestamp.toLocaleDateString();
        var level = getClassLogLevel(json.logLevel);
        var tid = json.threadId;

        var user = json.user;
        if (user == null) {
            user = "";
        }

        var msg = getMessage(json);
        var sourceClassName = json.sourceClassName;
        var sourceClassNameFull = json.sourceClassNameFull;
        var sourceMethodName = json.sourceMethodName;
        var sequenceNumber = json.sequenceNumber;

        var rowStr = "<tr class='" + level + "'>\n";
        rowStr += "<td data-tooltip='" + json.logLevell + "' data-position='top left'>\n";
        rowStr += "<a class='ui " + getLogLevelColor(json.logLevel) + " empty circular label'></a><span class='sequenceHolder'>" + sequenceNumber + "</span></td>\n";
        rowStr += "<td>" + tid + "</td>\n";
        rowStr += "<td>" + user + "</td>\n";
        rowStr += "<td data-tooltip='" + datestring + "' data-position='top left'>" + timestring + "</td>\n";
        rowStr += "<td data-tooltip='" + sourceClassNameFull + "' data-position='top left'>" + sourceClassName + "</td>\n"
        rowStr += "<td>" + sourceMethodName + "</td>\n";

        var traceStr = null;

        if (json.stacktrace) {
            traceStr = "";
            for (var i in json.stacktrace) {
                var stacktrace = enhanceStacktrace(json.loggerName, json.stacktrace[i]);
                traceStr += "<div class='ui'>" + stacktrace + "</div>\n";
            }

            rowStr += "<td class='dopopup'  data-html=\"" + traceStr + "\">" + msg + "</td>\n";
            //rowStr += "<td class='ui' data-html='<b>hi</b>'>" + msg + "</td>\n";
        } else {
            rowStr += "<td colspan='2'>" + msg + "</td>\n";
        }

        rowStr += "</tr>";

        var $newRow = writeResponse(rowStr);
        $newRow.data('json', json);
        toggleRowVisibility($newRow);
        //registerPopups();
    }

    function messageStartup(json) {
        $("#applicationName").html("<h2>" + json.applicationName + "</h2>");
        setUILogLevel(json.logLevel);
    }
}

function closeSocket() {
    webSocket.close();
}

function startLog() {
    var loggerName = $('#loggerDropdown').dropdown('get text');

    if (loggerName) {
        $("#startIcon").addClass("disabled");
        $("#startIcon").prop("disabled", true);
        $("#loggerDropdown").addClass("disabled");
        $("#loggerDropdown").prop("disabled", true);

        var exceptionsOnly = $('#buttonExceptionsOnly').prop('checked');
        var logLevel = getUILogLevel();
        var filters = getRowFilters();
        //exceptionsOnly = true;
        var map = new Map();
        map.set("loggerName", loggerName);
        map.set("exceptionsOnly", exceptionsOnly);
        map.set("logLevel", logLevel);
        map.set("filters", filters);
        var msg = createJsonMessage("start", map);

        webSocket.send(msg);

        $("#stopIcon").removeClass("disabled");
        $("#stopIcon").prop("disabled", false);
        //$("#settingsIcon").removeClass("disabled");
        $("#settingsIcon").prop("disabled", false);

        $("#loggerNameDiv").removeClass("error");
    } else {
        $("#loggerNameDiv").addClass("error");
    }
}

function stopLog() {
    var loggerName = $('#loggerDropdown').dropdown('get text');

    if (loggerName) {
        $("#stopIcon").addClass("disabled");
        $("#stopIcon").prop("disabled", true);
        //$("#settingsIcon").addClass("disabled");
        //$("#settingsIcon").prop("disabled", true);

        var map = new Map();
        map.set("logger", loggerName);
        var msg = createJsonMessage("stop", map);
        webSocket.send(msg);

        $("#startIcon").removeClass("disabled");
        $("#startIcon").prop("disabled", false);
        $("#loggerDropdown").removeClass("disabled");
        $("#loggerDropdown").prop("disabled", false);
    }
}

function getUIMaxLogRecords() {
    var val = $("#maxLogRecords").val();
    return val;
}

function setUIMaxLogRecords(records) {
    $("#maxLogRecords").val(records);
}

function getUILogLevel() {
    var levelValue = $("input[name='level']:checked").val();
    return levelValue;
}

function updateMaxLogRecords() {

    var value = getUIMaxLogRecords();
    maxLogs = value;
}

function toggleLogLevel() {

    var level = getUILogLevel();

    var map = new Map();
    map.set("logLevel", level);
    var msg = createJsonMessage("setLogLevel", map);
    webSocket.send(msg);
}

function toggleExceptionsOnly() {
    var map = new Map();
    map.set("exceptionsOnly", $('#buttonExceptionsOnly').is(":checked"));
    var msg = createJsonMessage("setExceptionsOnly", map);
    webSocket.send(msg);
}

function getClassLogLevel(level) {

    if (level === "WARNING")
        return "warning";
    if (level === "SEVERE")
        return "error";
    if (level === "INFO")
        return "positive";
    if (level === "FINE")
        return "blue";
    if (level === "FINER")
        return "blue"; // TODO: Find better colors
    if (level === "FINEST")
        return "blue"; // TODO: Find better colors
    return level;
}

function getLogLevelColor(level) {

    if (level === "WARNING")
        return "orange";
    if (level === "SEVERE")
        return "red";
    if (level === "INFO")
        return "green";
    if (level === "FINE")
        return "teal";
    if (level === "FINER")
        return "blue";
    if (level === "FINEST")
        return "violet";
    return level;
}

function getMessage(json) {
    if (json.stacktrace) {
        return "<span style='cursor:pointer;'><i class='warning sign icon'></i>" + json.message + "</span>";
    }
    return json.message;
}

function enhanceStacktrace(loggerName, stacktrace) {
    var enhancedStacktraceArray = [];
    var lines = stacktrace.split('\n');
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i].trim();
        if (line) {
            var startWithAt = line.startsWith("at ");
            if (!startWithAt) {
                var parts = line.split(":");
                line = "<a class='ui red ribbon label'>" + parts[0] + "</a><span><b>" + parts[1] + "</b></span>";
            }
        }

        var isMyClass = line.includes(loggerName);

        if (isMyClass && loggerName) {
            line = "<span class='red text stacktrace-line'>" + line + "</span>";
        } else {
            line = "<span class='stacktrace-line'>" + line + "</span>";

        }

        enhancedStacktraceArray.push(line + "<br/>");
    }
    var newStacktrace = enhancedStacktraceArray.join('');
    return newStacktrace;
}

function writeResponse(text) {
    //$tableBody.innerHTML += text;
    var atBottom = isScrollPositionAtBottom();

    //setTimeout(function () {
    var $newRow = $(text).appendTo($tableBody);
    ensureLotNotExceedMaxLogs();

    if (atBottom) {
        console.log("AT BOTTOM")
        scrollToBottom();
    }

    if (isTableSorted()) {
        debounceSortTableData();
        //sortTableData();
    }

    //});

    return $newRow;

}


function toObject(map) {
    const out = Object.create(null)
    map.forEach((value, key) => {
        if (value instanceof Map) {
            out[key] = toObject(value);
        } else {
            out[key] = value;
        }
    })
    return out;
}

function createJsonMessage(doAction, params) {
    params.set("action", doAction);
    var o = toObject(params);
    return JSON.stringify(o);
}

function clearScreen() {
    //$tableBody.innerHTML = "";
    $tableBody.empty();
}

function showSettingsModal() {
    // Here get the current settings
    //var loggerName = $('#loggerDropdown').dropdown('get text');
    //if (loggerName) {
    //var url = contextRoot + "/zol/servlet?action=getLoggerLevel&name=" + loggerName;
    //var resp = httpGet(url);
    //var levelJson = JSON.parse(resp);
    //setUILogLevel(levelJson.logLevel);

    $('#modalSettings')
        .modal({
            onShow: function () {
                setUIMaxLogRecords(maxLogs);
            },
            onHide: function () {
            },
            onHidden: function () {
                cleanupModal();
            }
        })
        .modal('setting', 'transition', 'vertical flip')
        .modal('show')
    //}
}

function showAboutModal() {
    $('#modalAbout')
        .modal({
            onHidden: function () {
                cleanupModal();
            }
        })
        .modal('setting', 'transition', 'vertical flip')
        .modal('show')
    ;
}

function setUILogLevel(level) {
    if (level === "INFO")
        $("#buttonInfo").prop("checked", "checked");
    if (level === "FINE")
        $("#buttonFine").prop("checked", "checked");
    if (level === "FINER")
        $("#buttonFiner").prop("checked", "checked");
    if (level === "FINEST")
        $("#buttonFinest").prop("checked", "checked");
    if (level === "WARNING")
        $("#buttonWarning").prop("checked", "checked");
    if (level === "SEVERE")
        $("#buttonSevere").prop("checked", "checked");
    if (level === "CONFIG")
        $("#buttonConfig").prop("checked", "checked");
}

function httpGet(theUrl) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", theUrl, false); // false for synchronous request
    xmlHttp.send(null);
    return xmlHttp.responseText;
}

function getParameterByName(name, url) {
    if (!url)
        url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results)
        return null;
    if (!results[2])
        return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function registerPopups() {
    //$('table td[data-html]').popup( { on: 'click'});
    $(document).on('click', 'table td.dopopup', function () {
        $(this)
            .popup({
                on: 'click',
                lastResort: 'bottom right',
                // onShow: () => {
                //     console.log("show")
                //   popupVisible = true;
                // },
                // onHide: () => {
                //     console.log("hide")
                //     popupVisible = false;
                // }
            })
            .popup('show reposition');
    });
}

function isTableSorted() {
    var data = getSortTableData();
    if (data == null) {
        return false;
    }

    if (data.index == null) return false
    return true;
}

function sortTableData() {

    // if (popupVisible) {
    //     // when a popup is visible we don't want to loose focus, so we don't sort anymore.
    //     return;
    // }

    var data = getSortTableData();
    var dir = data.direction;
    var $th = $("#logtable thead tr th").eq(data.index);
    data.sort($th, dir);
}

function getSortTableData() {
    var data = $logtable.data('tablesort');
    return data;
}

// function addTableSortCompleteListener() {
//     $logtable.on('tablesort:complete', function(event, tablesort) {
//         $logtable.find("a.clearSort i").addClass("eraser");
//     });
// }

function cleanupModal() {
    $('body').removeClass('dimmable scrolling');
}

// Returns a function, that, as long as it continues to be invoked, will not
// be triggered. The function will be called after it stops being called for
// N milliseconds. If `immediate` is passed, trigger the function on the
// leading edge, instead of the trailing.
function debounce(func, wait, immediate) {
    var timeout;
    var result = function () {
        var context = this, args = arguments;
        var later = function () {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        var callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(context, args);
    };

    result.cancel = function () {
        clearTimeout(timeout);
        timeout = null;
    };

    return result;
}

function getScrollContainer() {
    var el = document.scrollingElement || document.documentElement;
    return el;
}

function isScrollPositionAtBottom() {
    let atBottom = false;
    //var body = document.getElementsByTagName("body")[0];

    var el = getScrollContainer();

    var scrollTop = el.scrollTop;
    var clientHeight = el.clientHeight;
    var scrollHeight = el.scrollHeight
    if (scrollTop + clientHeight == scrollHeight) {
        atBottom = true;
    }
    return atBottom;
}


function scrollToBottom() {

    // if (popupVisible) {
    //     // when a popup is visible we don't want to loose focus, so we don't scroll anymore.
    //     return;
    // }

    var el = getScrollContainer();

    var scrollHeight = el.scrollHeight
    el.scrollTop = scrollHeight;
}

function isWebSocketOpen() {
    if (webSocket == null || webSocket.readyState == WebSocket.CLOSED) {
        return false;
    }
    return true;
}

function parseJson(item) {

    var result = {
        valid: false,
        data: null
    }

    try {
        item = JSON.parse(item);

    } catch (e) {
        return result;
    }

    if (typeof item === "object" && item !== null) {
        result.valid = true;
        result.data = item;
        return result;
    }

    return result;
}

function ensureLotNotExceedMaxLogs() {
    var sequences = getOldestSequencesAsArray();
    if (sequences.length == 0) {
        return;
    }

    //Iterate all td's in second column
    $tableBody.find('.sequenceHolder').each(function () {

        // The first item in cell is an link representing the log type through a colored circle
        var seq = $(this).text();
        if (sequences.includes(seq)) {
            $row = $(this).parent().parent();
            $row.remove();
        }
    });
}

function getOldestSequencesAsArray() {

    // The table columns could be sorted so we need to iterate all table rows, get the sequences
    // and return them in ascending order.

    var values = [];

//Iterate all td's in second column
    $tableBody.find('.sequenceHolder').each(function () {
        var seq = $(this).text();
        values.push(seq);
    });

    var elementsToRemove = [];

    if (values.length > maxLogs) {
        values.sort();

        var numOfElementsToRemove = values.length - maxLogs;
        elementsToRemove = values.splice(0, numOfElementsToRemove);
    }

    return elementsToRemove;

}

function clearSort(evt) {
    evt.stopPropagation();
    var data = getSortTableData();

    if (data == null) {
        return;
    }

    data.destroy();
    $logtable.tablesort();
}

function updateRemoteFilters() {
    var filters = getRowFilters();

    var map = new Map();
    map.set("filters", filters);

    var msg = createJsonMessage("setFilters", map);
    webSocket.send(msg);
}

function applyFilters() {

    var newRowFilters = getRowFilters();
    if (rowFiltersUnchanged(newRowFilters, currentRowFilters)) {
        return;
    }

    currentRowFilters = newRowFilters;

    $tableBody.find('tr').each(function () {
        var $row = $(this);

        toggleRowVisibility($row, currentRowFilters);
    });

    updateRemoteFilters();
}

function toggleRowVisibility($row, rowFilters) {

    var logRecord = $row.data('json');

    // shouldn't happen
    if (logRecord == null) {
        return;
    }

    var show = canShowRecord(logRecord, rowFilters);

    if (show) {
        $row.show();
    } else {
        $row.hide();
    }
}

/**
 * Return true if the record passes the filter checks
 */
function canShowRecord(logRecord, rowFilters) {

    if (rowFilters == null) {
        rowFilters = getRowFilters();
    }
    var show = true;

    $.each(rowFilters, function (filterName, filterValue) {

        // if a previous filter set show to false we WILL hide
        if (!show) {
            return;
        }

        if (filterValue == null || filterValue.trim().length == 0) {
            return;
        }

        filterValue = filterValue.toLowerCase();

        var value = logRecord[filterName];
        if (value == null) {
            value = "";
        }
        value = value.toString().toLowerCase();

        if (value.indexOf(filterValue) == -1) {
            show = false;
        }
    });

    return show;
}

function getRowFilters() {
    var obj = {};
    $logtable.find('tr[data-filter-row] input').each(function () {
        var $input = $(this);
        var value = $input.val();
        var name = $input.attr('name');
        obj[name] = value;
    });
    return obj;
}

function registerFilterListeners() {

    var debounceApplyFilters = debounce(applyFilters, 500);

    $logtable.find('tr[data-filter-row] input').on('keyup', function () {
        console.log("keyup")
        debounceApplyFilters();
    });

    $logtable.find('tr[data-filter-row] input').on('change', function () {
        debounceApplyFilters.cancel();
        console.log("change")
        applyFilters();
    });
}

function rowFiltersUnchanged(a, b) {
    let aStr = JSON.stringify(a);
    let bStr = JSON.stringify(b);
    return aStr === bStr
}

