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
