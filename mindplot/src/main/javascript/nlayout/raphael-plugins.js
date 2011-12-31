Raphael.fn.drawGrid = function (x, y, w, h, wv, hv, color) {
    color = color || "#999";
    var path = ["M", x, y, "L", x + w, y, x + w, y + h, x, y + h, x, y],
        rowHeight = h / hv,
        columnWidth = w / wv;
    for (var i = 0; i < hv + 1; i++) {
        var offset = y + i * rowHeight;
        path = this.path(["M", x, offset, "L", x + w, y + i * rowHeight]);
        if (offset == 0 || offset == h) {
            path.attr({stroke: "#000"});
        } else if (offset == h/2) {
            path.attr({stroke: "#c00"})
        } else {
            path.attr({stroke: "#999"})
        }
    }
    for (var i = 0; i < wv + 1; i++) {
        var offset = x + i * columnWidth;
        path = this.path(["M", offset, y, "L", x + i * columnWidth, y + h]);
        if (offset == 0 || offset == w) {
            path.attr({stroke: "#000"});
        } else if (offset == w/2) {
            path.attr({stroke: "#c00"})
        } else {
            path.attr({stroke: "#999"})
        }
    }
//    return this.path(path.join(",")).attr({stroke: color});
    return this.path;
};