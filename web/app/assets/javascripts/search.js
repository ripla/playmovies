$(function() {
    // add a click handler to the button
    $("#search").change(function(event) {
        // make an ajax get request to get the message
        jsRoutes.controllers.SearchController.search().ajax({
            success: function(data) {
                console.log(data)
                //$(".well").append($("<h1>").text(data.value))
            }
        })
    })
})