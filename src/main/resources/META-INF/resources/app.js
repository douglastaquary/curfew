(function () {
    var log = document.querySelector("#scan-log");
    if (!log || log.getAttribute("data-live") !== "true" || typeof EventSource === "undefined") {
        return;
    }
    var source = new EventSource("/varredura/eventos");
    source.addEventListener("line", function (event) {
        var item = document.createElement("li");
        item.textContent = event.data;
        log.appendChild(item);
        log.scrollTop = log.scrollHeight;
    });
    source.addEventListener("fault", function (event) {
        var item = document.createElement("li");
        item.className = "fault";
        item.textContent = event.data;
        log.appendChild(item);
        source.close();
    });
    source.addEventListener("done", function (event) {
        var parts = event.data.split(" ");
        setText("stat-items", parts[0]);
        setText("stat-rules", parts[1]);
        setText("stat-calls", parts[2]);
        setText("stat-tokens", parts[3]);
        source.close();
    });
    source.onerror = function () {
        source.close();
    };

    function setText(id, value) {
        var node = document.getElementById(id);
        if (node && value) {
            node.textContent = value;
        }
    }
}());

(function () {
    var buttons = document.querySelectorAll("[data-filter]");
    var rows = document.querySelectorAll("[data-kind]");
    if (!buttons.length) {
        return;
    }
    buttons.forEach(function (button) {
        button.addEventListener("click", function () {
            var kind = button.getAttribute("data-filter");
            buttons.forEach(function (other) {
                other.setAttribute("aria-pressed", other === button ? "true" : "false");
            });
            rows.forEach(function (row) {
                row.hidden = kind !== "todos" && row.getAttribute("data-kind") !== kind;
            });
        });
    });
}());
