$(document).ready(function() {
    $.getJSON('/geo').done(function(data) {
        $('.name').append(data.name);
        $('.latitude').append(data.latitude);
        $('.longitude').append(data.longitude);
    });
});