function renderD3Tree(hr,initLoad,label){
    var treeData = hr.network;
    var redrawObj = constructExtraLinks(hr.extraLinks);
    var multiParentLinks = constructMultiParentLink(hr.multipleParentLinks);
    var totalNodes = 0;
    var i = 0;
    var duration = 750;
    var root;
    var maxLabelLength = 0;
    var viewerWidth = $(document).width();
    var viewerHeight = $(document).height();
    if (viewerWidth > 1100) {
        viewerWidth = 1100;
    }
    if(viewerHeight > 750) {
        viewerHeight = 750;
    }

    var tree = d3.layout.tree().size([viewerHeight, viewerWidth]);

    var diagonal = d3.svg.diagonal().projection(function (d) {
            return [d.y, d.x];
        }
    );

    function visit(parent, visitFn, childrenFn) {
        if (!parent){
            return;
        }
        visitFn(parent);
        var children = childrenFn(parent);
        if (children) {
            var count = children.length;
            for (var i = 0; i < count; i++) {
                visit(children[i], visitFn, childrenFn);
            }
        }
    }

    visit(treeData, function (d) {
        totalNodes++;
        if(initLoad) {
           maxLabelLength = Math.max(d.name.length, maxLabelLength);
        }
    }, function (d) {
        return d.children && d.children.length > 0 ? d.children : null;
    });


    function sortTree() {
        tree.sort(function (a, b) {
            return b.name.toLowerCase() < a.name.toLowerCase() ? 1 : -1;
        });
    }
    sortTree();

    function zoom() {
        svgGroup.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    }


    // define the zoomListener which calls the zoom function on the "zoom" event constrained within the scaleExtents
    var zoomListener = d3.behavior.zoom().scaleExtent([0.1, 3]).on("zoom", zoom);

    // define the baseSvg, attaching a class for styling and the zoomListener
    d3.select("#tree-container").select("svg").remove();
    var baseSvg = d3.select("#tree-container").append("svg")
        .attr("width", viewerWidth)
        .attr("height", viewerHeight)
        //.attr("class", "overlay")
        .call(zoomListener);

    // Function to center node when clicked/dropped so node doesn't get lost when collapsing/moving with large amount of children.
    function centerNode(source) {
        scale = zoomListener.scale();
        x = -source.y0;
        y = -source.x0;
        x = x * scale ;
        y = y * scale + viewerHeight / 2;
        d3.select('g').transition()
            .duration(duration)
            .attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
        zoomListener.scale(scale);
        zoomListener.translate([x, y]);
    }


    // Toggle children function
    function toggleChildren(d) {
        if (d.children) {
            collapseAll(d.children);
            d._children = d.children;
            d.children = null;
        } else if (d._children) {
            d.children = d._children;
            d._children = null;
        }
        return d;
    }

    function expandAll(d,depth) {
        if(depth > 1) {
            for (var i = 0; i < d.length; i++) {
                if (d[i]._children) {
                    expandAll(d[i]._children, depth - 1);
                    d[i].children = d[i]._children;
                    d[i]._children = null;
                }
            }
        }
    }

    function collapseAll(d) {
        for (var i = 0; i < d.length; i++) {
            if(d[i].children) {
                collapseAll(d[i].children);
                d[i]._children = d[i].children;
                d[i].children = null;
            }
        }
    }

    // Toggle children on click.
    function click(d) {
        d3.selectAll(".dotted-link").remove();
        d3.selectAll(".multi-link").remove();
        if (d3.event.defaultPrevented) return; // click suppressed
        d = toggleChildren(d);
        update(d);
        centerNode(d);
        setTimeout(function(){ drawExtra(d) }, 1000);
        setTimeout(function(){ drawMultiParentLink(d) }, 2000);
    }


    var div = document.getElementById("toolt");

    // add a popup on mouseover
    function mouseover(d) {
        var a = "";
        if(d.children) {
            a += label + " : " +  d.children.length +  "<br/>";
        } else if(d._children) {
            a += label + " : " +  d._children.length +  "<br/>";
        }
        if(checkNotNullEmpty(a)) {
            div.innerHTML = a;
            div.style.left = (d3.event.pageX - 75) + "px";
            div.style.top = (d3.event.pageY - 50 - viewerHeight / 2) + "px";
            div.style.display = "block";
            div.style.fontSize = "13px";
            div.style.padding = "5px";
            div.className += "popover";
        }
        d3.select(this).select('circle').attr('r',6).attr('stroke-width',2.5);

    }

    // Toggle children on click.
    function mouseout(d) {
        div.style.display = "none";
        div.className = "";
        d3.select(this).select('circle').attr('r',4.5).attr('stroke-width',1.5);
    }

    function getChildren(d) {
        if(d.children) {
            return d.children;
        }else if(d._children) {
            return d._children;
        } else {
            return [];
        }
    }

    function countChildren(d) {
        var children;
        if(d.children || d._children){
            children = d.children ? d.children : d._children;
            return children.length;
        }
        return 0;
    }

    function constructExtraLinks(data) {
        var redrawObj = [];
        if(checkNotNullEmpty(data)) {
            for(var i in data) {
                var destinations = data[i];
                for(var j=0; j< destinations.length; j++) {
                    redrawObj.push({"source":"#custom" + i, "target":"#custom" + destinations[j]});
                }
            }

            return redrawObj;
        }

    }

    function constructMultiParentLink(data) {
        var drawMultiParentLink = [];
        if(checkNotNullEmpty(data)) {
            for(var i in data) {
                var sources = data[i];
                for(var j=0; j < sources.length; j++) {
                    drawMultiParentLink.push({"source":"#custom" + sources[j], "target":"#custom" + i});
                }
            }
            return drawMultiParentLink;
        }
    }
    function drawExtra(d){
        d3.selectAll(".dotted-link").remove();

        if(redrawObj != null && redrawObj.length > 0) {
            for(var i=0; i<redrawObj.length; i++) {
                if(d3.select(redrawObj[i].source) != undefined && d3.select(redrawObj[i].target) != undefined) {
                    var src = d3.select(redrawObj[i].source);
                    var trgt = d3.select(redrawObj[i].target);
                    drawExtraLink(src,trgt);
                }
            }
        }
    }

    function drawMultiParentLink(d) {
        d3.selectAll(".multi-link").remove();
        if(multiParentLinks != null && multiParentLinks.length > 0) {
            for(var i=0; i<multiParentLinks.length; i++) {
                if(d3.select(multiParentLinks[i].source) != undefined && d3.select(multiParentLinks[i].target) != undefined) {
                    var src = d3.select(multiParentLinks[i].source);
                    var trgt = d3.select(multiParentLinks[i].target);
                    drawMultiLink(src,trgt);
                }
            }
        }
    }

    function drawMultiLink(sourceId,targetId) {
        if((sourceId[0] != null && sourceId[0][0] != null) && (targetId[0] != null && targetId[0][0] != null)) {
            var diagonal = d3.svg.diagonal()
                .projection(function (d) {
                    return [d.y, d.x];
                });
            var link = d3.select("g").insert("path", "g").attr("class", "multi-link")
                .attr("d", function (d) {
                    return diagonal({
                        source: {
                            x: d3.transform(sourceId.attr("transform")).translate[1],
                            y: d3.transform(sourceId.attr("transform")).translate[0]
                        },
                        target: {
                            x: d3.transform(targetId.attr("transform")).translate[1],
                            y: d3.transform(targetId.attr("transform")).translate[0]
                        }
                    });
                });
        }
    }

    function drawExtraLink(sourceId,targetId) {
        if((sourceId[0] != null && sourceId[0][0] != null) && (targetId[0] != null && targetId[0][0] != null)) {
            var diagonal = d3.svg.diagonal()
                .projection(function (d) {
                    return [d.y, d.x];
                });
            var link = d3.select("g").insert("path", "g").attr("class", "dotted-link")
                .attr("d", function (d) {
                    return diagonal({
                        source: {
                            x: d3.transform(sourceId.attr("transform")).translate[1],
                            y: d3.transform(sourceId.attr("transform")).translate[0]
                        },
                        target: {
                            x: d3.transform(targetId.attr("transform")).translate[1],
                            y: d3.transform(targetId.attr("transform")).translate[0]
                        }
                    });
                });
        }
    }

    function returnLevelColor(level) {
        var color = "#fff";
        if(checkNotNullEmpty(level)) {
            switch (level) {
                case 1:
                    color = "#85144b";
                    break;
                case 2:
                    color = "#FF851B";
                    break;
                case 3:
                    color = "#0074D9";
                    break;
                case 4:
                    color = "#2ECC40";
                    break;
                case 5:
                    color = "#FF1493";
                    break;
                case 6:
                    color = "#AAAAAA";
                    break;
                case 7:
                    color = "#001f3f";
                    break;
                case 8:
                    color = "#3D9970";
                    break;
                case 9:
                    color = "#FFDC00";
                    break;
                default:
                    color = "#FF4136";
                    break;
            }
            return color;
        }
    }

    function getChildrenCountText(d) {
        var txt = undefined;
        var level = d.level;
        var children = countChildren(d);
        if(children > 0) {
            txt = "(" + "<span style='color:"+returnLevelColor(level+1) +";font-size:10px;font-family:sans-serif'>" + children + "</span>";
        } else {
            return undefined;
        }
        if(txt) {
            var childrenObj = getChildren(d);
            do {
                var childrenCnt = gc(childrenObj);
                ++level;
                if(childrenCnt > 0) {
                    txt += ("," + "<span style='color:"+returnLevelColor(level+1) +";font-size:10px;font-family:sans-serif'>" + childrenCnt + "</span>");
                    var cObj = [];
                    childrenObj.forEach(function(dd) {
                        getChildren(dd).forEach(function (cd) {
                            cObj.push(cd);
                        });
                    });
                    childrenObj = cObj;
                }
            } while (childrenCnt > 0);
        }
        return txt + ")";
    }

    function gc(d) {
        var cnt = 0;
        d.forEach(function (d) {
            cnt += countChildren(d);
        });
        return cnt;
    }

    function update(source) {
        // Compute the new height, function counts total children of root node and sets tree height accordingly.
        // This prevents the layout looking squashed when new nodes are made visible or looking sparse when nodes are removed
        // This makes the layout more consistent.
        var levelWidth = [1];
        var childCount = function (level, n) {
            if (n.children && n.children.length > 0) {
                if (levelWidth.length <= level + 1) levelWidth.push(0);

                levelWidth[level + 1] += n.children.length;
                n.children.forEach(function (d) {
                    childCount(level + 1, d);
                });
            }
        };
        childCount(0, root);
        var newHeight = d3.max(levelWidth) * 30; // 25 pixels per line
        tree = tree.size([newHeight, viewerWidth]);

        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse(),
            links = tree.links(nodes);

        // Set widths between levels based on maxLabelLength.
        nodes.forEach(function (d) {
            d.y = (d.depth * (maxLabelLength * 10)); //maxLabelLength * 10px
            // alternatively to keep a fixed scale one can set a fixed depth per level
            // Normalize for fixed-depth by commenting out below line
            // d.y = (d.depth * 500); //500px per level.
        });

        // Update the nodes…
        node = svgGroup.selectAll("g.node")
            .data(nodes, function (d) {
                return d.id || (d.id = ++i);
            });

        var div = d3.select("body")
            .append("div")  // declare the tooltip div
            .attr("class", "uib-tooltip")              // apply the 'tooltip' class
            .style("opacity", 0);

        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node.enter().append("g")
            //.call(dragListener)
            .attr("class", "node")
            .attr("transform", function (d) {
                return "translate(" + source.y0 + "," + source.x0 + ")";
            })
            .attr("id",function (d) {
                return 'custom' + d.id;
            })
            .on('click', click)
            .on('mouseover', mouseover)
            .on('mouseout', mouseout);

        nodeEnter.append("circle")
            .attr('class', 'nodeCircle')
            .attr("r", 0)
            .attr('stroke-width',1.5);

        nodeEnter.append("text")
            .attr("x", function (d) {
                return d.children || d._children ? -10 : 10;
            })
            .attr("dy", ".35em")
            .attr('class', 'nodeText')
            .attr("text-anchor", function (d) {
                return d.children || d._children ? "end" : "start";
            })
            //.attr("text-anchor","start")
            .text(function (d) {
                return d.name;
            })
            .style("fill-opacity", 0);

        // Update the text to reflect whether node has children or not.
        node.select('text')
            .attr("x", function (d) {
                return d.children ? -10 : 10;
            })
            .attr("text-anchor", function (d) {
                return d.children ? "end" : "start";
            })
            .text(function (d) {
                return d.name;
            });

        nodeEnter.append("foreignObject")
            .attr("x", function (d) {
                return -5;
            })
            .attr("y", function (d) {
                return 2;
            })
            .attr("text-anchor", function (d) {
                return d.children ? "end" : "start";
            })
            .append("xhtml:body")
            .html(function (d) {
                return getChildrenCountText(d);
            });

        // Change the circle fill depending on whether it has children and is collapsed
        var nodeSelect = node.select("circle.nodeCircle");
        nodeSelect
            .attr("r", 4.5)
            .style("fill", function(d) {
                if(d._children){
                    return returnLevelColor(d.level);
                }
            })
            .style("stroke", function (d) {
                return d.tag == 'inactive' ? "darkgrey" : returnLevelColor(d.level);
            });

        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function (d) {
                return "translate(" + d.y + "," + d.x + ")";
            });

        // Fade the text in
        nodeUpdate.select("text")
            .style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function (d) {
                return "translate(" + source.y + "," + source.x + ")";
            })
            .remove();

        nodeExit.select("circle")
            .attr("r", 0);

        nodeExit.select("text")
            .style("fill-opacity", 0);

        // Update the links…
        var link = svgGroup.selectAll("path.link")
            .data(links, function (d) {
                try {
                    return d.target.id;
                }catch(err){
                    return 0;
                }
            });

        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function (d) {
                var o = {
                    x: source.x0,
                    y: source.y0
                };
                return diagonal({
                    source: o,
                    target: o
                });
            });

        // Transition links to their new position.
        link.transition()
            .duration(duration)
            .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function (d) {
                var o = {
                    x: source.x,
                    y: source.y
                };
                return diagonal({
                    source: o,
                    target: o
                });
            })
            .remove();
        // Stash the old positions for transition.
        nodes.forEach(function (d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });

        node.each(function(d){
            if (d.name == "dummy")
                d3.select(this).remove();});
        link.each(function(d){
            if (d.source.name == "dummy")
                d3.select(this).remove();});
    }

    var svgGroup = baseSvg.append("g");
    root = treeData;
    root.x0 = viewerHeight / 2;
    root.y0 = viewerWidth / 8;

    collapseAll(root.children);
    expandAll(root.children,hr.sl);
    update(root);
    centerNode(root);
    setTimeout(function(){ drawExtra(root) }, 1000);
    setTimeout(function(){ drawMultiParentLink(root) }, 2000);
    return maxLabelLength;
}