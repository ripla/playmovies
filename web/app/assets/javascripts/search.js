$(function() {
    // add a click handler to the button
    $("#search").change(function() {

        // make an ajax get request to get the message
        jsRoutes.controllers.SearchController.search(event.target.value).ajax({
            success: function(result) {
                console.log(result);
                //$(".well").append($("<h1>").text(data.value))
                $("#result-body")
                    .append($("<tr>")
                        .append($("<td>").text(result.name))
                        .append($("<td>").text(result.year))
                        .append($("<td>").text(result.rating))
                        .append($("<td>").text(result.service))
                    );
            }
        });
    });
});