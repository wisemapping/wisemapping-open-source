/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

/**
 * RUZEE.ShadedBorder 0.2
 * (c) 2006 Steffen Rusitschka
 *
 * RUZEE.ShadedBorder is freely distributable under the terms of an MIT-style license.
 * For details, see http://www.ruzee.com/
 */

var RUZEE = window.RUZEE || {};

RUZEE.ShadedBorder = {

    create: function(opts) {
        var isie = /msie/i.test(navigator.userAgent) && !window.opera;
        function sty(el, h) {
            for (k in h) {
                if (/ie_/.test(k)) {
                    if (isie) el.style[k.substr(3)] = h[k];
                } else el.style[k] = h[k];
            }
        }
        function crdiv(h) {
            var el = document.createElement("div");
            el.className = "sb-gen";
            sty(el, h);
            return el;
        }
        function op(v) {
            v = v < 0 ? 0 : v;
            v = v > 0.99999 ? 0.99999 : v;
            if (isie) {
                return " filter:alpha(opacity=" + (v * 100) + ");";
            } else {
                return " opacity:" + v + ';';
            }
        }

        var sr = opts.shadow || 0;
        var r = opts.corner || 0;
        var bor = 0;
        var bow = opts.border || 0;
        var shadow = sr != 0;
        var lw = r > sr ? r : sr;
        var rw = lw;
        var th = lw;
        var bh = lw;
        if (bow > 0) {
            bor = r;
            r = r - bow;
        }
        var cx = r != 0 && shadow ? Math.round(lw / 3) : 0;
        var cy = cx;
        var cs = Math.round(cx / 2);
        var iclass = r > 0 ? "sb-inner" : "sb-shadow";
        var sclass = "sb-shadow";
        var bclass = "sb-border";
        var edges = opts.edges || "trlb";
        if (!/t/i.test(edges)) th = 0;
        if (!/b/i.test(edges)) bh = 0;
        if (!/l/i.test(edges)) lw = 0;
        if (!/r/i.test(edges)) rw = 0;

        var p = { position:"absolute", left:"0", top:"0", width:lw + "px", height:th + "px",
            ie_fontSize:"1px", overflow:"hidden" };
        var tl = crdiv(p);
        delete p.left;
        p.right = "0";
        p.width = rw + "px";
        var tr = crdiv(p);
        delete p.top;
        p.bottom = "0";
        p.height = bh + "px";
        var br = crdiv(p);
        delete p.right;
        p.left = "0";
        p.width = lw + "px";
        var bl = crdiv(p);

        var tw = crdiv({ position:"absolute", width:"100%", height:th + "px", ie_fontSize:"1px",
            top:"0", left:"0", overflow:"hidden" });
        var t = crdiv({ position:"relative", height:th + "px", ie_fontSize:"1px", marginLeft:lw + "px",
            marginRight:rw + "px", overflow:"hidden" });
        tw.appendChild(t);

        var bw = crdiv({ position:"absolute", left:"0", bottom:"0", width:"100%", height:bh + "px",
            ie_fontSize:"1px", overflow:"hidden" });

        var b = crdiv({ position:"relative", height:bh + "px", ie_fontSize:"1px", marginLeft:lw + "px",
            marginRight:rw + "px", overflow:"hidden" });

        bw.appendChild(b);

        var mw = crdiv({ position:"absolute", top:(-bh) + "px", left:"0", width:"100%", height:"100%",
            overflow:"hidden", ie_fontSize:"1px" });

        function corner(el, t, l) {
            var w = l ? lw : rw;
            var h = t ? th : bh;
            var s = t ? cs : -cs;
            var dsb = [];
            var dsi = [];
            var dss = [];

            var xp = 0;
            var xd = 1;
            if (l) {
                xp = w - 1;
                xd = -1;
            }
            for (var x = 0; x < w; ++x) {
                var yp = 0;
                var yd = 1;
                if (t) {
                    yp = h - 1;
                    yd = -1;
                }
                for (var y = 0; y < h; ++y) {
                    var div = '<div style="position:absolute; top:' + yp + 'px; left:' + xp + 'px; ' +
                              'width:1px; height:1px; overflow:hidden;';

                    var xc = x - cx;
                    var yc = y - cy - s;
                    var d = Math.sqrt(xc * xc + yc * yc);
                    var doShadow = false;

                    if (r > 0) {
                        // draw border
                        if (xc < 0 && yc < bor && yc >= r || yc < 0 && xc < bor && xc >= r) {
                            dsb.push(div + '" class="' + bclass + '"></div>');
                        } else
                            if (d < bor && d >= r - 1 && xc >= 0 && yc >= 0) {
                                var dd = div;
                                if (d >= bor - 1) {
                                    dd += op(bor - d);
                                    doShadow = true;
                                }
                                dsb.push(dd + '" class="' + bclass + '"></div>');
                            }

                            // draw inner
                        var dd = div + ' z-index:2;';
                        if (xc < 0 && yc < r || yc < 0 && xc < r) {
                            dsi.push(dd + '" class="' + iclass + '"></div>');
                        } else
                            if (d < r && xc >= 0 && yc >= 0) {
                                if (d >= r - 1) {
                                    dd += op(r - d);
                                    doShadow = true;
                                }
                                dsi.push(dd + '" class="' + iclass + '"></div>');
                            } else doShadow = true;
                    } else doShadow = true;

                    // draw shadow
                    if (sr > 0 && doShadow) {
                        d = Math.sqrt(x * x + y * y);
                        if (d < sr) {
                            dss.push(div + ' z-index:0; ' + op(1 - (d / sr)) + '" class="' + sclass + '"></div>');
                        }
                    }
                    yp += yd;
                }
                xp += xd;
            }
            el.innerHTML = dss.concat(dsb.concat(dsi)).join('');
        }

        function mid(mw) {
            var ds = [];

            ds.push('<div style="position:relative; top:' + (th + bh) + 'px;' +
                    ' height:10000px; margin-left:' + (lw - r - cx) + 'px;' +
                    ' margin-right:' + (rw - r - cx) + 'px; overflow:hidden;"' +
                    ' class="' + iclass + '"></div>');

            var dd = '<div style="position:absolute; width:1px;' +
                     ' top:' + (th + bh) + 'px; height:10000px;';
            for (var x = 0; x < lw - r - cx; ++x) {
                ds.push(dd + ' left:' + x + 'px;' + op((x + 1.0) / lw) +
                        '" class="' + sclass + '"></div>');
            }

            for (var x = 0; x < rw - r - cx; ++x) {
                ds.push(dd + ' right:' + x + 'px;' + op((x + 1.0) / rw) +
                        '" class="' + sclass + '"></div>');
            }

            if (bow > 0) {
                var su = ' width:' + bow + 'px;' + '" class="' + bclass + '"></div>';
                ds.push(dd + ' left:' + (lw - bor - cx) + 'px;' + su);
                ds.push(dd + ' right:' + (rw - bor - cx) + 'px;' + su);
            }

            mw.innerHTML = ds.join('');
        }

        function tb(el, t) {
            var ds = [];
            var h = t ? th : bh;
            var dd = '<div style="height:1px; overflow:hidden; position:absolute;' +
                     ' width:100%; left:0px; ';
            var s = t ? cs : -cs;
            for (var y = 0; y < h - s - cy - r; ++y) {
                ds.push(dd + (t ? 'top:' : 'bottom:') + y + 'px;' + op((y + 1) * 1.0 / h) +
                        '" class="' + sclass + '"></div>');
            }
            if (y >= bow) {
                ds.push(dd + (t ? 'top:' : 'bottom:') + (y - bow) + 'px;' +
                        ' height:' + bow + 'px;" class="' + bclass + '"></div>');
            }

            ds.push(dd + (t ? 'top:' : 'bottom:') + y + 'px;' +
                    ' height:' + (r + cy + s) + 'px;" class="' + iclass + '"></div>');

            el.innerHTML = ds.join('');
        }

        corner(tl, true, true);
        corner(tr, true, false);
        corner(bl, false, true);
        corner(br, false, false);
        mid(mw);
        tb(t, true);
        tb(b, false);

        return {
            render: function(el) {
                if (typeof el == 'string') el = document.getElementById(el);
                if (el.length != undefined) {
                    for (var i = 0; i < el.length; ++i) this.render(el[i]);
                    return;
                }
                // remove generated children
                var node = el.firstChild;
                while (node) {
                    var nextNode = node.nextSibling;
                    if (node.nodeType == 1 && node.className == 'sb-gen')
                        el.removeChild(node);
                    node = nextNode;
                }

                var iel = el.firstChild;

                var twc = tw.cloneNode(true);
                var mwc = mw.cloneNode(true);
                var bwc = bw.cloneNode(true);

                el.insertBefore(tl.cloneNode(true), iel);
                el.insertBefore(tr.cloneNode(true), iel);
                el.insertBefore(bl.cloneNode(true), iel);
                el.insertBefore(br.cloneNode(true), iel);
                el.insertBefore(twc, iel);
                el.insertBefore(mwc, iel);
                el.insertBefore(bwc, iel);

                if (isie) {
                    function resize() {
                        twc.style.width = bwc.style.width = mwc.style.width = el.offsetWidth + "px";
                        mwc.firstChild.style.height = el.offsetHeight + "px";
                    }
                    el.onresize = resize;
                    resize();
                }
            }
        };
    }
};

// add our styles to the document
document.write(
        '<style type="text/css">' +
        '.sb, .sbi, .sb *, .sbi * { position:relative;}' +
        '* html .sb, * html .sbi { height:1%; }' +
        '.sbi { display:inline-block; }' +
        '.sb-inner { background:#ddd; }' +
        '.sb-shadow { background:#000; }' +
        '.sb-border { background:#bbb; }' +
        '</style>'
        );
