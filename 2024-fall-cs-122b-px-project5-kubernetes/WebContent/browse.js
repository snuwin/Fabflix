$(document).ready(function() {
    loadGenres();
    loadAlphanumericLinks();

    $('#genres_list').on('click', '.genre-link', function(event) {
        event.preventDefault();
        const genre = $(this).data('genre');
        window.location.href = `index.html?genre=${genre}`;
    });

    $('#letters_list').on('click', '.letter-link', function(event) {
        event.preventDefault();
        const letter = $(this).data('letter');
        window.location.href = `index.html?letter=${letter}`;
    });
});


function loadGenres() {
    jQuery.ajax({
        url: 'api/browse',
        method: 'GET',
        success: function(response) {
            const genresList = $('#genres_list');
            genresList.empty();
            response.forEach(genre => {
                genresList.append(`<a href="#" class="list-group-item genre-link" data-genre="${genre.genre}">${genre.genre}</a>`);
            });
        },
        error: function(error) {
            console.error('Error loading genres:', error);
        }
    });
}

function loadAlphanumericLinks() {
    const letters = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*';
    const lettersList = $('#letters_list');
    lettersList.empty();

    for (let i = 0; i < letters.length; i++) {
        const letter = letters[i];
        lettersList.append(`<a href="#" class="list-group-item letter-link" data-letter="${letter}">${letter}</a>`);
    }
}
